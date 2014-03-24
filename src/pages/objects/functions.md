---
layout: page
title: Objects as Functions
---

In the final exercise of the previous section, we defined a class called `Adder`:

~~~ scala
class Adder(amount: Int) {
  def add(in: Int) = in + amount
}
~~~

In the discussion we described `Adders` as representing the computation of adding an `amount` to a number. This is effectively like having a method that we can pass around as a value.

This is such a powerful concept, that Scala has a fully blown set of language features that allow us to create objects that also behave like computations. These computational objects are called *functions*, and are the basis of **functional programming**.

### The apply method

Scala has numerous language features for supporting functional programming. For now we are going to look at just one, called **apply syntax**. The other features will come later this chapter.

In Scala, by convention, an object can by "called" like a function if it has a method called `apply`. Naming a method `apply` affords us a special shortened call syntax: `foo.apply(args)` becomes `foo(args)`.

For example, if we rename the `add` method in our `Adder` to `apply` we can use it as follows:

~~~ scala
scala> class Adder(amount: Int) {
     |   def apply(in: Int) = in + amount
     | }
defined class Adder

scala> val add3 = new Adder(3)
add3: Adder = Adder@1d4f0fb4

scala> add3(2)
res7: Int = 5
~~~

With this one neat trick objects can look like functions. There are lots of things that we can now do now that we couldn't do with methods, including assign them to variables and pass them as arguments. Functions are **first class values**, which means we can do anything with them that we can with other objects.
