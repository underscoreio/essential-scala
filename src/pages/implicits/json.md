## JSON Serialisation

In this section we have an extended example involving serializing Scala data to JSON, which is one of the classic use cases for type classes. The typical process for converting data to JSON in Scala involves two steps. First we convert our data types to an intermediate case class representation, then we serialize the intermediate representation to a string.

Here is a suitable case class representation of a subset of the JSON language. We have a `sealed trait JsValue` that defines a `stringify` method, and a set of subtypes for two of the main JSON data types---objects and strings:

```tut:book:silent
sealed trait JsValue {
  def stringify: String
}

final case class JsObject(values: Map[String, JsValue]) extends JsValue {
  def stringify = values
    .map { case (name, value) => "\"" + name + "\":" + value.stringify }
    .mkString("{", ",", "}")
}

final case class JsString(value: String) extends JsValue {
  def stringify = "\"" + value.replaceAll("\\|\"", "\\\\$1") + "\""
}
```

You should recognise this as the algebraic data type pattern.

We can construct JSON objects and serialize them as follows:

```tut:book
val obj = JsObject(Map("foo" -> JsString("a"), "bar" -> JsString("b"), "baz" -> JsString("c")))

obj.stringify
```

### Convert X to JSON

Let's create a type class for converting Scala data to JSON. Implement a `JsWriter` trait containing a single abstract method `write` that converts a value to a `JsValue`.

<div class="solution">
The *type class* is generic in a type `A`. The `write` method converts a value of type `A` to some kind of `JsValue`.

```tut:book:silent
trait JsWriter[A] {
  def write(value: A): JsValue
}
```
</div>

Now let's create the dispatch part of our type class. Write a `JsUtil` object containing a single method `toJson`. The method should accept a value of an arbitrary type `A` and convert it to JSON.

Tip: your method will have to accept an implicit `JsWriter` to do the actual conversion.

<div class="solution">
```tut:book:silent
object JsUtil {
  def toJson[A](value: A)(implicit writer: JsWriter[A]) =
    writer write value
}
```
</div>

Now, let's revisit our data types from the web site visitors example in the [Sealed traits](/traits/sealed-traits.html) section:

```tut:book:silent
import java.util.Date

sealed trait Visitor {
  def id: String
  def createdAt: Date
  def age: Long = new Date().getTime() - createdAt.getTime()
}

final case class Anonymous(
  val id: String,
  val createdAt: Date = new Date()
) extends Visitor

final case class User(
  val id: String,
  val email: String,
  val createdAt: Date = new Date()
) extends Visitor
```

Write `JsWriter` instances for `Anonymous` and `User`.

<div class="solution">
```tut:book:silent
implicit object AnonymousWriter extends JsWriter[Anonymous] {
  def write(value: Anonymous) = JsObject(Map(
    "id"           -> JsString(value.id),
    "createdAt"    -> JsString(value.createdAt.toString)
  ))
}

implicit object UserWriter extends JsWriter[User] {
  def write(value: User) = JsObject(Map(
    "id"           -> JsString(value.id),
    "email"        -> JsString(value.email),
    "createdAt"    -> JsString(value.createdAt.toString)
  ))
}
```
</div>

Given these two definitions we can implement a `JsWriter` for `Visitor` as follows. This uses a new type of pattern -- `a: B` -- which matches any value of type `B` and binds it to a variable `a`:

```tut:book:silent
implicit object VisitorWriter extends JsWriter[Visitor] {
  def write(value: Visitor) = value match {
    case anon: Anonymous => JsUtil.toJson(anon)
    case user: User      => JsUtil.toJson(user)
  }
}
```

Finally, verify that your code works by converting the following list of users to JSON:

```tut:book:silent
val visitors: Seq[Visitor] = Seq(Anonymous("001", new Date), User("003", "dave@xample.com", new Date))
```

<div class="solution">
```tut:book:silent
visitors.map(visitor => JsUtil.toJson(visitor))
```
</div>

### Prettier Conversion Syntax

Let's improve our JSON syntax by combining type classes and type enrichment. Convert `JsUtil` to an `implicit class` with a `toJson` method. Sample usage:

```scala
Anonymous("001", new Date).toJson
```

<div class="solution">
```tut:book:silent
implicit class JsUtil[A](value: A) {
  def toJson(implicit writer: JsWriter[A]) =
    writer write value
}
```

In the previous exercise we only defined `JsWriters` for our main case classes. With this convenient syntax, it makes sense for us to have an complete set of `JsWriters` for all the serializable types in our codebase, including `Strings` and `Dates`:

```tut:book:silent
implicit object StringWriter extends JsWriter[String] {
  def write(value: String) = JsString(value)
}

implicit object DateWriter extends JsWriter[Date] {
  def write(value: Date) = JsString(value.toString)
}
```

With these definitions we can simplify our existing `JsWriters` for `Anonymous`, `User`, and `Visitor`:

```tut:invisible
// I must repeat this here for some reason or the implicit resolution in the block below fails
implicit class JsUtil[A](value: A) {
  def toJson(implicit writer: JsWriter[A]) =
    writer write value
}
```

```tut:book:silent
implicit object AnonymousWriter extends JsWriter[Anonymous] {
  def write(value: Anonymous) = JsObject(Map(
    "id"        -> value.id.toJson,
    "createdAt" -> value.createdAt.toJson
  ))
}

implicit object UserWriter extends JsWriter[User] {
  def write(value: User) = JsObject(Map(
    "id"        -> value.id.toJson,
    "email"     -> value.email.toJson,
    "createdAt" -> value.createdAt.toJson
  ))
}

implicit object VisitorWriter extends JsWriter[Visitor] {
  def write(value: Visitor) = value match {
    case anon: Anonymous => anon.toJson
    case user: User      => user.toJson
  }
}
```
</div>
