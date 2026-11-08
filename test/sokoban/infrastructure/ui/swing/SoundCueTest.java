package sokoban.infrastructure.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;
import sokoban.application.GameService;
import sokoban.application.GameSnapshot;
import sokoban.application.Score;
import sokoban.application.port.out.ProgressRepository;
import sokoban.domain.Direction;
import sokoban.domain.LevelFixture;
import sokoban.infrastructure.audio.Sound;

class SoundCueTest {

    private final List<GameSnapshot> received = new ArrayList<>();

    @Test
    void picksASoundForWalkingPushingAndUndoing() {
        GameService game = start("#######", "#@ $ .#", "#######");
        assertEquals(Optional.empty(), SoundCue.forSnapshot(last()));

        game.move(Direction.RIGHT);
        assertEquals(Optional.of(Sound.STEP), SoundCue.forSnapshot(last()));

        game.move(Direction.RIGHT);
        assertEquals(Optional.of(Sound.PUSH), SoundCue.forSnapshot(last()));

        game.undo();
        assertEquals(Optional.of(Sound.UNDO), SoundCue.forSnapshot(last()));
    }

    @Test
    void boxReachingAGoalHasItsOwnSound() {
        GameService game = start("########", "#@$.$ .#", "########");

        game.move(Direction.RIGHT); // la primera caja llega a su objetivo; falta la otra

        assertEquals(Optional.of(Sound.BOX_ON_GOAL), SoundCue.forSnapshot(last()));
    }

    @Test
    void solvingTheLevelPlaysTheFanfare() {
        GameService game = start("#####", "#@$.#", "#####");

        game.move(Direction.RIGHT);

        assertEquals(Optional.of(Sound.SOLVED), SoundCue.forSnapshot(last()));
    }

    private GameService start(String... rows) {
        GameService game = new GameService(() -> List.of(LevelFixture.level(rows)), new NoProgress());
        game.addListener(received::add);
        return game;
    }

    private GameSnapshot last() {
        return received.get(received.size() - 1);
    }

    private static final class NoProgress implements ProgressRepository {
        @Override
        public Optional<Score> bestScore(String levelId) {
            return Optional.empty();
        }

        @Override
        public void saveBestScore(String levelId, Score score) {
        }

        @Override
        public OptionalInt lastLevel(String collectionName) {
            return OptionalInt.empty();
        }

        @Override
        public void saveLastLevel(String collectionName, int levelNumber) {
        }
    }
}
