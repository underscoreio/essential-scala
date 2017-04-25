---
layout: page
title: "This and That: Traits as Mixins"
---

In the previous section we looked at categorising objects using a single set of traits. In this section we will look at objects that can simultaneously be categorised using several type hierarchies. In functional programming these are known as [product types]. In object oriented programming there is a related concept: [multiple inheritance].

[product types]: http://en.wikipedia.org/wiki/Product_type
[multiple inheritance]: http://en.wikipedia.org/wiki/Multiple_inheritance

## Traits as Mixins

We achieve composition in Scala by separating each component into its own trait and **mixing the traits we want to use** together to form classes.

The syntax is to write `extends` for the first trait and `with` for each following trait: `A extends B with C with D`. For example, imagine the following model of staff and students at a university:

```tut:invisible
trait Course
```

```tut:book:silent
trait Person {
  def firstName: String
  def lastName: String
}

trait Student extends Person {
  def studentNumber: String
  def coursesTaken: Seq[Course] // a list of courses
}

trait Staff extends Person {
  def staffNumber: String
  def coursesTaught: Seq[Course] // a list of courses
}

trait TeachingAssistant extends Staff with Student
```

In this example, `TeachingAssistent` ends up with all of the methods from `Person`, `Staff`, and `Student`.

## Is-a vs Has-a

A trait or class is a subtype of every trait it extends. This means that if `A extends B`, `A` **is a** `B` and may be used wherever a `B` is expected. A `TeachingAssistant` is a `Staff` member, a `Student`, and a `Person` and can be treated as any of these types.

Don't confuse an is-a relationship with a **has a** relationship. A book has a publisher but is not a publisher itself, so we would not mixin a `Publisher` trait to a `Book`.

## Overriding and Super Calls

Traits and classes can mofidy the fields and methods they inherit from supertypes. We can use the `override` keyword to redefine an existing field or method, and use the `super` keyword to refer to the original definition that we are overriding. For example:

```tut:book:silent
trait Person {
  def firstName: String
  def lastName: String
  def name = s"$firstName $lastName"
}

trait Veteran extends Person {
  def rank: String
  override def name = s"$rank ${super.name}"
}
```

## Trait Linearization

Overriding can lead to interesting interactions if we have many traits that each override a particular method. Consider the following code---what happens when we call `foo` on an instance of `Example`?

```tut:book:silent
trait Fooable { def foo: String }

trait A extends Fooable { override def foo: String = "A" }
trait B extends Fooable { override def foo: String = "B" }
trait C extends Fooable { override def foo: String = "C" }

case class Example() extends A with B with C
```

The simple way to tell is to ask the REPL:

```
scala> Example().foo
res1: String = C
```

Ambiguity between traits is resolved using **linearization**. They are effectively stacked on top of one another and any method call is resolved by searchin up the stack until a matching definition is found. In the example the search order imposed by linearization is `Example -> C -> B -> A -> AnyRef -> Any` (extending `AnyRef` is implicit when you don't explicitly `extend` anything else).

Linearization enables us to come up with all sorts of byzantine designs where traits add pieces of behaviour to a few common methods. The simple rule of designing with linearization is: don't. **If you depend on the order in which traits are stacked, you are doing something wrong**---it is a sure way to introduce bugs into your code.

## Take home points

We can model **multiple inheritance** in Scala by **mixing traits** with one another using the `with` keyword.

Traits solve the method resolution problems of multiple inheritance by defining a **linearization order** that dictates the order of overriding. The linearization order is tied to the order of traits in the header of a class.

**If you find yourself relying on the linearization order in your type heirarchy, stop!** That way madness lies.

Multiple inheritance is one way (but not the only way) of modelling a **this and that** relationship between types. In functional programming, this is called a **product type**.

We can also model product types using *generics*---we'll see these later.

## Exercises

### Perplexing publications

Let's create a simple model for publisher data. Code a set of traits and classes according to the following description:

  - A *publication* is a *book* or a *periodical*.

  - A *book* has an *author* while a *periodical* has an *editor*.

  - *Periodicals* have many *issues*, each of which has  a *volume* and an *issue number*.

  - A *manuscript* is a document of a certain *length* written by an *author*.

  - A *book* is a *manuscript*, but an *issue* of a periodical contains a sequence of *manuscripts*.

Tip: a sequence of type `A` has type `Seq[A]`.

<div class="solution">
This is as much a problem of parsing the problem description as it is writing code. However, looking at the final Scala code it is easy to verify that all of the initial requirements hold:

```tut:invisible
trait Issue
```

```tut:book:silent
trait Publication {
  def title: String
}

trait Manuscript {
  def title: String
  def length: Int
  def author: String
}

case class Book(
  val title: String,
  val author: String,
  val length: Int
) extends Publication with Manuscript

case class Periodical(
  val title: String,
  val editor: String,
  val issues: Seq[Issue]
) extends Publication

case class Issue(
  volume: Int,
  issue: Int,
  manuscripts: Seq[Manuscript]
)
```
</div>
