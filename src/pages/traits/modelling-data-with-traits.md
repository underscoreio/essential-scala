---
layout: page
title: Modelling Data with Traits
---

With (sealed) traits and (final) case classes we have seen some of the most important features in Scala and the ones you will use most often in day-to-day programming. In this section we're going to look at how we can directly translate a data model into Scala code using traits and classes. In particular we're going to look at **is-a** and **has-a** relationships, **logical ors** and **logical ands**, and their translation in Scala. The type of data we will be created are called **algebraic data types**.

Our goal in this section is to see how to translate a data model into Scala code. In the next section we'll see patterns for code that uses algebraic data types.

## The Has-a And Pattern

Our first pattern is to model data that contains other data. We might describe this as "`A` has a `B` and `C`". For example, a `Cat` has a colour and a favourite food; a `Visitor` has an id and a creation date; and so on.

The way we write this is to use a case class. We've already done this many times in exercises; now we're formalising the pattern.

<div class="callout callout-info">
#### Has-a And Pattern

If `A` has a `b` (with type `B`) *and* a `c` (with type `C`) write

~~~ scala
case class A(b: B, c: C)
~~~
</div>

## The Is-a Or Pattern

Our next pattern is the model data that is two or more distinct cases. We might describe this as "`A` is a `B` or `C`". For example, a `Feline` is a `Cat`, `Lion`, or `Tiger`; a `Visitor` is a `Anonymous` or `User`; and so on.

We write this using the sealed trait / final case class pattern.

<div class="callout callout-info">
#### Is-a Or Pattern

If `A` is a `B` or `C` write

~~~ scala
sealed trait A
final case class B()
final case class C()
~~~
</div>

## Algebraic Data Types

An algebraic data type is any data that uses the above two patterns.

This illustrates the most important role of using traits, and the one on which we're focusing: defining a logical relationship between traits and classes. Given the definitions above we can say that a `Visitor` *is-a* `User` *or* an `Anonymous`.



## Modelling Data With Traits

The most important use of traits, and the one we're focusing on in this course, is to model a **logical or** relationship.

In the example above we modelled a website visitor which is either a registered user or anonymous. There are many other cases. Attempting to login to a website can succeed or failure. A smartphone can run iOS, Android, Windows Mobile, Firefox OS, or a few other operating systems. These are all cases of logical ors.

In some examples we can enumerate all the cases (logins either succeed or fail; there are no other choices), whilst in other cases we cannot (such as the smartphone example; new mobile operating systems will continue to be developed). The trait pattern allows us to model either case but we will shortly see an refinement specifically for the case when we can completely enumerate all the variants. For now this is our pattern:

<div class="callout callout-info">
### The Logical Or Pattern

If A *is a* B *or* a C, we write

~~~ scala
trait A

case class B() extends A
case class C() extends A
~~~
</div>

## Subtyping and Polymorphism

A trait is a type just like a class. A class that extends a trait is a *subtype* of that trait, and any object of that class is both a value of the subtype and a value of the supertype. This is a kind of [polymorphism](http://en.wikipedia.org/wiki/Polymorphism_(computer_science)) -- essentially the polymorphism we get from Java.

Anywhere in our code that we expect an instance of the supertype, we can use an instance of the subtype instead. For example, we can assign `User` to a variable of type `Visitor` or pass it to a method that expects a `Visitor` as a parameter:

~~~ scala
scala> val visitor: Visitor = User("a", "me@example.com")
visitor: Visitor = User(a,me@example.com,Fri Feb 14 12:05:25 GMT 2014)

scala> def ageString(v: Visitor) =
     |   v.id + " is " + v.age + "ms old"
ageString: (v: Visitor)String

scala> ageString(User("a", "me@example.com"))
res14: String = a is 0ms old
~~~

## Exploiting Types

In one sense a type is just a collection of values that share common properties. More than that, though, a type can represent **any property of a program that we can establish without evaluating it.**

In old languages like C, types were used to specify the machine representation of data, essentially providing optimisation hints to the compiler. In modern languages like Scala, **types are used to ensure that important program properties are maintained**.

A good Scala developer uses types to his or her advantage to avoid bugs and write self-documenting code.
