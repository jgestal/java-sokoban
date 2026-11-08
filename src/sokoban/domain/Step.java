package sokoban.domain;

import java.util.Objects;

/**
 * Un movimiento ya realizado: en qué dirección y si empujó una caja.
 *
 * <p>La secuencia de pasos de una partida es su solución. Se puede escribir
 * en notación LURD (minúscula = andar, mayúscula = empujar).</p>
 *
 * @param direction hacia dónde se movió el jugador
 * @param push      si el movimiento empujó una caja
 */
public record Step(Direction direction, boolean push) {

    public Step {
        Objects.requireNonNull(direction, "direction");
    }
}
