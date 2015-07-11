## Implicit Conversions

So far we have seen two programming patterns using implicits: *type enrichment*, which we implement using *implicit classes*, and *type classes*, which we implement using *implicit values and parameter lists*.

Scala has a third implicit mechanism called *implicit conversions* that we will cover here for completeness. Implicit conversions can be seen as a more general form of implicit classes, and can be used in a wider variety of contexts.

<div class="alert alert-warning">
**Here be dragons**

As we shall see later in this section, undisciplined use of implicit conversions can cause as many problems as it fixes for the beginning programmer. Scala even requires us to write a special import statement to silence compiler warnings resulting from the use of implicit conversions:

~~~ scala
import scala.language.implicitConversions
~~~

We recommend using implicit classes and implicit values/parameters over implicit conversions wherever possible. By sticking to the type enrichment and type class design patterns you should find very little cause to use implicit conversions in your code.

You have been warned!
</div>

### Implicit conversions

Implicit conversions are a more general form of implicit classes. We can tag any single-argument method with the `implicit` keyword to allow the compiler to implicitly use the method to perform automated conversions from one type to another:

~~~ scala
class B {
  def bar = "This is the best method ever!"
}

class A

implicit def aToB(in: A): B = new B()

new A().bar
// res: String = This is the best method ever!
~~~

Implicit classes are actually just syntactic sugar for the combination of a regular class and an implicit conversion. With an implicit class we have to define a new type as a target for the conversion; with an implicit method we can convert from any type to any other type as long as an implicit is available in scope.

### Designing with Implicit Conversions

The power of implicit conversions tends to cause problems for newer Scala developers. We can easily define very general type conversions that play strange games with the semantics of our programs:

~~~ scala
implicit def intToBoolean(int: Int) = int == 0

if(1) "yes" else "no"
// res: String = no

if(0) "yes" else "no"
// res: String = yes
~~~

This example is ridiculous, but it demonstrates the potential problems implicits can cause. `intToBoolean` could be defined in a library in a completely different part of our codebase, so how would we debug the bizarre behaviour of the `if` expressions above?

Here are some tips for designing using implicits that will prevent situations like the one above:

 - Wherever possible, stick to the type enrichment and type class programming patterns.

 - Wherever possible, use implicit classes, values, and parameter lists over implicit conversions.

 - Package implicits clearly, and bring them into scope only where you need them. We recommend using the packaging guidelines introduced earlier this chapter.

 - Avoid creating implicit conversions that convert from one general type to another general type---the more specific your types are, the less likely the implicit is to be applied incorrectly.

### Exercises

#### Implicit Class Conversion

Any implicit class can be reimplemented as a class paired with an implicit method. Re-implement the `IntOps` class from the *type enrichment* section in this way. Verify that the class still works the same way as it did before.

<div class="solution">
Here is the solution. The methods `yeah` and `times` are exactly as we implemented them previously. The only differences are the removal of the `implicit` keyword on the `class` and the addition of the `implicit def` to do the job of the implicit constructor:

~~~ scala
object IntImplicits {
  class IntOps(n: Int) {
    def yeah =
      times(_ => println("Oh yeah!"))

    def times(func: Int => Unit) =
      for(i <- 0 until n) func(i)
  }

  implicit def intToIntOps(value: Int) =
    new IntOps(value)
}
~~~

The code still works the same way it did previously. The implicit conversion is not available until we bring it into scope:

~~~ scala
5.yeah
// <console>:8: error: value yeah is not a member of Int
//               5.yeah
//                 ^
~~~

Once the conversion has been brought into scope, we can use `yeah` and `times` as usual:

~~~ scala
import IntImplicits._

5.yeah
// Oh yeah!
// Oh yeah!
// Oh yeah!
// Oh yeah!
// Oh yeah!
~~~
</div>
