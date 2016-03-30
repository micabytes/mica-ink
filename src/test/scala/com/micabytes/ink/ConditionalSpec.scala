package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ConditionalSpec extends Specification {

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
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("I entered the casino.")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("I entered the casino again.")
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("Once more, I went inside.")
      story.choose(0)
      val text3 = story.nextChoice()
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
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("I held my breath.")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("I waited impatiently.")
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("I paused.")
      story.choose(0)
      val text3 = story.nextChoice()
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
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("Would my luck hold?")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      text1.get(0) must beEqualTo("Could I win the hand?")
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(0)
      story.choose(0)
      val text3 = story.nextChoice()
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
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(1)
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      story.choose(0)
      val text3 = story.nextChoice()
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
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("At the table, I drew a card. Ace of Hearts.")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(2)
      text1.get(0) must beEqualTo("I drew a card. 2 of Diamonds.")
      text1.get(1) must beEqualTo("\"Should I hit you again,\" the croupier asks.")
      story.choose(0)
      val text2 = story.nextChoice()
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
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("At the table, I drew a card. Ace of Hearts.")
      story.choose(0)
      val text1 = story.nextChoice()
      text1.size() must beEqualTo(2)
      text1.get(0) must beEqualTo("I drew a card. 2 of Diamonds.")
      text1.get(1) must beEqualTo("\"Should I hit you again,\" the croupier asks.")
      story.choose(0)
      val text2 = story.nextChoice()
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
      val text0 = story.nextChoice()
      text0.size() must beEqualTo(1)
      text0.get(0) must beEqualTo("At the table, I drew a card. Ace of Hearts.")
      story.choose(0)
      story.nextChoice()
      story.getChoiceSize must beEqualTo(2)
      story.choose(0)
      val text2 = story.nextChoice()
      text2.size() must beEqualTo(1)
      text2.get(0) must beEqualTo("I left the table.")
    }

  }


}
