---
layout: page
title: "This or That and Nothing Else: Sealed Traits"
---

We have seen how to use traits to model data that is of one type or another. Now let's look at using this information to write code that works with any type in our hierarchy.

Let's return to our web site visitors example. Imagine we want to add the ability to email `Visitors`. We can only actually email `Users` because we don't have an email address for `Anonymous` visitors. If we have a `Visitor`, how can we tell what type it is, and thus whether we can email them?

We're going to look at two solutions: an object-oriented solution based on *inheritance* and a functional solution based on *pattern matching*.

### Approach 1: Inheritance

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

**I maintain, this is a bad design.**

As we add more features our code we will soon find `Visitor` filling up with abstract methods. It is also likely that `Anonymous` will fill up with empty implementations. For every feature we add it will become harder to maintain and test our class hierarchy, and any system that uses `Visitor` will have to carry around all its dependencies.

### Approach 2: Pattern Matching

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

`expr0` is an expression that yields the value we want to match on. It is followed by the keyword `match` and a series of `case` clauses. Each clause consists of the keyword `case`, a *pattern*, the `=>` symbol, and a series of zero or more Scala expressions.

Pattern matching operates by checking the return value of `expre0` against each pattern in turn, finding the first pattern that matches, and evaluating the corresponding set of expressions[^compilation]. The whole `match` expression yields the value of the last expression in the corresponding block.

[^compilation]: In reality patterns are compiled to a more efficient form than a sequence of tests, but the semantics are the same.

We should pay attention to the *pattern* components of the expression: although they look like regular Scala code, they are actually implemented in a *pattern matching DSL* that has its own separate syntax and semantics. We will examine the pattern language in detail later on. For now we only need concern ourselves with one type of pattern -- case classes.

The pattern syntax for case classes matches the constructor syntax:  For example, the pattern `User(a, b, c)` matches any `User` object and binds the names `id`, `address`, and `createdAt` fields to variables called `a`, `b`, and `c`. These variables can be used in the code on the right-hand side of the `=>`.

We can use the `_` symbol in place of any of the variables `a`, `b`, or `c` to indicate that we aren't interested in binding a variable for that value.

Note that there is a lot more to the pattern language than this. We will revisit it later to unlock its full power once we have more of Scala features under our belts.

## Sealed Traits

The Scala compiler won't complain if we miss out a case in our pattern matching, but we will get an exception at runtime. For example:

~~~ scala
scala> def missingCase(v: Visitor) =
     |   v match {
     |     // There's no case for `Anonymous` here:
     |     case User(_, _, _) =>
     |       "Got a user"
     |   }
missingCase: (v: Visitor)String

scala> missingCase(Anonymous("a"))
missingCase(Anonymous("a"))
scala.MatchError: Anonymous(a,Fri Feb 14 19:47:42 GMT 2014) (of class Anonymous)
  at .missingCase(<console>:12)
    ...
~~~

The reason we don't get a compiler error is that there could be any number of implementations of `Visitor` in our codebase or anything using our code as a library. The Scala compiler can't be sure that only two cases exist.

This is a problem for our `Visitor` example -- the last thing we want is to get a runtime error when we're trying to send an email! Fortunately there is a way to tell Scala that all subtypes of `Visitor` *must* implemented in the same file. This means the compiler can correctly flag incomplete matches and prevent us from missing out cases. We do this by making `Visitor` a **sealed trait**:

~~~ scala
sealed trait Visitor {
  def id: String
  def createdAt: Date
  def age: Long = new Date().getTime() - createdAt.getTime()
}

case class Anonymous(
  val id: String,
  val createdAt: Date = new Date()
) extends Visitor

case class User(
  val id: String,
  val emailAddress: String,
  val createdAt: Date = new Date()
) extends Visitor
~~~

We using a sealed trait we must define all the subtypes in the same file, which is a resonable restriction in our `Visitor` example. When we do this, the compiler warns us if we don't have exhaustive pattern matching:

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

Note that we can still extend subtypes of a sealed trait outside of the file where they are defined. For example, in other code we could extend `User` or `Anonymous`. If we want to prevent this possibility we should declare them as `sealed` (if we want to allow extensions within the file) or `final` if we want to disallow all extensions. For the visitors example it probably doesn't make sense to allow any extension, so the simplified code should look like this:

~~~ scala
sealed trait Visitor { /* ... */ }
final case class User(/* ... */) extends Visitor
final case class Anonymous(/* ... */) extends Visitor
~~~

Now imagine the problem of parsing HTTP headers. Most of the HTTP headers are defined in various RFCs, and we could define types for these as follows:

~~~ scala
sealed trait Header { ... }
final case class Accept(...) extends Header
final case class AcceptCharset(...) extends Header
...
~~~

It certainly doesn't make sense to subtype one of the defined headers so we can defined them as `final`. However custom headers are used all the time, so we need to provide some extension point. To achieve this we can define a `CustomHeader` subtype of `Header` that is neither `sealed` nor `final`. This allows the user to extend `CustomHeader` in their code, but we still retain the benefits of `sealed` and `final` types in ours[^http]. This is the approach taken in, for example, the Scala web framework [Spray].

[Spray]: http://spray.io/documentation/api/#spray.http.HttpHeaders$

[^http]: There are so many HTTP headers that we're unlikely to perform pattern matching on them, but hopefully you can generalise this example to your own problems.

## Take home points

In this section we looked at two ways of implementing the same piece of functionality. The approach #1 favoured heavyweight data objects with lots of embedded functionality, while approach #2 favoured lightweight data objects with additional functionality supplied by external modules.

We have already expressed a preference for the more modular approach #2. Scala provides language features that directly support this style of coding:

 - Case classes make it very easy to create lightweight data-holding classes.

 - Sealed trait allow us to enumerate fixed sets of data types that we do not expect to change.

 - Pattern matching provides a means to run different blocks of code for the types we have defined, quickly deconstructing these into the fields we need.

## Exercises

### Printing Shapes

Let's revisit the `Shapes` example from the previous section.

First make `Shape` a sealed trait. Then write a singleton object called `Draw` with an `apply` method that takes a `Shape` as an argument and prints it on the console.

Don't worry -- we won't make you *actually draw the shape* on the console. For the purposes of the exercise, simply print the type.

Finally, verify that the compiler complains when you comment out a clause.

<div class="solution">
~~~ scala
object Draw {
  def apply(shape: Shape) = shape match {
    case Rectangle(width, height) =>
      println(s"Rectangle size ${width}x${height}")

    case Square(size) =>
      println(s"Square size ${size}")

    case Circle(radius) =>
      println(s"Circle radius ${radius}")
  }
}
~~~
</div>

### Really Printing Shapes

We lied...

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