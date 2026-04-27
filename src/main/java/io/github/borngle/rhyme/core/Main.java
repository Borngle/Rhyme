/**
 * File: Main.java
 *
 * <p>Brief: Entry point into the program.</p>
 *
 * <p>Details: Sequentially executes the steps required to transcribe a MIDI file to tablature format
 * and outputs the resulting tablature.</p>
 *
 * @author Aidan
 * @since 24-10-2025
 **/

package io.github.borngle.rhyme.core;

import javax.sound.midi.*;
import java.io.File;
import java.util.*;

public class Main {
    // For the song being transcribed
    static int resolution;
    static int[] timeSignature;

    final static Random random = new Random();

    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }
        File song = new File(args[0]);
        String songName = song.getName();
        resolution = Reader.getResolution(song);
        timeSignature = Reader.getTimeSignature(song);
        ArrayList<ArrayList<Note>> songTracks = Reader.readSong(song);
        StringBuilder songTablature = new StringBuilder();
        for(int i = 0; i < songTracks.size(); i++) {
            ArrayList<Note> track = songTracks.get(i);
            if(songTracks.size() > 1) {
                songTablature.append("\nTrack: ").append(i + 1).append("\n"); // Formatting for multi-track songs
            }
            int generations = 500;
            int populationSize = 1500;
            Tablature tablature = optimise(new Optimiser(populationSize, track, 0.05), generations, 0.1, 0.2);
            int capo = tablature.getCapoFret();
            if(tablature.getCapoFret() > 0) {
                tablature.transpose();
                songTablature.append("Capo: ").append(capo).append("\n");
            }
            songTablature.append(TypeSetter.render(tablature));
        }
        output(songName, String.valueOf(songTablature));
    }

    /**
     * Runs the genetic algorithm over a given number of {@code generations}.
     * @param optimiser the genetic algorithm
     * @param generations the number of generations fitness, crossover, and mutation runs for
     * @param selectionPressure the percentage of genomes kept after a generation
     * @param elitePool the percentage of the highest scoring genomes from the selected population to be chosen for crossover
     * @return the best scoring {@link Tablature}
     */
    public static Tablature optimise(Optimiser optimiser, int generations, double selectionPressure, double elitePool) {
        int selectionSize = (int) (selectionPressure * optimiser.getPopulationSize());
        int elitePoolSize = (int) (selectionSize * elitePool);
        for(int i = 0; i < generations; i++) {
            Collections.sort(optimiser.getPopulation());
            while(optimiser.getPopulation().size() > selectionSize) { // Remove bottom x%
                optimiser.getPopulation().removeLast();
            }
            while(optimiser.getPopulation().size() < optimiser.getPopulationSize()) { // Building population back up
                int elite = random.nextInt(elitePoolSize); // Random elite genome
                int other = random.nextInt(selectionSize - elitePoolSize) + elitePoolSize; // Tablature within remaining population but not elite
                Tablature child = optimiser.crossover(optimiser.getPopulation().get(elite), optimiser.getPopulation().get(other));
                optimiser.mutate(child);
                optimiser.getPopulation().add(child);
            }
        }
        Collections.sort(optimiser.getPopulation());
        Tablature best = optimiser.getPopulation().getFirst();
        best.findCapoFret();
        return best;
    }

    /**
     * Prints and writes tablature to a text file.
     * @param songName the name of the supplied MIDI file
     * @param songTablature the rendered tablature
     */
    public static void output(String songName, String songTablature) {
        System.out.println("Song: " + songName);
        System.out.println("Timing: " + timeSignature[0] + "/" + timeSignature[1]);
        System.out.println(songTablature);
        //TypeSetter.writeFile(songName, songTablature.toString());
    }
}
