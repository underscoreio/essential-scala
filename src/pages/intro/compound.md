---
layout: page
title: Interacting with Objects
---

We interact with objects by calling or invoking methods on them. Method calls are expressions and thus they return values. For example, we can get the uppercase version of a `String` by calling the `toUpperCase` method.

~~~scala
scala> "hello".toUpperCase
res21: String = HELLO
~~~

We can make several methods calls to create a more complex program.

~~~scala
scala> "hello".toUpperCase.toLowerCase
res22: String = hello
~~~

## Operators versus Methods

In Scala **everything is an object**. This means that `Int` and other primitive types in Java are actually objects in Scala. If this is the case, then `+`, `-`, and so on should be methods on `Int` not operators. Is this correct? Yes!

~~~scala
scala> 43 - 3 + 2
res18: Int = 42

scala> (43).-(3).+(2)
res20: Int = 42
~~~

(Note in the second example above I had to bracket `43` to stop `43.` being interpreted as a `Double`.)

This is a general rule in Scala. Any expression you can write as `a.b(c)` you can also write as `a b c`. This is known as *operator style*. Note that `a b c d` is equivalent to `a.b(c).d`, not `a.b(c, d)`. You can only use operator style with methods that take one or no arguments.


## Conditionals

Conditionals are an essential part of any programming language. Scala's `if` statement has the same syntax as Java's. One difference in Scala is that a conditional returns a value. That is, a conditional is also an expression.

~~~scala
scala> if(true) {
         42
       } else {
         40
       }
res45: Int = 42
~~~

You can drop the brackets if a single expression follows an arm, and even write a conditional on one line.

~~~scala
scala> if(true) 42 else 40
res47: Int = 42
~~~
