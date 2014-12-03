## Objects as Functions

In the final exercise of the previous section, we defined a class called `Adder`:

~~~ scala
class Adder(amount: Int) {
  def add(in: Int) = in + amount
}
~~~

In the discussion we described an `Adder` as an object representing a computation---a bit like having a method that we can pass around as a value.

This is such a powerful concept that Scala has a fully blown set of language features for creating objects that behave like computations. These computational objects are called *functions*, and are the basis of **functional programming**.

### The apply method

For now we are going to look at just one of Scala's features supporting functional programming---**function application syntax**.

In Scala, by convention, an object can be "called" like a function if it has a method called `apply`. Naming a method `apply` affords us a special shortened call syntax: `foo.apply(args)` becomes `foo(args)`.

For example, let's rename the `add` method in `Adder` to `apply`:

~~~ scala
scala> class Adder(amount: Int) {
         def apply(in: Int) = in + amount
       }
defined class Adder

scala> val add3 = new Adder(3)
add3: Adder = Adder@1d4f0fb4

scala> add3(2) // shorthand for add3.apply(2)
res7: Int = 5
~~~

With this one simple trick, objects can "look" syntactically like functions. There are lots of things that we can now do now that we couldn't do with methods, including assign them to variables and pass them around as arguments.

<div class="callout callout-info">

#### Function Application Syntax

The method call `object.apply(parameter, ...)` can also be written as `object(parameter, ...)`

</div>

### Take home points

In this section we looked at **function application syntax**, which lets us "call" an object as if it is a function.

Function application syntax is available for any object defining an `apply` method.

With function application syntax, we now have first class values that behave like computations. Unlike methods, objects can be passed around as data. This takes us one step closer towards true functional programming in Scala.

### Exercises

#### When is a Function not a Function?

We'll get a chance to write some code at the end of the next section. For now we should think about an important theoretical question:

How close does function application syntax get us to creating truly reusable objects to do computations for us? What are we missing?

<div class="solution">
The main thing we're missing is **types**, which are the way we properly abstract across values.

At the moment we can define a class called `Adder` to capture the idea of adding to a number, but that code isn't properly portable across codebases---other developers need to know about our specific class to use it.

We could define a library of common function types with names like `Handler`, `Callback`, `Adder`, `BinaryAdder`, and so on, but this quickly becomes impractical.

Later this chapter we will see how Scala copes with this problem by defining a generic set of function types that we can use in a wide variety of situations.
</div>
