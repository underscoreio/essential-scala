---
layout: page
title: Case Classes
---

**Case classes** are an exceptionally useful way of defining a class, a companion object, and  a lot of sensible defaults in one go. They are ideal for defining lightweight data-holding classes with the minimum of hassle.

Case classes are created simply by prepending a class definition with the keyword `case`:

~~~ scala
scala> case class Person(firstName: String, lastName: String) {
     |   def name = firstName + " " + lastName
     | }
defined class Person
~~~

Whenever we declare a case class, Scala automatically generates a **class and companion object** and populates them with default method definitions:

~~~ scala
scala> new Person("Dave", "Gurnell") // we have a class
res0: Person = Person(Dave,Gurnell)

scala> Person // and a companion object too
res1: Person.type = Person
~~~

### Features of the class

1. A **field for each constructor argument** -- we don't even need to write `val` in our constructor definition, although there's no harm in doing so.

   ~~~ scala
   scala> res0.firstName
   res2: String = Dave
   ~~~

2. A **default `toString` method** that prints a sensible constructor-like representation of the class (no more `@` signs):

   ~~~ scala
   scala> res0
   res2: Person = Person("Dave","Gurnell")
   ~~~

3. Sensible **`equals`, and `hashCode` methods** that operate on the field field values in the object.

   This makes it easy to use case classes with collections like `Lists`, `Sets` and `Maps`. It also means we can compare objects on the basis of their contents:

   ~~~ scala
   scala> new Person("Noel", "Welsh") == new Person("Noel", "Welsh")
   res4: Boolean = true
   ~~~

   <div class="alert alert-info">
   **Java tip:** Scala's `==` operator is different from Java's -- it delegates to `equals` rather than comparing values on reference identity.

   Scala has an operator called `eq` that behaves like Java's `==` operator. However, it is rarely used in application code:

   ~~~ scala
   scala> new Person("Noel", "Welsh") eq (new Person("Noel", "Welsh"))
   res5: Boolean = false

   scala> res0 eq res0
   res6: Boolean = true
   ~~~
   </div>

4. A **copy method** that creates a new object with the same field values:

   ~~~ scala
   scala> res0.copy()
   res7: Person = Person(Dave,Gurnell)
   ~~~

   Note that the copy constructor returns a *new object* of the class rather than updating the existing object. This is good because it keeps our code free of side-effects, promoting a clean functional programming style:

   ~~~ scala
   scala> res0.copy() eq res0
   res10: Boolean = false
   ~~~

   The `copy` method actually accepts the same parameters as the constructor, but provides a default value for each that transfers the relevant field value from the current object to the new one. This is ideal for use with keyword parameters -- we can add, remove, and reorder the fields in our case class without affecting the semantics of the copy:

   ~~~ scala
   scala> res0.copy(firstName = "Dave2")
   res8: Person = Person(Dave2,Gurnell)

   scala> res0.copy(lastName = "Gurnell2")
   res9: Person = Person(Dave,Gurnell2)
   ~~~

### Features of the companion object

The companion object contains an `apply` method with the same arguments as the class constructor. Scala programmers tend to prefer the `apply` method over the constructor for its brevity, which facilitates creating objects inline within expressions:

~~~ scala
scala> Person("Dave", "Gurnell") == Person("Noel", "Welsh")
res7: Boolean = false

scala> Person("Dave", "Gurnell") == Person("Dave", "Gurnell")
res8: Boolean = true
~~~

Finally, the companion object also contains code to implement an *extractor pattern* for use in pattern matching. We'll see this in a moment.

### Case objects

A final note. If you find yourself defining a case class with no constructor arguments you can instead a define a **case object**. A case object is defined just like a case class and has the same default methods as a case class.

~~~ scala
case object Citizen {
  def firstName = "John"
  def lastName  = "Doe"
  def name = firstName + " " + lastName
}
~~~

### Exercises

#### Case Class Counter

Reimplement `Counter` as a case class, using `copy` where appropriate. Additionally initialise `count` to a default value of 0.

<div class="solution">
~~~ scala
case class Counter(val count: Int = 0) {
  def dec = new Counter(count - 1)
  def inc = new Counter(count + 1)
  def adjust(adder: Adder) = Counter(adder(count))
}
~~~

This is almost a trick exercise: the only differences are the `case` keyword and the removal of the `new` operator in `adjust`. However, notice the extra functionality we got for free:

~~~ scala
scala> Counter(0) // construct objects without `new`
res9: Counter = Counter(0)

scala> Counter().inc // printout shows the value of `count`
res10: Counter = Counter(1)

scala> Counter().inc.dec == Counter().dec.inc // semantic equality check
res11: Boolean = true
~~~
</div>
