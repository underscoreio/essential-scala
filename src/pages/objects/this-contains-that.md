---
layout: page
title: This contains That
---

In this section we'll look at data that contains other data. We already know how to this in simple cases using fields. Here we are going to look when we want to give the user freedom about the type we contain. In other words, we're going to look at generic types.

## Generic Types

Generic types naturally arise in collections, so let's consider a really simple collection: a box that stores a single value. We don't care what type is stored in the box, but we want to make sure we preserve that type when we get the value out of the box. To do this we use a generic type.

~~~ scala
scala> case class Box[A](val get: A)
defined class Box

scala> Box(2).get
res5: Int = 2

scala> Box("hi").get
res6: String = hi
~~~

The syntax `[A]` introduces the generic type, binding a name to a type. Wherever `A` occurs in our class definition we will substitute in the same type. This works in the same way that binding a name to a value (using `val`) allows us to substitute in the value wherever the name occurs. The only difference is that we're operating on types rather than values.

We can also declare generic types on methods like so:

~~~ scala
scala> def generic[A](in: A): A = in
generic: [A](in: A)A

scala> generic("foo")
res10: String = foo

scala> generic(1)
res11: Int = 1
~~~

## Exercise

Implement a class called `Tuple` that stores two value `one` and `two`. `Tuple` should be generic in both arguments. Example usage:

~~~ scala
scala> val tuple = Tuple("hi", 2)
tuple: Tuple[String,Int] = Tuple(hi,2)

scala> tuple.one
res13: String = hi

scala> tuple.two
res14: Int = 2
~~~

<div class="solution">
~~~ scala
case class Tuple[A, B](val one: A, val two: B)
~~~
</div>

## Tuples

Scala includes generic tuple types with up to 22 elements, along with special syntax for creating them. The classes are `Tuple1` through to `Tuple22` but they are usually written as `(A, B, ...)` for generic types `A`, `B`, and so on. For example:

~~~ scala
scala> ("hi", 1)
res19: (String, Int) = (hi,1)

scala> ("hi", 1, true)
res20: (String, Int, Boolean) = (hi,1,true)
~~~

We can define methods that accept tuples using this same syntax.

~~~ scala
scala> def tuplized[A, B](in: (A, B)) = in._1
tuplized: [A, B](in: (A, B))A

scala> tuplized(("a", 1))
res21: String = a
~~~

Pattern matching is the usual way of destructing a tuple, but we can use accessors `_1`, `_2`, and so on as in the example above.

Tuples are the most generic **product type**, which you will recall we discussed in the previous section.

## Exercise

#### Generic Sum Type

Implement a generic **sum type** for two cases. Call the two cases `Left` and `Right` respectively, and the overall type `Sum`. Example usage:

~~~ scala
scala> Left[Int, String](1).get
res24: Int = 1

scala> Right[Int, String]("foo").get
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
~~~ scala
sealed trait Sum[A, B]
final case class Left[A, B](val get: A) extends Sum[A, B]
final case class Right[A, B](val get: B) extends Sum[A, B]
</div>
~~~

Scala has the generic sum type `Either` for two cases, but it does not have types for more cases.
</div>

#### Generic Functions

Add a method `map` to `Box` that takes a function of type `A => B` and returns a `Box[B]`. Example usage:

~~~ scala
scala> Box(2) map (x => x.toString)
res27: Box[String] = Box(2)
~~~

<div class="solution">
~~~ scala
case class Box[A](val get: A) {
  def map[B](f: A => B): Box[B] =
      Box(f(get))
}
~~~
</div>

## Type Bounds

It is sometimes useful to constrain a generic type. We can do this with type bounds indicating that a generic type should be a sub- or super-type of some given types. The syntax is `A <: Type` to declare `A` must be a subtype of `Type` and `A >: Type` to declare a supertype.

## Variance Annotations

Consider our `Box` type and the type `Foo` declared below.

~~~ scala
sealed trait Foo()
final case class Ex1() extends Foo
final case class Ex2() extends Foo
~~~

Is a `Box[Ex1]` a subtype of `Box[Foo]`? Let's ask the REPL.

~~~ scala
scala> def fooIt(in: Box[Foo]): Foo = in.get
fooIt: (in: Box[Foo])Foo

scala> val box = Box(Ex1())
box: Box[Ex1] = Box(Ex1())

scala> fooIt(box)
<console>:15: error: type mismatch;
 found   : Box[Ex1]
 required: Box[Foo]
Note: Ex1 <: Foo, but class Box is invariant in type A.
You may wish to define A as +A instead. (SLS 4.5)
              fooIt(box)
                    ^
~~~

This interaction might be surprising. `Ex1` is a subtype of `Foo` so we might expect `Box[Ex1]` to be a subtype of `Box[Foo]`. Subtyping relationships with generic types are subtle. By default generic types in Scala are **invariant**, meaning that for a type `F[A]` neither subtypes nor supertypes of `A` make a subtype of `F[A]`. This is the behaviour we're seeing with `Box`.

**Covariance** is the behaviour most people expect. For a covariant type `F` a subtype of `A` is a subtype of `F[A]`. We can make a generic type covariant by introducing a generic type as `+A` instead of `A`.

## Exercise

Make `Box` covariant.

<div class="solution">
~~~ scala
case class Box[+A](val get: A)
~~~
</div>

**Contravariance** is the opposite of covariance. For a contravariant type `F` a supertype of `A` is a subtype of `F[A]`. Why would we ever want contravariance? Consider a function `f: A => B`. What functions can we safely use in place of this function? A function returning a subtype of `B` is ok, because it's result type will have all the properties of `B` that we might depend on. A function expecting a supertype of `A` is also ok. A function expecting a subtype of `A` is not ok, however, as it will expect properties of its input that we do not enforce. Thus functions are covariant in their return type but contravariant in their input type. We annotate contravariance as `-A` for a type `A`.

There is no doubt that variance is confusing to many. The good news is you don't have to use it in your code. You might have to add a few type declarations to get code to work without variance annotations, but that's it. For example, here's the code above with type declarations to make it compile.

~~~ scala
scala> val box : Box[Foo] = Box(Ex1())
box: Box[Foo] = Box(Ex1())

scala> fooIt(box)
res32: Foo = Ex1()
~~~
