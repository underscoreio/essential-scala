---
layout: page
---

# Introducing Scala's Type System

So far we've seen some simple expression and briefly mentioned some of Scala's types. Now it's time to look in a bit more depth at the type system. We're not going to cover all of it here, but rather just look at some of the basics.

## The Type Hierarchy

Understanding the type hierarchy is fairly basic. Unlike Java, which separates primitive and object types, in Scala everything is an object. As a result the "primitive" types must live in the object hierarchy. `AnyVal` is their commmon supertype. `AnyRef` is how you spell `java.lang.Object` in Scala and is the supertype of all reference types. `Any` is the root of the class hierarchy. At the bottom of the hierarchy is `Nothing`. It is a subtype of all types, but no values with type `Nothing` exist. Until Scala 2.10 all Scala objects also extended `ScalaObject`. Since 2.10 `ScalaObject` has been removed.

## Value Types

Remember that Scala's value types (`Int`, `Double`, and so) are exactly equal to Java's primitive types (`int`, `double`, and so). Yet in Scala they are objects. How can this be? The answer is that Scala does autoboxing just like Java to give the appearance that they are objects when it is necessary. When it isn't necessary a Scala `Int` is exactly a Java `int` with the same performance and space usage.

In Scala 2.10 and onwards we'll be able to define our own value types, using a method known as [Value Classes](http://docs.scala-lang.org/sips/pending/value-classes.html). This is moderately advanced so we're not going to discuss it here, but it may be useful to be aware of this possibility.

## Type Declarations

So far we haven't mentioned any types, though it is clear that the REPL has inferred them. We can declare a type for an expression by following the expression with a colon and then a type. For example, to type an `Int` expression as an `AnyVal` we could write:

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
