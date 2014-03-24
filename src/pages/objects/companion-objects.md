---
layout: page
title: Companion Objects
---

Sometimes we want to create a method that logically belongs to a class but is independent of any particular object. In Java we would use a *static method* for this, but Scala has a much simpler solution: singleton objects.

One common use case is auxiliary constructors. Although Scala does support having multiple constructors for a single class, most Scala programmers prefer to implement additional constructors as `apply` methods on an object with the same name as the class. We refer to the object as the *companion* of the class. For example:

~~~ scala
scala> import java.util.Date
import java.util.Date

scala> :paste
// Entering paste mode (ctrl-D to finish)

class Timestamp(val seconds: Long)

object Timestamp {
  def apply(hours: Int, minutes: Int, seconds: Int) =
    new Timestamp(hours*60*60 + minutes*60 + seconds)
}
^D

// Exiting paste mode, now interpreting.

defined class Timestamp
defined module Timestamp

scala> Timestamp(1, 1, 1).seconds
res2: Long = 3661
~~~

Scala has two namespaces: a space of *type names* and a space of *value names*. This is what allows us to name the class and companion object the same thing. It is important to note that **the companion object is not an instance of the class**.

~~~ scala
scala> Timestamp // note that the type is `Timestamp.type`, not `Timestamp`
res3: Timestamp.type = Timestamp$@602b24e6
~~~

### Exercises

#### Construct-a-Person

Implement a companion object for `Person` containing an `apply` method that accepts a whole name as a single string rather than as a first and last name individually.

Here's a tip: you can split a `String` into an `Array` of components as follows:

~~~ scala
scala> val parts = "John Doe".split(" ")
parts: Array[String] = Array(John, Doe)

scala> parts(0)
res36: String = John
~~~

<div class="solution">
Here is the code:

~~~ scala
object Person {
  def apply(name: String): Person = {
    val parts = name.split(" ")
    new Person(parts(0), parts(1))
  }
}
~~~

And here it is in use:

~~~ scala
scala> Person.apply("John Doe").firstName // full method call
res6: String = John

scala> Person("John Doe").firstName // sugared apply syntax
res7: String = John
~~~
</div>
