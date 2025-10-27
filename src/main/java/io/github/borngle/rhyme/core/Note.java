/**
 * @file        Note.java
 * @author      Aidan
 * @date        24-10-2025
 * @brief       Represents a note in a song
 *
 * @details     None
 *
 * @note        All timing is considered in ticks
 *
 * @references  None
 **/

package io.github.borngle.rhyme.core;

import java.util.HashMap;
import java.util.Map;

public class Note {
    final static String[] notes = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private final int pitch; // MIDI note number
    private final long start;
    private long duration;
    private final int velocity; // Hardness or softness of note

    public Note(int pitch, long start, long duration, int velocity) {
        this.pitch = pitch;
        this.start = start;
        this.duration = duration;
        this.velocity = velocity;
    }

    public Note(int pitch, long start, int velocity) {
        this.pitch = pitch;
        this.start = start;
        this.velocity = velocity;
    }

    public int getPitch() {
        return pitch;
    }

    public long getStart() {
        return start;
    }

    public long getDuration() {
        return duration;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @brief         Gets all the possible fret positions a note can be
     *                played at
     *
     * @details       Takes in a tuning in pitch values, and for each string
     *                evaluates the difference between the given pitch and
     *                the string pitch, ignoring negative values or values
     *                greater than the guitar standard of 24 frets
     *
     * @param tuning  The input guitar tuning
     * @return        A map of fret positions for each string
     *
     * @note          This is compatible with any tuning and instruments with a
     *                varying number of strings
     **/
    public Map<Integer, Integer> getFretPositions(int[] tuning) {
        Map<Integer, Integer> fretPositions = new HashMap<>(); // String is key and fret is value
        for(int i = 0; i < tuning.length; i++) {
            int fret = this.pitch - tuning[i];
            if(fret < 0 || fret > 24) { // Impossible or exceeds fret limit of 24
                continue;
            }
            fretPositions.put(tuning.length - i, fret);
        }
        return fretPositions;
    }

    @Override
    public String toString() {
        // Actual note and octave
        return notes[this.pitch % 12] + (this.pitch / 12 - 1);
    }
}
