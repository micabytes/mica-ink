package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class DivertSpec : Spek({

  given("Diverts") {

    val simpleDivert =
        """=== back_in_london ===
        |We arrived into London at 9.45pm exactly.
        |-> hurry_home
        |
        |=== hurry_home ===
        |We hurried home to Savile Row as fast as we could.
      """.trimMargin()

    it("divert text from one knot/stitch to another") {
      val inputStream = IOUtils.toInputStream(simpleDivert, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text = story.next()
      text.size shouldEqual (2)
      text[0] shouldEqual ("We arrived into London at 9.45pm exactly.")
      text[1] shouldEqual ("We hurried home to Savile Row as fast as we could.")
    }

    val invisibleDivert =
        """=== hurry_home ===
        |We hurried home to Savile Row -> as_fast_as_we_could
        |
        |=== as_fast_as_we_could ===
        |as fast as we could.
      """.trimMargin()

    it("divert from one line of text to new content invisibly") {
      val inputStream = IOUtils.toInputStream(invisibleDivert, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("We hurried home to Savile Row as fast as we could.")
    }

    val divertOnChoice =
        """== paragraph_1 ===
        |You stand by the wall of Analand, sword in hand.
        |* [Open the gate] -> paragraph_2
        |
        |=== paragraph_2 ===
        |You open the gate, and step out onto the path.
      """.trimMargin()

    it("branch directly from choices") {
      val inputStream = IOUtils.toInputStream(divertOnChoice, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      val text = story.next()
      text.size shouldEqual (2)
      text[1] shouldEqual ("You open the gate, and step out onto the path.")
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

    it("be usable to branch and join text seamlessly (example 1)") {
      // First path through text
      val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      val text = story.next()
      text.size shouldEqual (3)
      text[1] shouldEqual ("\"There is not a moment to lose!\" I declared.")
      text[2] shouldEqual ("We hurried home to Savile Row as fast as we could.")
    }

    it("be usable to branch and join text seamlessly (example 2)") {
      // Second path through text
      val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(1)
      val text = story.next()
      text.size shouldEqual (4)
      text[1] shouldEqual ("\"Monsieur, let us savour this moment!\" I declared.")
      text[2] shouldEqual ("My master clouted me firmly around the head and dragged me out of the door.")
      text[3] shouldEqual ("He insisted that we hurried home to Savile Row as fast as we could.")
    }

    it("be usable to branch and join text seamlessly (example 3)") {
      // Third path through text
      val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(2)
      val text = story.next()
      text.size shouldEqual (2)
      text[1] shouldEqual ("We hurried home to Savile Row as fast as we could.")
    }

  }
})