---
layout: page
title: Implicit Conversions
---

Implicits are a general mechanism to get the compiler to do work for you. In particular, with implicits you can get the compiler to:

* call a function you have not explicitly written, called an **implicit conversion**; and
* supply a parameter to a method that have not explicitly supplied, called an **implicit parameter**.

Implicits allow a new way of factoring code, called a **type class*, but also require care to ensure code remains comprehensible.

In this section we introduce the two forms of implicits: implicit conversions and implicit parameter lists. In later sections we'll see some common uses.


## Implicit conversions

An implicit conversion is one of the two types of implicits. It allows the compiler to automatically apply a conversion from one type to another, when that conversion has not been written in the code. The process works like this:

* we have a class `B` with a method `bar`;
* we class `A` without method `bar`;
* we have an implicit conversion from `A` to `B`; and
* we call method `bar` on an instance of `A`.

If all these conditions are met, the compiler will insert a call to the implicit conversion, converting `A` to `B` so the method call of `foo` compiles.

~~~ scala
scala> class B {
     |   def bar = "This is the best method ever!"
     | }
defined class B

scala> class A {}
defined class A

scala> new A().bar // Fails to compile
<console>:9: error: value bar is not a member of A
              new A().bar
                      ^

scala> implicit def aToB(in: A): B = new B()
aToB: (in: A)B

scala> new A().bar
res1: String = This is the best method ever!
~~~

This pattern is sometimes called pimping, after a [blog post](http://www.artima.com/weblogs/viewpost.jsp?thread=179766) by Martin Odersky.

## Exercises

#### Oh Yeah!

Use your newfound powers to add a method `yeah` to `Int`, which prints `Oh yeah!` as many times as the `Int` on which it is called if the `Int` is positive, and is silent otherwise. Here's an example of usage:

~~~ scala
scala> 2.yeah
Oh yeah!
Oh yeah!

scala> 3.yeah
Oh yeah!
Oh yeah!
Oh yeah!

scala> -1.yeah

~~~

<div class="solution">
~~~ scala
class Yeah(n: Int) {
  def yeah =
    for(_ <- 0 until n) println("Oh yeah!")
}

implicit def intToYeah(n: Int): Yeah =
  new Yeah(n)
~~~
</div>

#### Times

Extend `Int` with a method called `times` that executes the body `n` times, where `n` is an `Int`. Bonus marks for using a call-by-name parameter. For example, I should be able to write `5 times println("Hi!")` and have `Hi!` printed five times.

<div class="solution">
~~~ scala
class Times(n: Int) {
  def times(e: => Any) =
      for(_ <- 0 until n) e
}

implicit def intToTimes(n: Int): Times =
  new Times(n)
~~~
</div>


## Implicit Parameters

Implicit parmaters are the other type of implicit. Implicit parameters allow the compiler to insert parameters to a method call if we do not supply parameters explicitly in our code.

### Multiple parameter lists

Before we can discuss implicit parameters we need to know about multiple parameter lists. In addition to supporting any number of parameters, methods and functions in Scala can have any number of *parameter lists*. For example:

~~~ scala
scala> def add(a: Int, b: Int)(c: Int, d: Int): Int = {
     |   a + b + c + d
     | }
add: (a: Int, b: Int)(c: Int, d: Int)Int

scala> add(1, 2)(3, 4)
res0: Int = 10
~~~

There are a number of reasons why this is useful: it allows for more flexibility when assigning default values to optional arguments, it helps type inference, it provides support for *currying*, and it provides support for *implicit argument lists*. This section covers the last of these three.

{% comment %}
TODO: Supply examples of currying and default values?
{% endcomment %}

### Implicit parameter lists

We can add the keyword `implicit` to the beginning of a parameter list which indicates the compiler may implicitly insert arguments for any parameter in the list. For example:

~~~ scala
scala> class User(val name: String)
defined class User

scala> def prompt(directory: String)(implicit user: User): String = {
     |   user.name + ":" + directory + "$"
     | }
prompt: (directory: String)(implicit user: User)String
~~~

When we call `prompt`, we have the option of supplying all the necessary arguments or allowing the compiler to supply those marked as `implicit`:

~~~ scala
scala> prompt("~")(new User("john"))
res1: String = john:~$
~~~

If we omit the implicit arguments from the method call, the compiler will search for values to insert in their place. It searches local scope (roughly - see below for caveats) for `vals` and argumentless `defs` that have been declared with the `implicit` keyword:

~~~ scala
scala> implicit def currentUser: User = new User("dave")
currentUser: User

scala> prompt("~") // compiler expands this to: prompt("~")(currentUser)
res2: String = dave:~$
~~~

## Exercises

#### Multiple Parameter Lists

Add a method `fold` to `Int`. `fold` has two parameter lists. The first accepts a seed of type `A`. The second accepts a function from `(A, Int) => A`. The method folds over the integers from zero until the given number. Example usage:

~~~ scala
scala> 4.fold(0)(_ + _)
res10: Int = 6

scala> -5.fold(1)(_ + _)
res13: Int = -9
~~~

Hint: This exercise uses an implicit conversion and multiple parameter lists but *not* implicit parameters.

<div class="solution">
~~~ scala
class IntFold(n: Int) {
  def fold[A](zero: A)(f: (A, Int) => A): A = {
    var result: A = zero

    val direction = if(n < 0) -1 else 1
    for(i <- 0 until n by direction) {
      result = f(result, i)
    }
    result
  }
}

implicit def intToFold(n: Int): IntFold = new IntFold(n)
~~~
</div>

#### Implicit Parameter Lists

A Pet is a Dog or a Cat. Model this in Scala.

<div class="solution">
~~~ scala
sealed trait Pet
final case class Dog() extends Pet
final case class Cat() extends Pet
~~~
</div>

Here is a `Sound` type:

~~~ scala
case class Sound()
~~~

Add a method `beg` to this class that takes a `Pet` and returns a `String` sound.

<div class="solution">
~~~ scala
case class Sound() {
  def beg(pet: Pet): String =
    pet match {
      case Dog() => "woof"
      case Cat() => "meow"
    }
}
~~~
</div>

Make an object `Mealtime` with a method `breakfast`. `Breakfast` has two parameter lists. The first accepts a `Pet`, the second implicit list accepts a `Sound`. When `breakfast` is called is returns the appropriate sound (a `String`) for the pet.

<div class="solution">
~~~ scala
object Mealtime {
  def breakfast(pet: Pet)(implicit sound: Sound) =
      sound.beg(pet)
}
~~~
</div>

Define an implicit instance of `Sound` so that you can call `Mealtime` with just a `Pet`.

<div class="solution">
~~~ scala
implicit val sound = Sound()

Mealtime.breakfast(Cat())
~~~

Notice that the implicit parameter is supplied by the compiler.
</div>

Define another implicit instance of `Sound` and call `Mealtime` with just a `Pet`. What happens?

<div class="solution">
Now there is ambiguity between implicits and the compiler refuses to supply one. And this brings us to the next section...
</div>

## Rules for Implicit Resolution

Implicits are, by definition, not explicitly used in our code. It is important to have a clear understanding of how the compiler searches for implicits to understand code using them.

### Rules for Implicit Values

When searching for implicit values to supply as implicit parameters, the compiler is restricted by a number of precise rules:

 1. **Marking Rule:** Only definitions marked `implicit` are available.
 2. **Scope Rule:** An implicit must be in *implicit scope* (see below).
 3. **Non-Ambiguity Rule:** An implicit can only be used if there are no other applicable implicits in scope.

### Implicit scope

 - Must be in scope as a single identifier (i.e. not `a.b`)
 - Except the compiler will look for definitions in the companion objects for the source and target types for conversion.

### Rules for Implicit Conversion Resolution

In addition to the rules for implicit value resolution above, use of implicit conversions is subject to the following additional rules:

 4. **One-at-a-time Rule:** Only a single implicit conversion can be used for any given situation (i.e. the compiler does not chain multiple conversions to resolve a conflict).
 5. **Explicits-First Rule:** Whenever code type checks as it is written, no implicits are attempted.

### Good Practice for Implicit Conversions

Implicit conversions can easily make code difficult to understand, particularly if the user is unaware they are using implicit conversions in their code. Here are some good practices for managing implicits:

- Separate implicits to their own trait, so the user must explicitly extend that trait to access the implicits.
- It is often useful to provide a companion object that also contains the implicits. This companion object can be imported in a limited scope to get around ambiguities or to make code clearer.
- Use a consistent naming convention for the classes you convert to. A common convention is to suffix the class with `W`, meaning wide. For example, if you extend `Int` with new methods you might put those methods in a class `IntW`.
