---
layout: page
---

# Definitions

So far we've had to type out the expression creating a value whenever we wanted to use that value. In this section we'll see how to give names to values, so we can bind a name to a value once and then use the name wherever we want to use the value. These parts of a program are called *defintions*.

## val

The simplest type of defintion is a `val`. It allows us to give a name to value. Let's give a name to the number `42`.

```scala
scala> val theAnswer = 42
theAnswer: Int = 42
```

Now whenever we want to refer to this value we can use the name instead.

```scala
scala> theAnswer
res0: Int = 42
```

<!--
So, to define our `square` function we could write:

```scala
scala> val square = (x: Int) => x * x
square: Int => Int = <function1>
```

Now whenever we want to use our function we can refer to it by name.

```scala
scala> square(2)
res0: Int = 4
```

We can bind *any* value to a name using `val`.

```scala
scala> val aString = "this is a string"
aString: String = this is a string

scala> aString
res2: String = this is a string
```
-->

## var

Sometimes (though very rarely in Scala) we will want to change the value that a name is bound to. This is called *reassignment*, *mutation*, a *side-effect* or a *destructive* operation. A `val` does not allow mutation.

```scala
scala> val aString = "this is a string"
aString: String = this is a string

scala> aString
res2: String = this is a string

scala> aString = "another string"
<console>:8: error: reassignment to val
       aString = "another string"
               ^
```

To allow a name to be rebound we must introduce the name using `var`.

```scala
scala> var aMutatingString = "now I'm here!"
aMutatingString: String = now I'm here!

scala> aMutatingString
res3: String = now I'm here!

scala> aMutatingString = "now I'm not!"
aMutatingString: String = now I'm not!

scala> aMutatingString
res4: String = now I'm not!
```

Mutation is strongly discouraged in Scala. It makes reasoning about programs more difficult and makes concurrent programs almost impossible to get right.

## def

Another kind of definition in Scala is a `def`. This defines a method. We can define them:

```scala
scala> def squareMethod(x: Int) = x * x
squareMethod: (x: Int)Int
```

and apply them

```scala
scala> squareMethod(2)
res7: Int = 4
```

Scala requires we declare the types of method parameters, but we can usually omit the return type. However it is good practice to declare the return type.

```scala
scala> def squareMethod(x: Int):Int = x * x
squareMethod: (x: Int)Int
```

A method is *not* a value. We cannot bind a `val` to a method, nor can we pass a method to a function (or a method), nor can we return a method from a function (or a method).

```scala
scala> squareMethod
<console>:9: error: missing arguments for method squareMethod in object $iw;
follow this method with `_' if you want to treat it as a partially applied function
              squareMethod
              ^

scala> val theMethod = squareMethod
<console>:8: error: missing arguments for method squareMethod in object $iw;
follow this method with `_' if you want to treat it as a partially applied function
       val theMethod = squareMethod
                       ^
```

However we can convert a method to a function using the all powerful underscore! A function is a value.

```scala
scala> squareMethod _
res11: Int => Int = <function1>
```

Given that methods are like restricted functions, why do we have them? The answer is for compatibility with Java. Java has a builtin concept of methods which are heavily optimised. By allowing methods we get both performance and easier interoperation with Java.

We'll have more to say about functions in a later section.

### Exercise

Write a method that computes the absolute value of an `Int`. The body of the method should be a single compound expression. Hint: recall that `if` is an expression in Scala.

## Object literals

The fourth kind of definition in Scala is an object literal. We define objects using the `object` keyword. An object can contain other definitions, and we can call these using the usual method syntax.

```scala
scala> object anObject {
     |   val myName = "anObject"
     |   val myAge  = 42
     |   def myMethod(x: String) = x + " with icecream!"
     | }
defined module anObject

scala> anObject.myName
res19: String = anObject

scala> anObject.myMethod("Le Beouf")
res20: String = Le Beouf with icecream!
```

Note that we refer to `val`s, `var`s, or `def`s within an object using the same syntax. This is called the *universal access property*. In fact, if we define a method with no arguments it is indistinguishable from a `val`.

```scala
scala> object tricky {
     |   val theVal = "I'm a method!"
     |   def theMethod = "I'm a var!"
     | }
defined module tricky

scala> tricky.theVal
res21: String = I'm a method!

scala> tricky.theMethod
res22: String = I'm a var!
```

Methods with no arguments and methods with a single empty argument are different things in Scala as the example below demonstrates.

```scala
scala> object EmptyMethods {
     |   def noArgs = 1
     |   def emptyArgs() = 2
     | }
defined module EmptyMethods

scala> EmptyMethods.noArgs
res26: Int = 1

scala> EmptyMethods.noArgs()
<console>:9: error: Int does not take parameters
              EmptyMethods.noArgs()
                                 ^

scala> EmptyMethods.emptyArgs()
res28: Int = 2

scala> EmptyMethods.emptyArgs
res29: Int = 2
```

In summary you can call an argument with an empty argument list with either an empty list or by omitting the list entirely. The same cannot be done for a method with no arguments.

There is a convention within Scala that methods with no arguments are reserved for methods that don't produce side-effects, while methods with empty arguments do. Furthermore, by convention calls to empty arguments methods (that produce side-effects) should always include the empty argument list.

## Recursive Methods

Now that we know how to define methods we can define recursive methods. A recursive method is one that calls itself. Here is an example:

```scala
scala> def factorial(n: Int): Int =
     |   if(n == 0) 1 else n * factorial(n - 1)
factorial: (n: Int)Int

scala> factorial(5)
res8: Int = 120
```

Note that we must specify the return type on a recursive method.

### Tail-recursive Loops

You've probably been taught that recursion is problematic because it consumes stack space. This is indeed true in many cases.

```scala
scala> def sum(n: Int): Int = if(n == 0) 0 else n + sum(n-1)
sum: (n: Int)Int

scala> sum(10)
res26: Int = 55

scala> sum(100)
res27: Int = 5050

scala> sum(10000)
java.lang.StackOverflowError
	at .sum(<console>:9)
```

If the last expression in a function or method is a function call this call is said to be in *tail position* and can be optimised to not use stack space. Due to JVM limitations Scala can only optimise self calls in final methods and in local functions. For these cases the recursive call will *not* use any extra stack space.

```scala
scala> def sumTail(n: Int, accum: Int):Int =
         if(n == 0) accum else sumTail(n-1, n + accum)
sumTail: (n: Int, accum: Int)Int

scala> sumTail(10000, 0)
res25: Int = 50005000
```

Ensuring a function is tail recursive is an important property, as tail recursive functions are used to implement general purpose loops in Scala. To assist with this, Scala includes a `@tailrec` annotation, available in the package `scala.annotation`. If a method is annotated with `@tailrec` the compiler will raise an warning if a method is not tail recursive.

```scala
scala> import scala.annotation.tailrec
import scala.annotation.tailrec

scala> @tailrec def sum(n: Int): Int = if(n == 0) 0 else n + sum(n-1)
<console>:11: error: could not optimize @tailrec annotated method sum: it contains a recursive call not in tail position
       @tailrec def sum(n: Int): Int = if(n == 0) 0 else n + sum(n-1)
                                                           ^

scala> @tailrec def sumTail(n: Int, accum: Int):Int =
                  if(n == 0) accum else sumTail(n-1, n + accum)
     | sumTail: (n: Int, accum: Int)Int
```

Note that any recursive method can be converted to a tail recursive function by using an accumulator, as we've done with `sumTail` above. Also note that any loop can be written as a recursive method. A typical pattern is to hide the tail recursive method behind a normal method with a more convenient interface.

```scala
scala> def sum(n: Int): Int = {
     |   @tailrec def sumTail(n: Int, accum: Int): Int =
     |     if(n == 0) accum else sumTail(n-1, n + accum)
     |   sumTail(n, 0)
     | }
sum: (n: Int)Int

scala> sum(10000)
res29: Int = 50005000
```

### Exercise

Write a tail recursive version of the `factorial` method. Valid it is tail-recursive by annotating it with the `@tailrec` annotation.
