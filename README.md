# jInk

[![CI Status](http://img.shields.io/travis/micabytes/jink.svg?style=flat)](https://travis-ci.org/micabytes/jink)

jInk is - or rather - may become a Java implementation of Inkle Studios (@inkle) scripting language ink for writing interactive narrative.

At present, this is a mostly experimental implementation which I did while working through some of the low-level nuts and bolts of Ink. You should probably not make use of this for anything serious yet, as I am likely to change things around in future (plus maybe implement a more robust parser).

Currently, this reimplements Part 1 to Part 3 of the [Ink documentation](https://github.com/inkle/ink/blob/master/Documentation/WritingWithInk.md), with the following omissions (that I know of - other things may be missing that I've simply forgotten to get implemented):

- Conditional texts (simply forgot to implement) 
- Conditions on choices (as well as the choice text) cannot be multi-line.
- evaluation of conditions has some limitations (lack of support for not, evaluation of 'and' and 'or' is space sensitive).
- INCLUDE is not implemented.
- Comments are not implemented.
- CHOICE_COUNT and TURNS_SINCE are not implemented (essentially functions, and those haven't been implemented yet).
- Doesn't parse check for -> END yet (i.e., quite happy to have knots and threads ending in nothing).
- Have not tested "Advanced: Gathers directly after an option"
- No temporary variables at this point (parameters exist, though).
- No pass by reference parameters.
- No constants.
- No extern functions.

And of course, the key elements of Part 4 are missing:
- Tunnels
- Threads

Most of the examples in the implemented section of the documentation are used as tests, with a few additional test cases grabbed from the 'ink/Tests' where appropriate.

This implementation does one thing that the standard Ink implementation does not, which is to allow the use of native Java objects as variables, and permit the calling of methods on those objects.

## Roadmap

I would like to use this implementation in some of my own (Java-based) games, so I plan to continue working on this as and when circumstances allow,

- Extend the engine until it implements all (or nearly all) of the standard Ink features.
- Add more comprehensive testing to code. Many corner cases are not covered adequately yet.
- Clean up and refactor the code. Especially the variable/expression evaluator needs some work.

Feel free to send in push requests to fix bugs or add features. All help is appreciated.

## Building

The library is a combined runtime and parser, instead of splitting it up. Neither JSON output or input is supported at present; only plain Ink. Since the JSON format is not really stable yet, I probably won't be spending much time looking at that for now.

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

To use the library:

```
import com.micabytes.ink.Story;
```

Loading a story file:
```
  Story story = InkParser.parse(inputStream, new StoryContainer());
```

Working through content line by line:
```
  while (story.hasNext()) {
    String line = story.next();
    ...
  }
```

Working through all content content line by line up to the next stop:
```
  List<String> lines = story.nextAll();
```

To get the available choices:
```
  for (int i=0; i<story.getChoiceSize(); i++) {
    Choice choice = story.getChoice(i);
    String choiceText = choice.getChoiceText(story);
    ...
  }
```

To select a choice:
```
  story.choose(0)
```

Note: Unfortunately, the existing ink interface doesn't work with standard Java (`continue` is a reserved keyword), so jInk will end up with a different interface. The above interface may change, as I clean up code and/or get a better idea.


