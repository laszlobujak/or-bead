import java.io.Serializable;

public class Song implements Serializable {

    private String lyric;
    private String notes;

    public Song(String notes) {
        this.notes = processRawInput(notes);
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    private static String processRawInput(String rawData) {
        StringBuilder sb = new StringBuilder();
        String[] split = rawData.split(" ");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("REP")) {
                String[] numbers = split[i + 1].split(";");
                StringBuilder rep = new StringBuilder();
                int n = Integer.parseInt(numbers[0]);
                int m = Integer.parseInt(numbers[1]);
                //Going back n notes and appending to a StringBuilder
                for (int j = 0; j < n * 2; j += 2) {
                    int index = i - (n * 2) + j;
                    rep.append(split[index] + " " + split[index + 1] + " ");
                }
                //appending the rep StringBuilder to the Builder we return m times
                for (int j = 0; j < m; j++) {
                    sb.append(rep);
                }
                //This makes the loop jump over the part where the input defines n and m
                i++;
            } else {
                sb.append(split[i] + " ");
            }
        }
        return sb.toString();
    }
}
