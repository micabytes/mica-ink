package com.micabytes.ink

import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils

class ConditionalSpec : WordSpec() {

  init {

    "Conditionals" should {
      val ifTrue =
          """=== test
        |VAR x = 2
        |VAR y = 0
        |{ x > 0:
        |    ~ y = x - 1
        |}
        |The value is {y}.
      """.trimMargin()

      "- evaluate the statements if the condition evaluates to true" {
        val inputStream = IOUtils.toInputStream(ifTrue, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value is 1.")
      }

      val ifFalse =
          """=== test
        |VAR x = 0
        |VAR y = 3
        |{ x > 0:
        |    ~ y = x - 1
        |}
        |The value is {y}.
      """.trimMargin()

      "- not evaluate the statement if the condition evaluates to false" {
        val inputStream = IOUtils.toInputStream(ifFalse, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value is 3.")
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
      """.trimMargin()

      "- evaluate an else statement if it exists and no other condition evaluates to true" {
        val inputStream = IOUtils.toInputStream(ifElse, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value is 1.")
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
      """.trimMargin()

      "- evaluate an extended else statement if it exists and no other condition evaluates to true" {
        val inputStream = IOUtils.toInputStream(ifElseExt, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("The value is -1.")
      }

      val ifElseExtText1 =
          """=== test
        |VAR x = 0
        |{
        |    - x == 0:
        |      This is text 1.
        |    - x > 0:
        |      This is text 2.
        |    - else:
        |      This is text 3.
        |}
        |+ [The Choice.] -> to_end
        |=== to_end
        |This is the end.
      """.trimMargin()

      "- evaluate an extended else statement with text and divert at the end" {
        val inputStream = IOUtils.toInputStream(ifElseExtText1, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("This is text 1.")
        story.choiceSize shouldBe (1)
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("This is the end.")
      }

      val ifElseExtText2 =
          """=== test
        |VAR x = 2
        |{
        |    - x == 0:
        |      This is text 1.
        |    - x > 0:
        |      This is text 2.
        |    - else:
        |      This is text 3.
        |}
        |+ [The Choice.] -> to_end
        |=== to_end
        |This is the end.
      """.trimMargin()

      "- evaluate an extended else statement with text and divert at the end" {
        val inputStream = IOUtils.toInputStream(ifElseExtText2, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("This is text 2.")
        story.choiceSize shouldBe (1)
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("This is the end.")
      }

      val ifElseExtText3 =
          """=== test
        |VAR x = -2
        |{
        |    - x == 0:
        |      This is text 1.
        |    - x > 0:
        |      This is text 2.
        |    - else:
        |      This is text 3.
        |}
        |+ [The Choice.] -> to_end
        |=== to_end
        |This is the end.
      """.trimMargin()

      "- evaluate an extended else statement with text and divert at the end" {
        val inputStream = IOUtils.toInputStream(ifElseExtText3, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text = story.next()
        text.size shouldBe (1)
        text.get(0) shouldBe ("This is text 3.")
        story.choiceSize shouldBe (1)
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("This is the end.")
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
      """.trimMargin()

      "- work with conditional content which is not only logic (example 1)" {
        val inputStream = IOUtils.toInputStream(condText, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(0)
        val text = story.next()
        text.size shouldBe (3)
        text.get(1) shouldBe ("I stared at Monsieur Fogg. \"But surely you are not serious?\" I demanded.")
      }

      "- work with conditional content which is not only logic (example 2)" {
        val inputStream = IOUtils.toInputStream(condText, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(1)
        val text = story.next()
        text.size shouldBe (2)
        text.get(0) shouldBe ("I stared at Monsieur Fogg. \"But there must be a reason for this trip,\" I observed.")
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
      """.trimMargin()

      "- work with options as conditional content (example 1)" {
        val inputStream = IOUtils.toInputStream(condOpt, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(0)
        story.next()
        story.choiceSize shouldBe (1)
      }

      "- work with options as conditional content (example 2)" {
        val inputStream = IOUtils.toInputStream(condOpt, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        story.next()
        story.choose(1)
        story.next()
        story.choiceSize shouldBe (2)
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
      """.trimMargin()

      "- go through the alternatives and stick on last when the keyword is stopping" {
        val inputStream = IOUtils.toInputStream(stopping, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("I entered the casino.")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("I entered the casino again.")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (1)
        text2.get(0) shouldBe ("Once more, I went inside.")
        story.choose(0)
        val text3 = story.next()
        text3.size shouldBe (1)
        text3.get(0) shouldBe ("Once more, I went inside.")
      }

      val cycle =
          """=== test
        |{ cycle:
        |    - I held my breath.
        |    - I waited impatiently.
        |    - I paused.
        |}
        |+ [Try again] -> test
      """.trimMargin()

      "- show each in turn and then cycle when the keyword is cycle" {
        val inputStream = IOUtils.toInputStream(cycle, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("I held my breath.")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("I waited impatiently.")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (1)
        text2.get(0) shouldBe ("I paused.")
        story.choose(0)
        val text3 = story.next()
        text3.size shouldBe (1)
        text3.get(0) shouldBe ("I held my breath.")
      }

      val once =
          """=== test
        |{ once:
        |    - Would my luck hold?
        |    - Could I win the hand?
        |}
        |+ [Try again] -> test
      """.trimMargin()

      "- show each, once, in turn, until all have been shown when the keyword is once" {
        val inputStream = IOUtils.toInputStream(once, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("Would my luck hold?")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("Could I win the hand?")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (0)
        story.choose(0)
        val text3 = story.next()
        text3.size shouldBe (0)
      }

      val shuffle =
          """=== test
        |{ shuffle:
        |    -   Ace of Hearts.
        |    -   King of Spades.
        |    -   2 of Diamonds.
        |}
        |+ [Try again] -> test
      """.trimMargin()

      "- show one at random when the keyword is shuffle" {
        val inputStream = IOUtils.toInputStream(shuffle, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (1)
        story.choose(0)
        val text3 = story.next()
        text3.size shouldBe (1)
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
      """.trimMargin()

      "- show multiple lines of texts from multiline list blocks" {
        val inputStream = IOUtils.toInputStream(multiline, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("At the table, I drew a card. Ace of Hearts.")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (2)
        text1.get(0) shouldBe ("I drew a card. 2 of Diamonds.")
        text1.get(1) shouldBe ("\"Should I hit you again,\" the croupier asks.")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (2)
        text2.get(0) shouldBe ("I drew a card. King of Spades.")
        text2.get(1) shouldBe ("\"You lose,\" he crowed.")
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
      """.trimMargin()

      "- allow for embedded diverts" {
        val inputStream = IOUtils.toInputStream(multilineDivert, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("At the table, I drew a card. Ace of Hearts.")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (2)
        text1.get(0) shouldBe ("I drew a card. 2 of Diamonds.")
        text1.get(1) shouldBe ("\"Should I hit you again,\" the croupier asks.")
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (2)
        text2.get(0) shouldBe ("I drew a card. King of Spades.")
        text2.get(1) shouldBe ("\"You lose,\" he crowed.")
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
      """.trimMargin()

      "- allow for embedded choices" {
        val inputStream = IOUtils.toInputStream(multilineChoice, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("At the table, I drew a card. Ace of Hearts.")
        story.choose(0)
        story.next()
        story.choiceSize shouldBe (2)
        story.choose(0)
        val text2 = story.next()
        text2.size shouldBe (1)
        text2.get(0) shouldBe ("I left the table.")
      }

    }

    "Conditionals in choices should " should {

      val condChoice1 =
          """=== knot
        |VAR choice = 1
        |This is a knot.
        |* [I have chosen.]
        |  { choice > 0:
        |  	  -> choice_1
        |  	- else:
        |  	  -> choice_0
        |  }
        |* I have failed.
        |  -> END
        |=== choice_0
        |This is choice 0.
        |-> END
        |=== choice_1
        |This is choice 1.
        |-> END
      """.trimMargin()

      "- allow for diverts in the conditional that direct to another knot" {
        val inputStream = IOUtils.toInputStream(condChoice1, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("This is a knot.")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("This is choice 1.")
      }

      val condChoice0 =
          """=== knot
        |VAR choice = 0
        |This is a knot.
        |* [I have chosen.]
        |  { choice > 0:
        |  	  -> choice_1
        |  	- else:
        |  	  -> choice_0
        |  }
        |* I have failed.
        |  -> END
        |=== choice_0
        |This is choice 0.
        |-> END
        |=== choice_1
        |This is choice 1.
        |-> END
      """.trimMargin()

      "- allow for diverts in the else clause" {
        val inputStream = IOUtils.toInputStream(condChoice0, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        val text0 = story.next()
        text0.size shouldBe (1)
        text0.get(0) shouldBe ("This is a knot.")
        story.choose(0)
        val text1 = story.next()
        text1.size shouldBe (1)
        text1.get(0) shouldBe ("This is choice 0.")
      }
    }
  }
}