---
layout: page
title: Generics
---

Generic types allow us to **abstract over types**. There are useful for all sorts of data structures, but commonly encountered in collections so that's where we'll start.

## Pandora's Box

Let's start with a collection that is even simpler than our list -- a box that stores a single value. We don't care what type is stored in the box, but we want to make sure we preserve that type when we get the value out of the box. To do this we use a generic type.

~~~ scala
scala> case class Box[A](value: A)
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

The syntax `[A]` is called a **type parameter**. Type parameters work in a way analagous to method parameters. When we call a method with bind the method's parameter names to the values given in the method call. When we "invoke" a class with a generic type, but creating an instance of the class, we bind the type parameters to concrete types. Wherever a type parameter occurs in class we substitute in the concrete type.

We can also add type parameters to methods, which limits the scope of the parameter to the method declaration and body:

~~~ scala
scala> def generic[A](in: A): A = in
generic: [A](in: A)A

scala> generic[String]("foo")
res10: String = foo

scala> generic(1) // again, if we omit the type parameter, scala will infer it
res11: Int = 1
~~~

<div class="callout callout-info">
#### Type Parameter Syntax

We declare generic types with a list of type names within square brackets like `[A, B, C]`. By convention we use single uppercase letters for generic types.

Generic types can be declared in a class or trait declaration in which case they are visible throughout the rest of the declaration.

~~~ scala
case class Name[A](...){ ... }
trait TraitName[A](...){ ... }
~~~

Alternatively they may be declared in a method declaration, in which case they are only visible within the method.

~~~ scala
def name[A](...){ ... }
~~~
</div>

## Arrays

*Arrays* are perhaps the best known compound data type. They represent fixed-length sequences of elements of the same type.

Scala has a built-in `Array` type that has the same underlying representation as a Java array. `Array` takes a single type parameter that determines its element type, for example `Array[Int]` or `Array[String]`.

The code below shows how to create an array, determine its length, and retrieve elements by index using its `apply` method. Notice how the return type of `apply` matches the type parameter on the array -- we always get out the type we put in:

~~~ scala
scala> val array = Array(1, 2, 3, 4 ,5)
array: Array[Int] = Array(1, 2, 3, 4, 5)

scala> array.length
res0: Int = 5

scala> array(2)
res1: Int = 3

scala> val array2 = Array("a", "b", "c")
array2: Array[String] = Array(a, b, c)

scala> array2(1)
res2: String = b
~~~

All of the items in an array must be of the same type. If we try to mix types in the constructor, Scala infers the overall type of the array as the *least common supertype* of the arguments. For example:

~~~ scala
scala> val array3 = Array(1, true, 3.0)
array3: Array[AnyVal] = Array(1, true, 3.0)

scala> array3(2)
res3: AnyVal = 3.0

scala> val array4 = Array(123, "abc")
array4: Array[Any] = Array(123, abc)

scala> array4(1)
res4: Any = abc
~~~

Again, the type of the elements retrieved from the array matches the type parameter on the array. `AnyVal` and `Any` aren't particularly useful types -- if we were using this code in a real application, we may have to use pattern matching to identify the type of data retrieved and we would lose type-safety.

## Generic Algebraic Data Types

We described type parameters as analogous to method parameters, and this analogy continues when extending a trait that has type parameters. Extending a trait, as we do in a sum type, is the type level equivalent of calling a method and we must supply values for an type parameters of the trait we're extending.

In previous sections we've seen sum types like the following:

~~~ scala
sealed trait Calculation
final case class Success(result: Double) extends Calculation
final case class Failure(reason: String) extends Calculation
~~~

Let's generalise this so that our result is not restricted to a `Double` but can be some generic type. In doing so let's change the name from `Calculation` to `Result` as we're not restricted to numeric calculations anymore. Now our data definition becomes:

A `Result` of type `A` is either a `Success` of type `A` or a `Failure` with a `String` reason. This translates to the following code

~~~ scala
sealed trait Result[A]
case class Success[A](result: A) extends Result[A]
case class Failure[A](reason: String) extends Result[A]
~~~

Notice that both `Success` and `Failure` introduce a type parameter `A` which is passed to `Result` when it is extended. `Success` also has a value of type `A`, but `Failure` only introduce `A` so it can pass it onward to `Result`. In a later section we'll introduce **variance**, giving us a cleaner way to ipmlement this, but for now this is the pattern we'll use.

<div class="callout callout-info">
#### Invariant Generic Sum Type Pattern

If `A` of type `T` is a `B` or `C` write

~~~ scala
sealed trait A[T]
final case class B[T]() extends A[T]
final case class C[T]() extends A[T]
~~~
</div>


## Exercises

#### Generic List

Our `IntList` type was defined as

~~~ scala
sealed trait IntList
final case object Empty extends IntList
final case class Cell(head: Int, tail: IntList) extends IntList
~~~

Change the name to `LinkedList` and make it generic in the type of data stored in the list.

<div class="solution">

This is an application of the generic sum type pattern.

~~~ scala
sealed trait LinkedList[A]
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
final case class Empty[A]() extends LinkedList[A]
~~~
</div>

#### Working With Generic Types

There isn't much we can do with our `LinkedList` type. Remember that types define the available operations, and with a generic type like `A` there isn't a concrete type that defines any available operations. (Remember generic types are made concrete when a class is instantiated, which is too late to make use of the information.)

However, we can still do some useful things with our `LinkedList`! Implement `length`, returning the length of the `LinkedList`.

<div class="solution">
This code is largely unchanged from the implementation of `length` on `IntList`.

~~~ scala
sealed trait LinkedList[A] {
  def length: Int
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def length: Int =
    1 + tail.length
}
final case class Empty[A]() extends LinkedList[A] {
  def length: Int =
    0
}

val example = Pair(1, Pair(2, Pair(3, Empty())))
assert(example.length == 3)
assert(example.tail.length == 2)
assert(Empty().length == 0)
~~~
</div>

On the JVM we can compare all values for equality. Implement a method `contains` that determines whether or not a given item is in the list.

<div class="solution">
This is another example of the standard structural recursion pattern. The important point is we take a parameter of type `A`.

~~~ scala
sealed trait LinkedList[A] {
  def contains(item: A): Boolean
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def contains(item: A): Boolean =
    if(head == item)
      true
    else
      tail.contains(item)
}
final case class Empty[A]() extends LinkedList[A] {
  def contains(item: A): Boolean =
    false
}

val example = Pair(1, Pair(2, Pair(3, Empty())))
assert(example.contains(3) == true)
assert(example.contains(4) == false)
assert(Empty().contains(0) == false)
~~~
</div>

Implement a method `apply` that returns the <em>n<sup>th</sup></em> item in the list

**Hint:** If you need to signal an error in your code (there's one situation in which you will need to do this), consider throwing an exception. Here is an example:

~~~ scala
throw new Exception("Bad things happened")
~~~

<div class="solution">
There are a few interesting things in this exercise. Possibly the easiest part is the use of the generic type as the return type of the `apply` method.

Next up is the `Empty` case, which the hint suggested you through an `Exception` for. Strictly speaking we should throw Java's `IndexOutOfBoundsException` in this instance, but we will shortly see a way to remove exception handling from our code altogether.

Finally we get to the actual structural recursion, which is perhaps the trickiest part. The key insight is that if the index is zero, we're selecting the current element, otherwise we subtract one from the index and recurse. We can recursively define the integers in terms of addition by one. For example, 3 = 2 + 1 = 1 + 1 + 1. Here we are performing structural recursion on the list *and* on the integers.

~~~ scala
sealed trait LinkedList[A] {
  def apply(index: Int): A
}
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A] {
  def apply(index: Int): A =
    if(index == 0)
      head
    else
      tail(index - 1)
}
final case class Empty[A]() extends LinkedList[A] {
  def apply(index: Int): A =
    throw new Exception("Attempted to get element from empty list")
}

val example = Pair(1, Pair(2, Pair(3, Empty())))
assert(example(0) == 1)
assert(example(1) == 2)
assert(example(2) == 3)
assert(try {
  example(3)
  false
} catch {
  case e: Exception => true
})
~~~
</div>