package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class GlueSpec extends Specification {

  "Glue" should {
    val simpleGlue =
      """Some <>
        |content<>
        | with glue.
        |
      """.stripMargin
    val glueWithDivert =
      """=== hurry_home ===
        |We hurried home <>
        |-> to_savile_row
        |
        |=== to_savile_row ===
        |to Savile Row
        |-> as_fast_as_we_could
        |
        |=== as_fast_as_we_could ===
        |<> as fast as we could.
      """.stripMargin

    "- bind text together across multiple lines of text" in {
      val inputStream = IOUtils.toInputStream(simpleGlue, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextChoice()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Some content with glue.")
    }

    "- bind text together across multiple knots/stitches" in {
      val inputStream = IOUtils.toInputStream(glueWithDivert, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextChoice()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("We hurried home to Savile Row as fast as we could.")
    }



  }


}
