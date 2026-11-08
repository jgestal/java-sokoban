package sokoban.infrastructure.ui.swing;

import java.util.Optional;
import sokoban.application.GameSnapshot;
import sokoban.application.Motion;
import sokoban.application.Tile;
import sokoban.infrastructure.audio.Sound;

/** Decide qué sonido corresponde a cada foto de la partida. */
final class SoundCue {

    private SoundCue() {
    }

    static Optional<Sound> forSnapshot(GameSnapshot snapshot) {
        return switch (snapshot.event()) {
            case MOVED -> Optional.of(Sound.STEP);
            case PUSHED -> Optional.of(pushSound(snapshot));
            case UNDONE -> Optional.of(Sound.UNDO);
            case LEVEL_STARTED, RESTARTED, REPLAY_STOPPED -> Optional.empty();
        };
    }

    private static Sound pushSound(GameSnapshot snapshot) {
        if (snapshot.solved()) {
            return Sound.SOLVED;
        }
        Motion motion = snapshot.motion();
        boolean onGoal = motion != null && snapshot.grid().tileAt(motion.boxX(), motion.boxY()) == Tile.BOX_ON_GOAL;
        return onGoal ? Sound.BOX_ON_GOAL : Sound.PUSH;
    }
}
