## Implicit Parameter and Interfaces

We've seen the basics of the type class pattern. Now let's look at how we can make it easier to use. Recall our starting point is a trait `HtmlWriter` which allows us to implement HTML rendering for classes without requiring access to their source code, and allows us to render the same class in different ways.

```tut:invisible
case class Person(name: String, email: String)
```

```tut:book:silent
trait HtmlWriter[A] {
  def write(in: A): String
}

object PersonWriter extends HtmlWriter[Person] {
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
}
```

This issue with this code is that we need to manage a lot of `HtmlWriter` instances when we render any complex data. We have already seen that we can manage this complexity using implicit values when we mentioned about *implicit parameters* in passing. In this section we go in depth on implicit parameters.

### Implicit Parameter Lists

Here is an example of an implicit parameter list:

```tut:book:silent
object HtmlUtil {
  def htmlify[A](data: A)(implicit writer: HtmlWriter[A]): String = {
    writer.write(data)
  }
}
```

The `htmlify` method accepts two arguments: some `data` to convert to HTML and a `writer` to do the conversion. The `writer` is an implicit parameter.

The `implicit` keyword applies to the *whole parameter list*, not just an individual parameter. This makes the parameter list optional---when we call `HtmlUtil.htmlify` we can either specify the list as normal

```tut:book
HtmlUtil.htmlify(Person("John", "john@example.com"))(PersonWriter)
```

or we can omit the implicit parameters. If we omit the implicit parameters, the compiler searches for implicit values of the correct type it can use to fill in the missing arguments. We have already learned about implicit values, but let's see a quick example to refresh our memory. First we define an implicit value.

```tut:book:silent
implicit object ApproximationWriter extends HtmlWriter[Int] {
  def write(in: Int): String =
    s"It's definitely less than ${((in / 10) + 1) * 10}"
}
```

When we use `HtmlUtil` we don't have to specify the implicit parameter if an implicit value can be found.

```tut:book:silent
HtmlUtil.htmlify(2)
```

### Interfaces Using Implicit Parameters

A complete use of the type class pattern requires an interface using implicit parameters, along with implicit type class instances. We've seen two examples already: the `sorted` method using `Ordering`, and the `htmlify` method above. The best interface depends on the problem being solved, but there is a pattern that occurs frequently enough that it is worth explaining here.

In many case the interface defined by the type class is the same interface we want to use. This is the case for `HtmlWriter` -- the only method of interest is `write`. We could write something like

```tut:book:silent
object HtmlWriter {
  def write[A](in: A)(implicit writer: HtmlWriter[A]): String =
    writer.write(in)
}
```

We can avoid this indirection (which becomes more painful to write as our interfaces become larger) with the following construction:

```tut:book:silent
object HtmlWriter {
  def apply[A](implicit writer: HtmlWriter[A]): HtmlWriter[A] =
    writer
}
```

In use it looks like

```tut:invisible
implicit object PersonWriter extends HtmlWriter[Person] {
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
}
```

```tut:book:silent
HtmlWriter[Person].write(Person("Noel", "noel@example.org"))
```

The idea is to simply select a type class instance by type (done by the no-argument `apply` method) and then directly call the methods defined on that instance.

<div class="callout callout-info">
#### Type Class Interface Pattern {-}

If the desired interface to a type class `TypeClass` is exactly the methods defined on the type class trait, define an interface on the companion object using a no-argument `apply` method like

```tut:invisible
trait TypeClass[A]
```

```tut:book:silent
object TypeClass {
  def apply[A](implicit instance: TypeClass[A]): TypeClass[A] =
    instance
}
```
</div>

### Take Home Points

Implicit parameters make type classes more convenient to use. We can make an entire parameter list using the `implicit` keyword. To make it an implicit parameter list we write as

```scala
def method[A](normalParam1: NormalType, ...)(implicit implicitParam1: ImplicitType[A], ...)
```

If we call a method and do not explicitly supply its implicit parameter list, the compiler will search for implicit values of the correct types to complete the parameter list for us.

Using implicit parameters we can make more convenient interfaces using type class instances. If the desired interface to a type class is exactly the methods defined on the type class we can create a convenient interface using the pattern

```tut:book:silent
object TypeClass {
  def apply[A](implicit instance: TypeClass[A]): TypeClass[A] =
    instance
}
```

### Exercises

#### Equality Again

In the previous section we defined a trait `Equal` along with some implementations for `Person`.

```tut:book:silent
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
```

Implement an object called `Eq` with an `apply` method. This method should accept two explicit parameters of type `A` and an implicit `Equal[A]`. It should perform the equality checking using the provided `Equal`. With appropriate implicits in scope, the following code should work

```scala
Eq(Person("Noel", "noel@example.com"), Person("Noel", "noel@example.com"))
```

<div class="solution">
```tut:book:silent
object Eq {
  def apply[A](v1: A, v2: A)(implicit equal: Equal[A]): Boolean =
    equal.equal(v1, v2)
}
```
</div>

Package up the different `Equal` implementations as implicit values in their own objects, and show you can control the implicit selection by changing which object is imported.

<div class="solution">
```tut:book:silent
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
```
</div>

Now implement an interface on the companion object for `Equal` using the no-argument apply method pattern. The following code should work.

```
import NameAndEmailImplicit._
Equal[Person].equal(Person("Noel", "noel@example.com"), Person("Noel", "noel@example.com"))
```

Which interface style do you prefer?

<div class="solution">
The following code is what we're looking for:

```tut:book:silent
object Equal {
  def apply[A](implicit instance: Equal[A]): Equal[A] =
    instance
}
```

In this case the `Eq` interface is slightly easier to use, as it requires less typing. For most complicated interfaces, with more than a single method, the companion object pattern would be preferred. In the next section we'll see how we can make interfaces that appear to be methods defined on the objects of interest.
</div>
