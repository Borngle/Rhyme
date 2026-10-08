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

import picocli.CommandLine;

import javax.sound.midi.*;
import java.io.File;
import java.util.*;

@CommandLine.Command(
        name = "rhyme",
        description = "Transcribe a MIDI file into guitar tablature.",
        mixinStandardHelpOptions = true
)

public class Main implements Runnable {
    // For the song being transcribed
    static int resolution;
    static int[] timeSignature;

    final static Random random = new Random();

    @CommandLine.Parameters(
            index = "0",
            description = "MIDI file.",
            arity = "0..1",
            paramLabel = "<midi-file>"
    )
    private File song;

    @CommandLine.Option(
            names = {"-g", "--generations"},
            description = "Number of generations (default : 500).",
            defaultValue = "500",
            paramLabel = "<count>"
    )
    private int generations;

    @CommandLine.Option(
            names = {"-p", "--population"},
            description = "Population size (default : 1000).",
            defaultValue = "1000",
            paramLabel = "<size>"
    )
    private int populationSize;

    @CommandLine.Option(
            names = {"-m", "--mutation"},
            description = "Mutation rate (default : 0.05).",
            defaultValue = "0.05",
            paramLabel = "<rate>"
    )
    private double mutationRate;

    @CommandLine.Option(
            names = {"-s", "--selection"},
            description = "Selection pressure (default : 0.1).",
            defaultValue = "0.1",
            paramLabel = "<rate>"
    )

    private double selectionPressure;

    @CommandLine.Option(
            names = {"-t", "--tuning"},
            description = "Target tuning.",
            paramLabel = "<name>"
    )
    private String tuningName;

    @CommandLine.Option(
            names = "--list-tunings",
            description = "List available tunings."
    )
    private boolean listTunings;

    @CommandLine.Option(
            names = {"--allow-tuning-mutation"},
            description = "Allow the optimiser to mutate the tuning."
    )
    private boolean allowTuningMutation;

    @CommandLine.Option(
            names = {"-w", "--write"},
            description = "Write output to a file."
    )
    private boolean write;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if(listTunings) {
            System.out.print(listTunings());
            return;
        }
        validate();
        String songName = song.getName();
        resolution = Reader.getResolution(song);
        timeSignature = Reader.getTimeSignature(song);
        ArrayList<ArrayList<Note>> songTracks = Reader.readSong(song);
        StringBuilder songTablature = new StringBuilder();
        int[] targetTuning = null;
        if(tuningName != null) {
            targetTuning = getTuning(tuningName);
        }
        for(int i = 0; i < songTracks.size(); i++) {
            ArrayList<Note> track = songTracks.get(i);
            if(songTracks.size() > 1) {
                songTablature.append("\nTrack: ").append(i + 1).append("\n"); // Formatting for multi-track songs
            }
            Tablature tablature = optimise(new Optimiser(populationSize, track, mutationRate, targetTuning, allowTuningMutation), generations, selectionPressure, 0.2);
            int capo = tablature.getCapoFret();
            if(tablature.getCapoFret() > 0) {
                tablature.transpose();
                songTablature.append("Capo: ").append(capo).append("\n");
            }
            songTablature.append(TypeSetter.render(tablature));
        }
        output(songName, String.valueOf(songTablature));
    }

    private void validate() {
        if(song == null) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Error: Missing required parameter <midi-file>"
            );
        }
        if(generations <= 0) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Error: Number of generations must be greater than 0"
            );
        }
        if(populationSize <= 1) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Error: Population size must be greater than 1"
            );
        }
        if(mutationRate < 0 || mutationRate > 1) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Error: Mutation rate must be between 0 and 1"
            );
        }
        if(selectionPressure <= 0 || selectionPressure > 1) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Error: Selection pressure must be between 0 and 1"
            );
        }
    }

    /**
     * Retrieves a target tuning from a provided name.
     *
     * @param name the target tuning name passed in from the CLI
     * @return the target tuning
     */
    private int[] getTuning(String name) {
        return switch (name) {
            case "eStandard" -> Tablature.eStandard;
            case "dStandard" -> Tablature.dStandard;
            case "bStandard" -> Tablature.bStandard;
            case "openG" -> Tablature.openG;
            case "openD" -> Tablature.openD;
            case "openC" -> Tablature.openC;
            case "openA" -> Tablature.openA;
            case "openE" -> Tablature.openE;
            case "openF" -> Tablature.openF;
            case "halfStepDown" -> Tablature.halfStepDown;
            case "halfStepUp" -> Tablature.halfStepUp;
            case "dropD" -> Tablature.dropD;
            case "dropCSharp" -> Tablature.dropCSharp;
            case "dropC" -> Tablature.dropC;
            case "dropB" -> Tablature.dropB;
            case "DADGAD" -> Tablature.DADGAD;
            case "lute" -> Tablature.lute;
            case "FADGBE" -> Tablature.FADGBE;
            case "GGDGBD" -> Tablature.GGDGBD;
            default -> throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Error: Unknown tuning: '" + name + "'."
            );
        };
    }

    /**
     * Outputs the available tunings.
     */
    private static String listTunings() {
        return "Available tunings:\n" + String.join("\n", Tablature.availableTunings);
    }

    /**
     * Runs the genetic algorithm over a given number of {@code generations}.
     *
     * @param optimiser the genetic algorithm
     * @param generations the number of iterations fitness, crossover, and mutation runs for
     * @param selectionPressure the percentage of genomes kept after a generation
     * @param elitePool the percentage of the highest scoring genomes from the selected population to be chosen for crossover
     * @return the best scoring {@link Tablature}
     */
    public static Tablature optimise(Optimiser optimiser, int generations, double selectionPressure, double elitePool) {
        int selectionSize = (int) (selectionPressure * optimiser.getPopulationSize());
        int elitePoolSize = (int) (selectionSize * elitePool);
        for(int i = 0; i < generations; i++) {
            ArrayList<Tablature> population = optimiser.getPopulation();
            Collections.sort(population);
            while(population.size() > selectionSize) { // Remove bottom x%
                population.removeLast();
            }
            while(population.size() < optimiser.getPopulationSize()) { // Building population back up
                int elite = random.nextInt(elitePoolSize); // Random elite genome
                int other = random.nextInt(selectionSize - elitePoolSize) + elitePoolSize; // Tablature within remaining population but not elite
                Tablature child = optimiser.crossover(population.get(elite), population.get(other));
                optimiser.mutate(child);
                population.add(child);
            }
        }
        Collections.sort(optimiser.getPopulation());
        Tablature best = optimiser.getPopulation().getFirst();
        best.findCapoFret();
        return best;
    }

    /**
     * Prints and writes tablature to a text file.
     *
     * @param songName the name of the supplied MIDI file
     * @param songTablature the rendered tablature
     */
    public void output(String songName, String songTablature) {
        System.out.println("Song: " + songName);
        System.out.println("Timing: " + timeSignature[0] + "/" + timeSignature[1]);
        System.out.println(songTablature);
        if(write) {
            TypeSetter.writeFile(songName, songTablature);
        }
    }
}
