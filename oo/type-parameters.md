---
layout: page
---

# Type parameters

*Type parameters* allow us to create *generic* classes and traits that can be used with a variety of types of data. The classic example is a collection such as a *stack*:

{% highlight scala %}
scala> class Stack[T] {
     |   var items: List[T] = Nil
     |
     |   def push(item: T): Unit = {
     |     items = item :: items
     |   }
     |
     |   def pop(): T = {
     |     val answer = items.head
     |     items = items.tail
     |     answer
     |   }
     | }
{% endhighlight %}

Scala uses square brackets in place of Java's echelons to denote type parameters: `Stack[T]` is a generic class with a single type parameter, `T`.

We can instantiate `Stack` by specifying a concrete type value for `T`. Scala's unified type hierarchy allows us to use `Stack` with any value type as well as any reference type. Here we create a `Stack[Int]`:

{% highlight scala %}
scala> val intStack = new Stack[Int]
intStack: Stack[Int] = Stack@7894b886

scala> :paste
// Entering paste mode (ctrl-D to finish)

intStack.push(1)
intStack.push(2)
intStack.push(3)
List(intStack.pop(), intStack.pop(), intStack.pop())

// Exiting paste mode, now interpreting.

res1: List[Int] = List(3, 2, 1)

scala> intStack.push(1.0)
<console>:10: error: type mismatch;
 found   : Double(1.0)
 required: Int
              intStack.push(1.0)
                            ^
{% endhighlight %}

## Upper type bounds

## Parameters on classes and traits

## Parameters on methods

## Covariance

## Contravariance

## Lower type bounds
