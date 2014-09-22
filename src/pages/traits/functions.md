---
layout: page
title: Functions
---

Much earlier in this Chapter we were introduced to the `apply` methods, which lets us treat objects as functions in a syntactic sense:

~~~ scala
scala> object add1 {
     |   def apply(in: Int) = in + 1
     | }
defined module add1

scala> add1(2)
res2: Int = 3
~~~

This is a big step towards doing real functional programming in Scala but we're missing one important component: *types*.

As we have seen, types allow us to abstract across values. We've seen all sorts of special case functions like `Adders` and `ActionListeners`, but what we really want is a generalised set of types that allow us to represent computations of any kind.

Enter Scala's `Function` types.

## Function types

Scala has 23 built-in generic classes for functions of 0 to 22 arguments. Here's what they look like:

~~~ scala
trait Function0[+R] {
  def apply: R
}

trait Function1[-A, +B] {
  def apply(a: A): B
}

trait Function2[-A, -B, +C] {
  def apply(a: A, b: B): C
}

// and so on...
~~~

For the most part this is all stuff we know. The only pieces of syntax we haven't seen are the `+` and `-` annotations on the type parameters. These are called *variance annotations*. Let's look at this for a moment.

### Invariance, Covariance and Contravariance

<div class="alert alert-warning">
**Note:** Variance is one of the trickier aspects of Scala's type system. Although it is useful to be aware of its existence, we rarely have to use it in application code. We will revisit covariance in the *Functional Programming* chapter, but we won't revisit contravariance for the remainder of the course.
</div>

The *variance* of a generic type determines how its supertype/subtype relationships change with respect with its type parameters:

A type `Foo[T]` is **invariant** in terms of `T`, meaning that the types `Foo[A]` and `Foo[B]` are unrelated regardless of the relationship between `A` and `B`. This is the default variance of any generic type in Scala.

A type `Foo[+T]` is **covariant** in terms of `T`, meaning that `Foo[A]` is a supertype of `Foo[B]` if `A` is a supertype of `B`. Most Scala collection classes are covariant in terms of their contents. We'll see these next chapter.

A type `Foo[-T]` is **contravariant** in terms of `T`, meaning that `Foo[A]` is a *subtype* of `Foo[B]` if `A` is a *supertype* of `B`. The only example of contravariance that I am aware of is function arguments.

Functions are contravariant in terms of their arguments and covariant in terms of their return type. This seems counterintuitive but it makes sense if we look at it from the point of view of function arguments. Consider some code that expects a `Function1[A, B]`:

~~~ scala
class Box[A](value: A) {
  /** Apply `func` to `value`, returning a `Box` of the result. */
  def map[B](func: Function1[A, B]): Box[B] =
    Box(func(a))
}
~~~

To understand variance, consider what functions can we safely pass to this `map` method:

 - A function from `A` to `B` is clearly ok.

 - A function from `A` to a subtype of `B` is ok because it's result type will have all the properties of `B` that we might depend on. This indicates that functions are covariant in their result type.

 - A function expecting a supertype of `A` is also ok, because the `A` we have in the box will have all the properties that the function expects.

 - A function expecting a subtype of `A` is not ok, because our value may in reality be a different subtype of `A`.

### Function type shorthand

Scala programmers use functions *a lot*, so Scala has some neat shorthand for writing function types. Here is a synopsis:

~~~ scala
() => A     // short for Function0[A]
(A) => B    // short for Function1[A, B]
(A, B) => C // short for Function2[A, B, C]
// and so on... also:
A => B      // short for Function1[A, B]
~~~

We can write these shorthands wherever we would write function type. For example:

~~~ scala
case class Box[A](value: A) {
  def map[B](func: A => B): Box[B] = Box(func(a))
}

case class Adder(amount: Int) extends (Int => Int) {
  def apply(value: Int) = value + amount
}
~~~

## Function literals

Scala takes this shorthand syntax further by giving us a **function literal syntax** specifically for creating new functions. Here is a function that adds one to an `Int`:

~~~ scala
scala> val sayHi = () => "Hi!"
sayHi: () => String = <function0>

scala> sayHi()
res1: String = Hi!

scala> val add1 = (x: Int) => x + 1
add1: Int => Int = <function1>

scala> add1(10)
res2: Int = 11

scala> val sum = (x: Int, y:Int) => x + y
sum: (Int, Int) => Int = <function2>

scala> sum(10, 20)
res3: Int = 30
~~~

In code where we know the argument types, we can sometimes **drop the type annotations** and allow Scala to infer them[^parens]:

[^parens]: Note that we only can drop the parentheses around the argument list on single-argument functions -- we still have to write `() => foo` and `(a, b) => foo` on functions of other arities.

~~~ scala
scala> Box(1).map(a => a * 2)
res4: Box[Int] = 2
~~~

### Placeholder syntax

One final level of syntactic sugar -- in very simple situations we can write inline functions using an extreme shorthand called **placeholder syntax**. It looks like this:

~~~ scala
scala> Box(1).map(_ * 2)
res4: Box[Int] = 2
~~~

`_ * 2` expands to `a => a * 2`, which in this example the compiler expands to `(a: Int) => a * 2`. The types obviously depend on the use case. Other examples:

~~~ scala
_ + _     // expands to `(a, b) => a + b`
foo(_)    // expands to `(a) => foo(a)`
foo(_, b) // expands to `(a) => foo(a, b)`
_(foo)    // expands to `(a) => a(foo)`
// and so on...
~~~

Placeholder syntax, while wonderfully terse, only works in certain situations. If the compiler can't infer the correct types of each argument and expression we will get a compilation error. Often tricks like wrapping placeholder functions in parentheses can help -- it's best to play with it to get a feel for the situations where it can be applied. We can always fall back to longer forms if required.

## Converting methods to functions

Scala contains one final feature that is directly relevant to this section -- the ability to convert method calls to functions. This is closely related to placeholder syntax -- simply follow a method with an underscore:

~~~ scala
scala> object Sum {
     |   def sum(x: Int, y: Int) = x + y
     | }
defined module Sum

scala> Sum.sum
<console>:9: error: missing arguments for method sum in object Sum;
follow this method with `_' if you want to treat it as a partially applied function
              Sum.sum
                  ^

scala> (Sum.sum _)
res11: (Int, Int) => Int = <function2>
~~~

In situations where Scala can infer that we need a function, we can even drop the underscore and simply write the method name -- the compiler will promote the method to a function automatically:

~~~ scala
scala> object MathStuff {
     |   def add1(num: Int) = num + 1
     | }
defined module MathStuff

scala> Counter(2).adjust(MathStuff.add1)
res12: Counter = Counter(3)
~~~

## Exercises

### Truly Functional Counters

Let's revisit our `Counter` exercise from the [Objects as Functions](apply.html) section one last time. At this point we have no need for our `Adder` class - we can simply use functions instead. Rewrite `Counter.adjust` to accept an argument of type `Int => Int`.

Once you have rewritten `Counter`, demonstrate the increased flexibility of the function argument over the `Adder`.

<div class="solution">
Here's a version of `Counter` that no longer depends on `Adder`. I've rewritten `inc` and `dec` in terms `adjust` for illustrative purposes:

~~~ scala
case class Counter(count: Int = 0) {
  def inc = adjust(_ + 1)
  def dec = adjust(_ - 1)
  def adjust(func: Int => Int) = new Counter(func(count))
}
~~~

To demonstrate that functions are more flexible than adders, let's do some new things other than adding to our counters:

~~~ scala
scala> Counter(10).adjust(_ * 2)
res5: Counter = Counter(20)

scala> Counter(3).adjust(_ + res5.count)
res6: Counter = Counter(23)
~~~

The advantage of using functions over `Adders` is that we can now perform arbitrary operations on counters -- even operations that capture values from surrounding variables. This is much more flexible than `Adders`, and all we had to do was delete a few lines of code!
</div>
