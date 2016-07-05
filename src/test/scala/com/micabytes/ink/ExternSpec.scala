package com.micabytes.ink

import org.apache.commons.io.IOUtils
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ExternSpec extends Specification {

  class testClass {
    def hello() = {
      "Hello, is it me you're looking for?"
    }
    def number(b: java.math.BigDecimal) = {
      "Mambo Number " + b.toPlainString
    }
    def wrong() = {
      false
    }
  }


  "Methods on external objects" should {

    val helloWorld =
      """{x.hello()}
        |-> END
      """.stripMargin

    "- be possible to call on an object without any parameters" in {
      val inputStream = IOUtils.toInputStream(helloWorld, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer())
      story.putVariable("x", new testClass())
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Hello, is it me you're looking for?")
    }

    val helloNoBrace =
      """{x.hello()}
        |-> END
      """.stripMargin

    "- be possible to call on an object without any parameters and no function brace" in {
      val inputStream = IOUtils.toInputStream(helloNoBrace, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer())
      story.putVariable("x", new testClass())
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Hello, is it me you're looking for?")
    }

    val mambo =
      """VAR y = 5
        |{x.number(y)}
        |-> END
      """.stripMargin

    "- be possible to call on an object with a parameter defined" in {
      val inputStream = IOUtils.toInputStream(mambo, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer())
      story.putVariable("x", new testClass())
      val text = story.nextAll()
      text.size() must beEqualTo(1)
      text.get(0) must beEqualTo("Mambo Number 5")
    }

    val externChoices =
    """=== choice_test ===
      |Test conditional choices
      |+ {x.wrong()} not displayed
      |+ shown
      """.stripMargin

    "- resolve external bools correctly in conditional choices" in {
      val inputStream = IOUtils.toInputStream(externChoices, "UTF-8")
      val story = InkParser.parse(inputStream, new StoryContainer())
      story.putVariable("x", new testClass())
      story.nextAll()
      story.getChoiceSize must beEqualTo(1)
    }


  }

}