package sokoban.domain;

/**
 * Lo que hay en el suelo de una casilla: la parte <strong>fija</strong> del
 * nivel, que nunca cambia durante la partida.
 *
 * <p>Decisión de diseño: el terreno (fijo, aquí) va separado de los objetos
 * móviles (jugador y cajas, en {@link GameState}). Así no hacen falta
 * combinaciones como "jugador sobre objetivo" y las reglas quedan muy
 * simples.</p>
 */
public enum Terrain {
    /** Muro: no se puede pisar ni empujar nada hacia él. */
    WALL,
    /** Suelo normal del interior del nivel. */
    FLOOR,
    /** Casilla objetivo donde hay que dejar una caja. */
    GOAL,
    /** Fuera del recinto (o fuera de los límites del tablero). */
    OUTSIDE;

    /** Indica si el jugador o una caja pueden ocupar esta casilla. */
    public boolean isWalkable() {
        return this == FLOOR || this == GOAL;
    }
}
