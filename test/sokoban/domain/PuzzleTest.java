package sokoban.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sokoban.domain.LevelFixture.puzzle;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Puzzle: reglas del juego")
class PuzzleTest {

    @Nested
    @DisplayName("al moverse")
    class Moving {

        @Test
        void movesPlayerToFreeCell() {
            Puzzle puzzle = puzzle(
                    "#####",
                    "#@ .#",
                    "# $ #",
                    "#####");

            MoveResult result = puzzle.move(Direction.RIGHT);

            assertEquals(MoveResult.MOVED, result);
            assertEquals(new Position(2, 1), puzzle.state().player());
            assertEquals(1, puzzle.moves());
            assertEquals(0, puzzle.pushes());
        }

        @Test
        void isBlockedByWallAndDoesNotCountMove() {
            Puzzle puzzle = puzzle(
                    "####",
                    "#@$.",
                    "####");

            MoveResult result = puzzle.move(Direction.UP);

            assertEquals(MoveResult.BLOCKED, result);
            assertEquals(new Position(1, 1), puzzle.state().player());
            assertEquals(0, puzzle.moves());
        }

        @Test
        void walksOverGoalWithoutChangingIt() {
            Puzzle puzzle = puzzle(
                    "######",
                    "#@. $.#",
                    "######");

            puzzle.move(Direction.RIGHT);
            puzzle.move(Direction.RIGHT);

            assertEquals(Terrain.GOAL, puzzle.board().terrainAt(new Position(2, 1)));
        }
    }

    @Nested
    @DisplayName("al empujar")
    class Pushing {

        @Test
        void pushesBoxIntoFreeCell() {
            Puzzle puzzle = puzzle(
                    "######",
                    "#@$ .#",
                    "######");

            MoveResult result = puzzle.move(Direction.RIGHT);

            assertEquals(MoveResult.PUSHED, result);
            assertEquals(new Position(2, 1), puzzle.state().player());
            assertEquals(Set.of(new Position(3, 1)), puzzle.state().boxes());
            assertEquals(1, puzzle.pushes());
        }

        @Test
        void cannotPushBoxIntoWall() {
            Puzzle puzzle = puzzle(
                    "#####",
                    "#.@$#",
                    "#####");

            assertEquals(MoveResult.BLOCKED, puzzle.move(Direction.RIGHT));
            assertEquals(Set.of(new Position(3, 1)), puzzle.state().boxes());
        }

        @Test
        void cannotPushTwoBoxesAtOnce() {
            Puzzle puzzle = puzzle(
                    "#######",
                    "#@$$ ..#",
                    "#######");

            assertEquals(MoveResult.BLOCKED, puzzle.move(Direction.RIGHT));
            assertEquals(0, puzzle.moves());
        }
    }

    @Nested
    @DisplayName("al resolver")
    class Solving {

        @Test
        void isSolvedWhenEveryBoxIsOnAGoal() {
            Puzzle puzzle = puzzle(
                    "#####",
                    "#@$.#",
                    "#####");
            assertFalse(puzzle.isSolved());

            puzzle.move(Direction.RIGHT);

            assertTrue(puzzle.isSolved());
        }

        @Test
        void ignoresMovesOnceSolved() {
            Puzzle puzzle = puzzle(
                    "######",
                    "#@$. #",
                    "######");
            puzzle.move(Direction.RIGHT);

            assertEquals(MoveResult.BLOCKED, puzzle.move(Direction.LEFT));
            assertEquals(1, puzzle.moves());
        }
    }

    @Nested
    @DisplayName("al deshacer y reiniciar")
    class UndoAndRestart {

        @Test
        void undoRestoresPreviousStateAndCounters() {
            Puzzle puzzle = puzzle(
                    "#######",
                    "#@$  .#",
                    "#######");
            GameState initial = puzzle.state();
            puzzle.move(Direction.RIGHT); // empuje

            assertTrue(puzzle.undo());

            assertEquals(initial, puzzle.state());
            assertEquals(0, puzzle.moves());
            assertEquals(0, puzzle.pushes());
            assertEquals(1, puzzle.undos());
        }

        @Test
        void undoWithoutHistoryDoesNothing() {
            Puzzle puzzle = puzzle(
                    "#####",
                    "#@$.#",
                    "#####");

            assertFalse(puzzle.undo());
            assertEquals(0, puzzle.undos());
        }

        @Test
        void undoAfterSolvingAllowsPlayingAgain() {
            Puzzle puzzle = puzzle(
                    "#####",
                    "#@$.#",
                    "#####");
            puzzle.move(Direction.RIGHT);

            puzzle.undo();

            assertFalse(puzzle.isSolved());
        }

        @Test
        void restartGoesBackToInitialStateAndResetsCounters() {
            Puzzle puzzle = puzzle(
                    "#######",
                    "#@$  .#",
                    "#######");
            GameState initial = puzzle.state();
            puzzle.move(Direction.RIGHT);
            puzzle.move(Direction.RIGHT);
            puzzle.undo();

            puzzle.restart();

            assertEquals(initial, puzzle.state());
            assertEquals(0, puzzle.moves());
            assertEquals(0, puzzle.pushes());
            assertEquals(0, puzzle.undos());
            assertFalse(puzzle.canUndo());
        }

        @Test
        void stepsListTheMovesInOrderWithoutTheUndoneOnes() {
            Puzzle puzzle = puzzle(
                    "#######",
                    "#@ $ .#",
                    "#######");
            puzzle.move(Direction.RIGHT);
            puzzle.move(Direction.RIGHT); // empuje
            puzzle.move(Direction.LEFT);
            puzzle.undo();

            assertEquals(List.of(new Step(Direction.RIGHT, false), new Step(Direction.RIGHT, true)),
                    puzzle.steps());
        }
    }
}
