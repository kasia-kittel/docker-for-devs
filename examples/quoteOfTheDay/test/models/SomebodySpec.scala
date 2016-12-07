package models

import org.scalatestplus.play.PlaySpec

class SomebodySpec extends PlaySpec {

  "somebodyBindable" should {
    "create Somebody from query string having only name" in new QueryStringSetup {
      val bindResult: Option[Either[String, Somebody]] = Somebody.somebodyBindable.bind("", paramsWithoutAge)

      bindResult must not be None
      bindResult.get must be('right)

      val bindSomebody = bindResult.get.right.get

      bindSomebody.age must be (None)
      bindSomebody.name must be (name)
    }

    "create Somebody from query string having name and age" in new QueryStringSetup {
      val bindResult: Option[Either[String, Somebody]] = Somebody.somebodyBindable.bind("", paramsWithAge)

      bindResult must not be None
      bindResult.get must be('right)

      val bindSomebody = bindResult.get.right.get

      bindSomebody.age must be (Some(age))
      bindSomebody.name must be (name)
    }

    "create query string from Somebody having name" in new SomebodySetup {
      val queryString =  Somebody.somebodyBindable.unbind("", somebodyWithoutAge)

      queryString must be (s"name=${somebodyWithoutAge.name}")
    }

    "create query string from Somebody having name and age" in new SomebodySetup {
      val queryString =  Somebody.somebodyBindable.unbind("", somebodyWithAge)

      queryString must be (s"name=${somebodyWithAge.name}&age=${somebodyWithAge.age.get.toString}")
    }
  }

  val name = "SomeName"
  val age = 35

  class QueryStringSetup {
    val paramsWithoutAge: Map[String, Seq[String]] = Map("name" -> Seq(name))
    val paramsWithAge: Map[String, Seq[String]] = Map("name" -> Seq(name), "age" -> Seq(age.toString))
  }

  class SomebodySetup {
    val somebodyWithoutAge = new Somebody(name, None)
    val somebodyWithAge = new Somebody(name, Some(age))
  }

}
