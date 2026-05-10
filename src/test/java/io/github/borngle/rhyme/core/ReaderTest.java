package io.github.borngle.rhyme.core;

public class ReaderTest {
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {

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
}
