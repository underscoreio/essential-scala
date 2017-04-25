## Generics

Generic types allow us to *abstract over types*. There are useful for all sorts of data structures, but commonly encountered in collections so that's where we'll start.

### Pandora's Box

Let's start with a collection that is even simpler than our list---a box that stores a single value. We don't care what type is stored in the box, but we want to make sure we preserve that type when we get the value out of the box. To do this we use a generic type.

```tut:book:silent
final case class Box[A](value: A)
```

```tut:book
Box(2)

res0.value

Box("hi") // if we omit the type parameter, scala will infer its value

res2.value
```

The syntax `[A]` is called a *type parameter*. We can also add type parameters to methods, which limits the scope of the parameter to the method declaration and body:

```tut:book:silent
def generic[A](in: A): A = in
```

```tut:book
generic[String]("foo")

generic(1) // again, if we omit the type parameter, scala will infer it
```

Type parameters work in a way analogous to method parameters. When we call a method we bind the method's parameter names to the values given in the method call. For example, when we call `generic(1)` the name `in` is bound to the value `1` within the body of `generic`.

When we call a method or construct a class with a type parameter, the type parameter is bound to the concrete type within the method or class body. So when we call `generic(1)` the type parameter `A` is bound to `Int` in the body of `generic`.

<div class="callout callout-info">
#### Type Parameter Syntax {-}

We declare generic types with a list of type names within square brackets like `[A, B, C]`. By convention we use single uppercase letters for generic types.

Generic types can be declared in a class or trait declaration in which case they are visible throughout the rest of the declaration.

```scala
case class Name[A](...){ ... }
trait Name[A]{ ... }
```

Alternatively they may be declared in a method declaration, in which case they are only visible within the method.

```scala
def name[A](...){ ... }
```
</div>

### Generic Algebraic Data Types

We described type parameters as analogous to method parameters, and this analogy continues when extending a trait that has type parameters. Extending a trait, as we do in a sum type, is the type level equivalent of calling a method and we must supply values for any type parameters of the trait we're extending.

In previous sections we've seen sum types like the following:

```tut:book:silent
sealed trait Calculation
final case class Success(result: Double) extends Calculation
final case class Failure(reason: String) extends Calculation
```

Let's generalise this so that our result is not restricted to a `Double` but can be some generic type. In doing so let's change the name from `Calculation` to `Result` as we're not restricted to numeric calculations anymore. Now our data definition becomes:

A `Result` of type `A` is either a `Success` of type `A` or a `Failure` with a `String` reason. This translates to the following code

```tut:book:silent
sealed trait Result[A]
case class Success[A](result: A) extends Result[A]
case class Failure[A](reason: String) extends Result[A]
```

Notice that both `Success` and `Failure` introduce a type parameter `A` which is passed to `Result` when it is extended. `Success` also has a value of type `A`, but `Failure` only introduces `A` so it can pass it onward to `Result`. In a later section we'll introduce *variance*, giving us a cleaner way to implement this, but for now this is the pattern we'll use.

<div class="callout callout-info">
#### Invariant Generic Sum Type Pattern {-}

If `A` of type `T` is a `B` or `C` write

```tut:book:silent
sealed trait A[T]
final case class B[T]() extends A[T]
final case class C[T]() extends A[T]
```
</div>

```tut:invisible:reset
// clear the types defined so far
```

### Exercises

#### Generic List

Our `IntList` type was defined as

```tut:book:silent
sealed trait IntList
final case object End extends IntList
final case class Pair(head: Int, tail: IntList) extends IntList
```

Change the name to `LinkedList` and make it generic in the type of data stored in the list.

<div class="solution">

This is an application of the generic sum type pattern.

```tut:book:silent
sealed trait LinkedList[A]
final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
final case class End[A]() extends LinkedList[A]
```
</div>

#### Working With Generic Types

There isn't much we can do with our `LinkedList` type. Remember that types define the available operations, and with a generic type like `A` there isn't a concrete type to define any available operations. (Generic types are made concrete when a class is instantiated, which is too late to make use of the information in the definition of the class.)

However, we can still do some useful things with our `LinkedList`! Implement `length`, returning the length of the `LinkedList`. Some test cases are below.

```scala
val example = Pair(1, Pair(2, Pair(3, End())))
assert(example.length == 3)
assert(example.tail.length == 2)
assert(End().length == 0)
```

<div class="solution">
This code is largely unchanged from the implementation of `length` on `IntList`.

```tut:book:silent
object solution {
  sealed trait LinkedList[A] {
    def length: Int =
      this match {
        case Pair(hd, tl) => 1 + tl.length
        case End() => 0
      }
  }
  final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
  final case class End[A]() extends LinkedList[A]
}
```
</div>

On the JVM we can compare all values for equality. Implement a method `contains` that determines whether or not a given item is in the list. Ensure your code works with the following test cases:

```scala
val example = Pair(1, Pair(2, Pair(3, End())))
assert(example.contains(3) == true)
assert(example.contains(4) == false)
assert(End().contains(0) == false)
// This should not compile
// example.contains("not an Int")
```

<div class="solution">
This is another example of the standard structural recursion pattern. The important point is `contains` takes a parameter of type `A`.

```tut:book:silent
object solution {
  sealed trait LinkedList[A] {
    def contains(item: A): Boolean =
      this match {
        case Pair(hd, tl) =>
          if(hd == item)
            true
          else
            tl.contains(item)
        case End() => false
      }
  }

  final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
  final case class End[A]() extends LinkedList[A]
}
```
</div>

Implement a method `apply` that returns the <em>n<sup>th</sup></em> item in the list

**Hint:** If you need to signal an error in your code (there's one situation in which you will need to do this), consider throwing an exception. Here is an example:

```tut:book:fail:silent
throw new Exception("Bad things happened")
```

Ensure your solution works with the following test cases:

```scala
val example = Pair(1, Pair(2, Pair(3, End())))
assert(example(0) == 1)
assert(example(1) == 2)
assert(example(2) == 3)
assert(try {
  example(3)
  false
} catch {
  case e: Exception => true
})
```

<div class="solution">
There are a few interesting things in this exercise. Possibly the easiest part is the use of the generic type as the return type of the `apply` method.

Next up is the `End` case, which the hint suggested you through an `Exception` for. Strictly speaking we should throw Java's `IndexOutOfBoundsException` in this instance, but we will shortly see a way to remove exception handling from our code altogether.

Finally we get to the actual structural recursion, which is perhaps the trickiest part. The key insight is that if the index is zero, we're selecting the current element, otherwise we subtract one from the index and recurse. We can recursively define the integers in terms of addition by one. For example, 3 = 2 + 1 = 1 + 1 + 1. Here we are performing structural recursion on the list *and* on the integers.

```tut:book:silent
object solution {
  sealed trait LinkedList[A] {
    def apply(index: Int): A =
      this match {
        case Pair(hd, tl) =>
          if(index == 0)
            hd
          else
            tl(index - 1)
        case End() =>
          throw new Exception("Attempted to get element from an Empty list")
      }
  }
  final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
  final case class End[A]() extends LinkedList[A]
}
```
</div>

Throwing an exception isn't cool. Whenever we throw an exception we lose type safety as there is nothing in the type system that will remind us to deal with the error. It would be much better to return some kind of result that encodes we can succeed or failure. We introduced such a type in this very section.

```tut:book:silent
sealed trait Result[A]
case class Success[A](result: A) extends Result[A]
case class Failure[A](reason: String) extends Result[A]
```

Change `apply` so it returns a `Result`, with a failure case indicating what went wrong. Here are some test cases to help you:

```scala
assert(example(0) == Success(1))
assert(example(1) == Success(2))
assert(example(2) == Success(3))
assert(example(3) == Failure("Index out of bounds"))
```

<div class="solution">
```tut:book:silent
object solution {
  sealed trait Result[A]
  case class Success[A](result: A) extends Result[A]
  case class Failure[A](reason: String) extends Result[A]

  sealed trait LinkedList[A] {
    def apply(index: Int): Result[A] =
      this match {
        case Pair(hd, tl) =>
          if(index == 0)
            Success(hd)
          else
            tl(index - 1)
        case End() =>
          Failure("Index out of bounds")
      }
  }
  final case class Pair[A](head: A, tail: LinkedList[A]) extends LinkedList[A]
  final case class End[A]() extends LinkedList[A]
}
```
</div>
