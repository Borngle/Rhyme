/**
 * @file        Tablature.java
 * @author      Aidan
 * @date        28-10-2025
 * @brief       A representation of a song through a collection of TablatureNote objects
 *              and its tuning
 *
 * @details     TablatureNote is an inner class of Tablature which groups a Note with the
 *              selected string and fret
 *
 * @note        None
 *
 * @references  None
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
     * @brief                Sections a bar from a Tablature
     *
     * @details              Finds all the notes within a bar and returns those notes
     *
     * @param barNumber      The bar being looked at
     * @param resolution     The song resolution
     * @param timeSignature  The song time signature
     * @return               An ArrayList of TablatureNote objects in the given bar
     *
     * @note                 None
     **/
    public ArrayList<TablatureNote> getBar(int barNumber, int resolution, int[] timeSignature) {
        ArrayList<TablatureNote> bar = new ArrayList<>();
        for(int i = 0; i < this.notes.size(); i++) {
            if(this.notes.get(i).getNote().getBar(resolution, timeSignature) == barNumber) {
                bar.add(this.notes.get(i));
            }
        }
        return bar;
    }
}
