package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class VariableTextSpec extends Specification {

  "Sequences" should {
    val sequence =
      """=== test
        |The radio hissed into life. {"Three!"|"Two!"|"One!"|There was the white noise racket of an explosion.}
        |+ [Again] -> test
      """.stripMargin

    "- step through each element and repeat the final element" in {
      val inputStream = IOUtils.toInputStream(sequence, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("The radio hissed into life. \"Two!\"")
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("The radio hissed into life. \"One!\"")
      story.choose(0)
      val text3 = story.nextChoice()
      text3.size() must beEqualTo(1)
      text3.get(0) must beEqualTo("The radio hissed into life. There was the white noise racket of an explosion.")
      story.choose(0)
      val text4 = story.nextChoice()
      text4.size() must beEqualTo(1)
      text4.get(0) must beEqualTo("The radio hissed into life. There was the white noise racket of an explosion.")
    }
  }

  "Cycles" should {
    val cycle =
      """=== test
        |The radio hissed into life. {&"Three!"|"Two!"|"One!"}
        |+ [Again] -> test
      """.stripMargin

    "- cycle through the element repeatedly" in {
      val inputStream = IOUtils.toInputStream(cycle, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("The radio hissed into life. \"Two!\"")
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("The radio hissed into life. \"One!\"")
      story.choose(0)
      val text3 = story.nextChoice()
      text3.size() must beEqualTo(1)
      text3.get(0) must beEqualTo("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text4 = story.nextChoice()
      text4.size() must beEqualTo(1)
      text4.get(0) must beEqualTo("The radio hissed into life. \"Two!\"")
    }
  }


  "Once-only lists" should {
    val once =
      """=== test
        |The radio hissed into life. {!"Three!"|"Two!"|"One!"}
        |+ [Again] -> test
      """.stripMargin

    "- step through each element and return no text once the list is exhausted" in {
      val inputStream = IOUtils.toInputStream(once, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("The radio hissed into life. \"Two!\"")
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("The radio hissed into life. \"One!\"")
      story.choose(0)
      val text3 = story.nextChoice()
      text3.size() must beEqualTo(1)
      text3.get(0) must beEqualTo("The radio hissed into life.")
      story.choose(0)
      val text4 = story.nextChoice()
      text4.size() must beEqualTo(1)
      text4.get(0) must beEqualTo("The radio hissed into life.")
    }
  }

  // TODO: No idea how to do a good tests of Shuffles (random lists).

  "Variable text" should {
    val emptyElements =
      """=== test
        |The radio hissed into life. {||"One!"}
        |+ [Again] -> test
      """.stripMargin
    val listInChoice =
      """=== test
        |He looked at me oddly.
        |+ ["Hello, {&Master|Monsieur|you}!"] -> test
      """.stripMargin
    val nestedList =
      """=== test
        |The radio hissed into life. {||"One!"}
        |+ [Again] -> test
      """.stripMargin

    "- allow for empty text elements in the list" in {
      val inputStream = IOUtils.toInputStream(emptyElements, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("The radio hissed into life.")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("The radio hissed into life.")
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("The radio hissed into life. \"One!\"")
    }

    "- be usable in a choice test" in {
      val inputStream = IOUtils.toInputStream(listInChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.nextChoice()
      val choice0 = story.getChoice(0)
      choice0.getChoiceText(story) must beEqualTo("\"Hello, Master!\"")
      story.choose(0)
      story.nextChoice()
      val choice1 = story.getChoice(0)
      choice1.getChoiceText(story) must beEqualTo("\"Hello, Monsieur!\"")
      story.choose(0)
      story.nextChoice()
      val choice2 = story.getChoice(0)
      choice2.getChoiceText(story) must beEqualTo("\"Hello, you!\"")
    }

  }


}
