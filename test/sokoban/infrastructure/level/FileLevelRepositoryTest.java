package sokoban.infrastructure.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sokoban.application.port.out.LevelLoadException;
import sokoban.domain.Level;

class FileLevelRepositoryTest {

    @TempDir
    Path folder;

    @Test
    void readsACollectionFromAFile() throws IOException {
        Path file = folder.resolve("Mis niveles.sok");
        Files.writeString(file, String.join("\n",
                "; Primero", "#####", "#@$.#", "#####", "",
                "; Segundo", "######", "#@ $.#", "######"), StandardCharsets.UTF_8);

        FileLevelRepository repository = new FileLevelRepository(file);
        List<Level> levels = repository.findAll();

        assertEquals("Mis niveles", repository.collectionName());
        assertEquals(List.of("Primero", "Segundo"), levels.stream().map(Level::name).toList());
    }

    @Test
    void acceptsLatin1Files() throws IOException {
        Path file = folder.resolve("latin1.txt");
        Files.writeString(file, "Pequeño\n#####\n#@$.#\n#####\n", StandardCharsets.ISO_8859_1);

        assertEquals("Pequeño", new FileLevelRepository(file).findAll().get(0).name());
    }

    @Test
    void failsWithPortExceptionWhenTheFileDoesNotExist() {
        FileLevelRepository repository = new FileLevelRepository(folder.resolve("no-existe.txt"));

        assertThrows(LevelLoadException.class, repository::findAll);
    }
}
