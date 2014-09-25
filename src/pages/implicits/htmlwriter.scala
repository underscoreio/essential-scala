case class Person(name: String, email: String)

trait HtmlWriter[T] {
  def write(in: T): String
}

object PersonWriter extends HtmlWriter[Person] {
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
}

object HtmlUtil {
  def htmlify[T](data: T)(implicit writer: HtmlWriter[T]): String = {
    writer.write(data)
  }
}

implicit object ApproximationWriter extends HtmlWriter[Int] {
  def write(in: Int): String =
    s"It's definitely less than ${((in / 10) + 1) * 10}"
}

HtmlUtil.htmlify(2)
