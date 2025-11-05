/**
 * File: Note.java
 *
 * <p>Brief: Represents a note in a song.</p>
 *
 * <p>Note: All timing is considered in ticks.</p>
 *
 * @author Aidan
 * @since  24-10-2025
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
     * Gets all the possible fret positions a note can be
     * played at.
     *
     * <p>Takes in a {@code tuning} in pitch values, and for each string
     * evaluates the difference between the given pitch and
     * the string pitch, ignoring negative values or values
     * greater than the guitar standard of 24 frets.</p>
     *
     * <p>This is compatible with any tuning and instruments with a
     * varying number of strings.</p>
     *
     * @param tuning  the input guitar tuning
     * @return        a {@code Map} of fret positions for each string
     **/
    public Map<Integer, Integer> getFretPositions(int[] tuning) {
        Map<Integer, Integer> fretPositions = new HashMap<>(); // String is key and fret is value
        for(int i = 0; i < tuning.length; i++) {
            int fret = this.pitch - tuning[i];
            if(fret < 0 || fret > 12) { // Impossible or exceeds fret limit of 24
                continue;
            }
            fretPositions.put(i + 1, fret);
        }
        return fretPositions;
    }

    public static String getActualNote(int pitch) {
        return notes[pitch % 12];
    }

    public int getActualOctave() {
        return this.pitch / 12 - 1;
    }

    public int getBar(int resolution, int[] timeSignature) {
        int numerator = timeSignature[0];
        int denominator = timeSignature[1];
        double ticksPerBar = resolution * (4.0 / denominator) * numerator;
        return (int) (this.start / ticksPerBar) + 1; // Bars start from 1
    }

    @Override
    public String toString() {
        // Actual note and octave
        return getActualNote(this.pitch) + this.getActualOctave();
    }
}
