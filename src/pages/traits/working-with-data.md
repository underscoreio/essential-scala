## Working With Data

In the previous section we saw how to define algebraic data types using a combination of the sum (or) and product type (and) patterns. In this section we'll see a pattern for using algebraic data types, known as **structural recursion**. We'll actually see two variants of this pattern: one using **polymorphism** and one using **pattern matching**.

Structural recursion is the precise opposite of the process of building an algebraic data type. If `A` has a `B` and `C` (the product-type pattern), to construct an `A` we must have a `B` and a `C`. The sum- and product-type patterns tell us how to combine data to make bigger data. Structural recursion says that if we have an `A` as defined before, we must break it into its constituent `B` and `C` that we then combine in some way to get closer to our desired answer. Structural recursion is essentially the process of breaking down data into smaller pieces.

Just as we have two patterns for building algebraic data types, we will have two patterns for decomposing them using structural recursion. We will actually have two variants of each pattern, one using polymorphism, which is the typical object-oriented style, and one using pattern matching, which is typical functional style. We'll end this section with some rules for choosing which pattern to use.

### Structural Recursion using Polymorphism

Polymorphic dispatch, or just polymorphism for short, is a fundamental object-oriented technique. If we define a method in a trait, and have different implementations in classes extending that trait, when we call that method the implementation on the actual concrete instance will be used. Here's a very simple example. We start with a simple definition using the familiar product type (or) pattern.

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

We declare a value with type `A` but we see the concrete implementation on `B` or `C` is used.

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
  def f: F = ???
}
~~~

In the body of the method we must use `b`, `c`, and any method parameters to construct the result of type `F`.

</div>


<div class="callout callout-info">
#### The Is-a Or Polymorphism Pattern

If `A` is a `B` or `C`, and we want to write a method `f` returning an `F`, define `f` as an abstract method on `A` and provide concrete implementations in `B` and `C`.

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


### Structural Recursion using Pattern Matching

Structural recursion with pattern matching proceeds along the same lines as polymorphism. We simply have a case for every subtype, and each pattern matching case must extract the fields we're interested in.

<div class="callout callout-info">
#### The Has-a And Pattern Matching Pattern

If `A` has a `b` (with type `B`) and a `c` (with type `C`), and we want to write a method `f` that accepts an `A` and returns an `F`, write

~~~ scala
def f(a: A): F =
  a match {
    case A(b, c) => ???
  }
~~~

In the body of the method we use `b` and `c` to construct the result of type `F`.

</div>


<div class="callout callout-info">
#### The Is-a Or Pattern Matching Pattern

If `A` is a `B` or `C`, and we want to write a method `f` accepting an `A` and returning an `F`, define a pattern matching case for `B` and `C`.

~~~ scala
def f(a: A): F =
  a match {
    case B() => ???
    case C() => ???
  }
~~~
</div>

### A Complete Example

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

Now using pattern matching. We actually have two choices when using pattern matching. We can implement our code in a single method on `Feline` or we can implement it in a method on another object. Let's see both.

~~~ scala
sealed trait Feline {
  def dinner: Food =
    this match {
      case Lion() => Antelope
      case Tiger() => TigerFood
      case Panther() => Licorice
      case Cat(favouriteFood) => CatFood(favouriteFood)
    }
}

object Diner {
  def dinner(feline: Feline): Food =
    feline match {
      case Lion() => Antelope
      case Tiger() => TigerFood
      case Panther() => Licorice
      case Cat(food) => CatFood(food)
    }
}
~~~

Note how we can directly apply the patterns, and the code almost falls out. This is the main point we want to make with structural recursion: the code follows the shape of the data, and can be produced in an almost mechanical way.

### Choosing Which Pattern to Use

We have three way of implementing structural recursion:

1. polymorphism;
2. pattern matching in the base trait; and
3. pattern matching in an external object (as in the `Diner` example above).

Which should we use? The first two methods give the same result: a method defined on the classes of interest. We should use whichever is more convenient. This normally ends up being pattern matching on the base trait as it requires less code duplication. When we implement a method in the classes of interest everything we can have only one implementation of the method, and everything that method requires to work must be contained within the class and parameters we pass to the method. When we implement methods using pattern matching in an external object we can provide multiple implementations (multiple `Diner`s in the example above).

The general rule is: if a method only depends on other fields and methods in a class it is a good candidate to be implemented inside the class. If the method depends on other data (for example, if we needed a `Cook` to make dinner) consider implementing is using pattern matching outside of the classes in question. If we want to have more than one implementation we should use pattern matching and implement it outside the classes.

### Object-Oriented vs Functional Extensibility

There is a fundamental difference between the kind of extensibility that object-oriented style (polymorphism, assuming traits are not sealed) and functional style (pattern matching, with sealed traits) gives us. With OO style we can easily add new data, by extending a trait, but adding a new method requires us to change existing code. With functional style we can easily add a new method but adding new data requires us to modify existing code. In tabular form:

|--------+-------------------------+-------------------------|
|        | Add new method          | Add new data            |
|--------+-------------------------+-------------------------|
| **OO** | Change existing code    | Existing code unchanged |
| **FP** | Existing code unchanged | Change existing code    |
|============================================================|
{: .table .table-bordered .table-responsive }

In Scala we have the flexibility to use both polymorphism and pattern matching, and we should use whichever is appropriate. However we generally prefer sealed traits as it gives us greater guarantees about our code's semantics, and we can use typeclasses, which we'll explore later, to get us OO-style extensibility.

### Exercises

#### Traffic Lights

In the previous section we implemented a `TrafficLight` data type like so:

~~~ scala
sealed trait TrafficLight
final case object Red extends TrafficLight
final case object Green extends TrafficLight
final case object Yellow extends TrafficLight
~~~

Using polymorphism and then using pattern matching implement a method called `next` which returns the next `TrafficLight` in the standard `Red` -> `Green` -> `Yellow` -> `Red` cycle. Do you think it is better to implement this method inside or outside the class? If inside, would you use pattern matching or polymorphism? Why?

<div class="solution">
First with polymorphism:

~~~ scala
sealed trait TrafficLight {
  def next: TrafficLight
}
final case object Red extends TrafficLight {
  def next: TrafficLight =
    Green
}
final case object Green extends TrafficLight {
  def next: TrafficLight =
    Yellow
}
final case object Yellow extends TrafficLight {
  def next: TrafficLight =
    Red
}
~~~

Now with pattern matching:

~~~ scala
sealed trait TrafficLight {
  def next: TrafficLight =
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

In this case I think implementing inside the class using pattern matching is best. `Next` doesn't depend on any external data and we probably only want one implementation of it. Pattern matching makes the structure of the state machine clearer than polymorphism.

Ultimately there are no hard-and-fast rules, and we must consider our design decisions in the context of the larger program we are writing.
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
assert(Calculator.+(Failure("Badness"), 1) == Failure("Badness"))
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

Now write a division method that fails if the divisor is 0. The following tests should pass. Note the behavior for the last test. This indicates "fail fast" behavior. If a calculation has already failed we keep that failure and don't process any more data even if, as is the case in the test, doing so would lead to another failure.

~~~ scala
assert(Calculator./(Success(4), 2) == Success(2))
assert(Calculator./(Success(4), 0) == Failure("Division by zero"))
assert(Calculator./(Failure("Badness"), 0) == Failure("Badness"))
~~~

<div class="solution">
The important points here are:

1. We have the same general pattern as before, matching on the `Calculation` *first* to implement our fail fast behavior.
2. After matching on our `Calculation` we then check for division by zero.

~~~ scala
def /(calc: Calculation, operand: Int): Calculation =
  calc match {
    case Success(result) =>
      operand match {
        case 0 => Failure("Division by zero")
        case _ => Success(result / operand)
      }
    case Failure(reason) => Failure(reason)
  }
~~~
</div>

#### Email

Recall the `Visitor` trait we looked at earlier: a website `Visitor` is either `Anonymous` or a signed-in `User`. Now imagine we wanted to add the ability to send emails to visitors. We can only email signed-in users, and sending an email requires a lot of knowledge about SMTP settings, MIME headers, and so on. Would an `email` method be better implemented using polymorphism on the `Visitor` trait or using pattern matching in an `EmailService` object? Why?

<div class="solution">
I would implement the method in an `EmailService` object. There are a lot of details to do with sending an email that have nothing to do with our `Visitor` class. I would rather keep these details in a separate abstraction.
</div>
