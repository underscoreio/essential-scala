---
layout: page
title: Companion Objects
---

Sometimes we want to create a method that logically belongs to a class but is independent of any particular object. In Java we would use a *static method* for this, but Scala has a much simpler solution: singleton objects.

One common use case is auxiliary constructors. Although Scala does technically let us define multiple constructors for a single class, Scala programmers almost always prefer to implement additional constructors as `apply` methods on an object with the same name as the class. We refer to the object as the **companion object** of the class. For example:

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

<div class="alert alert-info">
**REPL tip:** Note our use of the `:paste` command in the transcript above. Companion objects must be defined in the same compilation unit as the classes they support. In normal application code this simply means defining them both in the same file, but on the REPL we have to enter then in one command using `:paste`. You can enter `:help` on the REPL to find out more.
</div>

Scala has two namespaces: a space of *type names* and a space of *value names*. This separation allows us to name our class and companion object the same thing without conflict.

It is important to note that **the companion object is not an instance of the class** -- it is a singleton object with its own type:

~~~ scala
scala> Timestamp // note that the type is `Timestamp.type`, not `Timestamp`
res3: Timestamp.type = Timestamp$@602b24e6
~~~

### Exercises

#### Friendly Person Factory

Implement a companion object for `Person` containing an `apply` method that accepts a whole name as a single string rather than individual first and last names.

Tip: you can split a `String` into an `Array` of components as follows:

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
