package io.github.borngle.rhyme.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;

public class StatisticalTest {
    // Default
    static final double mutationRate = 0.05;
    static final int populationSize = 200;
    static final int generations = 80;
    static final int selectionPressure = (int) (populationSize * 0.25);
    static final double elitePool = 0.2;
    static final int seed = 42;
    // Mutation rate testing
    static final double mutationRateA = 0.05;
    static final double mutationRateB = 0.1;
    // Population size testing
    static final int populationSizeA = 300;
    static final int populationSizeB = 700;

    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }
        ArrayList<ArrayList<Note>> songs = loadSongs(args[0]);
        double[] scoresA; // Fitness scores from optimiser configuration A
        double[] scoresB; // Fitness scores from optimiser configuration B
        // Mutation test
        scoresA = testConfiguration(songs, mutationRateA, populationSize, generations, selectionPressure, elitePool);
        scoresB = testConfiguration(songs, mutationRateB, populationSize, generations, selectionPressure, elitePool);
        analyse(scoresA, scoresB);
        // Population test
        scoresA = testConfiguration(songs, mutationRate, populationSizeA, generations, selectionPressure, elitePool);
        scoresB = testConfiguration(songs, mutationRate, populationSizeB, generations, selectionPressure, elitePool);
        analyse(scoresA, scoresB);
    }

    /**
     * Loads all MIDI files from the specified directory and returns them as sequences of notes.
     *
     * @param path the path to the directory with MIDI files
     * @return a 2D {@code ArrayList} of {@link Note} objects in sequence, representing the songs
     */
    public static ArrayList<ArrayList<Note>> loadSongs(String path) {
        ArrayList<ArrayList<Note>> songs = new ArrayList<>();
        File directory = new File(path);
        File[] songFiles = directory.listFiles();
        if(songFiles == null) {
            return songs;
        }
        for(int i = 0; i < songFiles.length; i++) {
            Main.resolution = Reader.getResolution(songFiles[i]);
            Main.timeSignature = Reader.getTimeSignature(songFiles[i]);
            ArrayList<ArrayList<Note>> tracks = Reader.readSong(songFiles[i]);
            if (!tracks.isEmpty() && !tracks.getFirst().isEmpty()) {
                songs.add(tracks.getFirst());
            }
        }
        return songs;
    }

    /**
     * Runs the GA with the provided configuration of parameters on every song for testing.
     *
     * @param songs the sequences of notes that make up the songs
     * @param mutationRate the rate at which mutation occurs
     * @param populationSize how many tablatures there are
     * @param generations the number of generations fitness, crossover, and mutation runs for
     * @param selectionPressure the amount of genomes kept after a generation
     * @param elitePool the percentage of the highest scoring genomes from the selected population to be chosen for crossover
     * @return the final scores from every song tested given the configuration
     */
    public static double[] testConfiguration(ArrayList<ArrayList<Note>> songs, double mutationRate, int populationSize,
                                             int generations, int selectionPressure, double elitePool) {
        double[] scores = new double[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            Main.random.setSeed(seed + i);
            Optimiser optimiser = new Optimiser(populationSize, songs.get(i), mutationRate);
            Tablature result = Main.optimise(optimiser, generations, selectionPressure, elitePool);
            scores[i] = Optimiser.fitness(result);
            System.out.println("Song " +  i + " done");
        }
        return scores;
    }

    /**
     * Performs t-tests on the configuration results, and reports mean scores, statistical power, and whether
     * a significant difference has been found between configurations.
     *
     * @param scoresA scores from configuration A
     * @param scoresB scores from configuration B
     */
    public static void analyse(double[] scoresA, double[] scoresB) {
        double[] differences = differences(scoresA, scoresB); // Differences in scores
        double cohensD = cohensD(differences);
        int requiredN = requiredN(cohensD);
        double statisticalPower = statisticalPower(cohensD, scoresA.length);
        double p = pairedTTest(differences);
        double mean = mean(differences);
        System.out.println("Mean score A: " + mean(scoresA));
        System.out.println("Mean score B: " + mean(scoresB));
        System.out.println("Mean difference: " + mean);
        System.out.println("Cohen's d: " + cohensD);
        System.out.println("Songs needed (80%): " + requiredN);
        System.out.println("Songs used: " + scoresA.length);
        System.out.println("Achieved power: " + statisticalPower * 100 + "%");
        System.out.println("p-value: " + p);
        System.out.println("Significant: " + (p < 0.05 ? "YES" : "NO"));
        if (statisticalPower < 0.80) {
            System.out.printf("Need " + Math.max(0, requiredN - scoresA.length) + " more songs for reliable results");
        }
        if (p < 0.05) {
            System.out.println("Significant difference found; configuration " + (mean > 0 ? "B" : "A") + " performs better");
        }
        else {
            System.out.println("No significant difference found between the configurations");
        }
    }

    /**
     * Subtracts scores {@code scoresA} from {@code scoresB}.
     *
     * @param scoresA scores from configuration A
     * @param scoresB scores from configuration B
     * @return the difference of scores for each song between run configurations
     */
    public static double[] differences(double[] scoresA, double[] scoresB) {
        double[] differences = new double[scoresA.length];
        for(int i = 0; i < scoresA.length; i++) {
            differences[i] = scoresB[i] - scoresA[i];
        }
        return differences;
    }

    /**
     * Effect size measure to determine how big the difference is.
     *
     * @param differences the difference of scores for each song between run configurations
     * @return how big the difference is
     */
    public static double cohensD(double[] differences) {
        return mean(differences) / standardDeviation(differences);
    }

    public static double mean(double[] values) {
        return Arrays.stream(values).sum() / values.length;
    }

    public static double standardDeviation(double[] values) {
        double mean = mean(values);
        double sumOfSquares = 0;
        for(int i = 0; i < values.length; i++) {
            sumOfSquares += Math.pow(values[i] - mean, 2);
        }
        return Math.sqrt(sumOfSquares / (values.length - 1));
    }

    /**
     * Calculates the amount of songs needed to give 80% power at alpha = 0.05.
     *
     * @param cohensD how big the difference is
     * @return the amount of songs needed
     */
    public static int requiredN(double cohensD) {
        NormalDistribution normal = new NormalDistribution();
        // z-score with 97.5% of normal distribution to left (two-tailed so 5% is split)
        double zAlpha = normal.inverseCumulativeProbability(0.975); // 1.96
        // z-score with 80% of normal distribution to left (statistical power threshold)
        double zBeta = normal.inverseCumulativeProbability(0.8); // 0.842
        return (int) Math.ceil(Math.pow((zAlpha + zBeta) / Math.abs(cohensD), 2)); // How many songs needed
    }

    /**
     * Calculates the actual statistical power achieved from the amount of songs provided.
     *
     * @param cohensD how big the difference is
     * @param n the amount of songs provided
     * @return achieved statistical power
     */
    public static double statisticalPower(double cohensD, int n) {
        // The power given the actual n
        NormalDistribution normal = new NormalDistribution();
        // z-score with 97.5% of normal distribution to left (two-tailed so 5% is split)
        double zAlpha = normal.inverseCumulativeProbability(0.975); // 1.96
        double lambda = Math.abs(cohensD) * Math.sqrt(n); // How far true distribution of differences shifted from 0
        // The proportion of true difference distribution that exceeds the significance threshold (statistical power)
        return normal.cumulativeProbability(lambda - zAlpha);
    }

    /**
     * Computes t-statistic from the {@code differences}, and returns two-tailed p-value from t-distribution.
     *
     * @param differences the difference of scores for each song between run configurations
     * @return the p-value
     */
    public static double pairedTTest(double[] differences) {
        double mean = mean(differences);
        double standardDeviation = standardDeviation(differences);
        // How many standard errors (standardDeviation / Math.sqrt(differences.length)) the observed mean is away from 0
        double tStatistic = mean / (standardDeviation / Math.sqrt(differences.length)); // Close to 0 (null hypothesis), further away is less likely chance
        TDistribution tDistribution = new TDistribution(differences.length - 1);
        return 2.0 * tDistribution.cumulativeProbability(-Math.abs(tStatistic)); // Two-tailed p-value ('2 *' adds right tail)
    }
}
