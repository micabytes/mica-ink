package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class HelloWorldSpec extends Specification {

  "A single line of plain text in an ink file" should {
    val testData = "Hello, world!"
    val inputStream = IOUtils.toInputStream(testData, "UTF-8")
    val story = InkParser.parse(inputStream)

    "- return a single line of text as output" in {
      val text = story.allLines()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Hello, world!")
    }

  }

  "Multiple lines of plain text in an ink file" should {

    "- return an equal number of lines as output" in {
      val testData =
        """Hello, world!
          |Hello?
          |Hello, are you there?""".stripMargin
      val inputStream = IOUtils.toInputStream(testData, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.allLines()
      text.size() must beEqualTo(3)
      text.get(0) must beEqualTo("Hello, world!")
      text.get(1) must beEqualTo("Hello?")
      text.get(2) must beEqualTo("Hello, are you there?")
    }

    "- strip empty lines of output" in {
      val testData =
        """Hello, world!
          |
          |Hello?
          |Hello, are you there?
          """.stripMargin
      val inputStream = IOUtils.toInputStream(testData, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.allLines()
      text.size() must beEqualTo(3)
      text.get(0) must beEqualTo("Hello, world!")
      text.get(1) must beEqualTo("Hello?")
      text.get(2) must beEqualTo("Hello, are you there?")
    }

  }

}
