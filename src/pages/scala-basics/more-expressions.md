---
layout: page
---

# More Complex Programs

We're now going to look at two more data types that have literal representations: tuples and functions. We will then briefly cover generic types.

## Tuples

Tuples are a container for a *fixed number* of other values. They allow us to easily store values of different types without having to create a class for them. For example, to define a tuple containing an `Int`, and `String`, and a `Boolean` we could write:

```scala
scala> (1, "tuple", true)
res48: (Int, java.lang.String, Boolean) = (1,tuple,true)
```

We can access the elements of tuples using the `_n` method, where `n` is the index (starting from 1) of the element.

```scala
scala> (1, "tuple", true)._1
res49: Int = 1

scala> (1, "tuple", true)._2
res50: java.lang.String = tuple

scala> (1, "tuple", true)._3
res51: Boolean = true
```

We will see more elegant ways of doing this later.

The `->` method provides a shortcut way of constructing tuples of two elements, sometimes called pairs.

```scala
scala> 1 -> "foo"
res52: (Int, java.lang.String) = (1,foo)

scala> 1.->"foo"
res53: (Double, java.lang.String) = (1.0,foo)
```

## Functions

Functions, like methods, allow us to abstract over values. For example, here is a function that squares any `Int` it is passed as a parameter.

```scala
scala> (x: Int) => x * x
res16: Int => Int = <function1>
```

A function literal consists of two parts, a parameter list and a body, separated by `=>`. Note we must give types to the parameters. If the body is a single expression we don't have to enclose it in braces but we must do so if it contains more than one expression.

```scala
scala> (x: Int) => { x * x }
res17: Int => Int = <function1>

scala> (x: Int) => {
     |   x + 1 // This has no useful purpose
     |   x * x
     | }
res18: Int => Int = <function1>
```

Functions are very much like methods, but there is a critical difference: they are values. This means we can bind a `val` to a function, pass a function as a parameter to a method or function, and return a function from a method or function.

```scala
scala> val square = (x: Int) => x * x
square: Int => Int = <function1>
```

In Scala all values are objects. A function is just an object with a method called `apply`. We can call (or apply) a function in the same way we'd call such an object.

```scala
scala> square.apply(2)
res9: Int = 4

scala> square(2)
res10: Int = 4

scala> square apply 2
res11: Int = 4
```


## Generic Types

How can we create a method that accepts an object of any type and returns it? Recalling that `Any` is the top of the type hierarchy we could write:

```scala
scala> def foo(x: Any) = x
foo: (x: Any)Any
```

This works but it loses type information. For example, when we pass in an `Int` the result has type `Any` and as such we can't use it in arthimetic expressions.

```scala
scala> foo(1)
res32: Any = 1

scala> foo(1) - 1
<console>:9: error: value - is not a member of Any
              foo(1) - 1
                     ^
```

What we want is a *generic type*, so we can say our method accepts a value of some type `A` and returns the same type. Here's how we write it.

```scala
scala> def foo[A](x: A) = x
foo: [A](x: A)A

scala> foo(1)
res34: Int = 1

scala> foo(1) - 1
res35: Int = 0

scala> foo("hi!")
res36: java.lang.String = hi!
```

As we can see above, when we actually use `foo` the concrete type of it's argument is substituted for `A`.

### Type Bounds

We've seen how to define generics that match any type. Sometimes we want to restrict the type to be a subtype or supertype of some other type. This is known as a type bound. So we want to implement a function that can handle only subtypes of `Enumeration`. We can use a type bounds like `[A <: Enumeration]` to do this.

```scala
scala> def foo[A <: Enumeration](enum: Enumeration) = enum.maxId
foo: [A <: Enumeration](enum: Enumeration)Int

scala> foo(scala.swing.Dialog.Result)
res3: Int = 3
```

You can also declare bounds in the other direction (an upper bound) using `[A >: Enumeration]` and also declare upper and lower bounds.
