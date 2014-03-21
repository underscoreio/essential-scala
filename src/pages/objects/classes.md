---
layout: page
title: Classes
---

A class is a template for creating objects. When we define a class we can create objects that all have similar methods and fields and, importantly, all have the same type. This means we can pass different objects with the same class to a method expecting that class. This will help us overcome the problem we had in the "*Greetings, Humans*" exercise in the last section.

### Defining a Class

Here is a simple class definition:

~~~ scala
scala> class Person {
     |   val firstName = "Noel"
     |   val lastName = "Welsh"
     |   def name = firstName + " " + lastName
     | }
defined class Person
~~~

The class is called `Person`. We can create a new `Person` object using the `new` operator as follows, and access its methods and fields of our new object in the usual way.

~~~ scala
scala> val noel = new Person
noel: Person = Person@3235186a

scala> noel.firstName
res24: String = Noel
~~~

Notice the type of the object is `Person`. We can use this to show that each call to `new` creates a distinct object of the same type.

~~~ scala
scala> noel // noel is still the object that prints '@3235186a'
res25: Person = Person@3235186a

scala> val newNoel = new Person // each new object has a new number
newNoel: Person = Person@2792b987

scala> val anotherNewNoel = new Person
anotherNewNoel: Person = Person@63ee4826
~~~

This means we can write a method that takes a `Person` as a parameter and use it with any of our objects.

~~~ scala
scala> object alien {
     |   def greet(p: Person) =
     |     "Greetings, " + p.firstName + " " + p.lastName
     | }
defined module alien

scala> alien.greet(noel)
res5: String = Greetings, Noel Welsh

scala> alien.greet(newNoel)
res6: String = Greetings, Noel Welsh
~~~

<div class="alert alert-info">
**Java tip:** The printed value of each person should be recognisable to Java developers - it is the default implementation of `toString` from `java.lang.Object`. Scala classes are all subclasses of `java.lang.Object` and are, for the most part usable from Java as well as Scala. We can override `toString` to provide our own implementation but we often don't have to as we will see in a moment.
</div>

[Scala in Depth]: http://www.manning.com/suereth

### Constructors

As it stands our `Person` class is rather useless. We can create as many new objects as we want, but all will have the same `firstName` and `lastName`. What if we want to model a person with a different name?

The solution is to introduce a **constructor**, which allows us to pass parameters to the new objects as we create them.

~~~ scala
scala> class Person(first: String, last: String) {
     |   val firstName = first
     |   val lastName = last
     |   def name = firstName + " " + lastName
     | }
defined class Person

scala> val dave = new Person("Dave", "Gurnell")
dave: Person = Person@3ed12df7

scala> dave.name
res29: String = Dave Gurnell
~~~

Scala provides us a useful short-hand way of declaring constructor parameters and fields in one go: simply prefix the constructor parameters with the `val` keyword:

~~~ scala
scala> class Person(val firstName: String, val lastName: String) {
     |   def name = firstName + " " + lastName
     | }
defined class Person

scala> new Person("Dave", "Gurnell").name
res29: String = Dave Gurnell
~~~

Constructor arguments are local variables that are only visible from the body of the class. You must declare a field or a method to access data from outside the object.

### Exercises

We now have enough machinery to have some fun playing with classes.

#### A Simple Counter

Implement a `Counter` class. The constructor should take an `Int`. The methods `inc` and `dec` should increment and decrement the counter respectively returning a new `Counter`. Here's an example of the usage:

~~~ scala
scala> new Counter(10).inc.dec.inc.inc.count
res42: Int = 12
~~~

<div class="solution">
~~~ scala
class Counter(val count: Int) {
  def dec = new Counter(count - 1)
  def inc = new Counter(count + 1)
}
~~~

There are two goals to this exercise. The first is practice creating simple classes and objects. The second is think about why `inc` and `dec` return a new `Counter`, rather than updating the same counter directly.

Note that we haven't seen any syntax for assigning a new value to a field yet. In fact, the `val` fields we have created are immutable and can't be changed. This is useful because assignment is a *side-effect* that means we can no longer reason about our programs using the substitution model.

The use-case `new Counter(10).inc.dec.inc.inc.count` actually creates 5 instances of `Counter` before returning its final `Int` value. You may be concerned about the extra memory and CPU overhead for such a simple calculation, but don't be.

It is true that writing side-effect-free functional code comes with a small performance penalty at run time. However, with a modern execution environment such as the JVM the cost is negligable in all but the most performance critical modules of code.
</div>

#### Constructors versus Factories

In this exercise, we will implement a more convenient way of creating a `Person` using their whole name instead of their first name and last names individually.

We can split a `String` into an `Array` of components as follows:

~~~ scala
scala> val parts = "John Doe".split(" ")
parts: Array[String] = Array(John, Doe)

scala> parts(0)
res36: String = John
~~~

Create a class, called a `PersonFactory` with a single method with signature `create(name: String): Person`. When `create` is called with a `String` it should split the `String` at the space character and construct a `Person`. Here's an example of use:

~~~ scala
scala> val factory = new PersonFactory()
factory: PersonFactory = PersonFactory@4bce79b8

scala> val john = factory.create("John Doe")
john: Person = Person@2fe6a820

scala> john.firstName
res37: String = John

scala> john.name
res38: String = John Doe
~~~

<div class="solution">
~~~ scala
class PersonFactory {
  def create(name: String): Person = {
    val parts = name.split(" ")
    new Person(parts(0), parts(1))
  }
}
~~~

The requirement to create a `PersonFactory` in order to conveniently create a `Person` is a bit awkward. We can simplify things a bit by making `PersonFactory` a singleton object.

~~~ scala
object PersonFactory {
  def create(name: String): Person = {
    val parts = name.split(" ")
    new Person(parts(0), parts(1))
  }
}
~~~

We can use this as follows.

~~~ scala
scala> PersonFactory.create("John Doe").firstName
res6: String = John
~~~

It is quite common in Scala to house auxiliary constructors for our classes in singleton objects. As we will see in the next section, Scala even has special rules for **companion objects** that explicitly support a class.
</div>

#### Counting Faster

Here is a simple class called `Adder`.

~~~ scala
class Adder(amount: Int) {
  def add(in: Int) = in + amount
}
~~~

Extend your `Counter` class to have a method called `incWithAdder`. This method should accept an `Adder` and return a new `Counter` with the result of applying the `Adder` to the `count`.

<div class="solution">
~~~ scala
class Counter(val count: Int) {
  def dec = new Counter(count - 1)
  def inc = new Counter(count + 1)
  def incWithAdder(adder: Adder) =
    new Counter(adder.add(count))
}
~~~

This is an interesting pattern: we are effectively using `Adder` objects to capture computations and pass them to an argument. Remember our discussion earler about methods: *methods are not expressions* - they cannot be stored in fields or passed around as data. Our `Adders` bypass this restriction by being both a computation and an object.

Using objects to represent computation is a common paradigm in object oriented programming languages. Consider, for example, event listeners such as Java Swing's `ActionListener`.

~~~ java
public class MyActionListener implements ActionListener {
  public void actionPerformed(ActionEvent evt) {
    // Do some computation in response to a button click
  }
}
~~~

`ActionListeners` a limited concept though: they can only be used in a particular circumstance. Scala includes a much more general concept called `Functions` that allow us to represent any kind of computation as an object, from an `Adder` to an `ActionListener` to almost any other piece of code we can imagine. We will see these in the next section.
</div>
