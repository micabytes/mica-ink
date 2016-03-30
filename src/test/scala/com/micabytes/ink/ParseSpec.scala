package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ParseSpec extends Specification {

  "Story parsing" should {

    val knotNaming =
      """=== knotA ===
        |This is a knot.
        |
        |=== knotB ===
        |This is another knot.
        |
        |=== knotA ===
        |This is an illegally named knot.
      """.stripMargin

    "- return an error when two knots/stitches share the same name" in {
      val inputStream = IOUtils.toInputStream(knotNaming, "UTF-8")
      InkParser.parse(inputStream) must throwA[InkParseException]
    }

  }

}
