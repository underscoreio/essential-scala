---
layout: page
title: Object Literals
---

So far we've seen how to create objects of built-in types like `Int` and `String` and combine them into expressions. More useful programs will require us to create objects tailored to the problem we're solving. In fact, creating the right objects could be seen as the goal of programming in Scala. We will start by seeing how to write object literals.

We can define an empty object as follows.

~~~scala
scala> object test {}
defined module test
~~~

This program is not an *expression* because it doesn't evaluate to a value. Rather it is a "statement" or "definition" that binds a name (`test`) to a value (an empty object). Notice that the Scala REPL tells us it has defined a **module**. We'll see what this means later.

Once we have defined `test`, we can use it in simple expressions.

~~~scala
scala> test
res0: test.type = test$@1668bd43
~~~

This expression is equivalent to writing a literal like `123` or `"abc"`.
Note that the type of the object is reported as `test.type`. This is not like any type we've seen before -- it's a new type, created just for our object, called a **singleton type**.

### Methods

We interact with objects via methods, so let's create an object with a method.

~~~scala
scala> object test2 {
     |   def name: String = "Probably the best object ever"
     | }
defined module test2
~~~

Here we've create a method called `name`. We can call it in the usual way.

~~~scala
scala> test2.name
res3: String = Probably the best object ever
~~~

Here's an object with a more complex method definition.

~~~scala
scala> object test3 {
     |   def hello(name: String) =
     |     "Hello " + name
     | }
defined module test3

scala> test3.hello("Noel")
res7: String = Hello Noel
~~~

From these examples we can see most of the important bits of method definitions:

  * Methoddefintions start with the `def` keyword, followed by a name and an optional list of parameters.

  * We must declare the types of any parameters using the syntax `name: type`;

  * We can optionally declare the return type of a method. If we don't declare a return type Scala will infer one for us.

  * The declaration is followed by an `=` sign and a body expression.

  * The return value of the method is determined by evaluating the body.

If you remember from the previous section, one type of expression is a *block*. We can use blocks to write multi-line methods with side-effects. Because the block evluates to the value of its last expression, there is no need to write `return`.

~~~scala
scala> object test4 {
     |   def hello(name: String) = {
     |     println("Running the 'hello' method!")
     |     "Hello " + name
     |   }
     | }
defined module test4

scala> test4.hello("Dave")
Running the 'hello' method!
res6: String = Hello Dave
~~~

### Fields

An object can also contain other objects, called **fields**. We introduce these using the `val` keyword, which looks similar to `def`.

~~~scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

object test5 {
  val name = "Noel"
  def hello(other: String) =
    name + " says hi to " + other
}
^D

// Exiting paste mode, now interpreting.

defined module test5

scala>
scala> test5.hello("Dave")
res8: String = Noel says hi to Dave
~~~

Here are the important parts:

  * Field definitions start with the `val` keyword, followed by a name and an optional type.

  * We can't have parameters!

  * The declaration is followed by an `=` sign and a body expression.

  * The value of the field is determined by evaluating the body.

As with a method, we can use a block to calculate the value of a field over several lines of code.

~~~scala
scala> object test6 {
     |   val name = {
     |     val title = "Dr"
     |     val theDoctor = title + " Who"
     |     theDoctor
     |   }
     | }
defined module test6

scala> test6.name
res9: String = "Dr Who"
~~~

### Methods versus Fields

You might wonder why we need fields when we can have methods of no arguments that seem to work the same. The difference is that a field gives a name to a value, whereas a method gives a name to a computation that produces a value.

Here's an object that shows the difference.

~~~scala
scala> object test7 {
     |   val simpleField = {
     |     println("Evaluating simpleField")
     |     42
     |   }
     |   def noArgMethod = {
     |     println("Evaluating noArgMethod")
     |     42
     |   }
     | }
defined module test7
~~~

Notice how the REPL says we've defined a module, but it hasn't run either of our `println` statements? This is due to a quirk of Scala and Java called *lazy loading*.

`objects` and `classes` (which we'll see later) aren't loaded until they are referenced by other code. This is what prevents Scala loading the entire standard library into memory to run a simple `"Hello world!"` app.

Let's force Scala to evaluate our object body by referencing `test7` in an expression.

~~~scala
scala> test7
Evaluating simpleField
res7: test7.type = test7$@b22e8c9
~~~

When the object is evaluated, Scala runs through and calculates the values of each of its fields. This results in the code printing `"Evaluating simpleField"` as a side-effect.

**The body expression of a field is run only once** after which the final value is stored in the object. The expression is never evaluated again -- notice the lack of println output below.

~~~scala
scala> test7.simpleField
res8: Int = 42

scala> test7.simpleField
res9: Int = 42
~~~

The body of a method, on the other hand, is evaluated again and again every time we invoke the method -- notice the repreated println output below.

~~~scala
scala> test7.noArgMethod
Evaluating noArgMethod
res11: Int = 42

scala> test7.noArgMethod
Evaluating noArgMethod
res12: Int = 42
~~~

### Exercises

#### Square dance!

Define an object called `calc` with a method called `square` that accepts a `Double` as an argument and... you guessed it... squares its input. Add a method called `cube` that cubes its input *and calls `square` as part of its body*.

<div class="solution">
Here is the solution. `cube(x)` calls `square(x)` and multiplies its value by `x` one more time to produce its output. The return type of each method is inferred by the compiler as `Double`.

~~~scala
object calc {
  def square(x: Double) = x * x
  def cube(x: Double) = x * square(x)
}
~~~
</div>

#### Precise square dance!

Copy and paste `calc` from the previous exercise to create a `calc2` that is generalized to work with `Ints` as well as `Doubles`. If you have Java experience, this should be fairly straightforward. If not, read the solution below.

<div class="solution">
Like Java, Scala can't generalize particularly well across `Ints` and `Doubles`. However, it will allow us to *"overload"* the `square` and `cube` methods by defining them for each type of parameter.

~~~scala
object calc {
  def square(value: Double) = value * value
  def cube(value: Double) = value * square(value)

  def square(value: Int) = value * value
  def cube(value: Int) = value * square(value)
}
~~~

"Overloaded" methods are ones we have defined several times for different argument types. Whenever we call an overloaded method type, Scala automatically determines which variant we need by looking at the type of the argument.

~~~scala
calc2.square(1.0) // calls the `Double` version of `square`
calc2.square(1)   // calls the `Int` version `square`
~~~

The Scala compiler is able to insert automatic conversions between numeric types wherever you have a lower precision and require a higher precision. For example, if you write `calc.square(2)`, the compiler determines that the only version of `calc.square` takes a `Double` and automatically infers that you really mean `calc.square(2.toDouble)`.

Conversions in the opposite direction, from high precision to low precision, are not handled automatically because they can lead to rounding errors. For example, the code below will not compile because `x` is an `Int` and its body expression is a `Double` (try it and see)!

~~~scala
val x: Int = calc.square(2) // compile error
~~~

You can manually use the `toInt` method of `Double` to work around this:

~~~scala
val x: Int = calc.square(2).toInt // toInt rounds down
~~~

<div class="alert alert-info">
**Java tip:** To main similar behaviour to Java, Scala also automatically converts any object to a `String` where required. This is to make it easy to write things like `println("a" + 1)`, which Scala automatically rewrites as `println("a" + 1.toString)`.

The fact that string concatenation and numeric addition share the same `+` method can sometimes cause unexpected bugs, so watch out!
</div>
</div>

#### Ordered evaluation (optional)

When entered on the REPL, what does the following program output, and what is the type and value of the final expression? Think carefully about the types, dependencies, and evaluation behaviour of each field and method.

~~~scala
object argh {
  def a = {
    println("a")
    1
  }

  val b = {
    println("b")
    a + 2
  }

  def c = {
    println("c")
    a
    b + "c"
  }
}

argh.c + argh.b + argh.a
~~~

<div class="solution">
Here is the solution:

~~~scala
b
a
c
a
a
res1: String = 3c31
~~~

The full sequence of evaluation is as follows:

 1. We calculate the main sum at the end of the program, which...
    1. Loads `argh`, which...
       1. Calculates all the fields in `argh`, which...
          1. Calculates `b`, which...
             1. Prints `"b"`
             1. Evaluates `a + 2`, which...
                1. Calls `a`, which...
                   1. Prints `"a"`
                   1. Returns `1`
                1. Returns `1 + 2`
             1. Stores the value `3` in `b`
    1. Calls `argh.c`, which...
       1. Prints `"c"`
       1. Evaluates `b + "c"`, which...
          1. Retrieves the value `3` from `b`
          1. Retrieves the value `"c"`
          1. Evaluates the `+`, determining that it actually refers to string
             concatenation and converting `3` to `"3"`
          1. Returns the `String` `"3c"`
    1. Calls `argh.b`, which...
       1. Retrieves the value `3` from `b`
    1. Evaluates the first `+`, determining that it actually refers to string
       concatentation, and yielding `"3c3"`
    1. Calls `argh.a`, which...
       1. Prints `"a"`
       1. Returns `1`
    1. Evaluates the first `+`, determining that it actually refers to string
       concatentation, and yielding `"3c31"`
</div>

#### Greetings, Humans

Define an object called `human` that contains fields called `firstName` and `lastName`. Define a second object called `alien` containing a method called `greet` that takes your human as a parameter and `printlns` a greeting using their `firstName`.

What is the type of the `greet` method? Can we use this method to greet other objects?

<div class="solution">
~~~scala
object human {
  val firstName = "Dave"
  val lastName = "Gurnell"
}

object alien {
  def greet(h: human.type) =
    println("Greetings, " + h.firstName)
}
~~~

Notice the type on the `h` parameter of `greet`: `human.type`. This is one of the *singleton types* we were referring to earlier - it is specific to the object `human`, which prevents us using `greet` on any other object. This is very different from a type such as `Int` that is shared by all Scala integers.

This imposes a significant limitation on our ability to write programs in Scala. We can only write methods that work with built-in types or single objects of our own creation. In order to build useful programs we need the ability to *define our own types* and create multiple values of each. We can do this using `classes`, which we will cover in the next section.
</div>

#### The value of methods

Are methods values? Are they expressions? Why might this be the case?

<div class="solution">
First let's deal with the equivalence between methods and expressions. As we know, expressions and computations that produce values. A simple test of whether something is an expression is to see if we can assign it to a field.

~~~scala
scala> object calculator {
     |   def square(x: Int) = x * x
     | }
defined module calculator

scala> val someField = calculator.square
<console>:8: error: missing arguments for method square in object calculator;
follow this method with `_' if you want to treat it as a partially applied function
       val someField = calculator.square
                                  ^
~~~

Although we don't understand this error message fully yet (we shall learn about "partially applied functions" later), it does show us that `square` **is not an expression**. However, a *call* to `square` *does* yield a value:

~~~scala
scala> val someField = calculator.square(2)
someField: Int = 4
~~~

A method with no arguments looks like it behaves differently. However, this is a trick of the syntax.

~~~scala
scala> object clock {
     |   def time = System.currentTimeMillis
     | }
defined module clock

scala> val now = clock.time
now: Long = 1395402828639
~~~

Although it looks like `now` is being assigned `clock.time` as a value, it is actually being assigned the *value returned by calling `clock.time`*. We can demonstrate this by calling the method again:

~~~scala
scala> val aBitLaterThanNow = clock.time
aBitLaterThanNow: Long = 1395403220551
~~~

As we saw above, references to fields and calls to argumentless methods look identical in Scala. This is by design, to allow us to swap the implementation of a field for a method (and vice versa) without affecting other code.

So, in summary, **calls to methods are expressions** but **methods themselves are not expressions**. In addition to methods, Scala also has a concept called **functions**, which are objects that can be invoked like methods. As we know objects are values, so functions are also values and can be treated as data. As you may have guessed, functions are a critical part of *functional programming*, which is one of Scala's major strengths. We will learn about functions and functional programming in a bit.
</div>
