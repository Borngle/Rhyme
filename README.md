# Rhyme
**Rhyme** is a MIDI-to-guitar-tablature transcriber.

## Table of Contents
* [Description](#description)
* [Requirements](#requirements)
* [Usage](#usage)

## Description
**Rhyme** uses a search-based optimisation technique called a genetic algorithm to find the most playable tablature representation of a MIDI song. It reads in a MIDI file using the [Java Sound API](https://docs.oracle.com/javase/8/docs/api/javax/sound/midi/package-summary.html) and generates a population of random, valid tablatures, which increasingly improve in playability over a number of generations. It does this using the core principles of a genetic algorithm: selection (fitness), crossover (reproduction), and mutation. It scores tablatures based on fretboard position, hand movement (fret and string jumps), and local average distances and hand spans in passages of notes. Crossover combines parent tablatures to create a new child tablature by merging segments from their tablature representations. Mutation introduces random changes to notes during a song to encourage diversity and prevent an early plateau in the rate of improvement. The final result is typeset into standard tablature notation and is output to the console and optionally written to a `.txt` file.

### Example
```
Song: Jansch Bert — Tinker's Blues [MIDIfind.com].mid
Timing: 4/4
E |----------------------------------------0-------2---------|
B |--------------------------------0-------------------------|
G |------------------------2---------------------------------|
D |----------------0-------------------------------0---------|
A |----------------------------------------------------------|
D |0-------------------------------0-------------------------|

E |2-----------------------2---------------------------------|
B |--------0-------------------------------0-----------------|
G |----------------0-----------------------------------------|
D |------------------------------------------------0---------|
A |----------------------------------------------------------|
D |0-------------------------------0-------------------------|

E |----------------------------------------0-------2---------|
B |3-------------------------------0-------------------------|
G |--------2---------------2---------------------------------|
D |----------------0-------------------------------0---------|
A |----------------------------------------------------------|
D |0-------------------------------0-------------------------|

E |2-----------------------2---------------------------------|
B |--------0-------------------------------0-----------------|
G |----------------------------------------------------------|
D |----------------0-------------------------------0---------|
A |----------------------------------------------------------|
D |0-------------------------------0-------------------------|
```

## Requirements
- Java 21+
- Maven 3.9+

## Usage
This program only takes a single argument: the path to your MIDI file.

**IntelliJ:**  
Set the program argument in your run configuration to the path of your MIDI file:
```
path/to/your/song.mid
```

**CLI:**  
Requires [Maven](https://maven.apache.org/download.cgi) to be installed and added to your system PATH. Navigate to the project root (where `pom.xml` is located) and build:
```
mvn package
```
Then run the generated JAR:
```
java -jar target/rhyme-1.0.jar path/to/your/song.mid
```
