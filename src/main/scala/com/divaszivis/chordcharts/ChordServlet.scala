package com.divaszivis.chordcharts

import chord.Operations._
import chord._
import grizzled.slf4j.Logging
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ContentEncodingSupport
import org.scalatra.json._
import org.scalatra.util.RicherString._

import scala.util.Try
import cats.implicits._

class ChordServlet extends ChordserverStack with NativeJsonSupport with Logging with ContentEncodingSupport {

  import ChordServlet._

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    response.headers ++= List(
      ("X-XSS-Protection", "1; mode=block"),
      ("X-Frame-Options", "DENY"),
      ("X-Content-Type-Options", "nosniff"),
      ("Access-Control-Allow-Origin", "*")
    )
	  contentType = formats("json")
  }

  get("/") {
    redirect("/index.html")
  }

  //todo: collapse API - probably no need for /chord and /chordprogression to be distinct
  get("/chord/:span?") {
    val (name, fret) = (params.get("name") map parseChord).get.head
    val condense = params.getOrElse("condense", "false").toBoolean
    val jazzVoicing = params.getOrElse("jazz", "false").toBoolean

    if (!name.isValid) halt(400) else {
      val span = params.getOrElse("span", "6").toInt
      val tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
      debug(s"/chord/$name/$span")
      debug(s"tuning: $tuning")
      showFingerings(name, span, fret, condense, jazzVoicing)
    }
  }

  get("/analyze/:fingering") {
    val fingerings = params.get("fingering").get.split(",").map{_.trim}
    implicit val tuning: Tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
    debug(s"tuning $tuning")
    val result = fingerings.map { f =>
      val (degrees, name, notes) = chords(f)
      Map("frets" -> frettingToJson(f), "degrees" -> degrees, "name" -> name, "notes" -> notes.show.split(" ").toList)
    }.toList
    debug(s"result: $result")
    Map("numChords" -> 1,
      "chordList" -> result)
    // val chord = Chord.unapply(fingering)
    // List(Map("frets" -> frettingToJson(fingering), "degrees" -> "", "name" -> ""))
  }

  get("/shellchord/:span?") {
    handleChords(ShellInputParser, params)
  }

  get("/chords/:span?") {
    handleChords(InputParser, params)
  }

  get("/arpeggio") {
    val chordName = normalize(params.get("chord").get.urlDecode)
    val chords = InputParser(chordName)
    implicit val tuning: Tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
    Map("arpeggioList" ->
      (for {chord <- chords.map(_._1)}
        yield Map("frets" -> arpeggio(chord), "name" -> chord.toString(), "roots" -> roots(chord.root))))
  }

  get("/scale") {
    val root = Note(params.get("root").get.urlDecode)
    val scaleName = params.get("scale").get.urlDecode
    implicit val tuning: Tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
    scaleFingering(root, MajorScale(root).semitones)
  }
}

object ChordServlet {

  import org.scalatra.Params

  def handleChords(parser: InputParser, params: Params): Map[String, Any] = {
    val chords = parser(normalize(params.get("chord").get.urlDecode))
    val condense = params.getOrElse("condense", "false").toBoolean
    val jazzVoicing = params.getOrElse("jazz", "false").toBoolean
   // debug(s"received $chords")
    val span = params.getOrElse("span", "6").toInt
    implicit val tuning: Tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
   // debug(s"tuning: $tuning")
    if (chords.size == 1) {
      Map("numChords" -> chords.size,
        "chordList" -> showFingerings(chords.head._1, span, chords.head._2, condense, jazzVoicing))
    } else {
      val chordList = chords.map {
        _._1
      }
      Map("numChords" -> chords.size,
        "chordList" -> progression(chordList, span, jazzVoicing).flatten.zip((Stream continually chordList).flatten).
          map { case (f, c) => fretlistToJson(f, c)}.grouped(chords.size).toList)
    }
  }

  def showFingerings(chord: Chord, span: Int, fret: Option[Int], condense: Boolean, jazzVoicing: Boolean)
                    (implicit tuning: Tuning): List[Map[String, AnyRef]] = {
    val result = if (condense)
      Operations.condense(fingerings(chord, span, jazzVoicing), span)
    else
      fingerings(chord, span, jazzVoicing)

    result.filter{c:FretList => fret.isEmpty || c.contains(fret)}.map { c: FretList => fretlistToJson(c, chord)}
  }

  def frettingToJson(c: String)(implicit tuning: Tuning): List[Any] =
    (if (c.trim.length == tuning.numStrings) {
      c.trim.toList.map {_.toString}
    } else c.split(" ").toList.filter {!_.isEmpty}
      ).map(n => Try {n.toInt}.getOrElse("x"))

  def fretlistToJson(c: FretList, chord: Chord)(implicit tuning: Tuning): Map[String, AnyRef] = {
    Map("frets" -> frettingToJson(c.show),
      "degrees" -> chord.asDegrees(c).show.split(" "),
      "name" -> chord.toString(),
      "notes" -> notes(chord)(c).show.split(" ")
    )
  }

  // todo - normalize/sanitize chord name - strip whitespace, lower case to Caps, minor/min -> m, Major/maj -> M

  def normalize(chord: String): String = {
    val tmp = chord.trim().split(" ").mkString.replaceAll("[m|M]in(or)?", "m").replaceAll("[m|M]aj(or)?", "M")
    tmp.split(",").map(_.capitalize).mkString(",")
  }

  def parseChord(c: String): List[(Chord, Option[Int])] = {
    InputParser(c)
  }
}
