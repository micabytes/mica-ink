package com.micabytes.ink

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class DivertSpec : WordSpec() {

  init {

    "Diverts" should {

      val simpleDivert =
          """=== back_in_london ===
        |We arrived into London at 9.45pm exactly.
        |-> hurry_home
        |
        |=== hurry_home ===
        |We hurried home to Savile Row as fast as we could.
      """.trimMargin()

      "divert text from one knot/stitch to another" {
        val inputStream = IOUtils.toInputStream(simpleDivert, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe(2)
        text[0] shouldBe("We arrived into London at 9.45pm exactly.")
        text[1] shouldBe("We hurried home to Savile Row as fast as we could.")
      }

      val invisibleDivert =
          """=== hurry_home ===
        |We hurried home to Savile Row -> as_fast_as_we_could
        |
        |=== as_fast_as_we_could ===
        |as fast as we could.
      """.trimMargin()

      "divert from one line of text to new content invisibly" {
        val inputStream = IOUtils.toInputStream(invisibleDivert, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe(1)
        text[0] shouldBe("We hurried home to Savile Row as fast as we could.")
      }

      val divertOnChoice =
          """== paragraph_1 ===
        |You stand by the wall of Analand, sword in hand.
        |* [Open the gate] -> paragraph_2
        |
        |=== paragraph_2 ===
        |You open the gate, and step out onto the path.
      """.trimMargin()

      "branch directly from choices" {
        val inputStream = IOUtils.toInputStream(divertOnChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(0)
        val text = story.next()
        text.size shouldBe(2)
        text[1] shouldBe("You open the gate, and step out onto the path.")
      }

      val complexBranching =
          """=== back_in_london ===
        |
        |We arrived into London at 9.45pm exactly.
        |
        |*   "There is not a moment to lose!"[] I declared.
        |    -> hurry_outside
        |
        |*   "Monsieur, let us savour this moment!"[] I declared.
        |    My master clouted me firmly around the head and dragged me out of the door.
        |    -> dragged_outside
        |
        |*   [We hurried home] -> hurry_outside
        |
        |
        |=== hurry_outside ===
        |We hurried home to Savile Row -> as_fast_as_we_could
        |
        |
        |=== dragged_outside ===
        |He insisted that we hurried home to Savile Row
        |-> as_fast_as_we_could
        |
        |
        |=== as_fast_as_we_could ===
        |<> as fast as we could.
      """.trimMargin()

      "be usable to branch and join text seamlessly (example 1)" {
        // First path through text
        val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(0)
        val text = story.next()
        text.size shouldBe(3)
        text[1] shouldBe("\"There is not a moment to lose!\" I declared.")
        text[2] shouldBe("We hurried home to Savile Row as fast as we could.")
      }

      "be usable to branch and join text seamlessly (example 2)" {
        // Second path through text
        val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(1)
        val text = story.next()
        text.size shouldBe(4)
        text[1] shouldBe("\"Monsieur, let us savour this moment!\" I declared.")
        text[2] shouldBe("My master clouted me firmly around the head and dragged me out of the door.")
        text[3] shouldBe("He insisted that we hurried home to Savile Row as fast as we could.")
      }

      "be usable to branch and join text seamlessly (example 3)" {
        // Third path through text
        val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(2)
        val text = story.next()
        text.size shouldBe(2)
        text[1] shouldBe("We hurried home to Savile Row as fast as we could.")
      }

    }
  }
}