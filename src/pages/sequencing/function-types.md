---
layout: page
title: Function Types
---

In the previous section we described how to write function literals and function types, but we glossed over how exactly function types are implemented. Scala has 23 built-in generic classes for functions of 0 to 22 arguments. Here's what they look like:

```tut:book:silent
trait Function0[+R] {
  def apply: R
}

trait Function1[-A, +B] {
  def apply(a: A): B
}

trait Function2[-A, -B, +C] {
  def apply(a: A, b: B): C
}

// and so on...
```

For the most part this is all stuff we know. The only pieces of syntax we haven't seen are the `+` and `-` annotations on the type parameters. These are called *variance annotations*. Let's look at this for a moment.

## Invariance, Covariance and Contravariance

<div class="callout callout-info">
#### Variance is Hard {-}

Variance is one of the trickier aspects of Scala's type system. Although it is useful to be aware of its existence, we rarely have to use it in application code.
</div>

If we have some type `Foo[A]`, and `A` is a subtype of `B`, is `Foo[A]` a subtype of `Foo[B]`? The answer depends on the *variance* of the type `Foo`. The variance of a generic type determines how its supertype/subtype relationships change with respect with its type parameters:

A type `Foo[T]` is **invariant** in terms of `T`, meaning that the types `Foo[A]` and `Foo[B]` are unrelated regardless of the relationship between `A` and `B`. This is the default variance of any generic type in Scala.

A type `Foo[+T]` is **covariant** in terms of `T`, meaning that `Foo[A]` is a supertype of `Foo[B]` if `A` is a supertype of `B`. Most Scala collection classes are covariant in terms of their contents. We'll see these next chapter.

A type `Foo[-T]` is **contravariant** in terms of `T`, meaning that `Foo[A]` is a *subtype* of `Foo[B]` if `A` is a *supertype* of `B`. The only example of contravariance that I am aware of is function arguments.

Functions are contravariant in terms of their arguments and covariant in terms of their return type. This seems counterintuitive but it makes sense if we look at it from the point of view of function arguments. Consider some code that expects a `Function1[A, B]`:

```tut:book:silent
case class Box[A](value: A) {
  /** Apply `func` to `value`, returning a `Box` of the result. */
  def map[B](func: Function1[A, B]): Box[B] =
    Box(func(value))
}
```

To understand variance, consider what functions can we safely pass to this `map` method:


 - A function from `A` to `B` is clearly ok.

 - A function from `A` to a subtype of `B` is ok because it's result type will have all the properties of `B` that we might depend on. This indicates that functions are covariant in their result type.

 - A function expecting a supertype of `A` is also ok, because the `A` we have in the Box will have all the properties that the function expects.

 - A function expecting a subtype of `A` is not ok, because our value may in reality be a different subtype of `A`.

## Methods on Functions

As functions are instances of a `Function` class we might ask what methods we get from the class apart from the `apply` method we define. The answer is not much that we'll use very often, but there are few that are useful to know about:

- for functions of a single parameters we can compose them together to apply one function to the result of another

  ```tut:book
  val f = ((x: Int) => x + 1).compose((x: Int) => x * 2)

  f(2)

  val f = ((x: Int) => x + 1).andThen((x: Int) => x * 2)

  f(2)
  ```

- for functions with two or more parameters we can convert them to functions that accepts a tuple of arguments, or a "curried" function which takes a single argument and returns a function.

  ```tut:book
  val f = ((x: Int, y: Int) => x + y).curried

  f(1)

  f(1)(2)

  val f = ((x: Int, y: Int) => x + y).tupled

  f( (1, 2) )
  ```

## Exercises

#### Covariance and Contravariance

```tut:invisible
object catExample {
  trait Animal
  trait Cat extends Animal { val color: String; val food: String }
  object Cat { def apply(aColor: String, aFood: String) = new Cat { val color = aColor; val food = aFood } }
  trait Siamese extends Cat

  trait Sound
  trait CatSound extends Sound
  trait Purr extends Sound
}
import catExample._
```

Using the notation `A <: B` to indicate `A` is a subtype of `B` and assuming:

- `Siamese <: Cat <: Animal`; and
- `Purr <: CatSound <: Sound`

if I have a method

```tut:book:silent
def groom(groomer: Cat => CatSound): CatSound = {
  val oswald = Cat("Black", "Cat food")
  groomer(oswald)
}
```

which of the following can I pass to `groom`?

- A function of type `Animal => Purr`
- A function of type `Siamese => Purr`
- A function of type `Animal => Sound`

<div class="solution">
The only function that will work is the the function of type `Animal => Purr`. The `Siamese => Purr` function will not work because the Oswald is a not a Siamese cat. The `Animal => Sound` function will not work because we require the return type is a `CatSound`.
</div>
