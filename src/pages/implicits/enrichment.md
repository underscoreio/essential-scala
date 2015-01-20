## Enriched Interfaces

A second type of type class interface, called **type enrichment**[^pimping] allow us to create
 interfaces that act as if they were methods defined on the classes of interest. For example, suppose we have a method called `numberOfVowels`:

~~~ scala
def numberOfVowels(str: String) =
  str.filter(Seq('a', 'e', 'i', 'o', 'u').contains(_)).length

numberOfVowels("the quick brown fox")
// res: Int = 5
~~~

[^pimping]: Type enrichment is sometimes referred to as pimping, but we will not use that term.


This is a method that we use all the time. It would be great if `numberOfVowels` was a built-in method of `String` so we could write `"a string".numberOfVowels`, but of course we can't change the source code for `String`. Scala has a feature called called **implicit classes** that allow us to add new functionality to an existing class without editing its source code. This is a similar concept to *categories* in Objective C or *extension methods* in C#, but the implementation is different in each case.

### Implicit Classes

Let's build up implicit classes piece by piece. We can wrap `String` in a class that adds our `numberOfVowels`:

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
implicit class ExtraStringMethods(str: String) { /* ... */ }

"the quick brown fox".numberOfVowels
// res: Int = 5
~~~

When the compiler process our call to `numberOfVowels`, it interprets it as a type error because there is no such method in `String`. Rather than give up, the compiler attempts to fix the error searching for an implicit class that provides the method and can be constructed from a `String`. It finds `ExtraStringMethods`. The compiler then inserts an invisible constructor call, and our code type checks correctly.

Implicit classes follow the same scoping rules as implicit values. Like implicit values, they must be defined within an enclosing object, class, or trait (except when writing Scala at the console).

There is one additional restriction for implicit classes: only a single implicit class with be used to resolve a type error. The compiler will not look to construct a chain of implicit classes to access the desired method. 

## Combining Type Classes and Type Enrichment

Implicit classes can be used on their own but we most often combine them with type classes to create a more natural style of interface. We keep the type class (`HtmlWriter`) and adapters (`PersonWriter`, `DateWriter` and so on) from our type class example, and add an implicit class with methods that themselves take implicit parameters. For example:

~~~ scala
implicit class HtmlOps[T](data: T) {
  def toHtml(implicit writer: HtmlWriter[T]) =
    writer.write(data)
}
~~~

This allows us to invoke our type-class pattern on any type for which we have an adapter *as if it were a built-in feature of the class*:

~~~ scala
Person("John", "john@example.com").toHtml
// res: String = <span>John &lt;john@example.com&gt;</span>
~~~

This gives us many benefits. We can extend existing types to give them new functionality, use simple syntax to invoke the functionality, *and* choose our preferred implementation by controlling which implicits we have in scope.

### Take Home Points

**Implicit classes** are a Scala language feature that allows us to define extra functionality on existing data types without using conventional inheritance. This is a programming pattern called **type enrichment**. 

The Scala compiler uses implicit classes to **fix type errors in our code**. When it encounters us accessing a method or field that doesn't exist, it looks through the available implicits to find some code it can insert to fix the error.

The rules for implicit classes are the same as for implicit values, with the additional restriction that only a single implicit class will be used to fix a type error.

### Exercises

#### Drinking the Kool Aid

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

When you have written your implicit class, package it in an `IntImplicits` object.

<div class="solution">
~~~ scala
object IntImplicits {
  implicit class IntOps(n: Int) {
    def yeah = for{ _ <- 0 until n } println("Oh yeah!")
  }
}

import IntImplicits._

2.yeah
// Oh yeah!
// Oh yeah!
~~~

The solution uses a `for` comprehension and a range to iterate through the correct number of iterations. Remember that the range `0 until n` is the same as `0 to n-1`---it contains all numbers from `0` inclusive to `n` exclusive.

The names `IntImplicits` and `IntOps` are quite vague -- we would probably name them something more specific in a production codebase. However, for this exercise they will suffice.
</div>

#### Times

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
object IntImplicits {
  implicit class IntOps(n: Int) {
    def yeah =
      times(_ => println("Oh yeah!"))

    def times(func: Int => Unit) =
      for(i <- 0 until n) func(i)
  }
}
~~~
</div>

### Easy Equality

Recall our `Equal` type class from a previous section. 

~~~
trait Equal[A] {
  def equal(v1: A, v2: A): Boolean
}
~~~

Implement an enrichment so we can use this type class via ` triple equal (`===`) method. For example, if the correct implicits are in scope the following should work.

~~~ scala
"abcd".===("ABCD") // Assumes case-insensitive equality implicit
~~~

<div class="solution">
We just need to define an implicit class, which I have here placed in the companion object of `Equal`.

~~~ scala
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
~~~

Here is an example of use.

~~~ scala
implicit val caseInsensitiveEquals = new Equal[String] {
  def equal(s1: String, s2: String) =
    s1.toLowerCase == s2.toLowerCase
}

import Equal._

"foo".===("FOO")
~~~
</div>
