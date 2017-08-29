---
layout: page
title: "This contains That: Generics"
---

Scala gives us the ability to parameterise types. Parameterised types, known as *generic types*, are useful for representing all sorts of data structures including

We already know how to do simple aggregation in simple cases using fields. Here we start generalising over the types in our fields using **generic types**.

## Generic Types

Generic types naturally arise in collections, so let's consider a really simple collection---a box that stores a single value. We don't care what type is stored in the box, but we want to make sure we preserve that type when we get the value out of the box. To do this we use a generic type.

```tut
case class Box[A](value: A)

val a = Box(2)

a.value

val b = Box("hi") // if we omit the type parameter, scala will infer its value

b.value
```

The syntax `[A]` is called a **type parameter**---it binds a name to a type. Wherever `A` occurs in our class definition we will substitute in the same type. This works in the same way that binding a name to a value (using `val`) allows us to substitute in the value wherever the name occurs. The only difference is that we're operating on types rather than values.

We can also add type parameters to methods, which limits the scope of the parameter to the method declaration and body:

```tut
def generic[A](in: A): A = in

generic[String]("foo")

generic(1) // again, if we omit the type parameter, scala will infer it
```

## Arrays

*Arrays* are perhaps the best known compound data type. They represent fixed-length sequences of elements of the same type.

Scala has a built-in `Array` type that has the same underlying representation as a Java array. `Array` takes a single type parameter that determines its element type, for example `Array[Int]` or `Array[String]`.

The code below shows how to create an array, determine its length, and retrieve elements by index using its `apply` method. Notice how the return type of `apply` matches the type parameter on the array---we always get out the type we put in:

```tut
val array = Array(1, 2, 3, 4 ,5)

array.length

array(2)

val array2 = Array("a", "b", "c")

array2(1)
```

All of the items in an array must be of the same type. If we try to mix types in the constructor, Scala infers the overall type of the array as the *least common supertype* of the arguments. For example:

```tut
val array3 = Array(1, true, 3.0)

array3(2)

val array4 = Array(123, "abc")

array4(1)
```

Again, the type of the elements retrieved from the array matches the type parameter on the array. `AnyVal` and `Any` aren't particularly useful types---if we were using this code in a real application, we may have to use pattern matching to identify the type of data retrieved.

## Linked Lists

A linked list is another type of generic sequence, similar to an array. Unlike an array, however, a linked list is stored internally as a chain of pairs. For example, the sequence `1, 2, 3` would be represented as follows:

<img src="src/pages/traits/linked-list.svg" alt="A linked list" />

In this example we have four links in our chain. `d` represents an empty list, and `a`, `b`, and `c` are pairs built on top of it:

```scala
val d = End()
val c = Pair(3, d)
val b = Pair(2, c)
val a = Pair(1, b)
```

In addition to being links in a chain, these data structures all represent complete sequences of integers:

 - `a` represents the sequence `1, 2, 3`
 - `b` represents the sequence `2, 3`
 - `c` represents the sequence `3` (only one element)
 - `d` represents an empty sequence

Using this implementation, we can build lists of arbitrary length by repeatedly taking an existing list and prepending a new element[^list].

[^list]: This is how Scala's built-in `List` data structure works. We will be introduced to `List` in the chapter on *Collections*.

### Exercise: Making a List

To implement a `LinkedList` in Scala we need to combine our newfound knowledge of generic types with our existing knowledge of sealed traits. We can define a linked list as a sealed trait `LinkedList[A]` with two subtypes:

 - a class `Pair[A]` with two fields, `head` and `tail`:
    - `head` is the item at this position in the list;
    - `tail` is another `LinkedList[A]`---either another `Pair` or an `Empty`;
 - a class `Empty[A]` with no fields.

Start by writing the simplest trait and classes you can so that you can build a list. You should be able to use your implementation as follows:

```scala
val list: LinkedList[Int] = Pair(1, Pair(2, Pair(3, End())))

list.isInstanceOf[LinkedList[Int]] // returns true

list.head      // returns 1 as an Int
list.tail.head // returns 2 as an Int
list.tail.tail // returns Pair(3, End()) as a LinkedList[Int]
```

<div class="solution">
Here is the model solution. `Empty` doesn't contain any data so it may seem more natural to define it as a singleton object. However, objects can't have type parameters so we have to define it as a class. We'll be able to work around this later when we learn about something called *variance* :

```tut:book:silent
sealed trait LinkedList[A]

final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]

final case class Empty[A]() extends LinkedList[A]
```
</div>

### Exercise: Checking it Twice

Now we have our `LinkedList` class, let's give it some useful methods. Define the following:

 - a method called `length` that returns the length of the list;
 - a method `apply` that returns the <em>n<sup>th</sup></em> item in the list;
 - a method `contains` that determines whether or not an item is in the list.

In each case, start by writing an abstract method definition in `LinkedList`. Think about the types of the arguments and the types of the results. Then implement the method for `Empty`---it should be pretty easy to provide a default implementation for an empty list. Finally, implement the method on `Pair`. The implementation will be recursive and defined in terms of `head` and `tail`.

**Hint:** If you need to signal an error in your code (there's one situation in which you will need to do this), consider throwing an exception. Here is an example:

```tut:book:fail
throw new Exception("Bad things happened")
```

<div class="solution">
The hint about exceptions was for the implementation of `apply` in `Empty`. The list is empty---there is no <em>n<sup>th</sup></em> element to return!

Strictly speaking we should throw Java's `IndexOutOfBoundsException` in this instance, but we will shortly see a way to remove exception handling from our code altogether.

```tut:book:silent
sealed trait LinkedList[A] {
  def head: A
  def tail: LinkedList[A]

  def length: Int =
    tail.length + 1

  def apply(index: Int): A =
    if(index == 0) head else tail(index - 1)

  def contains(item: A): Boolean =
    (item == head) || (tail contains item)
}

case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]

case class Empty[A]() extends LinkedList[A] {
  def head = throw new Exception("Attempt to get head of an empty list!")
  def tail = throw new Exception("Attempt to get head of an empty list!")

  override def length = 0
  override def contains(item: A) = false
}
```
</div>
