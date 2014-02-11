---
layout: page
title: Literal Objects
---

The simplest expressions are literals. A literal expression represents a fixed value that stands "for itself". Here's an example:

```scala
scala> 42
res0: Int = 42
```

This interaction at the REPL shows us that the literal `42` evaluates to the `Int` `42`. Don't confuse a literal with the value it evaluates to! The literal expression is the representation in the program text before the program is run. The value is the representation in the computer's memory after the program has run.

If you have prior programming experience you won't be surprised at the available literals in Java. Here's a quick run down of the major ones.

### Numbers

Numbers share the same types available in Java: `Int` for 32-bit integers, `Double` for 64-bit floating point, `Float` for 32-bit floating point, and `Long` for 64-bit integers.

```scala
scala> 42
res0: Int = 42

scala> 42.0
res1: Double = 42.0

scala> 42.0f
res2: Float = 42.0

scala> 42.0l
res3: Long = 42
```

Scala also has 16-bit `Short` integers and 8-bit `Byte`s, but there is no literal syntax for creating them. We'll see how to create them in a bit.

### String

Strings are exactly Java's strings, and written the same way.

```scala
scala> "this is a string"
res8: java.lang.String = this is a string

scala> "the\nusual\tescape characters apply"
res9: java.lang.String =
the
usual	escape characters apply
```

### Booleans

Booleans are exactly the same as Java.

```scala
scala> true
res11: Boolean = true

scala> false
res12: Boolean = false
```

### Char

Characters (`Char`s) are 16-bit Unicode values written as a single character enclosed in single quotes.

```scala
scala> 'a'
res34: Char = a
```

### Null

Null is the same as Java, though not used nearly as often

```scala
scala> null
res13: Null = null
```

### Unit

Unit, written `()` is the Scala equivalent of Java's `void`. Unit is the result of expressions that evaluate to no interesting value, such as printing to standard output using `println`. The REPL doesn't print unit but we can ask for the type of an expression to see that unit is in fact the result of some expressions.

```scala
scala> ()

scala> :type ()
Unit

scala> println("something")
something

scala> :type println("something")
Unit
```

Unit is an important concept in Scala. Most of a Scala program consists of expressions, and expressions must evaluate to values. We need a value for expressions that have no useful value, and unit is that value.
