package sokoban.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import sokoban.application.port.out.ProgressRepository;

/** Doble de pruebas: guarda el progreso en mapas en memoria. */
final class InMemoryProgressRepository implements ProgressRepository {

    final Map<String, Score> best = new HashMap<>();
    final Map<String, Integer> last = new HashMap<>();

    @Override
    public Optional<Score> bestScore(String levelId) {
        return Optional.ofNullable(best.get(levelId));
    }

    @Override
    public void saveBestScore(String levelId, Score score) {
        best.put(levelId, score);
    }

    @Override
    public OptionalInt lastLevel(String collectionName) {
        Integer level = last.get(collectionName);
        return level == null ? OptionalInt.empty() : OptionalInt.of(level);
    }

    @Override
    public void saveLastLevel(String collectionName, int levelNumber) {
        last.put(collectionName, levelNumber);
    }
}
