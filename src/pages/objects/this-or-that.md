---
layout: page
title: This or That
---

We have seen the basics of classes. We're now going to explore how we can use Scala features to **abstract over common patterns**. Simply put, we want to extract common patterns out into reusable components. In this section we're going to look at traits, the fundamental unit of composition in Scala. We'll then see how we can model data where were we want to say that something is one thing or another.

As a concrete example, imagine we are writing analytics software for a web site and we want to record actions taken by visitors. There are two types of visitor: anonymous users and those that have logged in to our site. How do we model this in Scala?

## Traits

Traits are the first building block we need. A trait is the smallest unit of composition in Scala. A trait is like a class in that it can have instance variables and method definitions, but it differs in several important ways:

A trait cannot have a constructor and thus you can't create instance of a trait. You must **extend** the trait in a class to be able to create an instances.

A trait can method signatures without implementation, known as **abstract methods**. This means you can define the name and type of a method without providing an implementation. Any class that implements the trait must provide a concrete implementation.

We can model visitors in our analytics software like this[^joda]:

~~~ scala
trait Visitor {
  def id: String // A unique id we assign to each user
  def createdAt: DateTime // The date this user first visited our site

  // How long has this visitor been around?
  def age: Duration =
    new Duration(createdAt, DateTime.now())
}

case class Anonymous(
  val id: String,
  val createdAt: DateTime = DateTime.now()
) extends Visitor

case class User(
  val id: String,
  val email: String,
  val createdAt: DateTime = DateTime.now()
) extends Visitor
~~~

[^joda]: In case it's of interest, I've used the [Joda-Time](http://www.joda.org/joda-time) API for date and time processing in this example.

This example demonstrates many of the features of traits. We have two abstract methods in `Visitor`, `id` and `createdAt`. The two concrete classes that extend Visitor both define these as `val`s, which raises two questions:

* how can we implement a method with an instance variable?; and
* why not define `id` and `createdAt` as instance variables in `Visitor`, rather than abstract methods?

Let's tackle them in turn.

Scala adhers to something called the **uniform access principle**.
