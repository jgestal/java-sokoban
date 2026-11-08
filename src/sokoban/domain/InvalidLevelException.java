package sokoban.domain;

/**
 * Se lanza cuando la descripción de un nivel rompe alguna regla del juego
 * (no hay jugador, una caja está sobre un muro, faltan objetivos…).
 *
 * <p>Es una excepción no comprobada porque representa datos de entrada
 * incorrectos, no una situación de la que el código llamante deba
 * recuperarse en cada llamada.</p>
 */
public class InvalidLevelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidLevelException(String message) {
        super(message);
    }
}
