## Modelling Data with Generic Types

In this section we'll see the additional power the generic types give us when modelling data. We see that with generic types we can implement *generic sum and product types*, and also model some other useful abstractions such as *optional values*.

### Generic Product Types

Let's look at using generics to model a *product type*. Consider a method that returns two values---for example, an `Int` and a `String`, or a `Boolean` and a `Double`:

```scala
def intAndString: ??? = // ...

def booleanAndDouble: ??? = // ...
```

The question is what do we use as the return types? We could use a regular class without any type parameters, with our usual algebraic data type patterns, but then we would have to implement one version of the class for each combination of return types:

```scala
case class IntAndString(intValue: Int, stringValue: String)

def intAndString: IntAndString = // ...

case class BooleanAndDouble(booleanValue: Boolean, doubleValue: Double)

def booleanAndDouble: BooleanAndDouble = // ...
```

The answer is to use generics to create a *product type*---for example a `Pair`---that contains the relevant data for *both* return types:

```scala
def intAndString: Pair[Int, String] = // ...

def booleanAndDouble: Pair[Boolean, Double] = // ...
```

Generics provide a different approach to defining product types--- one that relies on aggregation as opposed to inheritance.

#### Exercise: Pairs

Implement the `Pair` class from above. It should store two values---`one` and `two`---and be generic in both arguments. Example usage:

```tut:invisible
case class Pair[A, B](one: A, two: B)
```

```tut:book
val pair = Pair[String, Int]("hi", 2)

pair.one

pair.two
```

<div class="solution">
If one type parameter is good, two type parameters are better:

```tut:book:silent
case class Pair[A, B](one: A, two: B)
```

This is just the product type pattern we have seen before, but we introduce generic types.

Note that we don't always need to specify the type parameters when we construct `Pairs`. The compiler will attempt to infer the types as usual wherever it can:

```tut:book
val pair = Pair("hi", 2)
```
</div>

### Tuples

A *tuple* is the generalisation of a pair to more terms. Scala includes built-in generic tuple types with up to 22 elements, along with special syntax for creating them. With these classes we can represent any kind of *this and that* relationship between almost any number of terms.

The classes are called `Tuple1[A]` through to `Tuple22[A, B, C, ...]` but they can also be written in the sugared[^sugar] form `(A, B, C, ...)`. For example:

[^sugar]: The term "syntactic sugar" is used to refer to convenience syntax that is not needed but makes programming sweeter. Operator syntax is another example of syntactic sugar that Scala provides.

```tut:book
Tuple2("hi", 1) // unsugared syntax

("hi", 1) // sugared syntax

("hi", 1, true)
```

We can define methods that accept tuples as parameters using the same syntax:

```tut:book
def tuplized[A, B](in: (A, B)) = in._1

tuplized(("a", 1))
```

We can also pattern match on tuples as follows:

```tut:book
(1, "a") match {
  case (a, b) => a + b
}
```

Although pattern matching is the natural way to deconstruct a tuple, each class also has a complement of fields named `_1`, `_2` and so on:

```tut:book
val x = (1, "b", true)

x._1

x._3
```

### Generic Sum Types

Now let's look at using generics to model a *sum type*. Again, we have previously implemented this using our algebraic data type pattern, factoring out the common aspects into a supertype. Generics allow us to abstract over this pattern, providing a ... well ... generic implementation.

Consider a method that, depending on the value of its parameters, returns one of two types:

```tut:book
def intOrString(input: Boolean) =
  if(input == true) 123 else "abc"
```

We can't simply write this method as shown above because the compiler infers the result type as `Any`. Instead we have to introduce a new type to explicitly represent the disjunction:

```tut:invisible
object sum {
  sealed trait Sum[A, B]
  final case class Left[A, B](value: A) extends Sum[A, B]
  final case class Right[A, B](value: B) extends Sum[A, B]
}
import sum._
```

```tut:book
def intOrString(input: Boolean): Sum[Int, String] =
  if(input == true) {
    Left[Int, String](123)
  } else {
    Right[Int, String]("abc")
  }
```

How do we implement `Sum`? We just have to use the patterns we've already seen, with the addition of generic types.

#### Exercise: Generic Sum Type

Implement a trait `Sum[A, B]` with two subtypes `Left` and `Right`. Create type parameters so that `Left` and `Right` can wrap up values of two different types.

Hint: you will need to put both type parameters on all three types. Example usage:

```tut:book
Left[Int, String](1).value

Right[Int, String]("foo").value

val sum: Sum[Int, String] = Right("foo")

sum match {
  case Left(x) => x.toString
  case Right(x) => x
}
```

<div class="solution">
The code is an adaptation of our invariant generic sum type pattern, with another type parameter:

```tut:book:silent
sealed trait Sum[A, B]
final case class Left[A, B](value: A) extends Sum[A, B]
final case class Right[A, B](value: B) extends Sum[A, B]
```

Scala's standard library has the generic sum type `Either` for two cases, but it does not have types for more cases.
</div>


### Generic Optional Values

Many expressions may sometimes produce a value and sometimes not. For example, when we look up an element in a hash table (associative array) by a key, there may not be a value there. If we're talking to a web service, that service may be down and not reply to us. If we're looking for a file, that file may have been deleted. There are a number of ways to model this situation of an optional value. We could throw an exception, or we could return `null` when a value is not available. The disadvantage of both these methods is they don't encode any information in the type system.

We generally want to write robust programs, and in Scala we try to utilise the type system to encode properties we want our programs to maintain. One common property is "correctly handle errors". If we can encode an *optional value* in the type system, the compiler will force us to consider the case where a value is not available, thus increasing the robustness of our code.

#### Exercise: Maybe that Was a Mistake

Create a generic trait called `Maybe` of a generic type `A` with two subtypes, `Full` containing an `A`, and `Empty` containing no value. Example usage:

```scala
val perhaps: Maybe[Int] = Empty[Int]

val perhaps: Maybe[Int] = Full(1)
```

<div class="solution">
We can apply our invariant generic sum type pattern and get

```tut:book:silent
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]
```
</div>

### Take Home Points

In this section we have used generics to model sum types, product types, and optional values using generics.

These abstractions are commonly used in Scala code and have implementations in the Scala standard library. The sum type is called `Either`, products are tuples, and optional values are modelled with `Option`.

### Exercises

#### Generics versus Traits

Sum types and product types are general concepts that allow us to model almost any kind of data structure. We have seen two methods of writing these types---traits and generics. When should we consider using each?

<div class="solution">
Ultimately the decision is up to us. Different teams will adopt different programming styles. However, we look at the properties of each approach to inform our choices:

Inheritance-based approaches---traits and classes---allow us to create permanent data structures with specific types and names. We can name every field and method and implement use-case-specific code in each class. Inheritance is therefore better suited to modelling significant aspects of our programs that are re-used in many areas of our codebase.

Generic data structures---`Tuples`, `Options`, `Eithers`, and so on---are extremely broad and general purpose. There are a wide range of predefined classes in the Scala standard library that we can use to quickly model relationships between data in our code. These classes are therefore better suited to quick, one-off pieces of data manipulation where defining our own types would introduce unnecessary verbosity to our codebase.
</div>

#### Folding Maybe

In this section we implemented a sum type for modelling optional data:

```tut:book:silent
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]
```

Implement fold for this type.

<div class="solution">
The code is very similar to the implementation for `LinkedList`. I choose pattern matching in the base trait for my solution.

```tut:book:silent
object wrapper {
sealed trait Maybe[A] {
  def fold[B](full: A => B, empty: B): B =
    this match {
      case Full(v) => full(v)
      case Empty() => empty
    }
}
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]
}; import wrapper._
```
</div>

#### Folding Sum

In this section we implemented a generic sum type:

```tut:book:silent
sealed trait Sum[A, B]
final case class Left[A, B](value: A) extends Sum[A, B]
final case class Right[A, B](value: B) extends Sum[A, B]
```

Implement `fold` for `Sum`.

<div class="solution">
```tut:book:silent
object wrapper {
sealed trait Sum[A, B] {
  def fold[C](left: A => C, right: B => C): C =
    this match {
      case Left(a) => left(a)
      case Right(b) => right(b)
    }
}
final case class Left[A, B](value: A) extends Sum[A, B]
final case class Right[A, B](value: B) extends Sum[A, B]
}; import wrapper._
```
</div>
