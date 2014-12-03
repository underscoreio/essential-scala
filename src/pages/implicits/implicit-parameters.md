## Implicit Parameters

We've seen the basics of the type class pattern. Now let's look at how we can make it easier to use. Recall our starting point is a trait `HtmlWriter` which allows us to implement HTML rendering for classes without requiring access to their source code, and allows us to render the same class in different ways.

~~~ scala
trait HtmlWriter[T] {
  def write(in: T): String
}

object PersonWriter extends HtmlWriter[Person] {
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
}
~~~

This issue with this code is that we need manage a lot of `HtmlWriter` instances when we render any complex data. Most classes will only have a single instance of `HtmlWriter` but we still need to call the correct object for the class we're rendering. It would be nice it Scala would just pick the right instance for us whenever there is no ambiguity, and that's exactly what we can get Scala to do using **implicit parameters**.

### Implicit Parameter Lists

Here is an example of an implicit parameter list:

~~~ scala
object HtmlUtil {
  def htmlify[T](data: T)(implicit writer: HtmlWriter[T]): String = {
    writer.write(data)
  }
}
~~~

The `htmlify` method accepts two arguments: some `data` to convert to HTML and a `writer` to do the conversion. The `writer` is an implicit parameter.

The `implicit` keyword applies to the *whole parameter list*, not just an individual parameter. This makes the parameter list optional---when we call `HtmlUtil.htmlify` we can either specify the list as normal

~~~ scala
scala> HtmlUtil.htmlify(Person("John", "john@example.com"))(PersonWriter)
res2: String = <span>John &lt;john@example.com&gt;</span>
~~~

or we can omit implicit parameters. The compiler searches for **implicit values** of the correct type it can use to fill in the missing arguments. For example we can declare an implicit value like so:

~~~ scala
implicit object ApproximationWriter extends HtmlWriter[Int] {
  def write(in: Int): String =
    s"It's definitely less than ${((in / 10) + 1) * 10}"
}
~~~

When we use `HtmlUtil` we don't have to specify the implicit value if one can be found.

~~~ scala
scala> HtmlUtil.htmlify(2)
res4: String = It's definitely less than 10
~~~

## Implicit Values

We can tag any `val`, `var`, `object` or zero-argument `def` with the `implicit` keyword, making it a potential candidate for an implicit parameter:

~~~ scala
implicit object PersonWriter extends HtmlWriter[Person] {
  def write(person: Person) =
    s"<span>${person.name} &lt;${person.email}&gt;</span>"
}
~~~

When the compiler expands an implicit argument list, it searches for candidate values for each argument by type. In our `htmlify` method the exact type will be decided by the type parameter `T`---if `T` is `Person`, for example, the compiler searches for a value of type `HtmlWriter[Person]`.

We'll look at the full implicit search rules in the next section. For now, we're going to use the simplest rule, which is that any implicits in the local scope take priority over other implicits. A simple way of packaging implicits is to declare them in an object

~~~ scala
object PersonImplicits {
  implicit object PersonWriter extends HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"
  }
}
~~~

We can then import this object into the scope where we'd like the implicits available.

~~~ scala
object PersonImplicitsExample {
  import PersonImplicits._ // Import everything from PersonImplicits
  HtmlUtil.htmlify(Person("Noel", "noel@underscoreconsulting.com")) // Implicits used here
}
~~~

### Take Home Points

Implicit parameters make type classes more convenient to use. We can make an entire parameter list with the `implicit` keyword to make it an implicit parameter list.

~~~ scala
def method[A](normalParam1: NormalType, ...)(implicit implicitParam1: ImplicitType[A], ...)
~~~

If we call a method and do not explicitly supply an explicit parameter, the compiler will search for an implicit value of the correct type and insert it as the parameter.

An implicit value is one declared with the `implicit` keyword. Although we can declare implicits directly in the console, in real Scala code they must be declared in a trait, class, or object.

The Scala compiler prefers implicit values in the local scope to any other implicit values. One simple way to package implicits is to declare them in an object and then import that object into the scope where it is needed.

~~~ scala
object Implicits {
  implicit object anImplicit = ...
}

// Meanwhile ...
trait ImplicitUse {
  import Implicits._
  ...
}
~~~

### Exercises

#### Equality

In the previous section we defined a trait `Equal` along with some implementations for `Person`.

~~~ scala
case class Person(name: String, email: String)

trait Equal[A] {
  def equal(v1: A, v2: A): Boolean
}

object EmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email
}

object NameEmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email && v1.name == v2.name
}
~~~

Implement an object called `Eq` with an `apply` method. This method should accept two explicit parameters of type `A` and an implicit `Equal[A]`. It should perform the equality checking using the provided `Equal`.

<div class="solution">
~~~ scala
object Eq {
  def apply[A](v1: A, v2: A)(implicit equal: Equal[A]): Boolean =
    equal.equal(v1, v2)
}
~~~
</div>

Package up the different `Equal` implementations as implicit values in their own objects, and show you can control the implicit selection by changing which object is imported.

<div class="solution">
~~~ scala
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
}
~~~
</div>

Hopefully you'll agree that adding the extra machinary has made our type class more pleasant to use.
