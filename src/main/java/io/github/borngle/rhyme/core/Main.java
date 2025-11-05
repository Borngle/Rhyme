/**
 * File: Main.java
 *
 * <p>Brief: Entry point into the program.</p>
 *
 * <p>Details: Sequentially executes the steps required to transcribe a MIDI file to tablature format
 * and outputs the resulting tablature.</p>
 *
 * @author Aidan
 * @since  24-10-2025
 **/

package io.github.borngle.rhyme.core;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Testing
        String songName = "Drake Nick — Day Is Done [MIDIfind.com].mid";
        File song = new File("midi/" + songName);
        int resolution = Reader.getResolution(song);
        int[] timeSignature = Reader.getTimeSignature(song);
        int[] eStandard = new int[]{64, 59, 55, 50, 45, 40}; // Strings 1 to 6 - high to low
        ArrayList<Note> notes = Reader.readSong(song);
        Tablature tablature = new Tablature(eStandard);
        for(int i = 0; i < notes.size(); i++) {
            Map<Integer, Integer> fretPositions =  notes.get(i).getFretPositions(eStandard);
            int tablatureString = 0;
            for(Integer string : fretPositions.keySet()) {
                tablatureString = string;
            }
            tablature.addNote(notes.get(i), tablatureString, fretPositions.get(tablatureString));
        }
        String songTablature = TypeSetter.render(tablature, resolution, timeSignature);
        TypeSetter.print(songName, timeSignature, songTablature);
        //TypeSetter.writeFile(songName, timeSignature, songTablature);
    }
}
