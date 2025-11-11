/**
 * File: Main.java
 *
 * <p>Brief: Entry point into the program.</p>
 *
 * <p>Details: Sequentially executes the steps required to transcribe a MIDI file to tablature format
 * and outputs the resulting tablature.</p>
 *
 * @author Aidan
 * @since 24-10-2025
 **/

package io.github.borngle.rhyme.core;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class Main {
    // For the song being transcribed
    static int resolution;
    static int[] timeSignature;

    public static void main(String[] args) {
        // Testing
        String songName = "Cohen Leonard — Suzanne [MIDIfind.com].mid";
        File song = new File("midi/" + songName);
        resolution = Reader.getResolution(song);
        timeSignature = Reader.getTimeSignature(song);
        int[] eStandard = new int[]{64, 59, 55, 50, 45, 40}; // Strings 1 to 6 - high to low
        ArrayList<ArrayList<Note>> songTracks = Reader.readSong(song);
        StringBuilder songTablature = new StringBuilder();
        System.out.println("Song: " + songName);
        System.out.println("Timing: " + timeSignature[0] + "/" + timeSignature[1]);
        for(int i = 0; i < songTracks.size(); i++) {
            if(songTracks.size() > 1) {
                songTablature.append("\nTrack: ").append(i + 1).append("\n");
            }
            ArrayList<Note> notes = songTracks.get(i);
            Tablature tablature = new Tablature(eStandard);
            for(int j = 0; j < notes.size(); j++) {
                Map<Integer, Integer> fretPositions =  notes.get(j).getFretPositions(eStandard);
                int tablatureString = 0;
                for(Integer string : fretPositions.keySet()) {
                    tablatureString = string;
                }
                tablature.addNote(notes.get(j), tablatureString, fretPositions.get(tablatureString));
            }
            songTablature.append(TypeSetter.render(tablature));
            //Optimiser optimiser = new Optimiser(100, notes, 0.6, 0.07);
        }
        System.out.println(songTablature);
        TypeSetter.writeFile(songName, songTablature.toString());
    }
}
