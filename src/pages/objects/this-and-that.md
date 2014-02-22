---
layout: page
title: This and That
---

In the previous section we looked at data that could be one of a number of choices. In this section we will look at data that is all of a number of components, sometimes called a **product type**.

Returning to our analytics example, our data collection servers will have a number of different roles. In addition to collecting the data, they must report health metrics (load, response time, and so on). They probably have some authorization checks, to ensure that only authorized users can submit data. We could build this all as one service but it is to break them into individual components. This allows greater reuse and also making testing easier.

## Traits as Mixins

We achieve composition in Scala by separating each component into its own trait and mixing in the traits we want to use. The syntax is to write `extends` for the first trait, and `with` for each following trait. For example, `A extends B with C with D`. With the analytics example we might have definitions like these:


~~~ scala
trait DataCollector {
  def record(key: ApiKey, event: Event): Unit
}

trait MetricsCollector {
  def metric(key: String, type: EventType, value: MetricValue): Unit
  def get(key: String): MetricValue
}

trait Authorizer {
  def authorized(key: ApiKey): Boolean
}

trait AnalyticsService extends DataCollector with MetricsCollector wtih Authorizer {
}
~~~


## Is-a vs Has-a

A trait or class is a subtype of every trait it extends. This means that if `A extends B`, `A` **is a** `B` and may be used wherever a `B` is expected. An `AnalyticsService` is a `DataCollector` as it performs that function. Don't confuse an is-a relationship with a **has a** relationship. A book has a publisher but is not a publisher itself, so we would not mixin a `Publisher` trait to a `Book`.

## Exercise

We're going to create a more complicated model for publisher data, which we first looked at in the last section. A publication is a book or a periodical. A book has an author while a periodical has an editor. Periodicals have many issues, which have a volume and issue number.

A manuscript is a document of a certain length written by an author. A book is a manuscript, but an issue of a periodical contains a sequence of manuscripts.

Tip: a sequence of type `A` has type `Seq[A]`.

<div class="solution">
~~~ scala
trait Manuscript {
  def title: String
  def length: Int
  def author: String
}

case class Issue(val volume: Int, val issue: Int, manuscripts: Seq[Manuscript])

trait Publication {
  def title: String
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
~~~
</div>

## Stacking Traits and Trait Linearization

Stacking traits is one nice pattern for using the ability to extend multiple traits. As discussed above we want to record a number of metrics for our analytics service. We can implement these metrics as traits that stack onto a base `DataCollector`, which makes it very easy to add new metrics. In the code below I've used a few new features of Scala which I'll explain below.

~~~ scala
type ApiKey = String
type Event = Int

trait DataCollector {
  def record(key: ApiKey, event: Event): Unit =
    println(s"Recorded data ${key} / ${event}")
}

trait ResponseTimeCollector extends DataCollector {

  override def record(key: ApiKey, event: Event): Unit = {
    val start = System.currentTimeMillis()
    val result = super.record(key, event)
    val end = System.currentTimeMillis()
    println(s"Event processing ${end - start}ms")
    result
  }
}

trait HitCountCollector extends DataCollector {
  override def record(key: ApiKey, event: Event): Unit = {
    println("Recorded a hit")
    super.record(key, event)
  }
}
~~~

When we create a concrete `DataCollector` we extend all the metric traits we want to include.

~~~
scala> case class RealDataCollector() extends DataCollector with ResponseTimeCollector with HitCountCollector
defined class RealDataCollector

scala> RealDataCollector().record("key", 1)
Recorded a hit
Recorded data key / 1
Event processing 0ms
~~~

Two quick new features introduced above:

* We can define a type alias using the syntax `type name = typeDefinition`. This gives us another name to use for an existing type. I've done this above to give more descriptive names to the types in use while still allowing us to run the code without adding extra overhead.

* We can perform **string interpolation** by prefixing a string with s and inserting expressions using `${expression}`. This makes writing strings much simpler.

Now back to trait stacking.

Scala allows a trait to modify methods it inherits just like in Java. We use the `override` keyword to indicate that we are changing the behaviour of a method we have inherited, and `super` to call the method we are overriding. This can lead to interesting interactions if we have many traits that each override a particular method. Consider the following code. What happens when we call `foo` on an instance of `Example`?

~~~ scala
trait Fooable {
  def foo: String
}

trait A extends Fooable {
  override def foo: String = "A"
}

trait B extends Fooable {
  override def foo: String = "B"
}

trait C extends Fooable {
  override def foo: String = "C"
}

case class Example() extends A with B with C
~~~

The simple way to tell is to ask the REPL:

~~~
scala> Example().foo
res1: String = C
~~~

Ambiguity between traits is resolved using **linearization**. Essentially traits are searched from the last one extended to the first looking for a method. So in the above example the search order is `Example -> C -> B -> A -> AnyRef -> Any`. (Extending `AnyRef` is implicit in an object definition.)

Designing with linearization is simple: don't. The metrics example is fine because the traits just add to the existing functionality and the order they are stacked doesn't matter. However if you depend on the order in which traits are stacked you are doing something wrong and should reconsider your design.


## Self Types

Sometime we want to provide a trait that adds additional functionality to another base trait. We've seen how we can implement this by extending both traits. This makes the extension trait a subtype of the base trait, which may not be sensible. We can instead express this dependency using a self type. The self type says that the extension trait *requires* the base trait but not that the extension trait *is a* base trait.

Take the `DataCollector` example we looked at above. When writing `DataCollector` we will probably have a dependency on `Authorizer`, for the obvious reason that we'll need to put some authorization checks in our code. A `DataCollector` is not an `Authorizer` but it does depend on one. We can express this using a self type.

~~~ scala
trait Authorizer {
  def authorized(key: ApiKey): Boolean
}

trait DataCollector { self: Authorizer =>
  def record(key: ApiKey, event: Event): Unit = {
    if(self.authorized(key)) {
      // Collect data
    } else {
      // Not authorized to collect data
    }
  }
}
~~~
