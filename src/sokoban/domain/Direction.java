package sokoban.domain;

/**
 * Las cuatro direcciones en las que el jugador puede moverse.
 *
 * <p>Es un {@code enum} y no un vector genérico: el dominio no depende de AWT
 * (una librería de interfaz gráfica) y es imposible pedir un movimiento
 * diagonal o de dos casillas.</p>
 *
 * <p>El eje Y crece hacia abajo, igual que las filas del fichero de niveles.</p>
 */
public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }
}
