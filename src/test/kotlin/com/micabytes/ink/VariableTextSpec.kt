package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class VariableTextSpec : Spek({

  given("Sequences") {
    val sequence =
        """=== test
        |The radio hissed into life. {"Three!"|"Two!"|"One!"|There was the white noise racket of an explosion.}
        |+ [Again] -> test
      """.trimMargin()

    it("step through each element and repeat the final element") {
      val inputStream = IOUtils.toInputStream(sequence, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text0 = story.next()
      text0.size shouldEqual (1)
      text0[0] shouldEqual ("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text1 = story.next()
      text1.size shouldEqual (2)
      text1[1] shouldEqual ("The radio hissed into life. \"Two!\"")
      story.choose(0)
      val text2 = story.next()
      text2.size shouldEqual (3)
      text2[2] shouldEqual ("The radio hissed into life. \"One!\"")
      story.choose(0)
      val text3 = story.next()
      text3.size shouldEqual (4)
      text3[3] shouldEqual ("The radio hissed into life. There was the white noise racket of an explosion.")
      story.choose(0)
      val text4 = story.next()
      text4.size shouldEqual (5)
      text4[4] shouldEqual ("The radio hissed into life. There was the white noise racket of an explosion.")
    }
  }

  given("Cycles") {
    val cycle =
        """=== test
        |The radio hissed into life. {&"Three!"|"Two!"|"One!"}
        |+ [Again] -> test
      """.trimMargin()

    it("cycle through the element repeatedly") {
      val inputStream = IOUtils.toInputStream(cycle, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text0 = story.next()
      text0.size shouldEqual (1)
      text0[0] shouldEqual ("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text1 = story.next()
      text1.size shouldEqual (2)
      text1[1] shouldEqual ("The radio hissed into life. \"Two!\"")
      story.choose(0)
      val text2 = story.next()
      text2.size shouldEqual (3)
      text2[2] shouldEqual ("The radio hissed into life. \"One!\"")
      story.choose(0)
      val text3 = story.next()
      text3.size shouldEqual (4)
      text3[3] shouldEqual ("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text4 = story.next()
      text4.size shouldEqual (5)
      text4[4] shouldEqual ("The radio hissed into life. \"Two!\"")
    }
  }


  given("Once-only lists") {
    val once =
        """=== test
        |The radio hissed into life. {!"Three!"|"Two!"|"One!"}
        |+ [Again] -> test
      """.trimMargin()

    it("step through each element and return no text once the list is exhausted") {
      val inputStream = IOUtils.toInputStream(once, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text0 = story.next()
      text0.size shouldEqual (1)
      text0[0] shouldEqual ("The radio hissed into life. \"Three!\"")
      story.choose(0)
      val text1 = story.next()
      text1.size shouldEqual (2)
      text1[1] shouldEqual ("The radio hissed into life. \"Two!\"")
      story.choose(0)
      val text2 = story.next()
      text2.size shouldEqual (3)
      text2[2] shouldEqual ("The radio hissed into life. \"One!\"")
      story.choose(0)
      val text3 = story.next()
      text3.size shouldEqual (4)
      text3[3] shouldEqual ("The radio hissed into life.")
      story.choose(0)
      val text4 = story.next()
      text4.size shouldEqual (5)
      text4[4] shouldEqual ("The radio hissed into life.")
    }
  }

  val shuffle =
      """=== test
        |The radio hissed into life. {~"Three!"|"Two!"|"One!"}
        |+ [Again] -> test
      """.trimMargin()

  it("cycle through the element repeatedly") {
    val inputStream = IOUtils.toInputStream(shuffle, "UTF-8")
    val story = InkParser.parse(inputStream, TestWrapper(), "Test")
    val text0 = story.next()
    text0.size shouldEqual (1)
    val res0 = text0[0] == "The radio hissed into life. \"Three!\"" || text0[0] == "The radio hissed into life. \"Two!\"" || text0[0] == "The radio hissed into life. \"One!\""
    res0 shouldEqual true
    story.choose(0)
    val text1 = story.next()
    text1.size shouldEqual (2)
    val res1 = text1[1] == "The radio hissed into life. \"Three!\"" || text1[1] == "The radio hissed into life. \"Two!\"" || text1[1] == "The radio hissed into life. \"One!\""
    res1 shouldEqual true
    story.choose(0)
    val text2 = story.next()
    text2.size shouldEqual (3)
    val res2 = text2[2] == "The radio hissed into life. \"Three!\"" || text2[2] == "The radio hissed into life. \"Two!\"" || text2[2] == "The radio hissed into life. \"One!\""
    res2 shouldEqual true
    story.choose(0)
    val text3 = story.next()
    text3.size shouldEqual (4)
    val res3 = text3[3] == "The radio hissed into life. \"Three!\"" || text3[3] == "The radio hissed into life. \"Two!\"" || text3[3] == "The radio hissed into life. \"One!\""
    res3 shouldEqual true
  }

  val emptyElements =
      """=== test
        |The radio hissed into life. {||"One!"}
        |+ [Again] -> test
      """.trimMargin()

  it("allow for empty text elements in the list") {
    val inputStream = IOUtils.toInputStream(emptyElements, "UTF-8")
    val story = InkParser.parse(inputStream, TestWrapper(), "Test")
    val text0 = story.next()
    text0.size shouldEqual (1)
    text0[0] shouldEqual ("The radio hissed into life.")
    story.choose(0)
    val text1 = story.next()
    text1.size shouldEqual (2)
    text1[1] shouldEqual ("The radio hissed into life.")
    story.choose(0)
    val text2 = story.next()
    text2.size shouldEqual (3)
    text2[2] shouldEqual ("The radio hissed into life. \"One!\"")
  }

  val listInChoice =
      """=== test
        |He looked at me oddly.
        |+ ["Hello, {&Master|Monsieur|you}!"] -> test
      """.trimMargin()

  it("be usable in a choice test") {
    val inputStream = IOUtils.toInputStream(listInChoice, "UTF-8")
    val story = InkParser.parse(inputStream, TestWrapper(), "Test")
    story.next()
    story.choiceText(0) shouldEqual ("\"Hello, Master!\"")
    story.choose(0)
    story.next()
    story.choiceText(0) shouldEqual ("\"Hello, Monsieur!\"")
    story.choose(0)
    story.next()
    story.choiceText(0) shouldEqual ("\"Hello, you!\"")
  }

  /*
val nestedList =
  """=== test
    |The radio hissed into life. {||"One!"}
    |+ [Again] -> test
  """.trimMargin()
*/
  // TODO: add test


  val one =
      """=== test
        |VAR x = 1
        |We needed to find {? x : nothing|one apple|two pears|many oranges}.
        |-> END
      """.trimMargin()

  it("return the text string in the sequence if the condition is a valid value") {
    val inputStream = IOUtils.toInputStream(one, "UTF-8")
    val story = InkParser.parse(inputStream, TestWrapper(), "Test")
    val text0 = story.next()
    text0.size shouldEqual (1)
    text0[0] shouldEqual ("We needed to find one apple.")
  }

  val minusOne =
      """=== test
        |VAR x = -1
        |We needed to find {? x : nothing|one apple|two pears|many oranges}.
        |-> END
      """.trimMargin()

  it("return the text string in the sequence if the condition is a valid value") {
    val inputStream = IOUtils.toInputStream(minusOne, "UTF-8")
    val story = InkParser.parse(inputStream, TestWrapper(), "Test")
    val text0 = story.next()
    text0.size shouldEqual (1)
    text0[0] shouldEqual ("We needed to find nothing.")
  }

  val ten =
      """=== test
        |VAR x = 10
        |We needed to find {? x : nothing|one apple|two pears|many oranges}.
        |-> END
      """.trimMargin()

  it("return the text string in the sequence if the condition is a valid value") {
    val inputStream = IOUtils.toInputStream(ten, "UTF-8")
    val story = InkParser.parse(inputStream, TestWrapper(), "Test")
    val text0 = story.next()
    text0.size shouldEqual (1)
    text0[0] shouldEqual ("We needed to find many oranges.")
  }
})