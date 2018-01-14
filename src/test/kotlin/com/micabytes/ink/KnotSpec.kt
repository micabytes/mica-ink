package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class KnotSpec : Spek({

  given("A knot with a single line of plain text") {
    val testData = """== hello_world
                       |Hello, World!""".trimMargin()

    it("return a single line of plain text as output") {
      val inputStream = IOUtils.toInputStream(testData, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "test")
      val text = story.next()
      text.size shouldEqual 1
      text[0] shouldEqual "Hello, World!"
    }
  }

  given("A knot with multiple lines line of plain text") {
    val testData = """== hello_world
                        |Hello, world!
                        |Hello?
                        |Hello, are you there?""".trimMargin()

    it("return an equal number of lines as output") {
      val inputStream = IOUtils.toInputStream(testData, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "test")
      val text = story.next()
      text.size shouldEqual 3
      text[0] shouldEqual "Hello, world!"
      text[1] shouldEqual "Hello?"
      text[2] shouldEqual "Hello, are you there?"
    }
  }

  given("A knot with multiple lines line of plain text") {
    val testData = """== hello_world
                        |Hello, world!
                        |Hello?
                        |
                        |Hello, are you there?""".trimMargin()

    it("return an equal number of lines as output") {
      val inputStream = IOUtils.toInputStream(testData, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "test")
      val text = story.next()
      text.size shouldEqual 3
      text[0] shouldEqual "Hello, world!"
      text[1] shouldEqual "Hello?"
      text[2] shouldEqual "Hello, are you there?"
    }
  }

})