# jInk

[![CI Status](http://img.shields.io/travis/micabytes/jink.svg?style=flat)](https://travis-ci.org/micabytes/jink)

jInk is - or rather - may become a Java implementation of Inkle Studios (@inkle) scripting language ink for writing interactive narrative.

At present, this is a mostly experimental implementation which I did while working through some of the low-level nuts and bolts of Ink. You should probably not make use of this for anything serious yet, as I am likely to change things around in future (plus maybe implement a more robust parser).

Currently, this reimplements Part 1 of the [Ink documentation](https://github.com/inkle/ink/blob/master/Documentation/WritingWithInk.md), with the following exceptions (that I know of - other things may be missing as well):
- Conditions on choices (as well as the choice text) cannot be multi-line.
- evaluation of conditions has some limitations (lack of support for not, evaluation of 'and' and 'or' is space sensitive).  
- INCLUDE is not implemented.
- Comments are not implemented.
- CHOICE_COUNT and TURNS_SINCE are not implemented (essentially functions, and those haven't been implemented yet).
- Doesn't parse check for -> END yet (i.e., quite happy to have knots and threads ending in nothing).

Most of the examples in that section of the documentation are implemented as tests, with a few additional test cases grabbed from the 'ink/Tests' where appropriate.

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

To use the library:

```
import com.micabytes.ink.*;
```

Loading a story file:
```
  Story story = InkParser.parse(inputstream);
```

Working through content line by line:
```
  while (story.hasNext()) {
    String line = story.next();
    ...
  }
```

Working through all content content line by line:
```
  List<String> lines = story.nextChoice();
```

Interface will almost certainly change later, though. Unfortunately, the existing ink interface doesn't work with standard Java (`continue` is a reserved keyword).


