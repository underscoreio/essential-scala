---
layout: page
title: Implicit Resolution
---

## Implicit Resolution Rules

Scala has three types of implicits---implicit classes, implicit values, and implicit conversions. Each works in the same way---the compiler detects a type error in our code, locates a matching implicit, and applies it to fix the error. This is a powerful mechanism, but we need to control it very carefully to prevent the compiler changing our code in ways we don't expect. For this reason, there is a strict set of **implicit resolution rules** that we can use to dictate the compiler's behaviour:

 1. **Explicits first rule**---if the code already type checks, the compiler ignores implicits altogether;
 2. **Marking rule**---the compiler only uses definitions marked with the `implicit` keyword;
 3. **Scope rule**---the compiler only uses definitions that are *in scope* at the current location in the code (see below);
 4. **Non-ambiguity rule**---the compiler only applies an implicit if it is the only candidate available;
 5. **One-at-a-time rule**---the compiler never chains implicits together to fix type errors---doing so would drastically increase compile times;

Note that the name of the implicit doesn't come into play in this process.

### Implicit Scope

The *scope rule* of implicit resolution uses a special set of scoping rules that allow us to package implicits in useful ways. These rules, collectively referred to as **implicit scope**, form a search path that the compiler uses to locate implicits:

 1. **Local scope**---First look locally for any identifier that is tagged as `implicit`. This must be a single identifier (i.e. `a`, not `a.b`), and can be defined locally or in the surrounding class, object, or trait, or `imported` from elsewhere.

 2. **Companion objects**---If an implicit cannot be found locally, the compiler looks in the companion objects of types involved in the type error. Will see more of this rule in the next section.

## Packaging Implicit Values

Implicits **cannot be defined at the top level** (except in the Scala console). They must be wrapped in an outer trait, class, or singleton object. The typical way of packaging an implicit value is to define it inside a trait called `SomethingImplicits` and extend that trait to create a singleton of the same name:

```tut:book:silent
trait VowelImplicits {
  implicit class VowelOps(str: String) {
    val vowels = Seq('a', 'e', 'i', 'o', 'u')
     def numberOfVowels =
       str.toList.filter(vowels contains _).length
  }
}

object VowelImplicits extends VowelImplicits
```

This gives developers two convenient ways of using our code:

 1. quickly bring our implicit into scope via the singleton object using an `import`:

    ```tut:book:silent
    // `VowelOps` is not in scope here

    def testMethod = {
      import VowelImplicits._

      // `VowelOps` is in scope here

      "the quick brown fox".numberOfVowels
    }

    // `VowelOps` is no longer in scope here
    ```

 2. stack our trait with a set of other traits to produce a library of implicits that can be brought into scope using inheritance or an `import`:

    ```tut:invisible
    trait VowelImplicits
    trait MoreImplicits
    trait YetMoreImplicits
    ```

    ```tut:book:silent
    object AllTheImplicits extends VowelImplicits with MoreImplicits with YetMoreImplicits

    import AllTheImplicits._

    // `VowelOps` is in scope here
    // along with other implicit classes
    ```

<div class="alert alert-info">
**Implicits tip:** Some Scala developers dislike implicits because they can be hard to debug. The reason for this is that an implicit definition at one point in our codebase can have an invisible affect on the meaning of a line of code written elsewhere.

While this is a valid criticism of implicits, the solution is not to abandon them altogether but to apply strict design principles to regulate their use. Here are some tips:

 1. Keep tight control over the scope of your implicits. Package them into traits and objects and only import them where you want to use them.

 2. Package all your implicits in traits/objects with names ending in `Implicits`. This makes them easy to find using a global search across your codebase.

 3. Only use implicits on specific types. Defining an implicit class on a general type like `Any` is more likely to cause problems than defining it on a specific type like `WebSiteVisitor`.

The same resolution rules apply for implicit values as for implicit classes. If the compiler is unable to find suitable candidates for all parameters in the list, we get a compilation error.

Let's redefine our adapters for `HtmlWriter` so we can bring them all into scope. Note that outside the REPL implicit values are subject to the same packaging restrictions as implicit classes---they have to be defined inside another class, object, or trait. We'll use the packaging convention we discussed in the previous section:

```tut:invisible
trait HtmlWriter[A] {
  def write(a: A): String
}
case class Person(name: String, email: String)
import java.util.Date
```

```tut:book:silent
object wrapper {
  trait HtmlImplicits {
    implicit object PersonWriter extends HtmlWriter[Person] {
      def write(person: Person) =
        s"<span>${person.name} &lt;${person.email}&gt;</span>"
    }

    implicit object DateWriter extends HtmlWriter[Date] {
      def write(in: Date) = s"<span>${in.toString}</span>"
    }
  }

  object HtmlImplicits extends HtmlImplicits
}; import wrapper._
```

```tut:invisible
object HtmlUtil {
  def htmlify[A](data: A)(implicit writer: HtmlWriter[A]): String = {
    writer.write(data)
  }
}
```

We can now use our adapters with `htmlify`:

```tut:book
import HtmlImplicits._

HtmlUtil.htmlify(Person("John", "john@example.com"))
```

This version of the code has much lighter syntax requirements than its predecessor. We have now assembled the complete type class pattern: `HtmlUtil` specifies our HTML rendering functionality, `HtmlWriter` and `HtmlWriters` implement the functionality as a set of adapters, and the implicit argument to `htmlify` implicitly selects the correct adapter for any given argument. However, we can take things one step further to really simplify things.
