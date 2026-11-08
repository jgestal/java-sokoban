package sokoban.infrastructure.level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import sokoban.application.port.out.LevelLoadException;
import sokoban.application.port.out.LevelRepository;
import sokoban.domain.Level;

/**
 * Adaptador de salida que implementa {@link LevelRepository} leyendo un
 * fichero de texto del <em>classpath</em>.
 *
 * <p>Leer del classpath, y no de una ruta relativa al directorio de trabajo,
 * hace que funcione igual desde el IDE, la terminal o el jar empaquetado.</p>
 *
 * <p>Esta clase solo hace E/S; el análisis del texto lo hace
 * {@link LevelCollectionParser} (responsabilidad única).</p>
 */
public final class ClasspathLevelRepository implements LevelRepository {

    /** Ubicación por defecto de los niveles dentro del classpath. */
    public static final String DEFAULT_RESOURCE = "/resources/levels/levels.txt";

    /** Nombre de la colección incluida en el juego. */
    public static final String DEFAULT_COLLECTION_NAME = "Microban";

    private final String resourcePath;
    private final String collectionName;
    private final LevelCollectionParser parser = new LevelCollectionParser();

    /** Colección con nombre por defecto ({@value #DEFAULT_COLLECTION_NAME}). */
    public ClasspathLevelRepository(String resourcePath) {
        this(resourcePath, DEFAULT_COLLECTION_NAME);
    }

    public ClasspathLevelRepository(String resourcePath, String collectionName) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
        this.collectionName = Objects.requireNonNull(collectionName, "collectionName");
    }

    @Override
    public String collectionName() {
        return collectionName;
    }

    @Override
    public List<Level> findAll() {
        try (InputStream in = ClasspathLevelRepository.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new LevelLoadException("No se encuentra el recurso de niveles: " + resourcePath);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return parser.parse(reader.lines().toList());
        } catch (IOException e) {
            // Se traduce la excepción técnica a la del puerto: la capa de
            // aplicación no debe conocer IOException.
            throw new LevelLoadException("Error leyendo " + resourcePath, e);
        }
    }
}
