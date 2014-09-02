---
layout: page
title: Compound Expressions
---

In the last section we saw the basic data types in Scala and how to write simple literals. In this section we will start combining literals using method calls.

## Methods

Every value in Scala is an object. We interact with objects by calling *methods* on them. Method calls are *expressions* and thus they have a type and evaluate to a value just like literals.

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

Some methods accept *parameters* or *arguments*, which control how the method works.

~~~ scala
scala> "abcdef".take(3)
"abcdefg12345".take(3)
res0: String = abc

scala> "abcdef".take(2)
"abcdefg12345".take(2)
res1: String = ab
~~~

Thus the syntax for a method call is

~~~ scala
anExpression.methodName(param1, param2, ...)
~~~

where `anExpression` is an expression that evaluates to the object on which we call the method, `methodName` is the name of the method we call, and the optional parameters ,`param1`, `param2`, and so on, are also expressions.

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


## Take home points

All Scala values are objects. We **interact with objects by calling methods** on them. If you come from a Java background note we can call methods on `Int` or any other primitive value.

The syntax for a method call is

~~~ scala
anExpression.methodName(parameter, ...)
~~~

or

~~~ scala
anExpression methodName parameter
~~~

**Scala has very few operators - almost everything is a method call.** We use syntactic conventions like infix operator notation to keep our code simple and readable, but we can always fall back to standard method notation where it makes sense.

**Expressions are fragments of code that have a type and evaluate to a value.** All method calls are expressions, even calls to methods that return `Unit`. Many of Scala's syntactic constructs are also expressions. This includes conditionals and blocks, which are *statements* in many other languages (i.e. they do not have types or values).

Variable declarations and assignments are statements -- although they update the value of a location in memory, they have a result type of `Unit`.

As we will see, Scala's focus on programming with expressions allows us to write much shorter code than we can in Java. It also allows us to reason about code in a very intuitive way using values and types.

## Exercises

### A classic rivalry

What is the type and value of the following conditional?

~~~ scala
if(1 > 2) "alien" else "predator"
~~~

<div class="solution">
  It's a `String` with value `"predator"`. Predators are clearly best.

  The type is determined by the upper bound of the types in the *then* and *else* expressions. In this case both expressions are `Strings` so the result is also a `String`.

  The value is determined at runtime. `2` is greater than `1` so the conditional evaluates to the value of the *else* expression.
</div>

### A less well known rivalry

What about this conditional?

~~~ scala
if(1 > 2) "alien" else 2001
~~~

<div class="solution">
It's a value of type `Any` with value `2001`.

This is similar to the previous exercise -- the difference is the type of the result. We saw earlier that the type is the *upper bound* of the positive and negative arms of the expression. `"alien"` and `2001` are completely different types - their closest common ancestor is `Any`, which is the grand supertype of all Scala types.

This is an important observation: types are determined at compile time, before the program is run. The compiler doesn't know which of `1` and `2` is greater before running the program, so it can only make a best guess at the type of the result of the conditional. `Any` is as close as it can get in this program, whereas in the previous exercise it can get all the way down to `String`.

We'll learn more about `Any` in the following sections. Java programmers shouldn't confuse it with `Object` because it subsumes value types like `Int` and `Boolean` as well.
</div>

### An if without an else

What about this conditional?

~~~ scala
if(false) "hello"
~~~

<div class="solution">
The result type and value are `Any` and `()` respectively.

All code being equal, conditionals without `else` expressions only evaluate to a value half of the time. Scala works around this by returning the `Unit` value if the `else` branch should be evaluted. We would usually only use these expressions for their side-effects.
</div>
