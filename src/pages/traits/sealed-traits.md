## This or That and Nothing Else: Sealed Traits

In many cases we can enumerate all the possible classes that can extend a trait. For example, we previously modelled a website visitor as `Anonymous` or a logged in `User`. These two cases cover all the possibilities as one is the negation of the other. We can model this case with a *sealed trait*, which allows the compiler to provide extra checks for us.

We create a sealed trait by simply writing `sealed` in front of our trait declaration:

```tut:book:silent
import java.util.Date

sealed trait Visitor {
  def id: String
  def createdAt: Date
  def age: Long = new Date().getTime() - createdAt.getTime()
}
```

When we mark a trait as `sealed` we *must* define all of its subtypes in the same file. Once the trait is sealed, the compiler knows the complete set of subtypes and will warn us if a pattern matching expression is missing a case:

```tut:invisible
final case class User(id: String, createdAt: Date, override val age: Long) extends Visitor
final case class Anonymous(id: String, createdAt: Date) extends Visitor
```

```tut:book:fail
def missingCase(v: Visitor) =
  v match {
    case User(_, _, _) => "Got a user"
  }
```

We will *not* get a similar warning from an unsealed trait.

We can still extend the subtypes of a sealed trait outside of the file where they are defined. For example, we could extend `User` or `Anonymous` further elsewhere. If we want to prevent this possibility we should declare them as `sealed` (if we want to allow extensions within the file) or `final` if we want to disallow all extensions. For the visitors example it probably doesn't make sense to allow any extension to `User` or `Anonymous`, so the simplified code should look like this:

```scala
sealed trait Visitor { /* ... */ }
final case class User(/* ... */) extends Visitor
final case class Anonymous(/* ... */) extends Visitor
```

This is a very powerful pattern and one we will use frequently.

<div class="callout callout-info">
#### Sealed Trait Pattern {-}

If all the subtypes of a trait are known, seal the trait

```scala
sealed trait TraitName {
  ...
}
```

Consider making subtypes `final` if there is no case for extending them

```scala
final case class Name(...) extends TraitName {
  ...
}
```

Remember subtypes must be defined in the same file as a sealed trait.
</div>

### Take home points

Sealed traits and final (case) classes allow us to control extensibility of types. *The majority of cases* should use the sealed trait / final case class pattern.

```scala
sealed trait TraitName { ... }
final case class Name(...) extends TraitName
```


The main advantages of this pattern are:

- the compiler will warn if we miss a case in pattern matching; and
- we can control extension points of sealed traits and thus make stronger guarantees about the behaviour of subtypes.


### Exercises

#### Printing Shapes

Let's revisit the `Shapes` example from Section [@sec:traits:shaping-up-2].

First make `Shape` a sealed trait. Then write a singleton object called `Draw` with an `apply` method that takes a `Shape` as an argument and returns a description of it on the console. For example:

```tut:book:invisible
trait Shape {
  def sides: Int
  def perimeter: Double
  def area: Double
}

case class Circle(radius: Double) extends Shape {
  val sides = 1
  val perimeter = 2 * math.Pi * radius
  val area = math.Pi * radius * radius
}

sealed trait Rectangular extends Shape {
  def width: Double
  def height: Double
  val sides = 4
  override val perimeter = 2*width + 2*height
  override val area = width*height
}

case class Rectangle(
  width: Double,
  height: Double
) extends Rectangular

case class Square(size: Double) extends Rectangular {
  def width = size
  def height = size
}

object Draw {
  def apply(shape: Shape): String = shape match {
    case Rectangle(width, height) =>
      s"A rectangle of width ${width}cm and height ${height}cm"

    case Square(size) =>
      s"A square of size ${size}cm"

    case Circle(radius) =>
      s"A circle of radius ${radius}cm"
  }
}
```

```tut:book
Draw(Circle(10))
Draw(Rectangle(3, 4))
```

Finally, verify that the compiler complains when you comment out a `case` clause.

<div class="solution">
```tut:book:silent
object Draw {
  def apply(shape: Shape): String = shape match {
    case Rectangle(width, height) =>
      s"A rectangle of width ${width}cm and height ${height}cm"

    case Square(size) =>
      s"A square of size ${size}cm"

    case Circle(radius) =>
      s"A circle of radius ${radius}cm"
  }
}
```
</div>

#### The Color and the Shape

```tut:book:invisible
// Shape uses Color so we define Color first:
sealed trait Color {
  // We decided to store RGB values as doubles between 0.0 and 1.0.
  //
  // It is always good practice to define abstract members as `defs`
  // so we can implement them with `defs`, `vals` or `vars`.
  def red: Double
  def green: Double
  def blue: Double

  // We decided to define a "light" colour as one with
  // an average RGB of more than 0.5:
  def isLight = (red + green + blue) / 3.0 > 0.5
  def isDark = !isLight
}

case object Red extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 0.0
  val blue = 0.0
 }

case object Yellow extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 1.0
  val blue = 0.0
}

case object Pink extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 0.0
  val blue = 1.0
}

// The arguments to the case class here generate `val` declarations
// that implement the RGB methods from `Color`:
final case class CustomColor(
  red: Double,
  green: Double,
  blue: Double) extends Color

// The code from the previous exercise comes across almost verbatim,
// except that we add a `color` field to `Shape` and its subtypes:
sealed trait Shape {
  def sides: Int
  def perimeter: Double
  def area: Double
  def color: Color
}

final case class Circle(radius: Double, color: Color) extends Shape {
  val sides = 1
  val perimeter = 2 * math.Pi * radius
  val area = math.Pi * radius * radius
}

sealed trait Rectangular extends Shape {
  def width: Double
  def height: Double
  val sides = 4
  val perimeter = 2 * width + 2 * height
  val area = width * height
}

final case class Square(size: Double, color: Color) extends Rectangular {
  val width = size
  val height = size
}

final case class Rectangle(
  width: Double,
  height: Double,
  color: Color
) extends Rectangular

object Draw {
  def apply(shape: Shape): String = shape match {
    case Circle(radius, color) =>
      s"A ${Draw(color)} circle of radius ${radius}cm"

    case Square(size, color) =>
      s"A ${Draw(color)} square of size ${size}cm"

    case Rectangle(width, height, color) =>
      s"A ${Draw(color)} rectangle of width ${width}cm and height ${height}cm"
  }

  def apply(color: Color): String = color match {
    case Red    => "red"
    case Yellow => "yellow"
    case Pink   => "pink"
    case color  => if(color.isLight) "light" else "dark"
  }
}
```

Write a sealed trait `Color` to make our shapes more interesting.

 - give `Color` three properties for its RGB values;
 - create three predefined colours: `Red`, `Yellow`, and `Pink`;
 - provide a means for people to produce their own custom `Colors`
   with their own RGB values;
 - provide a means for people to tell whether any `Color` is
   "light" or "dark".

A lot of this exercise is left deliberately open to interpretation. The important thing is to practice working with traits, classes, and objects.

Decisions such as how to model colours and what is considered a light or dark colour can either be left up to you or discussed with other class members.

Edit the code for `Shape` and its subtypes to add a colour to each shape.

Finally, update the code for `Draw.apply` to print the colour of the argument as well as its shape and dimensions:

 - if the argument is a predefined colour, print that colour by name:

```tut:book
Draw(Circle(10, Yellow))
```

 - if the argument is a custom colour rather than a predefined one,
   print the word "light" or "dark" instead.

You may want to deal with the colour in a helper method.

<div class="solution">
One solution to this exercise is presented below. Remember that a lot of the implementation details are unimportant---the crucial aspects of a correct solution are:

 - There must be a `sealed trait Color`:
    - The trait should contain three `def` methods for the RGB values.
    - The trait should contains the `isLight` method,
      defined in terms of the RGB values.

 - There must be three objects representing the predefined colours:
    - Each object must `extend Color`.
    - Each object should override the RGB values as `vals`.
    - Marking the objects as `final` is optional.
    - Making the objects `case objects` is also optional.

 - There must be a class representing custom colours:
    - The class must `extend Color`.
    - Marking the class `final` is optional.
    - Making the class a `case class` is optional (although highly recommended).

 - There should ideally be two methods in `Draw`:
    - One method should accept a `Color` as a parameter and one a `Shape`.
    - The method names are unimportant.
    - Each method should perform a `match` on the supplied value and provide
      enough `cases` to cover all possible subtypes.

 - The whole codebase should compile and produce sensible values when tested!

```tut:book:silent
// Shape uses Color so we define Color first:
sealed trait Color {
  // We decided to store RGB values as doubles between 0.0 and 1.0.
  //
  // It is always good practice to define abstract members as `defs`
  // so we can implement them with `defs`, `vals` or `vars`.
  def red: Double
  def green: Double
  def blue: Double

  // We decided to define a "light" colour as one with
  // an average RGB of more than 0.5:
  def isLight = (red + green + blue) / 3.0 > 0.5
  def isDark = !isLight
}

case object Red extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 0.0
  val blue = 0.0
 }

case object Yellow extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 1.0
  val blue = 0.0
}

case object Pink extends Color {
  // Here we have implemented the RGB values as `vals`
  // because the values cannot change:
  val red = 1.0
  val green = 0.0
  val blue = 1.0
}

// The arguments to the case class here generate `val` declarations
// that implement the RGB methods from `Color`:
final case class CustomColor(
  red: Double,
  green: Double,
  blue: Double) extends Color

// The code from the previous exercise comes across almost verbatim,
// except that we add a `color` field to `Shape` and its subtypes:
sealed trait Shape {
  def sides: Int
  def perimeter: Double
  def area: Double
  def color: Color
}

final case class Circle(radius: Double, color: Color) extends Shape {
  val sides = 1
  val perimeter = 2 * math.Pi * radius
  val area = math.Pi * radius * radius
}

sealed trait Rectangular extends Shape {
  def width: Double
  def height: Double
  val sides = 4
  val perimeter = 2 * width + 2 * height
  val area = width * height
}

final case class Square(size: Double, color: Color) extends Rectangular {
  val width = size
  val height = size
}

final case class Rectangle(
  width: Double,
  height: Double,
  color: Color
) extends Rectangular

// We decided to overload the `Draw.apply` method for `Shape` and
// `Color` on the basis that we may want to reuse the `Color` code
// directly elsewhere:
object Draw {
  def apply(shape: Shape): String = shape match {
    case Circle(radius, color) =>
      s"A ${Draw(color)} circle of radius ${radius}cm"

    case Square(size, color) =>
      s"A ${Draw(color)} square of size ${size}cm"

    case Rectangle(width, height, color) =>
      s"A ${Draw(color)} rectangle of width ${width}cm and height ${height}cm"
  }

  def apply(color: Color): String = color match {
    // We deal with each of the predefined Colors with special cases:
    case Red    => "red"
    case Yellow => "yellow"
    case Pink   => "pink"
    case color  => if(color.isLight) "light" else "dark"
  }
}
```

```tut:book
// Test code:

Draw(Circle(10, Pink))

Draw(Rectangle(3, 4, CustomColor(0.4, 0.4, 0.6)))
```

</div>

#### A Short Division Exercise

Good Scala developers don't just use types to model data. Types are a great way to put artificial limitations in place to ensure we don't make mistakes in our programs. In this exercise we will see a simple (if contrived) example of this---using types to prevent division by zero errors.

Dividing by zero is a tricky problem---it can lead to exceptions. The JVM has us covered as far as floating point division is concerned but integer division is still a problem:

```tut:book
1.0 / 0.0
```

```tut:book:fail
1 / 0
```

Let's solve this problem once and for all using types!

Create an object called `divide` with an `apply` method that accepts two `Ints` and returns `DivisionResult`. `DivisionResult` should be a sealed trait with two subtypes: a `Finite` type encapsulating the result of a valid division, and an `Infinite` type representing the result of dividing by `0`.

Here's some example usage:

```scala
val x = divide(1, 2)
// x: DivisionResult = Finite(0)

val y = divide(1, 0)
// y: DivisionResult = Infinite
```

Finally, write a sample function that calls `divide`, matches on the result, and returns a sensible description.

<div class="solution">
Here's the code:

```tut:book:silent
sealed trait DivisionResult
final case class Finite(value: Int) extends DivisionResult
case object Infinite extends DivisionResult

object divide {
  def apply(num: Int, den: Int): DivisionResult =
    if(den == 0) Infinite else Finite(num / den)
}
```

```tut:book
divide(1, 0) match {
  case Finite(value) => s"It's finite: ${value}"
  case Infinite      => s"It's infinite"
}
```

The result of `divide.apply` is a `DivisionResult`, which is a `sealed trait` with two subtypes. The subtype `Finite` is a `case class` encapsulting the result, but the subtype `Infinite` can simply be an object. We've used a `case object` for parity with `Finite`.

The implementation of `divide.apply` is simple - we perform a test and return a result. Note that we haven't annotated the method with a result type---Scala is capable of inferring the type `DivisionResult` as the least upper bound of `Infinite` and `Finite`.

Finally, the match illustrates a case class pattern with the parentheses, and a case object pattern without.
</div>
