package io.github.borngle.rhyme.core;

import java.util.Map;

public class NoteTest {
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        Main.resolution = 480;
        Main.timeSignature = new int[]{4, 4};
        // getActualNote
        test_getActualNote_E2_returnsE();
        test_getActualNote_nextOctave_returnsE();
        // getActualOctave
        test_getActualOctave_E2_returnsOctave2();
        test_getActualOctave_E3_returnsOctave3();
        // getBar
        test_getBar_tickZero_returnsOne();
        test_getBar_middle_returnsSame();
        test_getBar_secondBar_returnsTwo();
        // getFretPositions
        test_getFretPositions_noteAtStringPitch_returnsZero();
        test_getFretPositions_noteFiveSemitonesAbove_returnsFive();
        test_getFretPositions_noteBelowLowest_returnsEmpty();
        test_getFretPositions_noteAboveHighest_returnsEmpty();
        test_getFretPositions_noteOnMultipleStrings_returnsEntries();
        // Summary
        System.out.println("Passed: " + passed + " / " + (passed + failed));
        System.out.println("Failed: " + failed + " / " + (passed + failed));
    }

    public static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("Passed: " + name);
            passed += 1;
        }
        else {
            System.out.println("Failed: " + name);
            failed += 1;
        }
    }

    // getActualNote

    public static void test_getActualNote_E2_returnsE() {
        Note note = new Note(40, 0, 480, 80); // E2
        check("getActualNote: pitch 40 (E2) should return E", Note.getActualNote(note.getPitch()).equals("E"));
    }

    public static void test_getActualNote_nextOctave_returnsE() {
        Note note = new Note(52, 0, 480, 80); // E3
        check("getActualNote: pitch 52 (E3) should return E", Note.getActualNote(note.getPitch()).equals("E"));
    }

    // getActualOctave

    public static void test_getActualOctave_E2_returnsOctave2() {
        Note note = new Note(40, 0, 480, 80); // (E2) 40 / 12 - 1 = 2
        check("getActualOctave: note E2 should return octave 2", note.getActualOctave() == 2);
    }

    public static void test_getActualOctave_E3_returnsOctave3() {
        Note note = new Note(52, 0, 480, 80); // (E3) 52 / 12 - 1 = 3
        check("getActualOctave: note E3 should return octave 3", note.getActualOctave() == 3);
    }

    // getBar

    public static void test_getBar_tickZero_returnsOne() {
        Note note = new Note(40, 0, 480, 80);
        check("getBar: tick 0 should be bar 1", note.getBar() == 1);
    }

    static void test_getBar_middle_returnsSame() {
        Note note = new Note(40, 960, 480, 80); // Halfway through first bar
        check("getBar: tick 960 should still be bar 1", note.getBar() == 1);
    }

    public static void test_getBar_secondBar_returnsTwo() {
        // 4/4 at resolution 480 = 1920 ticks per bar
        Note note = new Note(40, 1920, 480, 80);
        check("getBar: tick 1920 should be bar 2", note.getBar() == 2);
    }

    // getFretPositions

    public static void test_getFretPositions_noteAtStringPitch_returnsZero() {
        int[] tuning = Tablature.eStandard;
        Note note = new Note(40, 0, 480, 80);
        Map<Integer, Integer> fretPositions = note.getFretPositions(tuning);
        check("getFretPositions: pitch 40 for eStandard (low E) should be fret 0", fretPositions.get(6) == 0);
    }

    public static void test_getFretPositions_noteFiveSemitonesAbove_returnsFive() {
        int[] tuning = Tablature.eStandard;
        Note note = new Note(45, 0, 480, 80);
        Map<Integer, Integer> fretPositions = note.getFretPositions(tuning);
        check("getFretPositions: pitch 45 for eStandard should be fret 5", fretPositions.get(6) == 5);
    }

    public static void test_getFretPositions_noteBelowLowest_returnsEmpty() {
        int[] tuning = Tablature.eStandard;
        Note note = new Note(1, 0, 480, 80);
        Map<Integer, Integer> fretPositions = note.getFretPositions(tuning);
        check("getFretPositions: pitch 1 for eStandard does not exist; should return empty", fretPositions.isEmpty());
    }

    public static void test_getFretPositions_noteAboveHighest_returnsEmpty() {
        int[] tuning = Tablature.eStandard;
        Note note = new Note(100, 0, 480, 80);
        Map<Integer, Integer> fretPositions = note.getFretPositions(tuning);
        check("getFretPositions: pitch 100 for eStandard does not exist; should return empty", fretPositions.isEmpty());
    }

    public static void test_getFretPositions_noteOnMultipleStrings_returnsEntries() {
        Note note = new Note(64, 0, 480, 80); // Pitch 64 (E4) appears multiple times across the fretboard
        Map<Integer, Integer> fretPositions = note.getFretPositions(Tablature.eStandard);
        check("getFretPositions: pitch 64 should be playable on more than one string", fretPositions.size() > 1);
    }
}
