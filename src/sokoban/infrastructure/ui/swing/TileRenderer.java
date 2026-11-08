package sokoban.infrastructure.ui.swing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import sokoban.application.Tile;

/**
 * Estrategia para dibujar una casilla (patrón <em>Strategy</em>).
 *
 * <p>{@link BoardPanel} solo sabe recorrer la cuadrícula y pedir a un
 * {@code TileRenderer} que pinte cada casilla. Cambiar el aspecto del juego
 * (sprites, figuras, un tema oscuro…) consiste en dar otra implementación,
 * sin modificar el panel (principio abierto/cerrado). Cualquier
 * implementación sirve en su lugar (sustitución de Liskov).</p>
 *
 * <p>Se pinta en dos capas, el suelo y el objeto que hay encima, para que el
 * panel pueda dibujar el jugador y las cajas desplazados durante una
 * animación.</p>
 */
public interface TileRenderer {

    /** Pinta la capa fija: muro, suelo u objetivo. */
    void paintGround(Graphics2D g, Tile tile, Rectangle cell);

    /** Pinta el objeto móvil de la casilla (caja o jugador), si lo hay. */
    void paintObject(Graphics2D g, Tile tile, Rectangle cell);

    /**
     * Pinta la casilla completa.
     *
     * @param g    contexto gráfico
     * @param tile qué hay en la casilla
     * @param cell zona de pantalla (cuadrada) donde pintarla
     */
    default void paint(Graphics2D g, Tile tile, Rectangle cell) {
        paintGround(g, tile, cell);
        paintObject(g, tile, cell);
    }
}
