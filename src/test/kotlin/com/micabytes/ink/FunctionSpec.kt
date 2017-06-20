package com.micabytes.ink

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class FunctionSpec : WordSpec() {

  init {

    "Functions" should {

      val funcBasic =
          """=== test_knot
          |VAR x = lerp(2, 8, 0.3)
          |The value of x is {x}.
          |-> END
          |
          |=== function lerp(a, b, k) ===
          |    ~ return ((b - a) * k) + a
        """.trimMargin()

      "return a value from a function in a variable expression" {
        val inputStream = IOUtils.toInputStream(funcBasic, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value of x is 3.8.")
      }

      val funcNone =
          """=== test_knot
          |VAR x = f()
          |The value of x is {x}.
          |-> END
          |
          |=== function f() ===
          |    ~ return 3.8
        """.trimMargin()

      "return a value from a function with no parameters" {
        val inputStream = IOUtils.toInputStream(funcNone, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value of x is 3.8.")
      }

      //TODO: Should tests that all different kinds of data can be passed as parameters: booleans, numbers, strings, and game objects.

      val funcInline =
          """=== test_knot
          |The value of x is {lerp(2, 8, 0.3)}.
          |-> END
          |
          |=== function lerp(a, b, k) ===
          |    ~ return ((b - a) * k) + a
        """.trimMargin()

      "handle conditionals in the function" {
        val inputStream = IOUtils.toInputStream(funcInline, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value of x is 3.8.")
      }

      val setVarFunc =
          """=== test_knot
          |~ herp(2, 3)
          |The value is {x}.
          |-> END
          |
          |=== function herp(a, b) ===
          |VAR x = a * b
        """.trimMargin()

      "be able to set a variable as a command" {
        val inputStream = IOUtils.toInputStream(setVarFunc, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value is 6.")
      }

      val complexFunc1 =
          """=== test_knot
          |~ derp(2, 3, 4)
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
        """.trimMargin()

      "handle conditionals and setting of values (test 1)" {
        val inputStream = IOUtils.toInputStream(complexFunc1, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The values are 6 and 10.")
      }

      val complexFunc2 =
          """=== test_knot
          |~ derp(2, 3)
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
        """.trimMargin()

      "handle conditionals and setting of values (test 2)" {
        val inputStream = IOUtils.toInputStream(complexFunc2, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The values are -1 and 0 and 1.")
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
          |{ mult == 2:
          |   ~ roll = 1
          |}
          |{ roll == 0:
          |   ~ mult = 3
          |}
          |VAR dst = 5
          |VAR deadline = (dst * (100)) / 100
          |VAR fee = (1 + dst) * 10 * mult
        """.trimMargin()

      "handle conditionals and setting of values (test 3)" {
        val inputStream = IOUtils.toInputStream(complexFunc3, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("\"I will pay you 120 reales if you get the goods to their destination. The goods will take up 20 cargo spaces.\"")
      }

    }

  }
}