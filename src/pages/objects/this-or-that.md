---
layout: page
title: This or That
---

We have seen the basics of classes. We're now going to explore how we can use Scala features to **abstract over common patterns**. Simply put, we want to extract common patterns out into reusable components. In this section we're going to look at traits, the fundamental unit of composition in Scala. We'll then see how we can model and use data where were we want to say that something is one of a number of alternatives, sometimes called a **sum type**.

As a concrete example, imagine we are writing analytics software for a web site and we want to record actions taken by visitors. There are two types of visitor: anonymous users and those that have logged in to our site. How do we model this in Scala?


## Traits

Traits are the first building block we need. A trait is the smallest unit of composition in Scala. A trait is like a class in that it can have instance variables and method definitions, but it differs in several important ways:

A trait cannot have a constructor and thus you can't create instance of a trait. You must **extend** the trait in a class to be able to create an instances.

A trait can method signatures without implementation, known as **abstract methods**. This means you can define the name and type of a method without providing an implementation. Any class that implements the trait must provide a concrete implementation.

We can model visitors in our analytics software like this[^joda]:

~~~ scala
import java.util.Date

trait Visitor {
  def id: String // A unique id we assign to each user
  def createdAt: Date // The date this user first visited our site

  // How long has this visitor been around?
  def age: Long =
    new Date().getTime() - createdAt.getTime()

}

case class Anonymous(
  val id: String,
  val createdAt: Date = new Date()
) extends Visitor

case class User(
  val id: String,
  val emailAddress: String,
  val createdAt: Date = new Date()
) extends Visitor
~~~

[^joda]: For production code I would use the [Joda-Time](http://www.joda.org/joda-time) library, but that adds an external dependency. For ease of use at the REPL this example uses Java's `Date` object.

This example demonstrates many of the features of traits. We have two abstract methods in `Visitor`, `id` and `createdAt`. The two concrete classes that extend Visitor both define these as `val`s, which raises two questions:

* how can we implement a method with an instance variable?; and
* why not define `id` and `createdAt` as instance variables in `Visitor`, rather than abstract methods?

Let's tackle them in turn.

Scala adhers to something called the **uniform access principle**. The broad idea is that you shouldn't be able to tell from the outside if an object's property is an instance variable or a method. This allows flexibility in the implementation. For example, you could start of method that fetches a value from a data store and later switch to an instance variable if you need to cache the value for performance reasons. If you've been wondering why Scala has methods with no arguments, now you know. This also examples how we can substitute a `val` for a method.

The upshot is that a trait declaring an abstract method of no arguments gives flexibility in implementation to classes that extend that trait. If the trait instead declares an instance variable then extending classes will also have that instance variable. Thus, as a general rule of thumb, **prefer abstract methods with no arguments to instance variables in traits**.

Also note the concrete method `age` in `Visitor`. All classes that extend `Visitor` will automatically have this method.

Finally, there is an `import` statement. We haven't seen one before, but in this case it works just the same as Java's.


## Scala's Type System

When a class extends a trait it becomes a subtype of that trait. Anywhere a particular type is expected we can also provide a subtype.

For example, we can bind a `User` with the type of `Visitor`.

~~~ scala
scala> val visitor: Visitor = User("a", "me@example.com")
visitor: Visitor = User(a,me@example.com,Fri Feb 14 12:05:25 GMT 2014)
~~~

We can also pass a `User` to a method expecting a `Visitor`.

~~~ scala
scala> def visitorAge(v: Visitor) =
     |   v.id + " is " + v.age + "ms old"
visitorAge: (v: Visitor)String

scala> visitorAge(User("a", "me@example.com"))
res14: String = a is 0ms old
~~~

It's time we looked at Scala's type system in more depth.

### What is a Type?

**A type is any property of a program that we can establish without evaluating the program.** This seems simple enough but the implication is profound. In old languages like C, types were used to specify the machine representation of data, essentially providing optimisation hints to the compiler. In modern languages like Scala, types are used by the programmer to ensure that important program properties are maintained. For example, error handling is an important part of many programs. We can encode error handling in the type system (using, for example, the `Try` type, or Scalaz's `Validation`) and then the compiler will ensure we always handle errors correctly. Appropriate use of the type system is a mark of an accomplished Scala developer.

### The Type Hierarchy

Unlike Java, which separates primitive and object types, in Scala everything is an object. As a result the "primitive" types must live in the object hierarchy. `AnyVal` is their commmon supertype. `AnyRef` is how you spell `java.lang.Object` in Scala and is the supertype of all reference types. `Any` is the root of the class hierarchy. At the bottom of the hierarchy is `Nothing`. It is a subtype of all types, but there are no values with type `Nothing`.

### Value Types

Remember that Scala's value types (`Int`, `Double`, and so) are exactly equal to Java's primitive types (`int`, `double`, and so). Yet in Scala they are objects. How can this be? The answer is that Scala does autoboxing just like Java to give the appearance that they are objects when it is necessary. When it isn't necessary a Scala `Int` is exactly a Java `int` with the same performance and space usage.

In Scala 2.10 and onwards we're be able to define our own value types, using a method known as [Value Classes](http://docs.scala-lang.org/sips/pending/value-classes.html). This is moderately advanced so we're not going to discuss it here, but it is useful to be aware of this possibility.


## Pattern Matching

We have seen how to use traits to model data that is of one type or another. Now we'll look at how to use this data. Presumably the distinction between types is important or we wouldn't have bothered making it in the first place. Let's imagine we want to add the ability to email `Visitor`s. We can only email `User`s, because we don't know the email address for `Anonymous` visitors. If we have a `Visitor` how can we tell what subtype we have, and thus if we can email them?

We're going to look at two solutions: an object-oriented solution based on dynamic dispatch, and a functional solution based on pattern matching.

The object-oriented solution is to add an abstract method to `Visitor` along with implementations in the subtypes. The code looks something like this (trimmed for simplicity).

~~~ scala
trait Visitor {
  def email(subject: String, body: String): Unit
}

case class Anonymous() extends Visitor {
  // Do nothing; you can't email an anonymous visitor
  def email(subject: String, body: String) =
    ()
}

case class User() extends Visitor {
  def email(subject: String, body: String) = {
    reallySendAnEmail(emailAddress, subject, body)
  }
}
~~~

Then if we have a `Visitor` we can simply call the `email` method and stuff happens, as if by magic.

I maintain **this is a bad design**. As we add more features to do with `Visitor`s we'll find the `Visitor` class getting bigger and bigger. It will be harder to maintain and test, and any system that uses `Visitor` will have to carry around all its dependencies.

A better design is to keep our case classes as [value objects](http://en.wikipedia.org/wiki/Value_object), meaning they shouldn't change (i.e. they are immutable), and they should only have methods that relate directly to the data they hold. The `age` method is fine, as it depends only on the `createdAt` date. The `email` method depends on a whole host of email processing code and thus should not appear in the `Visitor` trait. Instead methods to email `Visitor`s should be in a separate service. Let's call it `EmailService`.

~~~ scala
trait EmailService {
  def email(v: Visitor, subject: String, body: String): Unit = {
    // What do we put here?
  }
}
~~~

In the `email` method we now need to know if we have a `User` or an `Anonymous`. Scala has an awesome general purpose tool called **pattern matching** that we use in this situation. The code looks like this:

~~~ scala
trait EmailService {
  def email(v: Visitor, subject: String, body: String): Unit = {
    v match {
      case Anonymous(id, createdAt) =>
        ()
      case User(id, address, createdAt) =>
        reallySendAnEmail(address, subject, body)
    }
  }
}
~~~

Pattern matching is introduced using the `match` keyword. It is followed by a sequence of `case` expressions. After each `case` keyword is a pattern, an `=>`, and an expression. Pattern matching is itself an expression and thus produces a value.

~~~
expr0 match {
  case pattern1 => expr1
  case pattern2 => expr1
  ...
}
~~~

Pattern matching proceeds by checking each pattern in turn, and evaluating the right-hand side expression of the first pattern that matches[^compilation].

[^compilation]: In reality patterns are compiled to a more efficient form than a sequence of tests, but the semantics are the same.

The syntax of pattern matching is very expressive. For case classes the pattern syntax matches the constructor syntax. So the pattern `User(id, address, createdAt)` matches a `User` and binds the names `id`, `address`, and `createdAt` to their respective values. Binding happens by position, not by name, so if we wrote `User(address, createdAt, id)` the name `address` would be bound to the value of the `id` and so on. If there is a value we don't want to bind to a name, we use the `_` symbol. As we only care about the `address` in the example above, we could just write `User(_, address, _)`.

Literals can be used as patterns, which match themselves. So a pattern like `User("a", _, _)` would match the user with `id "a"`.
