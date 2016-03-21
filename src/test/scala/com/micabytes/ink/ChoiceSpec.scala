package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ChoiceSpec extends Specification {

  "Choices" should {
    val singleChoice =
      """Hello, world!
        |* Hello back!
        |  Nice to hear from you
      """.stripMargin
    val multiChoice =
      """Hello, world!
        |* Hello back!
        |  Nice to hear from you
        |* Goodbye
        |  See you later
      """.stripMargin
    val suppressChoice =
      """Hello world!
        |*   [Hello back!]
        |    Nice to hear from you.
      """.stripMargin
    val mixedChoice =
      """Hello world!
        |*   Hello [back!] right back to you!
        |    Nice to hear from you.
      """.stripMargin
    val varyingChoice =
      """=== find_help ===
        |
        |    You search desperately for a friendly face in the crowd.
        |    *   The woman in the hat[?] pushes you roughly aside. -> find_help
        |    *   The man with the briefcase[?] looks disgusted as you stumble past him. -> find_help
      """.stripMargin
    val stickyChoice =
      """=== homers_couch ===
        |    +   [Eat another donut]
        |        You eat another donut. -> homers_couch
        |    *   [Get off the couch]
        |        You struggle up off the couch to go and compose epic poetry.
        |        -> END
      """.stripMargin
    val fallbackChoice =
      """=== find_help ===
        |
        |    You search desperately for a friendly face in the crowd.
        |    *   The woman in the hat[?] pushes you roughly aside. -> find_help
        |    *   The man with the briefcase[?] looks disgusted as you stumble past him. -> find_help
        |    *   [] But it is too late: you collapse onto the station platform. This is the end.
        |        -> END
      """.stripMargin
    val conditionalChoice =
      """=== choice_test ===
        |Test conditional choices
        |* { true } { false } not displayed
        |* { true } { true } { true and true }  one
        |* { false } not displayed
        |* { true } two
        |* { true } { true } three
        |* { true } four
      """.stripMargin

    "- demarcate end of text for parent container" in {
      val inputStream = IOUtils.toInputStream(singleChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.allLines
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Hello, world!")
    }

    "- continue processing with the choice text when a choice is selected" in {
      val inputStream = IOUtils.toInputStream(singleChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines
      story.choose(0)
      val text = story.allLines
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("Hello back!")
      text.get(1) must beEqualTo("Nice to hear from you")
    }

    "- continue with the text of the selected choice when multiple choices exist" in {
      val inputStream = IOUtils.toInputStream(multiChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines
      story.choose(1)
      val text = story.allLines
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("Goodbye")
      text.get(1) must beEqualTo("See you later")
    }

    "- be suppressed in the text flow using the [] syntax" in {
      val inputStream = IOUtils.toInputStream(suppressChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines()
      val choice = story.getChoice(0)
      choice.getChoiceText(story) must beEqualTo("Hello back!")
      story.choose(0)
      val text = story.allLines()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Nice to hear from you.")
    }

    "- be mixed in to the text using the [] syntax" in {
      val inputStream = IOUtils.toInputStream(mixedChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines()
      val choice = story.getChoice(0)
      choice.getChoiceText(story) must beEqualTo("Hello back!")
      story.choose(0)
      val text = story.allLines()
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("Hello right back to you!")
      text.get(1) must beEqualTo("Nice to hear from you.")
    }

    "- disappear when used if they are a once-only choice" in {
      val inputStream = IOUtils.toInputStream(varyingChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines()
      story.getChoiceSize must beEqualTo(2)
      story.choose(0)
      story.allLines()
      story.getChoiceSize must beEqualTo(1)
      val choice = story.getChoice(0)
      choice.getChoiceText(story) must beEqualTo("The man with the briefcase?")
    }

    "- not disappear when used if they are a sticky choices" in {
      val inputStream = IOUtils.toInputStream(stickyChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines()
      story.getChoiceSize must beEqualTo(2)
      story.choose(0)
      story.allLines()
      story.getChoiceSize must beEqualTo(2)
    }

    "- not be shown if it is a fallback choice and there are non-fallback choices available" in {
      val inputStream = IOUtils.toInputStream(fallbackChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines
      story.getChoiceSize must beEqualTo(2)
    }

    "- should be diverted to directly if it is a fallback choice and no others exist" in {
      val inputStream = IOUtils.toInputStream(fallbackChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines()
      story.getChoiceSize must beEqualTo(2)
      story.choose(0)
      story.allLines()
      story.choose(0)
      story.allLines()
      story.isEnded must beEqualTo(true)
    }

    // TODO: Probably throw an error if there are non-fallback choices following a fallback choice.

    "- not be visible if their conditions evaluate to 0" in {
      val inputStream = IOUtils.toInputStream(conditionalChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.allLines()
      story.getChoiceSize must beEqualTo(4)
    }



  }


}
