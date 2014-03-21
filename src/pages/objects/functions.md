---
layout: page
title: Objects and Functions
---

In the final exercise in the [Classes](classes.html) section we defined a class called `Adder`. Here's a recap.

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

[^whytwentytwo]: Why 22? Honestly, there's no specific reason. The creators of Scala had to stop somewhere, and they chose to stop at 22.

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

The **case class** is another trick in Scala that automates much of we've just discussed. If you write

~~~ scala
case class Person(val firstName: String, val lastName: String) {
  def name = firstName + " " + lastName
}
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

Here we see the use of **keyword arguments** in Scala. Any method in Scala can be called with keyword arguments. The names of the keywords are simply the names of the parameters in the method definition.

The `copy` method also illustrates the use of **optional arguments**. Optional arguments allow us to omit passing a particular argument and have a default substituted in. For the example above we don't pass any value for `lastName` and the default is the existing value of the object.

Defining default arguments for a method is quite simple. The syntax looks like this:

~~~ scala
scala> def defaultExample(a: Int = 1, b: Int = 2, c: Int = 3) =
  a + b + c
defaultExample: (a: Int, b: Int, c: Int)Int
~~~

And here is it in use

~~~ scala
// c takes the default value of 3
scala> defaultExample(2, 3)
defaultExample(2, 3)
res0: Int = 8

// b and c take the default values
scala> defaultExample(4)
defaultExample(4)
res1: Int = 9

// We use keyword arguments and b takes a default value
scala> defaultExample(a = 4, c = 4)
defaultExample(a = 4, c = 4)
res2: Int = 10
~~~

Using keyword arguments with the `copy` method is a good idea as it ensures our code will still work if we add fields or rearrange fields in the case class, and it will cause a compilation error if we remove that field. The same is not true if we used normal positional arguments.

There are other benefits to using case classes which we will encounter shortly.

A final note. If you find yourself defining a case class with no constructor arguments you can instead a define a **case object**. A case object is defined just like a case class and has the same default methods as a case class.

~~~ scala
case object Citizen {
  def firstName = "John"
  def lastName  = "Doe"
  def name = firstName + " " + lastName
}
~~~

## Exercises

#### Case Class Counter

Reimplement `Counter` as a case class, using `copy` where appropriate. Additionally initialise `count` to a default value of 0.

<div class="solution">
~~~ scala
case class Counter(val count: Int = 0) {
  def dec = copy(count = count - 1)
  def inc = copy(count = count + 1)
  def map(f: Int => Int) =
    copy(count = f(count))
}
~~~
</div>
