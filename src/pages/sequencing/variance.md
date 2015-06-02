## Variance

In this section we cover *variance annotations*, which allow us to control subclass relationships between types with type parameters. To motivate this, let's look again at our invariant generic sum type pattern.

Recall our `Maybe` type, which we defined as

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


### Invariance, Covariance, and Contravariance

<div class="alert alert-warning">
**Note:** Variance is one of the trickier aspects of Scala's type system. Although it is useful to be aware of its existence, we rarely have to use it in application code.
</div>

If we have some type `Foo[A]`, and `A` is a subtype of `B`, is `Foo[A]` a subtype of `Foo[B]`? The answer depends on the *variance* of the type `Foo`. The variance of a generic type determines how its supertype/subtype relationships change with respect with its type parameters:

A type `Foo[T]` is *invariant* in terms of `T`, meaning that the types `Foo[A]` and `Foo[B]` are unrelated regardless of the relationship between `A` and `B`. This is the default variance of any generic type in Scala.

A type `Foo[+T]` is *covariant* in terms of `T`, meaning that `Foo[A]` is a supertype of `Foo[B]` if `A` is a supertype of `B`. Most Scala collection classes are covariant in terms of their contents. We'll see these next chapter.

A type `Foo[-T]` is *contravariant* in terms of `T`, meaning that `Foo[A]` is a *subtype* of `Foo[B]` if `A` is a *supertype* of `B`. The only example of contravariance that I am aware of is function arguments.

### Function Types

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


### Covariant Sum Types

Now we know about variance annotations we can solve our problem with `Maybe` by making it covariant.

~~~ scala
sealed trait Maybe[+A]
final case class Full[A](value: A) extends Maybe[A]
final case object Empty extends Maybe[Nothing]
~~~

In use we get the behaviour we expect. `Empty` is a subtype of all `Full` values.

~~~ scala
val perhaps: Maybe[Int] = Empty
// perhaps: Maybe[Int] = Empty
~~~

This pattern is the most commonly used one with generic sum types. We should only use covariant types where the container type is immutable. If the container allows mutation we should only use invariant types.

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


### Contravariant Position

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
  def flatMap[C](f: B => Sum[A, C]): Sum[A, C] =
                 ^
~~~

<div class="solution">
~~~ scala
sealed trait Sum[+A, +B] {
  def flatMap[C](f: B => Sum[A, C]): Sum[A, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => f(v)
    }
}
final case class Failure[A](value: A) extends Sum[A, Nothing]
final case class Success[B](value: B) extends Sum[Nothing, B]
~~~
</div>

What is going on here? Let's momentarily switch to a simpler example that illustrates the problem.

~~~ scala
case class Box[+A](value: A) {
  def set(a: A): Box[A] = Box(a)
}
~~~

which causes the error

~~~ scala
error: covariant type A occurs in contravariant position in type A of value a
  def set(a: A): Box[A] = Box(a)
          ^
~~~

Remember that functions, and hence methods, which are just like functions, are contravariant in their input parameters. In this case we have specified that `A` is covariant but in `set` we have a parameter of type `A` and the type rules requires `A` to be contravariant here. This is what the compiler means by a "contravariant position".

The solution is introduce a new type that is a supertype of `A`. We can do this with the notation `[AA >: A]` like so:

~~~ scala
case class Box[+A](value: A) {
  def set[AA >: A](a: AA): Box[AA] = Box(a)
}
~~~

This successfully compiles.

Back to `flatMap`, the function `f` is a parameter, and thus in a contravariant position. This means we accept *supertypes* of `f`. It is declared with type `B => Sum[A, C]` and thus a supertype is *covariant* in `B` and *contravariant* in `A` and `C`. `B` is declared as covariant, so that is fine. `C` is invariant, so that is fine as well. `A` on the other hand is covariant but in a contravariant position. Thus we have to apply the same solution we did for `Box` above.

~~~ scala
sealed trait Sum[+A, +B] {
  def flatMap[AA >: A, C](f: B => Sum[AA, C]): Sum[AA, C] =
    this match {
      case Failure(v) => Failure(v)
      case Success(v) => f(v)
    }
}
final case class Failure[A](value: A) extends Sum[A, Nothing]
final case class Success[B](value: B) extends Sum[Nothing, B]
~~~

<div class="callout callout-info">
#### Contravariant Position Pattern

If `A` of a covariant type `T` and a method `f` of `A` complains that `T` is used in a contravariant position, introduce a type `TT >: T` in `f`.

~~~ scala
case class A[+T] {
  def f[TT >: T](t: TT): A[TT]
}
~~~
</div>


### Type Bounds

We have see some type bounds above, in the contravariant position pattern. Type bounds extend to specify subtypes as well as supertypes. The syntax is `A <: Type` to declare `A` must be a subtype of `Type` and `A >: Type` to declare a supertype.

For example, the following type allows us to store a `Visitor` or any subtype:

~~~ scala
case class WebAnalytics[A <: Visitor](
  visitor: A,
  pageViews: Int,
  searchTerms: List[String],
  isOrganic: Boolean
)
~~~


### Exercises

#### Covariance and Contravariance

Using the notation `A <: B` to indicate `A` is a subtype of `B` and assuming:

- `Siamese <: Cat <: Animal`; and
- `Purr <: CatSound <: Sound`

if I have a method

~~~ scala
def groom(groomer: Cat => CatSound): CatSound =
  val oswald = Cat("Black", "Cat food")
  groomer(oswald)
}
~~~

which of the following can I pass to `groom`?

- A function of type `Animal => Purr`
- A function of type `Siamese => Purr`
- A function of type `Animal => Sound`

<div class="solution">
The only function that will work is the the function of type `Animal => Purr`. The `Siamese => Purr` function will not work because the Oswald is a not a Siamese cat. The `Animal => Sound` function will not work because we require the return type is a `CatSound`.
</div>


#### Calculator Again

We're going to return to the interpreter example we saw at the end of the last chapter. This time we're going to use the general abstractions we've created in this chapter, and our new knowledge of `map`, `flatMap`, and `fold`.

We're going to represent calculations as `Sum[String, Double]`, where the `String` is an error message. Extend `Sum` to have `map` and `fold` method.

<div class="solution">
~~~ scala
sealed trait Sum[+A, +B] {
  def fold[C](error: A => C, success: B => C): C =
    this match {
      case Failure(v) => error(v)
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
final case class Failure[A](value: A) extends Sum[A, Nothing]
final case class Success[B](value: B) extends Sum[Nothing, B]
~~~
</div>

Now we're going to reimplement the calculator from last time. We have an abstract syntax tree defined via the following algebraic data type:

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
