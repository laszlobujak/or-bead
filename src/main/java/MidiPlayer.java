import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

public class MidiPlayer {
    private Synthesizer synth;
    private MidiChannel[] channels;
    private MidiChannel channel;

    public MidiPlayer() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channels = synth.getChannels();
            channel = channels[2];
        } catch (Exception e) {
        }
    }

    public void close() {
        synth.close();
        channels[0].allSoundOff();
        channels[0].allNotesOff();
        channel.allNotesOff();
        channel.allSoundOff();
    }

    public void playNote(int note) {
        if (note > 0) {
            channel.noteOn(note, 100);
        }
    }
}