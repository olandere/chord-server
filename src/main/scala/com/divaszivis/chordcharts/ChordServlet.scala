package com.divaszivis.chordcharts

import cats.implicits._
import chord.Operations._
import chord._
import grizzled.slf4j.Logging
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ContentEncodingSupport
import org.scalatra.json._
import org.scalatra.util.RicherString._

import scala.util.Try

class ChordServlet extends ChordserverStack with NativeJsonSupport with Logging with ContentEncodingSupport {

  import ChordServlet._

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    response.setHeader("X-XSS-Protection", "1; mode=block")
    response.setHeader("X-Frame-Options", "DENY")
    response.setHeader("X-Content-Type-Options", "nosniff")
    response.setHeader("Access-Control-Allow-Origin", "*")

	  contentType = formats("json")
  }

  get("/") {
    redirect("/index.html")
  }

  //todo: collapse API - probably no need for /chord and /chordprogression to be distinct
  get("/chord/:span?") {
    val (name, fret, topNote) = (params.get("name") map parseChord).get.head
    val condense = params.getOrElse("condense", "false").toBoolean
    val jazzVoicing = params.getOrElse("jazz", "false").toBoolean
    val span = params.getOrElse("span", "6").toInt
    info(s"/chord/$name/$span")

    if (!name.isValid) halt(400) else {
      val tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
      debug(s"/chord/$name/$span")
      debug(s"tuning: $tuning")
      showFingerings(name, span, fret, topNote, condense, jazzVoicing)
    }
  }

  get("/analyze/:fingering") {
    val fingerings = params.get("fingering").get.split(",").map{_.trim}
    implicit val tuning: Tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
    debug(s"tuning $tuning")
    val result = fingerings.map { f =>
      val (degrees, name, notes) = chords(f)
      if (degrees.nonEmpty) {
        Map("frets" -> frettingToJson(f),
          "degrees" -> degrees.show.split(" ").toList,
          "name" -> name,
          "notes" -> notes.show.split(" ").toList)
      } else Map()
    }.toList.filter(_.nonEmpty)
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
      (for {chord <- chords.map(_._1)
            if chord.isValid
      } yield Map("frets" -> arpeggio(chord), "name" -> chord.toString(), "roots" -> roots(chord.root))))
  }

  get("/scale") {
    val root = Note(params.get("root").get.urlDecode)
    val scaleName = params.get("scale").get.urlDecode
    implicit val tuning: Tuning = params.get("tuning").map(t => TuningParser(t)).getOrElse(Tuning.StandardTuning)
    val scale = Scale(root, scaleName)
    Map("arpeggioList" ->
    List(Map("frets" -> scaleFingering(root, scale.semitones),
      "name" -> s"$scale", "roots" -> roots(root))))
  }

  get("/supportedScales") {
    Scale.supportedScales
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
        "chordList" -> showFingerings(chords.head._1, span, chords.head._2, chords.head._3, condense, jazzVoicing))
    } else {
      val chordList = chords.map {
        _._1
      }
      Map("numChords" -> chords.size,
        "chordList" -> progression(chordList, span, jazzVoicing).flatten.zip((Stream continually chords).flatten).
          map { case (f, (c, fret, topNote)) => fretlistToJson(applyFilters(c, List(f), fret, topNote).head, c)}.grouped(chords.size).toList)
    }
  }

  def showFingerings(chord: Chord, span: Int, fret: Option[Int], topNote: Option[Note], condense: Boolean, jazzVoicing: Boolean)
                    (implicit tuning: Tuning): List[Map[String, AnyRef]] = {

    val result = if (condense)
      Operations.condense(fingerings(chord, span, jazzVoicing), span)
    else
      fingerings(chord, span, jazzVoicing)

    applyFilters(chord, result, fret, topNote).map { c: FretList => fretlistToJson(c, chord)}
  }

  def applyFilters(chord: Chord, fingerings: List[FretList], fret: Option[Int], topNote: Option[Note]): List[FretList] = {

    def highestNote(c:FretList): Note = {
      notes(chord)(c).filter(_.isDefined).last.get
    }

    fingerings.filter{c:FretList => (fret.isEmpty || c.contains(fret)) &&
      (topNote.isEmpty || topNote.contains(highestNote(c)))}
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

  def parseChord(c: String): List[(Chord, Option[Int], Option[Note])] = {
    InputParser(c)
  }
}
