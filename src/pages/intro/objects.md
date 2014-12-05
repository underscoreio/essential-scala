## Interacting with Objects


In the previous section we saw the fundamental components of Scala programs: expressions, types, and values. We learned that **all values are objects**. In this section we will learn more about objects and how we can interact with them.

### Objects

An object is a grouping of data and operations on that data. For example, `2` is an object. The data is the integer 2, and the operations on that data are familiar operations like `+`, `-`, and so on.

We have some special terminology for the data and operations of an object. The operations are known as *methods*. The data is stored in *fields*.

### Method Calls

We interact with objects by *calling* methods[^patterns]. We have already seen some examples of calling methods. For example, we have seen we can get the uppercase version of a `String` by calling its `toUpperCase` method:

~~~ scala
scala> "hello".toUpperCase
res21: String = HELLO
~~~

Some methods accept *parameters* or *arguments*, which control how the method works.

~~~ scala
scala> "abcdef".take(3)
"abcdef".take(3)
res0: String = abc

scala> "abcdef".take(2)
"abcdef".take(2)
res1: String = ab
~~~

<div class="callout callout-info">
#### Method Call Syntax

The syntax for a method call is

~~~ scala
anExpression.methodName(param1, ...)
~~~

or

~~~ scala
anExpression.methodName
~~~

where

- `anExpression` is any expression (which evaluates to an object)
- `methodName` is the name of the method
- the optional `param1, ...` is one or more expressions evaluating to the parameters to the method.
</div>

A method call is an expression, and thus evaluates to an object. This means we can chain method calls together to make more complex programs:

~~~ scala
scala> "hello".toUpperCase.toLowerCase
res22: String = hello
~~~

In what order are the various expressions in a method call evaluated? Method parameters are evaluated left-to-right, before the method is called. So in the expression

~~~ scala
"Hello world!".take(2 + 3)
~~~

the expression `"Hello world!"` is evaluated first, then `2 + 3` (which requires evaluating `2` and then `3` first), then finally `"Hello world!".take(5)`.

### Operators

Because every value in Scala is an object we can also call methods on primitive types such as `Int` and `Boolean`. This is in contrast to Java where `int` and `boolean` are not objects:

~~~ scala
scala> 123.toShort // this is how we define a `Short` in Scala
res1: Short = 123

scala> 123.toByte // and this is how we define a `Byte`
res2: Byte = 123
~~~

But if an `Int` is an object, what are the basic methematical operators such as `+` and `-`? Are they also methods? Yes---Scala methods can have symbolic names as well as alphanumeric ones!

~~~ scala
scala> 43 - 3 + 2
res18: Int = 42

scala> 43.-(3).+(2)
res20: Int = 42
~~~

<div class="callout callout-warning">
Note that in Scala 2.10 and earlier you would have to write `(43).-(3).+(2)` to prevent `43.` being interpreted as a `Double`.
</div>

<div class="callout callout-info">
#### Infix Operator Notation

Any Scala expression written `a.b(c)` can also be written `a b c`.

Note that `a b c d e` is equivalent to `a.b(c).d(e)`, not `a.b(c, d, e)`.
</div>

We can use **infix operator notation** with any method that takes one parameter, regardless of whether it has a symbolic or alphanumeric name:

~~~ scala
scala> "the quick brown fox" split " "
res0: Array[String] = Array(the, quick, brown, fox)
~~~

Infix notation is one of several syntactic shorthands that allow us to write simple operator expressions instead of verbose method calls. There are also notations for **prefix**, **postfix**, **right-associative**, and **assignment-style operators**.

A question poses itself---what precedence rules should we associate with infix operators? Scala uses a set of [precedence rules] derived from the identifiers we use as method names that follow our intuitive understanding from mathematics and logic:

~~~ scala
scala> 2 * 3 + 4 * 5
res0: Int = 26

scala> (2 * 3) + (4 * 5)
res1: Int = 26

scala> 2 * (3 + 4) * 5
res2: Int = 70
~~~

### Take home points

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

As we will see, Scala's focus on programming with expressions allows us to write much shorter code than we can in Java. It also allows us to reason about code in a very intuitive way using values and types.

### Exercises

#### Operator Style

Rewrite in operator-style

~~~ scala
"foo".take(1)
~~~

<div class="solution">
~~~ scala
"foo" take 1
~~~
</div>

Rewrite in method call style

~~~ scala
1 + 2 + 3
~~~

<div class="solution">
~~~ scala
(1).+(2).+(3)
~~~

Note the need to wrap the `1` in brackets. If you fail to do this then `1.` will be interpreted as a `Double` and your result will have type `Double` not `Int`. A mildly subtle difference.
</div>

#### Substitution

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

[precedence rules]: http://stackoverflow.com/questions/2922347/operator-precedence-in-scala

[^patterns]: There is another way of interacting with objects, called pattern matching. We will introduce pattern matching later.
