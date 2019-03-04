# mica-ink
[![Licence MIT](https://img.shields.io/badge/licence-MIT-blue.svg)](https://github.com/micabytes/mica-ink/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/micabytes/mica-ink.svg?branch=master)](https://travis-ci.org/micabytes/mica-ink)
[![Release](https://jitpack.io/v/micabytes/mica-ink.svg)](https://jitpack.io/#micabytes/mica-ink)

Note: mica-ink is no longer being actively developed. In current implementation, I'm now instead using [blade-ink](http://github.com/bladecoder/blade-ink) and using external functions + extra text processing to get around the limitations of Ink. The advantage of doing this is that it allows for a better utilization of Ink's native tools (such as Inky) + it helps to concentrate the "Java" community on one library, rather than split us up over multiple OSS projects. Thanks for following this project.

# README
[mica-ink](http://github.com/micabytes/mica-ink) is a Kotlin (originally Java) implementation of Inkle Studios (@inkle) scripting language [ink](http://github.com/inkle/ink)
for writing interactive narrative. It was developed in order to have an ink alternative for Java, and is used as the narrative engine in my various projects;
most notable [Pirates and Traders 2](https://play.google.com/store/apps/details?id=com.micabytes.pirates2). StoryBytes is a simple open-source sample 
application (for [Desktop](https://github.com/micabytes/storybytes-desktop)) that uses the mica-ink engine and runs compatible ink scripts. 

Currently, this reimplements Part 1 to Part 3 of the [Ink documentation](https://github.com/inkle/ink/blob/master/Documentation/WritingWithInk.md), with the
following omissions (that I know of - probably things added to Ink since I made this list as well):

- Conditions on choices (as well as the choice header) cannot be multi-line.
- evaluation of conditions has some limitations (not only supported as a function, evaluation of 'and' and 'or' is space sensitive).
- Multi-line comments are not implemented.
- CHOICE_COUNT and TURNS_SINCE are not implemented (essentially functions, and those haven't been implemented yet).
- Doesn't parse check for -> END yet (i.e., quite happy to have knots and threads ending in nothing).
- Have not tested "Advanced: Gathers directly after an option"
- No temporary values at this point (parameters exist, though).
- No pass by reference parameters.
- No constants.
- No extern functions.

And of course, the key elements of Part 4 are missing:
- Tunnels
- Threads

Most of the examples in the implemented section of the documentation are used as tests, with a few additional test cases grabbed from the 'ink/Tests' where
appropriate.

This implementation does one thing that the standard Ink implementation does not, which is to allow the use of native Java/Kotlin objects as values, and permit
the calling of methods on those objects.

## Roadmap

I used this implementation in my own Kotlin-based games, so I plan to continue working on the game engine as and when I require new features. I would like to stay compatible with the main ink functionality, but my focus is on making games rather than developing a game engine; as such, it is never going to be a priority for me to stay 100% in sync with the Ink engine.

- Extend the engine until it implements all (or nearly all) of the standard Ink features; particularly threads and tunnels.
- Add more comprehensive testing to code. Many corner cases are not covered adequately yet.
- Clean up and refactor the code. Especially the variable/expression evaluator needs some work.

Feel free to send in push requests to fix bugs or add features. All help is appreciated.

### Useful contributions

If you would like to continue developing on this, feel free to fork it.

## Building

The library is a combined runtime and parser, instead of splitting it up. Neither JSON output or input is supported at present; only plain Ink. In general, I
am not particularly interested in the JSON format, and probably won't ever implement it, given that I don't really see a compelling need for it.

To build the jar:
```
./gradlew build
```

To run the tests:
```
./gradlew test
```
Use the `bat` file on windows.


## Usage

To use the library, include the jitpack.io repository and the dependency to the latest release:

```
repositories {
  maven { url 'https://jitpack.io' }
}

dependencies {
  compile 'com.github.micabytes:mica-ink:0.1.0'
}
```

For usage in general, check the StoryViewModel in the [sample StoryBytes app](https://github.com/micabytes/storybytes-desktop).


