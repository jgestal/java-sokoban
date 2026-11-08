package sokoban.infrastructure.level;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;
import sokoban.domain.InvalidLevelException;
import sokoban.domain.Level;

/**
 * Trocea un fichero de colección de niveles en niveles individuales.
 *
 * <p>Formato (el de {@code levels.txt}):</p>
 * <pre>
 * Level 44          ← líneas de título (cualquier línea que no sea tablero)
 * 'Duh!'
 * #####             ← filas del tablero
 * #@$.#
 * #####
 *                   ← una línea en blanco separa un nivel del siguiente
 * </pre>
 *
 * <p>Varias líneas de título se unen con " - " y se quitan las comillas
 * simples. Así "Level 44" + "'Duh!'" queda como "Level 44 - Duh!". Las líneas
 * de comentario con {@code ;} (habituales en ficheros {@code .sok} y
 * {@code .xsb} de otros autores) también sirven como título.</p>
 *
 * <p>Decisión: un nivel mal formado <b>no</b> impide cargar los demás. Se
 * registra un aviso y se descarta. En una colección grande, uno defectuoso no
 * debería dejar al jugador sin juego.</p>
 *
 * <p>Este parser solo trabaja con texto en memoria, sin ficheros. Eso lo hace
 * fácil de probar; la lectura está en {@link ClasspathLevelRepository} y
 * {@link FileLevelRepository}.</p>
 */
final class LevelCollectionParser {

    private static final Logger LOG = System.getLogger(LevelCollectionParser.class.getName());

    private final XsbBoardParser boardParser = new XsbBoardParser();

    List<Level> parse(List<String> lines) {
        List<Level> levels = new ArrayList<>();
        List<String> titleLines = new ArrayList<>();
        List<String> boardRows = new ArrayList<>();

        for (String line : lines) {
            if (line.isBlank()) {
                flush(titleLines, boardRows, levels);
            } else if (isBoardRow(line)) {
                boardRows.add(line.stripTrailing());
            } else {
                String title = stripComment(line.strip());
                if (!title.isEmpty()) {
                    titleLines.add(title);
                }
            }
        }
        flush(titleLines, boardRows, levels); // último nivel si el fichero no acaba en línea en blanco
        return levels;
    }

    /** Cierra el bloque actual, crea el nivel (si lo hay) y vacía los buffers. */
    private void flush(List<String> titleLines, List<String> boardRows, List<Level> levels) {
        if (!boardRows.isEmpty()) {
            String name = buildName(titleLines, levels.size() + 1);
            try {
                levels.add(boardParser.parse(name, List.copyOf(boardRows)));
            } catch (InvalidLevelException e) {
                LOG.log(Logger.Level.WARNING, "Nivel descartado: {0}", e.getMessage());
            }
        }
        titleLines.clear();
        boardRows.clear();
    }

    /** Una fila de tablero solo usa símbolos XSB e incluye al menos un muro. */
    static boolean isBoardRow(String line) {
        if (line.indexOf('#') < 0) {
            return false;
        }
        for (int i = 0; i < line.length(); i++) {
            if (XsbBoardParser.VALID_CHARS.indexOf(line.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    private static String buildName(List<String> titleLines, int fallbackNumber) {
        if (titleLines.isEmpty()) {
            return "Nivel " + fallbackNumber;
        }
        List<String> cleaned = titleLines.stream()
                .map(LevelCollectionParser::stripQuotes)
                .toList();
        return String.join(" - ", cleaned);
    }

    private static String stripComment(String text) {
        return text.startsWith(";") ? text.substring(1).strip() : text;
    }

    private static String stripQuotes(String text) {
        if (text.length() >= 2 && text.startsWith("'") && text.endsWith("'")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }
}
