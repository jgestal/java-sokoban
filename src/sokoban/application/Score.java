package sokoban.application;

import java.util.Objects;

/**
 * Récord de un nivel: la mejor partida que lo ha resuelto.
 *
 * <p>Una puntuación es mejor que otra si usa menos movimientos; a igualdad de
 * movimientos, gana la que tiene menos empujes. Se guarda también la solución
 * en LURD para poder reproducirla.</p>
 *
 * @param moves    movimientos de la solución
 * @param pushes   empujes de la solución
 * @param solution la solución en notación LURD
 */
public record Score(int moves, int pushes, String solution) {

    public Score {
        if (moves < 0 || pushes < 0 || pushes > moves) {
            throw new IllegalArgumentException("Puntuación no válida: " + moves + " movimientos, " + pushes + " empujes");
        }
        Objects.requireNonNull(solution, "solution");
    }

    /** Indica si esta puntuación supera a {@code other}. */
    public boolean isBetterThan(Score other) {
        return moves < other.moves || (moves == other.moves && pushes < other.pushes);
    }
}
