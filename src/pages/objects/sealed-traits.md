---
layout: page
title: "This or That and Nothing Else: Sealed Traits"
---

We have seen how to use traits to model data that is of one type or another. Now let's look at using this information to write code that works with a range of possible types.

Let's return to our web site visitors example. Imagine we want to add the ability to email `Visitors`. We can only actually email `Users` because we don't have an email address for `Anonymous` visitors. This poses a question: if we have a `Visitor`, how can we tell what type it is and thus whether we can email them?

We're going to look at two solutions: an object-oriented solution based on *inheritance* and a functional solution based on **pattern matching**. The pattern matching solution will lead us to **sealed traits**.

## Approach #1: Inheritance

The object-oriented solution is to add an abstract method to `Visitor` and implement it in each of the  subtypes. The code looks something like this (trimmed for simplicity):

~~~ scala
trait Visitor {
  def email(subject: String, body: String): Unit
}

case class Anonymous(/* ... */) extends Visitor {
  def email(subject: String, body: String): Unit = {
    () // do nothing
  }
}

case class User(/* ... */) extends Visitor {
  def email(subject: String, body: String) = {
    reallySendAnEmail(email, subject, body)
  }
}
~~~

Given this code we can call the `email` method on any `Visitor`. If the visitor is a `User` an email will be sent, and if the visitor is `Anonymous` nothing will happen.

**This is bad design.** As we add more features our code we will soon find `Visitor` filling up with abstract methods. It is also likely that `Anonymous` will fill up with empty implementations. For every feature we add it will become harder to maintain and test our class hierarchy, and any system that uses `Visitor` will have to carry around all its dependencies.

## Approach #2: Pattern Matching

A better design is to keep our case classes as lightweight [value objects](http://en.wikipedia.org/wiki/Value_object) and implement new features as separate libraries of code.

Let's refactor our `email` method into its own library:

~~~ scala
trait EmailService {
  def email(visitor: Visitor, subject: String, body: String): Unit = {
    // To send or not to send... that is the question
  }
}
~~~

The question is: how do we implement the body of the `email` method? We need to know if we have a `User` or an `Anonymous`. Fortunately, Scala has great general purpose tool called **pattern matching** that is perfect for this situation. The code looks like this:

~~~ scala
trait EmailService {
  def email(visitor: Visitor, subject: String, body: String): Unit = {
    visitor match {
      case Anonymous(id, createdAt) =>
        ()

      case User(id, email, createdAt) =>
        reallySendAnEmail(email, subject, body)
    }
  }
}
~~~

We introduce pattern matching using a `match` expression that looks like the following:

~~~
expr0 match {
  case pattern1 => expr1 ...
  case pattern2 => expr2 ...
  ...
}
~~~

`expr0` is an expression that yields the value we want to match on. It is followed by the keyword `match` and a series of `case` clauses[^match-keyword]. Each clause consists of the keyword `case`, a *pattern*, the `=>` symbol, and a series of zero or more Scala expressions.

[^match-keyword]: `match` is actually a keyword, not a method name or syntactic sugar for a method call.

Pattern matching operates by checking the return value of `expr0` against each pattern in turn, finding the first pattern that matches, and evaluating the corresponding set of expressions[^compilation]. The whole `match` expression yields the value of the last expression in the corresponding block.

[^compilation]: In reality patterns are compiled to a more efficient form than a sequence of tests, but the semantics are the same.

We should pay attention to the *pattern* components of the expression -- although they look like regular Scala code, they are actually implemented in a **pattern matching DSL** that has its own separate syntax and semantics. We will examine the pattern language in detail later on. For now we only need concern ourselves with one type of pattern -- case classes.

The pattern syntax for case classes mirrors the constructor syntax:  For example, the pattern `User(a, b, c)` matches any `User` object and binds the names `id`, `email`, and `createdAt` fields to variables called `a`, `b`, and `c`. These variables can be used in the code on the right-hand side of the `=>`.

We can substitute the `_` symbol for any variable that we're not interested in. So, for example, `User(_, _, c)` binds `c` to the `createdAt` field and leaves `id` and `email` unbound.

There is a lot more to the pattern language than this -- we will revisit it later in depth once we have covered more features of Scala.

## Sealed Traits

The Scala compiler won't complain if we miss out a case in our pattern matching, but we will get an exception at runtime. For example:

~~~ scala
scala> def missingCase(v: Visitor) =
     |   v match {
     |     case User(_, _, _) =>
     |       "Got a user"
     |
     |     // No case for `Anonymous`
     |   }
missingCase: (v: Visitor)String

scala> missingCase(Anonymous("a"))
missingCase(Anonymous("a"))
scala.MatchError: Anonymous(a,Fri Feb 14 19:47:42 GMT 2014) (of class Anonymous)
  at .missingCase(<console>:12)
    ...
~~~

The fact that we get a runtime error may be surprising. There could be any number of implementations of `Visitor` in our codebase -- it is impossible for Scala to tell at compile time whether our `match` is exhaustive.

This is a problem -- the last thing we want is to get a runtime error when we're trying to send an email! Fortunately there is a way to tell Scala that the subtypes of we have provided form a complete set, allowing the compiler to correctly flag incomplete matches at compile time. We do this by making `Visitor` a **sealed trait**:

~~~ scala
sealed trait Visitor {
  def id: String
  def createdAt: Date
  def age: Long = new Date().getTime() - createdAt.getTime()
}
~~~

When we mark a trait as `sealed` we *must* define all of its subtypes in the same file. This is a resonable restriction in our `Visitor` example. Once the trait is sealed, the compiler knows the complete set of  types we have to cover to make our `matches` exhaustive:

~~~ scala
scala> def missingCase(v: Visitor) =
       |  v match {
       |    case User(_, _, _) => "Got a user"
       |  }
<console>:21: warning: match may not be exhaustive.
It would fail on the following input: Anonymous(_, _)
               v match {
               ^
missingCase: (v: Visitor)String
~~~

We can still extend the subtypes of a sealed trait outside of the file where they are defined. For example, we could extend `User` or `Anonymous` further elsewhere. If we want to prevent this possibility we should declare them as `sealed` (if we want to allow extensions within the file) or `final` if we want to disallow all extensions. For the visitors example it probably doesn't make sense to allow any extension to `User` or `Anonymous`, so the simplified code should look like this:

~~~ scala
sealed trait Visitor { /* ... */ }
final case class User(/* ... */) extends Visitor
final case class Anonymous(/* ... */) extends Visitor
~~~

Now imagine a problem with a similar structure -- parsing HTTP headers. Most of the HTTP headers are defined in various RFCs, and we could define types for these as follows:

~~~ scala
sealed trait Header { /* ... */ }
final case class Accept(/* ... */) extends Header
final case class AcceptCharset(/* ... */) extends Header
...
~~~

It doesn't make sense to subtype a header like `Accept` so we can make all of our leaf classes `final`. However, custom headers are commonplace in HTTP so we do need to provide some extension point. A convenient solution is to define a `CustomHeader` class that is neither `sealed` nor `final`. This allows us create custom HTTP headers when we need to while retaining the benefits of `sealed` and `final` types elsewhere[^http]. This is the approach taken in, for example, the [Spray] web framework.

[Spray]: http://spray.io/documentation/api/#spray.http.HttpHeaders$

[^http]: There are so many HTTP headers that we're unlikely to perform pattern matching on them, but hopefully you can generalise this example to your own problems.

## Take home points

In this section we looked at two ways of implementing the same piece of functionality. Approach #1 favoured heavyweight data objects with lots of embedded functionality, while approach #2 favoured lightweight data objects with additional functionality supplied by external modules.

We have already expressed our preference for the modularity of approach #2. Scala provides a number of language features that directly support this style of coding:

 - **Case classes** make it very easy to create lightweight data-holding classes.

 - **Sealed traits** allow us to enumerate fixed sets of data types that we do not expect to change.

 - **Pattern matching** provides a means to run different blocks of code for the types we have defined, quickly deconstructing these into the fields we need.

## Exercises

## Printing Shapes

Let's revisit the `Shapes` example from the previous section.

First make `Shape` a sealed trait. Then write a singleton object called `Draw` with an `apply` method that takes a `Shape` as an argument and returns a description of it on the console. For example:

~~~ scala
Draw(Circle(10))      // returns "A circle of radius 10cm"

Draw(Rectangle(3, 4)) // returns "A rectangle of width 3cm and height 4cm"

// and so on...
~~~

Finally, verify that the compiler complains when you comment out a `case` clause.

<div class="solution">
~~~ scala
object Draw {
  def apply(shape: Shape) = shape match {
    case Rectangle(width, height) =>
      s"A rectangle of width ${width}cm and height ${height}cm"

    case Square(size) =>
      s"A square of size ${size}cm"

    case Circle(radius) =>
      s"A circle of radius ${radius}cm"
  }
}
~~~
</div>

## The Color and the Shape

Write a sealed trait `Color` to make our shapes more interesting.

 - give `Color` three properties for its RGB values;
 - create three predefined colours: `Red`, `Yellow`, and `Pink`;
 - provide a means for people to produce their own custom `Colors`
   with their own RGB values;
 - provide a means for people to tell whether any `Color` is
   "light" or "dark".

<div class="alert alert-info">
**Note:** A lot of this is left deliberately open to interpretation. The important thing is to practice working with traits, classes, and objects.

Decisions such as how to model colours and what is considered a light or dark colour can either be left up to you or discussed with other class members.
</div>

Edit the code for `Shape` and its subtypes to add a colour to each shape.

Finally, update the code for `Draw.apply` to print the colour of the argument as well as its shape and dimensions (hint: you may want to deal with the colour in a helper method):

 - if the argument is a predefined colour, print that colour by name:

   ~~~ scala
   Draw(Circle(10, Yellow)) // returns "A yellow square of size 10cm"
   ~~~

 - if the argument is a custom colour rather than a predefined one,
   print the word "light" or "dark" instead.

<div class="solution">
One solution to this exercise is presented below. Remember that a lot of the implementation details are unimportant -- the crucial aspects of a correct solution are:

 - There must be a `sealed trait Color`:
    - The trait should contain three `def` methods for the RGB values.
    - The trait should contains the `isLight` method,
      defined in terms of the RGB values.

 - There must be three objects representing the predefined colours:
    - Each object must `extend Color`.
    - Each object should override the RGB values as `vals`.
    - Marking the objects as `final` is optional.
    - Making the objects `case objects` is also optional.

 - There must be a class representing custom colours:
    - The class must `extend Color`.
    - Marking the class `final` is optional.
    - Making the class a `case class` is optional (although highly recommended).

 - There should ideally be two methods in `Draw`:
    - One method should accepti a `Color` as a parameter and one a `Shape`.
    - The method names are unimportant.
    - Each method should perform a `match` on the supplied value and provide
      enough `cases` to cover all possible subtypes.

 - The whole codebase should compile and produce sensible values when tested!

~~~ scala
// Shape uses Color so we define Color first:
sealed trait Color {
  // We decided to store RGB values as doubles between 0.0 and 1.0.
  //
  // It is always good practice to define abstract members as `defs`
  // so we can implement them with `defs`, `vals` or `vars`.
  def red: Double
  def green: Double
  def blue: Double

  // We decided to define a "light" colour  as one with
  // an average RGB of more than 0.5:
  def isLight = (red + green + blue) / 3.0 > 0.5
}

final case object Red extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 0.0
  val blue = 0.0
 }

final case object Yellow extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 1.0
  val blue = 0.0
}

final case object Pink extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 0.0
  val blue = 1.0
}

// The arguments to the case class here generate `val` declarations
// that implement the RGB methods from `Color`:
final case class CustomColor(
  red: Double,
  green: Double,
  blue: Double) extends Color

// The code from the previous exercise comes across almost verbatim,
// except that we add a `color` field to `Shape` and its subtypes:
sealed trait Shape {
  def sides: Int
  def perimeter: Double
  def area: Double
  def color: Color
}

final case class Circle(radius: Double, color: Color) extends Shape {
  val sides = 1
  val perimeter = 2 * math.Pi * radius
  val area = math.Pi * radius * radius
}

sealed trait Rectangular extends Shape {
  def width: Double
  def height: Double
  val sides = 4
  val perimeter = 2 * width + 2 * height
  val area = width * height
}

final case class Square(size: Double, color: Color) extends Rectangular {
  val width = size
  val height = size
}

final case class Rectangle(
  width: Double,
  height: Double,
  color: Color) extends Rectangular

// We decided to overload the `Draw.apply` method for `Shape` and
// `Color` on the basis that we may want to reuse the `Color` code
// directly elsewhere:
object Draw {
  def apply(shape: Shape): String = shape match {
    case Circle(radius, color) =>
      s"A ${Draw(color)} circle of radius ${radius}cm"

    case Square(size, color) =>
      s"A ${Draw(color)} square of size ${size}cm"

    case Rectangle(width, height, color) =>
      s"A ${Draw(color)} rectangle of width ${width}cm and height ${height}cm"
  }

  def apply(color: Color): String = color match {
    // We deal with each of the predefined Colors with special cases:
    case Red    => "red"
    case Yellow => "yellow"
    case Pink   => "pink"
    case color  => if(color.isLight) "light" else "dark"
  }
}

// Test code:

Draw(Circle(10, Pink))
// returns "A pink circle of radius 10.0cm"

Draw(Rectangle(3, 4, CustomColor(0.4, 0.4, 0.6)))
// returns "A dark rectangle of width 3.0cm and height 4.0cm"
~~~

</div>

## A Short Division Exercise

Good Scala developers don't just use types to model data. Types are a great way to put artificial limitations in place to ensure we don't make mistakes in our programs. In this exercise we will see a simple (if contrived) example of this -- using types to prevent division by zero errors.

Dividing by zero is a tricky problem -- it can lead to exceptions. The JVM has us covered as far as floating point division is concerned but integer division is still a problem:

~~~ scala
scala> 1.0 / 0.0
res0: Double = Infinity

scala> 1 / 0
java.lang.ArithmeticException: / by zero
~~~

Let's solve this problem once and for all using types!

Create an object called `divide` with an `apply` method that accepts two `Ints` and returns `DivisionResult`. `DivisionResult` should be a sealed trait with two subtypes: a `Finite` type encapsulating the result of a valid division, and an `Infinite` type representing the result of dividing by `0`.

Here's some example usage:

~~~ scala
scala> divide(1, 2)
res7: DivisionResult = Finite(0)

scala> divide(1, 0)
res8: DivisionResult = Infinite
~~~

Finally, write a sample function that calls `divide`, matches on the result, and returns a sensible description.

<div class="solution">
Here's the code:

~~~ scala
sealed trait DivisionResult
final case class Finite(value: Int) extends DivisionResult
final case object Infinite extends DivisionResult

object divide {
  def apply(num: Int, den: Int) =
    if(den == 0) Infinite else Finite(num / den)
}

divide(1, 0) match {
  case Finite(value) => s"It's finite: ${value}"
  case Infinite      => s"It's infinite"
}
~~~

The result of `divide.apply` is a `DivisionResult`, which is a `sealed trait` with two subtypes. The subtype `Finite` is a `case class` encapsulting the result, but the subtype `Infinite` can simply be an object. We've used a `case object` for parity with `Finite`.

The implementation of `divide.apply` is simple - we perform a test and return a result. Note that we haven't annotated the method with a result type -- Scala is capable of inferring the type `DivisionResult` as the least upper bound of `Infinite` and `Finite`.

Finally, the match illustrates a case class pattern with the parentheses, and a case object pattern without.
</div>

{% comment %}
## Really Printing Shapes

We lied earlier...

For the enthusiastic, modify your code from the previous exercise to actually print the shapes using asterisks. This exercise is probably best done as homework.

Here are some useful tips:

 - `Circle` is hardest -- leave it for last;

 - you can print an asterisk character without moving to a new line using the code `print('*')`;

 - you can move to a new line using the code `println()`;

 - you can write a simple `for` loop like this: `for(i <- 0 to 10) { ... }`;

 - you'll need to round your `Doubles` to `Ints` using `.toInt` to use them in the loop ranges.

<div class="solution">
~~~ scala
object Draw {
  def apply(shape: Shape) = shape match {
    case Rectangle(width, height) =>
      val w = width.toInt
      val h = height.toInt

      for(y <- 0 to h) {
        for(x <- 0 to w) {
          print('*')
        }
        println()
      }

    case Square(size) =>
      val s = size.toInt

      for(y <- 0 to s) {
        for(x <- 0 to s) {
          print('*')
        }
        println()
      }

    case Circle(radius) =>
      val r = radius.toInt

      for(y <- -r to r) {
        for(x <- -r to r) {
          if(x*x + y*y < radius*radius) {
            print('*')
          } else {
            print(' ')
          }
        }
        println()
      }
  }
}
~~~
</div>
{% endcomment %}