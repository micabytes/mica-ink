package com.micabytes.ink

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class IncludeSpec : WordSpec() {

  init {

    "Includes" should {

      val include1 =
          """INCLUDE includeTest1
            |=== knotA ===
            |This is a knot. -> includeKnot
      """.trimMargin()

      "process an INCLUDE statement and add the content of the include file" {
        val inputStream = IOUtils.toInputStream(include1, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text[0] shouldBe ("This is a knot. This is an included knot.")
      }

    }
  }

}