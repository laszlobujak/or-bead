import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MusicBoxHandler implements Runnable {
    private Socket socket;
    private final Map<String, Song> songs;
    private final List<CurrentPlay> listOfCurrentPlays;

    public MusicBoxHandler(Socket socket, Map<String, Song> songs, List<CurrentPlay> listOfCurrentPlays) {
        this.socket = socket;
        this.songs = songs;
        this.listOfCurrentPlays = listOfCurrentPlays;
    }

    @Override
    public void run() {
        System.out.println("Connected: " + socket);
        try (Scanner in = new Scanner(socket.getInputStream());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String command;
            while (in.hasNextLine()) {
                command = in.nextLine();

                switch (command.split(" ")[0]) {
                    case "play":
                        playSong(out, command);
                        break;

                    case "addlyrics":
                        addLyric(in, out, command);
                        break;

                    case "add":
                        addSong(in, out, command);
                        break;

                    case "stop":
                        stopSong(command);
                        break;

                    case "change":
                        changeSong(command);
                        break;

                    default:
                        out.println("Unknown command");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error:" + socket);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Closed: " + socket);
        }

    }

    private void playSong(PrintWriter out, String command) {
        String[] split = command.split(" ");
        String title = getSongNameFromCommand(command, 3);
        boolean songExists;
        synchronized (songs) {
            songExists = this.songs.containsKey(title);
        }

        if (songExists) {
            Song song;
            synchronized (songs) {
                song = this.songs.get(title);
            }

            CurrentPlay cr = new CurrentPlay(Integer.parseInt(split[1]), Integer.parseInt(split[2]), song, out);

            synchronized (listOfCurrentPlays) {
                this.listOfCurrentPlays.add(cr);
            }
            out.println("playing " + cr.getId());

            Thread t = new Thread(() -> {
                int restCounter = 0;
                String[] notes = cr.getSong().getNotes().split(" ");
                for (int i = 0; i < notes.length; i += 2) {
                    if (!cr.isStopped()) {
                        String note = notes[i].split("/")[0];
                        String lyric = generateLyric(cr, restCounter, i, note);
                        String toSend;

                        if (note.equals("R")) {
                            restCounter++;
                            toSend = " " + note + " " + lyric + ",";
                        } else {
                            toSend = " " + NoteProcessor.transpose(note, cr.getTranspose()) + " " + lyric + ",";
                        }
                        out.println(toSend);

                        try {
                            TimeUnit.MILLISECONDS.sleep(Integer.parseInt(notes[i + 1]) * cr.getTempo());
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        return;
                    }
                }
                //FIN if play is over
                out.println("FIN");
            });
            t.start();
        } //FIN if song doesn't exist
        else {
            out.println("FIN");
        }
    }

    private String generateLyric(CurrentPlay cr, int restCounter, int i, String note) {
        String lyric = "???";
        if (cr.getSong().getLyric() != null && !note.equals("R")
                && ((i / 2) - restCounter) < cr.getSong().getLyric().split(" ").length) {
            lyric = cr.getSong().getLyric().split(" ")[(i / 2) - restCounter];
        }
        return lyric;
    }

    private void changeSong(String command) {
        synchronized (listOfCurrentPlays) {
            for (CurrentPlay currentPlay : this.listOfCurrentPlays) {
                if (currentPlay.getId() == Integer.parseInt(command.split(" ")[1])) {
                    int transpose;
                    if (command.split(" ").length == 4) {
                        transpose = Integer.parseInt(command.split(" ")[3]);
                    } else {
                        transpose = 0;
                    }
                    currentPlay.setTranspose(transpose);
                    currentPlay.setTempo(Integer.parseInt(command.split(" ")[2]));
                }
            }
        }
    }

    private void stopSong(String command) {
        synchronized (listOfCurrentPlays) {
            for (CurrentPlay listOfCurrentPlay : this.listOfCurrentPlays) {
                if (listOfCurrentPlay.getId() == Integer.parseInt(command.split(" ")[1])) {
                    listOfCurrentPlay.setStopped(true);
                    listOfCurrentPlay.getPrintWriter().println("FIN");
                }
            }
        }
    }

    private void addSong(Scanner in, PrintWriter out, String command) {
        String title = getSongNameFromCommand(command, 1);
        out.println("Enter the notes:");
        synchronized (songs) {
            this.songs.put(title, new Song(in.nextLine()));
        }
        out.println("Successfully uploaded the song to the server.");
    }

    private void addLyric(Scanner in, PrintWriter out, String command) {
        String title = getSongNameFromCommand(command, 1);
        Song songToAddLyricTo;
        synchronized (songs) {
            songToAddLyricTo = this.songs.get(title);
        }
        out.println("Enter the lyric:");

        if (songToAddLyricTo != null) {
            songToAddLyricTo.setLyric(in.nextLine());
        } else {
            out.println("Successfully added the lyric to the song.");
        }
    }

    private String getSongNameFromCommand(String command, int index) {
        StringBuilder sb = new StringBuilder();
        String[] split = command.split(" ");
        for (int i = index; i < split.length; i++) {
            sb.append(split[i]);
        }
        return sb.toString();
    }
}

