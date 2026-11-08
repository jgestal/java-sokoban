package sokoban.infrastructure.ui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import sokoban.application.Tile;

/**
 * Pinta las casillas con figuras geométricas, sin imágenes.
 *
 * <p>Sirve de <b>alternativa</b> si no se pueden cargar los sprites: el juego
 * sigue siendo jugable en lugar de mostrar una pantalla vacía. También muestra
 * el principio abierto/cerrado: se ha añadido un aspecto visual nuevo sin
 * tocar {@link BoardPanel}.</p>
 */
public final class ShapeTileRenderer implements TileRenderer {

    private static final Color WALL = new Color(0x5B4A3F);
    private static final Color WALL_EDGE = new Color(0x3E322A);
    private static final Color FLOOR = new Color(0xD9CBB3);
    private static final Color GOAL = new Color(0xC2452D);
    private static final Color BOX = new Color(0xC8913A);
    private static final Color BOX_DONE = new Color(0x4E8F4A);
    private static final Color PLAYER = new Color(0x2F5D8A);

    @Override
    public void paintGround(Graphics2D shared, Tile tile, Rectangle cell) {
        if (tile == Tile.OUTSIDE) {
            return;
        }
        Graphics2D g = localCopy(shared);
        try {
            if (tile == Tile.WALL) {
                fill(g, WALL, cell);
                g.setColor(WALL_EDGE);
                g.drawRect(cell.x, cell.y, cell.width - 1, cell.height - 1);
                return;
            }
            fill(g, FLOOR, cell);
            if (tile.isOnGoal()) {
                paintGoal(g, cell);
            }
        } finally {
            g.dispose();
        }
    }

    @Override
    public void paintObject(Graphics2D shared, Tile tile, Rectangle cell) {
        if (!tile.hasBox() && !tile.hasPlayer()) {
            return;
        }
        Graphics2D g = localCopy(shared);
        try {
            if (tile.hasBox()) {
                paintBox(g, cell, tile == Tile.BOX_ON_GOAL ? BOX_DONE : BOX);
            } else {
                paintPlayer(g, cell);
            }
        } finally {
            g.dispose();
        }
    }

    /**
     * Copia local: el grosor de línea o el antialiasing de una casilla no
     * deben "contagiarse" a la siguiente (Graphics2D es un objeto con estado).
     */
    private static Graphics2D localCopy(Graphics2D shared) {
        Graphics2D g = (Graphics2D) shared.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    private static void fill(Graphics2D g, Color color, Rectangle cell) {
        g.setColor(color);
        g.fillRect(cell.x, cell.y, cell.width, cell.height);
    }

    private static void paintGoal(Graphics2D g, Rectangle cell) {
        int size = cell.width / 3;
        g.setColor(GOAL);
        g.setStroke(new BasicStroke(Math.max(1f, cell.width / 12f)));
        g.drawOval(cell.x + (cell.width - size) / 2, cell.y + (cell.height - size) / 2, size, size);
    }

    private static void paintBox(Graphics2D g, Rectangle cell, Color color) {
        int inset = cell.width / 8;
        int size = cell.width - 2 * inset;
        g.setColor(color);
        g.fillRoundRect(cell.x + inset, cell.y + inset, size, size, inset, inset);
        g.setColor(color.darker());
        g.drawRoundRect(cell.x + inset, cell.y + inset, size, size, inset, inset);
    }

    private static void paintPlayer(Graphics2D g, Rectangle cell) {
        int inset = cell.width / 5;
        int size = cell.width - 2 * inset;
        g.setColor(PLAYER);
        g.fillOval(cell.x + inset, cell.y + inset, size, size);
    }
}
