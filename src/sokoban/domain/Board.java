package sokoban.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Plano inmutable de un nivel: el {@link Terrain} de cada casilla.
 *
 * <p>Como el plano no cambia nunca, se puede compartir entre partidas y
 * reinicios sin copiarlo. Las consultas fuera de los límites devuelven
 * {@link Terrain#OUTSIDE} en lugar de lanzar una excepción, así las reglas de
 * movimiento no necesitan comprobar los bordes por separado.</p>
 */
public final class Board {

    private final Terrain[][] grid;
    private final int width;
    private final int height;
    private final Set<Position> goals;

    /**
     * @param grid filas del tablero; pueden tener longitudes distintas (las
     *             casillas que falten se tratan como {@link Terrain#OUTSIDE}).
     *             Se hace una copia defensiva.
     */
    public Board(Terrain[][] grid) {
        Objects.requireNonNull(grid, "grid");
        if (grid.length == 0) {
            throw new InvalidLevelException("El tablero no tiene filas");
        }
        this.height = grid.length;
        this.grid = new Terrain[height][];
        int maxWidth = 0;
        Set<Position> foundGoals = new HashSet<>();
        for (int y = 0; y < height; y++) {
            this.grid[y] = grid[y].clone();
            maxWidth = Math.max(maxWidth, grid[y].length);
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] == Terrain.GOAL) {
                    foundGoals.add(new Position(x, y));
                }
            }
        }
        this.width = maxWidth;
        this.goals = Set.copyOf(foundGoals);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Terreno de la casilla; {@link Terrain#OUTSIDE} si cae fuera del tablero. */
    public Terrain terrainAt(Position position) {
        int x = position.x();
        int y = position.y();
        if (y < 0 || y >= height || x < 0 || x >= grid[y].length) {
            return Terrain.OUTSIDE;
        }
        return grid[y][x];
    }

    public boolean isWalkable(Position position) {
        return terrainAt(position).isWalkable();
    }

    /** Conjunto inmutable con las casillas objetivo. */
    public Set<Position> goals() {
        return goals;
    }
}
