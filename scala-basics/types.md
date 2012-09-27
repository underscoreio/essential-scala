---
layout: page
---

# Introducing Scala's Type System

So far we've seen some simple expressions. Every expression has a type, and here we're going to look at some of the basics of the type system.

## What is a Type?

Before we get going, let's define what a type is: *A type is any property of a program that we can establish without evaluating the program.* This seems simple enough but the implication is profound. In old languages like C types were used to specify the machine representation of data, essentially providing optimisation hints to the compiler. In modern languages like Scala types are used by the programmer to ensure that important program properties are maintained. For example, error handling is an important part of many programs. We can encode error handling in the type system (using, for example, the `Either` type, Scalaz's `Validation`, or Scala 2.10's `Try`) and then the compiler will ensure we always handle errors correctly. Appropriate use of the type system is a mark of an accomplished Scala developer.

## The Type Hierarchy

Unlike Java, which separates primitive and object types, in Scala everything is an object. As a result the "primitive" types must live in the object hierarchy. `AnyVal` is their commmon supertype. `AnyRef` is how you spell `java.lang.Object` in Scala and is the supertype of all reference types. `Any` is the root of the class hierarchy. At the bottom of the hierarchy is `Nothing`. It is a subtype of all types, but no values with type `Nothing` exist. Until Scala 2.10 all Scala objects also extended `ScalaObject`. Since 2.10 `ScalaObject` has been removed.

## Value Types

Remember that Scala's value types (`Int`, `Double`, and so) are exactly equal to Java's primitive types (`int`, `double`, and so). Yet in Scala they are objects. How can this be? The answer is that Scala does autoboxing just like Java to give the appearance that they are objects when it is necessary. When it isn't necessary a Scala `Int` is exactly a Java `int` with the same performance and space usage.

In Scala 2.10 and onwards we'll be able to define our own value types, using a method known as [Value Classes](http://docs.scala-lang.org/sips/pending/value-classes.html). This is moderately advanced so we're not going to discuss it here, but it may be useful to be aware of this possibility.

## Type Declarations

We can declare a type for an expression by following the expression with a colon and then a type. For example, to type an `Int` expression as an `AnyVal` we could write:

{% highlight scala %}
scala> 42 : AnyVal
res40: AnyVal = 42
{% endhighlight %}

Note once we've done so we can only call methods that exist on `AnyVal`.

{% highlight scala %}
scala> (42: AnyVal).-(2)
<console>:8: error: value - is not a member of AnyVal
              (42: AnyVal).-(2)
                           ^

scala> (42: AnyVal).hashCode
res44: Int = 42
{% endhighlight %}

## Type Inference

In all of our programs so far we haven't needed to declare types. This is because Scala performs *type inference*, a process by which is works out the types of expressions.

Scala uses a simple type inference algorithm, where types flow from parameters to result of methods, and left to right across expressions and parameter lists. This algorithms makes it easy to reason about type inference, but does mean we have to provide more type declarations than we might need to with a more complex algorithm.
