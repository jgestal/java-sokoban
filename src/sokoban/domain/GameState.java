package sokoban.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Foto inmutable de la parte <strong>móvil</strong> de una partida: dónde está
 * el jugador y dónde están las cajas.
 *
 * <p>Decisión de diseño: ser inmutable hace que "deshacer" sea trivial y
 * barato. Basta con guardar el estado anterior en una pila (patrón
 * <em>Memento</em>). Solo se guardan el jugador y las cajas, nunca el
 * tablero completo.</p>
 *
 * @param player posición del jugador
 * @param boxes  posiciones de las cajas (se guarda una copia inmutable)
 */
public record GameState(Position player, Set<Position> boxes) {

    public GameState {
        Objects.requireNonNull(player, "player");
        boxes = Set.copyOf(Objects.requireNonNull(boxes, "boxes"));
        if (boxes.contains(player)) {
            throw new InvalidLevelException("El jugador no puede estar sobre una caja: " + player);
        }
    }

    public boolean hasBoxAt(Position position) {
        return boxes.contains(position);
    }

    /** Nuevo estado con el jugador en otra casilla y las mismas cajas. */
    GameState withPlayerAt(Position newPlayer) {
        return new GameState(newPlayer, boxes);
    }

    /** Nuevo estado en el que el jugador avanza y empuja la caja de {@code from} a {@code to}. */
    GameState withBoxPushed(Position newPlayer, Position from, Position to) {
        Set<Position> moved = new HashSet<>(boxes);
        moved.remove(from);
        moved.add(to);
        return new GameState(newPlayer, moved);
    }
}
