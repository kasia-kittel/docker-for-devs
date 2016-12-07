package controllers


import org.joda.time.LocalDate
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.scalatest.{Matchers, WordSpec}

class ControllerUtilsSpec extends WordSpec with Matchers{

  "convertDateForYLN" should {

    "convert LocalDate type to string with correct formatting" in {
      val date = LocalDate.parse("2011-12-03",  DateTimeFormat.forPattern("yyyy-MM-dd"))
      ControllerUtils.convertDateForYLN(date) should be ("03-Dec-11")
    }
  }

}
