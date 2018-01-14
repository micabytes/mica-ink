package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class GatherSpec : Spek({

  given("Gathers") {

    val gatherBasic =
        """=== test_knot ===
          |"What's that?" my master asked.
          |    *  "I am somewhat tired[."]," I repeated.
          |       "Really," he responded. "How deleterious."
          |    *  "Nothing, Monsieur!"[] I replied.
          |       "Very good, then."
          |    *  "I said, this journey is appalling[."] and I want no more of it."
          |       "Ah," he replied, not unkindly. "I see you are feeling frustrated. Tomorrow, things will improve."
          |- With that Monsieur Fogg left the room.
        """.trimMargin()

    it("gather the flow back together again") {
      val inputStream = IOUtils.toInputStream(gatherBasic, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(1)
      val text = story.next()
      text.size shouldEqual (4)
      text[0] shouldEqual ("\"What's that?\" my master asked.")
      text[1] shouldEqual ("\"Nothing, Monsieur!\" I replied.")
      text[2] shouldEqual ("\"Very good, then.\"")
      text[3] shouldEqual ("With that Monsieur Fogg left the room.")
    }

    val gatherChain =
        """=== escape ===
          |I ran through the forest, the dogs snapping at my heels.
          |    *   I checked the jewels[] were still in my pocket, and the feel of them brought a spring to my step. <>
          |    *  I did not pause for breath[] but kept on running. <>
          |    *   I cheered with joy. <>
          |-   The road could not be much further! Mackie would have the engine running, and then I'd be safe.
          |    *   I reached the road and looked about[]. And would you believe it?
          |    *   I should text to say Mackie is normally very reliable[]. He's never once let me down. Or rather, never once, previously to that night.
          |-   The road was empty. Mackie was nowhere to be seen.
        """.trimMargin()

    it("form chains of content with multiple gathers") {
      val inputStream = IOUtils.toInputStream(gatherChain, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (3)
      story.choose(1)
      val text0 = story.next()
      text0.size shouldEqual (2)
      text0[1] shouldEqual ("I did not pause for breath but kept on running. The road could not be much further! Mackie would have the engine running, and then I'd be safe.")
      story.choiceSize shouldEqual (2)
      story.choose(0)
      val text1 = story.next()
      text1.size shouldEqual (4)
      text1[2] shouldEqual ("I reached the road and looked about. And would you believe it?")
      text1[3] shouldEqual ("The road was empty. Mackie was nowhere to be seen.")
    }

    val nestedFlow =
        """=== test_knot ===
            |Well, Poirot? Murder or suicide?"
            |    *   "Murder!"
            |        "And who did it?"
            |        * *     "Detective-Inspector Japp!"
            |        * *     "Captain Hastings!"
            |        * *     "Myself!"
            |    *   "Suicide!"
            |    -   Mrs. Christie lowered her manuscript a moment. The rest of the writing group sat, open-mouthed.
          """.trimMargin()

    it("allow nested options to pop out to a higher level gather") {
      val inputStream = IOUtils.toInputStream(nestedFlow, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choiceSize shouldEqual (2)
      story.choose(0)
      story.next()
      story.choiceSize shouldEqual (3)
      story.choose(2)
      val text = story.next()
      text.size shouldEqual (5)
      text[3] shouldEqual ("\"Myself!\"")
      text[4] shouldEqual ("Mrs. Christie lowered her manuscript a moment. The rest of the writing group sat, open-mouthed.")
    }

    val nestedGather =
        """=== test_knot ===
            |Well, Poirot? Murder or suicide?"
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
          """.trimMargin()

    it("allow nested gathers within the flow") {
      val inputStream = IOUtils.toInputStream(nestedGather, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      story.next()
      story.choose(2)
      val text0 = story.next()
      text0.size shouldEqual (5)
      text0[3] shouldEqual ("\"Myself!\"")
      text0[4] shouldEqual ("\"You must be joking!\"")
      story.choiceSize shouldEqual (2)
      story.choose(0)
      val text1 = story.next()
      text1.size shouldEqual (7)
      text1[5] shouldEqual ("\"Mon ami, I am deadly serious.\"")
      text1[6] shouldEqual ("Mrs. Christie lowered her manuscript a moment. The rest of the writing group sat, open-mouthed.")
    }

    val deepNesting =
        """=== test_knot ===
            |Tell us a tale, Captain!"
            |    *   "Very well, you sea-dogs. Here's a tale..."
            |        * *     "It was a dark and stormy night..."
            |                * * *   "...and the crew were restless..."
            |                        * * * *  "... and they said to their Captain..."
            |                                * * * * *       "...Tell us a tale Captain!"
            |    *   "No, it's past your bed-time."
            |-   To a man, the crew began to yawn.
          """.trimMargin()

    it("gather the flow back together again from arbitrarily deep options") {
      val inputStream = IOUtils.toInputStream(deepNesting, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      story.next()
      story.choose(0)
      story.next()
      story.choose(0)
      story.next()
      story.choose(0)
      story.next()
      story.choose(0)
      val text = story.next()
      text.size shouldEqual (7)
      text[5] shouldEqual ("\"...Tell us a tale Captain!\"")
      text[6] shouldEqual ("To a man, the crew began to yawn.")
    }

    val complexFlow =
        """=== test_knot ===
            |I looked at Monsieur Fogg
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
            |  -> END
          """.trimMargin()

    it("offer a compact way to weave and blend text and options (Example 1)") {
      val inputStream = IOUtils.toInputStream(complexFlow, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(1)
      val text = story.next()
      text.size shouldEqual (2)
      text[1] shouldEqual ("... but I said nothing and we passed the day in silence.")
    }

    it("offer a compact way to weave and blend text and options (Example 2)") {
      val inputStream = IOUtils.toInputStream(complexFlow, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      val text0 = story.next()
      text0.size shouldEqual (4)
      story.choose(0)
      val text1 = story.next()
      text1.size shouldEqual (6)
      story.choose(1)
      val text2 = story.next()
      text2.size shouldEqual (8)
      story.choose(1)
      val text3 = story.next()
      text3.size shouldEqual (11)
    }

    it("offer a compact way to weave and blend text and options (Example 3)") {
      val inputStream = IOUtils.toInputStream(complexFlow, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      story.next()
      story.choose(0)
      val text0 = story.next()
      text0.size shouldEqual (4)
      story.choose(0)
      val text1 = story.next()
      text1.size shouldEqual (6)
      story.choose(1)
      val text2 = story.next()
      text2.size shouldEqual (8)
      story.choose(2)
      val text3 = story.next()
      text3.size shouldEqual (9)
      text3[8] shouldEqual ("I asked nothing further of him then, and after a final, polite cough, he offered nothing more to me. After that, we passed the day in silence.")
    }

  }

})