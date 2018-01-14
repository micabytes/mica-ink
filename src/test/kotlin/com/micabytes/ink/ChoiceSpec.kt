package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import com.micabytes.ink.util.InkRunTimeException
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit

object ChoiceSpec : Spek({

  given("a section with a single choice") {
    val singleChoice =
        """== test_knot
          |Hello, world!
          |* Hello back!
          |  Nice to hear from you
          """.trimMargin()

    it("demarcates the end of the text for tue parent container") {
      val inputStream = IOUtils.toInputStream(singleChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text = story.next()
      text.size shouldEqual 1
      text[0] shouldEqual "Hello, world!"
    }

    it("continues processing with the choice text when a choice is selected") {
      val inputStream = IOUtils.toInputStream(singleChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      val text = story.next()
      text.size shouldEqual (3)
      text[0] shouldEqual ("Hello, world!")
      text[1] shouldEqual ("Hello back!")
      text[2] shouldEqual ("Nice to hear from you")
    }
  }

  given("a section with multiple choices") {
    val multiChoice =
        """== test_knot
          |Hello, world!
          |* Hello back!
          |  Nice to hear from you
          |* Goodbye
          |  See you later
          """.trimMargin()

    it("continues with the text of the selected choice when multiple choices exist") {
      val inputStream = IOUtils.toInputStream(multiChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(1)
      val text = story.next()
      text.size shouldEqual (3)
      text[1] shouldEqual ("Goodbye")
      text[2] shouldEqual ("See you later")
    }

  }

  given("a choice with suppressed text") {
    val suppressChoice =
        """== test_knot
          |Hello, world!
          |*  [Hello back!]
          |  Nice to hear from you.
      """.trimMargin()

    it("should be suppressed in the text flow using the [] syntax") {
      val inputStream = IOUtils.toInputStream(suppressChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceText(0) shouldEqual ("Hello back!")
      story.choose(0)
      val text = story.next()
      text.size shouldEqual (2)
      text[1] shouldEqual ("Nice to hear from you.")
    }

    val mixedChoice =
        """== test_knot
                |Hello world!
                |*   Hello [back!] right back to you!
                |    Nice to hear from you.
                """.trimMargin()

    it("be mixed in to the text using the [] syntax") {
      val inputStream = IOUtils.toInputStream(mixedChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceText(0) shouldEqual ("Hello back!")
      story.choose(0)
      val text = story.next()
      text.size shouldEqual (3)
      text[1] shouldEqual ("Hello right back to you!")
      text[2] shouldEqual ("Nice to hear from you.")
    }

  }

  given("a section with once-only choices") {

    val varyingChoice =
        """=== find_help ===
        |
        |    You search desperately for a friendly face in the crowd.
        |    *   The woman in the hat[?] pushes you roughly aside. -> find_help
        |    *   The man with the briefcase[?] looks disgusted as you stumble past him. -> find_help
      """.trimMargin()

    it("disappear when used the first time") {
      val inputStream = IOUtils.toInputStream(varyingChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (2)
      story.choose(0)
      story.next()
      story.choiceSize shouldEqual (1)
      story.choiceText(0) shouldEqual ("The man with the briefcase?")
    }

  }

  given("a sticky choices") {
    val stickyChoice =
        """=== homers_couch ===
        |    +   [Eat another donut]
        |        You eat another donut. -> homers_couch
        |    *   [Get off the couch]
        |        You struggle up off the couch to go and compose epic poetry.
        |        -> END
      """.trimMargin()

    it("not disappear when used") {
      val inputStream = IOUtils.toInputStream(stickyChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (2)
      story.choose(0)
      story.next()
      story.choiceSize shouldEqual (2)
    }

  }

  given("a fallback choices") {
    val fallbackChoice =
        """=== find_help ===
        |
        |    You search desperately for a friendly face in the crowd.
        |    *   The woman in the hat[?] pushes you roughly aside. -> find_help
        |    *   The man with the briefcase[?] looks disgusted as you stumble past him. -> find_help
        |    *   [] But it is too late: you collapse onto the station platform. This is the end.
        |        -> END
      """.trimMargin()

    it("should not be shown if there are non-fallback choices available") {
      val inputStream = IOUtils.toInputStream(fallbackChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (2)
    }

    it("should be diverted to directly if all other choices are exhausted") {
      val inputStream = IOUtils.toInputStream(fallbackChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (2)
      story.choose(0)
      story.next()
      story.choiceSize shouldEqual (1)
      story.choose(0)
      val text = story.next()
      story.isEnded shouldEqual (true)
      text[text.size - 1] shouldEqual ("But it is too late: you collapse onto the station platform. This is the end.")
    }

    // TODO: Error if fallback choice is not the last.

  }

  given("a conditional choice") {
    val conditionalChoice =
        """=== choice_test ===
        |Test conditional choices
        |* { true } { false } not displayed
        |* { true } { true } { true and true }  one
        |* { false } not displayed
        |* { true } two
        |* { true } { true } three
        |* { true } four
      """.trimMargin()

    it("should not be visible if the condition evaluates to 0 (false)") {
      val inputStream = IOUtils.toInputStream(conditionalChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (4)
    }
  }

  given("a labelled choice") {

    val labelFlow =
        """=== meet_guard ===
        |The guard frowns at you.
        |*   (greet) [Greet him]
        |    'Greetings.'
        |*   (get_out) 'Get out of my way[.'],' you tell the guard.
        |-   'Hmm,' replies the guard.
        |*   {greet}     'Having a nice day?'
        |*   'Hmm?'[] you reply.        |
        |*   {get_out} [Shove him aside]
        |    You shove him sharply. He stares in reply, and draws his sword!
        |    -> END
        |-   'Mff,' the guard replies, and then offers you a paper bag. 'Toffee?'
        |    -> END
      """.trimMargin()

    it("handle labels on choices and evaluate in expressions (example 1)") {
      val inputStream = IOUtils.toInputStream(labelFlow, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      story.next()
      story.choiceSize shouldEqual (2)
      story.choiceText(0) shouldEqual ("\'Having a nice day?\'")
    }

    it("handle labels on choices and evaluate in expressions (example 2)") {
      val inputStream = IOUtils.toInputStream(labelFlow, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(1)
      story.next()
      story.choiceSize shouldEqual (2)
      story.choiceText(1) shouldEqual ("Shove him aside")
    }

    val labelScope =
        """=== knot ===
          |  = stitch_one
          |    * an option
          |    - (gatherpoint) Some content.
          |      -> knot.stitch_two
          |  = stitch_two
          |    * {knot.stitch_one.gatherpoint} Found gatherpoint
        """.trimMargin()

    it("allow label references out of scope using the full path id") {
      val inputStream = IOUtils.toInputStream(labelScope, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      story.next()
      story.choiceSize shouldEqual (1)
      story.choiceText(0) shouldEqual ("Found gatherpoint")
    }

    val labelScopeError =
        """=== knot ===
        |  = stitch_one
        |    * an option
        |    - (gatherpoint) Some content.
        |      -> knot.stitch_two
        |  = stitch_two
        |    * {gatherpoint} Found gatherpoint
      """.trimMargin()

    xit("fail label references that are out of scope") {
      val inputStream = IOUtils.toInputStream(labelScopeError, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      val wrongNext = { story.next() }
      wrongNext shouldThrow InkRunTimeException::class
    }//.config(enabled = false)

  }

  given("a divert choice") {
    val divertChoice =
        """=== knot
          |You see a soldier.
          |*   [Pull a face]
          |    You pull a face, and the soldier comes at you! -> shove
          |*   (shove) [Shove the guard aside] You shove the guard to one side, but he comes back swinging.
          |*   {shove} [Grapple and fight] -> fight_the_guard
          |-   -> knot
        """.trimMargin()

    xit("be used up if they are once-only and a divert goes through them") {
      val inputStream = IOUtils.toInputStream(divertChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (2)
      story.choose(0)
      val text = story.next()
      text.size shouldEqual (2)
      text[1] shouldEqual ("You pull a face, and the soldier comes at you! You shove the guard to one side, but he comes back swinging.")
      story.choiceSize shouldEqual (1)
      story.choiceText(0) shouldEqual ("Grapple and fight")
    }//.config(enabled = false)

  }

})
