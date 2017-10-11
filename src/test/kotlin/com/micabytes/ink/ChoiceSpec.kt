package com.micabytes.ink

import com.micabytes.ink.util.InkRunTimeException
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class ChoiceSpec : WordSpec() {

  init {

    "Choices" should {

      val singleChoice =
          """== test_knot
          |Hello, world!
          |* Hello back!
          |  Nice to hear from you
          """.trimMargin()

      "demarcate end of text for parent container" {
        val inputStream = IOUtils.toInputStream(singleChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text[0] shouldBe ("Hello, world!")
      }

      "continue processing with the choice text when a choice is selected" {
        val inputStream = IOUtils.toInputStream(singleChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(0)
        val text = story.next()
        text.size shouldBe (3)
        text[0] shouldBe ("Hello, world!")
        text[1] shouldBe ("Hello back!")
        text[2] shouldBe ("Nice to hear from you")
      }

      val multiChoice =
          """== test_knot
          |Hello, world!
          |* Hello back!
          |  Nice to hear from you
          |* Goodbye
          |  See you later
          """.trimMargin()

      "continue with the text of the selected choice when multiple choices exist" {
        val inputStream = IOUtils.toInputStream(multiChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(1)
        val text = story.next()
        text.size shouldBe (3)
        text[1] shouldBe ("Goodbye")
        text[2] shouldBe ("See you later")
      }

      val suppressChoice =
          """== test_knot
          |Hello, world!
          |*  [Hello back!]
          |  Nice to hear from you.
      """.trimMargin()

      "be suppressed in the text flow using the [] syntax" {
        val inputStream = IOUtils.toInputStream(suppressChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceText(0) shouldBe ("Hello back!")
        story.choose(0)
        val text = story.next()
        text.size shouldBe (2)
        text[1] shouldBe ("Nice to hear from you.")
      }

      val mixedChoice =
              """== test_knot
                |Hello world!
                |*   Hello [back!] right back to you!
                |    Nice to hear from you.
                """.trimMargin()

      "be mixed in to the text using the [] syntax" {
        val inputStream = IOUtils.toInputStream(mixedChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceText(0) shouldBe ("Hello back!")
        story.choose(0)
        val text = story.next()
        text.size shouldBe (3)
        text[1] shouldBe ("Hello right back to you!")
        text[2] shouldBe ("Nice to hear from you.")
      }

      val varyingChoice =
          """=== find_help ===
        |
        |    You search desperately for a friendly face in the crowd.
        |    *   The woman in the hat[?] pushes you roughly aside. -> find_help
        |    *   The man with the briefcase[?] looks disgusted as you stumble past him. -> find_help
      """.trimMargin()

      "disappear when used if they are a once-only choice" {
        val inputStream = IOUtils.toInputStream(varyingChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceSize shouldBe (2)
        story.choose(0)
        story.next()
        story.choiceSize shouldBe (1)
        story.choiceText(0) shouldBe ("The man with the briefcase?")
      }

      val stickyChoice =
          """=== homers_couch ===
        |    +   [Eat another donut]
        |        You eat another donut. -> homers_couch
        |    *   [Get off the couch]
        |        You struggle up off the couch to go and compose epic poetry.
        |        -> END
      """.trimMargin()

      "not disappear when used if they are a sticky choices" {
        val inputStream = IOUtils.toInputStream(stickyChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceSize shouldBe (2)
        story.choose(0)
        story.next()
        story.choiceSize shouldBe (2)
      }

      val fallbackChoice =
          """=== find_help ===
        |
        |    You search desperately for a friendly face in the crowd.
        |    *   The woman in the hat[?] pushes you roughly aside. -> find_help
        |    *   The man with the briefcase[?] looks disgusted as you stumble past him. -> find_help
        |    *   [] But it is too late: you collapse onto the station platform. This is the end.
        |        -> END
      """.trimMargin()

      "not be shown if it is a fallback choice and there are non-fallback choices available" {
        val inputStream = IOUtils.toInputStream(fallbackChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceSize shouldBe (2)
      }

      "should be diverted to directly if it is a fallback choice and no others exist" {
        val inputStream = IOUtils.toInputStream(fallbackChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceSize shouldBe (2)
        story.choose(0)
        story.next()
        story.choiceSize shouldBe (1)
        story.choose(0)
        val text = story.next()
        story.isEnded shouldBe (true)
        text[text.size - 1] shouldBe ("But it is too late: you collapse onto the station platform. This is the end.")
      }

      // TODO: Error if fallback choice is not the last.

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

      "not be visible if their conditions evaluate to 0" {
        val inputStream = IOUtils.toInputStream(conditionalChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceSize shouldBe (4)
      }

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

      "handle labels on choices and evaluate in expressions (example 1)" {
        val inputStream = IOUtils.toInputStream(labelFlow, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(0)
        story.next()
        story.choiceSize shouldBe (2)
        story.choiceText(0) shouldBe ("\'Having a nice day?\'")
      }

      "handle labels on choices and evaluate in expressions (example 2)" {
        val inputStream = IOUtils.toInputStream(labelFlow, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(1)
        story.next()
        story.choiceSize shouldBe (2)
        story.choiceText(1) shouldBe ("Shove him aside")
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

      "allow label references out of scope using the full path id" {
        val inputStream = IOUtils.toInputStream(labelScope, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(0)
        story.next()
        story.choiceSize shouldBe (1)
        story.choiceText(0) shouldBe ("Found gatherpoint")
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

    "fail label references that are out of scope" {
      val inputStream = IOUtils.toInputStream(labelScopeError, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      shouldThrow<InkRunTimeException> {
        story.next()
      }
    }.config(enabled = false)

      val divertChoice =
          """=== knot
          |You see a soldier.
          |*   [Pull a face]
          |    You pull a face, and the soldier comes at you! -> shove
          |*   (shove) [Shove the guard aside] You shove the guard to one side, but he comes back swinging.
          |*   {shove} [Grapple and fight] -> fight_the_guard
          |-   -> knot
        """.trimMargin()

      "be used up if they are once-only and a divert goes through them" {
        val inputStream = IOUtils.toInputStream(divertChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceSize shouldBe (2)
        story.choose(0)
        val text = story.next()
        text.size shouldBe (2)
        text[1] shouldBe ("You pull a face, and the soldier comes at you! You shove the guard to one side, but he comes back swinging.")
        story.choiceSize shouldBe (1)
        story.choiceText(0) shouldBe ("Grapple and fight")
      }.config(enabled = false)

    }

  }
}