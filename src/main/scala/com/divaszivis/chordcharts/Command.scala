package com.divaszivis.chordcharts

import chord.{Drop2and4, RootPosition, Chord}

/**
 * Created by eolander on 3/17/15.
 */
sealed trait Command {
  def apply(c: Chord): Chord
}

object Command {
  def apply(cmd: Option[String]) = cmd.map {
    _.toLowerCase match {
      case "root" => Root
      case "drop2" => Drop2
      case "drop24" => Drop24
      case "shell" => Shell
      case _ => Noop
    }
  }.getOrElse(Noop)
}

case object Root extends Command {
  def apply(c: Chord): Chord = {
    new Chord(c) with RootPosition
  }
}

case object Drop2 extends Command {
  def apply(c: Chord): Chord = {
    new Chord(c) with chord.Drop2
  }
}

case object Drop24 extends Command {
  def apply(c: Chord): Chord = {
    new Chord(c) with Drop2and4
  }
}

case object Shell extends Command {
  def apply(c: Chord): Chord = {
    c.asShell
  }
}

case object Noop extends Command {
  def apply(c: Chord): Chord = {
    c
  }
}

