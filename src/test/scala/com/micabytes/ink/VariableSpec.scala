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

  }


}
