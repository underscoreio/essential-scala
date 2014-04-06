---
layout: page
title: Type Enrichment
---

Type enrichment, often referred to colloquially as *"pimping"*, allows us to **augment existing classes with extra functionality**. For example, suppose we have a method called `numberOfVowels`:

~~~ scala
scala> def numberOfVowels(str: String) =
     |   str.filter(Seq('a', 'e', 'i', 'o', 'u').contains(_)).length
numberOfVowels: (str: String)Int

scala> numberOfVowels("the quick brown fox")
res0: Int = 5
~~~

This is a useful method that we use all the time. It would be great if `numberOfVowels` was a built-in method of `String` so we could write `string.numberOfVowels`, but unfortunately that is not the case. We don't have access to the source code for `String`, so there is no way we can directly add the method ourselves.

Fortunately, Scala has a feature called called **implicit classes** that allow us to add new functionality to an existing class without editing its source code. This is a similar concept to *categories* in Objective C or *extension methods* in C#, but the implementation is different in each case.

Implicit classes are one of three flavours of Scala's implicit mechanic. The other two -- *implicit values* and *implicit conversions* -- are useful in other ways. We shall see these later this chapter.

## Implicit Classes

An class that adds our `numberOfVowels` method to `String`:

~~~ scala
scala> class ExtraStringMethods(str: String) {
     |   val vowels = Seq('a', 'e', 'i', 'o', 'u')
     |
     |   def numberOfVowels =
     |     str.toList.filter(vowels contains _).length
     | }
defined class ExtraStringMethods
~~~

We can use this to wrap up our `String` and gain access to our new method:

~~~ scala
new ExtraStringMethods("the quick brown fox").numberOfVowels
~~~

Writing `new ExtraStringMethods` every time we want to use `numberOfVowels` is unwieldy. However, if we tag our class with the `implicit` keyword, we give Scala the ability to insert the constructor call automatically into our code:

~~~ scala
scala> implicit class ExtraStringMethods(str: String) { /* ... */ }
defined class ExtraStringMethods

scala> "the quick brown fox".numberOfVowels
res1: Int = 5
~~~

When the compiler process our call to `numberOfVowels`, it interprets it as a type error because there is no such method in `String`. Rather than give up, the compiler attempts to fix the error using implicits. It finds `ExtraStringMethods`, which is an implicit class that is capable of transforming our `String` into something with a `numberOfVowels` method. The compiler inserts an invisible constructor call, and our code type checks correctly.

## Implicit Resolution Rules

Scala has three types of implicits -- implicit classes, implicit values, and implicit conversions. Each works in the same way -- the compiler detects a type error in our code, locates a matching implicit, and applies it to fix the error. This is a powerful mechanism, but we need to control it very carefully to prevent the compiler changing our code in ways we don't expect. For this reason, there is a strict set of **implicit resolution rules** that we can use to dictate the compiler's behaviour:

 1. **Explicits first rule** -- if the code already type checks, the compiler ignores implicits altogether;
 2. **Marking rule** -- the compiler only uses definitions marked with the `implicit` keyword;
 3. **Scope rule** -- the compiler only uses definitions that are *in scope* at the current location in the code (see below);
 4. **Non-ambiguity rule** -- the compiler only applies an implicit if it is the only candidate available;
 5. **One-at-a-time rule** -- the compiler never chains implicits together to fix type errors -- doing so would drastically increase compile times;

Note that the name of the implicit doesn't come into play in this process.

### Implicit Scope

The *scope rule* of implicit resolution uses a special set of scoping rules that allow us to package implicits in useful ways. These rules, collectively reverred to as **implicit scope**, form a search path that the compiler uses to locate implicits:

 1. **Local scope** -- First look locally for any identifier that is tagged as `implicit`. This must be a single identifier (i.e. `a`, not `a.b`), and can be defined locally or in the surrounding class, object, or trait, or `imported` from elsewhere.

 2. **Companion objects** -- If an implicit cannot be found locally, the compiler looks in the companion objects of types involved in the type error. Will see more of this rule in the next section.

## Packaging Implicit Classes

Implicits, including implicit classes, **cannot be defined at the top level**. They must be wrapped in an outer trait, class, or singleton object. The typical way of packaging an implicit class is to define it inside a trait called `SomethingImplicits` and extend that trait to create a singleton of the same name:

~~~ scala
trait VowelImplicits {
  implicit class VowelOps(str: String) {
    val vowels = Seq('a', 'e', 'i', 'o', 'u')
     def numberOfVowels =
       str.toList.filter(vowels contains _).length
  }
}

object VowelImplicits extends VowelImplicits
~~~

This gives developers two convenient ways of using our code:

 1. quickly bring our implicit into scope via the singleton object using an `import`:

    ~~~ scala
    // `VowelOps` is not in scope here

    def testMethod = {
      import VowelImplicits._

      // `VowelOps` is in scope here

      "the quick brown fox".numberOfVowels
    }

    // `VowelOps` is no longer in scope here
    ~~~

 2. stack our trait with a set of other traits to produce a library of implicits that can be brought into scope using inheritance or an `import`:

    ~~~ scala
    object AllTheImplicits extends VowelImplicits
      with MoreImplicits
      with YetMoreImplicits

    import AllTheImplicits._

    // `VowelOps` is in scope here
    // along with other implicit classes
    ~~~

<div class="alert alert-info">
**Implicits tip:** Some Scala developers dislike implicits because they can be hard to debug. The reason for this is that an implicit definition at one point in our codebase can have an invisible affect on the meaning of a line of code written elsewhere.

While this is a valid criticism of implicits, the solution is not to abandon them altogether but to apply strict design principles to regulate their use. Here are some tips:

 1. Keep tight control over the scope of your implicits. Package them into traits and objects and only import them where you want to use them.

 2. Package all your implicits in traits/objects with names ending in `Implicits`. This makes them easy to find using a global search across your codebase.

 3. Only use implicits on specific types. Defining an implicit class on a general type like `Any` is more likely to cause problems than defining it on a specific type like `WebSiteVisitor`.

 4. Stick to the design patterns we teach you here. Type enrichment and type classes (coming next) are well known patterns that are easy to identify. Other less disciplined uses of implicits can be difficult to spot.
</div>

## Take Home Points

**Implicit classes** are a Scala language feature that allows us to define extra functionality on existing data types without using conventional inheritance.

This is a programming pattern called **type enrichment** or, more colloquially, *type "pimping"*.

The Scala compiler uses implicit classes to **fix type errors in our code**. When it encounters us accessing a method or field that doesn't exist, it looks through the available implicits to find some code it can insert to fix the error.

The compiler uses a strict set of **implicit resolution rules** to determine whether/which implicits can be used to fix a type error.

We can control which implicits are available by **bringing then into scope** using `import` statements or inheritance. It is good practice to do this only when we need to, to make it easy to see which implicits are being used where.

## Exercises

### Drinking the Kool Aid

Use your newfound powers to add a method `yeah` to `Int`, which prints `Oh yeah!` as many times as the `Int` on which it is called if the `Int` is positive, and is silent otherwise. Here's an example of usage:

~~~ scala
scala> 2.yeah
Oh yeah!
Oh yeah!

scala> 3.yeah
Oh yeah!
Oh yeah!
Oh yeah!

scala> -1.yeah

~~~

When you have written your implicit class, package it in an `IntImplicits` trait/singleton as discussed above.

<div class="solution">
~~~ scala
object IntImplicits extends IntImplicits

trait IntImplicits {
  implicit class IntOps(n: Int) {
    def yeah = for(_ <- 0 until n) println("Oh yeah!")
  }
}

import IntImplicits._

2.yeah
// Oh yeah!
// Oh yeah!
~~~

The solution uses a `for` comprehension and a range to iterate through the correct number of iterations. Remember that the range `0 until n` is the same as `0 to n-1` -- it contains all numbers from `0` inclusive to `n` exclusive.

We have used the parenthesis for of `for` here to keep the solution small. Writing the solution with braces is also fine. Note that we have omitted the `yield` keyword, resulting in a for comprehension that does not accumulate a value.

The names `IntImplicits` and `IntOps` are quite vague -- we would probably name them something more specific in a production codebase. However, for this exercise they will suffice perfectly.
</div>

### Times

Extend your previous example to give `Int` an extra method called `times` that accepts a function of type `Int => Unit` as an argument and executes it `n` times. Example usage:

~~~ scala
scala> 3.times(i => println("Look - it's the number $i!"))
Look - it's the number 0!
Look - it's the number 1!
Look - it's the number 2!
~~~

For bonus points, re-implement `yeah` in terms of `times`.

<div class="solution">
~~~ scala
object IntImplicits extends IntImplicits

trait IntImplicits {
  implicit class YeahOps(n: Int) {
    def yeah =
      times(_ => println("Oh yeah!"))

    def times(func: Int => Unit) =
      for(i <- 0 until n) func(i)
  }
}
~~~
</div>

### Multiple Parameter Lists

Add a method `fold` to `Int`. `fold` has two parameter lists. The first accepts a seed of type `A`. The second accepts a function from `(A, Int) => A`. The method folds over the integers from zero until the given number. Example usage:

~~~ scala
scala> 4.fold(0)(_ + _)
res10: Int = 6

scala> 5.fold[Seq[Int]](Seq())(_ :+ _)
res2: Seq[Int] = List(0, 1, 2, 3, 4)
~~~

<div class="solution">
~~~ scala
object IntImplicits extends IntImplicits

trait IntImplicits {
  implicit class YeahOps(n: Int) {
    // Code from previous exercises...

    def fold[A](seed: A)(func: (A, Int) => A) =
      (0 until n).foldLeft(seed)(func)
  }
}
~~~
</div>
