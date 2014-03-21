---
layout: page
title: Companion Objects
---

Sometimes we want to create a method that logically belongs to a class but is independent of any particular instance of the class. The auxiliary constructor in `PersonFactory` in the exercises from the previous section is a good example[^constructor]. In Java we would use *static methods* for this, but Scala has a much simpler solution: singleton objects!

In the previous exercises we crated a `PersonFactory` object to construct `Persons`, but ideally we'd like to call our factory object the same thing as our class. Fortunately, Scala uses separate namespaces for classes and objects, so it is possible to define a `Person` object and a `Person` class without any conflicts.

[^constructor]: Actually, technically speaking you *can* define more than one constructor for a class in Scala, but it is unusual and unnecessary and we won't cover it here.

Scala has a special name for a singleton object called the same thing as a class: a **companion object**. In addition to providing a convenient place for methods, companion objects also have special significance for something called *implicit resolution* that we will cover much later.

Due to the way companion objects and classes are compiled, they must be defined in the same file. Here's the `PersonFactory` example rewritten as a companion object. Note the use of *paste mode* on the REPL to define the class and companion at the same time.

~~~ scala
scala> :paste
// Entering paste mode (ctrl-D to finish)
class Person(val firstName: String, val lastName: String) {
  def name = firstName + " " + lastName
}

object Person {
  def create(name: String): Person = {
    val parts = name.split(" ")
    new Person(parts(0), parts(1))
  }
}
^D

// Exiting paste mode, now interpreting.

defined class Person
defined module Person

scala> Person.create("John Doe")
res0: Person = Person@4ca8cd58

scala> Person.create("John Doe").firstName
res1: String = John
~~~

No exercises here - let's continue to the next section!