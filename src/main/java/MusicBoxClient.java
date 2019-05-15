import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MusicBoxClient {

    private MidiPlayer midiPlayer;

    private void run(String tempo, String transpose, String song) {
        try (Socket socket = new Socket("127.0.0.1", 40000);
             Scanner in = new Scanner(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {
            out.println("play " + tempo + " " + transpose + " " + song);

            Thread senderThread = new Thread(() -> {
                String line;
                try {
                    while ((line = stdIn.readLine()) != null) {
                        out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

            Thread receiverThread = new Thread(() -> {
                while (in.hasNextLine()) {
                    String line = in.nextLine();
                    System.out.println(line);
                    if (line.startsWith("playing")) {
                        midiPlayer = new MidiPlayer();
                        line = in.nextLine();
                        do {
                            midiPlayer.playNote(NoteProcessor.convertToPitch(line.split(" ")[1]));
                            System.out.print(line);
                            line = in.nextLine();
                        } while (!line.equals("FIN"));
                        System.out.print(line);
                        midiPlayer.close();
                    }
                }
            });

            receiverThread.start();
            senderThread.start();

            senderThread.join();
            receiverThread.join();


        } catch (UnknownHostException e) {
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) throws Exception {
        MusicBoxClient client = new MusicBoxClient();
        client.run(args[0], args[1], args[2]);
    }

}
