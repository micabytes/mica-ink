package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConditionalSpec extends Specification {


  "Conditionals" should {
    val ifTrue =
      """=== test
        |VAR x = 2
        |VAR y = 0
        |{ x > 0:
        |    ~ y = x - 1
        |}
        |The value is {y}.
      """.stripMargin

    "- evaluate the statements if the condition evaluates to true" in {
      val inputStream = IOUtils.toInputStream(ifTrue, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value is 1.")
    }

    val ifFalse =
      """=== test
        |VAR x = 0
        |VAR y = 3
        |{ x > 0:
        |    ~ y = x - 1
        |}
        |The value is {y}.
      """.stripMargin

    "- not evaluate the statement if the condition evaluates to false" in {
      val inputStream = IOUtils.toInputStream(ifFalse, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value is 3.")
    }

    val ifElse =
      """=== test
        |VAR x = 0
        |VAR y = 3
        |{ x > 0:
        |    ~ y = x - 1
        |- else:
        |    ~ y = x + 1
        |}
        |The value is {y}.
      """.stripMargin

    "- evaluate an else statement if it exists and no other condition evaluates to true" in {
      val inputStream = IOUtils.toInputStream(ifElse, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value is 1.")
    }

    val ifElseExt =
      """=== test
        |VAR x = -2
        |VAR y = 3
        |{
        |    - x == 0:
        |        ~ y = 0
        |    - x > 0:
        |        ~ y = x - 1
        |    - else:
        |        ~ y = x + 1
        |}
        |The value is {y}.
      """.stripMargin

    "- evaluate an extended else statement if it exists and no other condition evaluates to true" in {
      val inputStream = IOUtils.toInputStream(ifElseExt, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("The value is -1.")
    }

    val condText =
      """ "We are going on a trip," said Monsieur Fogg.
        |* [The wager.] -> know_about_wager
        |* [I was surprised.] -> i_stared
        |
        |=== know_about_wager
        |I had heard about the wager.
        |-> i_stared
        |
        |=== i_stared
        |I stared at Monsieur Fogg.
        |{ know_about_wager:
        |    <> "But surely you are not serious?" I demanded.
        |- else:
        |    <> "But there must be a reason for this trip," I observed.
        |}
        |He said nothing in reply, merely considering his newspaper with as much thoroughness as entomologist considering his latest pinned addition.
      """.stripMargin

    "- work with conditional content which is not only logic (example 1)" in {
      val inputStream = IOUtils.toInputStream(condText, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.nextAll()
      story.choose(0)
      val text = story.nextAll()
      text.size() must beEqualTo(3)
      text.get(1) must beEqualTo("I stared at Monsieur Fogg. \"But surely you are not serious?\" I demanded.")
    }

    "- work with conditional content which is not only logic (example 2)" in {
      val inputStream = IOUtils.toInputStream(condText, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.nextAll()
      story.choose(1)
      val text = story.nextAll()
      text.size() must beEqualTo(2)
      text.get(0) must beEqualTo("I stared at Monsieur Fogg. \"But there must be a reason for this trip,\" I observed.")
    }

    val condOpt =
      """I looked...
        |* [at the door]
        |  -> door_open
        |* [outside]
        |  -> leave
        |
        |=== door_open
        |at the door. It was open.
        |-> leave
        |
        |=== leave
        |I stood up and...        |
        |{ door_open:
        |    *   I strode out of the compartment[] and I fancied I heard my master quietly tutting to himself.           -> go_outside
        |- else:
        |    *   I asked permission to leave[] and Monsieur Fogg looked surprised.   -> open_door
        |    *   I stood and went to open the door[]. Monsieur Fogg seemed untroubled by this small rebellion. -> open_door
        |}
      """.stripMargin

    "- work with options as conditional content (example 1)" in {
      val inputStream = IOUtils.toInputStream(condOpt, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.nextAll()
      story.choose(0)
      story.nextAll()
      story.getChoiceSize() must beEqualTo(1)
    }

    "- work with options as conditional content (example 2)" in {
      val inputStream = IOUtils.toInputStream(condOpt, "UTF-8")
      val story = InkParser.parse(inputStream)
      story.nextAll()
      story.choose(1)
      story.nextAll()
      story.getChoiceSize() must beEqualTo(2)
    }

  }

  "Multiline list blocks" should {
    val stopping =
      """=== test
        |{ stopping:
        |    -   I entered the casino.
        |    -  I entered the casino again.
        |    -  Once more, I went inside.
        |}
        |+ [Try again] -> test
      """.stripMargin

    "- go through the alternatives and stick on last when the keyword is stopping" in {
      val inputStream = IOUtils.toInputStream(stopping, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextAll()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("I entered the casino.")
      story.choose(0)
      val text1 = story.nextAll()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("I entered the casino again.")
      story.choose(0)
      val text2 = story.nextAll()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("Once more, I went inside.")
      story.choose(0)
      val text3 = story.nextAll()
      text3.size() must beEqualTo(1)
      text3.get(0) must beEqualTo("Once more, I went inside.")
    }

    val cycle =
      """=== test
        |{ cycle:
        |    - I held my breath.
        |    - I waited impatiently.
        |    - I paused.
        |}
        |+ [Try again] -> test
      """.stripMargin

    "- show each in turn and then cycle when the keyword is cycle" in {
      val inputStream = IOUtils.toInputStream(cycle, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextAll()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("I held my breath.")
      story.choose(0)
      val text1 = story.nextAll()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("I waited impatiently.")
      story.choose(0)
      val text2 = story.nextAll()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("I paused.")
      story.choose(0)
      val text3 = story.nextAll()
      text3.size() must beEqualTo(1)
      text3.get(0) must beEqualTo("I held my breath.")
    }

    val once =
      """=== test
        |{ once:
        |    - Would my luck hold?
        |    - Could I win the hand?
        |}
        |+ [Try again] -> test
      """.stripMargin

    "- show each, once, in turn, until all have been shown when the keyword is once" in {
      val inputStream = IOUtils.toInputStream(once, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextAll()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("Would my luck hold?")
      story.choose(0)
      val text1 = story.nextAll()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("Could I win the hand?")
      story.choose(0)
      val text2 = story.nextAll()
      text2.size() must beEqualTo(0)
      story.choose(0)
      val text3 = story.nextAll()
      text3.size() must beEqualTo(0)
    }

    val shuffle =
      """=== test
        |{ shuffle:
        |    -   Ace of Hearts.
        |    -   King of Spades.
        |    -   2 of Diamonds.
        |}
        |+ [Try again] -> test
      """.stripMargin

    "- show one at random when the keyword is shuffle" in {
      val inputStream = IOUtils.toInputStream(shuffle, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextAll()
      text0.size() must beEqualTo(1)
      story.choose(0)
      val text1 = story.nextAll()
      text1.size() must beEqualTo(1)
      story.choose(0)
      val text2 = story.nextAll()
      text2.size() must beEqualTo(1)
      story.choose(0)
      val text3 = story.nextAll()
      text3.size() must beEqualTo(1)
      // No check of the result, as that is random
    }

    val multiline =
      """=== test
        |{ stopping:
        |    -   At the table, I drew a card. Ace of Hearts.
        |    -   2 of Diamonds.
        |        "Should I hit you again," the croupier asks.
        |    -   King of Spades.
        |    "You lose," he crowed.
        |}
        |+ [Draw a card] I drew a card. -> test
      """.stripMargin

    "- show multiple lines of texts from multiline list blocks" in {
      val inputStream = IOUtils.toInputStream(multiline, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextAll()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("At the table, I drew a card. Ace of Hearts.")
      story.choose(0)
      val text1 = story.nextAll()
      text1.size() must beEqualTo(2)
      text1.get(0) must beEqualTo("I drew a card. 2 of Diamonds.")
      text1.get(1) must beEqualTo("\"Should I hit you again,\" the croupier asks.")
      story.choose(0)
      val text2 = story.nextAll()
      text2.size() must beEqualTo(2)
      text2.get(0) must beEqualTo("I drew a card. King of Spades.")
      text2.get(1) must beEqualTo("\"You lose,\" he crowed.")
    }

    val multilineDivert =
      """=== test
        |{ stopping:
        |    -   At the table, I drew a card. Ace of Hearts.
        |    -   2 of Diamonds.
        |        "Should I hit you again," the croupier asks.
        |    -   King of Spades.
        |        -> he_crowed
        |}
        |+ [Draw a card] I drew a card. -> test
        |
        |== he_crowed
        |"You lose," he crowed.
      """.stripMargin

    "- allow for embedded diverts" in {
      val inputStream = IOUtils.toInputStream(multilineDivert, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextAll()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("At the table, I drew a card. Ace of Hearts.")
      story.choose(0)
      val text1 = story.nextAll()
      text1.size() must beEqualTo(2)
      text1.get(0) must beEqualTo("I drew a card. 2 of Diamonds.")
      text1.get(1) must beEqualTo("\"Should I hit you again,\" the croupier asks.")
      story.choose(0)
      val text2 = story.nextAll()
      text2.size() must beEqualTo(2)
      text2.get(0) must beEqualTo("I drew a card. King of Spades.")
      text2.get(1) must beEqualTo("\"You lose,\" he crowed.")
    }

    val multilineChoice =
      """=== test
        |{ stopping:
        |    -   At the table, I drew a card. Ace of Hearts.
        |    -   2 of Diamonds.
        |        "Should I hit you again," the croupier asks.
        |        * [No.] I left the table. -> END
        |    -   King of Spades.
        |        "You lose," he crowed.
        |        -> END
        |}
        |+ [Draw a card] I drew a card. -> test
      """.stripMargin

    "- allow for embedded choices" in {
      val inputStream = IOUtils.toInputStream(multilineChoice, "UTF-8")
      val story = InkParser.parse(inputStream)
      val text0 = story.nextAll()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("At the table, I drew a card. Ace of Hearts.")
      story.choose(0)
      story.nextAll()
      story.getChoiceSize must beEqualTo(2)
      story.choose(0)
      val text2 = story.nextAll()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("I left the table.")
    }

  }


}
