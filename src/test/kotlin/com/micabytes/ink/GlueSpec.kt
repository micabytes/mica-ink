package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class GlueSpec : Spek({

  given("Glue") {
    val simpleGlue =
        """=== test_knot ===
            |Some <>
            |content<>
            | with glue.
            |""".trimMargin()

    it("bind text together across multiple lines of text") {
      val inputStream = IOUtils.toInputStream(simpleGlue, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("Some content with glue.")
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


    it("bind text together across multiple knots/stitches") {
      val inputStream = IOUtils.toInputStream(glueWithDivert, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("We hurried home to Savile Row as fast as we could.")
    }

  }

})
