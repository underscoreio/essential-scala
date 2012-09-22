---
layout: page
---

# Inheritance

## Type hierarchy

Scala models all data types as part of a single type hierarchy:

    scala.Any                                         -+
     |                                                 |
     +- scala.AnyVal                                  -+
     |   |                                             |
     |   +- scala.Int                                 -+
     |   +- scala.Double                              -+
     |   +- ...all value types...                     -+
     |   |                                             |
     |   +- scala.Unit                                -+
     |                                                 |
     +- scala.AnyRef (== java.lang.Object) -+         -+
         |                                  |          |
         +- java.lang.String               -+         -+
         +- ...all java classes...         -+         -+
         |                                  |          |
         +- ...all scala classes...        -+         -+
                                            |          |
                                           scala.Null -+
                                                       |
                                                      scala.Nothing

The root type `Any` unifies all types available in Scala and Java. It has two subtypes: `AnyVal` and `AnyRef`:

`AnyVal` is the supertype of all value types such as `Int`, `Boolean` and `Double`. It is also supertype to `Unit`, Scala's equivalent of `void`.

`AnyRef` is the supertype of all reference types. It is actually an alias for Java's `java.lang.Object` -- all Scala and Java classes are descendents of `AnyRef`.

### Value types

While Java makes a distinction between primitive value types such as `int` and object wrappers such as `Int`, Scala manages all of this behind the scenes. The programmer almost never needs to worry about the difference between value types and object wrappers.

Because Scala masks all of this complexity, it provides a number of conveniences to the programmer:

 - we can use any Scala or Java collection, including basic Java arrays, to store any type of value, without worrying about boxing or unboxing;
 - we can treat any value like an object, making use of a wide variety of useful methods;
 - we can declare generic types that work with value and reference types alike.

Note, however, that we cannot *extend* value types to produce new custom classes of our own. All Scala classes must extend `AnyRef` or one of its descendants.

### Unit

An aside on the special type `Unit`. You can think of `Unit` as Scala's equivalent of the `void` keyword in Java. While the two are very similar, there is an important conceptual difference.

`void` is essentially special case keyword meaning that a method returns *no value*. In functional programming, however, we think of *every* method as returning a value. The distinction we make instead is whether a method returns a *useful* value.

`Unit` is a type that consists of precisely one value, *unit*, which is used to represent an *uninteresting* value. The literal value *unit* is written `()`, although it does not often appear in code.

When we write a method that does not return an interesting value, we define it as returning `Unit`:

{% highlight scala %}
def setFoo(newFoo: Int): Unit = {
  foo = newFoo
}
{% endhighlight %}

Scala recognises this and implicitly inserts a literal `()` as the last expression in the method:

{% highlight scala %}
def setFoo(newFoo: Int): Unit = {
  foo = newFoo
  ()
}
{% endhighlight %}

This means the return value of the method is always `()` -- our last expression is always ignored.

While the distinction between `void` and `Unit` may seem trivial, it is in fact essential for writing things like generic functions. For example, in Scala this is valid code:

{% highlight scala %}
scala> class Function[A, B] {
     |   def apply(a: A): B
     | }
defined class Function

scala> class PrintFunction extends Function[String, Unit] {
     |   def apply(str: String): Unit = println(str)
     | }
defined class PrintFunction

scala> (new PrintFunction).apply("Hello world!")
Hello world!
{% endhighlight %}

whereas the equivalent in Java is not:

{% highlight scala %}
interface Function<A, B> {
   B apply(Arg arg);
}

// The use of "void" on the next line is illegal:
class PrintFunction implements Function<String, void> {
  void apply(String str) {
    System.out.println(str);
  }
}
{% endhighlight %}

The difference is that `Unit` is an actual type with an actual value, instead of just a keyword.

### Null and Nothing

There are two other special types in Scala's type hierarchy, `Null` and `Nothing`, that don't play directly into inheritance but are worth mentioning as a further aside.

`Null` is the type of the value `null`, and is defined as being a *subtype* of everything that extends `AnyRef`. This means that any reference to an object in Scala can be assigned the value `null` without complaint from the type checker:

{% highlight scala %}
scala> var x: String = null
x: String = null

scala> var y: Int = null
<console>:7: error: type mismatch;
 found   : Null(null)
 required: Int
{% endhighlight %}

`Nothing` is the type of all `throw` statements (note - the type of the *statement*, not the exception thrown), and is defined as being a subtype of *all other types*. Again, this is for the type checker's sake -- it allows any method to terminate with a `throw` without us having to annotate it with a special return type:

{% highlight scala %}
scala> def x(a: Int) = throw new Exception("Foo")
x: (a: Int)Nothing

scala> def y(a: Int) = if(a == 0) throw new Exception("Zero!") else a
y: (a: Int)Int
{% endhighlight %}

## Class inheritance

Now we get into the meat of inheritance: the `extends` keyword. As we saw above, all classes in Scala are descendants of the type `AnyRef`, which is an alias for `java.lang.Object`. If we omit the `extends` clause from a class definition, Scala implicitly inserts `extends AnyRef` instead:

{% highlight scala %}
scala> class Circle(var radius: Double) { /* ... extends AnyRef */
     |   def area = math.Pi * radius * radius
     |   def diameter = 2 * radius
     | }
defined class Circle
{% endhighlight %}

As in Java, we can use `extends` to select a different superclass instead:

{% highlight scala %}
scala> class Sphere(radius: Double) extends Circle(radius) {
     |   override def area = 4 * math.Pi * radius * radius
     |   def volume = 4 * math.Pi * radius * radius * radius / 3
     | }
defined class Sphere
{% endhighlight %}

The usual extension semantics apply as in Java. Here, `Sphere` inherits all of `Circle's` fields and methods. It adds its own method, `volume`, and replaces the implementation of `area`:

{% highlight scala %}
scala> val c = new Circle(10)
c: Circle = Circle@6d13722b

scala> val s = new Sphere(10)
s: Sphere = Sphere@e208506

scala> List(c.area, c.diameter)
res1: List[Double] = List(314.1592653589793, 20.0)

scala> List(s.area, s.diameter, s.volume)
res2: List[Double] = List(1256.6370614359173, 20.0, 4188.790204786391)
{% endhighlight %}

### Overriding field and methods

Unlike Java, Scala requires us to be *explicit* whenever we override a concrete definition from a superclass. The `override` keyword on the `area` method in `Sphere` is not just a courtesy to the reader -- it is a mandatory.

Note that tt is possible to override an argumentless `def` with a `val` or `var` of the same type:

{% highlight scala %}
scala> class Bike {
     |   def wheels: Int = 2
     | }

scala> class Tricycle extends Bike {
     |   val wheels = 2
     | }
{% endhighlight %}

This allows us to override a method using a field, which is exceptionally useful when dealing with *abstract methods* as we shall see below.

### Constructors and super-constructors

Scala's compact constructor syntax was introduced in an earlier section. In that section we saw how constructor arguments are declared in the first line of the class definition: right after the class name. We declare arguments for the superclass constructor in the same way: right after the superclass name. In the example above, the constructor for `Sphere` accepts a single argument, `radius`, and passes it straight to the constructor for `Circle`:

{% highlight scala %}
class Sphere(radius: Double) extends Circle(radius)
{% endhighlight %}

The first action performed by the constructor for any class is to call the constructor of its superclass. This means constructors are executed in inheritance order, from superclass to subclass.

Note that the constructor for `Circle` uses the `var` keyword in its argument definition whereas the constructor for `Sphere` does not. If we expand the constructor for `Circle` we can see why:

{% highlight scala %}
class Circle(somePrivateVariable: Double) {
  def radius = somePrivateVariable
  def radius_=(newRadius: Double) = {
    somePrivateVariable = newRadius
  }

  // ...
}
{% endhighlight %}

The `var` keyword effectively creates a public accessor and public mutator for `radius` in the body of the class. Because the accessor and mutator are defined in `Circle`, there is no need to define them again in `Sphere`. We can therefore omit the `var` keyword in `Sphere`.

### Class membership and casting

Java's *instanceof* keyword and *casting* syntax are replaced in Scala by two methods of the `Any` class: `isInstanceOf` and `asInstanceOf`.

`isInstanceOf` is used to test for class membership. It accepts a single type argument and returns `true` if the callee is a member of that type:

{% highlight scala %}
scala> List(c.isInstanceOf[Any],
            c.isInstanceOf[AnyRef],
            c.isInstanceOf[Circle],
            c.isInstanceOf[Sphere])
res3: List[Boolean] = List(true, true, true, false)
{% endhighlight %}

Because `isInstanceOf` is defined in the `Any` type, it is available when working with value types as well as reference types:

{% highlight scala %}
scala> List(1.isInstanceOf[Any],
            1.isInstanceOf[AnyVal],
            1.isInstanceOf[Int],
            1.isInstanceOf[Double])
res4: List[Boolean] = List(true, true, true, false)
{% endhighlight %}

The `asInstanceOf` method is used to perform explicit casting. Use of this method should be avoided wherever possible as it can cause `ClassCastExceptions` at runtime. However, in some situations -- for example when interfacing with Java code or working around type erasure -- its use is unavoidable:

{% highlight scala %}
scala> c.asInstanceOf[AnyRef]
res4: AnyRef = Circle@6d13722b

scala> c.asInstanceOf[Sphere]
java.lang.ClassCastException: Circle cannot be cast to Sphere
  // ...stack trace...
{% endhighlight %}

Scala offers a safe way to do casting using the `:` operator (aka *type ascription*). Uses of `:` will only compile in situations where Scala can be 100% sure that the types work out. The difference is that `asInstanceOf` fails at runtime, whereas `:` fails at compile time:

{% highlight scala %}
scala> s : Circle
res12: Circle = Sphere@e208506

scala> c : Sphere
<console>:11: error: type mismatch;
 found   : Circle
 required: Sphere
              c : Sphere
              ^
{% endhighlight %}

## Abstract classes

Scala's `abstract classes` are similar to Java's in that:

 - abstract classes can contain *abstract* (i.e. unimplemented) fields and methods as well as concrete ones;
 - abstract classes cannot be directly instantiated.

We declare a class as abstract using the `abstract` keyword. We declare abstract fields and methods simply by omitting their definitions:

{% highlight scala %}
scala> abstract class Animal {
     |   val species: String // abstract immutable field
     |   var age: Int        // abstract mutable field
     |   def makeNoise: Unit // abstract method
     | }
{% endhighlight %}

Abstract fields and methods can be *implemented* in a subclass to provide a concrete class that can be instantiated:

{% highlight scala %}
scala> class Dog(val species: String) extends Animal {
     |   var age: Int = 0
     |   def makeNoise = println("Bark!")
     | }
{% endhighlight %}

In the example, we override `age` and `makeNoise` in the class body and `species` using the shorthand field definition syntax in the constructor. We don't need to use the `override` keyword because none of the members are overriding concrete definitions from the superclass.

Note that, as with overriding, it is possible to implement an abstract argumentless `def` with a concrete `val` or `var` of the same type:

{% highlight scala %}
scala> abstract class Vehicle {
     |   def wheels: Int
     | }

scala> class Bicycle extends Vehicle {
     |   val wheels = 2
     | }
{% endhighlight %}

This makes `def` the most flexible type of abstract definition as it can be implemented using a method or an immutable or mutable field. It is usually considered best practice to declare abstract members using `def` unless there is a specific reason to do otherwise.

## Traits

Scala replaces Java's *interfaces* with a new type of inheritance construct: *traits*. Traits provide a *mixin-based* form of multiple inheritence that avoids some of the problems with ambiguity and verbosity found in other programming languages.

A trait is a class-like structure that can contain a mixture of abstract and concrete definitions:

{% highlight scala %}
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
{% endhighlight %}

A trait can be *mixed in* to an existing class using the `with` operator. This process produces a new class with similar overriding and extension semantics to subclassing:

{% highlight scala %}
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
{% endhighlight %}

Because the result of mixing is a class, multiple traits can be mixed in in sequence in a single class definition:

{% highlight scala %}
scala> trait C
defined trait C

scala> trait D
defined trait D

scala> class Z extends J with C with D
defiend class Z
{% endhighlight %}

Methods are resolved by *linearizing* the classes and traits involved. So, for example, the linearization of Z above would be:

    Z -> D -> C -> J -> B -> A -> AnyRef -> Any

Any method call is resolved by first searching for the method in the body of Z, then in D, then in C, and so on up the chain until the method is located.

### Examples: extending classes with traits

Here is an example of simple single-trait inheritance:

{% highlight scala %}
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
{% endhighlight %}

`NoisyLogger` fills in the abstract `loggingEnabled` method from `Logger` and `Noisy` creates a concrete class from `NoisyLogger`, which we can instantiate and use:

{% highlight scala %}
scala> (new Noisy).saySomething
OHAI! I CAN HAS SCREEN SPACE?
{% endhighlight %}

We can expand on this to provide an example of inheritance from multiple traits:

{% highlight scala %}
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
{% endhighlight %}

`Unreliable` blends concrete functionality from `FiftyFifty` and `Logger`.

### Shorthand syntax: extending `AnyRef` with traits

We have seen how Scala implicitly extends `AnyRef` when we can omit the `extends` clause from the class header. The same shorthand syntax is also available when we are mixing in traits. For example, we can rewrite the header of `Unreliable` above as follows:

{% highlight scala %}
class Unreliable extends Logger with FiftyFifty { /* ... */ }
{% endhighlight %}

Note that this shorthand syntax is misleading -- it seems to imply that `Logger` is a class, when in fact it is a trait. Don't be fooled -- traits cannot be extended directly -- they must first be mixed into a class. In reality, the word `Logger` in this context is shorthand for `AnyRef with Logger`, which makes sense because it is a class. This distinction is subtle but important as it gives us a clean conceptual model of how class and trait composition works.

### Extending traits

Sometimes it only makes sense to mix a trait into a class that already has certain features. For example:

{% highlight scala %}
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
{% endhighlight %}

The `Loud` trait does not compile here because Scala cannot guarantee that the class it is mixed into will have the method `saySomething`. We can enforce this constraint using an `extends` clause:

{% highlight scala %}
scala> trait Loud extends Vocal {
     |   def shout: Unit = {
     |     saySomething.toUpperCase + "!"
     |   }
     | }
defined trait Loud
{% endhighlight %}

Whenever we mix the `Loud` trait into a class, the compiler checks to see if `Vocal` is already in the linearization of that class. If it is not, the compiler inserts it just before `Loud`. This means we can extend `Vocal` as follows:

{% highlight scala %}
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
{% endhighlight %}

In fact, every trait actually extends a class and zero or more other traits -- the `extends` syntax simply has the same `AnyRef` shorthand that we have already seen in class definitions. Classes and mixins are added to the linearization only when they are necessary to fulfill these constraints:

{% highlight scala %}
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

{% endhighlight %}

In this example, the final value of `Counter` is 3. This shows that, despite the redundancy in the way the inheritance constraints are written on `B` through `E`, each trait is only mixed in once to produce the final product.

### When to use traits

Traits in Scala are very flexible for defining reusable blocks of code. However, they have a some drawbacks. For example traits cannot take constructor arguments and cannot be used in Java code. If you find yourself unsure whether to use a trait, class, or abstract class, consider the following guidelines:

 - if you don't need to inheritance, create a concrete class;
 - if you need to use inheritance, create a trait;
 - if you need to use your code from Java, create a class or abstract class;
 - if you're unsure, create a trait - it will give you the most options if you need to rewrite it later.

{% comment %}

## Self types

{% endcomment %}
TODO: Complete
