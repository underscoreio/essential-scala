---
layout: page
title: Working With Data
---

In the previous section we saw how to define algebraic data types using a combination of the sum- (or) and product-type (and) patterns. In this section we'll a pattern for using algebraic data types, known as **structural recursion**. We'll actually see two variants of this pattern: one using **polymorphism** and one using **pattern matching**.

Structural recursion is the precise opposite of the process of building an algebraic data type. If `A` has a `B` and `C` (the product-type pattern), to construct an `A` we must have a `B` and a `C`. The sum- and product-type patterns tell us how to combine data to make bigger data. Structural recursion says that if we have an `A` as defined before, we must break it into its constituent `B` and `C` that we then combine in some way to get closer to our desired answer. Structural recursion is essentially the process of breaking down data into smaller pieces.

Just as we have two paterns for building algebraic data types, we will have two patterns for decomposing them using structural recursion. We will actually have two variants of each pattern, one using polymorphism, which is the typical object-oriented style, and one using pattern matching, which is typical functional style. We'll end this section with some rules for choosing which pattern to use.

## Structural Recursion using Polymorphism

Polymorphic dispatch, or just polymorphism for short, is a fundamental object-oriented technique. If we define a method in a trait with different implementations in classes extending that trait, when we call that method the implementation on the actual concrete instance will be used. Here's a very simple example. We start with a simple definition using the familiar product-type (or) pattern.

~~~ scala
sealed trait A {
  def foo: String
}
final case class B() extends A {
  def foo: String =
    "It's B!"
}
final case class C() extends A {
  def foo: String =
    "It's C!"
}
~~~

In use we declare a value with type `A` but we see in use the concrete implementation on `B` or `C` is used.

~~~ scala
scala> val anA: A = B()
anA: A = B()

scala> anA.foo
res1: String = It's B!

scala> val anA: A = C()
anA: A = C()

scala> anA.foo
res2: String = It's C!
~~~

We can define an implementation in a trait, and change the implementation in an extending class using the `override` keyword.

~~~ scala
sealed trait A {
  def foo: String =
    "It's A!"
}
final case class B() extends A {
  override def foo: String =
    "It's B!"
}
final case class C() extends A {
  override def foo: String =
    "It's C!"
}
~~~

The behaviour is as before; the implementation on the concrete class is selected.

~~~ scala
scala> val anA: A = B()
anA: A = B()

scala> anA.foo
res3: String = It's B!
~~~

Remember that if you provide a default implementation in a trait, you should ensure that implementation is valid for all subtypes.

Now we understand how polymorphism works, how do we use it with an algebraic data types? We've actually seen everything we need, but let's make it explicit and see the patterns.

<div class="callout callout-info">
#### The Has-a And Polymorphism Pattern

If `A` has a `b` (with type `B`) and a `c` (with type `C`), and we want to write a method `f` returning an `F`, simply write the method in the usual way.

~~~ scala
case class A(b: B, c: C) {
  def f: Fjj = ???
}
~~~

In the body of the method we must use `b`, `c`, and any method parameters to construct the result of type `F`.

</div>


<div class="callout callout-info">
#### The Is-a Or Polymorphism Pattern

If `A` is a `B` or `C`, and we want to write a method `f` returning an `F`, define `f` as an abstract method on `A` and provide concrete implementations on `B` and `C`.

~~~ scala
sealed trait A {
  def f: F
}
final case class B() extends A {
  def f: F =
    ???
}
final case class C() extends A {
  def f: F =
    ???
}
~~~
</div>


## Structural Recursion using Pattern Matching

Structural recursion with pattern matching proceeds along the same lines as with polymorphism. We simply have a case for every subtype, and each pattern matching case must extract the fields we're interested in.

<div class="callout callout-info">
#### The Has-a And Pattern Matching Pattern

If `A` has a `b` (with type `B`) and a `c` (with type `C`), and we want to write a method `f` that accepts an `A` and returns an `F`, write

~~~ scala
def f(a: A): F =
  match a {
    case A(b, c) => ???
  }
~~~

In the body of the method we must use `b`, `c`, to construct the result of type `F`.

</div>


<div class="callout callout-info">
#### The Is-a Or Pattern Matching Pattern

If `A` is a `B` or `C`, and we want to write a method `f` accepting an `A` and returning an `F`, define a pattern matching case for `B` and `C`.

~~~ scala
def f(a: A): F =
  match a {
    case B() => ???
    case C() => ???
  }
~~~
</div>

## A Complete Example

Let's look at a complete example of the algebraic data type and structural recursion patterns, using our familiar `Feline` data type.

We start with a description of the data. A `Feline` is a `Lion`, `Tiger`, `Panther`, or `Cat`. We're going to simplify the data description, and just say that a `Cat` has a `String` `favouriteFood`. From this description we can immediately apply our pattern to define the data.

~~~ scala
sealed trait Feline
final case class Lion() extends Feline
final case class Tiger() extends Feline
final case class Panther() extends Feline
final case class Cat(favouriteFood: String) extends Feline
~~~

Now let's implement a method using both polymorphism and pattern matching. Our method, `dinner`, will return the appropriate food for the feline in question. For a `Cat` their dinner is their `favouriteFood`. For `Lion`s it is antelope, for `Tiger`s it is tiger food, and for `Panther`s it is licorice.

We could represent food as a `String`, but we can do better and represent it with a type. This avoids, for example, spelling mistakes in our code. So let's define our `Food` type using the now familiar patterns.

~~~ scala
sealed trait Food
final case object Antelope extends Food
final case object TigerFood extends Food
final case object Licorice extends Food
final case class CatFood(food: String) extends Food
~~~

Now we can implement `dinner` as a method returning `Food`. First using polymorphism:

~~~ scala
sealed trait Feline {
  def dinner: Food
}
final case class Lion() extends Feline {
  def dinner: Food =
    Antelope
}
final case class Tiger() extends Feline {
  def dinner: Food =
    TigerFood
}
final case class Panther() extends Feline {
  def dinner: Food =
    Licorice
}
final case class Cat(favouriteFood: String) extends Feline {
  def dinner: Food =
    CatFood(favouriteFood)
}
~~~

Now using pattern matching:

~~~ scala
object Diner {
  def dinner(feline: Feline): Food =
    feline match {
      case Lion() => Antelope
      case Tiger() => TigerFood
      case Panter() => Licorice
      case Cat(food) => CatFood(food)
    }
}
~~~

Note how we can directly apply the patterns, and the code almost falls out. This is the main point we want to make with structural recursion: the code follows the shape of the data, and can be produced in an almost mechanical way.

## Choosing Which Pattern to Use

We have two patterns for implementing structural recursion. Which should we use?
The meaning of the two methods (polymorphism and pattern matching) is slightly different. When we implement a method using polymorphism, we implement it on the classes of interest (though our methods can also take other parameters). This means we can have one implementation of the method, and everything that method requires to work must be contained within the class and parameters we pass to the method. When we implement methods using pattern matching we can provide multiple implementations (multiple `Diner`s in the example above).

The general rule is: if a method only depends on other fields and methods in a class it is a good candidate to use polymorphism. If the method depends on other data (for example, if we needed a `Cook` to make dinner) consider implementing is using pattern matching outside of the classes in question. If we want to have more than one implementation we should use pattern matching and implement it outside the classes.

## Object-Oriented vs Functional Extensibility

## Exercises

#### Traffic Lights

In the previous section we implemented a `TrafficLight` data type like so:

~~~ scala
sealed trait TrafficLight
final case object Red extends TrafficLight
final case object Green extends TrafficLight
final case object Yellow extends TrafficLight
~~~

Using polymorphim and then using pattern matching implement a method called `tick` which returns a `TrafficLight`, then next one in the standard `Red` -> `Green` -> `Yellow` -> `Red` cycle. Which do you think is better for this application?

<div class="solution">
First with polymorphism:

~~~ scala
sealed trait TrafficLight {
  def tick: TrafficLight
}
final case object Red extends TrafficLight {
  def tick: TrafficLight =
    Green
}
final case object Green extends TrafficLight {
  def tick: TrafficLight =
    Yellow
}
final case object Yellow extends TrafficLight {
  def tick: TrafficLight =
    Red
}
~~~

Now with pattern matching:

~~~ scala
sealed trait TrafficLight {
  def tick: TrafficLight =
    this match {
      case Red => Green
      case Green => Yellow
      case Yellow => Red
    }
}
final case object Red extends TrafficLight
final case object Green extends TrafficLight
final case object Yellow extends TrafficLight
~~~

In this case I think polymorphism is best, as `tick` doesn't depend on any external data and we probably only one one implementation of it. Notice how I defined my pattern matching implementation on `TrafficLight`. This achieves the same thing as polymorphism (subtypes have a method `tick`) while using pattern matching for the actual implementation. It's a neat implementation method that is something advantegeous to use.
</div>

#### Calculation

In the last section we created a `Calculation` data type like so:

~~~ scala
sealed trait Calculation
final case class Success(result: Int) extends Calculation
final case class Failure(reason: String) extends Calculation
~~~

We're now going to write some methods that use a `Calculation` to perform a larger calculation. These methods will have a somewhat unusual shape -- this is a precursor to things we'll be exploring soon -- but if you follow the patterns you will be fine.

Create a `Calculator` object. On `Calculator` define methods `+` and `-` that accept a `Calculation` and an `Int`, and return a new `Calculation`. Here are some examples

~~~ scala
assert(Calculator.+(Success(1), 1) == Success(2))
assert(Calculator.-(Success(1), 1) == Success(0))
assert(Calculator.+(Faillure("Badness"), 1) == Failure("Badness"))
~~~

<div class="solution">
Start by implementing the framework the exercise calls for:

~~~ scala
object Calculator {
  def +(calc: Calculation, operand: Int): Calculation = ???
  def -(calc: Calculation, operand: Int): Calculation = ???
}
~~~

Now apply the structural recursion pattern:

~~~ scala
object Calculator {
  def +(calc: Calculation, operand: Int): Calculation =
    calc match {
        case Success(result) => ???
        case Failure(reason) => ???
    }
  def -(calc: Calculation, operand: Int): Calculation =
    calc match {
      case Success(result) => ???
      case Failure(reason) => ???
    }
}
~~~

To write the remaining bodies of the methods we can no longer rely on the patterns. However, a bit of thought quickly leads us to the correct answer. We know that `+` and `-` are binary operations; we need two integers to use them. We also know we need to return a `Calculation`. Looking at the `Failure` cases, we don't have two `Int`s available. The only result that makes sense to return is `Failure`. On the `Success` side, we *do* have two `Int`s and thus we should return `Success`. This gives us:

~~~ scala
object Calculator {
  def +(calc: Calculation, operand: Int): Calculation =
    calc match {
        case Success(result) => Success(result + operand)
        case Failure(reason) => Failure(reason)
    }
  def -(calc: Calculation, operand: Int): Calculation =
    calc match {
      case Success(result) => Success(result - operand)
      case Failure(reason) => Failure(reason)
    }
}
~~~
</div>

Now write a division method that fails if the divisor is 0. The following tests should pass.

~~~ scala
assert(Calculation./(Success(4), Success(2)) == 2)
assert(Calculation./(Success(4), 0) == Failure("Division by zero"))
~~~

<div class="solution">
~~~ scala
def /(calc: Calculation, operand: Int): Calculation =
  operand match {
    0 => Failure("Division by zero")
    _ => calc match {
           case Success(result) => result / operand
           case Failure(reason) => Failure(reason)
         }
  }
~~~
</div>

#### A Calculator

We're now going to expand on the concepts in the last exercise to implement a simple interpreter for programs containing only numeric operations.

We start by defining some types to represent the expressions we'll be operating on. In the compiler literature this is known as an *abstract syntax tree*.

Our representation is:

- An `Expression` is an `Addition`, `Subtraction`, or a `Number`
- An Addition has a `left` and `right` Expression
- A Subtraction has a `left` and `right` Expression
- A Number has a `value` of type Int

Implement this in Scala.

<div class="solution">
The algebraic data type pattern is appropriate to implement the calculatur abstract syntax tree:

~~~ scala
sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Number(value: Int) extends Expression
~~~

Implementing *eval* is straightforward structural recursion

~~~ scala
sealed trait Expression {
  def eval: Number
}
final case class Addition(left: Expression, right: Expression) extends Expression {
  def eval: Number =
    Number(left.eval.value + right.eval.value)
}
final case class Subtraction(left: Expression, right: Expression) extends Expression {
  def eval: Number =
    Number(left.eval.value - right.eval.value)
}
final case class Number(value: Int) extends Expression {
  def eval: Number =
    this
}
~~~
</div>
