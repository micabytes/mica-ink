package com.micabytes.ink

import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class ExternSpec : WordSpec() {

  init {

    "Methods on external objects" should {

      val helloWorld =
          """=== test_knot
          |{x.hello()}
          |-> END
        """.trimMargin()

      "- be possible to call on an object without any parameters" {
        val inputStream = IOUtils.toInputStream(helloWorld, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.putVariable("x", TestClass())
        val text = story.next()
        text.size  shouldBe (1)
        text.get(0) shouldBe ("Hello, is it me you're looking for?")
      }

      val helloNoBrace =
          """=== test_knot
          |{x.hello()}
          |-> END
        """.trimMargin()

      "- be possible to call on an object without any parameters and no function brace" {
        val inputStream = IOUtils.toInputStream(helloNoBrace, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.putVariable("x", TestClass())
        val text = story.next()
        text.size  shouldBe (1)
        text.get(0) shouldBe ("Hello, is it me you're looking for?")
      }

      val mambo =
          """=== test_knot
          | VAR y = 5
          |{x.number(y)}
          |-> END
        """.trimMargin()

      "- be possible to call on an object with a parameter defined" {
        val inputStream = IOUtils.toInputStream(mambo, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.putVariable("x", TestClass())
        val text = story.next()
        text.size  shouldBe (1)
        text.get(0) shouldBe ("Mambo Number 5")
      }

      val externChoices =
          """=== choice_test ===
          |Test conditional choices
          |+ {x.wrong()} not displayed
          |+ shown
          """.trimMargin()

      "- resolve external bools correctly in conditional choices" {
        val inputStream = IOUtils.toInputStream(externChoices, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.putVariable("x", TestClass())
        story.next()
        story.choiceSize shouldBe (1)
      }


    }

  }

}