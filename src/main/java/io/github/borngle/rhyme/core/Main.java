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

    final static int[][] tunings = new int[][]{
            eStandard, dStandard, bStandard, openG,
            openD, openC, openA, openE,
            openF, halfStepDown, halfStepUp, dropD,
            dropCSharp, dropC, dropB, DADGAD
    };

    // For the song being transcribed
    static int resolution;
    static int[] timeSignature;

    final static Random random = new Random();

    public static void main(String[] args) {
        String songName = "Cohen Leonard — Suzanne [MIDIfind.com].mid";
        File song = new File("midi/" + songName);
        resolution = Reader.getResolution(song);
        timeSignature = Reader.getTimeSignature(song);
        ArrayList<ArrayList<Note>> songTracks = Reader.readSong(song);
        StringBuilder songTablature = new StringBuilder();
        for(int i = 0; i < songTracks.size(); i++) {
            ArrayList<Note> track = songTracks.get(i);
            if(songTracks.size() > 1) {
                songTablature.append("\nTrack: ").append(i + 1).append("\n"); // Formatting for multi-track songs
            }
            Tablature tablature = optimise(new Optimiser(400, track, 0.025));
            songTablature.append(TypeSetter.render(tablature));
        }
        output(songName, String.valueOf(songTablature));
    }

    public static Tablature optimise(Optimiser optimiser) {
        int generations = 200;
        int quarter = (int) (0.25 * optimiser.getPopulationSize());
        for(int i = 0; i < generations; i++) {
            Collections.sort(optimiser.getPopulation());
            System.out.println(Optimiser.fitness(optimiser.getPopulation().getFirst()));
            while(optimiser.getPopulation().size() > quarter) { // Remove bottom 75%
                optimiser.getPopulation().removeLast();
            }
            while(optimiser.getPopulation().size() < optimiser.getPopulationSize()) { // Building population back up
                int elite = random.nextInt(3); // Random elite genome
                int other = random.nextInt(quarter - 3) + 3; // Tablature within remaining population but not elite
                Tablature child = optimiser.crossover(optimiser.getPopulation().get(elite), optimiser.getPopulation().get(other));
                optimiser.mutate(child);
                optimiser.getPopulation().add(child);
            }
        }
        Collections.sort(optimiser.getPopulation());
        return optimiser.getPopulation().getFirst();
    }

    public static void output(String songName, String songTablature) {
        System.out.println("Song: " + songName);
        System.out.println("Timing: " + timeSignature[0] + "/" + timeSignature[1]);
        System.out.println(songTablature);
        //TypeSetter.writeFile(songName, songTablature.toString());
    }
}
