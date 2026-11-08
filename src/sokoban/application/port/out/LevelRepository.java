package sokoban.application.port.out;

import java.util.List;
import sokoban.domain.Level;

/**
 * Puerto de salida: de dónde salen los niveles.
 *
 * <p>Aplica la <b>inversión de dependencias</b> (la D de SOLID): la capa de
 * aplicación define lo que necesita y la infraestructura lo implementa. Hay
 * un adaptador para la colección incluida en el juego y otro para ficheros
 * elegidos por el jugador, y la lógica del juego no distingue entre ellos.</p>
 */
@FunctionalInterface
public interface LevelRepository {

    /**
     * Devuelve los niveles en el orden en que se juegan.
     *
     * @throws LevelLoadException si no se puede leer el origen de datos
     */
    List<Level> findAll();

    /** Nombre de la colección, para mostrarlo y para recordar por dónde se iba. */
    default String collectionName() {
        return "Niveles";
    }
}
