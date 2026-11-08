package sokoban.infrastructure.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import sokoban.application.GameSnapshot;
import sokoban.application.InvalidSolutionException;
import sokoban.application.port.in.GameCommands;
import sokoban.application.port.out.LevelLoadException;
import sokoban.application.port.out.LevelRepository;
import sokoban.domain.Direction;

/**
 * Todas las acciones del jugador como {@link Action} de Swing, más los atajos
 * de teclado que las disparan.
 *
 * <p>Decisiones de diseño:</p>
 * <ul>
 *   <li><b>Una sola definición por acción (DRY).</b> El botón "Deshacer" y la
 *       tecla Z usan el mismo objeto {@code Action}. Al desactivarlo se
 *       desactivan los dos. Lo mismo pasa con las opciones del menú.</li>
 *   <li><b>Key Bindings en lugar de {@code KeyListener}.</b> Con
 *       {@code WHEN_IN_FOCUSED_WINDOW} los atajos funcionan en toda la
 *       ventana, tenga quien tenga el foco, y las teclas se nombran con
 *       {@code KeyEvent.VK_*}.</li>
 *   <li><b>Adaptador fino.</b> Cada acción solo traduce un evento de Swing en
 *       una llamada a {@link GameCommands}. No hay lógica de juego aquí.</li>
 * </ul>
 *
 * <p>Las acciones que solo están en el menú llevan su atajo como acelerador
 * del menú ({@link Action#ACCELERATOR_KEY}); el resto se registran en el
 * panel raíz con {@link #installKeyBindings(JComponent)}.</p>
 */
final class GameActions {

    private final GameCommands commands;
    private final Supplier<GameSnapshot> currentSnapshot;
    private final Component dialogParent;
    private final Function<Path, LevelRepository> collectionOpener;
    private final Consumer<String> notifier;
    private File lastDirectory;

    final Action undo;
    final Action restart;
    final Action next;
    final Action previous;
    final Action goToLevel;
    final Action openCollection;
    final Action replaySolution;
    final Action copySolution;
    final Action stopReplay;
    final Action toggleSound;

    /**
     * @param commands         puerto de entrada de la aplicación
     * @param currentSnapshot  última foto recibida (para saber si el nivel está resuelto)
     * @param dialogParent     componente sobre el que centrar los diálogos
     * @param collectionOpener crea el repositorio de niveles de un fichero elegido
     * @param notifier         muestra un aviso breve al jugador
     */
    GameActions(GameCommands commands, Supplier<GameSnapshot> currentSnapshot, Component dialogParent,
                Function<Path, LevelRepository> collectionOpener, Consumer<String> notifier) {
        this.commands = Objects.requireNonNull(commands, "commands");
        this.currentSnapshot = Objects.requireNonNull(currentSnapshot, "currentSnapshot");
        this.dialogParent = dialogParent;
        this.collectionOpener = Objects.requireNonNull(collectionOpener, "collectionOpener");
        this.notifier = Objects.requireNonNull(notifier, "notifier");

        undo = action("Deshacer", "Deshace el último movimiento (Z / Retroceso)", commands::undo);
        restart = action("Reiniciar", "Vuelve a empezar el nivel (R)", commands::restart);
        next = action("Siguiente ▶", "Siguiente nivel (N / AvPág)", commands::nextLevel);
        previous = action("◀ Anterior", "Nivel anterior (P / RePág)", commands::previousLevel);
        goToLevel = action("Ir a nivel…", "Elegir un nivel (G)", this::askForLevel);

        openCollection = menuAction("Abrir colección…", KeyEvent.VK_O, this::chooseCollection);
        replaySolution = menuAction("Reproducir solución…", KeyEvent.VK_L, this::askForSolution);
        copySolution = menuAction("Copiar solución (LURD)", KeyEvent.VK_C, this::copyCurrentSolution);
        stopReplay = menuAction("Detener reproducción", KeyEvent.VK_ESCAPE, commands::stopReplay);
        toggleSound = menuAction("Sonido", KeyEvent.VK_M, () -> { });
        toggleSound.putValue(Action.SELECTED_KEY, Boolean.TRUE);
    }

    /** Activa o desactiva las acciones según lo que se puede hacer ahora. */
    void refresh(GameSnapshot snapshot) {
        undo.setEnabled(snapshot.moves() > 0);
        next.setEnabled(snapshot.hasNextLevel());
        previous.setEnabled(snapshot.hasPreviousLevel());
        copySolution.setEnabled(snapshot.moves() > 0);
        stopReplay.setEnabled(snapshot.replaying());
    }

    /** Si el jugador tiene el sonido activado (casilla del menú o tecla M). */
    boolean soundEnabled() {
        return Boolean.TRUE.equals(toggleSound.getValue(Action.SELECTED_KEY));
    }

    /** Registra los atajos de teclado sobre el componente raíz de la ventana. */
    void installKeyBindings(JComponent root) {
        bind(root, move(Direction.UP), "move-up", KeyEvent.VK_UP, KeyEvent.VK_W);
        bind(root, move(Direction.DOWN), "move-down", KeyEvent.VK_DOWN, KeyEvent.VK_S);
        bind(root, move(Direction.LEFT), "move-left", KeyEvent.VK_LEFT, KeyEvent.VK_A);
        bind(root, move(Direction.RIGHT), "move-right", KeyEvent.VK_RIGHT, KeyEvent.VK_D);
        bind(root, undo, "undo", KeyEvent.VK_Z, KeyEvent.VK_BACK_SPACE);
        bind(root, restart, "restart", KeyEvent.VK_R);
        bind(root, next, "next", KeyEvent.VK_N, KeyEvent.VK_PAGE_DOWN);
        bind(root, previous, "previous", KeyEvent.VK_P, KeyEvent.VK_PAGE_UP);
        bind(root, goToLevel, "go-to-level", KeyEvent.VK_G);
        bind(root, action("Continuar", null, this::continueIfSolved), "continue", KeyEvent.VK_ENTER);
    }

    // --- Detalles privados --------------------------------------------------

    private Action move(Direction direction) {
        return action(direction.name(), null, () -> commands.move(direction));
    }

    /** Enter solo avanza cuando el nivel actual está resuelto. */
    private void continueIfSolved() {
        GameSnapshot snapshot = currentSnapshot.get();
        if (snapshot != null && snapshot.solved() && snapshot.hasNextLevel()) {
            commands.nextLevel();
        }
    }

    private void askForLevel() {
        GameSnapshot snapshot = currentSnapshot.get();
        int current = snapshot == null ? 1 : snapshot.levelNumber();
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(current, 1, commands.levelCount(), 1));
        int choice = JOptionPane.showConfirmDialog(dialogParent, spinner,
                "Ir al nivel (1-" + commands.levelCount() + ")", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {
            // El modelo del spinner ya limita el rango; goToLevel lo valida igualmente.
            commands.goToLevel((Integer) spinner.getValue());
        }
    }

    private void chooseCollection() {
        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setDialogTitle("Abrir colección de niveles (formato XSB)");
        chooser.setFileFilter(new FileNameExtensionFilter("Colecciones de Sokoban (*.txt, *.sok, *.xsb)",
                "txt", "sok", "xsb"));
        if (chooser.showOpenDialog(dialogParent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        lastDirectory = file.getParentFile();
        try {
            commands.loadCollection(collectionOpener.apply(file.toPath()));
        } catch (LevelLoadException e) {
            showError("No se pudo abrir la colección", e.getMessage());
        }
    }

    private void askForSolution() {
        JTextArea text = new JTextArea(commands.bestSolution().orElse(""), 6, 44);
        text.setLineWrap(true);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("<html>Solución en formato LURD: <b>l u r d</b> para andar, "
                + "<b>L U R D</b> para empujar.<br>Si el nivel tiene récord, aparece su solución.</html>"),
                BorderLayout.NORTH);
        panel.add(new JScrollPane(text), BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(dialogParent, panel, "Reproducir solución",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            commands.startReplay(text.getText());
        } catch (InvalidSolutionException e) {
            showError("La solución no es válida", e.getMessage());
        }
    }

    private void copyCurrentSolution() {
        String solution = commands.currentSolution();
        if (solution.isEmpty()) {
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(solution), null);
        notifier.accept("Solución copiada (" + solution.length() + " movimientos)");
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(dialogParent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private static void bind(JComponent root, Action action, String key, int... keyCodes) {
        for (int keyCode : keyCodes) {
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), key);
        }
        root.getActionMap().put(key, action);
    }

    private static Action menuAction(String name, int keyCode, Runnable body) {
        Action action = action(name, null, body);
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, 0));
        return action;
    }

    /** Fábrica de {@link Action} a partir de una lambda: evita clases anónimas repetidas. */
    private static Action action(String name, String tooltip, Runnable body) {
        Action action = new AbstractAction(name) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                body.run();
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, tooltip);
        return action;
    }
}
