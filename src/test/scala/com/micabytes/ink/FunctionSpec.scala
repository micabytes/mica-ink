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

    val funcNone =
      """VAR x = f()
        |The value of x is {x}.
        |-> END
        |
        |=== function f() ===
        |    ~ return 3.8
      """.stripMargin

    "- return a value from a function with no parameters" in {
      val inputStream = IOUtils.toInputStream(funcNone, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value of x is 3.8.")
    }

    //TODO: Should tests that all different kinds of data can be passed as parameters: booleans, numbers, strings, and game objects.

    val funcInline =
      """The value of x is {lerp(2, 8, 0.3)}.
        |-> END
        |
        |=== function lerp(a, b, k) ===
        |    ~ return ((b - a) * k) + a
      """.stripMargin

    "- handle conditionals in the function" in {
      val inputStream = IOUtils.toInputStream(funcInline, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value of x is 3.8.")
    }

    val setVarFunc =
      """~ herp(2, 3)
        |The value is {x}.
        |-> END
        |
        |=== function herp(a, b) ===
        |VAR x = a * b
      """.stripMargin

    "- be able to set a variable as a command" in {
      val inputStream = IOUtils.toInputStream(setVarFunc, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value is 6.")
    }

    val complexFunc1 =
      """~ derp(2, 3, 4)
        |The values are {x} and {y}.
        |-> END
        |
        |=== function derp(a, b, c) ===
        |VAR x = a + b
        |VAR y = 3
        |{ x == 5:
        |   ~ x = 6
        |}
        |~ y = x + c
      """.stripMargin

    "- handle conditionals and setting of variables (test 1)" in {
      val inputStream = IOUtils.toInputStream(complexFunc1, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The values are 6 and 10.")
    }

    val complexFunc2 =
      """~ derp(2, 3)
        |The values are {x} and {y} and {z}.
        |-> END
        |
        |=== function derp(a, b) ===
        |VAR x = a - b
        |VAR y = 3
        |{
        |  - x == 0:
        |    ~ y = 0
        |  - x > 0:
        |    ~ y = x - 1
        |  - else:
        |    ~ y = x + 1
        |}
        |VAR z = 1
      """.stripMargin

    "- handle conditionals and setting of variables (test 2)" in {
      val inputStream = IOUtils.toInputStream(complexFunc2, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The values are -1 and 0 and 1.")
    }

    val complexFunc3 =
      """=== merchant
        |~ merchant_init()
        | "I will pay you {fee} reales if you get the goods to their destination. The goods will take up {weight} cargo spaces."
        |
        |
        |=== function merchant_init()
        |VAR weight = 20
        |VAR roll = 0
        |VAR mult = 1
        |{ roll == 0:
        |   ~ mult = 2
        |}
        |{ mult == 2:
        |   ~ roll = 1
        |}
        |{ roll == 0:
        |   ~ mult = 3
        |}
        |VAR dst = 5
        |VAR deadline = (dst * (100)) / 100
        |VAR fee = (1 + dst) * 10 * mult
      """.stripMargin

    "- handle conditionals and setting of variables (test 3)" in {
      val inputStream = IOUtils.toInputStream(complexFunc3, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("\"I will pay you 120 reales if you get the goods to their destination. The goods will take up 20 cargo spaces.\"")
    }

  }

}