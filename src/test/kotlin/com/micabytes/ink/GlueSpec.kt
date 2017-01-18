package com.micabytes.ink

import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class GlueSpec : WordSpec() {


  init {

    "Glue" should {
      val simpleGlue =
          """=== test_knot ===
            |Some <>
            |content<>
            | with glue.
            |""".trimMargin()

      "bind text together across multiple lines of text" {
        val inputStream = IOUtils.toInputStream(simpleGlue, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("Some content with glue.")
      }

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
            |<> as fast as we could.""".trimMargin()


      "bind text together across multiple knots/stitches" {
        val inputStream = IOUtils.toInputStream(glueWithDivert, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("We hurried home to Savile Row as fast as we could.")
      }

    }

  }

}
