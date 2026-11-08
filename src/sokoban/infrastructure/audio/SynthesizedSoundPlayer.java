package sokoban.infrastructure.audio;

import java.lang.System.Logger;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * Efectos de sonido sintetizados al arrancar con {@code javax.sound.sampled}.
 *
 * <p>No hay ficheros de audio: cada efecto es una pequeña secuencia de notas
 * que se genera como PCM una sola vez y se guarda en un {@link Clip}. Así el
 * juego sigue sin dependencias externas ni recursos con licencia aparte.</p>
 */
public final class SynthesizedSoundPlayer implements SoundPlayer {

    private static final Logger LOG = System.getLogger(SynthesizedSoundPlayer.class.getName());

    private static final float SAMPLE_RATE = 44_100f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    private static final double ATTACK_SECONDS = 0.003;

    /** Una nota: frecuencia en Hz, duración en milisegundos y volumen (0-1). */
    private record Note(double frequency, int millis, double volume) { }

    private final Map<Sound, Clip> clips = new EnumMap<>(Sound.class);

    private SynthesizedSoundPlayer() throws LineUnavailableException {
        for (Sound sound : Sound.values()) {
            byte[] pcm = render(notesFor(sound));
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, pcm, 0, pcm.length);
            clips.put(sound, clip);
        }
    }

    /** Crea el reproductor o, si no hay audio disponible, uno silencioso. */
    public static SoundPlayer createOrSilent() {
        try {
            return new SynthesizedSoundPlayer();
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            LOG.log(Logger.Level.WARNING, "Audio no disponible, el juego irá sin sonido: {0}", e.getMessage());
            return SILENT;
        }
    }

    @Override
    public void play(Sound sound) {
        Clip clip = clips.get(sound);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    private static List<Note> notesFor(Sound sound) {
        return switch (sound) {
            case STEP -> List.of(new Note(196, 28, 0.10));
            case PUSH -> List.of(new Note(110, 70, 0.35));
            case BOX_ON_GOAL -> List.of(new Note(659, 60, 0.25), new Note(988, 110, 0.25));
            case UNDO -> List.of(new Note(392, 40, 0.15), new Note(294, 50, 0.15));
            case SOLVED -> List.of(new Note(523, 110, 0.25), new Note(659, 110, 0.25),
                    new Note(784, 110, 0.25), new Note(1047, 260, 0.25));
        };
    }

    /** Ondas senoidales con ataque corto y caída exponencial, en PCM de 16 bits. */
    private static byte[] render(List<Note> notes) {
        int total = notes.stream().mapToInt(note -> samples(note.millis())).sum();
        byte[] pcm = new byte[total * 2];
        int offset = 0;
        for (Note note : notes) {
            int length = samples(note.millis());
            int attack = (int) (ATTACK_SECONDS * SAMPLE_RATE);
            for (int i = 0; i < length; i++) {
                double envelope = Math.min(1.0, i / (double) attack) * Math.exp(-4.0 * i / length);
                double value = Math.sin(2 * Math.PI * note.frequency() * i / SAMPLE_RATE) * envelope * note.volume();
                short sample = (short) Math.round(value * Short.MAX_VALUE);
                pcm[offset++] = (byte) sample;
                pcm[offset++] = (byte) (sample >> 8);
            }
        }
        return pcm;
    }

    private static int samples(int millis) {
        return (int) (SAMPLE_RATE * millis / 1000);
    }
}
