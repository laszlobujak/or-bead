public class NoteProcessor {

    public static int convertToPitch(String note) {
        String sym = "";
        int oct = 5; //because this is the default according to the example
        String[][] notes = {{"C"}, {"Db", "C#"}, {"D"}, {"Eb", "D#"}, {"E"},
                {"F"}, {"Gb", "F#"}, {"G"}, {"Ab", "G#"}, {"A"}, {"Bb", "A#"}, {"B"}};

        char[] splitNote = note.toCharArray();

        if (splitNote.length == 1) {// --> C
            sym += splitNote[0];
        } else if (splitNote.length == 2) {// --> C#
            sym += Character.toString(splitNote[0]);
            sym += Character.toString(splitNote[1]);
        } else if (splitNote.length == 3) {// --> C/2
            sym += splitNote[0];
            oct += Integer.parseInt(splitNote[2] + "");
        } else if (splitNote.length == 4) {
            if (splitNote[2] == '/') {// --> C#/2
                sym += Character.toString(splitNote[0]);
                sym += Character.toString(splitNote[1]);
                oct += Integer.parseInt(splitNote[3] + "");
            } else {//--> C/-2
                sym += Character.toString(splitNote[0]);
                oct -= Integer.parseInt(splitNote[3] + "");
            }

        } else if (splitNote.length == 5) {// --> C#/-2
            sym += Character.toString(splitNote[0]);
            sym += Character.toString(splitNote[1]);
            oct -= Integer.parseInt(splitNote[4] + "");
        }

        // Find the corresponding note in the array.
        for (int i = 0; i < notes.length; i++)
            for (int j = 0; j < notes[i].length; j++) {
                if (notes[i][j].equals(sym)) {
                    return Character.getNumericValue(oct + '0') * 12 + i;
                }
            }
        return -1;
    }

    public static String transpose(String note, int transpose) {
        String[] split = note.split("/");
        String transposedNote = "";
        int octave = 0;

        if (split.length == 2) octave = Integer.parseInt(split[1]);

        String[][] notes = {{"C"}, {"Db", "C#"}, {"D"}, {"Eb", "D#"}, {"E"},
                {"F"}, {"Gb", "F#"}, {"G"}, {"Ab", "G#"}, {"A"}, {"Bb", "A#"}, {"B"}};
        for (int i = 0; i < notes.length; i++)
            for (int j = 0; j < notes[i].length; j++) {
                if (notes[i][j].equals(split[0])) {
                    transposedNote = notes[Math.floorMod(i + transpose, notes.length)][0];
                    octave += Math.floor((double) (i + transpose) / notes.length);
                }
            }
        return octave == 0 ? transposedNote : transposedNote + "/" + octave;
    }

}