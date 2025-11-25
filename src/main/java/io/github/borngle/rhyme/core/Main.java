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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

public class Main {
    // For the song being transcribed
    static int resolution;
    static int[] timeSignature;

    public static void main(String[] args) {
        // Testing
        String songName = "Cohen Leonard — Suzanne [MIDIfind.com].mid";
        File song = new File("midi/" + songName);
        resolution = Reader.getResolution(song);
        timeSignature = Reader.getTimeSignature(song);
        int[] eStandard = new int[]{64, 59, 55, 50, 45, 40}; // Strings 1 to 6 - high to low
        ArrayList<ArrayList<Note>> songTracks = Reader.readSong(song);
        StringBuilder songTablature = new StringBuilder();
        for(int i = 0; i < songTracks.size(); i++) {
            if(songTracks.size() > 1) {
                songTablature.append("\nTrack: ").append(i + 1).append("\n");
            }
            ArrayList<Note> notes = songTracks.get(i);
            Tablature tablature = new Tablature(eStandard);
            for(int j = 0; j < notes.size(); j++) {
                Map<Integer, Integer> fretPositions =  notes.get(j).getFretPositions(eStandard);
                int tablatureString = 0;
                for(Integer string : fretPositions.keySet()) {
                    tablatureString = string;
                }
                tablature.addNote(notes.get(j), tablatureString, fretPositions.get(tablatureString));
            }
            songTablature.append(TypeSetter.render(tablature));
        }
        Optimiser optimiser = new Optimiser(400, songTracks.getFirst(), 0.05);
        Random random = new Random();
        int generations = 80;
        int thirtyPercent = (int) (0.3 * optimiser.getPopulationSize());
        for(int i = 0; i < generations; i++) {
            Collections.sort(optimiser.getPopulation());
            System.out.println("Generation " + (i + 1) + " score: " + Optimiser.fitness(optimiser.getPopulation().getFirst()));
            while(optimiser.getPopulation().size() > thirtyPercent) { // Remove bottom 70%
                optimiser.getPopulation().removeLast();
            }
            while(optimiser.getPopulation().size() < optimiser.getPopulationSize()) { // Building population back up
                int elite = random.nextInt(3); // Random elite genome
                int other = random.nextInt(thirtyPercent - 3) + 3; // Tablature within remaining population but not elite
                Tablature child = optimiser.crossover(optimiser.getPopulation().get(elite), optimiser.getPopulation().get(other));
                optimiser.mutate(child);
                optimiser.getPopulation().add(child);
            }
        }
        Collections.sort(optimiser.getPopulation());
        System.out.println();
        System.out.println("Song: " + songName);
        System.out.println("Timing: " + timeSignature[0] + "/" + timeSignature[1]);
        System.out.println(TypeSetter.render(optimiser.getPopulation().getFirst()));
        //TypeSetter.writeFile(songName, songTablature.toString());
    }
}
