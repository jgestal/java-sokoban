package sokoban.application;

/**
 * Lo que hay que <em>dibujar</em> en una casilla: terreno y objeto ya
 * combinados.
 *
 * <p>Es un modelo de presentación (view model) y no forma parte del dominio.
 * El dominio separa terreno y objetos porque así las reglas son más simples;
 * la vista prefiere una sola etiqueta por casilla. Esta capa hace la
 * traducción para que la interfaz no tenga que conocer el dominio.</p>
 */
public enum Tile {
    OUTSIDE,
    WALL,
    FLOOR,
    GOAL,
    BOX,
    BOX_ON_GOAL,
    PLAYER,
    PLAYER_ON_GOAL;

    /** Indica si debajo del objeto hay un objetivo. */
    public boolean isOnGoal() {
        return this == GOAL || this == BOX_ON_GOAL || this == PLAYER_ON_GOAL;
    }

    public boolean hasBox() {
        return this == BOX || this == BOX_ON_GOAL;
    }

    public boolean hasPlayer() {
        return this == PLAYER || this == PLAYER_ON_GOAL;
    }
}
