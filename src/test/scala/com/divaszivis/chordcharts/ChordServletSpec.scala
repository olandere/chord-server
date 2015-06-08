package com.divaszivis.chordcharts

import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest.ScalatraSuite

class ChordServletSpec extends ScalatraSuite with FunSuiteLike {

  addServlet(classOf[ChordServlet], "/*")

  test("GET /chord on ChordServlet") {
    get("/chord/Am7/") {
      status should equal(200)
    }

    get("/chord/Am7@5/4") {
      status should equal(200)
    }
  }


}
