package com.divaszivis.chordcharts

import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest.ScalatraSuite

class ChordServletSpec extends ScalatraSuite with FunSuiteLike {

  addServlet(classOf[ChordServlet], "/*")

  test("GET /analyze on ChordServlet") {
//    get("/chord/Am7/") {
//      status should equal(200)
//    }
//
//    get("/chord/Am7@5/4") {
//      status should equal(200)
//    }

    get("/analyze/xx3004?chord=xx3004&tuning=C%23+G%23+C%23+D%23+C%23+E") {
      status shouldBe 200
    }

    get("/analyze/xx0232?chord=xx0232&condense=false&tuning=EADGBE") {
      status shouldBe 200
      println(s"body: $body")
    }

  }

  test("GET A5") {
    get("/chords/4?chord=A5") {
      status should equal(200)
    }
  }

  test("GET e,a") {
    get("/chords/4?chord=e%252Ca&condense=false&tuning=dadgaf") {
      status should equal(200)
    }
  }

  test("GET Cdim7") {
    get("/chords/4?chord=Cdim7") {
      status should equal(200)
    }
  }

  test("GET C77") {
    get("/chords/4?chord=C77") {
      status should equal(200)
    }
  }

  test("GET C77 arpeggio") {
    get("/arpeggio?chord=C77") {
      status should equal(200)
    }
  }

}
