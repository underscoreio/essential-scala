---
layout: page
title: Objects and Functions
---

Hopefully you did the last set of exercises as they motivate what we're covering here. We're going to look at two special types of objects things: companion objects and functions.


## Companion Objects

Sometimes we want a method that is independent of any instance, but logically belongs to some class. For example, we might want to validate an email address before we accept it as a login. In Java we'd use a static method. In Scala we can just put that method on an object. We don't need the concept of static methods as objects replace their use. However, which object should hold that method?

Sometimes we want to have more than one constructor for a class. The convention in Scala is to have only one constructor per class[^constructor]. In the last exercises we implemented a rather awkward `PersonFactory` class to construct `Person`s. Really this functionality should just be a method on an object, but again what object should hold it? Ideally it would be a `Person` object, but we already have a class with that name.

[^constructor:] You can have more than one constructor per class, but it is unusual, and we won't cover it here.

Scala answers this issue with the concept of **companion objects**. A companion object is just an object with the same name as a class. Due to the way companion objects and classes are compiled, they must be defined in the same file. Here's the `PersonFactory` example rewritten with a companion object. In this example we use the console's *paste mode* to define the companions at the same time:

~~~ scala
scala> :paste
// Entering paste mode (ctrl-D to finish)
class Person(val firstName: String, val lastName: String) {
  def name = firstName + " " + lastName
}

object Person {
  def fromName(name: String): Person = {
    val parts = name.split(" ")
    new Person(parts(0), parts(1))
  }
}
^D

// Exiting paste mode, now interpreting.

defined class Person
defined module Person
~~~

Using a companion object is no different to using a normal object.

~~~ scala
scala> Person.fromName("John Doe")
res0: Person = Person@4ca8cd58

scala> Person.fromName("John Doe").name
res1: String = John Doe
~~~


## Functions

If an object `foo` has a method called `apply` we can call that method using `foo(args)`. For example, `String` has an apply method (through a mysterious mechansim we [explain later](/collections/arrays-and-strings.html)) that allows to us to index characters within the string.

~~~scala
scala> "hi there!"(0)
res35: Char = h
~~~
Be aware there is no dot before the parenthesis. Adding one is an error!

~~~scala
scala> "hi there!".(0)
<console>:1: error: identifier expected but '(' found.
       "hi there!".(0)
~~~

With this one neat trick objects can become functions. There are lots of things that methods can't do: be passed to other methods or returned from methods, for instance, which objects can. Since we can treat objects as functions this means we can do the same things with functions. They are what is called **first class values**.

Scala takes this trick further, giving us some short-hand syntax for functions. For example, here's the function that adds one to an `Int`.

~~~ scala
scala> (x: Int) => x + 1
res3: Int => Int = <function1>
~~~

What's the type of this?

~~~ scala
scala> :type (x: Int) => x + 1
Int => Int
~~~

This means a function that takes an `Int` and returns an `Int`. This extends naturally to functions of more than one argument.

~~~ scala
scala> :type (x: Int, y:Int) => x + y
(Int, Int) => Int
~~~

Scala defines classes for functions, beginning with `Function0` for functions that take no arguments, and going upwards as the number of arguments increase. The syntax `Int => Int` is a shorthand for `Function1[Int, Int]`.

Finally, Scala gives us syntax to convert a method to a function. If we follow a method name with an underscore, Scala will convert the method to a function.

~~~ scala
scala> val john = Person.fromName("John Doe")
john: Person = Person@30d8f246

scala> john.name _
res9: () => String = <function0>
~~~

## Exercises

#### Functional Counters

Rewrite the `map` method of `Counter` (see the exercises in the previous section) to take a function.

<div class="solution">
~~~ scala
class Counter(val count: Int) {
  def dec = new Counter(count - 1)
  def inc = new Counter(count + 1)
  def map(f: Int => Int) =
    new Counter(f(count))
}
~~~
</div>

#### Companion Object Apply

What happens if we add an `apply` method to a companion object? By convention in Scala this is used to call the class constructor without the user having to write `new`. Implement this for `Person`.

<div class="solution">
~~~ scala
class Person(val firstName: String, val lastName: String) {
  def name = firstName + " " + lastName
}

object Person {
  def apply(firstName: String, lastName: String) =
    new Person(firstName, lastName)

  def fromName(name: String): Person = {
    val parts = name.split(" ")
    new Person(parts(0), parts(1))
  }
}
~~~
</div>

Why is this useful? We've already seen, with the `Counter` example, a style of programming where we create objects instead of changing existing objects. This style of programming is one of the hallmarks of functional programming, and we're going to see a lot more of it. Writing (and reading) `new` everywhere gets very annoying in this style, so Scala programmers minimise its use.


## Case Classes

The **case class* is a final trick in Scala that automates much of we've just discussed. If you write

~~~ scala
case class Person(val firstName: String, val lastName: String)
~~~

Scala will automatically generate a companion object with an `apply` constructor as well as a number of other useful methods on the class and companion object. Here's the companion object constructor in use:

~~~
scala> Person("John", "Doe")
res11: Person = Person(John,Doe)
~~~

Notice that `Person` object prints out in a meaningful way, instead of the strange hieroglyphs like `Person@30d8f246` we have seen before. That's one change. We also a `copy` method that makes it easy to create a new `Person` derived from an existing one.

~~~ scala
scala> Person("John", "Doe").copy(firstName = "James")
res12: Person = Person(James,Doe)
~~~

Here we see the use of **keyword arguments** in Scala. They are entirely optional
