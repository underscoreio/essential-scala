---
layout: page
---

# Expressions

Expressions are the most important program construct in Scala. The defining characteristic of an expression is that when it runs it produces a value. More concisely, we say that expressions evaluate to values.

## Simple Literals

The simplest expressions are literals. A literal represents a fixed value. Here's an example:

```scala
scala> 42
res0: Int = 42
```

This interaction at the REPL shows us that the literal `42` evaluates to the `Int` `42`. Don't confuse a literal with the value it evaluates too! The literal expression is the representation in the program text before the program is run. The value is the representation in the computer's memory after the program has run.

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
res8: String = this is a string

scala> "the\nusual\tescape characters apply"
res9: String =
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

Unit, written `()` is the Scala equivalent of Java's `void`. Unit is the result of expressions that evaluate to no interesting value, such as printing to standard output using `println`. The REPL doesn't print unit be can ask for the type of an expression to see that unit is in fact the result of some expressions.

```scala
scala> ()

scala> :type ()
Unit

scala> println("something")
something

scala> :type println("something")
Unit
```


## Compound Expressions

Literals on their own aren't very interesting. It's only when we combine expressions into larger expressions that useful programs can be created.

You're probably used to simple arthimetic expressions.

```scala
scala> 43 - 3 + 2
res14: Int = 42
```

You are probably also familiar with the dot-notation to call methods on objects. Here's one way to create shorts and bytes:

```scala
scala> 42.toShort()
res16: Short = 42

scala> 42.toByte()
res17: Byte = 42
```

### Conditionals

Conditionals are an essential part of any programming language. Scala's `if` statement has the same syntax as Java's. One difference in Scala is that a conditional returns a value.

```scala
scala> if(true) {
         42
       } else {
         40
       }
res45: Int = 42
```

You can drop the brackets if a single expression follows an arm, and even write a conditional on one line.

```scala
scala> if(true) 42 else 40
res47: Int = 42
```

### Operators versus Methods

In Scala **everything is an object**. This means that `Int` and other primitive types in Java are actually objects in Scala. If this is the case, then `+`, `-`, and so on should be methods on `Int` not operators. Is this correct? Yes!

```scala
scala> 43 - 3 + 2
res18: Int = 42

scala> (43).-(3).+(2)
res20: Int = 42
```

(Note in the second example above I had to bracket `43` to stop `43.` being interpreted as a `Double`.)

This is a general rule in Scala. Any expression you can write as `a.b(c)` you can also write as `a b c`. This is known as *operator style*. Note that `a b c d` is equivalent to `a.b(c).d`, not `a.b(c, d)`. You can only operator style with methods that take one or no arguments.

### Operator shortcuts

Scala has a few other shortcuts in addition to operator style. If an object `foo` has a method called `apply` we can call that method using `foo(args)`. For example, `String` has an apply method (through a mysterious mechansim we [explain later](/collections/arrays-and-strings.html)) that allows to us to index characters within the string.

```scala
scala> "hi there!"(0)
res35: Char = h
```

Be aware there is no dot before the parenthesis. Adding one is an error!

```scala
scala> "hi there!".(0)
<console>:1: error: identifier expected but '(' found.
       "hi there!".(0)
```

<!--
If an object `foo` has a method called `update` we can call it using `foo(idx) = value`. `String` doesn't have such a method, but the error message when we attempt to this indicates the compiler has converted the call into a call to `update`.

```scala
scala> "hi there!"(0) = 'b'
<console>:8: error: value update is not a member of String
              "hi there!"(0) = 'b'
              ^
```
-->
