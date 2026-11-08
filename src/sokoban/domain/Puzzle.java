package sokoban.domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Una partida en curso de un nivel. Es la <em>raíz del agregado</em> del
 * dominio: contiene las reglas del juego y es la única clase que puede
 * cambiar el estado.
 *
 * <p>Decisiones de diseño:</p>
 * <ul>
 *   <li><b>Modelo de dominio rico.</b> Las reglas (mover, empujar, deshacer,
 *       detectar la victoria) están aquí y no en un controlador. Se pueden
 *       probar sin ventanas ni ficheros.</li>
 *   <li><b>Memento para deshacer.</b> Cada movimiento apila el
 *       {@link GameState} anterior (inmutable). Deshacer es desapilar, sin
 *       copias de matrices.</li>
 *   <li><b>Contadores coherentes.</b> Los movimientos son el tamaño del
 *       historial, así que deshacer descuenta el movimiento deshecho. Los
 *       empujes también se descuentan. Los "deshacer" se cuentan aparte.</li>
 *   <li><b>Solución reproducible.</b> El historial guarda cada {@link Step},
 *       así que la partida se puede exportar y volver a reproducir.</li>
 * </ul>
 *
 * <p>No es thread-safe: lo usa un único hilo (el de Swing).</p>
 */
public final class Puzzle {

    /** Lo que se apila en cada movimiento: el estado previo y el paso dado. */
    private record HistoryEntry(GameState previous, Step step) { }

    private final Board board;
    private final GameState initialState;
    private final Deque<HistoryEntry> history = new ArrayDeque<>();

    private GameState state;
    private int pushes;
    private int undos;

    Puzzle(Board board, GameState initialState) {
        this.board = Objects.requireNonNull(board, "board");
        this.initialState = Objects.requireNonNull(initialState, "initialState");
        this.state = initialState;
    }

    /**
     * Intenta mover al jugador. Reglas:
     * <ol>
     *   <li>Si la casilla destino está libre, el jugador avanza.</li>
     *   <li>Si hay una caja y la casilla de detrás está libre (transitable y
     *       sin otra caja), la caja se empuja y el jugador avanza.</li>
     *   <li>En cualquier otro caso (o si ya está resuelto) no pasa nada.</li>
     * </ol>
     */
    public MoveResult move(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        if (isSolved()) {
            return MoveResult.BLOCKED;
        }
        Position target = state.player().step(direction);

        if (isFree(target)) {
            apply(state.withPlayerAt(target), new Step(direction, false));
            return MoveResult.MOVED;
        }

        if (state.hasBoxAt(target)) {
            Position beyondBox = target.step(direction);
            if (isFree(beyondBox)) {
                apply(state.withBoxPushed(target, target, beyondBox), new Step(direction, true));
                return MoveResult.PUSHED;
            }
        }
        return MoveResult.BLOCKED;
    }

    /**
     * Deshace el último movimiento.
     *
     * @return {@code false} si no había nada que deshacer
     */
    public boolean undo() {
        HistoryEntry last = history.pollFirst();
        if (last == null) {
            return false;
        }
        state = last.previous();
        if (last.step().push()) {
            pushes--;
        }
        undos++;
        return true;
    }

    /** Vuelve al estado inicial y pone todos los contadores a cero. */
    public void restart() {
        history.clear();
        state = initialState;
        pushes = 0;
        undos = 0;
    }

    /** Resuelto cuando cada caja está sobre un objetivo. */
    public boolean isSolved() {
        return board.goals().containsAll(state.boxes());
    }

    public Board board() {
        return board;
    }

    public GameState state() {
        return state;
    }

    public int moves() {
        return history.size();
    }

    public int pushes() {
        return pushes;
    }

    public int undos() {
        return undos;
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    /** Pasos dados desde el inicio, en orden cronológico (sin los deshechos). */
    public List<Step> steps() {
        List<Step> steps = new ArrayList<>(history.size());
        Iterator<HistoryEntry> oldestFirst = history.descendingIterator();
        while (oldestFirst.hasNext()) {
            steps.add(oldestFirst.next().step());
        }
        return List.copyOf(steps);
    }

    // --- Detalles privados -------------------------------------------------

    /** Una casilla está libre si es transitable y no hay una caja encima. */
    private boolean isFree(Position position) {
        return board.isWalkable(position) && !state.hasBoxAt(position);
    }

    private void apply(GameState next, Step step) {
        history.push(new HistoryEntry(state, step));
        state = next;
        if (step.push()) {
            pushes++;
        }
    }
}
