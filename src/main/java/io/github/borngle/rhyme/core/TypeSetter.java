/**
 * File: TypeSetter.java
 *
 * <p>Brief: Converts a {@link Tablature} object into ASCII format.</p>
 *
 * <p>Details: Provides functions for generating and outputting a guitar tablature.</p>
 *
 * @author Aidan
 * @since 29-10-2025
 **/

package io.github.borngle.rhyme.core;

/*
Example format:
E |-----------0---|
B |--2---2-----2--|
G |--4---4-----4--|
D |--2---2-2---2-2|
A |--0---0-----0--|
E |0----0---0-0---|
 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static io.github.borngle.rhyme.core.Main.resolution;
import static io.github.borngle.rhyme.core.Main.timeSignature;

public class TypeSetter {
    /**
     * Builds and returns a {@code String} tablature.
     *
     * <p>Creates tablatures bar by bar and merges them at the end.</p>
     *
     * @param tablature the {@link Tablature} object
     * @return a formatted {@code String} tablature
     **/
    public static String render(Tablature tablature) {
        int totalBars = tablature.getNotes().getLast().getBar();
        // 4 / denominator converts denominator note value to quarter note value
        int ticksPerBar = (int) (resolution * ((4.0 / timeSignature[1]) * timeSignature[0]));
        int ticksPerSubdivision = resolution / 8; // 1/32nd notes (8 per quarter)
        int subdivisions = ticksPerBar / ticksPerSubdivision;
        StringBuilder finalTablature = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            int barStartTick = i * ticksPerBar; // Tick position of first beat of bar
            ArrayList<Tablature.TablatureNote> bar = tablature.getBar(i + 1);
            String[][] barTablature = new String[tablature.getTuning().length][subdivisions];
            for (String[] strings : barTablature) {
                Arrays.fill(strings, "--");
            }
            if(bar.isEmpty()) { // Ignore empty bars
                continue;
            }
            for(int j = 0; j < bar.size(); j++) {
                Tablature.TablatureNote tablatureNote = bar.get(j);
                int ticksIntoBar = (int) (tablatureNote.getNote().getStart() - barStartTick);
                double fractionOfBar = (double) ticksIntoBar / ticksPerBar;
                int gridPosition = (int) Math.floor(fractionOfBar * subdivisions) + 2; // + 2 to account for string note and "|"
                gridPosition = Math.min(gridPosition, subdivisions - 2); // Bounds check
                String fret = String.valueOf(tablatureNote.getFret());
                if (fret.length() < 2) { // Single digit fret, so needs an additional "-" to keep tablature width consistent
                    fret = fret + "-";
                }
                barTablature[tablatureNote.getStringIndex() - 1][gridPosition] = fret;
            }
            for(int k = 0; k < tablature.getTuning().length; k++) { // Formatting tablature (tuning and pipe symbols)
                String note = Note.getActualNote(tablature.getTuning()[k]);
                if(note.length() == 1) { // Accounting for shift caused by sharp notes
                    note += " ";
                }
                barTablature[k][0] = note;
                barTablature[k][1] = "|";
                barTablature[k][subdivisions - 1] = "|";
            }
            for(int l = 0; l < barTablature.length; l++) { // Building bar tablature
                for(int m = 0; m < barTablature[l].length; m++) {
                    finalTablature.append(barTablature[l][m]);
                }
                finalTablature.append("\n");
            }
            if(i < totalBars - 1) { // Avoids additional newline at the end
                finalTablature.append("\n");
            }
        }
        return finalTablature.toString();
    }

    /**
     * Writes a {@code String} tablature and song metadata to a file.
     *
     * <p>Uses {@code FileWriter} to create or overwrite a text file.</p>
     *
     * @param song the song name
     * @param tablature the song tablature
     **/
    public static void writeFile(String song, String tablature) {
        try (FileWriter fileWriter = new FileWriter(song + ".txt", false)) {
            fileWriter.write("Song: " + song + "\n" + "Timing: " + timeSignature[0] + "/" + timeSignature[1] + "\n\n" + tablature);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}