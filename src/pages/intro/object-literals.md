---
layout: page
title: Object Literals
---

So far we've seen how to create objects of built-in types like `Int` and `String` and combine them into expressions. In this section we will see how to create objects of our own choosing using **object literals**.

When we write an object literal we use a **declaration**, which is a different kind of program to an expression. A declaration does not evaluate to a value. Instead is gives a name to a value. This name can then be used to refer to the value in other code.

We can declare an empty object as follows:

~~~ scala
scala> object Test {}
defined module Test
~~~

This is not an expression -- it does not evaluate to a value. Rather, it binds a name (`Test`) to a value (an empty object). Notice that Scala tells us it has defined a **module**. We'll see what this means later.

Once we have bound the name `Test` we can use it in simple expressions, where it evaluates to the object we have declared. The simplest expression is just the name on its own, which evaluates to the value itself:

~~~ scala
scala> Test
res0: Test.type = Test$@1668bd43
~~~

This expression is equivalent to writing a literal like `123` or `"abc"`.
Note that the type of the object is reported as `Test.type`. This is not like any type we've seen before -- it's a new type, created just for our object, called a **singleton type**. We cannot create other values of this type.

Empty objects are not so useful. Within the body (between the braces) of an object declaration we can put expressions. It is more common, however, to put declarations such as declaring methods, fields, or even more objects.

<div class="callout callout-info">

#### Object Declaration Syntax

The syntax for declaring an object is

~~~ scala
object name {
  declarationOrExpression ...
}
~~~

where

- `name` is the name of the object; and
- the optional `declarationOrExpression`s are declarations or expressions.

</div>

Let's see how to declare methods and fields.

## Methods

We interact with objects via methods so let's create an object with a method.

~~~ scala
scala> object Test2 {
         def name: String = "Probably the best object ever"
       }
defined module Test2
~~~

Here we've create a method called `name`. We can call it in the usual way.

~~~ scala
scala> Test2.name
res3: String = Probably the best object ever
~~~

Here's an object with a more complex method:

~~~ scala
scala> object Test3 {
         def hello(name: String) =
           "Hello " + name
       }
defined module Test3

scala> Test3.hello("Noel")
res7: String = Hello Noel
~~~

<div class="callout callout-info">
#### Method Declaration Syntax

The syntax for declaring a method is

~~~ scala
def name(parameter: type, ...): resultType =
  bodyExpression
~~~

or

~~~ scala
def name: resultType =
  bodyExpression
~~~

where

- `name` is the name of the method;
- the optional `parameter`s are the names given to parameters to the method;
- the `type`s are the types of the method parameters;
- the optional `resultType` is the type of the result of the method;
- the `bodyExpression` is an expression that calling the method evaluates to.

Method parameters are optional, but if a method has parameters their type must be given. Although the result type is optional it is good practice to define it as it serves as (machine checked!) documentation.
</div>

<div class="java-tip">
The return value of the method is determined by evaluating the body -- there is no need to write `return`.
</div>


## Fields

An object can also contain other objects, called **fields**. We introduce these using the keywords `val` or `var`, which look similar to `def`:

~~~ scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

object Test4 {
  val name = "Noel"
  def hello(other: String) =
    name + " says hi to " + other
}
^D

// Exiting paste mode, now interpreting.

defined module Test4

scala> Test4.hello("Dave")
res8: String = Noel says hi to Dave
~~~

<div class="callout callout-info">
#### Field Declaration Syntax

The syntax for declaring a field is

~~~ scala
val name: type = valueExpression
~~~

or

~~~ scala
var name: type = valueExpression
~~~

where

- `name` is the name of the field;
- the optional `type` declaration gives the type of the field;
- the `valueExpression` evaluates to the object that is bound to the `name`.
</div>

Using `val` defines an *immutable* field, meaning we cannot change the value bound to the name. A `var` field is *mutable*, allowing us to change the bound value.

**Always prefer `val` to `var`.** Scala programmers prefer to use immutable fields wherever possible. While you will no doubt create the occassional mutable field in your application code, we will stay away from `var` for most of this course and you should do the same in your Scala programming.


## Methods versus fields

You might wonder why we need fields when we can have methods of no arguments that seem to work the same. The difference is subtle -- a field gives a name to a value, whereas a method gives a name to a computation that produces a value.

Here's an object that shows the difference:

~~~ scala
scala> object Test7 {
         val simpleField = {
           println("Evaluating simpleField")
           42
         }
         def noArgMethod = {
           println("Evaluating noArgMethod")
           42
         }
       }
defined module Test7
~~~

Here we have used a `println` expression to print something to the console, and a block expression (expressions surrounded by `{` and `}`) to group expressions. We'll see more about block expressions in the next section.

Notice how the REPL says we've defined a module, but it hasn't run either of our `println` statements? This is due to a quirk of Scala and Java called *lazy loading*.

`objects` and `classes` (which we'll see later) aren't loaded until they are referenced by other code. This is what prevents Scala loading the entire standard library into memory to run a simple `"Hello world!"` app.

Let's force Scala to evaluate our object body by referencing `Test7` in an expression:

~~~ scala
scala> Test7
Evaluating simpleField
res7: Test7.type = Test7$@b22e8c9
~~~

When the object is first loaded, Scala runs through its definition and calculates the values of each of its fields. This results in the code printing `"Evaluating simpleField"` as a side-effect.

**The body expression of a field is run only once** after which the final value is stored in the object. The expression is never evaluated again -- notice the lack of `println` output below.

~~~ scala
scala> Test7.simpleField
res8: Int = 42

scala> Test7.simpleField
res9: Int = 42
~~~

The body of a method, on the other hand, is evaluated again and again every time we call the method -- notice the repreated println output below.

~~~ scala
scala> Test7.noArgMethod
Evaluating noArgMethod
res11: Int = 42

scala> Test7.noArgMethod
Evaluating noArgMethod
res12: Int = 42
~~~

## Take home points

In this section we have created our own objects, given them methods and fields, and referenced them in expressions.

We have seen the syntax for declaring objects

~~~ scala
object name {
  declarationOrExpression ...
}
~~~

for declaring methods

~~~ scala
def name(parameter: type, ...): resultType = bodyExpression
~~~

and for declaring fields

~~~ scala
val name = valueExpression
var name = valueExpression
~~~

All of these are **declarations**, binding names to values. Declarations are different to expressions. They do not evaluate to a value and do not have a type.

We have also seen the difference between methods and fields -- fields refer to values stored within an object, whereas methods refer to computations that produce values.

## Exercises

### Cat-o-matique

The table below shows the names, colour, and favourite foods of three cats. Define an object for each cat. (For experienced programmers: we haven't covered classes yet.)

|-----------+-----------------+-------|
| Name      | Colour          | Food  |
|-----------+-----------------+-------|
| Oswald    | Black           | Milk  |
| Henderson | Ginger          | Chips |
| Quentin   | Tabby and white | Curry |
|=====================================|
{: .table .table-bordered .table-responsive }

<div class="solution">

This is just a finger exercise to get you used to the syntax of defining objects. You should have a solution similar to the code below.

~~~ scala
object Oswald {
  val colour: String = "Black"
  val food: String = "Milk"
}

object Henderson {
  val colour: String = "Ginger"
  val food: String = "Chips"
}

object Quentin {
  val colour: String = "Tabby and white"
  val food: String = "Curry"
}
~~~

</div>


### Square dance!

Define an object called `calc` with a method `square` that accepts a `Double` as an argument and... you guessed it... squares its input. Add a method called `cube` that cubes its input *calling `square`* as part of its result calculation.

<div class="solution">
Here is the solution. `cube(x)` calls `square(x)` and multiplies its value by `x` one more time. The return type of each method is inferred by the compiler as `Double`.

~~~ scala
object calc {
  def square(x: Double) = x * x
  def cube(x: Double) = x * square(x)
}
~~~
</div>

### Precise square dance!

Copy and paste `calc` from the previous exercise to create a `calc2` that is generalized to work with `Ints` as well as `Doubles`. If you have Java experience, this should be fairly straightforward. If not, read the solution below.

<div class="solution">
Like Java, Scala can't generalize particularly well across `Ints` and `Doubles`. However, it will allow us to *"overload"* the `square` and `cube` methods by defining them for each type of parameter.

~~~ scala
object calc2 {
  def square(value: Double) = value * value
  def cube(value: Double) = value * square(value)

  def square(value: Int) = value * value
  def cube(value: Int) = value * square(value)
}
~~~

"Overloaded" methods are ones we have defined several times for different argument types. Whenever we call an overloaded method type, Scala automatically determines which variant we need by looking at the type of the argument.

~~~ scala
calc2.square(1.0) // calls the `Double` version of `square`
calc2.square(1)   // calls the `Int` version `square`
~~~

The Scala compiler is able to insert automatic conversions between numeric types wherever you have a lower precision and require a higher precision. For example, if you write `calc.square(2)`, the compiler determines that the only version of `calc.square` takes a `Double` and automatically infers that you really mean `calc.square(2.toDouble)`.

Conversions in the opposite direction, from high precision to low precision, are not handled automatically because they can lead to rounding errors. For example, the code below will not compile because `x` is an `Int` and its body expression is a `Double` (try it and see)!

~~~ scala
val x: Int = calc.square(2) // compile error
~~~

You can manually use the `toInt` method of `Double` to work around this:

~~~ scala
val x: Int = calc.square(2).toInt // toInt rounds down
~~~

<div class="alert alert-info">
**Java tip:** To maintain similar behaviour to Java, Scala also automatically converts any object to a `String` where required. This is to make it easy to write things like `println("a" + 1)`, which Scala automatically rewrites as `println("a" + 1.toString)`.

The fact that string concatenation and numeric addition share the same `+` method can sometimes cause unexpected bugs, so watch out!
</div>
</div>

### Order of evaluation

When entered on the REPL, what does the following program output, and what is the type and value of the final expression? Think carefully about the types, dependencies, and evaluation behaviour of each field and method.

~~~ scala
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

~~~ scala
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
       1. Evalutates `a`
         1. Prints `"a"`
         1. Returns `1` - Which we discard
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

### Greetings, humans

Define an object called `person` that contains fields called `firstName` and `lastName`. Define a second object called `alien` containing a method called `greet` that takes your person as a parameter and returns a greeting using their `firstName`.

What is the type of the `greet` method? Can we use this method to greet other objects?

<div class="solution">
~~~ scala
object person {
  val firstName = "Dave"
  val lastName = "Gurnell"
}

object alien {
  def greet(p: person.type) =
    "Greetings, " + p.firstName + " " + p.lastName
}

alien.greet(person)
~~~

Notice the type on tpe `h` parameter of `greet`: `person.type`. This is one of the *singleton types* we were referring to earlier - it is specific to the object `person`, which prevents us using `greet` on any other object. This is very different from a type such as `Int` that is shared by all Scala integers.

This imposes a significant limitation on our ability to write programs in Scala. We can only write methods that work with built-in types or single objects of our own creation. In order to build useful programs we need the ability to *define our own types* and create multiple values of each. We can do this using `classes`, which we will cover in the next section.
</div>

### The value of methods

Are methods values? Are they expressions? Why might this be the case?

<div class="solution">
First let's deal with the equivalence between methods and expressions. As we know, expressions are computations that produce values. A simple test of whether something is an expression is to see if we can assign it to a field.

~~~ scala
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

~~~ scala
scala> val someField = calculator.square(2)
someField: Int = 4
~~~

A method with no arguments looks like it behaves differently. However, this is a trick of the syntax.

~~~ scala
scala> object clock {
     |   def time = System.currentTimeMillis
     | }
defined module clock

scala> val now = clock.time
now: Long = 1395402828639
~~~

Although it looks like `now` is being assigned `clock.time` as a value, it is actually being assigned the *value returned by calling `clock.time`*. We can demonstrate this by calling the method again:

~~~ scala
scala> val aBitLaterThanNow = clock.time
aBitLaterThanNow: Long = 1395403220551
~~~

As we saw above, references to fields and calls to argumentless methods look identical in Scala. This is by design, to allow us to swap the implementation of a field for a method (and vice versa) without affecting other code. It is a programming language feature called the *[uniform access principle]*.

[uniform access principle]: http://en.wikipedia.org/wiki/Uniform_access_principle

So, in summary, **calls to methods are expressions** but **methods themselves are not expressions**. In addition to methods, Scala also has a concept called **functions**, which are objects that can be invoked like methods. As we know objects are values, so functions are also values and can be treated as data. As you may have guessed, functions are a critical part of *functional programming*, which is one of Scala's major strengths. We will learn about functions and functional programming in a bit.
</div>
