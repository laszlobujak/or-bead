import java.io.PrintWriter;

public class CurrentPlay {
    private static int idCounter = 0;
    private Song song;
    private int id;
    private int transpose;
    private int tempo;
    private volatile boolean stopped;
    private PrintWriter out;

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public CurrentPlay(int tempo, int transpose, Song song, PrintWriter out) {
        this.song = song;
        this.id = idCounter;
        this.transpose = transpose;
        this.out = out;
        this.tempo = tempo;
        this.stopped = false;
        synchronized (this) {
            idCounter++;
        }
    }

    public PrintWriter getPrintWriter() {
        return this.out;
    }

    public Song getSong() {
        return song;
    }

    public int getId() {
        return id;
    }

    public int getTranspose() {
        return transpose;
    }

    public void setTranspose(int transpose) {
        this.transpose = transpose;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }
}
