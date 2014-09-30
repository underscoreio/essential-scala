---
layout: page
title: Type Enrichment
---

Type enrichment, often referred to colloquially as *"pimping"*, allows us to **augment existing classes with extra functionality**. For example, suppose we have a method called `numberOfVowels`:

~~~ scala
scala> def numberOfVowels(str: String) =
     |   str.filter(Seq('a', 'e', 'i', 'o', 'u').contains(_)).length
numberOfVowels: (str: String)Int

scala> numberOfVowels("the quick brown fox")
res0: Int = 5
~~~

This is a method that we use all the time. It would be great if `numberOfVowels` was a built-in method of `String` so we could write `string.numberOfVowels`, but unfortunately that is not the case. We don't have access to the source code for `String`, so there is no way we can directly add the method ourselves.

Fortunately, Scala has a feature called called **implicit classes** that allow us to add new functionality to an existing class without editing its source code. This is a similar concept to *categories* in Objective C or *extension methods* in C#, but the implementation is different in each case.

## Implicit Classes

An class that adds our `numberOfVowels` method to `String`:

~~~ scala
class ExtraStringMethods(str: String) {
  val vowels = Seq('a', 'e', 'i', 'o', 'u')

  def numberOfVowels =
    str.toList.filter(vowels contains _).length
}
~~~

We can use this to wrap up our `String` and gain access to our new method:

~~~ scala
new ExtraStringMethods("the quick brown fox").numberOfVowels
~~~

Writing `new ExtraStringMethods` every time we want to use `numberOfVowels` is unwieldy. However, if we tag our class with the `implicit` keyword, we give Scala the ability to insert the constructor call automatically into our code:

~~~ scala
scala> implicit class ExtraStringMethods(str: String) { /* ... */ }
defined class ExtraStringMethods

scala> "the quick brown fox".numberOfVowels
res1: Int = 5
~~~

When the compiler process our call to `numberOfVowels`, it interprets it as a type error because there is no such method in `String`. Rather than give up, the compiler attempts to fix the error using implicits. It finds `ExtraStringMethods`, which is an implicit class that is capable of transforming our `String` into something with a `numberOfVowels` method. The compiler inserts an invisible constructor call, and our code type checks correctly.

## Combining Type Classes and Type Enrichment

Type classes allow us to define adapter-style patterns that implement fixed behaviour for any type we specify. Type enrichment allows us to add functionality to existing classes without changing their definitions. We can combine the two techniques to add standard functionality to a range of classes.

To do this we keep the type class (`HtmlWriter`) and adapters (`PersonWriter`, `DateWriter` and so on) from our type class example, but replace our `HtmlUtils` singleton with an implicit generic class. For example:

~~~ scala
scala> implicit class HtmlOps[T](data: T) {
         def toHtml(implicit writer: HtmlWriter[T]) =
           writer.write(data)
       }
defined class HtmlOps
~~~

This allows us to invoke our type-class pattern on any type for which we have an adapter *as if it were a built-in feature of the class*:

~~~ scala
scala> Person("John", "john@example.com").toHtml
res5: String = <span>John &lt;john@example.com&gt;</span>
~~~

This gives us many benefits. We can extend existing types to give them new functionality, use simple syntax to invoke the functionality, *and* choose our preferred implementation by controlling which implicits we have in scope.

## Take Home Points

**Implicit classes** are a Scala language feature that allows us to define extra functionality on existing data types without using conventional inheritance.

This is a programming pattern called **type enrichment** or, more colloquially, *type "pimping"*.

The Scala compiler uses implicit classes to **fix type errors in our code**. When it encounters us accessing a method or field that doesn't exist, it looks through the available implicits to find some code it can insert to fix the error.

The compiler uses a strict set of **implicit resolution rules** to determine whether/which implicits can be used to fix a type error.

We can control which implicits are available by **bringing then into scope** using `import` statements or inheritance. It is good practice to do this only when we need to, to make it easy to see which implicits are being used where.

## Exercises

### Drinking the Kool Aid

Use your newfound powers to add a method `yeah` to `Int`, which prints `Oh yeah!` as many times as the `Int` on which it is called if the `Int` is positive, and is silent otherwise. Here's an example of usage:

~~~ scala
scala> 2.yeah
Oh yeah!
Oh yeah!

scala> 3.yeah
Oh yeah!
Oh yeah!
Oh yeah!

scala> -1.yeah

~~~

When you have written your implicit class, package it in an `IntImplicits` trait/singleton as discussed above.

<div class="solution">
~~~ scala
trait IntImplicits {
  implicit class IntOps(n: Int) {
    def yeah = for(_ <- 0 until n) println("Oh yeah!")
  }
}
object IntImplicits extends IntImplicits

import IntImplicits._

2.yeah
// Oh yeah!
// Oh yeah!
~~~

The solution uses a `for` comprehension and a range to iterate through the correct number of iterations. Remember that the range `0 until n` is the same as `0 to n-1` -- it contains all numbers from `0` inclusive to `n` exclusive.

We have used the parenthesis for of `for` here to keep the solution small. Writing the solution with braces is also fine. Note that we have omitted the `yield` keyword, resulting in a for comprehension that does not accumulate a value.

The names `IntImplicits` and `IntOps` are quite vague -- we would probably name them something more specific in a production codebase. However, for this exercise they will suffice perfectly.
</div>

### Times

Extend your previous example to give `Int` an extra method called `times` that accepts a function of type `Int => Unit` as an argument and executes it `n` times. Example usage:

~~~ scala
scala> 3.times(i => println(s"Look - it's the number $i!"))
Look - it's the number 0!
Look - it's the number 1!
Look - it's the number 2!
~~~

For bonus points, re-implement `yeah` in terms of `times`.

<div class="solution">
~~~ scala
object IntImplicits extends IntImplicits

trait IntImplicits {
  implicit class IntOps(n: Int) {
    def yeah =
      times(_ => println("Oh yeah!"))

    def times(func: Int => Unit) =
      for(i <- 0 until n) func(i)
  }
}
~~~
</div>

### Multiple Parameter Lists

Add a method `fold` to `Int`. `fold` has two parameter lists. The first accepts a seed of type `A`. The second accepts a function from `(A, Int) => A`. The method folds over the integers from zero until the given number. Example usage:

~~~ scala
scala> 4.fold(0)(_ + _)
res10: Int = 6

scala> 5.fold[Seq[Int]](Seq())(_ :+ _)
res2: Seq[Int] = List(0, 1, 2, 3, 4)
~~~

<div class="solution">
~~~ scala
object IntImplicits extends IntImplicits

trait IntImplicits {
  implicit class IntOps(n: Int) {
    // Code from previous exercises...

    def fold[A](seed: A)(func: (A, Int) => A) =
      (0 until n).foldLeft(seed)(func)
  }
}
~~~
</div>

### Prettier Conversion Syntax

Let's improve our JSON syntax by combining type classes and type enrichment. Convert `JsUtil` to an `implicit class` with a `toJson` method. Sample usage:

~~~ scala
Anonymous("001", new Date).toJson
~~~

<div class="solution">
~~~ scala
implicit class JsUtil[A](value: A) {
  def toJson(implicit writer: JsWriter[A]) =
    writer write value
}
~~~

In the previous exercise we only defined `JsWriters` for our main case classes. With this convenient syntax, it makes sense for us to have an complete set of `JsWriters` for all the serializable types in our codebase, including `Strings` and `Dates`:

~~~ scala
implicit object StringWriter extends JsWriter[String] {
  def write(value: String) = JsString(value)
}

implicit object DateWriter extends JsWriter[Date] {
  def write(value: Date) = JsString(value.toString)
}
~~~

With these definitions we can simplify our existing `JsWriters` for `Anonymous`, `User`, and `Visitor`:

~~~ scala
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
~~~
</div>
