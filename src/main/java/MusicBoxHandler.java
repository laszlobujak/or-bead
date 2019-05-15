import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MusicBoxHandler implements Runnable {
    private Socket socket;

    public MusicBoxHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Connected: " + socket);
        try (Scanner in = new Scanner(socket.getInputStream());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String command;
            while (true) {
                command = in.nextLine();

                switch (command.split(" ")[0]) {
                    case "play":
                        if (command.split(" ").length >= 4) {
                            String[] split = command.split(" ");
                            String title = getSongNameFromCommand(command, 3);
                            if (MusicBox.songs.containsKey(title)) {
                                CurrentPlay cr = new CurrentPlay(Integer.parseInt(split[1]), Integer.parseInt(split[2]), MusicBox.songs.get(title), socket);
                                MusicBox.listOfCurrentPlays.add(cr);
                                out.println("playing " + cr.getId());
                                Thread t = new Thread(() -> {
                                    int restCounter = 0;
                                    for (int i = 0; i < cr.getSong().getNotes().split(" ").length; i += 2) {
                                        if (!cr.isStopped()) {
                                            String note = cr.getSong().getNotes().split(" ")[i].split("/")[0];

                                            //[(i / 2) - restCounter]
                                            //C 4 E 4 R 3 R 3 C 4
                                            //Bo ci bo
                                            // 0. -> (0/2) - 0 = 0 -> lyrics array 0. index
                                            // 2. -> (2/2) - 0 = 1 -> lyrics array 1. index
                                            // 4. -> restCounter++
                                            // 6. -> restCounter++
                                            // 8. -> (8/2) - 2 = 2 -> lyrics array 2. index
                                            String lyric = "???";
                                            if (cr.getSong().getLyric() != null && !note.equals("R") && ((i / 2) - restCounter) < cr.getSong().getLyric().split(" ").length) {
                                                lyric = cr.getSong().getLyric().split(" ")[(i / 2) - restCounter];
                                            }

                                            //if it's a rest send it to the client and increment restcounter, if not, transpose it and then send is to the client
                                            String toSend;
                                            if (note.equals("R")) {
                                                restCounter++;
                                                toSend = " " + note + " " + lyric + ",";
                                            } else {
                                                toSend = " " + NoteProcessor.transpose(note, cr.getTranspose()) + " " + lyric + ",";
                                            }
                                            out.println(toSend);
                                            out.flush();
                                            try {
                                                Thread.currentThread().sleep(Integer.parseInt(cr.getSong().getNotes().split(" ")[i + 1] + "") * cr.getTempo());
                                            } catch (InterruptedException e1) {
                                                e1.printStackTrace();
                                            }
                                        } else {
                                            return;
                                        }
                                    }
                                    out.println("FIN");
                                });
                                t.start();
                            }
                        } else {
                            out.println("FIN");
                        }
                        break;

                    case "addlyrics":
                        String title = getSongNameFromCommand(command, 1);
                        if (MusicBox.songs.containsKey(title)) {
                            out.println("Enter the lyric:");
                            MusicBox.songs.get(title).setLyric(in.nextLine());
                            out.println("Successfully added the lyric to the song.");
                        }
                        break;

                    case "add":
                        String title1 = getSongNameFromCommand(command, 1);
                        out.println("Enter the notes:");
                        synchronized (this) {
                            MusicBox.songs.put(title1, new Song(in.nextLine()));
                        }
                        out.println("Successfully uploaded the song to the server.");
                        break;

                    case "stop":
                        if (command.split(" ").length == 2) {
                            for (int i = 0; i < MusicBox.listOfCurrentPlays.size(); i++) {
                                if (MusicBox.listOfCurrentPlays.get(i).getId() == Integer.parseInt(command.split(" ")[1])) {
                                    MusicBox.listOfCurrentPlays.get(i).setStopped(true);
                                    new PrintWriter(MusicBox.listOfCurrentPlays.get(i).getSocket().getOutputStream(), true).println("FIN");
                                }
                            }
                        }
                        break;

                    case "change":
                        for (int i = 0; i < MusicBox.listOfCurrentPlays.size(); i++) {
                            if (MusicBox.listOfCurrentPlays.get(i).getId() == Integer.parseInt(command.split(" ")[1])) {
                                MusicBox.listOfCurrentPlays.get(i).setTempo(Integer.parseInt(command.split(" ")[2]));
                                int transpose;
                                if (command.split(" ").length == 4) {
                                    transpose = Integer.parseInt(command.split(" ")[3]);
                                } else {
                                    transpose = 0;
                                }
                                MusicBox.listOfCurrentPlays.get(i).setTranspose(transpose);
                            }
                        }
                        break;

                    default:
                        out.println("Unknown command");
                        break;
                }
            }
        } catch (
                Exception e) {
            System.out.println("Error:" + socket);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.out.println("Closed: " + socket);

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

