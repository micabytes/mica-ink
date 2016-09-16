package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GatherSpec extends Specification {

  "Gathers" should {
    val gatherBasic =
      """What's that?" my master asked.
        |    *  "I am somewhat tired[."]," I repeated.
        |       "Really," he responded. "How deleterious."
        |    *  "Nothing, Monsieur!"[] I replied.
        |       "Very good, then."
        |    *  "I said, this journey is appalling[."] and I want no more of it."
        |       "Ah," he replied, not unkindly. "I see you are feeling frustrated. Tomorrow, things will improve."
        |- With that Monsieur Fogg left the room.
      """.stripMargin
    val nestedFlow =
      """Well, Poirot? Murder or suicide?"
        |    *   "Murder!"
        |        "And who did it?"
        |        * *     "Detective-Inspector Japp!"
        |        * *     "Captain Hastings!"
        |        * *     "Myself!"
        |    *   "Suicide!"
        |    -   Mrs. Christie lowered her manuscript a moment. The rest of the writing group sat, open-mouthed.
      """.stripMargin
    val nestedGather =
      """Well, Poirot? Murder or suicide?"
        |        *   "Murder!"
        |            "And who did it?"
        |            * *     "Detective-Inspector Japp!"
        |            * *     "Captain Hastings!"
        |            * *     "Myself!"
        |            - -     "You must be joking!"
        |            * *     "Mon ami, I am deadly serious."
        |            * *     "If only..."
        |        *   "Suicide!"
        |            "Really, Poirot? Are you quite sure?"
        |            * *     "Quite sure."
        |            * *     "It is perfectly obvious."
        |        -   Mrs. Christie lowered her manuscript a moment. The rest of the writing group sat, open-mouthed.
      """.stripMargin

    "- gather the flow back together again" in {
      val inputStream = IOUtils.toInputStream(gatherBasic, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll
      story.choose(1)
      val text = story.nextAll
      text.size() must beEqualTo(3)
      text.get(0) must beEqualTo("\"Nothing, Monsieur!\" I replied.")
      text.get(1) must beEqualTo("\"Very good, then.\"")
      text.get(2) must beEqualTo("With that Monsieur Fogg left the room.")
    }

    val gatherChain =
      """=== escape ===
        |I ran through the forest, the dogs snapping at my heels.
        |    *   I checked the jewels[] were still in my pocket, and the feel of them brought a spring to my step. <>
        |    *  I did not pause for breath[] but kept on running. <>
        |    *   I cheered with joy. <>
        |-   The road could not be much further! Mackie would have the engine running, and then I'd be safe.
        |    *   I reached the road and looked about[]. And would you believe it?
        |    *   I should interrupt to say Mackie is normally very reliable[]. He's never once let me down. Or rather, never once, previously to that night.
        |-   The road was empty. Mackie was nowhere to be seen.
      """.stripMargin

    "- form chains of content with multiple gathers" in {
      val inputStream = IOUtils.toInputStream(gatherChain, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll
      story.getChoiceSize must beEqualTo(3)
      story.choose(1)
      val text0 = story.nextAll
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("I did not pause for breath but kept on running. The road could not be much further! Mackie would have the engine running, and then I'd be safe.")
      story.getChoiceSize must beEqualTo(2)
      story.choose(0)
      val text1 = story.nextAll
      text1.size() must beEqualTo(2)
      text1.get(0) must beEqualTo("I reached the road and looked about. And would you believe it?")
      text1.get(1) must beEqualTo("The road was empty. Mackie was nowhere to be seen.")
    }

    "- allow nested options to pop out to a higher level gather" in {
      val inputStream = IOUtils.toInputStream(nestedFlow, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll
      story.choose(0)
      story.nextAll
      story.choose(2)
      val text = story.nextAll
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("\"Myself!\"")
      text.get(1) must beEqualTo("Mrs. Christie lowered her manuscript a moment. The rest of the writing group sat, open-mouthed.")
    }

    "- allow nested gathers within the flow" in {
      val inputStream = IOUtils.toInputStream(nestedGather, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll
      story.choose(0)
      story.nextAll
      story.choose(2)
      val text0 = story.nextAll
      text0.size() must beEqualTo(2)
      text0.get(0) must beEqualTo("\"Myself!\"")
      text0.get(1) must beEqualTo("\"You must be joking!\"")
      story.choose(0)
      val text1 = story.nextAll
      text1.size() must beEqualTo(2)
      text1.get(0) must beEqualTo("\"Mon ami, I am deadly serious.\"")
      text1.get(1) must beEqualTo("Mrs. Christie lowered her manuscript a moment. The rest of the writing group sat, open-mouthed.")
    }

    val deepNesting =
      """Tell us a tale, Captain!"
        |    *   "Very well, you sea-dogs. Here's a tale..."
        |        * *     "It was a dark and stormy night..."
        |                * * *   "...and the crew were restless..."
        |                        * * * *  "... and they said to their Captain..."
        |                                * * * * *       "...Tell us a tale Captain!"
        |    *   "No, it's past your bed-time."
        |-   To a man, the crew began to yawn.
      """.stripMargin

    "- gather the flow back together again from arbitrarily deep options" in {
      val inputStream = IOUtils.toInputStream(deepNesting, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll
      story.choose(0)
      story.nextAll
      story.choose(0)
      story.nextAll
      story.choose(0)
      story.nextAll
      story.choose(0)
      story.nextAll
      story.choose(0)
      val text = story.nextAll
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("\"...Tell us a tale Captain!\"")
      text.get(1) must beEqualTo("To a man, the crew began to yawn.")
    }

    val complexFlow =
      """I looked at Monsieur Fogg
        | *   ... and I could contain myself no longer.
        |    'What is the purpose of our journey, Monsieur?'
        |    'A wager,' he replied.
        |    * *     'A wager!'[] I returned.
        |            He nodded.
        |            * * *   'But surely that is foolishness!'
        |            * * *  'A most serious matter then!'
        |            - - -   He nodded again.
        |            * * *   'But can we win?'
        |                    'That is what we will endeavour to find out,' he answered.
        |            * * *   'A modest wager, I trust?'
        |                    'Twenty thousand pounds,' he replied, quite flatly.
        |            * * *   I asked nothing further of him then[.], and after a final, polite cough, he offered nothing more to me. <>
        |    * *     'Ah[.'],' I replied, uncertain what I thought.
        |    - -     After that, <>
        |*   ... but I said nothing[] and <>
        |- we passed the day in silence.
        |- -> END
      """.stripMargin


    "- offer a compact way to weave and blend text and options (Example 1)" in {
      val inputStream = IOUtils.toInputStream(complexFlow, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll
      story.choose(1)
      val text = story.nextAll
      text.get(0) must beEqualTo("... but I said nothing and we passed the day in silence.")
    }

    "- offer a compact way to weave and blend text and options (Example 2)" in {
      val inputStream = IOUtils.toInputStream(complexFlow, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer(), null)
      story.nextAll
      story.choose(0)
      val text0 = story.nextAll
      text0.size() must beEqualTo(3)
      story.choose(0)
      val text1 = story.nextAll
      text1.size() must beEqualTo(2)
      story.choose(1)
      val text2 = story.nextAll
      text2.size() must beEqualTo(2)
      story.choose(1)
      val text3 = story.nextAll
      text3.size() must beEqualTo(3)
    }

  }

}