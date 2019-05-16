import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicBox {
    private static Map<String, Song> songs = new HashMap<>();
    private static List<CurrentPlay> listOfCurrentPlays = new ArrayList<>();

    public static void main(String[] args) {

        songs.put("boci", new Song("C 4 E 4 C 4 E 4 G 8 G 8 REP 6;1 C/1 4 B 4 A 4 G 4 F 8 A 8 G 4 F 4 E 4 D 4 C 8 C 8"));
        songs.put("valami", new Song("D 1 D 3 D/1 1 D/1 3 C/1 1 C/1 3 C/1 2 C/1 2 D/1 1 D/1 3 C/1 1 Bb 3 A 4 A 2 R 2 REP 15;1 Bb 4 A 2 G 2 F 1 F 3 E 2 D 2 G 2 G 2 C/1 2 Bb 2 A 4 D/1 2 R 2 C/1 1 Bb 3 A 2 G 2 G 1 A 3 G 2 F 2 A 1 G 3 F# 2 Eb 2 D 4 D 2 R 2"));

        try (ServerSocket listener = new ServerSocket(40000)) {
            System.out.println("The music server is running...");
            while (true) {
                new Thread(new MusicBoxHandler(listener.accept(), songs, listOfCurrentPlays)).start();
            }
        } catch (IOException ignored) {
        }
    }


}
