package sokoban.application;

import java.util.Optional;

/**
 * Todo lo que la interfaz necesita para pintar la partida en un instante.
 *
 * <p>Es un DTO inmutable que cruza la frontera entre aplicación e
 * infraestructura. La vista no recibe nunca el {@code Puzzle}, que es
 * mutable, así que no puede saltarse las reglas del juego modificándolo.</p>
 *
 * @param collectionName nombre de la colección de niveles
 * @param levelNumber    número del nivel actual (desde 1)
 * @param levelCount     total de niveles de la colección
 * @param levelName      nombre legible del nivel
 * @param grid           casillas a pintar
 * @param moves          movimientos realizados (los deshechos se descuentan)
 * @param pushes         empujes realizados
 * @param undos          veces que se ha usado "deshacer"
 * @param solved         si el nivel está resuelto
 * @param solvedCount    niveles de la colección resueltos alguna vez
 * @param best           récord del nivel, o {@code null} si no se ha resuelto nunca
 * @param newRecord      si la partida que acaba de resolver el nivel es un récord
 * @param event          qué ha provocado esta foto
 * @param motion         desplazamiento a animar, o {@code null} si no lo hay
 * @param replaying      si se está reproduciendo una solución
 */
public record GameSnapshot(
        String collectionName,
        int levelNumber,
        int levelCount,
        String levelName,
        TileGrid grid,
        int moves,
        int pushes,
        int undos,
        boolean solved,
        int solvedCount,
        Score best,
        boolean newRecord,
        GameEvent event,
        Motion motion,
        boolean replaying) {

    public boolean hasNextLevel() {
        return levelNumber < levelCount;
    }

    public boolean hasPreviousLevel() {
        return levelNumber > 1;
    }

    /** Se ha resuelto el último nivel: fin de la colección. */
    public boolean allLevelsCompleted() {
        return solved && !hasNextLevel();
    }

    /** Récord del nivel, si se ha resuelto alguna vez. */
    public Optional<Score> bestScore() {
        return Optional.ofNullable(best);
    }

    /** Desplazamiento a animar, si la foto viene de un movimiento. */
    public Optional<Motion> lastMotion() {
        return Optional.ofNullable(motion);
    }
}
