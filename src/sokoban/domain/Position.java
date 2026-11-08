package sokoban.domain;

/**
 * Coordenada inmutable de una casilla del tablero ({@code x} = columna,
 * {@code y} = fila).
 *
 * <p>Es un <em>value object</em>: dos posiciones con las mismas coordenadas son
 * iguales. Como es un {@code record}, {@code equals}/{@code hashCode} vienen
 * generados y se puede usar sin riesgo como elemento de un {@link java.util.Set}
 * (así se guardan las cajas).</p>
 */
public record Position(int x, int y) {

    /** Devuelve la posición vecina en la dirección indicada. */
    public Position step(Direction direction) {
        return new Position(x + direction.dx(), y + direction.dy());
    }
}
