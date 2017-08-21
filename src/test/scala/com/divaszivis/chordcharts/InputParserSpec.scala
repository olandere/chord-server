package com.divaszivis.chordcharts

import chord.InvalidChord
import org.scalatest.{FlatSpec, Matchers}

class InputParserSpec extends FlatSpec with Matchers {

  "InputParser" should "recognize lists of chords" in {
    InputParser("C/G,G,D/A,A5")
  }

  it should "handle power chords" in {
    InputParser("A5") shouldNot be(List((InvalidChord, None)))
  }

  it should "handle invalid input" in {
    InputParser("A57") shouldBe List((InvalidChord, None))
  }
}