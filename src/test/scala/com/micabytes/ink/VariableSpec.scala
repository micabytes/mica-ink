package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VariableSpec extends Specification {

  "Variables" should {
    val variableDeclaration =
      """VAR friendly_name_of_player = "Jackie"
        |VAR age = 23
        |
        |"My name is Jean Passepartout, but my friend's call me {friendly_name_of_player}. I'm {age} years old."
      """.stripMargin

    "- be declared with a VAR statement and print out a text value when used in content" in {
      val inputStream = IOUtils.toInputStream(variableDeclaration, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextChoice()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("\"My name is Jean Passepartout, but my friend's call me Jackie. I'm 23 years old.\"")
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
      """.stripMargin

    "- be declared with a VAR statement and print out a text value when used in content" in {
      val inputStream = IOUtils.toInputStream(varCalc, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextChoice()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The values are true and -1 and -6.")
    }

    val varDivert =
      """VAR current_epilogue = -> everybody_dies
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
      """.stripMargin

    "- be declarable as diverts and be usable in text" in {
      val inputStream = IOUtils.toInputStream(varDivert, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.nextChoice()
      story.choose(1)
      val text = story.nextChoice()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Everybody dies.")
    }

  }


}
