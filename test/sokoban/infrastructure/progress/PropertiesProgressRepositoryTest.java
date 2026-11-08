package sokoban.infrastructure.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sokoban.application.Score;

class PropertiesProgressRepositoryTest {

    @TempDir
    Path folder;

    @Test
    void progressSurvivesARestart() {
        Path file = folder.resolve("sub/progress.properties");
        PropertiesProgressRepository first = new PropertiesProgressRepository(file);
        first.saveBestScore("abc123", new Score(12, 3, "rrUUlD"));
        first.saveLastLevel("Microban", 44);

        PropertiesProgressRepository second = new PropertiesProgressRepository(file);

        assertEquals(Optional.of(new Score(12, 3, "rrUUlD")), second.bestScore("abc123"));
        assertEquals(OptionalInt.of(44), second.lastLevel("Microban"));
        assertTrue(second.bestScore("otro").isEmpty());
    }

    @Test
    void ignoresCorruptEntries() throws IOException {
        Path file = folder.resolve("progress.properties");
        Files.writeString(file, "best.abc=no-es-un-numero\nlast.Microban=x\n");

        PropertiesProgressRepository repository = new PropertiesProgressRepository(file);

        assertTrue(repository.bestScore("abc").isEmpty());
        assertTrue(repository.lastLevel("Microban").isEmpty());
    }
}
