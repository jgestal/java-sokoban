package sokoban.infrastructure.audio;

/**
 * Reproduce efectos de sonido.
 *
 * <p>Interfaz pequeña para que la ventana no dependa de cómo se generan los
 * sonidos y para poder usar {@link #SILENT} si no hay salida de audio.</p>
 */
@FunctionalInterface
public interface SoundPlayer {

    /** No reproduce nada. Se usa cuando el sistema no tiene audio. */
    SoundPlayer SILENT = sound -> { };

    /** Reproduce el sonido sin bloquear al llamante. */
    void play(Sound sound);
}
