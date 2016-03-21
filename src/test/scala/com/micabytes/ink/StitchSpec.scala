package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class StitchSpec extends Specification {

  "Stitches" should {
    val autoStitch =
      """=== the_orient_express ===
        |
        |= in_first_class
        |    I settled my master.
        |    *   [Move to third class]
        |        -> in_third_class
        |
        |= in_third_class
        |    I put myself in third.
      """.stripMargin
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
      """.stripMargin

    "- be automatically diverted to if there is no content in a knot" in {
      val inputStream = IOUtils.toInputStream(autoStitch, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.allLines()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("I settled my master.")
    }

    "- not be diverted to if the knot has content" in {
      val inputStream = IOUtils.toInputStream(manualStitch, "UTF-8")
      val story = InkParser.parse(inputStream)
      val knotText = story.allLines()
      knotText.size() must beEqualTo(1)
      knotText.get(0) must beEqualTo("How shall we travel?")
      story.choose(1)
      val stitchText = story.allLines()
      stitchText.size() must beEqualTo(1)
      stitchText.get(0) must beEqualTo("I put myself in third.")
    }

    "- be usable locally without the full name" in {
      val inputStream = IOUtils.toInputStream(manualStitch, "UTF-8")
      val story = InkParser.parse(inputStream)
      val knotText = story.allLines()
      knotText.size() must beEqualTo(1)
      knotText.get(0) must beEqualTo("How shall we travel?")
      story.choose(0)
      val stitchText = story.allLines()
      stitchText.size() must beEqualTo(1)
      stitchText.get(0) must beEqualTo("I settled my master.")
    }

  }


}
