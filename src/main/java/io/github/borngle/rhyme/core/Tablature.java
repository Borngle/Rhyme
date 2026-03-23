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
import java.util.Map;
import java.util.stream.Collectors;

public class Tablature implements Comparable<Tablature> {
    // Base tunings
    final static int[] eStandard = new int[]{64, 59, 55, 50, 45, 40};
    final static int[] dStandard = new int[]{62, 57, 53, 48, 43, 38};
    final static int[] bStandard = new int[]{59, 54, 50, 45, 40, 35};
    final static int[] openG = new int[]{67, 62, 55, 50, 43, 38};
    final static int[] openD = new int[]{62, 57, 54, 50, 45, 38};
    final static int[] openC = new int[]{64, 60, 55, 48, 43, 36};
    final static int[] openA = new int[]{69, 64, 57, 52, 45, 40};
    final static int[] openE = new int[]{64, 59, 56, 52, 47, 40};
    final static int[] openF = new int[]{65, 60, 53, 48, 45, 41};
    final static int[] halfStepDown = new int[]{63, 58, 54, 49, 44, 39};
    final static int[] halfStepUp = new int[]{65, 60, 56, 51, 46, 41};
    final static int[] dropD = new int[]{64, 59, 55, 50, 45, 38};
    final static int[] dropCSharp = new int[]{63, 58, 54, 49, 44, 37};
    final static int[] dropC = new int[]{62, 57, 53, 48, 43, 36};
    final static int[] dropB = new int[]{61, 56, 52, 47, 42, 35};
    final static int[] DADGAD = new int[]{62, 57, 55, 50, 45, 38};

    final static int[][] allTunings = new int[][]{
            eStandard, dStandard, bStandard, openG,
            openD, openC, openA, openE,
            openF, halfStepDown, halfStepUp, dropD,
            dropCSharp, dropC, dropB, DADGAD
    };

    final static int[][] commonTunings = new int[][]{
            eStandard, dropD, halfStepDown, openG, openD
    };

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

    private ArrayList<TablatureNote> tablatureNotes;
    private int[] tuning;
    private int capoFret;

    public Tablature(int[] tuning) {
        this.tablatureNotes = new ArrayList<>();
        this.tuning = tuning;
        this.capoFret = 0;
    }

    public Tablature(Tablature tablature) {
        this.tuning = tablature.tuning;
        this.tablatureNotes = new ArrayList<>();
        this.tablatureNotes.addAll(tablature.tablatureNotes);
        this.capoFret = tablature.capoFret;
    }

    public ArrayList<TablatureNote> getTablatureNotes() {
        return tablatureNotes;
    }

    public ArrayList<Note> getNotes() {
        return tablatureNotes.stream()
                .map(TablatureNote::getNote)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int[] getTuning() {
        return tuning;
    }

    public int getCapoFret() {
        return capoFret;
    }

    public void setTuning(int[] tuning) {
        this.tuning = tuning;
    }

    public void setCapoFret(int capoFret) {
        this.capoFret = capoFret;
    }

    public void addNote(Note note, int string, int fret) {
        TablatureNote tablatureNote = new TablatureNote(note, string, fret);
        this.tablatureNotes.add(tablatureNote);
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
        for(int i = 0; i < this.tablatureNotes.size(); i++) {
            if(this.tablatureNotes.get(i).getNote().getBar() == barNumber) {
                bar.add(this.tablatureNotes.get(i));
            }
        }
        return bar;
    }

    /**
     * Compares the score between a tablature without a capo and candidate capo fretted tablatures, and sets the
     * {@code capoFret} to the best scoring.
     */
    public void findCapoFret() {
        int maximumCapo = 7;
        int bestCapo = 0;
        int bestScore = Optimiser.fitness(this); // No capo
        int maximumFret = this.tablatureNotes // Highest fret
                .stream()
                .mapToInt(TablatureNote::getFret)
                .max().orElse(0);
        int upperBound = Math.min(maximumFret, maximumCapo);
        for(int i = 1; i <= upperBound; i++) { // Ignore 0 by default
            if (!isValidCapo(i)) {
                continue;
            }
            Tablature candidate = new Tablature(this);
            candidate.capoFret = i;
            candidate.transpose();
            int score = Optimiser.fitness(candidate);
            if (score > bestScore) {
                bestScore = score;
                bestCapo = i;
            }
        }
        this.capoFret = bestCapo;
    }

    /**
     * Takes a candidate capo fret, and checks if the notes in the song have alternate positions to support the
     * introduction of a capo.
     *
     * @param candidateCapo the potential capo fret
     * @return true if the song has a valid capo transposition at {@code candidateCapo}, false otherwise
     */
    private boolean isValidCapo(int candidateCapo) {
        for(int i = 0; i < this.tablatureNotes.size(); i++) {
            TablatureNote tablatureNote = this.tablatureNotes.get(i);
            Map<Integer, Integer> fretPositions = tablatureNote.getNote().getFretPositions(this.tuning);
            boolean hasAlternative = fretPositions.values().stream()
                    .anyMatch(fret -> fret >= candidateCapo);
            if(!hasAlternative) {
                return false;
            }
        }
        return true;
    }

    /**
     * Shifts every note to its lowest position at or above capo fret, and then subtracts note fret by capo to give
     * relative position for that capo fret.
     */
    public void transpose() {
        if (this.capoFret == 0) {
            return;
        }
        for(int i = 0; i < this.tablatureNotes.size(); i++) {
            TablatureNote tablatureNote = this.tablatureNotes.get(i);
            Map<Integer, Integer> fretPositions = tablatureNote.note.getFretPositions(this.tuning);
            // Finds the lowest valid fret at or above the capo fret on any string
            int bestFret = Integer.MAX_VALUE;
            int bestString = tablatureNote.stringIndex;
            for(Map.Entry<Integer, Integer> entry : fretPositions.entrySet()) {
                int fret = entry.getValue();
                if(fret >= this.capoFret && fret < bestFret) {
                    bestFret = fret;
                    bestString = entry.getKey();
                }
            }
            tablatureNote.setStringIndex(bestString);
            tablatureNote.setFret(bestFret - this.capoFret);
        }
    }

    /**
     * Checks if a tablature can be played in a given tuning.
     *
     * @param targetTuning the target tuning
     * @param notes the sequence of notes in the song
     * @return true if all notes have valid positions in the given tuning, false if even one note has none
     */
    public static boolean isValidTuning(int[] targetTuning, ArrayList<Note> notes) {
        for(int i = 0; i < notes.size(); i++) {
            Map<Integer, Integer> fretPositions = notes.get(i).getFretPositions(targetTuning);
            if(fretPositions.isEmpty()) {
               return false;
            }
        }
        return true;
    }

    public void map() {
        for(int i = 0; i < this.tablatureNotes.size(); i++) {
            TablatureNote tablatureNote = this.tablatureNotes.get(i);
            Map<Integer, Integer> fretPositions = tablatureNote.note.getFretPositions(this.tuning);
            int closestScore = Integer.MAX_VALUE;
            int bestFret = tablatureNote.fret;
            int bestString = tablatureNote.stringIndex;
            // For each note, should find the closest equivalent alternative fret to current position
            for(Map.Entry<Integer, Integer> entry : fretPositions.entrySet()) {
                int fret = entry.getValue();
                int string = entry.getKey();
                int fretDistance = Math.abs(tablatureNote.fret - fret);
                int stringDistance = Math.abs(tablatureNote.stringIndex - string);
                int distanceScore = fretDistance + 2 * stringDistance;
                if(distanceScore < closestScore) {
                    closestScore = distanceScore;
                    bestFret = fret;
                    bestString = string;

                }
            }
            tablatureNote.setStringIndex(bestString);
            tablatureNote.setFret(bestFret);
        }
    }

    @Override
    public int compareTo(Tablature other) {
        return Optimiser.fitness(other) - Optimiser.fitness(this);
    }
}
