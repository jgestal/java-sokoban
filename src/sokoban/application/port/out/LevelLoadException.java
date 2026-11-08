package sokoban.application.port.out;

/**
 * Error al obtener los niveles del origen de datos.
 *
 * <p>Se define en la capa de aplicación, junto al puerto, para que quien use
 * {@link LevelRepository} no tenga que conocer las excepciones concretas de la
 * infraestructura ({@code IOException}, errores de base de datos…).</p>
 */
public class LevelLoadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LevelLoadException(String message) {
        super(message);
    }

    public LevelLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
