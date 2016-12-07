package controllers

import models.Somebody
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.PlaySpec

class QuoteControllerSpec extends PlaySpec with PropertyChecks {

  "calculateKarma" should {
    "return number between 0 and 9" in {

      forAll { (name: String, age:Int) =>
          val s = Somebody(name, Some(age))
          val k = Utils.calculateKarma(s)

          k must be > 0
          k must be < 10
      }
    }
  }

}
