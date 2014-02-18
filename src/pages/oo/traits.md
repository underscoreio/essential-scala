---
layout: page
---

# Traits

Scala replaces Java's *interfaces* with a new type of inheritance construct: *traits*. Traits provide a *mixin-based* form of multiple inheritence that avoids some of the problems with ambiguity and verbosity found in other programming languages.

A trait is a class-like structure that can contain a mixture of abstract and concrete definitions:

~~~ scala
scala> trait Logger {
     |   def loggingEnabled: Boolean // abstract definition
     |
     |   def log(str: String) = {    // concrete definition
     |     if(loggingEnabled) {
     |       println(str)
     |     }
     |   }
     | }
defined trait Logger
~~~

A trait can be *mixed in* to an existing class using the `with` operator. This process produces a new class with similar overriding and extension semantics to subclassing:

~~~ scala
scala> class A
defined class A

scala> trait B
defined trait B

scala> class J extends A with B
defined class J

scala> val j = new J
j: J = J@15dbac11

scala> List(j.isInstanceOf[A], j.isInstanceOf[B])
res0: List[Boolean] = List(true, true)
~~~

Because the result of mixing is a class, multiple traits can be mixed in in sequence in a single class definition:

~~~ scala
scala> trait C
defined trait C

scala> trait D
defined trait D

scala> class Z extends J with C with D
defiend class Z
~~~

Methods are resolved by *linearizing* the classes and traits involved. So, for example, the linearization of Z above would be:

    Z -> D -> C -> J -> B -> A -> AnyRef -> Any

Any method call is resolved by first searching for the method in the body of Z, then in D, then in C, and so on up the chain until the method is located.

### Examples: extending classes with traits

Here is an example of simple single-trait inheritance:

~~~ scala
scala> trait NoisyLogger extends AnyRef with Logger {
     |   def loggingEnabled = true
     | }
defined trait NoisyLogger

scala> class Noisy extends AnyRef with NoisyLogger {
     |   def saySomething = {
     |     log("OHAI! I CAN HAS SCREEN SPACE?")
     |   }
     | }
defined class Noisy
~~~

`NoisyLogger` fills in the abstract `loggingEnabled` method from `Logger` and `Noisy` creates a concrete class from `NoisyLogger`, which we can instantiate and use:

~~~ scala
scala> (new Noisy).saySomething
OHAI! I CAN HAS SCREEN SPACE?
~~~

We can expand on this to provide an example of inheritance from multiple traits:

~~~ scala
scala> trait FiftyFifty {
     |   def flipCoin: Boolean = {
     |     math.random > .5
     |   }
     | }

scala> class Unreliable extends AnyRef with Logger with FiftyFifty {
     |   def loggingEnabled = flipCoin
     |
     |   def saySomething = {
     |     log("There's a 50% chance you won't see this message.")
     |   }
     | }
~~~

`Unreliable` blends concrete functionality from `FiftyFifty` and `Logger`.

### Shorthand syntax: extending `AnyRef` with traits

We have seen how Scala implicitly extends `AnyRef` when we can omit the `extends` clause from the class header. The same shorthand syntax is also available when we are mixing in traits. For example, we can rewrite the header of `Unreliable` above as follows:

~~~ scala
class Unreliable extends Logger with FiftyFifty { /* ... */ }
~~~

Note that this shorthand syntax is misleading -- it seems to imply that `Logger` is a class, when in fact it is a trait. Don't be fooled -- traits cannot be extended directly -- they must first be mixed into a class. In reality, the word `Logger` in this context is shorthand for `AnyRef with Logger`, which makes sense because it is a class. This distinction is subtle but important as it gives us a clean conceptual model of how class and trait composition works.

### Extending traits

Sometimes it only makes sense to mix a trait into a class that already has certain features. For example:

~~~ scala
scala> trait Vocal {
     |   def saySomething: String
     | }
defined trait Vocal

scala> trait Loud {
     |   def shout: String = {
     |     saySomething.toUpperCase + "!"
     |   }
     | }
<console>:9: error: not found: value saySomething
           saySomething.toUpperCase
~~~

The `Loud` trait does not compile here because Scala cannot guarantee that the class it is mixed into will have the method `saySomething`. We can enforce this constraint using an `extends` clause:

~~~ scala
scala> trait Loud extends Vocal {
     |   def shout: Unit = {
     |     saySomething.toUpperCase + "!"
     |   }
     | }
defined trait Loud
~~~

Whenever we mix the `Loud` trait into a class, the compiler checks to see if `Vocal` is already in the linearization of that class. If it is not, the compiler inserts it just before `Loud`. This means we can extend `Vocal` as follows:

~~~ scala
scala> class Dog extends Loud
<console>:8: error: class Dog needs to be abstract, since method saySomething in trait Vocal of type => String is not defined

scala> class Dog extends Loud {
     |   def saySomething = "Bark"
     | }
defined class Dog

scala> val d = new Dog
d: Dog = Dog@5ed63a62

scala> List(d.isInstanceOf[Vocal], d.isInstanceOf[Loud]
res1: List[Boolean] = List(true, true)

scala> d.shout
res2: String = BARK!
~~~

In fact, every trait actually extends a class and zero or more other traits -- the `extends` syntax simply has the same `AnyRef` shorthand that we have already seen in class definitions. Classes and mixins are added to the linearization only when they are necessary to fulfill these constraints:

~~~ scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

class A { var counter = 0 }
trait B extends A { counter += 1 }
trait C extends B { counter += 1 }
trait D extends C { counter += 1 }
class E extends A with B with C with D
(new E).counter

// Exiting paste mode, now interpreting.

defined class A
defined trait B
defined trait C
defined trait D
defined class E
res3: Int = 3

~~~

In this example, the final value of `Counter` is 3. This shows that, despite the redundancy in the way the inheritance constraints are written on `B` through `E`, each trait is only mixed in once to produce the final product.

### When to use traits

Traits in Scala are very flexible for defining reusable blocks of code. However, they have a some drawbacks. For example traits cannot take constructor arguments and cannot be used in Java code. If you find yourself unsure whether to use a trait, class, or abstract class, consider the following guidelines:

 - if you don't need to inheritance, create a concrete class;
 - if you need to use inheritance, create a trait;
 - if you need to use your code from Java, create a class or abstract class;
 - if you're unsure, create a trait - it will give you the most options if you need to rewrite it later.

## Self types

Sometime we want to provide a trait that adds additional functionality to another base trait. We've seen how we can implement this by extending both traits. This makes the extension trait a sub-type of the base trait, which may not be sensible. We can instead express this dependency using a self type. The self type says that the extension trait *requires* the base trait but not that the extension trait *is a* base trait.

Here's how we can implement the example above using a self type.

~~~ scala
scala> trait Vocal {
     |   def saySomething: String
     | }
defined trait Vocal

scala> trait Loud {
     |   self: Vocal =>
     |
     |   def shout: String = {
     |     self.saySomething.toUpperCase + "!"
     |   }
     | }
defined trait Loud
~~~

Now when we implement `Loud` we must also implement `Vocal`.

~~~ scala
scala> class Dog extends Loud {
     |   def saySomething = "Bark"
     | }
<console>:34: error: illegal inheritance;
 self-type Dog does not conform to Loud's selftype Loud with Vocal
       class Dog extends Loud {
                         ^

scala> class Dog extends Vocal with Loud {
     |   def saySomething = "Bark"
     | }
defined class Dog

scala> new Dog().shout
res13: String = BARK!
~~~
