package sokoban.application;

import java.util.ArrayList;
import java.util.List;
import sokoban.domain.Direction;
import sokoban.domain.Step;

/**
 * Notación estándar <b>LURD</b> para escribir soluciones de Sokoban.
 *
 * <p>Cada letra es un paso: {@code l} izquierda, {@code u} arriba, {@code r}
 * derecha, {@code d} abajo. En mayúscula significa que el paso empuja una
 * caja. Al leer se aceptan espacios y saltos de línea, y también un número
 * delante de una letra para repetirla ({@code 3r} = {@code rrr}), como hacen
 * muchos programas de Sokoban.</p>
 */
public final class Lurd {

    /** Límite de repeticiones para que un número absurdo no agote la memoria. */
    private static final int MAX_REPEAT = 10_000;

    private Lurd() {
    }

    /** Escribe los pasos en LURD. */
    public static String encode(List<Step> steps) {
        StringBuilder text = new StringBuilder(steps.size());
        for (Step step : steps) {
            char letter = letterFor(step.direction());
            text.append(step.push() ? Character.toUpperCase(letter) : letter);
        }
        return text.toString();
    }

    /**
     * Lee una solución en LURD.
     *
     * @return los pasos; {@link Step#push()} indica si el texto dice que es un empuje
     * @throws InvalidSolutionException si el texto tiene caracteres no válidos
     */
    public static List<Step> decode(String text) {
        List<Step> steps = new ArrayList<>();
        int repeat = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c >= '0' && c <= '9') {
                repeat = repeat * 10 + (c - '0');
                if (repeat > MAX_REPEAT) {
                    throw new InvalidSolutionException("Repetición demasiado grande en la posición " + (i + 1));
                }
                continue;
            }
            Step step = new Step(directionFor(c, i), Character.isUpperCase(c));
            for (int n = Math.max(1, repeat); n > 0; n--) {
                steps.add(step);
            }
            repeat = 0;
        }
        if (repeat != 0) {
            throw new InvalidSolutionException("La solución termina con un número sin movimiento");
        }
        return List.copyOf(steps);
    }

    static char letterFor(Direction direction) {
        return switch (direction) {
            case LEFT -> 'l';
            case UP -> 'u';
            case RIGHT -> 'r';
            case DOWN -> 'd';
        };
    }

    private static Direction directionFor(char c, int index) {
        return switch (Character.toLowerCase(c)) {
            case 'l' -> Direction.LEFT;
            case 'u' -> Direction.UP;
            case 'r' -> Direction.RIGHT;
            case 'd' -> Direction.DOWN;
            default -> throw new InvalidSolutionException(
                    "Carácter no válido '" + c + "' en la posición " + (index + 1) + " (se esperaba l, u, r o d)");
        };
    }
}
