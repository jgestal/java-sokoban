package sokoban.application;

import sokoban.domain.Board;
import sokoban.domain.GameState;
import sokoban.domain.Position;

/**
 * Cuadrícula inmutable de {@link Tile} lista para pintar.
 *
 * <p>Se usa una clase en lugar de exponer un {@code Tile[][]} porque los
 * arrays son mutables: quien recibiera la matriz podría modificarla. Aquí el
 * array es privado y solo se lee con {@link #tileAt(int, int)}.</p>
 */
public final class TileGrid {

    private final Tile[][] tiles;
    private final int width;
    private final int height;

    private TileGrid(Tile[][] tiles, int width, int height) {
        this.tiles = tiles;
        this.width = width;
        this.height = height;
    }

    /** Combina el plano fijo y el estado móvil en una cuadrícula de dibujo. */
    static TileGrid of(Board board, GameState state) {
        int width = board.width();
        int height = board.height();
        Tile[][] tiles = new Tile[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = tileFor(board, state, new Position(x, y));
            }
        }
        return new TileGrid(tiles, width, height);
    }

    private static Tile tileFor(Board board, GameState state, Position position) {
        // Un switch exhaustivo sobre el enum: si mañana se añade un terreno
        // nuevo, el compilador obliga a decidir cómo se pinta.
        return switch (board.terrainAt(position)) {
            case WALL -> Tile.WALL;
            case OUTSIDE -> Tile.OUTSIDE;
            case FLOOR -> objectTile(state, position, Tile.FLOOR, Tile.BOX, Tile.PLAYER);
            case GOAL -> objectTile(state, position, Tile.GOAL, Tile.BOX_ON_GOAL, Tile.PLAYER_ON_GOAL);
        };
    }

    private static Tile objectTile(GameState state, Position position, Tile empty, Tile box, Tile player) {
        if (state.hasBoxAt(position)) {
            return box;
        }
        if (state.player().equals(position)) {
            return player;
        }
        return empty;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Casilla a dibujar; {@link Tile#OUTSIDE} fuera de los límites. */
    public Tile tileAt(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return Tile.OUTSIDE;
        }
        return tiles[y][x];
    }
}
