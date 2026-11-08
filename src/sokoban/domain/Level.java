package sokoban.domain;

import java.util.Objects;

/**
 * Definición inmutable de un nivel: nombre, plano y posición inicial.
 *
 * <p>Un {@code Level} es la "plantilla"; cada vez que se juega se crea un
 * {@link Puzzle} nuevo con {@link #newPuzzle()}. Así reiniciar un nivel o
 * volver a él más tarde nunca arrastra estado de una partida anterior.</p>
 *
 * <p>El constructor valida las invariantes del nivel. Es preferible fallar al
 * cargar que tener un nivel imposible a mitad de partida.</p>
 */
public record Level(String name, Board board, GameState initialState) {

    public Level {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(initialState, "initialState");
        validate(board, initialState);
    }

    /** Crea una partida nueva de este nivel, en su estado inicial. */
    public Puzzle newPuzzle() {
        return new Puzzle(board, initialState);
    }

    private static void validate(Board board, GameState state) {
        if (!board.isWalkable(state.player())) {
            throw new InvalidLevelException("El jugador no está sobre una casilla transitable: " + state.player());
        }
        for (Position box : state.boxes()) {
            if (!board.isWalkable(box)) {
                throw new InvalidLevelException("Caja sobre una casilla no transitable: " + box);
            }
        }
        if (state.boxes().isEmpty()) {
            throw new InvalidLevelException("El nivel no tiene cajas");
        }
        if (state.boxes().size() > board.goals().size()) {
            throw new InvalidLevelException("Hay más cajas (" + state.boxes().size()
                    + ") que objetivos (" + board.goals().size() + ")");
        }
    }
}
