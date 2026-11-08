package sokoban.application;

/**
 * Qué acaba de pasar para que se publique una nueva {@link GameSnapshot}.
 *
 * <p>La vista lo usa para decidir si anima el movimiento o qué sonido toca.
 * Si el nivel se acaba de resolver, el evento es {@link #PUSHED} y
 * {@link GameSnapshot#solved()} es {@code true}.</p>
 */
public enum GameEvent {
    /** Se ha cargado un nivel (al empezar, al cambiar de nivel o de colección). */
    LEVEL_STARTED,
    /** El jugador ha andado una casilla. */
    MOVED,
    /** El jugador ha empujado una caja. */
    PUSHED,
    /** Se ha deshecho un movimiento. */
    UNDONE,
    /** El nivel ha vuelto a su estado inicial. */
    RESTARTED,
    /** Se ha detenido la reproducción de una solución. */
    REPLAY_STOPPED
}
