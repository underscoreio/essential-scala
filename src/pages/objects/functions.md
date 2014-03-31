---
layout: page
title: Objects as Functions
---

In the final exercise of the previous section, we defined a class called `Adder`:

~~~ scala
class Adder(amount: Int) {
  def add(in: Int) = in + amount
}
~~~

In the discussion we described `Adders` as representing the computation of adding an `amount` to a number. This is effectively like having a method that we can pass around as a value.

This is such a powerful concept that Scala has a fully blown set of language features for creating objects that behave like computations. These computational objects are called *functions*, and are the basis of **functional programming**.

## The apply method

For now we are going to look at just one of Scala's features supporting functional programming -- **function application syntax**. We'll see the rest of Scala's functional programming features at the end of the chapter.

In Scala, by convention, an object can be "called" like a function if it has a method called `apply`. Naming a method `apply` affords us a special shortened call syntax: `foo.apply(args)` becomes `foo(args)`.

For example, if we rename the `add` method in our `Adder` to `apply` we can use it as follows:

~~~ scala
scala> class Adder(amount: Int) {
     |   def apply(in: Int) = in + amount
     | }
defined class Adder

scala> val add3 = new Adder(3)
add3: Adder = Adder@1d4f0fb4

scala> add3(2)
res7: Int = 5
~~~

With this one neat trick objects can look like functions. There are lots of things that we can now do now that we couldn't do with methods, including assign them to variables and pass them as arguments. **Functions are first class values**, which means we can do anything with them that we can with other objects.

## Take home points

In this section we looked at **function application syntax**, which lets us "call" an object as if it is a function.

Function application syntax is available for any object defining an `apply` method.

With function application syntax, we now have first class values that behave like computations. Unlike methods, objects can be passed around as data. This takes us one step towards true functional programming in Scala.

## Exercises

### When is a Function not a Function?

How close does function application syntax get us to creating truly reusable objects to do computations for us? What are we missing?

<div class="solution">
The main thing we're missing is **types**, which are the way we properly abstract across values.

At the moment we can define a class called `Adder` to capture the idea of adding to a number, but that code isn't properly portable across codebases -- other developers need to know our class exists in order to use it.

Perhaps we could define a library of common function types with names like `Handler`, `Callback`, `Adder`, `BinaryAdder`, and so on, but this quickly becomes impractical.

Later this chapter we will see how Scala copes with this problem by defining a generic set of function types that we can use in a wide variety of situations.
</div>
