package sokoban.infrastructure.audio;

/** Los efectos de sonido del juego. */
public enum Sound {
    /** El jugador anda una casilla. */
    STEP,
    /** El jugador empuja una caja. */
    PUSH,
    /** Una caja llega a un objetivo. */
    BOX_ON_GOAL,
    /** Se deshace un movimiento. */
    UNDO,
    /** Se resuelve el nivel. */
    SOLVED
}
