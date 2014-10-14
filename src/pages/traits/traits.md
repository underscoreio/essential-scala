---
layout: page
title: "This or That: Traits"
---

**Traits** are templates for creating classes, in the same way that classes are templates for creating objects. Traits allow us to express that two or more classes can be considered the same, and thus both implement the same operations. In other words, traits allow us to express that multiple classes share a common super-type (outside of the `Any` super-type that all classes share).

<div class="java-tip">
Traits are very much like Java 8's *interfaces* with *default methods*. If you have not used Java 8, you can think of traits as being like a cross between interfaces and *abstract classes*.
</div>


## An Example of Traits

Let's start with an example of a trait. Imagine we're modelling visitors to a website. There are two types of visitor: those who have registered on our site and those who are anonymous. We can model this with two classes:

~~~ scala
import java.util.Date

case class Anonymous(id: String, createdAt: Date = new Date())

case class User(
  id: String,
  email: String,
  createdAt: Date = new Date()
)
~~~

With these class definitions we're saying that both anonymous and registered visitors have an id and a creation date. But we only know the email address of registered visitors.

There is obvious duplication here, and it would be nice to not have to write the same definitions twice. More important though, is to create some common type for the two kinds of visitors. If they had some type in common (other than `AnyRef` and `Any`) we could write methods that worked on any kind of visitor. We can do this with a trait like so:

~~~ scala
import java.util.Date

trait Visitor {
  def id: String      // Unique id assigned to each user
  def createdAt: Date // Date this user first visited the site

  // How long has this visitor been around?
  def age: Long = new Date().getTime - createdAt.getTime
}

case class Anonymous(id: String, createdAt: Date = new Date()) extends Visitor

case class User(
  id: String,
  email: String,
  createdAt: Date = new Date()
) extends Visitor
~~~

Note the two changes:

- we defined the trait `Visitor`; and
- we declared that `Anonymous` and `User` are subtypes of the `Visitor` trait by using the `extends` keyword.

The `Visitor` trait expresses an interface that any subtype must implement: they must implement a `String` called `id` and a `createdAt` `Date`. Any sub-type of `Visitor` also automatically has a method `age` as defined in `Visitor`.

By defining the `Visitor` trait we can write methods that work with any subtype of visitor, like so:

~~~ scala
scala> def older(v1: Visitor, v2: Visitor): Boolean =
         v1.createdAt.before(v2.createdAt)

scala> older(Anonymous("1"), User("2", "test@example.com"))
older(Anonymous("1"), User("2", "test@example.com"))
res4: Boolean = true
~~~

Here the method `older` can be called with either an `Anonymous` or a `User` as they are both subtypes of `Visitor`.

<div class="callout callout-info">
#### Trait Syntax

To declare a trait we write

~~~ scala
trait TraitName {
  declarationOrExpression ...
}
~~~

To declare that a class is a subtype of a trait we write

~~~ scala
class Name(...) extends TraitName {
  ...
}
~~~

More commonly we'll use case classes, but the syntax is the same

~~~ scala
case class Name(...) extends TraitName {
 ...
}
~~~
</div>

## Traits Compared to Classes

Like a class, a trait is a named set of field and method definitions. However, it differs from a class in a few important ways:

 - **A trait cannot have a constructor** -- we can't create objects directly from a trait. Instead we can use a trait to create a class, and then create objects from that class. We can base as many classes as we like on a trait.

 - Traits can define **abstract methods** that have names and type signatures but no implementation. We saw this in the `Visitor` trait. We must specify the implementation when we create a class that extends the trait, but until that point we're free to leave definitions abstract.

Let's return to the `Visitor` trait to further explore abstract definitions. Recall the definition of `Visitor` is

~~~ scala
import java.util.Date

trait Visitor {
  def id: String      // Unique id assigned to each user
  def createdAt: Date // Date this user first visited the site

  // How long has this visitor been around?
  def age: Long = new Date().getTime - createdAt.getTime
}
~~~

`Visitor` prescribes two abstract methods. That is, methods which do not have an implementation but must be implemented by extending classes. These are `id` and `createdAt`. It also defines a concrete method, `age`, that is defined in terms of one of the abstract methods.

`Visitor` is used as a building block for two classes: `Anonymous` and `User`. Each class `extends Visitor`, meaning it inherits all of its fields and methods:

~~~ scala
scala> Anonymous("anon1")
res14: Anonymous = Anonymous(anon1)

scala> res14.createdAt
res15: java.util.Date = Mon Mar 24 15:11:45 GMT 2014

scala> res14.age
res16: Long = 8871
~~~

`id` and `createdAt` are abstract so they must be defined in extending classes. Our classes implement them as `vals` rather than `defs`. This is legal in Scala, which sees `def` as a more general version of `val`[^uap]. It is good practice to never define `val`s in a trait, but rather to use `def`. A concrete implementation can then implement it using using a `def` or `val` as appropriate.

[^uap]: This is all part of the [uniform access principle] we saw in the exercises for [Object Literals](object-literals.html).

[uniform access principle]: http://en.wikipedia.org/wiki/Uniform_access_principle

## Take Home Points

Traits are a way of **abstracting over classes** that have similar properties, just like classes are a way of abstracting over objects.

Using a traits has two parts. Declaring the trait

~~~ scala
trait TraitName {
  declarationOrExpression ...
}
~~~

and extending the trait from a class (usually a case class)

~~~ scala
case class Name(...) extends TraitName {
  ...
}
~~~

## Exercises

### Cats, and More Cats

Demand for Cat Simulator 1.0 is exploding! For v2 we're going to go beyond the domestic cat to model `Tiger`s, `Lion`s, and `Panther`s in additional to the `Cat`. Define a trait `Feline` and then define all the different species as subtypes of `Feline`. To make things interesting, define:

- on `Feline` a `colour` as before;
- on `Feline` a `String` `sound`, which for a cat is `"meow"` and is `"roar"` for all other felines;
- only `Cat` has a favourite food; and
- `Lion`s have an `Int` `maneSize`.

<div class="solution">
This is mostly a finger exercise to get you used to trait syntax but there are a few interesting things in the solution.

~~~ scala
trait Feline {
  def colour: String
  def sound: String
}
case class Lion(colour: String, maneSize: Int) extends Feline {
  val sound = "roar"
}
case class Tiger(colour: String) extends Feline {
  val sound = "roar"
}
case class Panther(colour: String) extends Feline {
  val sound = "roar"
}
case class Cat(colour: String, food: String) extends Feline {
  val sound = "meow"
}
~~~

Notice that `sound` is not defined as a constructor argument. Since it is a constant, it doesn't make sense to give users a chance to modify it. There is a lot of duplication in the definition of `sound`. We could define a default value in `Feline` like so

~~~ scala
trait Feline {
  def colour: String
  def sound: String = "roar"
}
~~~

This is generally a bad practice. If we define a default implementation it should be an implementation that is suitable for all subtypes.

Another alternative to define an intermediate type, perhaps called `BigCat` that defines sound as `"roar"`. This is a better solution.

~~~ scala
trait BigCat extends Feline {
  val sound = "roar"
}
case class Tiger(...) extends BigCat
case class Lion(...) extends BigCat
case class Panther(...) extends BigCat
~~~
</div>

### Shaping up with traits

Define a trait called `Shape` and give it three abstract methods:

 - `sides` returns the number of sides;
 - `perimeter` returns the total length of the sides;
 - `area` returns the area.

Implement `Shape` with three classes: `Circle`, `Rectangle`, and `Square`. In each case provide implementations of each of the three methods. Ensure that the main constructor parameters of each shape (e.g. the radius of the circle) are accessible as fields.

**Tip:** The value of &pi; is accessible as `math.Pi`.

<div class="solution">
~~~ scala
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

case class Rectangle(width: Double, height: Double) extends Shape {
  val sides = 4
  val perimeter = 2 * width + 2 * height
  val area = width * height
}

case class Square(size: Double) extends Shape {
  val sides = 4
  val perimeter = 4 * size
  val area = size * size
}
~~~
</div>

### Shaping up 2 (da streets)

The solution from the last exercise delivered three distinct types of shape. However, it doesn't model the relationships between the three correctly. A `Square` isn't just a `Shape` -- it's also a type of `Rectangle` where the width and height are the same.

Refactor the solution to the last exercise so that `Square` and `Rectangle` are subtypes of a common type `Rectangular`.

**Tip:** A trait can extend another trait.

<div class="solution">
The new code looks like this:

~~~ scala
// trait Shape ...

// case class Circle ...

trait Rectangular extends Shape {
  def width: Int
  def height: Int
  val sides = 4
  val perimeter = 2*width + 2*height
  val area = width*height
}

case class Square(val size: Int) extends Rectangular {
  val width = size
  val height = size
}

case class Rectangle(val width: Int, val height: Int) extends Rectangular
~~~
</div>
