---
layout: page
title: Variance
---
In this section we cover **variance annotations**, which allow us to control subclass relationships between types with type parameters. To motivate this, let's look again at our invariant generic sum type pattern.

The invariant generic sum type pattern is a bit unsatisfying. Recall our `Maybe` type, which we defined as

~~~ scala
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]
~~~

Ideally we would like to drop the unused type parameter on `Empty` and write something like

~~~ scala
sealed trait Maybe[A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[???]
~~~

Objects can't have type parameters. In order to make `Empty` an object we need to provide a concrete type in the `extends Maybe` part of the definition. But what type parameter should we use? In the absence of a preference for a particular data type, we could use something like `Unit` or `Nothing`. However this leads to type errors:

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

The problem here is that `Empty` is a `Maybe[Nothing]` and a `Maybe[Nothing]` is not a subtype of `Maybe[Int]`. To overcome this issue we need to introduce variance annotations.

## Invariance, Covariance, and Contravariance

<div class="alert alert-warning">
**Note:** Variance is one of the trickier aspects of Scala's type system. Although it is useful to be aware of its existence, we rarely have to use it in application code.
</div>

If we have some type `Foo[A]`, and `A` is a subtype of `B`, is `Foo[A]` a subtype of `Foo[B]`? The answer depends on the *variance* of the type `Foo`. The variance of a generic type determines how its supertype/subtype relationships change with respect with its type parameters:

A type `Foo[T]` is **invariant** in terms of `T`, meaning that the types `Foo[A]` and `Foo[B]` are unrelated regardless of the relationship between `A` and `B`. This is the default variance of any generic type in Scala.

A type `Foo[+T]` is **covariant** in terms of `T`, meaning that `Foo[A]` is a supertype of `Foo[B]` if `A` is a supertype of `B`. Most Scala collection classes are covariant in terms of their contents. We'll see these next chapter.

A type `Foo[-T]` is **contravariant** in terms of `T`, meaning that `Foo[A]` is a *subtype* of `Foo[B]` if `A` is a *supertype* of `B`. The only example of contravariance that I am aware of is function arguments.

## Function Types

When we discussed function types we glossed over how exactly they are implemented. Scala has 23 built-in generic classes for functions of 0 to 22 arguments. Here's what they look like:

~~~ scala
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
~~~

Functions are contravariant in terms of their arguments and covariant in terms of their return type. This seems counterintuitive but it makes sense if we look at it from the point of view of function arguments. Consider some code that expects a `Function1[A, B]`:

~~~ scala
class Box[A](value: A) {
  /** Apply `func` to `value`, returning a `Box` of the result. */
  def map[B](func: Function1[A, B]): Box[B] =
    Box(func(a))
}
~~~

To understand variance, consider what functions can we safely pass to this `map` method:


 - A function from `A` to `B` is clearly ok.

 - A function from `A` to a subtype of `B` is ok because it's result type will have all the properties of `B` that we might depend on. This indicates that functions are covariant in their result type.

 - A function expecting a supertype of `A` is also ok, because the `A` we have in the Box will have all the properties that the function expects.

 - A function expecting a subtype of `A` is not ok, because our value may in reality be a different subtype of `A`.


## Covariant Sum Types

Now we know about variance annotations we can solve our problem with `Maybe` by making it covariant.

~~~ scala
sealed trait Maybe[+A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]
~~~

In use we get the behaviour we expect. `Empty` is a subtype of all `Full` values.

~~~ scala
scala> val perhaps: Maybe[Int] = Empty
perhaps: Maybe[Int] = Empty
~~~

This pattern is the most commonly used one with generic sum types.

<div class="callout callout-info">
#### Covariant Generic Sum Type Pattern

If `A` of type `T` is a `B` or `C`, and `C` is not generic, write

~~~ scala
sealed trait A[+T]
final case class B[T](t: T) extends A[T]
final case object C extends A[Nothing]
~~~

This pattern extends to more than one type parameter. If a type parameter is not needed for a specific case of a sum type, we can substitute `Nothing` for that parameter.
</div>


## Contravariant Position

There is another pattern we need to learn for covariant sum types, which involves the interaction of covariant type parameters and contravariant method and function parameters. To illustrate this issue let's develop a covariant `Sum`.

#### Exercise: Covariant Sum

Implement a covariant `Sum` using the covariant generic sum type pattern.

<div class="solution">
~~~ scala
sealed trait Sum[+A, +B]
final case class Failure[A](value: A) extends Sum[A, Nothing]
final case class Success[B](value: B) extends Sum[Nothing, B]
~~~
</div>

Now let's see what happens when we implement `flatMap` on `Sum`.

#### Exercise: Some sort of flatMap

Implement `flatMap` and verify you receive an error like

~~~ scala
error: covariant type A occurs in contravariant position in type B => Sum[A,C] of value f
  def flatMap[C](f: B => Sum[A, C]) =
                 ^
~~~

<div class="solution">
~~~ scala
sealed trait Sum[+A, +B] {
  def flatMap[C](f: B => Sum[A, C]) =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => f(v)
    }
}
final case class Failure[A](value: A) extends Sum[A, Nothing]
final case class Success[B](value: B) extends Sum[Nothing, B]
~~~
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


## Exercises

We're going to return to the interpreter example we saw at the end of the last chapter. This time we're going to use the general abstractions we've created here, and our new knowledge of `map`, `flatMap`, and `fold`.

We're going to represent calculations as `Sum[String, Double]`, where the `String` is an error message. Last time we saw `Sum` we had this definition:

Now implement `flatMap` using the same logic as `map`. The obvious implementation should lead to an error

~~~ scala
error: covariant type A occurs in contravariant position in type B => Sum[A,C] of value f
~~~

This takes some explaining. Remember that functions are contravariant in their input parameters and covariant in their result. In this case `A` appears in the result so it isn't in contravariant position in the function we pass to `flatMap`. However, `flatMap` is a method and methods are like functions in terms of contra- and covariance. So the function we pass to `flatMap` is in a contravariant position, and this leads to error message we see. The solution is introduce a new type called, say, `AA` along with a type bound `AA >: A`. That is, `flatMap` should have declaration

~~~ scala
def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C]
~~~

<div class="solution">
~~~ scala
sealed trait Sum[+A, +B] {
  def fold[C](failure: A => C, success: B => C): C =
    this match {
      case Failure(v) => failure(v)
      case Success(v) => success(v)
    }
  def map[C](f: B => C): Sum[A, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => Success(f(v))
    }
  def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => f(v)
    }
}
~~~
</div>

That was involved, but we've now seen an important pattern that you'll find throughout the Scala standard library.
Now we're going to reimplement that calculator from last time. We have an abstract syntax tree defined via the following algebraic data type:

~~~ scala
sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Double) extends Expression
~~~

Now implement a method `eval: Sum[String, Double]` on `Expression`. Use `flatMap` and `map` on `Sum` and introduce any utility methods you see fit to make the code more compact. Here are some test cases:

~~~ scala
assert(Addition(Number(1), Number(2)).eval == Success(3))
assert(SquareRoot(Number(-1)).eval == Failure("Square root of negative number"))
assert(Division(Number(4), Number(0)).eval == Failure("Division by zero"))
assert(Division(Addition(Subtraction(Number(8), Number(6)), Number(2)), Number(2)).eval == Success(2.0))
~~~

<div class="solution">
Here's my solution. I used a helper method `lift2` to "lift" a function into the result of two expressions. I hope you'll agree the code is both more compact and easier to read than our previous solution!

~~~ scala
sealed trait Expression {
  def eval: Sum[String, Double] =
    this match {
      case Addition(l, r) => lift2(l, r, (left, right) => Success(left + right))
      case Subtraction(l, r) => lift2(l, r, (left, right) => Success(left - right))
      case Division(l, r) => lift2(l, r, (left, right) =>
        if(right == 0)
          Failure("Division by zero")
        else
          Success(left / right)
      )
      case SquareRoot(v) =>
        v.eval flatMap { value =>
          if(value < 0)
            Failure("Square root of negative number")
          else
            Success(Math.sqrt(value))
        }
      case Number(v) => Success(v)
    }

  def lift2(l: Expression, r: Expression, f: (Double, Double) => Sum[String, Double]) =
    l.eval flatMap { left =>
      r.eval flatMap { right =>
        f(left, right)
      }
    }
}
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Int) extends Expression
~~~
</div>
