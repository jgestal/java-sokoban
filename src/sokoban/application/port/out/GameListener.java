package sokoban.application.port.out;

import sokoban.application.GameSnapshot;

/**
 * Puerto de salida: a quién avisar cuando cambia la partida (patrón
 * <em>Observer</em>).
 *
 * <p>La aplicación no sabe si al otro lado hay una ventana Swing, una consola
 * o un test. Solo envía una {@link GameSnapshot} inmutable con todo lo
 * necesario para pintar.</p>
 *
 * <p>Tiene un único método (segregación de interfaces), así que se puede
 * implementar con una lambda, lo que viene muy bien en los tests.</p>
 */
@FunctionalInterface
public interface GameListener {

    void onGameChanged(GameSnapshot snapshot);
}
