package com.micabytes.ink

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import org.apache.commons.io.IOUtils
import java.math.BigDecimal

class ExpressionSpec : WordSpec() {

  init {

    "Expression" should {

      val testStory =
          """== test_knot
          |Hello, world!
          |-> END
          """.trimMargin()

      "handle AND" {
        val inputStream = IOUtils.toInputStream(testStory, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        Expression("1&&1").eval(story) shouldBe(BigDecimal.ONE)
        Expression("1&&0").eval(story) shouldBe(BigDecimal.ZERO)
        Expression("0&&0").eval(story) shouldBe(BigDecimal.ZERO)
      }

      "handle OR" {
        val inputStream = IOUtils.toInputStream(testStory, "UTF-8")
        val story = InkParser.parse(inputStream, TestWrapper(), "Test")
        Expression("1||1").eval(story) shouldBe(BigDecimal.ONE)
        Expression("1||0").eval(story) shouldBe(BigDecimal.ONE)
        Expression("0||0").eval(story) shouldBe(BigDecimal.ZERO)
      }

      /*
	assertEquals("1", new Expression("2>1").eval().toString());
		assertEquals("0", new Expression("2<1").eval().toString());
		assertEquals("0", new Expression("1>2").eval().toString());
		assertEquals("1", new Expression("1<2").eval().toString());
		assertEquals("0", new Expression("1=2").eval().toString());
		assertEquals("1", new Expression("1=1").eval().toString());
		assertEquals("1", new Expression("1>=1").eval().toString());
		assertEquals("1", new Expression("1.1>=1").eval().toString());
		assertEquals("0", new Expression("1>=2").eval().toString());
		assertEquals("1", new Expression("1<=1").eval().toString());
		assertEquals("0", new Expression("1.1<=1").eval().toString());
		assertEquals("1", new Expression("1<=2").eval().toString());
		assertEquals("0", new Expression("1=2").eval().toString());
		assertEquals("1", new Expression("1=1").eval().toString());
		assertEquals("1", new Expression("1!=2").eval().toString());
		assertEquals("0", new Expression("1!=1").eval().toString());
	}

	@Test
	public void testCompareCombined() {
		assertEquals("1", new Expression("(2>1)||(1=0)").eval().toString());
		assertEquals("0", new Expression("(2>3)||(1=0)").eval().toString());
		assertEquals("1", new Expression("(2>3)||(1=0)||(1&&1)").eval().toString());
	}

	@Test
	public void testMixed() {
		assertEquals("0", new Expression("1.5 * 7 = 3").eval().toString());
		assertEquals("1", new Expression("1.5 * 7 = 10.5").eval().toString());
	}

	@Test
	public void testNot() {
		assertEquals("0", new Expression("not(1)").eval().toString());
		assertEquals("1", new Expression("not(0)").eval().toString());
		assertEquals("1", new Expression("not(1.5 * 7 = 3)").eval().toString());
		assertEquals("0", new Expression("not(1.5 * 7 = 10.5)").eval().toString());
	}

	@Test
	public void testConstants() {
		assertEquals("1", new Expression("TRUE!=FALSE").eval().toString());
		assertEquals("0", new Expression("TRUE==2").eval().toString());
		assertEquals("1", new Expression("NOT(TRUE)==FALSE").eval().toString());
		assertEquals("1", new Expression("NOT(FALSE)==TRUE").eval().toString());
		assertEquals("0", new Expression("TRUE && FALSE").eval().toString());
		assertEquals("1", new Expression("TRUE || FALSE").eval().toString());
	}

	@Test
	public void testIf() {
		assertEquals("5", new Expression("if(TRUE, 5, 3)").eval().toString());
		assertEquals("3", new Expression("IF(FALSE, 5, 3)").eval().toString());
		assertEquals("5.35", new Expression("If(2, 5.35, 3)").eval().toString());
	}
       */

    }

  }

}