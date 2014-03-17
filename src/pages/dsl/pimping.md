---
layout: page
title: Pimp My Type Class
---

It is quite common to combine the "pimping" pattern and the type class pattern. Both pimp-my-library and type classes select adapters from a pool using implicits -- pimp-my-library uses implicit conversions rather than implicit parameters. We can combine the two to select an operation based on both the method receiver and the desired output type, which gives a great deal of flexibility.

Let's expand the HTML rendering example to use both pimping and type classes. Our goal is to write a generic `write` method where we can select the output format based on a type. That is, we want to be able to write code like:

~~~ scala
aPerson.write[String]  // Write the person as a String
aPersom.write[NodeSeq] // Write the person as XML
aDate.write[String]    // Write the date as a String
aDate.write[NodeSeq]   // Write the date as XML
~~~

By controlling which implicits are in scope we can change both the available output types and the format of the output.

This construction is quite intricate. To get started we need to define a `Writer` trait, which has the `write` method we'll use.

~~~ scala
trait Writer[In,Out] {
  def write(in: In): Out
}
~~~

Now we'll define implicit conversions to add a `write` method to our target classes, in this case `Person` and `Date`. This `write` method uses the type class pattern to select a `Writer`.

~~~ scala
case class WriteW[A](data: A) {
  def write[T](implicit writer: Writer[A,T]) = writer.write(data)
}

trait WriteWImplicits {
  implicit def personToWriteW(in: Person): WriteW[Person] = WriteW(in)
  implicit def dateToWriteW(in: Date): WriteW[Date] = WriteW(in)
}
~~~

Finally we need to add some `Writer` implicits to implement the type class side of the pattern.

~~~ scala
trait WriterImplicits {
  implicit object PersonStringWriter extends Writer[Person,String] {
    def write(in: Person): String = in.toString
  }

  implicit object PersonXmlWriter extends Writer[Person,NodeSeq] {
    def write(in: Person): NodeSeq = <span>{ in.name } &lt;{ in.email }&gt;</span>
  }

  implicit object DateStringWriter extends Writer[Date,String] {
    def write(in: Date): String = in.toString
  }

  implicit object DateXmlWrite extends Writer[Date,NodeSeq] {
    def write(in: Date): NodeSeq = <span>{ in.toString }</span>
  }
}
~~~

Finally it's good practice to add object versions of our implicits. If nothing else this lets us run the code from the REPL!

~~~ scala
object WriteWImplicits extends WriteWImplicits
object WriterImplicits extends WriterImplicits
~~~

Now we can test the code at the REPL to see it works.

~~~ scala
scala> import WriteWImplicits._
import WriterImplicits._
val me  = Person("Noel", "noel@_")
val now = new Date()
me.write[String]
me.write[NodeSeq]
now.write[String]
now.write[NodeSeq]

import WriteWImplicits._

scala> import WriterImplicits._

scala> me: Person = Person(Noel,noel@_)

scala> now: java.util.Date = Wed Sep 26 21:24:45 BST 2012

scala> res0: String = Person(Noel,noel@_)

scala> res1: scala.xml.NodeSeq = <span>Noel &lt;noel@_&gt;</span>

scala> res2: String = Wed Sep 26 21:24:45 BST 2012

scala> res3: scala.xml.NodeSeq = <span>Wed Sep 26 21:24:45 BST 2012</span>
~~~

If we wanted to make available different output formats available we can simply import different implicits into scope. So, for example, if we wanted to render people with obsfuscated email addresses we could simply define new `Writer`s for `Person` and use them. All in all this is a very flexible system.

Here's the complete code:

~~~ scala
import scala.xml._
import java.util.Date

case class Person(val name: String, val email: String)

trait Writer[In,Out] {
  def write(in: In): Out
}

case class WriteW[A](data: A) {
  def write[T](implicit writer: Writer[A,T]) = writer.write(data)
}

trait WriteWImplicits {
  implicit def personToWriteW(in: Person): WriteW[Person] = WriteW(in)
  implicit def dateToWriteW(in: Date): WriteW[Date] = WriteW(in)
}

trait WriterImplicits {
  implicit object PersonStringWriter extends Writer[Person,String] {
    def write(in: Person): String = in.toString
  }

  implicit object PersonXmlWriter extends Writer[Person,NodeSeq] {
    def write(in: Person): NodeSeq = <span>{ in.name } &lt;{ in.email }&gt;</span>
  }

  implicit object DateStringWriter extends Writer[Date,String] {
    def write(in: Date): String = in.toString
  }

  implicit object DateXmlWrite extends Writer[Date,NodeSeq] {
    def write(in: Date): NodeSeq = <span>{ in.toString }</span>
  }
}

object WriteWImplicits extends WriteWImplicits
object WriterImplicits extends WriterImplicits

import WriteWImplicits._
import WriterImplicits._
val me  = Person("Noel", "noel@_")
val now = new Date()
me.write[String]
me.write[NodeSeq]
now.write[String]
now.write[NodeSeq]
~~~
