package sokoban.infrastructure.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sokoban.domain.InvalidLevelException;
import sokoban.domain.Level;
import sokoban.domain.Position;
import sokoban.domain.Terrain;

class XsbBoardParserTest {

    private final XsbBoardParser parser = new XsbBoardParser();

    @Test
    void parsesAllStandardSymbols() {
        Level level = parser.parse("demo", List.of(
                "#######",
                "#+$* .#",
                "#######"));

        assertEquals(new Position(1, 1), level.initialState().player());
        assertEquals(Set.of(new Position(2, 1), new Position(3, 1)), level.initialState().boxes());
        assertEquals(Set.of(new Position(1, 1), new Position(3, 1), new Position(5, 1)), level.board().goals());
        assertEquals(Terrain.FLOOR, level.board().terrainAt(new Position(4, 1)));
    }

    @Test
    void marksUnreachableSpacesAsOutside() {
        Level level = parser.parse("demo", List.of(
                "  ####",
                "  #@$.#",
                "  ####"));

        assertEquals(Terrain.OUTSIDE, level.board().terrainAt(new Position(0, 0)));
        assertEquals(Terrain.OUTSIDE, level.board().terrainAt(new Position(1, 1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-", "_"})
    void acceptsAlternativeFloorSymbols(String floor) {
        Level level = parser.parse("demo", List.of(
                "######",
                "#@" + floor + "$.#",
                "######"));

        assertTrue(level.board().isWalkable(new Position(2, 1)));
    }

    @Test
    void rejectsUnknownSymbols() {
        assertThrows(InvalidLevelException.class, () -> parser.parse("demo", List.of("#@$.X#")));
    }

    @Test
    void rejectsLevelWithoutPlayer() {
        assertThrows(InvalidLevelException.class, () -> parser.parse("demo", List.of("# $.#")));
    }

    @Test
    void rejectsLevelWithTwoPlayers() {
        assertThrows(InvalidLevelException.class, () -> parser.parse("demo", List.of("#@@$.#")));
    }
}
