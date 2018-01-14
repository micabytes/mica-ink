package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class StitchSpec : Spek({

  given("Stitches") {
    val autoStitch =
        """=== the_orient_express ===
        |
        |= in_first_class
        |    I settled my master.
        |    *  [Move to third class]
        |        -> in_third_class
        |    *  [Are you sure] -> the_orient_express
        |
        |= in_third_class
        |    I put myself in third.
      """.trimMargin()

    it("be automatically started with if there is no content in a knot") {
      val inputStream = IOUtils.toInputStream(autoStitch, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("I settled my master.")
    }

    it("be automatically diverted to if there is no other content in a knot") {
      val inputStream = IOUtils.toInputStream(autoStitch, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(1)
      val text = story.next()
      text.size shouldEqual (2)
      text[1] shouldEqual ("I settled my master.")
    }

    val manualStitch =
        """=== the_orient_express ===
        |How shall we travel?
        |* [In first class] -> in_first_class
        |* [I'll go cheap] -> the_orient_express.in_third_class
        |
        |= in_first_class
        |    I settled my master.
        |    *   [Move to third class]
        |        -> in_third_class
        |
        |= in_third_class
        |    I put myself in third.
      """.trimMargin()

    it("not be diverted to if the knot has content") {
      val inputStream = IOUtils.toInputStream(manualStitch, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val knotText = story.next()
      knotText.size shouldEqual (1)
      knotText[0] shouldEqual ("How shall we travel?")
      story.choose(1)
      val stitchText = story.next()
      stitchText.size shouldEqual (2)
      stitchText[1] shouldEqual ("I put myself in third.")
    }

    it("be usable locally without the full name") {
      val inputStream = IOUtils.toInputStream(manualStitch, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val knotText = story.next()
      knotText.size shouldEqual (1)
      knotText[0] shouldEqual ("How shall we travel?")
      story.choose(0)
      val stitchText = story.next()
      stitchText.size shouldEqual (2)
      stitchText[1] shouldEqual ("I settled my master.")
    }

  }

})