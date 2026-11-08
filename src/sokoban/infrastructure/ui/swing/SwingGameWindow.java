package sokoban.infrastructure.ui.swing;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import sokoban.application.GameSnapshot;
import sokoban.application.port.in.GameCommands;
import sokoban.application.port.out.GameListener;
import sokoban.application.port.out.LevelRepository;
import sokoban.infrastructure.audio.SoundPlayer;

/**
 * Ventana principal. Es el <b>adaptador</b> de interfaz gráfica: implementa
 * el puerto de salida {@link GameListener} (recibe fotos de la partida) y
 * envía las órdenes del jugador al puerto de entrada {@link GameCommands}.
 *
 * <p>Decisiones de diseño:</p>
 * <ul>
 *   <li><b>Composición en vez de herencia.</b> Usa un {@code JFrame}, no
 *       hereda de él. Así no expone decenas de métodos de {@code JFrame} que
 *       nada tienen que ver con el juego.</li>
 *   <li><b>Hilo de Swing (EDT).</b> {@link #onGameChanged} puede llamarse
 *       desde cualquier hilo. Si no es el EDT, se reenvía con
 *       {@code invokeLater}, porque los componentes Swing no son
 *       thread-safe.</li>
 *   <li><b>Ritmo de la reproducción.</b> La aplicación sabe qué pasos quedan;
 *       la ventana decide cada cuánto se da uno con un {@link Timer} de
 *       Swing, que dispara en el EDT.</li>
 * </ul>
 */
public final class SwingGameWindow implements GameListener {

    private static final int REPLAY_STEP_MILLIS = 160;

    private final String appTitle;
    private final JFrame frame;
    private final BoardPanel boardPanel;
    private final InfoPanel infoPanel;
    private final GameActions actions;
    private final SoundPlayer sounds;
    private final Timer replayTimer;

    /** Última foto recibida; solo se lee y escribe desde el EDT. */
    private GameSnapshot current;

    /**
     * Debe construirse en el EDT.
     *
     * @param appTitle         título base de la ventana
     * @param commands         órdenes del juego
     * @param renderer         cómo pintar las casillas
     * @param sounds           cómo reproducir los efectos de sonido
     * @param collectionOpener crea el repositorio de una colección elegida por el jugador
     */
    public SwingGameWindow(String appTitle, GameCommands commands, TileRenderer renderer,
                           SoundPlayer sounds, Function<Path, LevelRepository> collectionOpener) {
        this.appTitle = Objects.requireNonNull(appTitle, "appTitle");
        this.sounds = Objects.requireNonNull(sounds, "sounds");
        this.frame = new JFrame(appTitle);
        this.boardPanel = new BoardPanel(renderer);
        this.actions = new GameActions(commands, () -> current, frame, collectionOpener, this::notifyPlayer);
        this.infoPanel = new InfoPanel(actions);
        this.replayTimer = new Timer(REPLAY_STEP_MILLIS, event -> {
            if (!commands.replayStep()) {
                ((Timer) event.getSource()).stop();
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(menuBar());
        frame.setLayout(new BorderLayout());
        frame.add(infoPanel, BorderLayout.LINE_START);
        frame.add(boardPanel, BorderLayout.CENTER);
        actions.installKeyBindings(frame.getRootPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /** Hace visible la ventana. */
    public void show() {
        frame.setVisible(true);
    }

    @Override
    public void onGameChanged(GameSnapshot snapshot) {
        if (SwingUtilities.isEventDispatchThread()) {
            render(snapshot);
        } else {
            SwingUtilities.invokeLater(() -> render(snapshot));
        }
    }

    private void notifyPlayer(String message) {
        infoPanel.notify(message);
    }

    private JMenuBar menuBar() {
        JMenu game = new JMenu("Juego");
        game.add(actions.openCollection);
        game.add(actions.goToLevel);
        game.addSeparator();
        game.add(new JCheckBoxMenuItem(actions.toggleSound));

        JMenu solution = new JMenu("Solución");
        solution.add(actions.replaySolution);
        solution.add(actions.copySolution);
        solution.add(actions.stopReplay);

        JMenuBar bar = new JMenuBar();
        bar.add(game);
        bar.add(solution);
        return bar;
    }

    private void render(GameSnapshot snapshot) {
        current = snapshot;
        frame.setTitle(appTitle + " — " + snapshot.collectionName() + " — " + snapshot.levelName());
        actions.refresh(snapshot);
        infoPanel.show(snapshot);
        boardPanel.show(snapshot);
        if (actions.soundEnabled()) {
            SoundCue.forSnapshot(snapshot).ifPresent(sounds::play);
        }
        if (snapshot.replaying() && !replayTimer.isRunning()) {
            replayTimer.start();
        } else if (!snapshot.replaying()) {
            replayTimer.stop();
        }
    }
}
