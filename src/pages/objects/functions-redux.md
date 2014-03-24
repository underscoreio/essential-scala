---
layout: page
title: Functions Redux
---

Way back, in the final exercise in the [Classes](classes.html) section, we defined a class called `Adder`. Here's a recap.

~~~ scala
class Adder(amount: Int) {
  def add(in: Int) = in + amount
}
~~~

In the discussion we described `Adders` as representing the computation of adding an `amount` to a number. It was like a method that is also a value.

This is such a powerful concept, that Scala has a fully blown set of language features that allow us to create objects that also behave like computations. These comutational objects are called *functions*, and are the basis of **functional programming**.

Finally, folks, we made it. Take a deep breath and let's start functional programming!

### The apply method

In Scala, a function is simply an object with a special method called `apply`. Using the method name `apply` in an object `foo` affords us a special syntax for `foo.apply(args)`: `foo(args)`.

For example, if we rename the `add` method to `apply` we can use it as follows:

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

With this one neat trick objects can become functions. There are lots of things that we can now do with functions that we couldn't do with methods: assign them to variables, pass them as arguments, and return them. Functions are **first class values**, which means we can do anything with them that we can with other values.

### Function literals

Scala takes this trick further by giving us a short-hand *function literal syntax* specifically for creating new functions. Here is a function that adds one to an `Int`:

~~~ scala
scala> (x: Int) => x + 1
res3: Int => Int = <function1>

scala> res3(10)
res4: Int = 11
~~~

Notice the type: `Int => Int`. This means a function that takes an `Int` as a parameter and returns an `Int`. This extends naturally to functions of more than one argument:

~~~ scala
scala> (x: Int, y:Int) => x + y
res5: (Int, Int) => Int = <function2>

scala> res5(10, 20)
res6: Int = 30
~~~

### Built-in function types

You may be wondering why the type and values of our revised `Adder` don't print the same as these functions. The answer is subtle: by naming our method `apply`, we gain acces to the short-hand syntax for function application. However, our `Adder` is still of type `Adder`, and it still has the same `toString` method as before..

Scala also defines standard abstract classes[^actuallytraits] for functions of various arities, from `Function0` for functions of no parameters upwards to `Function22` for functions of 22 parameters[^whytwentytwo]. The notations `Int => Int` and `(Int, Int) => Int` are syntactic sugar for `Function1[Int, Int]` and `Function2[Int, Int, Int]` respectively.

These built-in function types allow us to write code that relies on a general function rather than something library-specific like `Adder`. For example:

~~~ scala
scala> class Counter(val count: Int = 0) {
     |   def adjust(func: Int => Int) = new Counter(func(count))
     | }
defined class Counter

scala> new Counter(2).adjust((x: Int) => x + 3).count
res9: Int = 5
~~~

We no longer need our `Adder` class -- we can pass any function to `Counter` to change its value in any arbitrary manner.

[^actuallytraits]: Actually, technically `Function1` through `Function22` are *traits* - we'll be introduced to these in the next couple of sections.

[^whytwentytwo]: Why 22? Honestly, there's no specific reason. The creators of Scala had to stop somewhere, and... oddly... they chose to stop at 22.

### Converting methods to functions

Finally, Scala gives us syntax to convert a methods into functions, by following a method name with an underscore.

~~~ scala
scala> object Sum {
     |   def sum(x: Int, y: Int) = x + y
     | }
defined module Sum

scala> Sum.sum
<console>:9: error: missing arguments for method sum in object Sum;
follow this method with `_' if you want to treat it as a partially applied function
              Sum.sum
                  ^

scala> (Sum.sum _)
res11: (Int, Int) => Int = <function2>
~~~

Finally, we understand that cryptic error message about underscores and partially applied functions.

### Exercises

#### Functional Counters

Take the definition of `Counter` from above and rewrite `inc` and `dec` in terms of `adjust`.

<div class="solution">
~~~ scala
class Counter(val count: Int) {
  def dec = adjust((count: Int) => count + 1)
  def inc = adjust((count: Int) => count + 1)
  def adjust(func: Int => Int) = new Counter(func(count))
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
