---
layout: page
title: Interacting with Objects
---

In the last section we learnt about the basic data types in Scala and learnt how to write simple literals. In this section we will start to combine literals using method calls and expressions.

### Methods

Important point: **every value in Scala is an object**. This means we can interact with any value by calling or invoking *methods* on it. Method calls are *expressions* and thus they return values and have types.

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

### Operators

Because every value in Scala is an object, we can also call methods on primitive types such as `Int` and `Boolean`. This is in contrast to Java where `int` and `boolean` are not objects:

~~~ scala
scala> 123.toShort
res1: Short = 123

scala> 123.toByte
res2: Byte = 123
~~~

But if an `Int` is an object, what are the basic methematical operators such as `+` and `-`? Are they also methods? Yes!

~~~ scala
scala> 43 - 3 + 2
res18: Int = 42

scala> 43 .-(3).+(2)
res20: Int = 42
~~~

(Note the space after `43` in the second example, which prevents it being interpreted as a `Double`.)

As a general rule, any Scala expression you can write as `a.b(c)` you can also write as `a b c` and vice-versa. This is known as **infix operator notation**. Note that `a b c d` is equivalent to `a.b(c).d`, not `a.b(c, d)`. You can use infix operator notation with methods that take one argument.

Infix notation is one of several conventions that allow us to write simple operator expressions that are implemented in terms of objects and method calls.There are also notations for **prefix, postfix, and right-associative operators**, and a set of **precedence rules** that follow our intuitive understanding from mathematics and logic.

~~~ scala
scala> 2 * 3 + 4 * 5
res0: Int = 26

scala> (2 * 3) + (4 * 5)
res1: Int = 26
~~~

### Conditionals

Conditionals are an essential part of any programming language. Scala's `if` statement has the same syntax as Java's. One important difference is that **Scala's conditional is an expression**: it has a type and returns a value.

~~~ scala
scala> if(true) {
         42
       } else {
         40
       }
res45: Int = 42
~~~

You can drop the braces around the true or false arm if it contains a single expression, and even write a conditional on one line.

~~~ scala
scala> if(true) 42 else 40
res47: Int = 42
~~~

### Blocks

*Blocks* are another type of expression that allow us to sequence computations together. They are written as a pair of braces containing sub-expressions separated by semicolons or newlines.

~~~ scala
scala> { 1; 2; 3 }
<console>:8: warning: a pure expression does nothing in statement position; you may be omitting necessary parentheses
              { 1; 2; 3 }
                ^
<console>:8: warning: a pure expression does nothing in statement position; you may be omitting necessary parentheses
              { 1; 2; 3 }

res0: Int = 3
~~~

As you can see, executing this code causes the REPL to raise a number of warnings and return the `Int` value `3`.

Like in Java, a block is a sequence of expressions surrounded by braces. Unlike Java, however, a Scala block is an expression: it executes each of its sub-expressions in order and returns the value of the last expression.

What's the point of this? Why execute `1` and `2` if we're going to throw their values away? This is a good question, and is the reason the Scala compiler raised those warnings. The main reason to use a block is to use code that produces side-effects before calculating a final value:

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

### Take home points

All Scala values are objects. This means **we can call methods on any value** we choose, even an `Int` or another primitive value.

Because all values are objects, it makes sense to implement all operations on values as method calls. **Scala has no operators - everything is a method call.** We use syntactic conventions like infix operator notation to keep our code simple and readable, but we can always fall back to standard method notation where it makes sense.

**Expressions are fragments of code that have a type and evaluate to a value.** All method calls are expressions, even calls to methods that return `Unit`. Many of Scala's syntactic constructs are also expressions. This includes conditionals and blocks, which are *statements* in many other languages (i.e. they do not have types or values).

As we will see in the next section, Scala's focus on programming with expressions allows us to write much shorter code than we can in Java. It also allows us to reason about code in a very intuitive way using values and types.

### Exercises

#### A classic rivalry

What is the type and value of the following conditional?

~~~ scala
val a = 1
val b = 2
if(a > b) "alien" else "predator"
~~~

<div class="solution">
  It's a `String` with value `"predator"`.

  The type is determined by the upper bound of the types in the *then* and *else* expressions. In this case both expressions are `Strings` so the result is also a `String`.

  The value is determined at runtime. `b` is greater than `a` so the conditional evaluates to the value of the *else* expression.
</div>

#### A less well known rivalry

What about this conditional?

~~~ scala
val a = 1
val b = 2
if(a > b) "alien" else 2001
~~~

<div class="solution">
It's a value of type `Any` with value `2001`.

The difference here is the type of the result. We saw earlier that the type is the *upper bound* of the positive and negative arms of the expression. `"alien"` and `2001` are completely different types - their closest common ancestor is `Any`, which is the grand supertype of all Scala types.

This is an important observation: types are determined at compile time, before the program is run. The compiler can't know which of `a` and `b` is greater before running the program, so it can only make a best guess at the type of the result of the conditional. `Any` is as close as it can get in this program, whereas in the previous exercise it can get all the way down to `String`.

We'll learn more about `Any` in the following sections. Java programmers shouldn't confuse it with `Object` because it subsumes value types like `Int` and `Boolean` as well.
</div>

#### An if without an else

What about this conditional?

~~~ scala
if(true) "hello"
~~~

<div class="solution">
It's a trick question! The value and type are `()` and `Unit` respectively.

Conditionals without `else` expressions only evaluate to a value half of the time. Scala works around this by simply not attempting to return a value.

Conditionals without `else` expressions are still useful for side-effects such as printing output.
</div>

#### Substitution

What is the difference between the following expressions? What are the similarities?

~~~ scala
1 + 2 + 3

6
~~~

<div class="solution">
The expressions have the same result type and return value. However, they arrive at their results in very different ways.

As neither expression has any side-effects, they are interchangeable from a user's point of view. Anywhere you can write `1 + 2 + 3` you can also write `6`. This is known as the **substitution model of evaluation**, although you may remember the principle from simplifying algebraic formulae at school.

As programmers, we must develop a mental model of how our code operates. In the absence of side-effects, the substitution model always works. If we know the types and values of each component of an expression, we know the type and value of the expression as a whole. In functional programming, we aim to avoid side-effects for this reason: it makes our programs very easy to reason about.
</div>
