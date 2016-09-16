package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class KnotSpec extends Specification {

  "A single line of plain text in an ink file" should {
    val testData = "Hello, world!"
    val inputStream = IOUtils.toInputStream(testData, "UTF-8")
    val story = InkParser.parse(inputStream, new StoryContainer(), null)

    "- return a single line of text as output" in {
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Hello, world!")
    }

  }

  "Multiple lines of plain text in an ink file" should {

    "- return an equal number of lines as output" in {
      val testData =
        """Hello, world!
          |Hello?
          |Hello, are you there?""".stripMargin
      val inputStream = IOUtils.toInputStream(testData, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      val text = story.nextAll()
      text.size() must beEqualTo(3)
      text.get(0) must beEqualTo("Hello, world!")
      text.get(1) must beEqualTo("Hello?")
      text.get(2) must beEqualTo("Hello, are you there?")
    }

    "- strip empty lines of output" in {
      val testData =
        """Hello, world!
          |
          |Hello?
          |Hello, are you there?
        """.stripMargin
      val inputStream = IOUtils.toInputStream(testData, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      val text = story.nextAll()
      text.size() must beEqualTo(3)
      text.get(0) must beEqualTo("Hello, world!")
      text.get(1) must beEqualTo("Hello?")
      text.get(2) must beEqualTo("Hello, are you there?")
    }

  }

  "Knots" should {

    val paramStrings =
      """Who do you accuse?
        |* [Accuse Hasting] -> accuse("Hastings")
        |* [Accuse Mrs Black] -> accuse("Claudia")
        |* [Accuse myself] -> accuse("myself")
        |
        |=== accuse(who) ===
        |    "I accuse {who}!" Poirot declared.
      """.stripMargin

    "- handle string parameters in a divert" in {
      val inputStream = IOUtils.toInputStream(paramStrings, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(2)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("\"I accuse myself!\" Poirot declared.")
    }

    val paramInts =
      """How much do you give?
        |* [$1] -> give(1)
        |* [$2] -> give(2)
        |* [Nothing] -> give(0)
        |
        |=== give(amount) ===
        |    You give {amount} dollars.
      """.stripMargin

    "- handle passing integer as parameters in a divert" in {
      val inputStream = IOUtils.toInputStream(paramInts, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(1)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("You give 2 dollars.")
    }

    val paramFloats =
    """How much do you give?
      |* [$1] -> give(1.2)
      |* [$2] -> give(2.5)
      |* [Nothing] -> give(0)
      |
      |=== give(amount) ===
      |    You give {amount} dollars.
    """.stripMargin

    "- handle passing floats as parameters in a divert" in {
      val inputStream = IOUtils.toInputStream(paramFloats, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(1)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("You give 2.5 dollars.")
    }

    val paramVars =
      """VAR x = 1
        |VAR y = 2
        |VAR z = 0
        |How much do you give?
        |* [$1] -> give(x)
        |* [$2] -> give(y)
        |* [Nothing] -> give(z)
        |
        |=== give(amount) ===
        |    You give {amount} dollars.
      """.stripMargin

    "- handle passing variables as parameters in a divert" in {
      val inputStream = IOUtils.toInputStream(paramVars, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(1)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("You give 2 dollars.")
    }

    val paramMulti =
      """VAR x = 1
        |VAR y = "Hmm."
        |How much do you give?
        |* [I don't know] -> give(x, 2, y)
        |
        |=== give(a, b, c) ===
        |    You give {a} or {b} dollars. {y}
      """.stripMargin

    "- handle passing multiple values as parameters in a divert" in {
      val inputStream = IOUtils.toInputStream(paramMulti, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(0)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("You give 1 or 2 dollars. Hmm.")
    }

    val paramRecurse =
      """-> add_one_to_one_hundred(0, 1)
        |
        |=== add_one_to_one_hundred(total, x) ===
        |    ~ total = total + x
        |    { x == 15:
        |        -> finished(total)
        |    - else:
        |        -> add_one_to_one_hundred(total, x + 1)
        |    }
        |
        |=== finished(total) ===
        |    "The result is {total}!" you announce.
        |    Gauss stares at you in horror.
        |    -> END
      """.stripMargin

    "- should support recursive calls with parameters on a knot" in {
      val inputStream = IOUtils.toInputStream(paramRecurse, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      val text = story.nextAll()
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("\"The result is 120!\" you announce.")
    }
  }

}
