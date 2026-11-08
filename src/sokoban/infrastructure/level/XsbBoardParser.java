package sokoban.infrastructure.level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sokoban.domain.Board;
import sokoban.domain.Direction;
import sokoban.domain.GameState;
import sokoban.domain.InvalidLevelException;
import sokoban.domain.Level;
import sokoban.domain.Position;
import sokoban.domain.Terrain;

/**
 * Convierte el dibujo ASCII de un tablero (formato estándar <em>XSB</em>) en
 * un {@link Level} del dominio.
 *
 * <pre>
 *   #  muro            .  objetivo
 *   espacio, - o _  suelo
 *   $  caja            *  caja sobre objetivo
 *   @  jugador         +  jugador sobre objetivo
 * </pre>
 *
 * <p>El formato de fichero es un detalle de infraestructura, por eso este
 * parser no está en el dominio. Si mañana se quisiera leer JSON, se añadiría
 * otro parser sin tocar el dominio (principio abierto/cerrado).</p>
 *
 * <p>Además distingue el <b>interior</b> del <b>exterior</b>: los espacios en
 * blanco fuera de los muros no son suelo, son {@link Terrain#OUTSIDE}. Se
 * calculan con un relleno por inundación (flood fill) desde el jugador. Así la
 * vista no pinta baldosas de suelo fuera del recinto.</p>
 */
final class XsbBoardParser {

    private static final char WALL = '#';
    private static final char GOAL = '.';
    private static final char BOX = '$';
    private static final char BOX_ON_GOAL = '*';
    private static final char PLAYER = '@';
    private static final char PLAYER_ON_GOAL = '+';

    /** Caracteres válidos en una fila de tablero. */
    static final String VALID_CHARS = "#.$*@+ -_";

    /**
     * @param name nombre del nivel
     * @param rows filas del tablero, de arriba abajo
     * @throws InvalidLevelException si el tablero está mal formado
     */
    Level parse(String name, List<String> rows) {
        if (rows.isEmpty()) {
            throw new InvalidLevelException("El nivel '" + name + "' no tiene tablero");
        }
        Terrain[][] grid = new Terrain[rows.size()][];
        Set<Position> boxes = new HashSet<>();
        Position player = null;

        for (int y = 0; y < rows.size(); y++) {
            String row = rows.get(y);
            grid[y] = new Terrain[row.length()];
            for (int x = 0; x < row.length(); x++) {
                char symbol = row.charAt(x);
                Position position = new Position(x, y);
                grid[y][x] = terrainFor(symbol, name, position);
                if (symbol == BOX || symbol == BOX_ON_GOAL) {
                    boxes.add(position);
                }
                if (symbol == PLAYER || symbol == PLAYER_ON_GOAL) {
                    if (player != null) {
                        throw new InvalidLevelException("El nivel '" + name + "' tiene más de un jugador");
                    }
                    player = position;
                }
            }
        }
        if (player == null) {
            throw new InvalidLevelException("El nivel '" + name + "' no tiene jugador");
        }
        markOutside(grid, player);
        return new Level(name, new Board(grid), new GameState(player, boxes));
    }

    private static Terrain terrainFor(char symbol, String levelName, Position position) {
        return switch (symbol) {
            case WALL -> Terrain.WALL;
            case GOAL, BOX_ON_GOAL, PLAYER_ON_GOAL -> Terrain.GOAL;
            case BOX, PLAYER, ' ', '-', '_' -> Terrain.FLOOR;
            default -> throw new InvalidLevelException(
                    "Símbolo desconocido '" + symbol + "' en " + position + " del nivel '" + levelName + "'");
        };
    }

    /**
     * Marca como {@link Terrain#OUTSIDE} el suelo al que el jugador no puede
     * llegar aunque no hubiera cajas (BFS por casillas que no son muro).
     */
    private static void markOutside(Terrain[][] grid, Position start) {
        boolean[][] reachable = new boolean[grid.length][];
        for (int y = 0; y < grid.length; y++) {
            reachable[y] = new boolean[grid[y].length];
        }
        Deque<Position> pending = new ArrayDeque<>();
        pending.add(start);
        reachable[start.y()][start.x()] = true;

        while (!pending.isEmpty()) {
            Position current = pending.poll();
            for (Direction direction : Direction.values()) {
                Position next = current.step(direction);
                if (isInside(grid, next)
                        && !reachable[next.y()][next.x()]
                        && grid[next.y()][next.x()] != Terrain.WALL) {
                    reachable[next.y()][next.x()] = true;
                    pending.add(next);
                }
            }
        }

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == Terrain.FLOOR && !reachable[y][x]) {
                    grid[y][x] = Terrain.OUTSIDE;
                }
            }
        }
    }

    private static boolean isInside(Terrain[][] grid, Position p) {
        return p.y() >= 0 && p.y() < grid.length && p.x() >= 0 && p.x() < grid[p.y()].length;
    }
}
