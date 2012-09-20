---
layout: page
---

# Object Oriented Programming in Scala



## A simple class definition

Here is a simple Scala class definition:

{% highlight scala %}
scala> class Counter {
     |   var counter = 0
     |
     |   def add(num: Int) = {
     |     counter = counter + num
     |     counter
     |   }
     | }
defined class Counter
{% endhighlight %}

Let's look at what we have. The class is called `Counter`. We can create a new `Counter` object using the `new` operator as follows:

{% highlight scala %}
scala> val example = new Counter()
example: Counter = Counter@526a4268
{% endhighlight %}

The class has one field called `counter`. We have used the `var` keyword to define the field making it *mutable*. Because we initialise the field to the value `0`, Scala infers its type as `Int`. If we wanted to be more explicit we could equivalently write:

{% highlight scala %}
var value: Int = 0
{% endhighlight %}

Fields in Scala are *public* by default -- there is no `public` keyword. We can get and set our `counter` as follows:

{% highlight scala %}
scala> example.counter
res0: Int = 0

scala> example.counter = 5
example.counter: Int = 5

scala> example.counter
res1: Int = 5
{% endhighlight %}

We could have declared the field as `private` as follows:

{% highlight scala %}
private var counter = 0
{% endhighlight %}

Finally, our class has one method called `add`, that accepts a single argument `num` of type `Int`. The method adds `num` to the `counter` field and returns its new value:

{% highlight scala %}
scala> example.add(10)
res2: Int = 15

scala> example.add(12)
res3: Int = 27
{% endhighlight %}

While the `return` keyword is available in Scala, its use is often considered bad form. Every statement in Scala is also an expression that returns a value. Methods return the value of the last statement executed -- in this case `value` -- so explicit use of `return` is often unnecessary.

We always have to specify the types of arguments to methods, but Scala is often capable of inferring the return type. In this case, because the last expression in the method is of type `Int`, Scala also infers the return type of our method as `Int`. If we wanted to be more explicit about the return type we could equivalently write:

{% highlight scala %}
def add(num: Int): Int = {
  // And so on...
}
{% endhighlight %}

## Equivalence of fields and methods

As we noted above, the default visibility of fields and methods in Scala is *public*. Public fields are considered bad design in Java so why are they the default in Scala?

The root of the problem lies in the separation of *interface* from *implementation*. Essentially, the cost of changing the implementation of a public field in Java is high. We probably need to make the field private and hide it behind a set of method calls. This means changing a lot of code throughout our application, because calling a method in Java *looks different* than accessing or mutating a field.

Scala, by contrast, provides syntactic tools that help blur the line between method calls and direct access to fields. You can define methods that work just like accessing or mutating fields. Consider, for example, the following alternative implementation of `Counter`:

{% highlight scala %}
scala> class Counter {
     |   private var _counter = 0
     |
     |   def counter: Int = {
     |     _counter
     |   }
     |
     |   def counter_=(in: Int): Unit = {
     |     _counter = in
     |   }
     |
     |   def add(num: Int): Int = {
     |     counter = num
     |     counter
     |   }
     | }
defined class Counter
{% endhighlight %}

Here we have turned our field into a `private var` and renamed it to `_counter`. We have added two new methods, `counter` and `counter_=` to act as a public interface to the field. Each method demonstrates a syntactic feature of Scala that helps us make the change without affecting the rest of our code.

The `counter` method has no argument list. In addition to supporting methods with any number of arguments, Scala also supports methods with any number of *argument lists*. Methods with no argument lists are called with no parentheses, making them indistinguishable from direct field access:

{% highlight scala %}
scala> val example = new Counter
example: Counter = Counter@6e1f5438

scala> example.counter // method call to 'counter'
res1: Int = 0
{% endhighlight %}

The `counter_=` method demonstrates a more exotic part of Scala syntax. Any method with a single argument and a name ending in `_=` can be called in an assignment style as follows:

{% highlight scala %}
scala> example.counter = 5 // method call to 'counter_='
example.counter: Int = 5
{% endhighlight %}

Note that, despite what the console may suggest, the return value of this method call is `Unit`.

If you use Scala classes from Java, you will notice that Scala actually copiles public `var` and `val` fields to private fields and public methods. Thus, the following definitions are equivalent:

{% highlight scala %}
var foo = 0

// is equivalent to ...

private var someAnonymousVariable: Int = 0

def foo: Int = someAnonymousVariable

def foo_=(in: Int): Unit = {
  someAnonymousVariable = in
}

// and ...

val foo = 0

// is equivalent to ...

private val someAnonymousVariable: Int = 0

def foo: Int = someAnonymousVariable
{% endhighlight %}

This equivalence of fields and methods creates a natural separation of interface and implementation that greatly simplifies refactoring code compared to Java.

## Constructors

The `Counter` class used a default argumentless constructor. Let's look at a class that defines its own constructor with arguments and initialisation code. We'll use an example from 2D geometry -- a `Vec` class representing a 2D vector:

{% highlight scala %}
scala> class Vec(x: Double, y: Double) {
     |   val length = math.sqrt(x * x + y * y)
     | }
defined class Vec
{% endhighlight %}

In Scala we write the main constructor for a class in the header line. Scala creates `private var` fields for each constructor argument. We create new instances of the class the same as we do in Java:

{% highlight scala %}
scala> val example = new Vec(3.0, 4.0)
example: Vec = Vec@74184b3b

scala> example.length
res0: Double = 5.0
{% endhighlight %}

This begs a number of questions. The first being, what do we do if we want to add code to the constructor? The answer is we simply add it to the body of the class:

{% highlight scala %}
scala> class Vec(x: Double, y: Double) {
     |   println("Created a Vec: " + x + ", " + y)
     |
     |   val length = math.sqrt(x * x + y * y)
     | }
defined class Vec

scala> new Vec(3.0, 4.0)
Created a Vec: 3.0, 4.0
res1: Vec = Vec@f52d950
{% endhighlight %}

Note that `val` and `var` fields are initialised in their natural order as we pass down through the body of the class. If we refer to them before their definitions, we may get unexpected results. Here, for example, we access `length` before it has been initialised and get back `0.0`:

{% highlight scala %}
scala> class Vec(x: Double, y: Double) {
     |   println("Created a Vec: " + length)
     |
     |   val length = math.sqrt(x * x + y * y)
     | }

scala> new Vec(3.0, 4.0) // notice that the length comes out wrong
Created a Vec: 0.0
res2: Vec = Vec@6e6196fc
{% endhighlight %}

The second question we might ask is how we provide access to the private fields from the constructor? The answer: we can turn them into public fields by including `val` or `var` in the constructor as appropriate:

{% highlight scala %}
scala> class Vec(val x: Double, val y: Double) { // make x and y public
     |   val length = math.sqrt(x * x + y * y)
     | }
defined class Vec

scala> val example = new Vec(3.0, 4.0)
example: Vec = Vec@7866eb46

scala> println("x=" + example.x + ", y=" + example.y) // access x and y
x=3.0, y=4.0
{% endhighlight %}

### Auxiliary constructors

If you need multiple constructors in a class, you can write auxiliary constructors as follows:

{% highlight scala %}
scala> class Vec(x: Double, y: Double) {
     |   // Homogeneous coordinate constructor:
     |   def this(x: Double, y: Double, w: Double) = {
     |     this(x / w, y / w)
     |   }
     |
     |   val length = math.sqrt(x * x + y * y)
     | }
{% endhighlight %}

Auxiliary constructors in Scala start with `def this(...)`, and *must invoke another constructor as their first action*. This means you can't have multiple statements in an auxiliary constructor, and all auxiliary constructors must end up calling the primary constructor at the top of the class definition.

This is lot more restrictive than multiple constructors in Java, which can perform any number of arbitrary operations before choosing whether to invoke another constructor. The common pattern in Scala is to do most constructor work by providing `apply` methods in companion objects -- we'll come to this later on.

### Private constructors

Sometimes it is necessary to define a class's constructor as private. Do this by placing the `private` keyword directly after the class name:

{% highlight scala %}
scala> class Vec private(val x: Double, val y: Double) { // private constructor
     | }

scala> new Vec(3.0, 4.0)
java.lang.Error: Unexpected New
// stack trace...
{% endhighlight %}

{% comment %}
## Special methods

TODO: Complete ... the important part is introducing `apply` before the next section

 - scala makes heavy use of convenion in method names to reduce boilerplate syntax
 - we've already seen three examples of this:
    - use of single-argument methods as infix operators
    - argumentless methods for field accessor syntax
    - foo_= methods for assignment syntax
 - there are some more special method names you should be aware of
 - apply
 - update
 - unapply
 - unary_foo
{% endcomment %}

## Singleton objects

Scala does not support the concepts of static fields and methods. Instead, it makes it easy to create *singleton objects* containing non-static methods that serve the same purpose. For example, in Scala the infamous `public static void main` method from Java is written as follows:

{% highlight scala %}
object MyApp {
  def main(args: Array[String]): Unit = {
    // Insert application here...
  }
}
{% endhighlight %}

### Companion objects

A singleton with the same name as a class is called the *companion object* to that class. Due to the way companion objects and classes are compiled, they must be defined in the same file. In this example we use the console's *paste mode* to define the companions at the same time:

{% highlight scala %}
scala> :paste
// Entering paste mode (ctrl-D to finish)

class Counter(var counter: Int)

object Counter {
  // A Map of counter names to Counters:
  private var counters = Map[String, Counter]()

  def get(name: String): Counter = {
    // If a counter of this name is in the Map...
    if(counters.contains(name)) {
      // ...retrieve and return it...
      counters(name)
    } else {
      // ...otherwise create and cache it.
      val ans = new Counter(0)
      counters = counters + (name -> ans)
      ans
    }
  }
}

// Exiting paste mode, now interpreting.

defined class Counter
defined module Counter
{% endhighlight %}

If we were writing this code in Java, we would define `counters` and `get` as static members of the `Counter` class. In Scala, our singleton `Counter` object is visible in the global namespace so we can access its members directly without having a `static` keyword:

{% highlight scala %}
scala> Counter.get("a").counter
res1: Int = 0

scala> Counter.get("a").counter += 5

scala> Counter.get("a").counter
res3: Int = 5

{% endhighlight %}

### Constructor methods

A common practice in Scala is to use define an `apply` method on a companion object to construct instances of the companion class. This provides a simple workaround for the restrictions we have seen regarding auxiliary constructors.

Here we reimplement the auxiliary constructor from our `Vec` example as an `apply`. This allows us to check for the case where `w` is zero and throw an `ArithmeticException`:

{% highlight scala %}
scala> :paste
// Entering paste mode (ctrl-D to finish)

class Vec(val x: Double, val y: Double) {
  println("Created a Vec: x=" + x + ", y=" + y)
}

object Vec {
  def apply(x: Double, y: Double, w: Double): Vec = {
    if(w == 0) {
      throw new ArithmeticException("w cannot be 0")
    } else {
      new Vec(x/w, y/w)
    }
  }
}

// Exiting paste mode, now interpreting.

defined class Vec
defined module Vec

scala> Vec(6, 8, 2)
Created a Vec: x=3, y=4
res0: Vec = Vec@778b3fee

scala> Vec(1, 2, 0)
java.lang.ArithmeticException: w cannot be 0
// and so on...
{% endhighlight %}

## Using objects as modules

Scala's flexible `import` syntax allows you to import members from objects as well as packages. This is a powerful feature that lets you use objects as modules of code.

A simple example but useful example of this is importing identifiers from a singleton object. Returning to the companion object for `Counter` above, we can write the following:

{% highlight scala %}
scala> import Counter._
import Counter._

scala> get("foo")
res0: Counter = Counter@3126cb1a
{% endhighlight %}

The import statement in the first command gives us direct access to the fields and methods defined in the `Counter` singleton. This allows us to write `get("foo")` instead of `Counter.get("foo")`.

This ability to import from singletons is essentially the same as Java's *static import* feature. However, Scala's imports go way beyond this. First, you can write import statements *anywhere* in your code, which scopes the imported members like local variables. This lets you import identifiers where you need them without polluting the namespace elsewhere (a practice that becomes incredibly useful when using *implicit conversions*):

{% highlight scala %}
scala> def mmmPi: Double = {
     |   import scala.math._
     |   Pi
     | }
mmmPi: Double

scala> mmmPi
res4: Double = 3.141592653589793

scala> Pi
<console>:13: error: not found: value Pi
              Pi
              ^
{% endhighlight %}

In this example, we import the value of `Pi` from the `scala.math` package. Because the import is inside the method body for `mmmPi`, the identifier is unavailable outside the method, and our attempt to use it in our final console command causes a compilation error.

Second, you can import members from *any stable identifier in scope*. This includes packages, singletons, and any `val` or `lazy val`. This lets you use any dynamically constructed object as a module:

{% highlight scala %}
scala> val x = "abc"
x: java.lang.String = abc

scala> import x._
import x._

scala> length()
res6: Int = 3
{% endhighlight %}

In this trivial example, we create an ordinary string and import its fields and methods. We call the `length` method directly by writing `length()` instead of `x.length()`.
