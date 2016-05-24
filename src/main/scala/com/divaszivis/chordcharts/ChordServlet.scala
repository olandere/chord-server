package com.divaszivis.chordcharts

import grizzled.slf4j.Logging
import org.scalatra.json._
import org.scalatra.util.RicherString._
import org.json4s.{DefaultFormats, Formats}
import chord._
import chord.Operations._
import scalaz._, syntax.show._
import scala.util.Try

class ChordServlet extends ChordserverStack with JacksonJsonSupport with Logging {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    response.headers ++= List(("X-XSS-Protection", "1; mode=block"),
      ("X-Frame-Options", "DENY"), ("X-Content-Type-Options", "nosniff"))
	  contentType = formats("json")
  }

  private def frettingToJson(c: String)(implicit tuning: Tuning): List[Any] =
    (if (c.trim.length == tuning.numStrings) {
      c.trim.toList.map {_.toString}
    } else c.split(" ").toList.filter {!_.isEmpty}
      ).map(n => Try {n.toInt}.getOrElse("x"))

  private def fretlistToJson(c:FretList, chord: Chord)(implicit tuning: Tuning) = {
    Map("frets" -> frettingToJson(c.shows),
      "degrees" -> chord.asDegrees(c).shows.split(" "),
      "name" -> chord.toString())
  }

  private def showFingerings(chord: Chord, span: Int, fret: Option[Int], condense: Boolean)(implicit tuning: Tuning) = {
    val result = if (condense) Operations.condense(fingerings(chord, span), span) else fingerings(chord, span)

    result.filter{c:FretList => fret.isEmpty || c.contains(fret)}.map
    {
      c: FretList => fretlistToJson(c, chord)
    }
  }

  // todo - normalize/sanitize chord name - strip whitespace, lower case to Caps, minor/min -> m, Major/maj -> M

  private def normalize(chord: String) =
    chord.trim().capitalize.split(" ").mkString.replaceAll("[m|M]in(or)?", "m").replaceAll("[m|M]aj(or)?", "M")

  private def parseChord(c: String) = {
    InputParser(c)
  }

  //todo: collapse API - probably no need for /chord and /chordprogression to be distinct
  get("/chord/:span?") {
    val (name, fret) = (params.get("name") map parseChord).get.head
    val condense = params.getOrElse("condense", "false").toBoolean

    if (!name.isValid) halt(400) else {
      val span = params.getOrElse("span", "6").toInt
      val tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
      info(s"/chord/$name/$span")
      info(s"tuning: $tuning")
      showFingerings(name, span, fret, condense)
    }
  }

  get("/analyze/:fingering") {
    val fingerings = params.get("fingering").get.split(",").map{_.trim}
    implicit val tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
    info(s"tuning $tuning")
    val result = fingerings.map { f =>
      val (degrees, name) = chords(f)
      Map("frets" -> frettingToJson(f), "degrees" -> degrees, "name" -> name)
    }
    println(result)
    Map("numChords" -> 1,
      "chordList" -> result.toList)
    // val chord = Chord.unapply(fingering)
    // List(Map("frets" -> frettingToJson(fingering), "degrees" -> "", "name" -> ""))
  }

  get("/shellchord/:span?") {
    handleChords(ShellInputParser)
  }

  def handleChords(parser: InputParser) = {
    val chords = parser(normalize(params.get("chord").get.urlDecode))
    val condense = params.getOrElse("condense", "false").toBoolean
    info(s"received $chords")
    val span = params.getOrElse("span", "6").toInt
    implicit val tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
    info(s"tuning: $tuning")
    if (chords.size == 1) {
      Map("numChords" -> chords.size,
        "chordList" -> showFingerings(chords.head._1, span, chords.head._2, condense))
    } else {
      val chordList = chords.map {
        _._1
      }
      Map("numChords" -> chords.size,
      "chordList" -> progression(chordList, span).flatten.zip((Stream continually chordList).flatten).map { case (f, c) => fretlistToJson(f, c)}.grouped(chords.size).toList)
    }
  }

  get("/chords/:span?") {
    handleChords(InputParser)
  }

  get("/") {
    redirect("/index.html")
  }

}
