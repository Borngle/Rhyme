/**
 * @file        Reader.java
 * @author      Aidan
 * @date        24-10-2025
 * @brief       Utility class providing functions for parsing MIDI file data
 *
 * @details     Separates MIDI events from a song into Note objects, and fetches
 *              additional song metadata
 *
 * @note        None
 *
 * @references  None
 **/

package io.github.borngle.rhyme.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.*;

public class Reader {
    /**
     * @brief       Reads a MIDI file in, parses events, and returns a collection of
     *              Note objects
     *
     * @details     Iterates through the MIDI sequence and recognises note
     *              events
     *
     * @param file  The input MIDI file
     * @return      An ArrayList of Note objects in sequence
     *
     * @note        None
     **/
    public static ArrayList<Note> readSong(File file) {
        Sequence sequence;
        ArrayList<Note> notes = new ArrayList<>();
        try {
            sequence = MidiSystem.getSequence(file); // Load MIDI
        }
        catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }
        Track[] tracks = sequence.getTracks(); // Get all tracks from sequence
        // TODO: MERGE OR SEPARATE TRACKS
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
                        notes.add(note);
                    }
                    // velocity == 0 means note off
                    else if (command == ShortMessage.NOTE_OFF || command == ShortMessage.NOTE_ON && velocity == 0) {
                        Note note = activeNotes.remove(pitch);
                        note.setDuration(tick - note.getStart());
                    }
                }
            }
        }
        return notes;
    }

    /**
     * @brief       Reads a MIDI file in and gets the resolution
     *
     * @details     None
     *
     * @param file  The input MIDI file
     * @return      The song resolution (ticks per quarter note)
     *
     * @note        None
     **/
    public static int getResolution(File file) {
        int resolution;
        try {
            resolution = MidiSystem.getSequence(file).getResolution();
        }
        catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }
        return resolution;
    }

    /**
     * @brief       Reads a MIDI file in and gets the time signature
     *
     * @details     None
     *
     * @param file  The input MIDI file
     * @return      The time signature (beats per bar)
     *
     * @note        None
     **/
    public static int[] getTimeSignature(File file) {
        Sequence sequence;
        try {
            sequence = MidiSystem.getSequence(file);
        }
        catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }
        Track[] tracks = sequence.getTracks();
        for (int i = 0; i < tracks.length; i++) {
            for (int j = 0; j < tracks[i].size(); j++) {
                MidiEvent event = tracks[i].get(j); // Get MidiEvent from track
                MidiMessage message = event.getMessage(); // Get MidiMessage
                if (message instanceof MetaMessage) { // Refers to song data
                    MetaMessage metaMessage = (MetaMessage) message;
                    if (metaMessage.getType() == 0x58) { // Time signature
                        byte[] data = metaMessage.getData();
                        int numerator = data[0] & 0xFF; // Beats per bar
                        // Denominator is a power of 2
                        int denominator = (int) Math.pow(2, data[1] & 0xFF); // Note value that gets a beat, e.g., 4 or 8
                        return new int[]{numerator, denominator};
                    }
                }
            }
        }
        return new int[] {4, 4}; // Default is 4/4
    }
}
