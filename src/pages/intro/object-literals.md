---
layout: page
title: Object Literals
---

So far we've seen how to create objects of built-in types like `Int` and `String`. More useful programs will require we create objects tailored to the problem we're solving. In fact creating the right objects could be seen as the goal of programming in Scala. We will start by seeing how to write object literals.

The simplest object literal is an empty object.

```scala
scala> object test {}
defined module test
```

This is simple program but there three important things to note:

* We have to give an object a name. Here we've called it `test`.
* This program is not an expression because it doesn't evaluate to a value. Rather it binds a name (`test`) to a value (the empty object). This type of program is called a definition or a statement.
* Finally notice that the Scala REPL tells us it has defined a **module**. We'll see what this means later.

The expression `test` evaluates to the object we've defined above.

```scala
scala> test
res0: test.type = test$@1668bd43
```

Note here the type of the object printed by the REPL: `test.type`. This is not like any type we've seen before -- it is a new type created just for our object.

## Methods

We interact with objects via methods, so let's create an object with a method.

```scala
scala> object test2 {
     | def name: String = "Probably the best object ever"
     | }
defined module test2
```

Here we've create a method called `name`. We can call it in the usual way.

```scala
scala> test2.name
res3: String = Probably the best object ever
```

Here's an object with a more complex method definition.

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

object test3 {
  def hello(name: String) =
    "Hello " + name
}
^D

// Exiting paste mode, now interpreting.

defined module test3

scala> test3.hello("Noel")
res7: String = Hello Noel
```

From these examples we can see most of the important bits of method definitions:

* Methods start with the `def` keyword, followed by a name and an optional list of parameters.
* We have to declare the types of parameters, using the syntax `name: type`.
* We can optionally declare the return type of a method. If we don't declare a return type Scala will infer one for us.
* The method declaration is followed by an equals signs and then the method body.
* The method body evaluates to the value of the last expression in the body. If you're used to Java note that no `return` statement is needed in Scala. A method body is an expression.


## Instance Variables

An object can also contain objects, called instance variables. We introduce these using the `val` statement, which looks like `val name = value`. Here's an example:

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

object test4 {
  val name = "Noel"
  def hello(other: String) =
    name + " says hi to " + other
}
^D

// Exiting paste mode, now interpreting.

defined module test4

scala>
scala> test4.hello("Dave")
res8: String = Noel says hi to Dave
```

The right-hand side of a `val` statement can be any expression. So a compound expression like `2 + 1` is valid. We can even have a sequence of statements and expressions, wrapped in braces, in which case the value of the last expression in the sequence becomes the value assigned to the name.

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

object test5 {
  val name = {
    val title = "Dr"
    val theDoctor = title + " Who"
    theDoctor
  }
}
^D

// Exiting paste mode, now interpreting.

defined module test5

scala> test5.name
res9: String = "Dr Who"
```

This rule also applies to method definitions.

## Methods vs Instance Variables

At this point you might be wondering why we need instance variables when we can have methods of no arguments that seem to work the same. The difference is that a instance variable gives a name to a value, whereas a method definition gives a name to a computation that produces a value.

Here's some code the shows the difference. I'm using a short-cut only available in the REPL that lets us use `def` and `val` without wrapping them in an object.

First let's start with an instance variable where the right-hand side prints out something before returning a value

```scala
scala> val instanceVariable = {
     |   println("Evaluating")
     |   42
     | }
Evaluating
instanceVariable: Int = 42
```

Notice how `Evaluating` is printed out before the variable is defined.

Here's the same code but as a method.

```scala
scala> def noArgMethod = {
     |   println("Evaluating")
     |   42
     | }
noArgMethod: Int
```

This time nothing is printed out. Now let's evaluate `instanceVariable` and `noArgMethod`.

```scala
scala> instanceVariable
res10: Int = 42

scala> noArgMethod
Evaluating
res11: Int = 42
```

This shows the difference between the two. The right-hand side of the `val` statement is evaluated immediately and its value bound to the name. The right-hand side of the `def` statement is evaluated every time the method is called, but not when the method is defined.

## Exercises

<TODO>
