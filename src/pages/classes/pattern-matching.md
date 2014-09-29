---
layout: page
title: Pattern Matching
---

Until now we have interacted with objects by calling methods or accessing fields. With case classes we can interact in another way, via **pattern matching**.

Pattern matching is like an extended `if` expression that allows us to evaluate an expression depending on the "shape" of the data. Recall the `Person` case class we've seen in previous examples:

~~~ scala
case class Person(firstName: String, lastName: String)
~~~

Now imagine we wanted to implement a `Stormtrooper` that is looking for members of the rebellion. We could use pattern matching like this:

~~~ scala
object Stormtrooper {
  def inspect(person: Person): String =
    person match {
      case Person("Luke", "Skywalker") => "Stop, rebel scum!"
      case Person("Han", "Solo") => "Stop, rebel scum!"
      case Person(first, last) => s"Move along, $first"
    }
}
~~~

Here it is in use:

~~~ scala
scala> Stormtrooper.inspect(Person("Noel", "Welsh"))
res0: String = Move along, Noel

scala> Stormtrooper.inspect(Person("Han", "Solo"))
res2: String = Stop, rebel scum!
~~~

<div class="callout callout-info">
#### Pattern Matching Syntax

The syntax of a pattern matching expression is

~~~ scala
expr0 match {
  case pattern1 => expr1
  case pattern2 => expr2
  ...
}
~~~

where

- the expression `expr0` evaluates to the value we match;
- the patterns, or *guards*, `pattern1`, `pattern2`, and so on are checked against this value in order; and
- the right-hand side expression (`expr1`, `expr2`, and so on) of the first pattern that matches is evaluated[^compilation].

Pattern matching is itself an expression and thus evaluates to a value -- the value of the matched expression.
</div>

[^compilation]: In reality patterns are compiled to a more efficient form than a sequence of tests, but the semantics are the same.


## Pattern Syntax

Scala has an expressive syntax for writing patterns or guards. For case classes the pattern syntax matches the constructor syntax. Take the data

~~~ scala
Person("Noel", "Welsh")
~~~

A pattern to match against the `Person` type is written

~~~ scala
Person(pat0, pat1)
~~~

where `pat0` and `pat1` are patterns to match agains the `firstName` and `lastName` respectively. There are four possible patterns we could use in place of `pat0` or `pat1`:

1. A name, which matches any value at that position and binds it to the given name. For example, the pattern `Person(first, last)` binds the name `first` to the value `"Noel"`, and the name `last` to the value `"Welsh"`.

2. An underscore (`_`), which matches any value and ignores it. For example, as Stormtroopers only care about the first name of ordinary citizens we could just write `Person(first, _)` to avoid binding a name to the value of the `lastName`.

3. A literal, which successfully matches only the value the literal respresents. So , for example, the pattern `Person("Han", "Solo")` matches the `Person` with first name `"Han"` and last name `"Solo"`.

4. Another case class using the same constructor style syntax.

Note there is a lot more we can do with pattern matching, and pattern matching is actually extensible. We'll look at these features in a later section.


## Take Home Points

Case classes allow a new form of interaction, called **pattern matching**. Pattern matching allows us to take apart a case class, and evaluate different expressions depending on what the case class contains.

The syntax for pattern matching is

~~~ scala
expr0 match {
  case pattern1 => expr1
  case pattern2 => expr2
  ...
}
~~~

A pattern can be one of

1. a name, binding any value to that name;
2. an underscore, matching any value and ignoring it;
3. a literal, matching the value the literal denotes; or
4. a constructor-style pattern for a case class.

## Exercises

### Feed the Cats

Define an object `ChipShop` with a method `willServe`. This method should accept a `Cat` and return true if the cat’s favourite food is chips, and false otherwise. Use pattern matching.

<div class="solution">
We can start by writing the skeleton suggested by the problem text.

~~~ scala
object ChipShop {
  def willServe(cat: Cat): Boolean =
    cat match {
      case Cat(???, ???, ???) => ???
    }
}
~~~

As the return type is `Boolean` we know we need at least two cases, one for true and one for false. The text of the exercise tells us what they should be: cats that prefer chips, and all other cats. We can implement this with a literal pattern and an `_` pattern.

~~~ scala
object ChipShop {
  def willServe(cat: Cat): Boolean =
    cat match {
      case Cat(_, "Chips") => true
      case Cat(_, _) => false
    }
}
~~~
</div>


### Get Off My Lawn!

In this exercise we're going to write a simulator of my Dad, the movie critic. It's quite simple: any movie directed by Clint Eastwood gets a rating 10.0, any movie directed by John McTiernan gets a 7.0, while any other movie gets a 3.0. Implement an object called `Dad` with a method `rate` which accepts a `Film` and returns a `Double`. Use pattern matching.

<div class="solution">
~~~ scala
object Dad {
  def rate(film: Film): Double =
    film match {
      case Film(_, _, _, Director("Clint", "Eastwood", _)) => 10.0
      case Film(_, _, _, Director("John", "McTiernan", _)) => 7.0
      case _ => 3.0
    }
}
~~~

Pattern matching is a bit annoying in this case. Later on we'll learn how we can use pattern matching to match a particular value, called a *constant pattern*.
</div>
