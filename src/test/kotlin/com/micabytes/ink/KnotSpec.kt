package com.micabytes.ink

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class KnotSpec : WordSpec() {

  init {
    
    "A knot with a single line of plain text" should {
      val testData = """== hello_world
                       |Hello, World!""".trimMargin()

      "return a single line of plain text as output" {
        val inputStream = IOUtils.toInputStream(testData, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "test")
        val text = story.next()
        text.size shouldBe 1
        text[0] shouldEqual "Hello, World!"
      }
    }

    "A knot with multiple lines line of plain text" should {
      val testData = """== hello_world
                        |Hello, world!
                        |Hello?
                        |Hello, are you there?""".trimMargin()

      "return an equal number of lines as output" {
        val inputStream = IOUtils.toInputStream(testData, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "test")
        val text = story.next()
        text.size shouldBe 3
        text[0] shouldBe "Hello, world!"
        text[1] shouldBe "Hello?"
        text[2] shouldBe "Hello, are you there?"
      }
    }

    "A knot with multiple lines line of plain text" should {
      val testData = """== hello_world
                        |Hello, world!
                        |Hello?
                        |
                        |Hello, are you there?""".trimMargin()

      "return an equal number of lines as output" {
        val inputStream = IOUtils.toInputStream(testData, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "test")
        val text = story.next()
        text.size shouldBe 3
        text[0] shouldBe "Hello, world!"
        text[1] shouldBe "Hello?"
        text[2] shouldBe "Hello, are you there?"
      }
    }

  }
}