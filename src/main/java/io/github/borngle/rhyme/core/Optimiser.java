/**
 * File: Optimiser.java
 *
 * <p>Brief: Uses a genetic algorithm to optimise the playability and accuracy of a {@link Tablature}.</p>
 *
 * @author Aidan
 * @since 05-11-2025
 **/

package io.github.borngle.rhyme.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
Gene -> fret choice
Chromosome -> frets within a chord or bar
Genome -> all fret placements (full tablatures)
Population -> set of genomes (multiple tablatures)
Start with initial population
For each note, randomly choose a fret that matches pitch
For each tablature, use fitness function to calculate hand movement cost
Select best (top 40% ish)
Create crossover between elites (top 10 from the best) and random selection of tablatures to maintain population size
Pick parent genomes, and choose crossover point (fret or bar), and combine parts
Mutation would be randomly picking some notes -> assign their string or fret to a different, pseudorandom, but still valid option
Repeat a certain amount of times
*/

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
        this.populationSize = populationSize;
        this.population = new ArrayList<>();
        for(int i = 0; i < populationSize; i++) {
            this.population.add(this.generateGenome(notes));
        }
        this.mutationRate = mutationRate;
    }

    private Tablature generateGenome(ArrayList<Note> notes) {
        int[] eStandard = new int[]{64, 59, 55, 50, 45, 40}; // Default tuning for now (testing)
        Tablature tablature = new Tablature(eStandard);
        Random rand = new Random();
        for(int i = 0; i < notes.size(); i++) {
            Map<Integer, Integer> fretPositions =  notes.get(i).getFretPositions(eStandard);
            List<Map.Entry<Integer, Integer>> validEntries = fretPositions.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null)
                    .toList();
            Map.Entry<Integer, Integer> randomEntry = validEntries.get(rand.nextInt(validEntries.size()));
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
        // TODO: RECOGNISE CHORD TYPES (OPEN AND BARRE)
        int score = 0;
        ArrayList<Tablature.TablatureNote> tablatureNotes = tablature.getNotes();
        for(int i = 0; i < tablatureNotes.size(); i++) {
            Tablature.TablatureNote tablatureNote = tablatureNotes.get(i);
            if(tablatureNote.getFret() == 0) { // Rewarding open frets
                score += 10;
            }
            if(i != 0) {
                // Higher string jumps and fret movements between consecutive notes has a higher penalty
                score -= (Math.abs(tablatureNote.getFret() - tablatureNotes.get(i - 1).getFret()));
                score -= (Math.abs(tablatureNote.getStringIndex() - tablatureNotes.get(i - 1).getStringIndex()));
            }
            int averageFretDistance = getAverageFretDistance(i, tablatureNotes);
            score -= averageFretDistance;
        }
        return score;
    }

    private static int getAverageFretDistance(int i, ArrayList<Tablature.TablatureNote> tablatureNotes) {
        int surroundingNeighbours = 4;
        int averageFretDistance; // From number of surrounding neighbours
        int start = Math.max(0, i - surroundingNeighbours / 2); // Always above start of array
        int end = Math.min(tablatureNotes.size() - 1, i + surroundingNeighbours / 2); // Always before end of array
        // Expand window if not enough neighbours (still within bounds)
        while ((end - start) < surroundingNeighbours && end < tablatureNotes.size() - 1) {
            end++;
        }
        while ((end - start) < surroundingNeighbours && start > 0) {
            start--;
        }
        double totalDifference = 0;
        int count = 0;
        for (int k = start; k <= end; k++) {
            if (k == i) {
                continue; // Skip current element
            }
            totalDifference += Math.abs(tablatureNotes.get(i).getFret() - tablatureNotes.get(k).getFret());
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
        // TODO: RESEARCH OTHER SELECTION METHODS (E.G., TOURNAMENT OR ROULETTE) AND TEST TWO-POINT CROSSOVER
        Random random = new Random();
        int crossoverPoint = random.nextInt(first.getNotes().size());
        int bar = first.getNotes().get(crossoverPoint).getNote().getBar();
        Tablature crossoverTablature = new Tablature(new int[]{64, 59, 55, 50, 45, 40});
        // Adds all notes from first up to the end of the random bar crossover point, then from second
        for(int i = 0; i < first.getNotes().size(); i++) {
            if(first.getNotes().get(i).getNote().getBar() < bar) {
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
     * <p>Iterates through the input {@code tablature}, and if mutation is satisfied, it randomly selects a new, valid string
     * and fret position for the evaluated {@link Tablature.TablatureNote}.</p>
     *
     * @param tablature the {@link Tablature} being mutated
     **/
    public void mutate(Tablature tablature) {
        ArrayList<Tablature.TablatureNote> tablatureNotes = tablature.getNotes();
        Random random = new Random();
        for(int i = 0; i < tablatureNotes.size(); i++) {
            if(Math.random() < this.mutationRate) {
                Map<Integer, Integer> fretPositions = tablatureNotes.get(i).getNote().getFretPositions(new int[]{64, 59, 55, 50, 45, 40});
                List<Map.Entry<Integer, Integer>> validPositions = fretPositions.entrySet().stream()
                        .filter(entry -> entry.getValue() != null).toList();
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
