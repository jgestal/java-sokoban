package sokoban.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import sokoban.domain.Board;
import sokoban.domain.GameState;
import sokoban.domain.Level;
import sokoban.domain.Position;

/**
 * Identificador estable de un nivel calculado a partir de su contenido.
 *
 * <p>Los récords se guardan con esta huella y no con el número o el nombre
 * del nivel. Así siguen siendo válidos aunque el nivel aparezca en otra
 * colección, en otra posición o con otro título.</p>
 */
final class LevelFingerprint {

    private static final int HEX_LENGTH_BYTES = 8;

    private LevelFingerprint() {
    }

    static String of(Level level) {
        Board board = level.board();
        GameState state = level.initialState();
        StringBuilder canonical = new StringBuilder();
        for (int y = 0; y < board.height(); y++) {
            for (int x = 0; x < board.width(); x++) {
                canonical.append(symbol(board, state, new Position(x, y)));
            }
            canonical.append('\n');
        }
        return HexFormat.of().formatHex(sha256(canonical.toString()), 0, HEX_LENGTH_BYTES);
    }

    private static char symbol(Board board, GameState state, Position p) {
        boolean box = state.hasBoxAt(p);
        boolean player = state.player().equals(p);
        return switch (board.terrainAt(p)) {
            case WALL -> '#';
            case OUTSIDE -> ' ';
            case FLOOR -> box ? '$' : player ? '@' : '-';
            case GOAL -> box ? '*' : player ? '+' : '.';
        };
    }

    private static byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // Toda JVM está obligada a incluir SHA-256.
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
