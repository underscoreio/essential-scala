---
layout: page
title: "This contains That: Generics"
---

In the previous sections we saw how to model relationships such as **this and that** and **this or that** using *inheritance* -- a classic object oriented design pattern. In this section we will look at how to model these relationships in another way using **aggregation** -- grouping objects together using other objects.

We already know how to do simple aggregation in simple cases using fields. Here we start generalising over the types in our fields using **generic types**.

## Generic Types

Generic types naturally arise in collections, so let's consider a really simple collection -- a box that stores a single value. We don't care what type is stored in the box, but we want to make sure we preserve that type when we get the value out of the box. To do this we use a generic type.

~~~ scala
scala> case class Box[A](val value: A)
defined class Box

scala> Box(2)
res0: Box[Int] = Box(2)

scala> res0.value
res1: Int = 2

scala> Box("hi") // if we omit the type parameter, scala will infer its value
res2: Box[String] = Box(hi)

scala> res2.value
res3: String = hi
~~~

The syntax `[A]` is called a **type parameter** -- it binds a name to a type. Wherever `A` occurs in our class definition we will substitute in the same type. This works in the same way that binding a name to a value (using `val`) allows us to substitute in the value wherever the name occurs. The only difference is that we're operating on types rather than values.

We can also add type parameters to methods, which limits the scope of the parameter to the method declaration and body:

~~~ scala
scala> def generic[A](in: A): A = in
generic: [A](in: A)A

scala> generic[String]("foo")
res10: String = foo

scala> generic(1) // again, if we omit the type parameter, scala will infer it
res11: Int = 1
~~~

## This and That

Let's look at using generics to model a *product type*. Consider a method that returns two values -- for example, an `Int` and a `String`, or a `Boolean` and a `Double`:

~~~ scala
def intAndString: ??? = // ...

def booleanAndDouble: ??? = // ...
~~~

The question is what do we use as the return types? We could use a regular class without any type parameters, but then we would have to implement one version of the class for each combination of return types:

~~~ scala
case class IntAndString(intValue: Int, stringValue: String)

def intAndString: IntAndString = // ...

case class BooleanAndDouble(booleanValue: Boolean, doubleValue: Double)

def booleanAndDoule: BoleanAndDouble = // ...
~~~

The answer is to use generics to create a **product type** -- for example a `Pair` -- that contains the relevant data for *both* return types:

~~~ scala
def intAndString: Pair[Int, String] = // ...

def booleanAndDouble: Pair[Boolean, Double] = // ...
~~~

Generics provide a different approach to defining product types --  one that relies on aggregation as opposed to inheritance.

### Exercise: Pairs

Implement the `Pair` class from above. It should store two values -- `one` and `two` -- and be generic in both arguments. Example usage:

~~~ scala
scala> val pair = Pair[String, Int]("hi", 2)
pair: Pair[String,Int] = Pair(hi,2)

scala> pair.one
res13: String = hi

scala> pair.two
res14: Int = 2
~~~

<div class="solution">
If one type parameter is good, two type parameters are better:

~~~ scala
case class Pair[A, B](val one: A, val two: B)
~~~

Note that we don't always need to specify the type parameters when we construct `Pairs`. The compiler will attempt to infer the types as usual wherever it can:

~~~ scala
scala> val pair = Pair("hi", 2)
pair: Pair[String,Int] = Pair(hi,2)
~~~
</div>

### Tuples

A *tuple* is the generalisation of a pair to any number of terms. Scala includes built-in generic tuple types with up to 22 elements, along with special syntax for creating them. With these classes we can represent any kind of *this and that* relationship between any number of terms.

The classes are called `Tuple1[A]` through to `Tuple22[A, B, C, ...]` but they can also be written in the sugared form `(A, B, C, ...)`. For example:

~~~ scala
scala> Tuple2("hi", 1) // unsugared syntax
res19: (String, Int) = (hi,1)

scala> ("hi", 1) // sugared syntax
res19: (String, Int) = (hi,1)

scala> ("hi", 1, true)
res20: (String, Int, Boolean) = (hi,1,true)
~~~

We can define methods that accept tuples as parameters using the same syntax:

~~~ scala
scala> def tuplized[A, B](in: (A, B)) = in._1
tuplized: [A, B](in: (A, B))A

scala> tuplized(("a", 1))
res21: String = a
~~~

We can also pattern match on tuples as follows:

~~~ scala
scala> (1, "a") match {
     |   case (a, b) => a + b
     | }
res3: String = 1a
~~~

Although pattern matching is the natural way to deconstruct a tuple, each class also has a compliment of fields named `_1`, `_2` and so on:

~~~ scala
scala> val x = (1, "b", true)
x: (Int, String, Boolean) = (1,b,true)

scala> x._1
res5: Int = 1

scala> x._3
res6: Boolean = true
~~~

## This or That

Now let's look at using generics to model a *sum type*. Again, we have previously implemented this using inheritance, factoring out the common aspects into a supertype. Generics provide a different tool to do the same thing.

Consider a method that, depending on the value of its parameters, returns one of two types:

~~~ scala
scala> def intOrString(input: Boolean) =
     |   if(input == true) 123 else "abc"
intOrString: (input: Boolean)Any
~~~

We can't simply write this method as shown above because the compiler infers the result type as `Any`. Instead we have to introduce a new type to explicitly represent the disjunction:

~~~ scala
def intOrString(input: Boolean): Sum[Int, String] =
  if(input == true) {
    Left[Int, String](123)
  } else {
    Right[Int, String]("abc")
  }
~~~

How do we implement `Sum`? We just have to use the idioms we've already seen in the [This or That](traits.html) section!

### Exercise: Generic Sum Type

Implement a trait `Sum[A, B]` with two subtypes `Left` and `Right`. Create type parameters so that `Left` and `Right` can wrap up values of two different types.

Hint: you will need to put both type parameters on all three types. Example usage:

~~~ scala
scala> Left[Int, String](1).value
res24: Int = 1

scala> Right[Int, String]("foo").value
res25: String = foo

scala> val sum: Sum[Int, String] = Right("foo")
sum: Sum[Int,String] = Right(foo)

scala> sum match {
     |   case Left(x) => x.toString
     |   case Right(x) => x
     | }
res26: String = foo
~~~

<div class="solution">
The code is very similar to `Box`, except that we need both type parameters:

~~~ scala
sealed trait Sum[A, B]
final case class Left[A, B](val value: A) extends Sum[A, B]
final case class Right[A, B](val value: B) extends Sum[A, B]
~~~

Scala has the generic sum type `Either` for two cases, but it does not have types for more cases.
</div>

### Exercise: Maybe that was a Mistake

In a previous exercise we "solved" the problem of dividing by zero by defining a type called `DivisionResult`. This forced us to handle the possibility of a division by zero in order to access the value.

With our knowledge of generics we can now generalise `DivisionResult` to encapsulate potential errors of any type. Modify `DivisionResult` to create a generic trait called `Maybe` with two subtypes, `Full` and `Empty`. Rewrite `divide` to return a `Maybe[Int]`. Example usage:

~~~ scala
divide(1, 0) match {
  case Full(value) => println(s"It's finite: ${value}")
  case Empty()     => println(s"It's infinite")
}
~~~

<div class="solution">
Ideally we would like to write something like this:

~~~ scala
sealed trait Maybe[A]
final case class Full[A](val value: A) extends Maybe[A]
final case object Empty extends Maybe[???]
~~~

However, objects can't have type parameters. In order to make `Empty` an object we need to provide a concrete type in the `extends Maybe` part of the definition. But what type parameter should we use? In the absence of a preference for a particular data type, we could use something like `Unit` or `Nothing`. However this leads to type errors:

~~~ scala
scala> :paste
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]
^D

defined trait Maybe
defined class Full
defined module Empty

scala> val possible: Maybe[Int] = Empty
<console>:9: error: type mismatch;
 found   : Empty.type
 required: Maybe[Int]
       val possible: Maybe[Int] = Empty
~~~

The problem here is that `Empty` is a `Maybe[Nothing]` and a `Maybe[Nothing]` is not a `Maybe[Int]`.

We'll see how to overcome this limitation later. In the meantime we can define `Empty` as a class with a type parameter as a stop-gap solution:

~~~ scala
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]

object division {
  def apply(num: Int, den: Int) =
    if(den == 0) Empty[Int] else Full(num / den)
}
~~~

Regardless, `Maybe` is a powerful concept -- it allows us to represent objects that may or may not contain a value. We can see this as an alternative to using `null`, except that the compiler can verify that we never get a `NullPointerException`.

In fact, `Maybe` is so useful that Scala defines a core class called `Option` for this very purpose. `Option` is a trait with two subtypes, `Some` for storing a value and `None` representing an empty value.
</div>

## Type Bounds

It is sometimes useful to constrain a generic type. We can do this with type bounds indicating that a generic type should be a sub- or super-type of some given types. The syntax is `A <: Type` to declare `A` must be a subtype of `Type` and `A >: Type` to declare a supertype.

For example, the following type allows us to store a `Visitor` or any subtype:

~~~ scala
case class WebAnalytics[A <: Visitor](
  visitor: A,
  pageViews: Int,
  searchTerms: List[String],
  isOrganic: Boolean
)
~~~

## Take Home Points

**Generic classes, traits, and methods** allow us to abstract across the types they store, accept, and return. We define them using **type parameters** that we can bind to different concrete types in each use case.

In this section we have used generics to model **product types** ("this and that") and **sum types** ("this or that") using generics. These are alternatives to the inheritance-based approaches we have seen previously using traits.

## Exercises

### Generics versus Traits

Sum types and product types are general concepts that allow us to model almost any kind of data structure. We have seen two methods of writing these types -- traits and generics -- when should we consider using each?

<div class="solution">
Ultimately the decision is up to us. Different teams will adopt different programming styles. However, we examine look at the properties of each approach to inform our choices:

Inheritance-based approaches -- traits and classes -- allow us to create permanent data structures with specific types and names. We can name every field and method and implement use-case-specific code in each class. Inheritance is therefore better suited to modelling significant aspects of our programs that are re-used in many areas of our codebase.

Generic data structures -- `Tuples`, `Options`, `Eithers`, and so on -- are extremely broad and general purpose. There are a wide range of predefined classes in the Scala standard library that we can use to quickly model relationships between data in our code. These classes are therefore better suited to quick, one-off pieces of data manipulation where defining our own types would introduce unnecessary verbosity to our codebase.
</div>
