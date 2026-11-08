package sokoban.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sokoban.domain.LevelFixture.level;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LevelTest {

    @Test
    void rejectsMoreBoxesThanGoals() {
        assertThrows(InvalidLevelException.class, () -> level(
                "######",
                "#@$$.#",
                "######"));
    }

    @Test
    void rejectsLevelWithoutBoxes() {
        assertThrows(InvalidLevelException.class, () -> level(
                "####",
                "#@.#",
                "####"));
    }

    @Test
    void rejectsPlayerOnWall() {
        Board board = new Board(new Terrain[][] {{Terrain.WALL, Terrain.FLOOR, Terrain.GOAL}});
        GameState state = new GameState(new Position(0, 0), Set.of(new Position(1, 0)));

        assertThrows(InvalidLevelException.class, () -> new Level("x", board, state));
    }

    @Test
    void eachPuzzleStartsFromTheInitialStateIndependently() {
        Level level = level(
                "######",
                "#@$ .#",
                "######");
        Puzzle first = level.newPuzzle();
        first.move(Direction.RIGHT);

        Puzzle second = level.newPuzzle();

        assertNotSame(first, second);
        assertEquals(level.initialState(), second.state());
    }

    @Test
    void gameStateDefensivelyCopiesBoxes() {
        Set<Position> boxes = new HashSet<>(Set.of(new Position(1, 1)));
        GameState state = new GameState(new Position(0, 0), boxes);

        boxes.add(new Position(5, 5));

        assertEquals(1, state.boxes().size());
    }

    @Test
    void boardReturnsOutsideBeyondItsLimits() {
        Board board = new Board(new Terrain[][] {{Terrain.FLOOR}});

        assertEquals(Terrain.OUTSIDE, board.terrainAt(new Position(-1, 0)));
        assertEquals(Terrain.OUTSIDE, board.terrainAt(new Position(0, 7)));
    }
}
