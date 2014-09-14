---
layout: page
title: Literal Objects
---

We have already covered some of Scala's basic types. In these section we're going to round out that knowledge by covering all Scala's **literal expressions**. A literal expression represents a fixed value that stands "for itself". Here's an example:

~~~ scala
scala> 42
res0: Int = 42
~~~

This interaction at the REPL shows us that the literal `42` evaluates to the `Int` `42`.

Don't confuse a literal with the value it evaluates to! The literal expression is the representation in the program text before the program is run, and the value is the representation in the computer's memory after the program has run.

If you have prior programming experience, particularly Java experience, the literals in Scala should be familiar to you:

## Numbers

Numbers share the same types available in Java: `Int` for 32-bit integers, `Double` for 64-bit floating point, `Float` for 32-bit floating point, and `Long` for 64-bit integers.

~~~ scala
scala> 42
res0: Int = 42

scala> 42.0
res1: Double = 42.0

scala> 42.0f
res2: Float = 42.0

scala> 42L
res3: Long = 42
~~~

Scala also has 16-bit `Short` integers and 8-bit `Byte`s, but there is no literal syntax for creating them. Instead, we create them using methods called `toShort` and `toByte`.

## Booleans

Booleans are exactly the same as Java: `true` or `false`.

~~~ scala
scala> true
res11: Boolean = true

scala> false
res12: Boolean = false
~~~

## Characters

`Chars` are 16-bit Unicode values written as a single character enclosed in single quotes.

~~~ scala
scala> 'a'
res34: Char = a
~~~

<div class="java-tip">
  **Java tip:** Although they are written with initial capitals, Scala's `Int`, `Double`, `Float`, `Long`, `Short`, `Byte`, `Boolen` and `Char` refer to exactly the same things as `int`, `double`, `float`, `long`, `short`, `byte`, `boolean`, and `char` in Java.

  In Scala all of these types act like objects with methods and fields. However, once your code is compiled, a Scala `Int` is exactly the same as a Java `int`. This makes interoperability between the two languages a breeze.
</div>

## Strings

Strings are exactly Java's strings, and are written the same way.

~~~ scala
scala> "this is a string"
res8: java.lang.String = this is a string

scala> "the\nusual\tescape characters apply"
res9: java.lang.String =
the
    usual escape characters apply
~~~

## Null

Null is the same as Java, though not used nearly as often. Scala's `null` also has its own type: `Null`.

~~~ scala
scala> null
res13: Null = null
~~~

<div class="java-tip">
  **Java tip:** Although `nulls` are common in Java code, they are considered very bad practice in Scala.

  The main use of `null` in Java is to implement *optional* values that have some or no value at different points of a program's execution. However, `null` values cannot be checked by the compiler, leading to possible runtime errors in the form of `NullPointerExceptions`.

  Later we will see that Scala has the means to define optional values that *are* checked by the compiler. This removes the necessity of using `null`, making our programs much safer.
</div>

## Unit

Unit, written `()`, is the Scala equivalent of Java's `void`. Unit is the result of expressions that evaluate to no interesting value, such as printing to standard output using `println`. The REPL doesn't print unit but we can ask for the type of an expression to see that unit is in fact the result of some expressions.

~~~ scala
scala> ()

scala> :type ()
Unit

scala> println("something")
something

scala> :type println("something")
Unit
~~~

Unit is an important concept in Scala. Many of Scala's syntactic constructs are *expressions* that have types and values. We need a placeholder for expressions that don't yield a useful value, and unit provides just that.

## Take home points

In this section we have seen **literal** expressions, which evaluate to basic data types. These basics types are mostly identical to Java, except for `Unit` which has no equivalent.

We note that every literal expression has a **type**, and evalutes to a **value** -- something which is also true for more complex Scala expressions.

In the next section we will learn how to define our own object literals.

## Exercises

### Literally Just Literals

What are the values and types of the following Scala literals?

~~~ scala
42

true

123L

42.0
~~~

<div class="solution">
`42` is an `Int`. `true` is a `Boolean`. `123L` is a `Long`. `42.0` is a `Double`.

This exercise just gives you some experience using the Scala console or Worksheet.
</div>

### Quotes and Misquotes

What is the difference between the following literals? What is the type and value of each?

~~~ scala
'a'

"a"
~~~

<div class="solution">
The first is a literal `Char` and the second is a literal `String`.
</div>

### An Aside on Side-Effects

What is the difference between the following expressions? What is the type and value of each?

~~~ scala
"Hello world!"

println("Hello world!")
~~~

<div class="solution">
The literal expression `"Hello world!"` evaluates to a `String` value. The expression `println("Hello world!")` evalutes to `Unit` and, as a side-effect, prints `"Hello world!"` on the console.

This an important distinction between a program that evalutes to a value and a program that prints a value as a side-effect. The former can be used in a larger expression but the latter cannot.
</div>

### Learning By Mistakes

What is the type and value of the following literal? Try writing it on the REPL or in a Scala worksheet and see what happens!

~~~ scala
'Hello world!'
~~~

<div class="solution">
You should see an error message. Take the time to read and get used to the error messages in your development environment -- you'll see plenty more of them soon!
</div>
