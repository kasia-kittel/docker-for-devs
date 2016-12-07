package controllers


import java.util.Base64

import org.joda.time.{LocalDate, Years}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsValue

trait ControllerUtils {

  def encode(str: String) = Base64.getEncoder.encodeToString(str.getBytes("UTF-8"))

  def encode(json: JsValue): String = encode(json.toString())

  def decode(str: String): String = new String(Base64.getDecoder.decode(str), "UTF-8")

  def convertDateForYLN(date: LocalDate):String =
    date.toString(DateTimeFormat.forPattern("dd-MMM-yy"))

  def calculateAge(dateOfBirth: LocalDate) =
    Years.yearsBetween(dateOfBirth, LocalDate.now()).getYears

}

object ControllerUtils extends ControllerUtils