package com.micabytes.ink

class KnotSpec : WordSpec() {

  init {
    "String.length" should {
      "return the length of the string" {
        "sammy".length shouldBe 5
        "".length shouldBe 0
      }
    }
  }
}