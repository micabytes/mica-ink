package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FunctionSpec extends Specification {

  "Functions" should {

    val funcBasic =
      """VAR x = lerp(2, 8, 0.3)
        |The value of x is {x}.
        |-> END
        |
        |=== function lerp(a, b, k) ===
        |    ~ return ((b - a) * k) + a
      """.stripMargin

    "- return a value from a function in a variable expression" in {
      val inputStream = IOUtils.toInputStream(funcBasic, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value of x is 3.8.")
    }

    val funcInline =
      """The value of x is {lerp(2, 8, 0.3)}.
        |-> END
        |
        |=== function lerp(a, b, k) ===
        |    ~ return ((b - a) * k) + a
      """.stripMargin

    "- return a value from a function in a call from a string" in {
      val inputStream = IOUtils.toInputStream(funcInline, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value of x is 3.8.")
    }

  }

}