package com.micabytes.ink

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class VariableTextSpec : WordSpec() {

  init {
    "Sequences" should {
      val sequence =
          """=== test
        |The radio hissed into life. {"Three!"|"Two!"|"One!"|There was the white noise racket of an explosion.}
        |+ [Again] -> test
      """.trimMargin()

      "step through each element and repeat the final element" {
        val inputStream = IOUtils.toInputStream(sequence, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0[0] shouldBe ("The radio hissed into life. \"Three!\"")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (2)
        text1[1] shouldBe ("The radio hissed into life. \"Two!\"")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (3)
        text2[2] shouldBe ("The radio hissed into life. \"One!\"")
        story.choose(0)
        val text3 = story.next()
        text3.size shouldBe (4)
        text3[3] shouldBe ("The radio hissed into life. There was the white noise racket of an explosion.")
        story.choose(0)
        val text4 = story.next()
        text4.size shouldBe (5)
        text4[4] shouldBe ("The radio hissed into life. There was the white noise racket of an explosion.")
      }
    }

    "Cycles" should {
      val cycle =
          """=== test
        |The radio hissed into life. {&"Three!"|"Two!"|"One!"}
        |+ [Again] -> test
      """.trimMargin()

      "cycle through the element repeatedly" {
        val inputStream = IOUtils.toInputStream(cycle, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0[0] shouldBe ("The radio hissed into life. \"Three!\"")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (2)
        text1[1] shouldBe ("The radio hissed into life. \"Two!\"")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (3)
        text2[2] shouldBe ("The radio hissed into life. \"One!\"")
        story.choose(0)
        val text3 = story.next()
        text3.size shouldBe (4)
        text3[3] shouldBe ("The radio hissed into life. \"Three!\"")
        story.choose(0)
        val text4 = story.next()
        text4.size shouldBe (5)
        text4[4] shouldBe ("The radio hissed into life. \"Two!\"")
      }
    }


    "Once-only lists" should {
      val once =
          """=== test
        |The radio hissed into life. {!"Three!"|"Two!"|"One!"}
        |+ [Again] -> test
      """.trimMargin()

      "step through each element and return no text once the list is exhausted" {
        val inputStream = IOUtils.toInputStream(once, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0[0] shouldBe ("The radio hissed into life. \"Three!\"")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (2)
        text1[1] shouldBe ("The radio hissed into life. \"Two!\"")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (3)
        text2[2] shouldBe ("The radio hissed into life. \"One!\"")
        story.choose(0)
        val text3 = story.next()
        text3.size shouldBe (4)
        text3[3] shouldBe ("The radio hissed into life.")
        story.choose(0)
        val text4 = story.next()
        text4.size shouldBe (5)
        text4[4] shouldBe ("The radio hissed into life.")
      }
    }

    // TODO: No idea how to do a good tests of Shuffles (random lists).

    "Declaration text" should {
      val emptyElements =
          """=== test
        |The radio hissed into life. {||"One!"}
        |+ [Again] -> test
      """.trimMargin()

      "allow for empty text elements in the list" {
        val inputStream = IOUtils.toInputStream(emptyElements, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0[0] shouldBe ("The radio hissed into life.")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (2)
        text1[1] shouldBe ("The radio hissed into life.")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (3)
        text2[2] shouldBe ("The radio hissed into life. \"One!\"")
      }

      val listInChoice =
          """=== test
        |He looked at me oddly.
        |+ ["Hello, {&Master|Monsieur|you}!"] -> test
      """.trimMargin()

      "be usable in a choice test" {
        val inputStream = IOUtils.toInputStream(listInChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choiceText(0) shouldBe ("\"Hello, Master!\"")
        story.choose(0)
        story.next()
        story.choiceText(0) shouldBe ("\"Hello, Monsieur!\"")
        story.choose(0)
        story.next()
        story.choiceText(0) shouldBe ("\"Hello, you!\"")
      }

      /*
    val nestedList =
      """=== test
        |The radio hissed into life. {||"One!"}
        |+ [Again] -> test
      """.trimMargin()
    */
      // TODO: add test

    }

    "Value evaluated lists" should {
      val one =
          """=== test
        |VAR x = 1
        |We needed to find {? x : nothing|one apple|two pears|many oranges}.
        |-> END
      """.trimMargin()

      "return the text string in the sequence if the condition is a valid value" {
        val inputStream = IOUtils.toInputStream(one, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0[0] shouldBe ("We needed to find one apple.")
      }

      val minusOne =
          """=== test
        |VAR x = -1
        |We needed to find {? x : nothing|one apple|two pears|many oranges}.
        |-> END
      """.trimMargin()

      "return the text string in the sequence if the condition is a valid value" {
        val inputStream = IOUtils.toInputStream(minusOne, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0[0] shouldBe ("We needed to find nothing.")
      }

      val ten =
          """=== test
        |VAR x = 10
        |We needed to find {? x : nothing|one apple|two pears|many oranges}.
        |-> END
      """.trimMargin()

      "return the text string in the sequence if the condition is a valid value" {
        val inputStream = IOUtils.toInputStream(ten, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0[0] shouldBe ("We needed to find many oranges.")
      }
    }
  }
}