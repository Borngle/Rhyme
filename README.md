# Rhyme
**Rhyme** is a MIDI-to-guitar-tablature transcriber developed in Java.

## Table of Contents
* [Description](#description)
* [Requirements](#requirements)
* [Usage](#usage)

## Description
### Overview
**Rhyme** uses a search-based optimisation technique called a genetic algorithm (GA) to find the most playable tablature representation of a MIDI song. It reads in a MIDI file using the [Java Sound API](https://docs.oracle.com/javase/8/docs/api/javax/sound/midi/package-summary.html) and generates a population of random, valid tablatures, which increasingly improve in playability over a number of generations. It does this iteratively by using the core principles of a GA: selection (fitness), crossover (reproduction), and mutation. 

### Optimisation 
The GA scores tablatures based on fretboard position, hand movement (fret and string jumps), and local average distances and hand spans in passages of notes throughout the song. Parent tablatures are combined to create a new child tablature by merging segments from their tablature representations. Mutation is introduced through random, controlled changes to notes during a song to encourage diversity and prevent an early plateau in the rate of improvement. The final result, after a number of iterations, is typeset into standard tablature notation and is output to the console and optionally written to a `.txt` file.

### Example Output
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
This program takes the path to your MIDI file as well as various optional arguments for configuring the GA and program behaviour.

```
Usage: rhyme [-hVw] [--allow-tuning-mutation] [--list-tunings] [-g=<count>]
             [-m=<rate>] [-p=<size>] [-s=<rate>] [-t=<name>] [<midi-file>]
Transcribe a MIDI file into guitar tablature.
      [<midi-file>]         MIDI file.
      --allow-tuning-mutation
                            Allow the optimiser to mutate the tuning.
  -g, --generations=<count> Number of generations (default : 500).
  -h, --help                Show this help message and exit.
      --list-tunings        List available tunings.
  -m, --mutation=<rate>     Mutation rate (default : 0.05).
  -p, --population=<size>   Population size (default : 1000).
  -s, --selection=<rate>    Selection pressure (default : 0.1).
  -t, --tuning=<name>       Target tuning.
  -V, --version             Print version information and exit.
  -w, --write               Write output to a file.
```

### IDE
Set the program arguments in your run configuration to the path of your MIDI file as well as any desired options:
```
path/to/your/song.mid
```

### CLI
Requires [Maven](https://maven.apache.org/download.cgi) to be installed and added to your system `PATH`. Navigate to the project root (where `pom.xml` is located) and build:
```
mvn package
```
Then run the generated JAR:
```
java -jar target/rhyme.jar path/to/your/song.mid
```
