package com.micabytes.ink

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class DeclarationSpec : WordSpec() {

  init {

    "Declarations" should {

      val variableDeclaration =
          """=== test_knot ===
          |VAR friendly_name_of_player = "Jackie"
          |VAR age = 23
          |
          |"My name is Jean Passepartout, but my friend's call me {friendly_name_of_player}. I'm {age} years old."
        """.trimMargin()

      "be declared with a VAR statement and print out a text value when used in content" {
        val inputStream = IOUtils.toInputStream(variableDeclaration, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("\"My name is Jean Passepartout, but my friend's call me Jackie. I'm 23 years old.\"")
      }

      val varCalc =
          """=== set_some_variables ===
          | VAR knows = false
          | VAR x = 2
          | VAR y = 3
          | VAR c = 4
          | ~ knows = true
          | ~ x = (x * x) - (y * y) + c
          | ~ y = 2 * x * y
          |
          |    The values are {knows} and {x} and {y}.
        """.trimMargin()

      "be declared with a VAR statement and print out a text value when used in content" {
        val inputStream = IOUtils.toInputStream(varCalc, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The values are 1 and -1 and -6.")
      }

      val varDivert =
          """=== test_knot ===
          |VAR current_epilogue = -> everybody_dies
          |Divert as variable example
          |-> continue_or_quit
          |
          |=== continue_or_quit
          |Give up now, or keep trying to save your Kingdom?
          |*  [Keep trying!]   -> continue_or_quit
          |*  [Give up]        -> current_epilogue
          |
          |=== everybody_dies
          |Everybody dies.
          |-> END
        """.trimMargin()

      "be declarable as diverts and be usable in text" {
        val inputStream = IOUtils.toInputStream(varDivert, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(1)
        val text = story.next()
        text.size shouldBe (3)
        text.get(2) shouldBe ("Everybody dies.")
      }

    }

  }
}