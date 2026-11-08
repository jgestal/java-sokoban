package sokoban;

import java.io.IOException;
import java.lang.System.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import sokoban.application.GameService;
import sokoban.application.port.out.LevelLoadException;
import sokoban.application.port.out.LevelRepository;
import sokoban.application.port.out.ProgressRepository;
import sokoban.infrastructure.audio.SoundPlayer;
import sokoban.infrastructure.audio.SynthesizedSoundPlayer;
import sokoban.infrastructure.level.ClasspathLevelRepository;
import sokoban.infrastructure.level.FileLevelRepository;
import sokoban.infrastructure.progress.PropertiesProgressRepository;
import sokoban.infrastructure.ui.swing.ShapeTileRenderer;
import sokoban.infrastructure.ui.swing.SpriteTileRenderer;
import sokoban.infrastructure.ui.swing.SwingGameWindow;
import sokoban.infrastructure.ui.swing.TileRenderer;

/**
 * Punto de entrada y <b>raíz de composición</b> (composition root).
 *
 * <p>Es el único sitio que conoce las clases concretas de todas las capas y
 * las conecta. El resto del código solo depende de interfaces, así que
 * cambiar una pieza (otra fuente de niveles, otra interfaz gráfica) solo
 * afecta a este método. No hace falta un framework de inyección de
 * dependencias para una aplicación de este tamaño (KISS).</p>
 *
 * <p>Uso: {@code java -cp build/classes sokoban.App [número-de-nivel]}. Sin
 * número, el juego continúa en el último nivel jugado.</p>
 */
public final class App {

    public static final String GAME_TITLE = "Sokoban";

    private static final Logger LOG = System.getLogger(App.class.getName());

    private App() {
        // No instanciable: solo contiene main.
    }

    public static void main(String[] args) {
        // 1. Infraestructura de datos -> caso de uso.
        GameService game;
        try {
            LevelRepository levels = new ClasspathLevelRepository(ClasspathLevelRepository.DEFAULT_RESOURCE);
            ProgressRepository progress = new PropertiesProgressRepository(PropertiesProgressRepository.defaultFile());
            game = new GameService(levels, progress);
            if (args.length > 0) {
                game.goToLevel(startLevel(args[0], game.levelCount()));
            }
        } catch (LevelLoadException e) {
            LOG.log(Logger.Level.ERROR, "No se pudieron cargar los niveles", e);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "No se pudieron cargar los niveles:\n" + e.getMessage(),
                    GAME_TITLE, JOptionPane.ERROR_MESSAGE));
            return;
        }

        // 2. Interfaz gráfica: se construye en el hilo de Swing (EDT).
        TileRenderer renderer = createRenderer();
        SoundPlayer sounds = SynthesizedSoundPlayer.createOrSilent();
        SwingUtilities.invokeLater(() -> {
            SwingGameWindow window = new SwingGameWindow(GAME_TITLE, game, renderer, sounds, FileLevelRepository::new);
            game.addListener(window); // el observador recibe enseguida el estado inicial
            window.show();
        });
    }

    /** Sprites si están disponibles; si no, figuras geométricas (degradación elegante). */
    private static TileRenderer createRenderer() {
        try {
            return SpriteTileRenderer.fromClasspath(SpriteTileRenderer.DEFAULT_DIRECTORY);
        } catch (IOException e) {
            LOG.log(Logger.Level.WARNING, "Sprites no disponibles, se usan figuras: {0}", e.getMessage());
            return new ShapeTileRenderer();
        }
    }

    /** Nivel inicial desde la línea de comandos (el 1 si el valor no es válido). */
    private static int startLevel(String arg, int levelCount) {
        try {
            int requested = Integer.parseInt(arg);
            if (requested >= 1 && requested <= levelCount) {
                return requested;
            }
        } catch (NumberFormatException e) {
            // Se ignora y se cae al aviso de abajo.
        }
        LOG.log(Logger.Level.WARNING, "Nivel inicial no válido ''{0}'', se empieza en el 1", arg);
        return 1;
    }
}
