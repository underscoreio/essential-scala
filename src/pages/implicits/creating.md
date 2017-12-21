## Creating Type Classes

In the previous sections we saw how to create and use type class instances. Now we're going to explore creating our own type classes.

### Elements of Type Classes

There are four components of the type class pattern:

- the actual type class itself;
- the type class instances;
- interfaces using implicit parameters; and
- interfaces using enrichment and implicit parameters.

We have already seen type class instances and talked briefly about implicit parameters. Here we will look at defining our own type class, and in the following section we will look at the two styles of interface.

### Creating a Type Class

Let's start with an example---converting data to HTML. This is a fundamental operation in any web application, and it would be great to be able to provide a `toHtml` method across the board in our application.

One implementation strategy is to create a trait we `extend` wherever we want this functionality:

```tut:book:silent
trait HtmlWriteable {
  def toHtml: String
}

final case class Person(name: String, email: String) extends HtmlWriteable {
  def toHtml = s"<span>$name &lt;$email&gt;</span>"
}
```

```tut:book
Person("John", "john@example.com").toHtml
```

This solution has a number of drawbacks. First, we are restricted to having just one way of rendering a `Person`. If we want to list people on our company homepage, for example, it is unlikely we will want to list everybody's email addresses without obfuscation. For logged in users, however, we probably want the convenience of direct email links. Second, this pattern can only be applied to classes that we have written ourselves. If we want to render a `java.util.Date` to HTML, for example, we will have to write some other form of library function.

Polymorphism has failed us, so perhaps we should try pattern matching instead? We could write something like

```tut:invisible
import java.util.Date
```

```tut:book:silent
object HtmlWriter {
  def write(in: Any): String =
    in match {
      case Person(name, email) => ???
      case d: Date => ???
      case _ => throw new Exception(s"Can't render ${in} to HTML")
    }
}
```

This implementation has its own issues. We have lost type safety because there is no useful supertype that covers just the elements we want to render and no more. We can't have more than one implementation of rendering for a given type. We also have to modify this code whenever we want to render a new type.

We can overcome all of these problems by moving our HTML rendering to an adapter class:

```tut:book:silent
trait HtmlWriter[A] {
  def write(in: A): String
}

object PersonWriter extends HtmlWriter[Person] {
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
}
```

```tut:book
PersonWriter.write(Person("John", "john@example.com"))
```

This is better. We can now define `HtmlWriter` functionality for other types, including types we have not written ourselves:

```tut:book:silent
import java.util.Date

object DateWriter extends HtmlWriter[Date] {
  def write(in: Date) = s"<span>${in.toString}</span>"
}
```

```tut:book
DateWriter.write(new Date)
```

We can also write another `HtmlWriter` for writing `People` on our homepage:

```tut:book:silent
object ObfuscatedPersonWriter extends HtmlWriter[Person] {
  def write(person: Person) =
    s"<span>${person.name} (${person.email.replaceAll("@", " at ")})</span>"
}
```

```tut:book
ObfuscatedPersonWriter.write(Person("John", "john@example.com"))
```

Much safer---it'll take a spam bot more than a few microseconds to decypher that!

You might recognise `PersonWriter`, `DateWriter`, and `ObfuscatedPersonWriter` as following the type class instance pattern (though we haven't made them implicit values at this point). The `HtmlWriter` trait, which the instances implement, is the type class itself.

<div class="callout callout-info">
#### Type Class Pattern {-}

A type class is a trait with at least one type variable. The type variables specify the concrete types the type class instances are defined for. Methods in the trait usually use the type variables.

```tut:invisible
trait Foo
```

```tut:book:silent
trait ExampleTypeClass[A] {
  def doSomething(in: A): Foo
}
```
</div>

The next step is to introduce implicit parameters, so we can use type classes with less boilerplate.

### Take Home Points

We have seen the basic pattern for implementing type classes.

- We declare some interface for the functionality we want

```tut:book:silent
trait HtmlWriter[A] {
  def toHtml(in: A): String
}
```

- We write type class instances for each concrete class we want to use and for each different situation we want to use it in

```tut:book:silent
object PersonWriter extends HtmlWriter[Person] {
  def toHtml(person: Person) =
    s"${person.name} (${person.email})"
}

object ObfuscatedPersonWriter extends HtmlWriter[Person] {
  def toHtml(person: Person) =
    s"${person.name} (${person.email.replaceAll("@", " at ")})"
}
```
- This allows us to implement the functionality for any type, and to provide different implementations for the same type.

### Exercises

#### Equality

Scala provides two equality predicates: by value (`==`) and by reference (`eq`). Nonetheless, we sometimes need additional predicates. For instance, we could compare people by just email address if we were validating new user accounts in some web application.

Implement a trait `Equal` of some type `A`, with a method `equal` that compares two values of type `A` and returns a `Boolean`. `Equal` is a type class.

<div class="solution">
```tut:book:silent
trait Equal[A] {
  def equal(v1: A, v2: A): Boolean
}
```
</div>

Our `Person` class is

```tut:book:silent
case class Person(name: String, email: String)
```

Implement instances of `Equal` that compare for equality by email address only, and by name and email.

<div class="solution">
```tut:book:silent
object EmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email
}

object NameEmailEqual extends Equal[Person] {
  def equal(v1: Person, v2: Person): Boolean =
    v1.email == v2.email && v1.name == v2.name
}
```
</div>
