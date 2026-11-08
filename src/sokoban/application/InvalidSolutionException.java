package sokoban.application;

/**
 * Una solución en LURD no se puede leer o no encaja con el nivel actual.
 *
 * <p>El mensaje está pensado para mostrárselo al jugador tal cual.</p>
 */
public class InvalidSolutionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidSolutionException(String message) {
        super(message);
    }
}
