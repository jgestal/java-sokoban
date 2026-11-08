package sokoban.application;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import sokoban.application.port.in.GameCommands;
import sokoban.application.port.out.GameListener;
import sokoban.application.port.out.LevelLoadException;
import sokoban.application.port.out.LevelRepository;
import sokoban.application.port.out.ProgressRepository;
import sokoban.domain.Direction;
import sokoban.domain.Level;
import sokoban.domain.MoveResult;
import sokoban.domain.Position;
import sokoban.domain.Puzzle;
import sokoban.domain.Step;

/**
 * Caso de uso principal: jugar una colección de niveles.
 *
 * <p>Responsabilidades (y solo estas):</p>
 * <ul>
 *   <li>Saber qué colección y qué nivel se están jugando, y cambiarlos.</li>
 *   <li>Pasar las órdenes del jugador al {@link Puzzle}, que tiene las reglas.</li>
 *   <li>Guardar récords y el último nivel jugado a través de
 *       {@link ProgressRepository}.</li>
 *   <li>Reproducir soluciones en LURD paso a paso.</li>
 *   <li>Avisar a los {@link GameListener} con una {@link GameSnapshot} cuando
 *       algo cambia.</li>
 * </ul>
 *
 * <p>No crea ventanas, no registra teclas, no lee ficheros y no contiene
 * reglas del juego. Recibe sus dependencias por el constructor (inyección de
 * dependencias), así se puede probar con dobles en memoria y sin interfaz
 * gráfica. Lo usa un único hilo (el de Swing).</p>
 *
 * <p>Una partida en la que se ha reproducido una solución no cuenta para los
 * récords hasta que se reinicia el nivel: el récord tiene que ser del jugador.</p>
 */
public final class GameService implements GameCommands {

    private final ProgressRepository progress;
    private final List<GameListener> listeners = new CopyOnWriteArrayList<>();
    private final Deque<Direction> pendingReplay = new ArrayDeque<>();

    private String collectionName;
    private List<Level> levels;
    private List<String> levelIds;
    private int solvedCount;

    private int currentIndex;
    private Puzzle puzzle;
    /** La partida actual usa una solución reproducida: no cuenta para récords. */
    private boolean assisted;
    private boolean newRecord;

    /**
     * Carga la colección y continúa en el último nivel jugado de ella.
     *
     * @param repository de donde se cargan los niveles
     * @param progress   donde se guardan récords y el último nivel jugado
     * @throws LevelLoadException si el repositorio no devuelve ningún nivel
     */
    public GameService(LevelRepository repository, ProgressRepository progress) {
        this.progress = Objects.requireNonNull(progress, "progress");
        useCollection(Objects.requireNonNull(repository, "repository"));
    }

    /**
     * Registra un observador y le envía enseguida el estado actual, así nunca
     * se queda con la pantalla vacía.
     */
    public void addListener(GameListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        listener.onGameChanged(snapshot(GameEvent.LEVEL_STARTED, null));
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    // --- GameCommands -------------------------------------------------------

    @Override
    public void move(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        boolean wasReplaying = beginCommand();
        if (!applyMove(direction) && wasReplaying) {
            publish(GameEvent.REPLAY_STOPPED, null);
        }
    }

    @Override
    public void undo() {
        boolean wasReplaying = beginCommand();
        if (puzzle.undo()) {
            publish(GameEvent.UNDONE, null);
        } else if (wasReplaying) {
            publish(GameEvent.REPLAY_STOPPED, null);
        }
    }

    @Override
    public void restart() {
        beginCommand();
        puzzle.restart();
        assisted = false;
        publish(GameEvent.RESTARTED, null);
    }

    @Override
    public void nextLevel() {
        if (currentIndex + 1 < levels.size()) {
            changeLevel(currentIndex + 1);
        }
    }

    @Override
    public void previousLevel() {
        if (currentIndex > 0) {
            changeLevel(currentIndex - 1);
        }
    }

    @Override
    public void goToLevel(int levelNumber) {
        if (levelNumber < 1 || levelNumber > levels.size()) {
            throw new IllegalArgumentException(
                    "El nivel debe estar entre 1 y " + levels.size() + ": " + levelNumber);
        }
        changeLevel(levelNumber - 1);
    }

    @Override
    public int levelCount() {
        return levels.size();
    }

    @Override
    public void loadCollection(LevelRepository repository) {
        Objects.requireNonNull(repository, "repository");
        beginCommand();
        useCollection(repository);
        publish(GameEvent.LEVEL_STARTED, null);
    }

    @Override
    public void startReplay(String lurd) {
        List<Step> steps = Lurd.decode(Objects.requireNonNull(lurd, "lurd"));
        if (steps.isEmpty()) {
            throw new InvalidSolutionException("La solución está vacía");
        }
        checkFitsCurrentLevel(steps, !lurd.equals(lurd.toLowerCase(Locale.ROOT)));

        beginCommand();
        puzzle.restart();
        assisted = true;
        steps.forEach(step -> pendingReplay.add(step.direction()));
        publish(GameEvent.RESTARTED, null);
    }

    @Override
    public boolean replayStep() {
        Direction next = pendingReplay.poll();
        if (next == null) {
            return false;
        }
        newRecord = false;
        if (!applyMove(next)) {
            // No debería pasar: la solución se ha comprobado al empezar.
            pendingReplay.clear();
            publish(GameEvent.REPLAY_STOPPED, null);
        }
        return !pendingReplay.isEmpty();
    }

    @Override
    public void stopReplay() {
        if (beginCommand()) {
            publish(GameEvent.REPLAY_STOPPED, null);
        }
    }

    @Override
    public String currentSolution() {
        return Lurd.encode(puzzle.steps());
    }

    @Override
    public Optional<String> bestSolution() {
        return progress.bestScore(currentLevelId()).map(Score::solution);
    }

    /** Estado actual como DTO inmutable. */
    public GameSnapshot snapshot() {
        return snapshot(GameEvent.LEVEL_STARTED, null);
    }

    // --- Detalles privados --------------------------------------------------

    /**
     * Prepara una orden nueva: borra el aviso de récord y detiene la
     * reproducción en curso.
     *
     * @return si había una reproducción en curso
     */
    private boolean beginCommand() {
        newRecord = false;
        boolean wasReplaying = !pendingReplay.isEmpty();
        pendingReplay.clear();
        return wasReplaying;
    }

    /** Mueve, guarda el récord si procede y publica. Devuelve si hubo movimiento. */
    private boolean applyMove(Direction direction) {
        Position from = puzzle.state().player();
        MoveResult result = puzzle.move(direction);
        if (!result.changedState()) {
            return false;
        }
        boolean pushed = result == MoveResult.PUSHED;
        if (puzzle.isSolved()) {
            newRecord = saveIfRecord();
        }
        publish(pushed ? GameEvent.PUSHED : GameEvent.MOVED, new Motion(from.x(), from.y(), direction, pushed));
        return true;
    }

    private boolean saveIfRecord() {
        if (assisted) {
            return false;
        }
        Score score = new Score(puzzle.moves(), puzzle.pushes(), currentSolution());
        Optional<Score> previous = progress.bestScore(currentLevelId());
        if (previous.isPresent() && !score.isBetterThan(previous.get())) {
            return false;
        }
        if (previous.isEmpty()) {
            solvedCount++;
        }
        progress.saveBestScore(currentLevelId(), score);
        return true;
    }

    /**
     * Simula la solución en una partida aparte para no tocar la actual si no
     * vale. Si el texto está todo en minúsculas no se comprueban los empujes:
     * hay soluciones publicadas así.
     */
    private void checkFitsCurrentLevel(List<Step> steps, boolean checkPushes) {
        Puzzle trial = levels.get(currentIndex).newPuzzle();
        for (int i = 0; i < steps.size(); i++) {
            Step expected = steps.get(i);
            String where = "Paso " + (i + 1) + " ('" + Lurd.encode(List.of(expected)) + "')";
            if (trial.isSolved()) {
                throw new InvalidSolutionException("El nivel ya queda resuelto antes del paso " + (i + 1)
                        + ": sobran movimientos");
            }
            MoveResult result = trial.move(expected.direction());
            if (!result.changedState()) {
                throw new InvalidSolutionException(where + ": el movimiento está bloqueado en este nivel");
            }
            if (checkPushes && (result == MoveResult.PUSHED) != expected.push()) {
                throw new InvalidSolutionException(where + ": "
                        + (expected.push() ? "debería empujar una caja y no la hay" : "empuja una caja (debería ir en mayúscula)"));
            }
        }
    }

    /** Carga una colección entera; si falla, el estado anterior no cambia. */
    private void useCollection(LevelRepository repository) {
        List<Level> loaded = List.copyOf(repository.findAll());
        if (loaded.isEmpty()) {
            throw new LevelLoadException("No se ha encontrado ningún nivel válido");
        }
        List<String> ids = loaded.stream().map(LevelFingerprint::of).toList();

        collectionName = repository.collectionName();
        levels = loaded;
        levelIds = ids;
        solvedCount = (int) ids.stream().filter(id -> progress.bestScore(id).isPresent()).count();
        int last = progress.lastLevel(collectionName).orElse(1);
        loadLevel(last >= 1 && last <= levels.size() ? last - 1 : 0);
    }

    private void changeLevel(int index) {
        beginCommand();
        loadLevel(index);
        progress.saveLastLevel(collectionName, index + 1);
        publish(GameEvent.LEVEL_STARTED, null);
    }

    private void loadLevel(int index) {
        currentIndex = index;
        puzzle = levels.get(index).newPuzzle();
        assisted = false;
    }

    private String currentLevelId() {
        return levelIds.get(currentIndex);
    }

    private GameSnapshot snapshot(GameEvent event, Motion motion) {
        Level level = levels.get(currentIndex);
        return new GameSnapshot(
                collectionName,
                currentIndex + 1,
                levels.size(),
                level.name(),
                TileGrid.of(puzzle.board(), puzzle.state()),
                puzzle.moves(),
                puzzle.pushes(),
                puzzle.undos(),
                puzzle.isSolved(),
                solvedCount,
                progress.bestScore(currentLevelId()).orElse(null),
                newRecord,
                event,
                motion,
                !pendingReplay.isEmpty());
    }

    private void publish(GameEvent event, Motion motion) {
        GameSnapshot snapshot = snapshot(event, motion);
        listeners.forEach(listener -> listener.onGameChanged(snapshot));
    }
}
