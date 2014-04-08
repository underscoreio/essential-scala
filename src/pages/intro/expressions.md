---
layout: page
title: Interacting with Objects
---

In the last section we saw the basic data types in Scala and how to write simple literals. In this section we will start combining literals using method calls and expressions.

## Methods

Every value in Scala is an object. This means we can interact with any value by calling *methods* on it. Method calls are *expressions* and thus they return values and have types.

For example, we can get the uppercase version of a `String` by calling its `toUpperCase` method:

~~~ scala
scala> "hello".toUpperCase
res21: String = HELLO
~~~

We can chain method calls together to make more complex programs:

~~~ scala
scala> "hello".toUpperCase.toLowerCase
res22: String = hello
~~~

## Operators

Because every value in Scala is an object, we can also call methods on primitive types such as `Int` and `Boolean`. This is in contrast to Java where `int` and `boolean` are not objects:

~~~ scala
scala> 123.toShort // this is how we define a `Short` in Scala
res1: Short = 123

scala> 123.toByte // and this is how we define a `Byte`
res2: Byte = 123
~~~

But if an `Int` is an object, what are the basic methematical operators such as `+` and `-`? Are they also methods? Yes -- Scala methods can have symbolic names as well as alphanumeric ones!

~~~ scala
scala> 43 - 3 + 2
res18: Int = 42

scala> 43 .-(3).+(2)
res20: Int = 42
~~~

<small>Note the space after `43` in the second example, which prevents it being interpreted as a `Double`.</small>

As a general rule, any Scala expression written `a.b(c)` can also be written `a b c`. This is known as **infix operator notation**. Note that `a b c d e` is equivalent to `a.b(c).d(e)`, not `a.b(c, d, e)`. We can use infix operator notation with any method that takes one parameter, regardless of whether it has a symbolic or alphanumeric name:

~~~ scala
scala> "the quick brown fox" split " "
res0: Array[String] = Array(the, quick, brown, fox)
~~~

Infix notation is one of several syntactic shorthands that allow us to write simple operator expressions instead of verbose method calls. There are also notations for **prefix**, **postfix**, **right-associative**, and **assignment-style operators**.

A question poses itself -- what precedence rules should we associate with infix operators? Scala uses a set of [precedence rules] derived from the identifiers we use as method names that follow our intuitive understanding from mathematics and logic:

~~~ scala
scala> 2 * 3 + 4 * 5
res0: Int = 26

scala> (2 * 3) + (4 * 5)
res1: Int = 26
~~~

[precedence rules]: http://stackoverflow.com/questions/2922347/operator-precedence-in-scala

## Conditionals

Conditionals are an essential part of any programming language. Scala's `if` statement has the same syntax as Java's. One important difference is that **Scala's conditional is an expression** -- it has a type and returns a value.

~~~ scala
scala> if(true) 42 else 40
res47: Int = 42
~~~

## Blocks

Blocks are expressions that allow us to sequence computations together. They are written as a pair of braces containing sub-expressions separated by semicolons or newlines.

~~~ scala
scala> { 1; 2; 3 }
<console>:8: warning: a pure expression does nothing in statement position; you may be omitting necessary parentheses
              { 1; 2; 3 }
                ^
<console>:8: warning: a pure expression does nothing in statement position; you may be omitting necessary parentheses
              { 1; 2; 3 }

res0: Int = 3
~~~

<div class="alert alert-info">
**Side effects tip:** As you can see, executing this code causes the REPL to raise a number of warnings and return the `Int` value `3`.

A block is a sequence of expressions surrounded by braces. A block is also an expression: it executes each of its sub-expressions in order and returns the value of the last expression.

What's the point of this? Why execute `1` and `2` if we're going to throw their values away? This is a good question, and is the reason the Scala compiler raised those warnings above. The main reason to use a block is to use code that produces side-effects before calculating a final value:

~~~ scala
scala> {
     |   println("This is a side-effect")
     |   println("This is a side-effect as well")
     |   3
     | }
This is a side-effect
This is a side-effect as well
res1: Int = 3
~~~
</div>

## Variable definitions

We should mention how to create local variables for use in expressions. Here is a simple local definition:

~~~ scala
scala> val x = 1
x: Int = 1
~~~

Variable definitions and assignments have type `Unit` and return a value of `()`, even though they perform assignments as side-effects.

<div class="alert alert-info">
**Immutability tip:** "Variable definition" is perhaps a slight misnomer here. The `val` keyword actually defines an *immutable value* -- attempting to assign to it will give us a compile error:

~~~ scala
scala> x = 2
<console>:8: error: reassignment to val
       x = 2
         ^
~~~

We can use the `var` keyword to define a mutable variable that we can assign to:

~~~ scala
scala> var y = 1
y: Int = 1

scala> y = 2
y: Int = 2
~~~

As we will see, Scala programmers vastly prefer to deal with immutable variables and side-effect code wherever possible. We won't be using `var` in this course except in a couple of places.
</div>

## Take home points

All Scala values are objects. This means **we can call methods on any value** we choose, even an `Int` or another primitive value.

**Scala has very few operators - almost everything is a method call.** We use syntactic conventions like infix operator notation to keep our code simple and readable, but we can always fall back to standard method notation where it makes sense.

**Expressions are fragments of code that have a type and evaluate to a value.** All method calls are expressions, even calls to methods that return `Unit`. Many of Scala's syntactic constructs are also expressions. This includes conditionals and blocks, which are *statements* in many other languages (i.e. they do not have types or values).

Variable declarations and assignments are statements -- although they update the value of a location in memory, they have a result type of `Unit`.

As we will see, Scala's focus on programming with expressions allows us to write much shorter code than we can in Java. It also allows us to reason about code in a very intuitive way using values and types.

## Exercises

### A classic rivalry

What is the type and value of the following conditional?

~~~ scala
val a = 1
val b = 2
if(a > b) "alien" else "predator"
~~~

<div class="solution">
  It's a `String` with value `"predator"`. Predators are clearly best (although you can mofidy `a` and `b` to alter this statement).

  The type is determined by the upper bound of the types in the *then* and *else* expressions. In this case both expressions are `Strings` so the result is also a `String`.

  The value is determined at runtime. `b` is greater than `a` so the conditional evaluates to the value of the *else* expression.
</div>

### A less well known rivalry

What about this conditional?

~~~ scala
val a = 1
val b = 2
if(a > b) "alien" else 2001
~~~

<div class="solution">
It's a value of type `Any` with value `2001`.

This is similar to the previous exercise -- the difference is the type of the result. We saw earlier that the type is the *upper bound* of the positive and negative arms of the expression. `"alien"` and `2001` are completely different types - their closest common ancestor is `Any`, which is the grand supertype of all Scala types.

This is an important observation: types are determined at compile time, before the program is run. The compiler can't know which of `a` and `b` is greater before running the program, so it can only make a best guess at the type of the result of the conditional. `Any` is as close as it can get in this program, whereas in the previous exercise it can get all the way down to `String`.

We'll learn more about `Any` in the following sections. Java programmers shouldn't confuse it with `Object` because it subsumes value types like `Int` and `Boolean` as well.
</div>

### An if without an else

What about this conditional?

~~~ scala
if(true) "hello"
~~~

<div class="solution">
Trick question! The result type and value are `Uni` and `()` respectively.

All code being equal, conditionals without `else` expressions only evaluate to a value half of the time. Scala works around this by simply not attempting to return a value. We can still use these expressions for their side-effects though.
</div>

### Substitution

What is the difference between the following expressions? What are the similarities?

~~~ scala
1 + 2 + 3

6
~~~

<div class="solution">
The expressions have the same result type and return value. However, they arrive at their results in different ways.

As neither expression has any side-effects, they are interchangeable from a user's point of view. Anywhere you can write `1 + 2 + 3` you can also write `6`. This is known as the **substitution model of evaluation**, although you may remember the principle from simplifying algebraic formulae at school.

As programmers we must develop a mental model of how our code operates. In the absence of side-effects, the substitution model always works. If we know the types and values of each component of an expression, we know the type and value of the expression as a whole. In functional programming we aim to avoid side-effects for this reason: it makes our programs easy to reason about without having to look beyond the current block of code.
</div>
