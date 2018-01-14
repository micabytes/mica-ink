package com.micabytes.ink

import com.micabytes.ink.helpers.TestWrapper
import org.amshove.kluent.shouldEqual
import org.apache.commons.io.IOUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class IncludeSpec : Spek({

  given("Includes") {

    val include1 =
        """INCLUDE includeTest1
            |=== knotA ===
            |This is a knot. -> includeKnot
      """.trimMargin()

    it("process an INCLUDE statement and add the content of the include file") {
      val inputStream = IOUtils.toInputStream(include1, "UTF-8")
      val story = InkParser.parse(inputStream, TestWrapper(), "Test")
      val text = story.next()
      text.size shouldEqual (1)
      text[0] shouldEqual ("This is a knot. This is an included knot.")
    }

  }

})