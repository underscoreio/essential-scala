trait Equal[A] {
  def equal(v1: A, v2: A): Boolean
}
object Equal {
  def apply[A](implicit instance: Equal[A]): Equal[A] =
    instance

  implicit class ToEqual[A](in: A) {
    def ===(other: A)(implicit equal: Equal[A]): Boolean =
      equal.equal(in, other)
  }
}

case class Person(name: String, email: String)

object EmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email
}

object NameEmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email && v1.name == v2.name
}

object Eq {
  def apply[A](v1: A, v2: A)(implicit equal: Equal[A]): Boolean =
    equal.equal(v1, v2)
}

object NameAndEmailImplicit {
  implicit object NameEmailEqual extends Equal[Person] {
    def equal(v1: Person, v2: Person): Boolean =
      v1.email == v2.email && v1.name == v2.name
  }
}

object EmailImplicit {
  implicit object EmailEqual extends Equal[Person] {
    def equal(v1: Person, v2: Person): Boolean =
      v1.email == v2.email
  }
}

object Examples {
  def byNameAndEmail = {
    import NameAndEmailImplicit._
    Eq(Person("Noel", "noel@example.com"), Person("Noel", "noel@example.com"))
  }

  def byEmail = {
    import EmailImplicit._
    Eq(Person("Noel", "noel@example.com"), Person("Dave", "noel@example.com"))
  }

  def companionObjectInterface = {
    import NameAndEmailImplicit._
    Equal[Person].equal(Person("Noel", "noel@example.com"), Person("Noel", "noel@example.com"))
  }
}

object SyntaxExample {
  implicit val caseInsensitiveEquals = new Equal[String] {
    def equal(s1: String, s2: String) =
      s1.toLowerCase == s2.toLowerCase
  }

  import Equal._

  "foo".===("FOO")
}
