package sokoban.infrastructure.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import sokoban.application.GameSnapshot;

/**
 * Panel lateral con el nivel, los contadores, los botones y la ayuda de teclas.
 *
 * <p>Es una vista pasiva: muestra lo que recibe en {@link #show(GameSnapshot)}
 * y sus botones ejecutan las {@link GameActions} compartidas. No conoce el
 * servicio del juego.</p>
 */
final class InfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int WIDTH = 220;
    private static final int GAP = 8;
    private static final int NAME_WRAP_WIDTH_CSS = (WIDTH - 40) * 3 / 4;

    private final JLabel levelNumber = new JLabel();
    private final JLabel levelName = new JLabel();
    private final JLabel moves = new JLabel();
    private final JLabel pushes = new JLabel();
    private final JLabel undos = new JLabel();
    private final JLabel record = new JLabel();
    private final JLabel solvedCount = new JLabel();
    private final JLabel status = new JLabel(" ");

    InfoPanel(GameActions actions) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(WIDTH, 0));

        levelNumber.setFont(levelNumber.getFont().deriveFont(Font.BOLD, 18f));
        status.setForeground(new Color(0x2E7D32));
        status.setFont(status.getFont().deriveFont(Font.BOLD));

        addAll(levelNumber, levelName, Box.createVerticalStrut(2 * GAP),
                moves, pushes, undos, Box.createVerticalStrut(GAP),
                record, solvedCount, Box.createVerticalStrut(2 * GAP),
                button(actions.undo), button(actions.restart), Box.createVerticalStrut(GAP),
                button(actions.previous), button(actions.next), button(actions.goToLevel),
                Box.createVerticalStrut(2 * GAP), status, Box.createVerticalGlue(), helpLabel());
    }

    /** Actualiza los textos con la nueva foto de la partida. */
    void show(GameSnapshot snapshot) {
        levelNumber.setText("Nivel " + snapshot.levelNumber() + " / " + snapshot.levelCount()
                + (snapshot.best() != null ? "  ✓" : ""));
        // HTML con ancho fijo para que los nombres largos hagan salto de línea en vez de cortarse.
        // El motor HTML de Swing escala los px de CSS (~1,33x), de ahí el factor.
        levelName.setText("<html><body style='width:" + NAME_WRAP_WIDTH_CSS + "px'>"
                + escapeHtml(snapshot.levelName()) + "</body></html>");
        moves.setText("Movimientos: " + snapshot.moves());
        pushes.setText("Empujes: " + snapshot.pushes());
        undos.setText("Deshacer usado: " + snapshot.undos());
        record.setText(snapshot.bestScore()
                .map(best -> "Récord: " + best.moves() + " mov. · " + best.pushes() + " emp.")
                .orElse("Récord: —"));
        solvedCount.setText("Resueltos: " + snapshot.solvedCount() + " / " + snapshot.levelCount());
        status.setText(statusText(snapshot));
    }

    /** Muestra un aviso breve en la línea de estado hasta el siguiente cambio de la partida. */
    void notify(String message) {
        status.setText(message);
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String statusText(GameSnapshot snapshot) {
        if (snapshot.replaying()) {
            return "Reproduciendo… (Esc para parar)";
        }
        if (!snapshot.solved()) {
            return " ";
        }
        String solved = snapshot.allLevelsCompleted() ? "¡Colección completada!" : "¡Resuelto! Pulsa Enter";
        return snapshot.newRecord() ? "<html>" + solved + "<br>Nuevo récord</html>" : solved;
    }

    private void addAll(Component... components) {
        for (Component component : components) {
            // BoxLayout exige la misma alineación en todos los hijos para alinear a la izquierda.
            if (component instanceof JComponent swingComponent) {
                swingComponent.setAlignmentX(LEFT_ALIGNMENT);
            }
            add(component);
        }
    }

    /**
     * Botón que no toma el foco: aunque con Key Bindings no es imprescindible,
     * evita que la barra espaciadora "pulse" el último botón usado.
     */
    private static JButton button(Action action) {
        JButton button = new JButton(action);
        button.setFocusable(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        return button;
    }

    private static JLabel helpLabel() {
        JLabel help = new JLabel("<html><b>Teclas</b><br>"
                + "Flechas / WASD: mover<br>"
                + "Z / Retroceso: deshacer<br>"
                + "R: reiniciar<br>"
                + "N / P: siguiente / anterior<br>"
                + "G: ir a nivel<br>"
                + "Enter: continuar<br>"
                + "L: reproducir solución<br>"
                + "C: copiar solución<br>"
                + "O: colección · M: sonido</html>");
        help.setForeground(Color.GRAY);
        help.setAlignmentX(LEFT_ALIGNMENT);
        return help;
    }
}
