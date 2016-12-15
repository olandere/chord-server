package com.divaszivis.chordcharts

import chord.InvalidChord
import org.scalatest.FlatSpec

class InputParserSpec extends FlatSpec {

  "InputParser" should "recognize lists of chords" in {
    InputParser("C/G,G,D/A,A5")
  }

  it should "handle power chords" in {
    assert(InputParser("A5") != List((InvalidChord, None)))
  }
}