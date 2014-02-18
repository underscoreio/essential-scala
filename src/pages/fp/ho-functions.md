---
layout: page
title: Higher-Order Functions
---

A **higher-order function** is one we pass to another function. We have seen several uses already, particularly with collections. Here we're going to use higher-order functions in some novel situations, with the aim of solidifying your understanding. This section will be heavy on the examples.

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

#### A Map of the Territory

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

There are a few concepts in this example. The first is the idea of the passing functions as reusable computations. As we've seen in the section on collections, we can

#### Folding Up the Map

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

<div class="solution>
~~~ scala
def accumulate(f: (Int, Int) => Int, accum: Int, in: List[Int]): Int =
  in match {
    case Nil => accum
    case (x :: xs) => f(x,  accumulate(xs))
  }
~~~
</div>

Now generalise your method to be generic over the type of `List`. What is this function conventionally called?

<div class="solution">
def fold[A, B](f:  => Int, accum: Int, in: List[Int]): Int =
  in match {
    case Nil => accum
    case (x :: xs) => f(x,  accumulate(xs))
  }
</div>

#### Calculus, the Easy Way

You perhaps encountered differentiation in school. When we differentiate a function we calculate it's rate of change, called its derivative. In many cases we can do this symbolically, but we're going to do it the CS way -- numerically.

Implement a function `derivative` that takes three arguments:

* A function `Double => Double`
* A point on the number line, a `Double`
* A tolerance, also a `Double`.

You should return the rate of change of the function at the given point up to the given tolerance.

Hint 1: We can approximate the derivative by calculating the *centered difference* `(f(x + h) - f(x - h)) / 2h` for small `h`. When this formula doesn't change to within our tolerance for decreasing (but positive) `h` we can stop.

Hint 2: You might want to define several helper methods inside `derivative`.

Hint 3: The `Math` object contains several useful methods such as `abs`. Note the derivative of `Math.exp(x)` is itself, and the derivative of `Math.cos` is `Math.sin`. You can use these properties to test your function.

<div class="solution">
~~~ scala
def derivative(f: Double => Double, x: Double, e: Double = 0.01) = {
  def centeredDifference(bracket: Double) =
    (f(x + bracket) - f(x - bracket)) / (2 * bracket)

  def iterate(bracket: Double, lastGuess: Double): Double = {
    val guess = centeredDifference(bracket)
    if(Math.abs(guess - lastGuess) < e)
      lastGuess
    else
      iterate(bracket / 2, guess)
  }

  iterate(0.05, centeredDifference(0.1))
}
~~~
</div>
