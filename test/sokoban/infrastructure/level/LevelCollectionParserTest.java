package sokoban.infrastructure.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import sokoban.domain.Level;

class LevelCollectionParserTest {

    private final LevelCollectionParser parser = new LevelCollectionParser();

    @Test
    void splitsLevelsByBlankLinesAndJoinsTitles() {
        List<Level> levels = parser.parse(List.of(
                "Level 1",
                "#####",
                "#@$.#",
                "#####",
                "",
                "Level 44",
                "'Duh!'",
                "#####",
                "#.$@#",
                "#####"));

        assertEquals(2, levels.size());
        assertEquals("Level 1", levels.get(0).name());
        assertEquals("Level 44 - Duh!", levels.get(1).name());
    }

    @Test
    void skipsInvalidLevelsButKeepsTheRest() {
        List<Level> levels = parser.parse(List.of(
                "Broken",
                "#$.#",       // sin jugador
                "",
                "Fine",
                "#@$.#"));

        assertEquals(1, levels.size());
        assertEquals("Fine", levels.get(0).name());
    }

    @Test
    void usesFallbackNameWhenThereIsNoTitle() {
        List<Level> levels = parser.parse(List.of("#@$.#"));

        assertEquals("Nivel 1", levels.get(0).name());
    }

    @Test
    void usesSemicolonCommentsAsTitles() {
        List<Level> levels = parser.parse(List.of(
                "; 12",
                ";",
                "#####",
                "#@$.#",
                "#####"));

        assertEquals("12", levels.get(0).name());
    }

    @Test
    void recognisesBoardRows() {
        assertTrue(LevelCollectionParser.isBoardRow("  # @$ #"));
        assertFalse(LevelCollectionParser.isBoardRow("Level 1"));
        assertFalse(LevelCollectionParser.isBoardRow("   "));
    }
}
