/**
 * File: Optimiser.java
 *
 * <p>Brief: Uses a genetic algorithm to optimise the playability and accuracy of a {@link Tablature}.</p>
 *
 * @author Aidan
 * @since 05-11-2025
 **/

package io.github.borngle.rhyme.core;

import java.util.*;
import static io.github.borngle.rhyme.core.Main.*;
import static io.github.borngle.rhyme.core.Tablature.*;

public class Optimiser {
    private int populationSize;
    private ArrayList<Tablature> population;
    private final double mutationRate;

    /**
     * Generates a random initial population of tablatures.
     *
     * @param populationSize how many tablatures there are
     * @param notes the notes read from the MIDI file
     * @param mutationRate the rate at which mutation occurs
     **/
    public Optimiser(int populationSize, ArrayList<Note> notes, double mutationRate) {
        int[] tuning;
        this.populationSize = populationSize;
        this.population = new ArrayList<>();
        List<int[]> validTunings = Arrays.stream(allTunings)
                .filter(candidate -> isValidTuning(candidate, notes))
                .toList();
        for(int i = 0; i < populationSize; i++) {
            if(random.nextDouble() < 0.7 && validTunings.contains(eStandard)) { // 70% chance to be eStandard if eStandard is valid
                tuning = eStandard;
            }
            else {
                tuning = validTunings.get(random.nextInt(validTunings.size()));
            }
            this.population.add(this.generateTablature(notes, tuning));
        }
        this.mutationRate = mutationRate;
    }

    /**
     * Randomly generates a valid {@link Tablature} (genome) from its available fret positions.
     *
     * @param notes the sequence of musical notes making up a song
     * @return the generated {@link Tablature}
     */
    private Tablature generateTablature(ArrayList<Note> notes, int[] tuning) {
        Tablature tablature = new Tablature(tuning);
        for(int i = 0; i < notes.size(); i++) {
            Map<Integer, Integer> fretPositions = notes.get(i).getFretPositions(tuning);
            List<Map.Entry<Integer, Integer>> validEntries = fretPositions.entrySet()
                    .stream()
                    .toList();
            Map.Entry<Integer, Integer> randomEntry = validEntries.get(random.nextInt(validEntries.size()));
            tablature.addNote(notes.get(i), randomEntry.getKey(), randomEntry.getValue());
        }
        return tablature;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public ArrayList<Tablature> getPopulation() {
        return population;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    /**
     * Fitness function for evaluating the score (playability) of an input {@code tablature}.
     *
     * <p>Checks metrics such as average fret distance from surrounding frets, string jumps, and chord shape difficulty.</p>
     *
     * @param tablature the {@link Tablature} being evaluated
     * @return the fitness score of {@code tablature}
     **/
    public static int fitness(Tablature tablature) {
        int score = 0;
        int fretBiasPenalty = 0; // Bias towards lower end of fretboard
        int stringJumpPenalty = 0; // Penalising large string jumps
        int fretJumpPenalty = 0; // Penalising large fret jumps
        int spanPenalty = 0; // Penalising large fret span around notes
        int neighbourPenalty = 0; // Penalising average distance between notes
        int openReward = 0; // Rewarding open frets
        int tuningReward = 0; // Rewarding commonly used tunings
        ArrayList<Tablature.TablatureNote> tablatureNotes = tablature.getNotes();
        for(int i = 0; i < tablatureNotes.size(); i++) {
            Tablature.TablatureNote tablatureNote = tablatureNotes.get(i);
            if(tablatureNote.getFret() == 0) {
                openReward += 5;
            }
            fretBiasPenalty += (int) Math.pow(tablatureNote.getFret(), 2);
            spanPenalty += getSpanPenalty(i, tablatureNotes);
            neighbourPenalty += getAverageFretDistance(i, tablatureNotes);
            if(Arrays.asList(commonTunings).contains(tablature.getTuning())) {
                tuningReward += 50;
            }
            if(i != 0) { // If not the first note
                // Shouldn't penalise distance between a non-open fret and an open fret
                if(tablatureNote.getFret() != 0 && tablatureNotes.get(i - 1).getFret() != 0) {
                    fretJumpPenalty += (int) Math.pow((Math.abs(tablatureNote.getFret() - tablatureNotes.get(i - 1).getFret())), 2);
                    stringJumpPenalty += (Math.abs(tablatureNote.getStringIndex() - tablatureNotes.get(i - 1).getStringIndex()));
                }
            }
        }
        score += openReward + tuningReward - fretBiasPenalty - stringJumpPenalty - fretJumpPenalty - (5 * spanPenalty) - (8 * neighbourPenalty);
        return score;
    }

    /**
     * Calculates the span of frets in the window of notes around {@code note}, and penalises spans
     * wider than 4 frets.
     * @param note the current note
     * @param tablatureNotes the sequence of notes in the song
     * @return the span penalty
     */
    private static int getSpanPenalty(int note, ArrayList<Tablature.TablatureNote> tablatureNotes) {
        int surroundingNotes = 7;
        int start = Math.max(0, note - surroundingNotes / 2);
        int end = Math.min(tablatureNotes.size() - 1, note + surroundingNotes / 2);
        int minimumFret = Integer.MAX_VALUE;
        int maximumFret = 0;
        for (int i = start; i <= end; i++) {
            int fret = tablatureNotes.get(i).getFret();
            if (fret == 0) {
                continue;
            }
            minimumFret = Math.min(minimumFret, fret);
            maximumFret = Math.max(maximumFret, fret);
        }
        if (minimumFret == Integer.MAX_VALUE) {
            return 0;
        }
        int span = maximumFret - minimumFret;
        if(span > 4) {
            return (span - 4) * 3;
        }
        else {
            return 0;
        }
    }


    /**
     * Calculates the average distance between {@code note} and its surrounding notes.
     * @param note the current note
     * @param tablatureNotes the sequence of notes in the song
     * @return the average fret distance
     */
    private static int getAverageFretDistance(int note, ArrayList<Tablature.TablatureNote> tablatureNotes) {
        int surroundingNotes = 7;
        int averageFretDistance; // From number of surrounding neighbours
        int start = Math.max(0, note - surroundingNotes / 2); // Always above start of array
        int end = Math.min(tablatureNotes.size() - 1, note + surroundingNotes / 2); // Always before end of array
        // Expand window if not enough neighbours (still within bounds)
        while ((end - start) < surroundingNotes && end < tablatureNotes.size() - 1) {
            end++;
        }
        while ((end - start) < surroundingNotes && start > 0) {
            start--;
        }
        double totalDifference = 0;
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (i == note) {
                continue; // Skip current element
            }
            totalDifference += Math.abs(tablatureNotes.get(note).getFret() - tablatureNotes.get(i).getFret());
            count++;
        }
        if(count > 0) {
            averageFretDistance = (int) totalDifference / count;
        }
        else {
            averageFretDistance = 0;
        }
        return averageFretDistance;
    }

    /**
     * Combines two parent tablatures to form a new child tablature.
     *
     * <p>Randomly selects a point from {@code first}, where thereafter it is replaced by {@code second}.</p>
     *
     * @param first the first {@link Tablature}
     * @param second the second {@link Tablature}
     * @return the new combined {@link Tablature}
     **/
    public Tablature crossover(Tablature first, Tablature second) {
        int firstCrossoverPoint = random.nextInt(first.getNotes().size());
        int secondCrossoverPoint = random.nextInt(first.getNotes().size());
        int firstBar = first.getNotes().get(firstCrossoverPoint).getNote().getBar();
        int secondBar = first.getNotes().get(secondCrossoverPoint).getNote().getBar();
        if (firstBar > secondBar) { // Ensuring first crossover point is before second
            int temp = firstBar;
            firstBar = secondBar;
            secondBar = temp;
        }
        Tablature crossoverTablature = new Tablature(first.getTuning());
        // Adds all notes from first up to the end of the random bar crossover point, then from second
        for(int i = 0; i < first.getNotes().size(); i++) {
            int currentBar = first.getNotes().get(i).getNote().getBar();
            if(currentBar < firstBar || currentBar > secondBar) {
                Note note = first.getNotes().get(i).getNote();
                int fret = first.getNotes().get(i).getFret();
                int stringIndex = first.getNotes().get(i).getStringIndex();
                crossoverTablature.addNote(note, stringIndex, fret);
            }
            else {
                Note note = second.getNotes().get(i).getNote();
                int fret = second.getNotes().get(i).getFret();
                int stringIndex = second.getNotes().get(i).getStringIndex();
                crossoverTablature.addNote(note, stringIndex, fret);
            }
        }
        return crossoverTablature;
    }

    /**
     * Emulates mutation on a given {@link Tablature}.
     *
     * @param tablature the {@link Tablature} being mutated
     */
    public void mutate(Tablature tablature) {
        mutateNotes(tablature);
        // TODO: MUTATE TUNING
    }

    /**
     * Mutates notes on a given {@link Tablature}.
     *
     * <p>Iterates through the input {@code tablature}, and if mutation is satisfied, it randomly selects a new, valid string
     * and fret position for the evaluated {@link Tablature.TablatureNote}.</p>
     *
     * @param tablature the {@link Tablature} being mutated
     **/
    private void mutateNotes(Tablature tablature) {
        ArrayList<Tablature.TablatureNote> tablatureNotes = tablature.getNotes();
        for(int i = 0; i < tablatureNotes.size(); i++) {
            if(random.nextDouble() < this.mutationRate) {
                Map<Integer, Integer> fretPositions = tablatureNotes.get(i).getNote().getFretPositions(tablature.getTuning());
                List<Map.Entry<Integer, Integer>> validPositions = fretPositions.entrySet()
                        .stream()
                        .toList();
                if(validPositions.isEmpty()) {
                    continue;
                }
                for(int attempts = 0; attempts < 5; attempts++) { // Mutation is random, but constrained so it produces decent results
                    Map.Entry<Integer, Integer> newPosition = validPositions.get(random.nextInt(validPositions.size()));
                    int newString = newPosition.getKey();
                    int newFret = newPosition.getValue();
                    if(newString == tablatureNotes.get(i).getStringIndex() && newFret == tablatureNotes.get(i).getFret()) { // Same
                        continue;
                    }
                    // Temporary
                    tablatureNotes.get(i).setStringIndex(newString);
                    tablatureNotes.get(i).setFret(newFret);
                    if(getAverageFretDistance(i, tablatureNotes) <= 3) { // Ideal
                        break;
                    }
                }
            }
        }
    }
}
