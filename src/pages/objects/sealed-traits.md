---
layout: page
title: "Pattern Matching"
---

We have seen how to use traits to model data that is of one type or another. Now we'll look at how to use this information to write code that is customised to each type in our hierarchy.

Let's return to our web site visitors example and imagine we want to add the ability to email `Visitors`. We can only actually email `Users` because we don't know the email address for `Anonymous` visitors. If we have a `Visitor` how can we tell what subtype we have, and thus if we can email them?

We're going to look at two solutions: an object-oriented solution based on inheritance and a functional solution based on pattern matching.

The object-oriented solution is to add an abstract method to `Visitor` along with implementations in the subtypes. The code looks something like this (trimmed for simplicity):

~~~ scala
trait Visitor {
  def email(subject: String, body: String): Unit
}

case class Anonymous() extends Visitor {
  def email(subject: String, body: String): Unit = {
    () // do nothing
  }
}

case class User(email: String) extends Visitor {
  def email(subject: String, body: String) = {
    reallySendAnEmail(email, subject, body)
  }
}
~~~

Given this code we can call the `email` method on any `Visitor` and stuff happens, as if by magic.

**This is a bad design.** As we add more features our code we will soon find `Visitor`  filling up with abstract methods. It is also likely that `Anonymous` will start to fill up with empty method stubs. For every feature we add, it will become harder to maintain and test our class hierarchy, and any system that uses `Visitor` will have to carry around all its dependencies.

A better design is to keep our case classes as [value objects](http://en.wikipedia.org/wiki/Value_object) and implement new features as separate libraries of code.

Let's refactor our `email` method into its own library:

~~~ scala
trait EmailService {
  def email(visitor: Visitor, subject: String, body: String): Unit = {
    // What do we put here?
  }
}
~~~

The question is: how do we implement the `email` method? We need to know if we have a `User` or an `Anonymous`. Fortunately, Scala has great general purpose tool called **pattern matching** that is perfect for this situation. The code looks like this:

~~~ scala
trait EmailService {
  def email(v: Visitor, subject: String, body: String): Unit = {
    v match {
      case Anonymous(id, createdAt) =>
        () // do nothing
      case User(id, email, createdAt) =>
        reallySendAnEmail(email, subject, body)
    }
  }
}
~~~

Pattern matching is introduced using the `match` keyword followed by a series of `case` clauses. Each clause consists of the keyword `case`, a *pattern*, an `=>` operator, and zero or more Scala expressions. Pattern matching is itself an expression that yields a type and a value:

~~~
expr0 match {
  case pattern1 => expr1 ...
  case pattern2 => expr2 ...
  ...
}
~~~

Pattern matching operates by checking the value of `expre0` against each pattern in turn, finding the first pattern that matches, and evaluating the relevant set of expressions[^compilation].

[^compilation]: In reality patterns are compiled to a more efficient form than a sequence of tests, but the semantics are the same.

Although patterns look like Scala, the pattern language actually has its own expressive syntax and semantics. We will look at the language in more detail later on. For now we only need concern ourselves with one type of pattern -- case class patterns.

The pattern syntax for case classes matches the constructor syntax. So, the pattern `User(a, b, c)` matches any `User` object and binds the names `id`, `address`, and `createdAt` fields to variables called `a`, `b`, and `c`.


.....


Scala has an operator called `match` that lets us deconstruct any case class into its component fields. The code looks like this:

~~~ scala
scala> case class Person(firstName: String, lastName: String)
defined class Person

scala> Person("Dave", "Gurnell")
res0: Person = Person(Dave,Gurnell)

scala> res0 match {
     |   case Person(f, l) =>
     |     s"The person's name is ${f} ${l}"
     | }
res2: String = The person's name is Dave Gurnell
~~~

In this example we deconstruct a `Person` into its constituent fields using a `match` expression. The code `Person(f, l)` is called a *pattern*, written in a special *pattern matching DSL* that resembles regular Scala code, but that actually has its own separate syntax and semantics. The pattern in the example extracts the `firstName` and `lastName` fields from the person and binds them to two variables, `f` and `l`. These

The general form of `match` expression is:

~~~ scala
someExpression match {
  case somePattern =>
    someCode
    // ...
  case someOtherPattern =>
    someOtherCode
    // ...
  case // ...
}
~~~

in which `someExpression` is a Scala expression, `someCode` and `someOtherCode` are sets of Scala expressions, and `somePattern` and `someOtherPattern` are patterns.


## Sealed Traits

The Scala compiler won't complain if we miss out a case in our pattern matching, but we will get an exception at runtime. For example:

~~~ scala
scala> def missingCase(v: Visitor) =
     |   v match {
     |     case User(_, _, _) => "Got a user"
     |   }
missingCase: (v: Visitor)String

scala> missingCase(Anonymous("a"))
missingCase(Anonymous("a"))
scala.MatchError: Anonymous(a,Fri Feb 14 19:47:42 GMT 2014) (of class Anonymous)
  at .missingCase(<console>:12)
    ...
~~~

The reason we don't get a compiler error is that other code could extend `Visitor`, creating new subtypes, so the Scala compiler can't be sure that only the two cases exist. This means that even a pattern match that appears complete could be rendered incomplete by additional subclasses. To avoid compilation errors in some cases but not others the compiler is silent in all cases.

However, when we define `Visitor` we can tell the Scala compiler that we are defining at the same time all the possible subtypes. In this case the compiler can correctly flag incomplete matches and the code above would fail to compile. We can do this by using a `sealed` trait.

We using a sealed trait we must define all the subtypes in the same file. For the `Visitor` example no other subtypes seem plausible -- either a visitor is anonymous or they are not. In this case we should use a sealed trait.

~~~ scala
sealed trait Visitor {
  def id: String // A unique id we assign to each user
  def createdAt: Date // The date this user first visited our site

  // How long has this visitor been around?
  def age: Long =
    new Date().getTime() - createdAt.getTime()

}

case class Anonymous(
  val id: String,
  val createdAt: Date = new Date()
) extends Visitor

case class User(
  val id: String,
  val emailAddress: String,
  val createdAt: Date = new Date()
) extends Visitor
~~~

When we do this, the compiler warns us if we don't have exhaustive pattern matching:

~~~ scala
scala> def missingCase(v: Visitor) =
       |  v match {
       |    case User(_, _, _) => "Got a user"
       |  }
<console>:21: warning: match may not be exhaustive.
It would fail on the following input: Anonymous(_, _)
               v match {
               ^
missingCase: (v: Visitor)String
~~~

Note that we can still extend subtypes of a sealed trait outside of the file where they are defined. For example, in other code we could extend `User` or `Anonymous`. If we want to prevent this possibility we should declare them as `sealed` (if we want to allow extensions within the file) or `final` if we want to disallow all extensions. For the analytics example it probably doesn't make sense to allow any extension, so the simplified code should look like this:

~~~ scala
sealed trait Visitor { ... }
final case class User(...) extends Visitor
final case class Anonymous(...) extends Visitor
~~~

Now imagine the problem of parsing HTTP headers. Most of the HTTP headers are defined in various RFCs, and we could define types for these as follows:

~~~ scala
sealed trait Header { ... }
final case class Accept(...) extends Header
final case class AcceptCharset(...) extends Header
...
~~~

It certainly doesn't make sense to subtype one of the defined headers so we can defined them as `final`. However custom headers are used all the time, so we need to provide some extension point. To achieve this we can define a `CustomHeader` subtype of `Header` that is neither `sealed` nor `final`. This allows the user to extend `CustomHeader` in their code, but we still retain the benefits of `sealed` and `final` types in ours[^http].

[^http]: There are so many HTTP headers that we're unlikely to perform pattern matching on them, but hopefully you can generalise this example to your own problems.
