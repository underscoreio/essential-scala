## Classes

A class is a template for creating objects that have similar methods and fields. In Scala a class also defines a type, and objects created from a class all share the same type. This allows us to overcome the problem we had in the *Greetings, Human* exercise in the last chapter.

### Defining a Class

Here is a declaration for a simple `Person` class:

~~~ scala
class Person {
  val firstName = "Noel"
  val lastName = "Welsh"
  def name = firstName + " " + lastName
}
~~~

Like an object declaration, a class declaration binds a name (in this case `Person`) and is not an expression. However, unlike an object name, we cannot use a class name in an expression. A class is not a value, and there is a different *namespace* in which classes live.

~~~ scala
Person
// error: not found: value Person
//  Person
//  ^
~~~

We can create a new `Person` object using the `new` operator. Objects are values and we access their methods and fields in the usual way:

~~~ scala
val noel = new Person
// noel: Person = Person@3235186a

noel.firstName
// res: String = Noel
~~~

Notice the type of the object is `Person`. Each call to `new` creates a distinct object of the same type:

~~~ scala
noel // noel is the object that prints '@3235186a'
// res: Person = Person@3235186a

val newNoel = new Person // each new object prints a new number
// newNoel: Person = Person@2792b987

val anotherNewNoel = new Person
// anotherNewNoel: Person = Person@63ee4826
~~~

This means we can write a method that takes any `Person` as a parameter:

~~~ scala
object alien {
  def greet(p: Person) =
    "Greetings, " + p.firstName + " " + p.lastName
}

alien.greet(noel)
// res: String = Greetings, Noel Welsh

alien.greet(newNoel)
// res: String = Greetings, Noel Welsh
~~~

<div class="callout callout-info">
#### Java Tip {-}

Scala classes are all subclasses of `java.lang.Object` and are, for the most part, usable from Java as well as Scala. The default printing behaviour of `Person` comes from the `toString` method defined in `java.lang.Object`.
</div>

### Constructors

As it stands our `Person` class is rather useless: we can create as many new objects as we want but they all have the same `firstName` and `lastName`. What if we want to give each person a different name?

The solution is to introduce a *constructor*, which allows us to pass parameters to new objects as we create them:

~~~ scala
class Person(first: String, last: String) {
  val firstName = first
  val lastName = last
  def name = firstName + " " + lastName
}

val dave = new Person("Dave", "Gurnell")
// dave: Person = Person@3ed12df7

dave.name
// res: String = Dave Gurnell
~~~

The constructor parameters `first` and `last` can only be used within the body of the class. We must declare a field or method using `val` or `def` to access data from outside the object.

Constructor arguments and fields are often redundant. Fortunately, Scala provides us a useful short-hand way of declaring both in one go. We can prefix constructor parameters with the `val` keyword to have Scala define fields for them automatically:

~~~ scala
class Person(val firstName: String, val lastName: String) {
  def name = firstName + " " + lastName
}

new Person("Dave", "Gurnell").firstName
// res: String = Dave
~~~

`val` fields are *immutable*---they are initialized once after which we cannot change their values. Scala also provides the `var` keyword for defining *mutable* fields.

Scala programmers tend to prefer to write immutability and side-effect-free code so we can reason about it using the substitution model. In this course we will concentrate almost exclusively on immutable `val` fields.

<div class="callout callout-info">

#### Class Declaration Syntax {-}

The syntax for declaring a class is

~~~ scala
class Name(parameter: type, ...) {
  declarationOrExpression ...
}
~~~

or

~~~ scala
class Name(val parameter: type, ...) {
  declarationOrExpression ...
}
~~~

where

- `Name` is the name of the class;
- the optional `parameter`s are the names given to constructor parameters;
- the `type`s are the types of the constructor parameters;
- the optional `declarationOrExpression`s are declarations or expressions.
</div>

### Default and Keyword Parameters

All Scala methods and constructors support *keyword parameters* and *default parameter values*.

When we call a method or constructor, we can *use parameter names as keywords* to specify the parameters in an arbitrary order:

~~~ scala
new Person(lastName = "Last", firstName = "First")
// res: Person = Person(First,Last)
~~~

This comes in doubly useful when used in combination with *default parameter values*, defined like this:

~~~ scala
def greet(firstName: String = "Some", lastName: String = "Body") =
  "Greetings, " + firstName + " " + lastName + "!"
~~~

If a parameter has a default value we can omit it in the method call:

~~~ scala
greet("Awesome")
// res: String = Greetings, Awesome Body
~~~

Combining keywords with default parameter values let us skip earlier parameters and just provide values for later ones:

~~~ scala
greet(lastName = "Dave")
// res: String = Greetings, Some Dave!
~~~

<div class="callout callout-info">
#### Keyword Parameters {-}

*Keyword parameters are robust to changes in the number and order of parameters.* For example, if we add a `title` parameter to the `greet` method, the meaning of keywordless method calls changes but keyworded calls remain the same:

~~~ scala
def greet(title: String = "Citizen", firstName: String = "Some", lastName: String = "Body") =
  "Greetings, " + title + " " + firstName + " " + lastName + "!"

greet("Awesome") // this is now incorrect
// res: String = Greetings, Awesome Some Body

greet(firstName = "Awesome") // this is still correct
// res: String = Greetings, Citizen Awesome Body
~~~

This is particularly useful when creating methods and constructors with a large number of parameters.
</div>

### Scala's Type Hierarchy

Unlike Java, which separates primitive and object types, everything in Scala is an object. As a result, "primitive" value types like `Int` and `Boolean` form part of the same type hierarchy as classes and traits.


\makebox[\linewidth]{\includegraphics[width=0.8\textwidth]{src/pages/classes/scala-type-hierarchy.pdf}}

<div class="figure">
<div class="text-center">
<img src="src/pages/classes/scala-type-hierarchy.svg" alt="Scala type hierarchy" />
</div>
</div>

Scala has a grand supertype called `Any`, under which there are two types, `AnyVal` and `AnyRef`. `AnyVal` is the supertype of all value types, which `AnyRef` is the supertype of all "reference types" or classes. All Scala and Java classes are subtypes of `AnyRef`[^value-classes].

[^value-classes]: We can actually define subtypes of `AnyVal`, which are known as [value classes](http://docs.scala-lang.org/overviews/core/value-classes.html). These are useful in a few specialised circumstances and we're not going to discuss them here.

Some of these types are simply Scala aliases for types that exist in Java: `Int` is `int`, `Boolean` is `boolean`, and `AnyRef` is `java.lang.Object`.

There are two special types at the *bottom* of the hierarchy. `Nothing` is the type of `throw` expressions, and `Null` is the type of the value `null`. These special types are subtypes of everything else, which helps us assign types to `throw` and `null` while keeping other types in our code sane. The following code illustrates this:

~~~ scala
def badness = throw new Exception("Error")

null
// res: Null = null

if(true) 123 else badness
// res: Int = 123

if(false) "it worked" else null
// res: String = null
~~~

Although the types of `badness` and `// res` are `Nothing` and `Null` respectively, the types of `res2` and `res3` are still sensible. This is because `Int` is the least common supertype of `Int` and `Nothing`, and `String` is the least common supertype of `String` and `Null`.

### Take Home Points

In this section we learned how to define *classes*, which allow us to create many objects with the same *type*. Thus, classes let us *abstract across objects* that have similar properties.

The properties of the objects of a class take the form of *fields* and *methods*. Fields are pre-computed values stored within the object and methods are computations we can call.

The syntax for declaring classes is

~~~ scala
class Name(parameter: type, ...) {
  declarationOrExpression ...
}
~~~

We create objects from a class by calling the constructor using the keyword `new`.

We also learned about *keyword parameters* and *default parameters*.

Finally we learned about Scala's type hierarchy, including the overlap with Java's type hierarchy, the special types `Any`, `AnyRef`, `AnyVal`, `Nothing`, `Null`, and `Unit`, and the fact that Java and Scala classes both occupy the same subtree of the type hierarchy.

### Exercises

We now have enough machinery to have some fun playing with classes.

### Cats, Again

Recall the cats from a previous exercise:

+-----------+-----------------+-------+
| Name      | Colour          | Food  |
+===========+=================+=======+
| Oswald    | Black           | Milk  |
+-----------+-----------------+-------+
| Henderson | Ginger          | Chips |
+-----------+-----------------+-------+
| Quentin   | Tabby and white | Curry |
+-----------+-----------------+-------+


Define a class `Cat` and then create an object for each cat in the table above.

<div class="solution">
This is a finger exercise to get you used to the syntax of defining classes.

~~~ scala
class Cat(val colour: String, val food: String)

val oswald = new Cat("Black", "Milk")
val henderson = new Cat("Ginger", "Chips")
val quentin = new Cat("Tabby and white", "Curry")
~~~

</div>


#### Cats on the Prowl

Define an object `ChipShop` with a method `willServe`. This method should accept a `Cat` and return `true` if the cat's favourite food is chips, and false otherwise.

<div class="solution">
~~~ scala
object ChipShop {
  def willServe(cat: Cat): Boolean =
    if(cat.food == "Chips")
      true
    else
      false
}
~~~
</div>


#### Directorial Debut

Write two classes, `Director` and `Film`, with fields and methods as follows:

 - `Director` should contain:
    - a field `firstName` of type `String`
    - a field `lastName` of type `String`
    - a field `yearOfBirth` of type `Int`
    - a method called `name` that accepts no parameters
      and returns the full name

 - `Film` should contain:
    - a field `name` of type `String`
    - a field `yearOfRelease` of type `Int`
    - a field `imdbRating` of type `Double`
    - a field `director` of type `Director`
    - a method `directorsAge` that returns
      the age of the director at the time of release
    - a method `isDirectedBy` that accepts a `Director`
      as a parameter and returns a `Boolean`

Copy-and-paste the following demo data into your code and adjust your constructors so that the code works without modification:

~~~ scala
val eastwood          = new Director("Clint", "Eastwood", 1930)
val mcTiernan         = new Director("John", "McTiernan", 1951)
val nolan             = new Director("Christopher", "Nolan", 1970)
val someBody          = new Director("Just", "Some Body", 1990)

val memento           = new Film("Memento", 2000, 8.5, nolan)
val darkKnight        = new Film("Dark Knight", 2008, 9.0, nolan)
val inception         = new Film("Inception", 2010, 8.8, nolan)

val highPlainsDrifter = new Film("High Plains Drifter", 1973, 7.7, eastwood)
val outlawJoseyWales  = new Film("The Outlaw Josey Wales", 1976, 7.9, eastwood)
val unforgiven        = new Film("Unforgiven", 1992, 8.3, eastwood)
val granTorino        = new Film("Gran Torino", 2008, 8.2, eastwood)
val invictus          = new Film("Invictus", 2009, 7.4, eastwood)

val predator          = new Film("Predator", 1987, 7.9, mcTiernan)
val dieHard           = new Film("Die Hard", 1988, 8.3, mcTiernan)
val huntForRedOctober = new Film("The Hunt for Red October", 1990, 7.6, mcTiernan)
val thomasCrownAffair = new Film("The Thomas Crown Affair", 1999, 6.8, mcTiernan)

eastwood.yearOfBirth         // should be 1930
dieHard.director.name        // should be "John McTiernan"
invictus.isDirectedBy(nolan) // should be false
~~~

Implement a method of `Film` called `copy`. This method should accept the same parameters as the constructor and create a new copy of the film. Give each parameter a default value so you can copy a film changing any subset of its values:

~~~ scala
highPlainsDrifter.copy(name = "L'homme des hautes plaines")
// returns Film("L'homme des hautes plaines", 1973, 7.7, /* etc */)

thomasCrownAffair.copy(yearOfRelease = 1968,
  director = new Director("Norman", "Jewison", 1926))
// returns Film("The Thomas Crown Affair", 1926, /* etc */)

inception.copy().copy().copy()
// returns a new copy of `inception`
~~~

<div class="solution">
This exercise provides some hands on experience writing Scala classes, fields and methods. The model solution is as follows:

~~~ scala
class Director(
  val firstName: String,
  val lastName: String,
  val yearOfBirth: Int) {

  def name: String =
    s"$firstName $lastName"

  def copy(
    firstName: String = this.firstName,
    lastName: String = this.lastName,
    yearOfBirth: Int = this.yearOfBirth): Director =
    new Director(firstName, lastName, yearOfBirth)
}

class Film(
  val name: String,
  val yearOfRelease: Int,
  val imdbRating: Double,
  val director: Director) {

  def directorsAge =
    yearOfRelease - director.yearOfBirth

  def isDirectedBy(director: Director) =
    this.director == director

  def copy(
    name: String = this.name,
    yearOfRelease: Int = this.yearOfRelease,
    imdbRating: Double = this.imdbRating,
    director: Director = this.director): Film =
    new Film(name, yearOfRelease, imdbRating, director)
}
~~~
</div>

#### A Simple Counter

Implement a `Counter` class. The constructor should take an `Int`. The methods `inc` and `dec` should increment and decrement the counter respectively returning a new `Counter`. Here's an example of the usage:

~~~ scala
new Counter(10).inc.dec.inc.inc.count
// res: Int = 12
~~~

<div class="solution">
~~~ scala
class Counter(val count: Int) {
  def dec = new Counter(count - 1)
  def inc = new Counter(count + 1)
}
~~~

Aside from practicing with classes and objects, this exercise has a second goal---to think about why `inc` and `dec` return a new `Counter`, rather than updating the same counter directly.

Because `val` fields are immutable, we need to come up with some other way of propagating the new value of `count`. Methods that return new `Counter` objects give us a way of returning new state without the side-effects of assignment. They also permit *method chaining*, allowing us to write whole sequences of updates in a single expression

<div class="alert alert-info">
**Performance tip**

The use-case `new Counter(10).inc.dec.inc.inc.count` actually creates 5 instances of `Counter` before returning its final `Int` value. You may be concerned about the extra memory and CPU overhead for such a simple calculation, but don't be. Modern execution environments like the JVM render the extra overhead of this style of programming negligable in all but the most performance critical code.
</div>
</div>

#### Counting Faster

Augment the `Counter` from the previous exercise to allow the user can optionally pass an `Int` parameter to `inc` and `dec`. If the parameter is omitted it should default to `1`.

<div class="solution">
The simplest solution is this:

~~~ scala
class Counter(val count: Int) {
  def dec(amount: Int = 1) = new Counter(count - amount)
  def inc(amount: Int = 1) = new Counter(count + amount)
}
~~~

However, this adds parentheses to `inc` and `dec`. If we omit the parameter we now have to provide an empty pair of parentheses:

~~~ scala
new Counter(10).inc
// error: missing arguments for method inc in class Counter;
// follow this method with `_' if you want to treat it as a partially applied function
//               new Counter(10).inc
//                               ^
~~~

We can work around this using *method overloading* to recreate our original parenthesis-free methods. Note that overloading methods requires us to specify the return types:

~~~ scala
class Counter(val count: Int) {
  def dec: Counter = dec()
  def inc: Counter = inc()
  def dec(amount: Int = 1): Counter = new Counter(count - amount)
  def inc(amount: Int = 1): Counter = new Counter(count + amount)
}

new Counter(10).inc.inc(10).count
// res: Int = 21
~~~
</div>

#### Additional Counting

Here is a simple class called `Adder`.

~~~ scala
class Adder(amount: Int) {
  def add(in: Int) = in + amount
}
~~~

Extend `Counter` to add a method called `adjust`. This method should accept an `Adder` and return a new `Counter` with the result of applying the `Adder` to the `count`.

<div class="solution">
~~~ scala
class Counter(val count: Int) {
  def dec = new Counter(count - 1)
  def inc = new Counter(count + 1)
  def adjust(adder: Adder) =
    new Counter(adder.add(count))
}
~~~

This is an interesting pattern that will become more powerful as we learn more features of Scala. *We are using `Adders` to capture computations* and pass them to `Counter`. Remember from our earlier discussion that *methods are not expressions*---they cannot be stored in fields or passed around as data. However, *`Adders` are both objects and computations*.

Using objects as computations is a common paradigm in object oriented programming languages. Consider, for example, the classic `ActionListener` from Java's Swing:

~~~ java
public class MyActionListener implements ActionListener {
  public void actionPerformed(ActionEvent evt) {
    // Do some computation
  }
}
~~~

The disadvantage of objects like `Adders` and `ActionListeners` is that they are limited to use in one particular circumstance. Scala includes a much more general concept called *functions* that allow us to represent any kind of computation as an object. We will be introduced to some of the concepts behind functions in this chapter.
</div>
