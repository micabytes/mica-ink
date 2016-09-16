package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class DivertSpec extends Specification {

  "Diverts" should {
    val simpleDivert =
      """=== back_in_london ===
        |We arrived into London at 9.45pm exactly.
        |-> hurry_home
        |
        |=== hurry_home ===
        |We hurried home to Savile Row as fast as we could.
      """.stripMargin
    val invisibleDivert =
      """=== hurry_home ===
        |We hurried home to Savile Row -> as_fast_as_we_could
        |
        |=== as_fast_as_we_could ===
        |as fast as we could.
      """.stripMargin
    val divertOnChoice =
      """== paragraph_1 ===
        |You stand by the wall of Analand, sword in hand.
        |* [Open the gate] -> paragraph_2
        |
        |=== paragraph_2 ===
        |You open the gate, and step out onto the path.
      """.stripMargin
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
      """.stripMargin

    "- divert text from one knot/stitch to another" in {
      val inputStream = IOUtils.toInputStream(simpleDivert, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      val text = story.nextAll()
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("We arrived into London at 9.45pm exactly.")
      text.get(1) must beEqualTo("We hurried home to Savile Row as fast as we could.")
    }

    "- divert from one line of text to new content invisibly" in {
      val inputStream = IOUtils.toInputStream(invisibleDivert, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("We hurried home to Savile Row as fast as we could.")
    }

    "- branch directly from choices" in {
      val inputStream = IOUtils.toInputStream(divertOnChoice, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(0)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("You open the gate, and step out onto the path.")
    }

    "- be usable to branch and join text seamlessly (example 1)" in {
      // First path through text
      val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(0)
      val text = story.nextAll()
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("\"There is not a moment to lose!\" I declared.")
      text.get(1) must beEqualTo("We hurried home to Savile Row as fast as we could.")
    }

    "- be usable to branch and join text seamlessly (example 2)" in {
      // Second path through text
      val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(1)
      val text = story.nextAll()
      text.size() must beEqualTo(3)
      text.get(0) must beEqualTo("\"Monsieur, let us savour this moment!\" I declared.")
      text.get(1) must beEqualTo("My master clouted me firmly around the head and dragged me out of the door.")
      text.get(2) must beEqualTo("He insisted that we hurried home to Savile Row as fast as we could.")
    }

    "- be usable to branch and join text seamlessly (example 3)" in {
      // Third path through text
      val inputStream = IOUtils.toInputStream(complexBranching, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll()
      story.choose(2)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("We hurried home to Savile Row as fast as we could.")
    }

  }


}
