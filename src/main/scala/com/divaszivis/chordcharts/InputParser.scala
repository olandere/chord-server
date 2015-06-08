package com.divaszivis.chordcharts

import chord.{InvalidChord, Chord, ChordParser}
import grizzled.slf4j.Logging

/**
 * Created by eolander on 6/7/15.
 */
object InputParser extends ChordParser with Logging {

  val drop2 = "drop2" ~> ":".? ~> chord ^^ {ch => Drop2(ch)}

  val rootPos = "root" ~> ":".? ~> chord ^^ {ch => Root(ch)}

  val drop24 = "drop24" ~> ":".? ~> chord ^^ {ch => Drop24(ch)}

  val shell = "shell" ~> ":".? ~> chord ^^ {ch => Shell(ch)}

  val noop = chord ^^ {Noop(_)}

  val atFret = (drop2 | rootPos | drop24 | shell | noop) ~ ("@" ~> """\d+""".r).? ^^ {
    case c ~ f => (c, f.map{_.toInt})
  }

  def parseIt = repsep(atFret, sep)

  def apply(input: String): List[(Chord, Option[Int])] = parseAll(parseIt, input) match {
    case Success(result, _) => result
    case failure : NoSuccess =>
      error(failure.msg)
      List((InvalidChord, None))
  }
}
