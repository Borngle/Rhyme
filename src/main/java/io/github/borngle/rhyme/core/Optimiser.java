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
Genome -> all fret placements (full tablatures)
Population -> set of genomes (multiple tablatures)
Start with initial population
For each note, randomly choose a fret that matches pitch
For each tablature, use fitness function to calculate hand movement cost
Select best
Create crossover
Pick parent genomes, and choose crossover point (fret or bar), and combine parts
Mutation would be randomly picking some notes -> assign their string or fret to a different, random, but still valid option
Repeat a certain amount of times
*/

public class Optimiser {
    private ArrayList<Tablature> population;
    private final double crossoverRate;
    private final double mutationRate;

    /**
     * Generates a random initial population of tablatures.
     *
     * @param populationSize how many tablatures there are
     * @param notes the notes read from the MIDI file
     * @param crossoverRate the rate at which crossover occurs
     * @param mutationRate the rate at which mutation occurs
     **/
    public Optimiser(int populationSize, ArrayList<Note> notes, double crossoverRate, double mutationRate) {
        this.population = new ArrayList<>();
        for(int i = 0; i < populationSize; i++) {
            this.population.add(this.generateGenome(notes));
        }
        this.crossoverRate = crossoverRate;
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

    public ArrayList<Tablature> getPopulation() {
        return population;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public int fitness(Tablature tablature) {
        int score = 0;
        for(int i = 0; i < tablature.getNotes().size(); i++) {
            ArrayList<Tablature.TablatureNote> bar = tablature.getBar(i + 1);
            for(int j = 0; j < bar.size(); j++) {
                Tablature.TablatureNote tablatureNote = bar.get(j);
                if(tablatureNote.getFret() == 0) { // Rewarding open frets
                    score += 10;
                }
                if(j != 0) {
                    // Higher string jumps and fret movements between consecutive notes has a higher penalty
                    score -= (Math.abs(tablatureNote.getFret() - bar.get(j - 1).getFret()));
                    score -= (Math.abs(tablatureNote.getStringIndex() - bar.get(j - 1).getStringIndex()));
                }
                int surroundingNeighbours = 4;
                int averageFretDistance; // From number of surrounding neighbours
                int start = Math.max(0, j - surroundingNeighbours / 2); // Always above start of array
                int end = Math.min(bar.size() - 1, j + surroundingNeighbours / 2); // Always before end of array
                // Expand window if not enough neighbours (still within bounds)
                while ((end - start) < surroundingNeighbours && end < bar.size() - 1) {
                    end++;
                }
                while ((end - start) < surroundingNeighbours && start > 0) {
                    start--;
                }
                double totalDifference = 0;
                int count = 0;
                for (int k = start; k <= end; k++) {
                    if (k == j) {
                        continue; // Skip current element
                    }
                    totalDifference += Math.abs(bar.get(j).getFret() - bar.get(k).getFret());
                    count++;
                }
                if(count > 0) {
                    averageFretDistance = (int) totalDifference / count;
                }
                else {
                    averageFretDistance = 0;
                }
                score -= averageFretDistance;
            }
        }
        return score;
    }

    public void crossover(Tablature first, Tablature second) {

    }

    public void mutate(Tablature tablature) {

    }
}
