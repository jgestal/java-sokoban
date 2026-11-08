package sokoban.infrastructure.ui.swing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import sokoban.application.Tile;

/**
 * Pinta las casillas con las imágenes PNG del juego.
 *
 * <p>Pinta <b>por capas</b>: primero el suelo (normal u objetivo) y encima el
 * objeto (caja o jugador). Así basta un sprite por elemento y el jugador sobre
 * un objetivo sale bien sin necesitar una imagen aparte.</p>
 *
 * <p>Las imágenes se cargan una sola vez con {@link #fromClasspath(String)} y
 * cada instancia guarda las suyas (no hay estado global).</p>
 */
public final class SpriteTileRenderer implements TileRenderer {

    /** Carpeta por defecto de los sprites dentro del classpath. */
    public static final String DEFAULT_DIRECTORY = "/resources/sprites/";

    /** Cada sprite y el fichero del que se carga. */
    private enum Sprite {
        WALL("wall.png"),
        FLOOR("floor.png"),
        GOAL("goal.png"),
        BOX("box.png"),
        BOX_ON_GOAL("box_on_goal.png"),
        PLAYER("player.png");

        private final String fileName;

        Sprite(String fileName) {
            this.fileName = fileName;
        }
    }

    private final Map<Sprite, BufferedImage> images;

    private SpriteTileRenderer(Map<Sprite, BufferedImage> images) {
        this.images = images;
    }

    /**
     * Carga todos los sprites de una carpeta del classpath.
     *
     * @throws IOException si falta alguno o no se puede leer. Se exige que
     *                     estén todos para no fallar a mitad de partida.
     */
    public static SpriteTileRenderer fromClasspath(String directory) throws IOException {
        Map<Sprite, BufferedImage> images = new EnumMap<>(Sprite.class);
        for (Sprite sprite : Sprite.values()) {
            String path = directory + sprite.fileName;
            try (InputStream in = SpriteTileRenderer.class.getResourceAsStream(path)) {
                if (in == null) {
                    throw new IOException("No se encuentra el sprite " + path);
                }
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    throw new IOException("Formato de imagen no soportado: " + path);
                }
                images.put(sprite, image);
            }
        }
        return new SpriteTileRenderer(images);
    }

    @Override
    public void paintGround(Graphics2D g, Tile tile, Rectangle cell) {
        switch (tile) {
            case OUTSIDE -> { /* fuera del recinto: se ve el fondo del panel */ }
            case WALL -> draw(g, Sprite.WALL, cell);
            case FLOOR, BOX, PLAYER -> draw(g, Sprite.FLOOR, cell);
            case GOAL, BOX_ON_GOAL, PLAYER_ON_GOAL -> draw(g, Sprite.GOAL, cell);
        }
    }

    @Override
    public void paintObject(Graphics2D g, Tile tile, Rectangle cell) {
        switch (tile) {
            case BOX -> draw(g, Sprite.BOX, cell);
            case BOX_ON_GOAL -> draw(g, Sprite.BOX_ON_GOAL, cell);
            case PLAYER, PLAYER_ON_GOAL -> draw(g, Sprite.PLAYER, cell);
            case OUTSIDE, WALL, FLOOR, GOAL -> { /* sin objeto */ }
        }
    }

    private void draw(Graphics2D g, Sprite sprite, Rectangle cell) {
        g.drawImage(images.get(sprite), cell.x, cell.y, cell.width, cell.height, null);
    }
}
