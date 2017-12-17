package com.divaszivis.chordcharts

import chord.{Chord, ChordParser, InvalidChord, Note, ShellChord}
import grizzled.slf4j.Logging

/**
  * Created by eolander on 6/7/15.
  */
class InputParser extends ChordParser with Logging {

  val drop2 = """(?i)d(rop)?2""".r ~> ":".? ~> chord ^^ { ch => Drop2(ch) }

  val rootPos = """(?i)r(oot)?""".r ~> ":".? ~> chord ^^ { ch => Root(ch) }

  val drop24 = """(?i)d(rop)?24""".r ~> ":".? ~> chord ^^ { ch => Drop24(ch) }

  val shell = """(?i)shell""".r ~> ":".? ~> chord ^^ { ch => Shell(ch) }

  val noop = (pitchClassWithRoot | chord) ^^ {
    Noop(_)
  }

  val atFret = (drop2 | rootPos | drop24 | shell | powerChord | noop) ~ ("@" ~> """\d+""".r).? ~ ("^" ~> root).? ^^ {
    case c ~ f ~ tn => (c, f.map {
      _.toInt
    }, tn.map{Note(_)})
  }

  def parseIt = repsep(atFret, sep)

  def apply(input: String): List[(Chord, Option[Int], Option[Note])] =
    parseAll(parseIt, input) match {
      case Success(result, _) => result
      case failure: NoSuccess =>
        error(s"$input: ${failure.msg}")
        List((InvalidChord, None, None))
    }
}

object InputParser extends InputParser

object ShellInputParser extends InputParser {
  //override val noop = chord ^^ {Shell(_)}

  override def apply(input: String): List[(Chord, Option[Int], Option[Note])] =
    parseAll(parseIt, input) match {
      case Success(result, _) => println(result); result.map { case (c, i, tn) => println(c); (ShellChord(c), i, tn) }
      case failure: NoSuccess =>
        error(s"$input: ${failure.msg}")
        List((InvalidChord, None, None))
    }
}
