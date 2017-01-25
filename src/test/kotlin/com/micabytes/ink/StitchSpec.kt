package com.micabytes.ink

import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class StitchSpec : WordSpec() {

  init {

    "Stitches" should {
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

      "- be automatically started with if there is no content in a knot" {
        val inputStream = IOUtils.toInputStream(autoStitch, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("I settled my master.")
      }

      "- be automatically diverted to if there is no other content in a knot" {
        val inputStream = IOUtils.toInputStream(autoStitch, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(1)
        val text = story.next()
        text.size shouldBe (2)
        text.get(1) shouldBe ("I settled my master.")
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

      "- not be diverted to if the knot has content" {
        val inputStream = IOUtils.toInputStream(manualStitch, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val knotText = story.next()
        knotText.size shouldBe (1)
        knotText.get(0) shouldBe ("How shall we travel?")
        story.choose(1)
        val stitchText = story.next()
        stitchText.size shouldBe (2)
        stitchText.get(1) shouldBe ("I put myself in third.")
      }

      "- be usable locally without the full name" {
        val inputStream = IOUtils.toInputStream(manualStitch, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val knotText = story.next()
        knotText.size shouldBe (1)
        knotText.get(0) shouldBe ("How shall we travel?")
        story.choose(0)
        val stitchText = story.next()
        stitchText.size shouldBe (2)
        stitchText.get(1) shouldBe ("I settled my master.")
      }

    }

  }

}