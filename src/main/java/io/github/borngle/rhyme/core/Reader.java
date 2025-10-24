/*********************************************************************
 * @file        Reader.java
 * @author      Aidan
 * @date        24-10-2025
 * @brief       Parses a MIDI file into a Music object
 *
 * @details     Separates MIDI events into data that make up a Music object
 *
 * @note        None
 *
 * @references  None
 *********************************************************************/

package io.github.borngle.rhyme.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.*;

public class Reader {
    public static void main(String[] args) {
        // Testing
        File song = new File("midi/Drake Nick — Day Is Done [MIDIfind.com].mid");
        int[] tuning = new int[]{40, 45, 50, 55, 59, 64}; // Standard tuning (EADGBE) - ordered from lowest to highest
        Music music = read(song);
        for(int i = 0; i < music.getNotes().size(); i++) {
            Map<Integer, Integer> fretPositions =  music.getNotes().get(i).getFretPositions(tuning);
            System.out.println(music.getNotes().get(i) + " can be played on:");
            for(Integer string : fretPositions.keySet()) {
                System.out.println("- String " + string + " at fret " + fretPositions.get(string));
            }
            System.out.println();
        }
    }

    public static Music read(File file) {
        try {
            Sequence sequence = MidiSystem.getSequence(file); // Load MIDI
            Track[] tracks = sequence.getTracks(); // Get all tracks from sequence
            Music music = new Music(sequence.getResolution(), sequence.getTickLength());
            for (int i = 0; i < tracks.length; i++) {
                Map<Integer, Note> activeNotes = new HashMap<>(); // Tracks currently playing notes
                for (int j = 0; j < tracks[i].size(); j++) {
                    MidiEvent event = tracks[i].get(j); // Get MidiEvent from track
                    MidiMessage message = event.getMessage(); // Get MidiMessage
                    long tick = event.getTick();
                    if (message instanceof ShortMessage) { // Refers to the channel and note 
                        ShortMessage shortMessage = (ShortMessage) message;
                        int command = shortMessage.getCommand();
                        int pitch = shortMessage.getData1();
                        int velocity = shortMessage.getData2();
                        // velocity > 0 means start note
                        if(command == ShortMessage.NOTE_ON && velocity > 0) {
                            Note note = new Note(pitch, tick, velocity);
                            activeNotes.put(pitch, note);
                            music.addNote(note);
                        }
                        // velocity == 0 means note off
                        else if (command == ShortMessage.NOTE_OFF || command == ShortMessage.NOTE_ON && velocity == 0) {
                            Note note = activeNotes.remove(pitch);
                            note.setDuration(tick - note.getStart());
                        }
                    }
                }
            }
            return music;
        }
        catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
