package sokoban.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sokoban.domain.Direction;
import sokoban.domain.Step;

class LurdTest {

    @Test
    void encodesWalksInLowerCaseAndPushesInUpperCase() {
        List<Step> steps = List.of(
                new Step(Direction.LEFT, false),
                new Step(Direction.UP, true),
                new Step(Direction.RIGHT, false),
                new Step(Direction.DOWN, true));

        assertEquals("lUrD", Lurd.encode(steps));
    }

    @Test
    void decodeIsTheInverseOfEncode() {
        String solution = "rrDDlluuRRdL";

        assertEquals(solution, Lurd.encode(Lurd.decode(solution)));
    }

    @Test
    void ignoresWhitespaceAndExpandsRepetitions() {
        assertEquals("rrrUUd", Lurd.encode(Lurd.decode(" 3r\n2U d ")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"rx", "r3", "r-l", "99999r"})
    void rejectsInvalidText(String text) {
        assertThrows(InvalidSolutionException.class, () -> Lurd.decode(text));
    }
}
