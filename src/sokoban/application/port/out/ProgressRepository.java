package sokoban.application.port.out;

import java.util.Optional;
import java.util.OptionalInt;
import sokoban.application.Score;

/**
 * Puerto de salida: dónde se guarda el progreso del jugador.
 *
 * <p>Guarda dos cosas: el récord de cada nivel (identificado por una huella
 * de su contenido) y el último nivel jugado de cada colección, para continuar
 * donde se dejó. La aplicación no sabe si va a un fichero, a una base de datos
 * o a memoria.</p>
 *
 * <p>Las implementaciones no deben lanzar excepciones al guardar: perder el
 * progreso es molesto, pero no debe interrumpir la partida.</p>
 */
public interface ProgressRepository {

    /** Récord guardado para el nivel. */
    Optional<Score> bestScore(String levelId);

    /** Guarda (o sustituye) el récord del nivel. */
    void saveBestScore(String levelId, Score score);

    /** Último nivel jugado de la colección (desde 1). */
    OptionalInt lastLevel(String collectionName);

    /** Recuerda el último nivel jugado de la colección. */
    void saveLastLevel(String collectionName, int levelNumber);
}
