/**
 * File: Tablature.java
 *
 * <p>Brief: A representation of a song through a collection of {@link TablatureNote}
 * objects and its {@code tuning}.</p>
 *
 * <p>Details: {@link TablatureNote} is an inner class of {@link Tablature}
 * which groups a {@link Note} with the selected {@code stringIndex} and {@code fret}.</p>
 *
 * @author Aidan
 * @since 28-10-2025
 **/

package io.github.borngle.rhyme.core;

import java.util.ArrayList;

public class Tablature {
    class TablatureNote {
        private final Note note;
        private int stringIndex;
        private int fret;

        public TablatureNote(Note note, int stringIndex, int fret) {
            this.note = note;
            this.stringIndex = stringIndex;
            this.fret = fret;
        }

        public Note getNote() {
            return note;
        }

        public int getStringIndex() {
            return stringIndex;
        }

        public int getFret() {
            return fret;
        }

        public void setStringIndex(int stringIndex) {
            this.stringIndex = stringIndex;
        }

        public void setFret(int fret) {
            this.fret = fret;
        }

        @Override
        public String toString() {
            return "Note: " + this.note + ", String: " + this.stringIndex + ", Fret: " + this.fret;
        }
    }

    private ArrayList<TablatureNote> notes;
    private int[] tuning;

    public Tablature(int[] tuning) {
        this.notes = new ArrayList<>();
        this.tuning = tuning;
    }

    public ArrayList<TablatureNote> getNotes() {
        return notes;
    }

    public int[] getTuning() {
        return tuning;
    }

    public void setTuning(int[] tuning) {
        this.tuning = tuning;
    }

    public void addNote(Note note, int string, int fret) {
        TablatureNote tablatureNote = new TablatureNote(note, string, fret);
        this.getNotes().add(tablatureNote);
    }

    /**
     * Sections a bar from {@code notes} in a {@link Tablature}.
     *
     * <p>Finds all the notes within a bar and returns an {@code ArrayList} of those notes.</p>
     *
     * @param barNumber the bar being looked at
     * @return an {@code ArrayList} of {@link TablatureNote}
     * objects in the given bar
     **/
    public ArrayList<TablatureNote> getBar(int barNumber) {
        ArrayList<TablatureNote> bar = new ArrayList<>();
        for(int i = 0; i < this.notes.size(); i++) {
            if(this.notes.get(i).getNote().getBar() == barNumber) {
                bar.add(this.notes.get(i));
            }
        }
        return bar;
    }
}
