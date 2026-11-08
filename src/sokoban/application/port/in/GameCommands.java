package sokoban.application.port.in;

import java.util.Optional;
import sokoban.application.InvalidSolutionException;
import sokoban.application.port.out.LevelLoadException;
import sokoban.application.port.out.LevelRepository;
import sokoban.domain.Direction;

/**
 * Puerto de entrada: las órdenes que puede dar el jugador.
 *
 * <p>La interfaz gráfica depende de esta interfaz y no de la clase concreta
 * {@code GameService}. Así se puede probar la interfaz con un doble y cambiar
 * la implementación sin tocarla.</p>
 *
 * <p>Usa {@link Direction} del dominio directamente: en Clean Architecture las
 * capas externas pueden conocer las internas. Crear un DTO idéntico solo para
 * la dirección no aportaría nada.</p>
 */
public interface GameCommands {

    /** Mueve al jugador; no hace nada si el nivel ya está resuelto. */
    void move(Direction direction);

    /** Deshace el último movimiento. */
    void undo();

    /** Reinicia el nivel actual. */
    void restart();

    /** Pasa al siguiente nivel, si existe. */
    void nextLevel();

    /** Vuelve al nivel anterior, si existe. */
    void previousLevel();

    /**
     * Salta a un nivel concreto.
     *
     * @param levelNumber número de nivel empezando en 1, como lo ve el jugador
     * @throws IllegalArgumentException si el número está fuera de rango
     */
    void goToLevel(int levelNumber);

    /** Número total de niveles disponibles. */
    int levelCount();

    /**
     * Sustituye la colección de niveles por otra y continúa en el último
     * nivel jugado de esa colección.
     *
     * @throws LevelLoadException si la colección no se puede leer o no tiene
     *                            niveles válidos; en ese caso no cambia nada
     */
    void loadCollection(LevelRepository repository);

    /**
     * Reinicia el nivel y prepara la reproducción de una solución en LURD.
     * Los pasos se ejecutan con {@link #replayStep()}. Una solución escrita
     * toda en minúsculas también se acepta.
     *
     * @throws InvalidSolutionException si el texto no es LURD válido o no
     *                                  encaja con el nivel; en ese caso no cambia nada
     */
    void startReplay(String lurd);

    /**
     * Ejecuta el siguiente paso de la reproducción.
     *
     * @return {@code true} si quedan más pasos
     */
    boolean replayStep();

    /** Detiene la reproducción en curso, dejando el tablero como esté. */
    void stopReplay();

    /** Los movimientos de la partida actual en LURD. */
    String currentSolution();

    /** La solución del récord del nivel actual, si existe. */
    Optional<String> bestSolution();
}
