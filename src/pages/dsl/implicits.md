---
layout: page
---

# Open Extension of Classes

In our DSLs we will often want to use existing classes, such as `Int` and `String`, in ways that were not envisioned when they were created. Scala has a powerful way of adding methods to a class in a controlled manner, called implicits. In this section have our first introduction to implicits. In later sections we'll see some common use cases.

## Multiple parameter lists

Before we can discuss implicits we need to know about multiple parameter lists. In addition to supporting any number of parameters, methods and functions in Scala can have any number of *parameter lists*. For example:

~~~ scala
scala> def add(a: Int, b: Int)(c: Int, d: Int): Int = {
     |   a + b + c + d
     | }
add: (a: Int, b: Int)(c: Int, d: Int)Int

scala> add(1, 2)(3, 4)
res0: Int = 10
~~~

There are a number of reasons why this is useful: it allows for more flexibility when assigning default values to optional arguments, it provides support for *currying*, and it provides support for *implicit argument lists*. This section covers the last of these three.

{% comment %}
TODO: Supply examples of currying and default values?
{% endcomment %}

## Implicit parameter lists

We can add the keyword `implicit` to the beginning of a parameter list to allow the compiler to implicitly insert arguments to our method calls. For example:

~~~ scala
scala> class User(val name: String)
defined class User

scala> def prompt(directory: String)(implicit user: User): String = {
     |   user.name + ":" + directory + "$"
     | }
prompt: (directory: String)(implicit user: User)String
~~~

When we call prompt, we have the option of supplying all the necessary arguments or allowing the compiler to supply those marked as `implicit`:

~~~ scala
scala> prompt("~")(new User("john"))
res1: String = john:~$
~~~

If we omit the implicit arguments from the method call, the compiler will search for values to insert in their place. It searches local scope (roughly - see below for caveats) for `vals` and argumentless `defs` that have been declared with the `implicit` keyword:

~~~ scala
scala> implicit def currentUser: User = new User("dave")
currentUser: User

scala> prompt("~") // compiler expands this to: prompt("~")(currentUser)
res2: String = dave:~$
~~~

### Rules for implicit resolution

When searching for implicit values, the compiler is restricted by a number of precise rules:

 1. **Marking Rule:** Only definitions marked `implicit` are available.
 2. **Scope Rule:** An implicit must be in *implicit scope* (see below).
 3. **Non-Ambiguity Rule:** An implicit can only be used if there are no other applicable implicits in scope.

### Implicit scope

 - Must be in scope as a single identifier (i.e. not a.b)
 - Except the compiler will look for definitions in the companion objects for the source and target types
   for conversion.


## Implicit conversions

One of the most basic uses for implicits is as *implicit conversions*. The process works like this: Say we have a class `Foo` with a method `bar`, and a class `Baz` without that method. We would like `Baz` to have the same `bar` method as `Foo` but we don't have access to the source of `Baz` to change it.

~~~ scala
scala> class Foo {
     |   def bar = "This is the best method ever!"
     | }
defined class Foo

scala> class Baz {}
defined class Baz

scala> new Baz().bar // Sad times
<console>:9: error: value bar is not a member of Baz
              new Baz().bar // Sad times
                        ^
~~~

We can solve this problem by creating an implicit function or method from `Baz` to `Foo`. Now if we try to call the `bar` method the Scala compiler will insert a call to this function and then call `bar` on the result.

~~~ scala
scala> implicit def bazToFoo(in: Baz):Foo = new Foo()
bazToFoo: (in: Baz)Foo

scala> new Baz().bar
res7: java.lang.String = This is the best method ever!
~~~

This pattern is sometimes called pimping, after a [blog post](http://www.artima.com/weblogs/viewpost.jsp?thread=179766) by Martin Odersky.

### Rules for implicit conversion resolution

In addition to the rules for implicit resolution above, use of implicit conversions is subject to the following additional rules:

 4. **One-at-a-time Rule:** Only a single implicit conversion can be used for any given situation
    (i.e. the compiler does not search for multiple conversions to resolve a conflict).
 5. **Explicits-First Rule:** Whenever code type checks as it is written, no implicits are attempted.

### Good practice for implicit conversions

Implicit conversions can easily make code difficult to understand, particularly if the user is unaware they are using implicit conversions in their code. Here are some good practices for managing implicits:

- Separate implicits to their own trait, so the user must explicitly extend that trait to access the implicits.
- It is often useful to provide a companion object that also contains the implicits. This companion object can be imported in a limited scope to get around ambiguities or to make code clearer.
- Use a consistent naming convention for the classes you convert to. A common convention is to suffix the class with `W`, meaning wide. For example, if you extend `Int` with new methods you might put those methods in a class `IntW`.


## Exercise

Extend `Int` with a method called `times` that executes the body `n` times, where `n` is an `Int`. Bonus marks for using a call-by-name parameter. For example, I should be able to write `5 times println("Hi!")` and have `Hi!` printed five times.
