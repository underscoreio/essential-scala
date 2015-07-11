---
layout: page
title: Higher-Order Functions
---

A **higher-order function** is a function that accepts another function. We have seen several uses already, particularly with collections. Here we're going to use higher-order functions in some novel situations, with the aim of solidifying our understanding. This section will be heavy on the examples.

We're going to spend a lot of time with Scala's `List` type, so a quick overview:

* A `List[A]` is either `Nil` (the empty list) or a pair `::(a: A, as: List[A])`
* You can write the pair constructor using infix syntax `a :: as`.
* You can pattern match on the two cases as they are both case classes.

A `List` is built recursively. For example, given the empty list we can prepend an element like so:

~~~ scala
scala> 1 :: Nil
res17: List[Int] = List(1)
~~~

We can build longer lists in the same way.

~~~ scala
scala> 1 :: (2 :: Nil)
res19: List[Int] = List(1, 2)
~~~

As `::` is right associative we can drop the brackets.

~~~ scala
scala> 1 :: 2 :: Nil
res20: List[Int] = List(1, 2)
~~~

## Spinning Wheels

Write a method that copies a `List`.

Hint: A `List` is `::` or `Nil`. Remember how we learned to deal with sum types?

<div class="solution">
~~~ scala
def copy(in: List[Int]): List[Int] =
  in match {
    case Nil => Nil
    case (x :: xs) => x :: copy(xs)
  }
~~~
</div>

This is a simple function that illustrates how the shape of the code follows the shape of the data. A list has two cases, and so does our method. Notice how the individual cases follow the shape of the data. `Nil` goes to `Nil`. The pair (`::`) goes to a pair with a recursive call to `copy`.

## A Map of the Territory

Write a method to add one to all the elements of `List[Int]`. Don't use any methods defined on `List` to do so.

<div class="solution">
~~~ scala
def addOne(in: List[Int]): List[Int] =
  in match {
    case Nil => Nil
    case (x :: xs) => (x + 1) :: addOne(xs)
  }
~~~
</div>

Alter your method to accept any `Int => Int` function.

<div class="solution">
~~~ scala
def map(f: Int => Int, in: List[Int]): List[Int] =
  in match {
    case Nil => Nil
    case (x :: xs) => f(x) :: map(f, xs)
  }
~~~
</div>

Make your function generic in the type of the `List`.

<div class="solution">
~~~ scala
def map[A](f: A => A, in: List[A]): List[A] =
  in match {
    case Nil => Nil
    case (x :: xs) => f(x) :: map(f, xs)
  }
~~~
</div>

There are a few concepts in this example. The first is the idea of the passing functions as reusable computations. We've already seen in the section on collections the benefits this brings.

More interesting is the similarity of this code to the code for `copy` above. Notice once again the pattern: two cases to match the two cases of `List`, and the computation follows the shape of the data.

## Folding Up the Map

Write a method to sum up the elements of `List[Int]`. Don't use any methods defined on `List` to do so.

<div class="solution">
~~~ scala
def sum(in: List[Int]): Int =
  in match {
    case Nil => 0
    case (x :: xs) => x + sum(xs)
  }
~~~
</div>

Now generalise your method to accept any function `(Int, Int) => Int` in place of addition. You'll need to make another change, but I'll leave you work out what that is.

<div class="solution">
~~~ scala
def accumulate(f: (Int, Int) => Int, zero: Int, in: List[Int]): Int =
  in match {
    case Nil => zero
    case (x :: xs) => f(x,  accumulate(xs))
  }
~~~
</div>

Now generalise your method to be generic over the type of `List`. What is this function conventionally called?

<div class="solution">
~~~ scala
def foldRight[A, B](f: (A, B) => B, zero: B, in: List[A]): B =
  in match {
    case Nil => zero
    case (x :: xs) => f(x,  foldRight(f, zero, xs))
  }
~~~
</div>

Notice it's the same pattern again, though slightly generalised from before!


## Calculus the Easy Way (Optional)

You perhaps encountered differentiation in school. When we differentiate a function we create another function that calculates it's rate of change, called its derivative. In many cases we can do this symbolically, but we're going to do it the CS way---numerically.

Implement a function `derivative` that takes a function `Double => Double` and returns the derivative (also `Double => Double`).

Hint 1: We can approximate the derivative by calculating the *centered difference* `(f(x + h) - f(x - h)) / 2h` for small `h`.

Hint 2: The `math` package contains several useful methods such as `abs`. Note the derivative of `math.exp(x)` is itself, and the derivative of `math.sin` is `math.cos`. You can use these properties to test your function.

<div class="solution">
~~~ scala
def derivative(f: Double => Double): Double => Double = {
  val h = 0.01
  (x: Double) =>
    (f(x + h) - f(x - h)) / (2 * h)
}
~~~
</div>

Choosing a fixed value of `h` is not always a good idea. Make a function `makeDerivative` that allows you to set the value of `h` and returns `derivative`.

<div class="solution">
~~~ scala
def makeDerivative(h: Double) = {
  val derivative = (f: Double => Double) =>
    (x: Double) =>
      (f(x + h) - f(x - h)) / (2 * h)
  derivative
}
~~~
</div>

Now we can adjust `h` till we have calculated `derivative` at a point to within a given tolerance. Write a function `solve` that solves the derivative of a function at a point to within a given tolerance.

<div class="solution">
~~~ scala
def solve(f: Double => Double, x: Double, tolerance: Double) = {
  def iterate(bracket: Double, lastGuess: Double): Double = {
    val guess = makeDerivative(bracket)(f)(x)
    if(math.abs(guess - lastGuess) < tolerance)
      lastGuess
    else
      iterate(bracket / 2, guess)
  }

  iterate(0.05, makeDerivative(0.1)(f)(x))
}
~~~
</div>
