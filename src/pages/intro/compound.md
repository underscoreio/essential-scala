---
layout: page
title: Interacting with Objects
---

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

## Conditionals

Conditionals are an essential part of any programming language. Scala's `if` statement has the same syntax as Java's. One difference in Scala is that a conditional returns a value. That is, a conditional is also an expression.

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

## Operators versus Methods

In Scala **everything is an object**. This means that `Int` and other primitive types in Java are actually objects in Scala. If this is the case, then `+`, `-`, and so on should be methods on `Int` not operators. Is this correct? Yes!

```scala
scala> 43 - 3 + 2
res18: Int = 42

scala> (43).-(3).+(2)
res20: Int = 42
```

(Note in the second example above I had to bracket `43` to stop `43.` being interpreted as a `Double`.)

This is a general rule in Scala. Any expression you can write as `a.b(c)` you can also write as `a b c`. This is known as *operator style*. Note that `a b c d` is equivalent to `a.b(c).d`, not `a.b(c, d)`. You can only use operator style with methods that take one or no arguments.

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

{% comment %}

If an object `foo` has a method called `update` we can call it using `foo(idx) = value`. `String` doesn't have such a method, but the error message when we attempt to this indicates the compiler has converted the call into a call to `update`.

```scala
scala> "hi there!"(0) = 'b'
<console>:8: error: value update is not a member of java.lang.String
              "hi there!"(0) = 'b'
              ^
```

{% endcomment %}
