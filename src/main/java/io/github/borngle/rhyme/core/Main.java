/**
 * @file        Main.java
 * @author      Aidan
 * @date        24-10-2025
 * @brief       Entry point into the program
 *
 * @details     Sequentially executes the steps required to transcribe a MIDI file to tablature format
 *              and outputs the resulting tablature
 *
 * @note        None
 *
 * @references  None
 **/

package io.github.borngle.rhyme.core;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Testing
        File song = new File("midi/Jansch Bert — Tinker's Blues [MIDIfind.com].mid");
        int resolution = Reader.getResolution(song);
        int timeSignature = Reader.getTimeSignature(song);
        int[] dropD = new int[]{64, 59, 55, 50, 45, 38}; // Strings 1 to 6 - high to low
        ArrayList<Note> notes = Reader.readSong(song);
        Tablature tablature = new Tablature(dropD);
        for(int i = 0; i < notes.size(); i++) {
            Map<Integer, Integer> fretPositions =  notes.get(i).getFretPositions(dropD);
            int tablatureString = 0;
            for(Integer string : fretPositions.keySet()) {
                tablatureString = string;
            }
            tablature.addNote(notes.get(i), tablatureString, fretPositions.get(tablatureString));
        }
        TypeSetter.render(tablature, resolution, timeSignature);
    }
}
