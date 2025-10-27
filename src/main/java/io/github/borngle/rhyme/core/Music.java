/**
 * @file        Music.java
 * @author      Aidan
 * @date        24-10-2025
 * @brief       Represents a song being transcribed
 *
 * @details     None
 *
 * @note        All timing is considered in ticks
 *
 * @references  None
 **/

package io.github.borngle.rhyme.core;

import java.util.ArrayList;

public class Music {
    private ArrayList<Note> notes;
    private final int resolution; // Timing - ticks per quarter note
    private final long duration;

    public Music(int resolution, long duration) {
        this.notes = new ArrayList<>();
        this.resolution = resolution;
        this.duration = duration;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public int getResolution() {
        return resolution;
    }

    public long getDuration() {
        return duration;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    public void addNote(Note note) {
        this.notes.add(note);
    }
}
