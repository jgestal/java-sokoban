package sokoban.application;

import java.util.Objects;
import sokoban.domain.Direction;

/**
 * El último desplazamiento del jugador, para que la vista pueda animarlo.
 *
 * @param fromX     columna de la que sale el jugador
 * @param fromY     fila de la que sale el jugador
 * @param direction hacia dónde se mueve
 * @param push      si empuja una caja (que pasa de la casilla de destino del
 *                  jugador a la siguiente)
 */
public record Motion(int fromX, int fromY, Direction direction, boolean push) {

    public Motion {
        Objects.requireNonNull(direction, "direction");
    }

    /** Columna a la que llega el jugador. */
    public int playerX() {
        return fromX + direction.dx();
    }

    /** Fila a la que llega el jugador. */
    public int playerY() {
        return fromY + direction.dy();
    }

    /** Columna a la que llega la caja empujada. */
    public int boxX() {
        return fromX + 2 * direction.dx();
    }

    /** Fila a la que llega la caja empujada. */
    public int boxY() {
        return fromY + 2 * direction.dy();
    }
}
