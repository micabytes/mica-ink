package com.micabytes.ink

import io.kotlintest.specs.WordSpec

class ParseSpec : WordSpec() {

  init {

    "Story parsing" should
        {

          val knotNaming =
              """=== knotA ===
        |This is a knot.
        |
        |=== knotB ===
        |This is another knot.
        |
        |=== knotA ===

        |This is an illegally named knot.
      """.trimMargin()

          "return an error when two knots/stitches share the same name" {
            // val inputStream = IOUtils.toInputStream(knotNaming, "UTF-8")
            // TODO: Throw exception on parse error (not implemented yet)
            // InkParser.parse(inputStream, TestWrapper(), "Test") must throwA[InkParseException]
          }

        }
  }

}