package sokoban.infrastructure.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.Timer;
import sokoban.application.GameSnapshot;
import sokoban.application.Motion;
import sokoban.application.TileGrid;

/**
 * Panel que dibuja el tablero, centrado y escalado al tamaño de la ventana.
 *
 * <p>Responsabilidad única: calcular la geometría y delegar el dibujo de cada
 * casilla en un {@link TileRenderer}. No conoce las reglas ni sabe de dónde
 * vienen los datos; solo pinta la última {@link GameSnapshot} recibida.</p>
 *
 * <p><b>Animación.</b> Si la foto trae un {@link Motion}, el jugador (y la
 * caja empujada) se dibujan deslizándose desde la casilla de origen durante
 * {@value #ANIMATION_MILLIS} ms. El estado de la partida ya está en su
 * posición final; la animación es solo visual, así que una tecla pulsada a
 * mitad de animación se procesa enseguida y la animación salta a la nueva
 * foto.</p>
 */
final class BoardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND = new Color(0x2B2B2B);
    private static final Color BANNER_BACKGROUND = new Color(0, 0, 0, 170);
    private static final int MIN_TILE_SIZE = 4;
    private static final Dimension PREFERRED_SIZE = new Dimension(640, 600);
    private static final int ANIMATION_MILLIS = 110;
    private static final int FRAME_MILLIS = 15;

    private final transient TileRenderer renderer;
    private final Timer animationTimer = new Timer(FRAME_MILLIS, event -> onAnimationFrame());

    private transient GameSnapshot snapshot;
    private transient Motion animating;
    private long animationStartNanos;

    BoardPanel(TileRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        setBackground(BACKGROUND);
        setPreferredSize(PREFERRED_SIZE);
    }

    /** Guarda la nueva foto de la partida, anima el movimiento si lo hay y repinta. */
    void show(GameSnapshot newSnapshot) {
        this.snapshot = newSnapshot;
        this.animating = newSnapshot.motion();
        if (animating != null) {
            animationStartNanos = System.nanoTime();
            animationTimer.restart();
        } else {
            animationTimer.stop();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (snapshot == null) {
            return;
        }
        // Se trabaja sobre una copia para no alterar el Graphics que Swing
        // reutiliza para otros componentes.
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            paintBoard(g, snapshot.grid());
            if (snapshot.solved() && animating == null) {
                paintSolvedBanner(g);
            }
        } finally {
            g.dispose();
        }
    }

    private void onAnimationFrame() {
        if (animationProgress() >= 1.0) {
            animating = null;
            animationTimer.stop();
        }
        repaint();
    }

    /** De 0 (recién empezada) a 1 (terminada). */
    private double animationProgress() {
        double elapsedMillis = (System.nanoTime() - animationStartNanos) / 1_000_000.0;
        return Math.min(1.0, elapsedMillis / ANIMATION_MILLIS);
    }

    private void paintBoard(Graphics2D g, TileGrid grid) {
        int tileSize = Math.max(MIN_TILE_SIZE,
                Math.min(getWidth() / grid.width(), getHeight() / grid.height()));
        int offsetX = (getWidth() - tileSize * grid.width()) / 2;
        int offsetY = (getHeight() - tileSize * grid.height()) / 2;

        // Vecino más próximo: mantiene nítidos los sprites pixel-art al escalar.
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Rectangle cell = new Rectangle(0, 0, tileSize, tileSize);
        // Primera pasada: el suelo de todo el tablero.
        for (int y = 0; y < grid.height(); y++) {
            for (int x = 0; x < grid.width(); x++) {
                cell.setLocation(offsetX + x * tileSize, offsetY + y * tileSize);
                renderer.paintGround(g, grid.tileAt(x, y), cell);
            }
        }
        // Segunda pasada: los objetos quietos.
        Motion motion = animating;
        for (int y = 0; y < grid.height(); y++) {
            for (int x = 0; x < grid.width(); x++) {
                if (!isMoving(motion, x, y)) {
                    cell.setLocation(offsetX + x * tileSize, offsetY + y * tileSize);
                    renderer.paintObject(g, grid.tileAt(x, y), cell);
                }
            }
        }
        // Por último, los objetos en movimiento, desplazados hacia su origen.
        if (motion != null) {
            double t = animationProgress();
            double eased = t * (2 - t); // frena al final
            int shiftX = (int) Math.round((1 - eased) * tileSize * -motion.direction().dx());
            int shiftY = (int) Math.round((1 - eased) * tileSize * -motion.direction().dy());
            if (motion.push()) {
                paintShifted(g, grid, motion.boxX(), motion.boxY(), cell, offsetX, offsetY, shiftX, shiftY);
            }
            paintShifted(g, grid, motion.playerX(), motion.playerY(), cell, offsetX, offsetY, shiftX, shiftY);
        }
    }

    private static boolean isMoving(Motion motion, int x, int y) {
        if (motion == null) {
            return false;
        }
        return (x == motion.playerX() && y == motion.playerY())
                || (motion.push() && x == motion.boxX() && y == motion.boxY());
    }

    private void paintShifted(Graphics2D g, TileGrid grid, int x, int y, Rectangle cell,
                              int offsetX, int offsetY, int shiftX, int shiftY) {
        int size = cell.width;
        cell.setLocation(offsetX + x * size + shiftX, offsetY + y * size + shiftY);
        renderer.paintObject(g, grid.tileAt(x, y), cell);
    }

    private void paintSolvedBanner(Graphics2D g) {
        String title = snapshot.allLevelsCompleted() ? "¡Has completado todos los niveles!" : "¡Nivel resuelto!";
        if (snapshot.newRecord()) {
            title += "  ·  Nuevo récord";
        }
        String subtitle = snapshot.hasNextLevel() ? "Pulsa Enter para ir al siguiente nivel" : "Enhorabuena";

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Font titleFont = getFont().deriveFont(Font.BOLD, 26f);
        Font subtitleFont = getFont().deriveFont(Font.PLAIN, 15f);

        int bannerHeight = 90;
        int bannerY = (getHeight() - bannerHeight) / 2;
        g.setColor(BANNER_BACKGROUND);
        g.fillRect(0, bannerY, getWidth(), bannerHeight);

        g.setColor(Color.WHITE);
        drawCentered(g, title, titleFont, bannerY + 40);
        drawCentered(g, subtitle, subtitleFont, bannerY + 68);
    }

    private void drawCentered(Graphics2D g, String text, Font font, int baselineY) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2, baselineY);
    }
}
