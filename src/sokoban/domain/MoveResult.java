package sokoban.domain;

/** Resultado de intentar un movimiento con {@link Puzzle#move(Direction)}. */
public enum MoveResult {
    /** El jugador ha avanzado a una casilla libre. */
    MOVED,
    /** El jugador ha avanzado empujando una caja. */
    PUSHED,
    /** No se ha podido mover (muro, caja bloqueada o nivel ya resuelto). */
    BLOCKED;

    /** Indica si el estado de la partida ha cambiado. */
    public boolean changedState() {
        return this != BLOCKED;
    }
}
