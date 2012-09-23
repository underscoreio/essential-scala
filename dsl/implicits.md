---
layout: page
---

# Open Extension of Classes

Multiple parameter lists

implicits parameters

Example: RichInt

extending a class

implicit resolution order

Rules for implicits
[23/09/2012 20:08:33] Dave Gurnell: Marking Rule: Only definitions marked implicit are available.
[23/09/2012 20:08:39] Dave Gurnell: Scope Rule: An inserted implicit conversion must be in scope as a single identifier, or be associated with the source or target type of the conver- sion.
[23/09/2012 20:08:51] Dave Gurnell: Non-Ambiguity Rule: An implicit conversion is only inserted if there is no other possible conversion to insert.
[23/09/2012 20:08:57] Dave Gurnell: One-at-a-time Rule: Only one implicit is tried.
[23/09/2012 20:09:05] Dave Gurnell: Explicits-First Rule: Whenever code type checks as it is written, no implicits are attempted.

Must be in scope as a single identifier (i.e. not a.b)
[23/09/2012 20:12:02] Dave Gurnell: Except...
[23/09/2012 20:12:14] Dave Gurnell: The compiler will look for definitions in the companion object.
[23/09/2012 20:12:25] Dave Gurnell: ...
[23/09/2012 20:12:28] Dave Gurnell: sorry
[23/09/2012 20:12:43] Dave Gurnell: ...in the companion objects for the source and target types for conversion

explicit implicits

## Exercise

Extend `Int` with a method called `times` that executes the body `n` times, where `n` is the `Int`. Bonus marks for using a call-by-name parameter.




## Type classes

*Type classes* are a programming idiom borrowed from the Haskell programming language. They provide a neat way to add functionality to existing classes without changing the classes themselves. This is useful for a couple of reasons:

 - We may not have access to the source for the original classes.
 - We may want to implement the same functionality in different ways in different parts of our application.

Broadly speaking, a type class in Scala involves three things:

 1. a trait, specifying some kind of desirable functionality;
 2. a set of adapters, implementing the functionality for various data types;
 3. an automatic way of selecting an appropriate adapter using implicits.

Let's motivate this with an example: converting data to HTML. This is a fundamental operation in any web application, and it would be great to be able to provide a `toHtml` method across the board in our application.

Let's start by naively implementing `toHtml` using a simple trait:

{% highlight scala %}
scala> import scala.xml._

scala> trait HtmlWriteable {
     |   def toHtml: NodeSeq
     | }
defined trait HtmlWriteable

scala> class Person(var name: String, var email: String) extends HtmlWriteable {
     |   def toHtml = <span>{ name } &lt;{ email }&gt;</span>
     | }
defined class Person

scala> new Person("John", "john@example.com").toHtml
res2: scala.xml.Elem = <span>John &lt;john@example.com&gt;</span>
{% endhighlight %}

This solution has a number of drawbacks. First, we are restricted to having just one way of rendering a `Person`. If we want to list people on our company homepage, for example, it is unlikely we will want to list everybody's email addresses without any kind of obfuscation. Second, this pattern can only be applied to classes that we have written ourselves. If we want to render `java.util.Data` to HTML, for example, we will have to write some other form of library function.

We can overcome both of these problems by moving our HTML rendering function to an adapter class:

{% highlight scala %}
scala> import java.util.Date
import java.util.Date

scala> trait HtmlWriter[T] {
     |   def write(in: T): NodeSeq
     | }
defined trait HtmlWriter

scala> object PersonWriter extends HtmlWriter[Person] {
     |   def write(in: Person) = <span>{ in.name } &lt;{ in.email }&gt;</span>
     | }
defined module PersonWriter

scala> object DateWriter extends HtmlWriter[Date] {
     |   def write(in: Date) = <span>{ in.toString }</span>
     | }
defined module DateWriter

scala> PersonWriter.write(new Person("John", "john@example.com"))
res3: scala.xml.Elem = <span>John &lt;john@example.com&gt;</span>
{% endhighlight %}

Now we can render any type of data in any way we want by simply choosing a different adapter for any given situation. However, there are still problems with the code. Whenever we want to render an object, we have to write out the name of a relevant `HtmlWriter`. This makes our code more verbose and difficult to maintain than the `toHtml` version we had above.

The type class pattern gives us a way of reducing the code overhead of the adapter approach. Let's introduce a simple example now. We know that we can convert anything to HTML as long as we have a compatible `HtmlWriter` in scope. Let's write this out in code:

{% highlight scala %}
scala> object HtmlUtil {
     |   def htmlify[T](data: T)(implicit writer: HtmlWriter[T]): NodeSeq = {
     |     writer.write(data)
     |   }
     | }
defined module HtmlUtil
{% endhighlight %}

The `htmlify` method accepts two arguments: some `data` to convert to HTML and a `writer` to do the conversion. The `writer` is an implicit argument, so the compiler will automatically use any compatible `HtmlWriter` that is in implicit scope. Let's redefine our adapters so we can bring them into scope:

{% highlight scala %}
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait HtmlWriters {
  implicit object PersonWriter extends HtmlWriter[Person] {
    def write(in: Person) = <span>{ in.name } &lt;{ in.email }&gt;</span>
  }
  implicit object DateWriter extends HtmlWriter[Date] {
    def write(in: Date) = <span>{ in.toString }</span>
  }
}

object HtmlWriters extends HtmlWriters

// Exiting paste mode, now interpreting.

defined trait HtmlWriters
defined object HtmlWriters
{% endhighlight %}

This is the idiomatic way of defining a set of adapters for use in a type class: we define everything in a trait, and then instantiate that trait as a singleton of the same name. The singleton gives us direct access to the definitions via an `import` statement, while the trait allows us to replace any adapter with anoter variant to create a custom set.

We can now use our adapters with `htmlify`:

{% highlight scala %}
scala> import HtmlUtil._
import HtmlUtil._

scala> import HtmlWriters._
import HtmlWriters._

scala> htmlify(new Person("John", "john@example.com"))
res4: scala.xml.NodeSeq = <span>John &lt;john@example.com&gt;</span>
{% endhighlight %}

This final example has much lighter syntax requirements than its predecessor. We have now assembled the complete type class pattern: `HtmlUtil` specifies our HTML rendering functionality, `HtmlWriter` and `HtmlWriters` implement the functionality as a set of adapters, and the implicit argument to `htmlify` implicitly selects the correct adapter for any given argument.

### Pimp-my-library

*Pimp-my-library* is an associated pattern that uses implicit conversions to make it look as if we can "add" new methods to existing classes. Both pimp-my-library and type classes select adapters from a pool using implicits -- pimp-my-library uses implicit conversions rather than implicit parameters. Let's re-implement the HTML rendering example using implicit conversions:

{% highlight scala %}
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait HtmlWriter {
  def toHtml: NodeSeq
}

trait HtmlWriters {
  implicit def wrapPerson(in: Person) = new HtmlWriter {
    def toHtml = <span>{ in.name } &lt;{ in.email }&gt;</span>
  }

  implicit def wrapDate(in: Date) = new HtmlWriter {
    def toHtml = <span>{ in.toString }</span>
  }
}

object HtmlWriters extends HtmlWriters

import HtmlWriters._

// Exiting paste mode, now interpreting.

defined trait HtmlWriter
defined trait HtmlWriters
defined module HtmlWriters
import HtmlWriters._

scala> new Person("John", "john@example.com").toHtml
res5: scala.xml.Elem = <span>John &lt;john@example.com&gt;</span>
{% endhighlight %}

The example uses the methods of `HtmlWriters` to convert data into instances of `HtmlWriter`. Because the convertor methods are defined as `implicit`, the compiler attempts to use one to satisfy any type error that arises when we wite `foo.toHtml`.

The *explicits first* rule of implicit resolution will ensure that implicit conversions are not used if `foo` has its own `toHtml` method. In any other situation, the compiler searches for a conversion from `foo` to an `HtmlWriter` to supply `toHtml`.
