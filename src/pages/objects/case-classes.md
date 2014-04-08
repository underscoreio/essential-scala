---
layout: page
title: Case Classes
---

**Case classes** are an exceptionally useful shorthand for defining a class, a companion object, and a lot of sensible defaults in one go. They are ideal for creating lightweight data-holding classes with the minimum of hassle.

Case classes are created simply by prepending a class definition with the keyword `case`:

~~~ scala
scala> case class Person(firstName: String, lastName: String) {
     |   def name = firstName + " " + lastName
     | }
defined class Person
~~~

Whenever we declare a case class, Scala automatically generates a **class and companion object**:

~~~ scala
scala> new Person("Dave", "Gurnell") // we have a class
res0: Person = Person(Dave,Gurnell)

scala> Person // and a companion object too
res1: Person.type = Person
~~~

What's more, the class and companion are pre-populated with some very useful features.

## Features of a case class

1. **A field for each constructor argument** -- we don't even need to write `val` in our constructor definition, although there's no harm in doing so.

   ~~~ scala
   scala> res0.firstName
   res2: String = Dave
   ~~~

2. **A default `toString` method** that prints a sensible constructor-like representation of the class (no more `@` signs and cryptic hex numbers):

   ~~~ scala
   scala> res0
   res2: Person = Person("Dave","Gurnell")
   ~~~

3. **Sensible `equals`, and `hashCode` methods** that operate on the field field values in the object.

   This makes it easy to use case classes with collections like `Lists`, `Sets` and `Maps`. It also means we can compare objects on the basis of their contents rather than their reference identity:

   ~~~ scala
   scala> new Person("Noel", "Welsh").equals(new Person("Noel", "Welsh"))
   res3: Boolean = true

   scala> new Person("Noel", "Welsh") == new Person("Noel", "Welsh")
   res4: Boolean = true
   ~~~

   <div class="alert alert-info">
   **Java tip:** The two expressions above are equivalent. Scala's `==` operator is different from Java's -- it delegates to `equals` rather than comparing values on reference identity.

   Scala has an operator called `eq` with the same behaviour as Java's `==`. However, it is rarely used in application code:

   ~~~ scala
   scala> new Person("Noel", "Welsh") eq (new Person("Noel", "Welsh"))
   res5: Boolean = false

   scala> res0 eq res0
   res6: Boolean = true
   ~~~
   </div>

4. **A `copy` method** that creates a new object with the same field values as the current one:

   ~~~ scala
   scala> res0.copy()
   res7: Person = Person(Dave,Gurnell)
   ~~~

   Note that the `copy` method creates and returns a *new object* of the class rather than returning the current one:

   ~~~ scala
   scala> res0.copy() eq res0
   res10: Boolean = false
   ~~~

   The `copy` method actually accepts optional parameters matching each of the constructor parameters. If a parameter is specified the new object uses that value instead of the existing value from the current object. This is ideal for use with keyword parameters to let us copy an object while changing the values of one or more fields:

   ~~~ scala
   scala> res0.copy(firstName = "Dave2")
   res8: Person = Person(Dave2,Gurnell)

   scala> res0.copy(lastName = "Gurnell2")
   res9: Person = Person(Dave,Gurnell2)
   ~~~

## Features of a case class companion object

The companion object contains an `apply` method with the same arguments as the class constructor. Scala programmers tend to prefer the `apply` method over the constructor for the brevity of omitting `new`, which makes constructors much easier to read insides expressions:

~~~ scala
scala> Person("Dave", "Gurnell") == Person("Noel", "Welsh")
res7: Boolean = false

scala> Person("Dave", "Gurnell") == Person("Dave", "Gurnell")
res8: Boolean = true
~~~

Finally, the companion object also contains code to implement an **extractor pattern** for use in *pattern matching*. We'll see this later this chapter.

## Case objects

A final note. If you find yourself defining a case class with no constructor arguments you can instead a define a **case object**. A case object is defined just like a case class and has the same default methods as a case class.

~~~ scala
case object Citizen {
  def firstName = "John"
  def lastName  = "Doe"
  def name = firstName + " " + lastName
}
~~~

The differences between a case object and a regular singleton object are:

 - The `case object` keyword defines a class and an object, and makes the object an instance (actually the only instance) of the class:

   ~~~ scala
   class Citizen { /* ... */ }
   object Citizen extends Citizen { /* ... */ }
   ~~~

 - With a case object we still get all of the functionality defined for case classes above.

## Take Home Points

Case classes are the **bread and butter of Scala data types**. Use them, learn them, love them.

Case classes have numerous auto-generated methods and features that save typing. We can override this behaviour on a piece-by-piece basis by implementing the relevant methods ourselves.

In Scala 2.10 and earlier we can define case classes containing 0 to 22 fields. In Scala 2.11 (June 2014 release) we gain the ability to define arbitrarily-sized case classes.

## Exercises

### Case Class Counter

Reimplement `Counter` as a case class, using `copy` where appropriate. Additionally initialise `count` to a default value of `0`.

<div class="solution">
~~~ scala
case class Counter(val count: Int = 0) {
  def dec = copy(count = count - 1)
  def inc = copy(count = count + 1)
  def adjust(adder: Adder) = copy(count = adder(count))
}
~~~

This is almost a trick exercise -- there are very few differences with the previous implementation However, notice the extra functionality we got for free:

~~~ scala
scala> Counter(0) // construct objects without `new`
res9: Counter = Counter(0)

scala> Counter().inc // printout shows the value of `count`
res10: Counter = Counter(1)

scala> Counter().inc.dec == Counter().dec.inc // semantic equality check
res11: Boolean = true
~~~
</div>

### Application, Application, Application

What happens when we define a companion object for a case class? Let's see.

Take our `Person` class from the previous section and turn it into a case class (hint: the code is above). Make sure you still have the companion object with the alternate `apply` method as well.

<div class="solution">
Here's the code:

~~~ scala
case class Person(firstName: String, lastName: String) {
  def name = firstName + " " + lastName
}

object Person {
  def apply(name: String): Person = {
    val parts = name.split(" ")
    apply(parts(0), parts(1))
  }
}
~~~

Even though we are defining a companion object for `Person`, Scala's case class code generator is still working as expected -- it adds the auto-generated companion methods to the object we have defined, which is why we need to place the class and companion in a single compilation unit.

This means we end up with a companion object with an overloaded `apply` method with two possible type signatures:

~~~ scala
def apply(name: String): Person // and ...
def apply(firstName: String, lastName: String): Person
~~~
</div>