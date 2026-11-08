package sokoban.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import sokoban.domain.LevelFixture;

class LevelFingerprintTest {

    @Test
    void sameLayoutGivesSameFingerprint() {
        assertEquals(
                LevelFingerprint.of(LevelFixture.level("#####", "#@$.#", "#####")),
                LevelFingerprint.of(LevelFixture.level("#####", "#@$.#", "#####")));
    }

    @Test
    void differentStartGivesDifferentFingerprint() {
        assertNotEquals(
                LevelFingerprint.of(LevelFixture.level("######", "#@ $.#", "######")),
                LevelFingerprint.of(LevelFixture.level("######", "# @$.#", "######")));
    }
}
