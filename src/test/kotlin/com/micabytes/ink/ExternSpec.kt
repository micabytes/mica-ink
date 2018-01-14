package com.micabytes.ink

import com.micabytes.ink.helpers.TestClassJava
import com.micabytes.ink.helpers.TestClassKotlin
import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class ExternSpec : Spek({

  given("a Kotlin class with public functions") {

    val helloWorld =
        """=== test_knot
          |{x.hello()}
          |-> END
        """.trimMargin()

    it("should be possible to call on an object without any parameters") {
      val inputStream = IOUtils.toInputStream(helloWorld, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassKotlin())
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("Hello, is it me you're looking for?")
    }

    val helloNoBrace =
        """=== test_knot
          |{x.hello()}
          |-> END
        """.trimMargin()

    it("should be possible to call on an object without any parameters and no function brace") {
      val inputStream = IOUtils.toInputStream(helloNoBrace, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassKotlin())
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("Hello, is it me you're looking for?")
    }

    val mambo =
        """=== test_knot
          | VAR y = 5
          |{x.number(y)}
          |-> END
        """.trimMargin()

    it("should be possible to call on an object with a parameter defined") {
      val inputStream = IOUtils.toInputStream(mambo, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassKotlin())
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("Mambo Number 5")
    }

    val externChoices =
        """=== choice_test ===
          |Test conditional choices
          |+ {x.wrong()} not displayed
          |+ shown
          """.trimMargin()

    it("resolve external bools correctly in conditional choices") {
      val inputStream = IOUtils.toInputStream(externChoices, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassKotlin())
      story.next()
      story.choiceSize shouldEqual (1)
    }

  }

  given("a Java class with public methods") {

    val helloWorld =
        """=== test_knot
          |{x.hello()}
          |-> END
        """.trimMargin()

    it("should be possible to call on an object without any parameters") {
      val inputStream = IOUtils.toInputStream(helloWorld, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassJava())
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("Hello, is it me you're looking for?")
    }

    val helloNoBrace =
        """=== test_knot
          |{x.hello()}
          |-> END
        """.trimMargin()

    it("should be possible to call on an object without any parameters and no function brace") {
      val inputStream = IOUtils.toInputStream(helloNoBrace, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassJava())
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("Hello, is it me you're looking for?")
    }

    val mambo =
        """=== test_knot
          | VAR y = 5
          |{x.number(y)}
          |-> END
        """.trimMargin()

    it("should be possible to call on an object with a parameter defined") {
      val inputStream = IOUtils.toInputStream(mambo, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassJava())
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("Mambo Number 5")
    }

    val externChoices =
        """=== choice_test ===
          |Test conditional choices
          |+ {x.wrong()} not displayed
          |+ shown
          """.trimMargin()

    it("resolve external bools correctly in conditional choices") {
      val inputStream = IOUtils.toInputStream(externChoices, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.putVariable("x", TestClassJava())
      story.next()
      story.choiceSize shouldEqual (1)
    }

  }

})
