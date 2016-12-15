package com.divaszivis.chordcharts

import chord.{ShellChord, InvalidChord, Chord, ChordParser}
import grizzled.slf4j.Logging

/**
  * Created by eolander on 6/7/15.
  */
class InputParser extends ChordParser with Logging {

  val drop2 = """(?i)drop2""".r ~> ":".? ~> chord ^^ { ch => Drop2(ch) }

  val rootPos = """(?i)root""".r ~> ":".? ~> chord ^^ { ch => Root(ch) }

  val drop24 = """(?i)drop24""".r ~> ":".? ~> chord ^^ { ch => Drop24(ch) }

  val shell = """(?i)shell""".r ~> ":".? ~> chord ^^ { ch => Shell(ch) }

  val noop = chord ^^ {
    Noop(_)
  }

  val atFret = (drop2 | rootPos | drop24 | shell | powerChord | noop) ~ ("@" ~> """\d+""".r).? ^^ {
    case c ~ f => (c, f.map {
      _.toInt
    })
  }

  def parseIt = repsep(atFret, sep)

  def apply(input: String): List[(Chord, Option[Int])] =
    parseAll(parseIt, input) match {
      case Success(result, _) => result
      case failure: NoSuccess =>
        error(failure.msg)
        List((InvalidChord, None))
    }
}

object InputParser extends InputParser

object ShellInputParser extends InputParser {
  //override val noop = chord ^^ {Shell(_)}

  override def apply(input: String): List[(Chord, Option[Int])] =
    parseAll(parseIt, input) match {
      case Success(result, _) => println(result); result.map { case (c, i) => println(c); (ShellChord(c), i) }
      case failure: NoSuccess =>
        error(failure.msg)
        List((InvalidChord, None))
    }
}
