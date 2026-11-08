package sokoban.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Construye niveles de prueba a partir de dibujos ASCII.
 *
 * <p>Es deliberadamente mínimo e independiente del parser de infraestructura:
 * los tests del dominio no deben depender de capas externas.</p>
 */
public final class LevelFixture {

    private LevelFixture() {
    }

    public static Level level(String... rows) {
        Terrain[][] grid = new Terrain[rows.length][];
        Set<Position> boxes = new HashSet<>();
        Position player = null;
        for (int y = 0; y < rows.length; y++) {
            grid[y] = new Terrain[rows[y].length()];
            for (int x = 0; x < rows[y].length(); x++) {
                char c = rows[y].charAt(x);
                grid[y][x] = switch (c) {
                    case '#' -> Terrain.WALL;
                    case '.', '*', '+' -> Terrain.GOAL;
                    default -> Terrain.FLOOR;
                };
                if (c == '$' || c == '*') {
                    boxes.add(new Position(x, y));
                }
                if (c == '@' || c == '+') {
                    player = new Position(x, y);
                }
            }
        }
        return new Level("test", new Board(grid), new GameState(player, boxes));
    }

    public static Puzzle puzzle(String... rows) {
        return level(rows).newPuzzle();
    }
}
