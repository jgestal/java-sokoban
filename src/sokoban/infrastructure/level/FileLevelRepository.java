package sokoban.infrastructure.level;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import sokoban.application.port.out.LevelLoadException;
import sokoban.application.port.out.LevelRepository;
import sokoban.domain.Level;

/**
 * Adaptador de salida que lee una colección de niveles de un fichero elegido
 * por el jugador ({@code .txt}, {@code .sok}, {@code .xsb}…).
 *
 * <p>Usa el mismo {@link LevelCollectionParser} que la colección incluida, así
 * que acepta cualquier fichero en formato XSB con los niveles separados por
 * líneas en blanco. Lee UTF-8 y, si el fichero no lo es, ISO-8859-1, que es
 * la codificación de muchas colecciones antiguas.</p>
 */
public final class FileLevelRepository implements LevelRepository {

    private final Path file;
    private final LevelCollectionParser parser = new LevelCollectionParser();

    public FileLevelRepository(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    @Override
    public List<Level> findAll() {
        return parser.parse(readLines());
    }

    /** El nombre del fichero sin extensión. */
    @Override
    public String collectionName() {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private List<String> readLines() {
        try {
            try {
                return Files.readAllLines(file, StandardCharsets.UTF_8);
            } catch (CharacterCodingException notUtf8) {
                return Files.readAllLines(file, StandardCharsets.ISO_8859_1);
            }
        } catch (NoSuchFileException e) {
            throw new LevelLoadException("No existe el fichero " + file, e);
        } catch (IOException e) {
            throw new LevelLoadException("Error leyendo " + file + ": " + e.getMessage(), e);
        }
    }
}
