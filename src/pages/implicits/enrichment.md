---
layout: page
title: Type Enrichment
---

Type enrichment, often referred to colloquially as *"pimping"*, allows us to **augment existing classes with extra functionality**. For example, suppose we have a method called `countVowels` that counts the vowels in a `String`:

~~~ scala
scala> def numberOfVowels(str: String) =
     |   str.toList.filter { char =>
     |     Seq('a', 'e', 'i', 'o', 'u').contains(char)
     |   }.length
numberOfVowels: (str: String)Int

scala> numberOfVowels("the quick brown fox")
res0: Int = 5
~~~

Ideally we would like this to be a method defined directly on `String`. However, we don't have access to the source code for `String` to add new functionality, nor would it be a sensible idea playing with classes in the core Java API.

Fortunately, Scala has a feature called called **implicit classes** that allow us to define new functionality as if it is part of an existing class. The resulting usage patterns are similar to *categories* in Objective C or *extension methods* in C#, although the implementation is different in each case.

Implicit classes are one of three flavours of Scala's implicit mechanic. We shall encounter the other two -- *implicit values* and *implicit conversions* -- later this chapter.

## Implicit Classes

Here is an implicit class that adds our `numberOfVowels` method to `String`:

~~~ scala
scala> implicit class ExtraStringMethods(str: String) {
     |   val vowels = Seq('a', 'e', 'i', 'o', 'u')
     |
     |   def numberOfVowels =
     |     str.toList.filter(vowels contains _).length
     | }
defined class ExtraStringMethods
~~~

We can use this like any other class to wrap up our `String` and gain access to our new method:

~~~ scala
new ExtraStringMethods("the quick brown fox").numberOfVowels
~~~

However, by tagging the class with the `implicit` keyword we give the compiler the ability to use it to fix type errors in our code. When we write an expression like:

~~~ scala
"the quick brown fox".numberOfVowels
~~~

the compiler detects the type error (i.e. `String` doesn't have a `numberOfVowels` method) and looks for ways to fix it. It finds our `ExtraStringMethods` class and inserts an invisible constructor call to fix the type error. This allows us to pretend our code was fine all along:

~~~ scala
scala> "the quick brown fox".numberOfVowels
res1: Int = 5
~~~

## Implicit Resolution Rules

As we have seen above, implicit classes work by allowing the compiler to insert constructor calls into our code to fix type errors. **The compiler uses a strict set of rules** to determine whether it can fix an error with an implicit, and which implicit it should use:

 1. **Explicits first rule** -- if the code already type checks, the compiler ignores implicits altogether;

 2. **Marking rule** -- the compiler only uses definitions marked with the `implicit` keyword;

 3. **Scope rule** -- the compiler only uses definitions that are *in scope* at the current location in the code;

 4. **Non-ambiguity rule** -- the compiler only applies an implicit if it is the only candidate available;

 5. **One-at-a-time rule** -- the compiler never chains implicits together to fix type errors -- doing so would drastically increase compile times;

Note that the name of the implicit doesn't come into play.

Also note that rule #2 uses a special set of scoping rules called **implicit scope** that are a superset of the regular scoping rules used in Scala. We'll learn more about these in the next section.

## Packaging Implicits

Implicits classes **cannot be defined at the top level**. They must be wrapped in an outer trait, class, or singleton object. The typical programming pattern is as follows:

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

We define a top-level trait called `VowelImplicits` and place our implicit class inside it. We then extend the trait to create a singleton object. This gives users of our library two convenient possibilities:

 1. quickly bring our implicit into scope via the singleton object using an explicit `import`:

    ~~~ scala
    // `VowelOps` is not in scope here

    def testMethod = {
      import VowelImplicits._

      // `VowelOps` is in scope here

      "the quick brown fox".numberOfVowels
    }

    // `VowelOps` is no longer in scope here
    ~~~

 2. stack our trait with a set of other traits to produce a library of implicits that can be brought into scope with a single `import`:

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

The compiler uses a strict set of **implicit resolution rules**.

We can control which implicits the compiler has available by **bringing then into scope** using `import` statements or inheritance. It is good practice to do this as and when necessary to simplify debugging.

## Exercises

### TODO
