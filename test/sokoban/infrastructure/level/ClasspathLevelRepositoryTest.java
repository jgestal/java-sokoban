package sokoban.infrastructure.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import sokoban.application.port.out.LevelLoadException;
import sokoban.domain.Level;

/** Test de integración: lee el fichero real de niveles del classpath. */
class ClasspathLevelRepositoryTest {

    @Test
    void loadsEveryLevelOfTheBundledCollection() {
        List<Level> levels = new ClasspathLevelRepository(ClasspathLevelRepository.DEFAULT_RESOURCE).findAll();

        assertEquals(155, levels.size(), "levels.txt contiene 155 niveles y todos deben ser válidos");
        assertEquals("Level 1", levels.get(0).name());
        assertEquals("Level 155 - The Dungeon", levels.get(154).name());
    }

    @Test
    void failsWithPortExceptionWhenResourceIsMissing() {
        ClasspathLevelRepository repository = new ClasspathLevelRepository("/no/existe.txt");

        assertThrows(LevelLoadException.class, repository::findAll);
    }
}
