---
layout: page
title: Function Types
---

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
