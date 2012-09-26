---
layout: page
---

# Pimp My Type Class

It is quite common to combine the "pimping" pattern and the type class pattern. Both pimp-my-library and type classes select adapters from a pool using implicits -- pimp-my-library uses implicit conversions rather than implicit parameters. We can combine the two to select an operation based on both the method receiver and the desired output type, which gives a great deal of flexibility.

Let's expand the HTML rendering example to use both pimping and type classes. Our goal is to write a generic `write` method where we can select the output format based on a type. That is, we want to be able to write code

{% highlight scala %}
aPerson.write[String]  // Write the person as a String
aPersom.write[NodeSeq] // Write the person as XML
{% endhighlight %}

By controlling which implicits are in scope we can change the both the available output types and the format of the output.

This construction is quite intricate. To get started we need to define a `Writer` trait, which has the `write` method that is implemented using a type class.

{% highlight scala %}
trait Writable[In,Out] {
  def write(in: In)(implicit writer: Writer[Out]):Out =
    writer.write(in)
}

{% endhighlight %}

Let's re-implement the HTML rendering example using implicit conversions:

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
