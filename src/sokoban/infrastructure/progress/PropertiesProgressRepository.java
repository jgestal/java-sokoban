package sokoban.infrastructure.progress;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.System.Logger;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Properties;
import sokoban.application.Score;
import sokoban.application.port.out.ProgressRepository;

/**
 * Adaptador de salida que guarda el progreso en un fichero {@code .properties}.
 *
 * <p>Por defecto el fichero es {@code ~/.sokoban/progress.properties}. Se lee
 * entero al arrancar y se reescribe en cada cambio (son pocos datos). Para no
 * dejar un fichero a medias si el programa se cierra mientras escribe, se
 * escribe primero en un temporal y después se mueve encima del original.</p>
 *
 * <p>Formato:</p>
 * <pre>
 * best.&lt;huella-del-nivel&gt;=movimientos,empujes,solución-LURD
 * last.&lt;colección&gt;=número-de-nivel
 * </pre>
 *
 * <p>Los errores de lectura o escritura se registran y no se propagan: la
 * partida sigue aunque no se pueda guardar.</p>
 */
public final class PropertiesProgressRepository implements ProgressRepository {

    private static final Logger LOG = System.getLogger(PropertiesProgressRepository.class.getName());

    private static final String BEST_PREFIX = "best.";
    private static final String LAST_PREFIX = "last.";

    private final Path file;
    private final Properties data = new Properties();

    public PropertiesProgressRepository(Path file) {
        this.file = Objects.requireNonNull(file, "file");
        load();
    }

    /** {@code ~/.sokoban/progress.properties} */
    public static Path defaultFile() {
        return Path.of(System.getProperty("user.home"), ".sokoban", "progress.properties");
    }

    @Override
    public Optional<Score> bestScore(String levelId) {
        String value = data.getProperty(BEST_PREFIX + levelId);
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(",", 3);
        try {
            return Optional.of(new Score(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), parts[2]));
        } catch (RuntimeException e) {
            LOG.log(Logger.Level.WARNING, "Récord ilegible para {0}: {1}", levelId, value);
            return Optional.empty();
        }
    }

    @Override
    public void saveBestScore(String levelId, Score score) {
        data.setProperty(BEST_PREFIX + levelId, score.moves() + "," + score.pushes() + "," + score.solution());
        store();
    }

    @Override
    public OptionalInt lastLevel(String collectionName) {
        String value = data.getProperty(LAST_PREFIX + collectionName);
        if (value == null) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(value.strip()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    @Override
    public void saveLastLevel(String collectionName, int levelNumber) {
        data.setProperty(LAST_PREFIX + collectionName, Integer.toString(levelNumber));
        store();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            data.load(reader);
        } catch (IOException | IllegalArgumentException e) {
            LOG.log(Logger.Level.WARNING, "No se pudo leer el progreso de {0}: {1}", file, e.getMessage());
        }
    }

    private void store() {
        try {
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "progress", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                data.store(writer, "Progreso de Sokoban");
            }
            move(temporary);
        } catch (IOException e) {
            LOG.log(Logger.Level.WARNING, "No se pudo guardar el progreso en {0}: {1}", file, e.getMessage());
        }
    }

    private void move(Path temporary) throws IOException {
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
