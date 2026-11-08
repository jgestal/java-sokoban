package sokoban.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sokoban.application.port.out.LevelLoadException;
import sokoban.application.port.out.LevelRepository;
import sokoban.domain.Direction;
import sokoban.domain.Level;
import sokoban.domain.LevelFixture;

/**
 * Prueba el caso de uso con dobles en memoria (un repositorio lambda, un
 * progreso en mapas y un listener que graba lo que recibe). No hace falta
 * ninguna librería de mocks gracias a que los puertos son interfaces pequeñas.
 */
class GameServiceTest {

    private static final List<Level> LEVELS = List.of(
            LevelFixture.level("#####", "#@$.#", "#####"),
            LevelFixture.level("######", "#@ $.#", "######"),
            LevelFixture.level("#######", "#@  $.#", "#######"));

    private final List<GameSnapshot> received = new ArrayList<>();
    private final InMemoryProgressRepository progress = new InMemoryProgressRepository();
    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService(collection("Pruebas", LEVELS), progress);
        service.addListener(received::add);
    }

    @Nested
    @DisplayName("al jugar")
    class Playing {

        @Test
        void newListenerImmediatelyReceivesCurrentState() {
            assertEquals(1, received.size());
            assertEquals(1, last().levelNumber());
            assertEquals(3, last().levelCount());
            assertEquals("Pruebas", last().collectionName());
            assertEquals(GameEvent.LEVEL_STARTED, last().event());
        }

        @Test
        void validMovePublishesUpdatedSnapshotWithMotion() {
            service.move(Direction.RIGHT);

            assertEquals(2, received.size());
            assertTrue(last().solved());
            assertEquals(Tile.BOX_ON_GOAL, last().grid().tileAt(3, 1));
            assertEquals(Tile.PLAYER, last().grid().tileAt(2, 1));
            assertEquals(GameEvent.PUSHED, last().event());
            assertEquals(new Motion(1, 1, Direction.RIGHT, true), last().motion());
        }

        @Test
        void blockedMoveDoesNotPublish() {
            service.move(Direction.UP);

            assertEquals(1, received.size());
        }

        @Test
        void undoPublishesWithoutMotion() {
            service.goToLevel(2);
            service.move(Direction.RIGHT);

            service.undo();

            assertEquals(GameEvent.UNDONE, last().event());
            assertNull(last().motion());
            assertEquals(0, last().moves());
        }

        @Test
        void navigatesBetweenLevelsWithinBounds() {
            service.previousLevel(); // ya en el primero: no hace nada
            assertEquals(1, received.size());

            service.nextLevel();
            service.nextLevel();
            service.nextLevel(); // ya en el último: no hace nada

            assertEquals(3, last().levelNumber());
            assertFalse(last().hasNextLevel());
            assertEquals(3, received.size());
        }

        @Test
        void changingLevelStartsAFreshPuzzle() {
            service.goToLevel(2);
            service.move(Direction.RIGHT);
            service.nextLevel();

            service.goToLevel(2);

            assertEquals(0, last().moves());
        }

        @Test
        void goToLevelRejectsOutOfRangeNumbers() {
            assertThrows(IllegalArgumentException.class, () -> service.goToLevel(0));
            assertThrows(IllegalArgumentException.class, () -> service.goToLevel(4));
        }

        @Test
        void solvingLastLevelCompletesTheCollection() {
            service.goToLevel(3); // "#@  $.#": dos pasos y un empuje
            service.move(Direction.RIGHT);
            service.move(Direction.RIGHT);
            service.move(Direction.RIGHT);

            assertTrue(last().allLevelsCompleted());
        }

        @Test
        void failsFastWhenThereAreNoLevels() {
            assertThrows(LevelLoadException.class, () -> new GameService(List::of, progress));
        }
    }

    @Nested
    @DisplayName("récords y progreso")
    class Progress {

        @Test
        void firstSolutionIsSavedAsRecord() {
            service.move(Direction.RIGHT);

            assertTrue(last().newRecord());
            assertEquals(new Score(1, 1, "R"), last().best());
            assertEquals(1, last().solvedCount());
        }

        @Test
        void betterSolutionReplacesTheRecordAndWorseDoesNot() {
            service.goToLevel(2); // "#@ $.#": la óptima es "rR"
            solveLevel2Slowly();
            assertEquals(4, last().best().moves());

            service.restart();
            service.move(Direction.RIGHT);
            service.move(Direction.RIGHT);
            assertTrue(last().newRecord());
            assertEquals(new Score(2, 1, "rR"), last().best());

            service.restart();
            solveLevel2Slowly();
            assertFalse(last().newRecord());
            assertEquals(2, last().best().moves());
            assertEquals(1, last().solvedCount(), "el mismo nivel no cuenta dos veces");
        }

        @Test
        void recordIsKnownWhenComingBackToTheLevel() {
            service.move(Direction.RIGHT);
            service.nextLevel();

            service.previousLevel();

            assertEquals(new Score(1, 1, "R"), last().best());
            assertFalse(last().newRecord());
            assertEquals(Optional.of("R"), service.bestSolution());
        }

        @Test
        void resumesTheLastPlayedLevelOfTheCollection() {
            service.goToLevel(3);

            GameService restarted = new GameService(collection("Pruebas", LEVELS), progress);

            assertEquals(3, restarted.snapshot().levelNumber());
        }

        @Test
        void currentSolutionIsWrittenInLurd() {
            service.goToLevel(3);
            service.move(Direction.RIGHT);
            service.move(Direction.RIGHT);

            assertEquals("rr", service.currentSolution());
        }

        /** Resuelve "#@ $.#" con rodeo: r, l, r, R (4 movimientos, 1 empuje). */
        private void solveLevel2Slowly() {
            service.move(Direction.RIGHT);
            service.move(Direction.LEFT);
            service.move(Direction.RIGHT);
            service.move(Direction.RIGHT);
            assertTrue(last().solved());
        }
    }

    @Nested
    @DisplayName("al reproducir soluciones")
    class Replay {

        @Test
        void replaysTheSolutionStepByStep() {
            service.goToLevel(3);
            service.move(Direction.RIGHT); // la reproducción reinicia el nivel

            service.startReplay("rrR");
            assertTrue(last().replaying());
            assertEquals(0, last().moves());

            assertTrue(service.replayStep());
            assertTrue(service.replayStep());
            assertFalse(service.replayStep());

            assertTrue(last().solved());
            assertFalse(last().replaying());
            assertEquals(GameEvent.PUSHED, last().event());
        }

        @Test
        void acceptsSolutionsWrittenAllInLowerCase() {
            service.goToLevel(3);

            service.startReplay("rrr");
            while (service.replayStep()) {
                // avanza hasta el final
            }

            assertTrue(last().solved());
        }

        @Test
        void replayedSolutionDoesNotCountAsRecord() {
            service.startReplay("R");
            service.replayStep();

            assertTrue(last().solved());
            assertFalse(last().newRecord());
            assertNull(last().best());
        }

        @Test
        void playerMoveStopsTheReplay() {
            service.goToLevel(3);
            service.startReplay("rrR");
            service.replayStep();

            service.move(Direction.LEFT);

            assertFalse(last().replaying());
            assertFalse(service.replayStep());
        }

        @Test
        void stopReplayPublishesOnlyWhenReplaying() {
            int before = received.size();
            service.stopReplay();
            assertEquals(before, received.size());

            service.goToLevel(3);
            service.startReplay("rrR");
            service.stopReplay();

            assertEquals(GameEvent.REPLAY_STOPPED, last().event());
            assertFalse(last().replaying());
        }

        @Test
        void rejectsSolutionsThatDoNotFitTheLevel() {
            service.goToLevel(3);
            service.move(Direction.RIGHT);
            int before = received.size();

            assertThrows(InvalidSolutionException.class, () -> service.startReplay("u"));     // muro
            assertThrows(InvalidSolutionException.class, () -> service.startReplay("rRR"));   // el 2.º no empuja
            assertThrows(InvalidSolutionException.class, () -> service.startReplay("R"));     // no empuja
            assertThrows(InvalidSolutionException.class, () -> service.startReplay("rrRl"));  // sobra un paso
            assertThrows(InvalidSolutionException.class, () -> service.startReplay("   "));   // vacía

            assertEquals(before, received.size(), "una solución no válida no cambia la partida");
            assertEquals(1, last().moves());
        }
    }

    @Nested
    @DisplayName("al cambiar de colección")
    class Collections {

        @Test
        void loadsAnotherCollectionAndResumesItsLastLevel() {
            progress.saveLastLevel("Otra", 2);

            service.loadCollection(collection("Otra", LEVELS));

            assertEquals("Otra", last().collectionName());
            assertEquals(2, last().levelNumber());
            assertEquals(GameEvent.LEVEL_STARTED, last().event());
        }

        @Test
        void recordsAreSharedBetweenCollectionsWithTheSameLevel() {
            service.move(Direction.RIGHT); // resuelve el nivel 1

            service.loadCollection(collection("Otra", List.of(LEVELS.get(2), LEVELS.get(0))));
            service.goToLevel(2);

            assertNotNull(last().best());
            assertEquals(1, last().solvedCount());
        }

        @Test
        void keepsTheCurrentCollectionWhenTheNewOneIsEmpty() {
            assertThrows(LevelLoadException.class, () -> service.loadCollection(collection("Vacía", List.of())));

            assertEquals("Pruebas", service.snapshot().collectionName());
            assertEquals(3, service.levelCount());
        }
    }

    private static LevelRepository collection(String name, List<Level> levels) {
        return new LevelRepository() {
            @Override
            public List<Level> findAll() {
                return levels;
            }

            @Override
            public String collectionName() {
                return name;
            }
        };
    }

    private GameSnapshot last() {
        return received.get(received.size() - 1);
    }
}
