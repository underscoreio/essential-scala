---
layout: page
---

# Definitions

We've seen how to create values but so far we've had to type out the expression creating the value whenever we wanted to use. For example, everytime we wanted to use our function that squares values we had to type out the function literal. In this section we'll see how to give names to values, so we can bind a name to a value once and then use the name wherever we want to use the value. These parts of a program are called *defintions*.

## val

The simplest type of defintion is a `val`. It allows us to give a name to value. So, to define our `square` function we could write:

{% highlight scala %}
scala> val square = (x: Int) => x * x
square: Int => Int = <function1>
{% endhighlight %}

Now whenever we want to use our function we can refer to it by name.

{% highlight scala %}
scala> square(2)
res0: Int = 4
{% endhighlight %}

We can find *any* value to a name using `val`.

{% highlight scala %}
scala> aTuple
res1: (Int, java.lang.String, Int) = (1,boo,3)

scala> val aString = "this may be a string"
aString: java.lang.String = this may be a string

scala> aString
res2: java.lang.String = this may be a string
{% endhighlight %}

## var

Sometimes (though very rarely in Scala) we will want to change the value that a name is bound to. This is called *reassignment*, *mutation*, a *side-effect* or a *destructive* operation. A `val` does not allow mutation.

{% highlight scala %}
scala> aString = "another string"
<console>:8: error: reassignment to val
       aString = "another string"
               ^
{% endhighlight %}

To allow a name to be rebound we must introduce the name using `var`.

{% highlight scala %}
scala> var aMutatingString = "now I'm here!"
aMutatingString: java.lang.String = now I'm here!

scala> aMutatingString
res3: java.lang.String = now I'm here!

scala> aMutatingString = "now I'm not!"
aMutatingString: java.lang.String = now I'm not!

scala> aMutatingString
res4: java.lang.String = now I'm not!
{% endhighlight %}

Mutation is strongly discouraged in Scala. It makes reasoning about programs more difficult and makes concurrent programs almost impossible to get right.

## def

Another kind of definition in Scala is a `def`. This defines a method. In the REPL a method behaves much like a function. We can define them:

{% highlight scala %}
scala> def squareMethod(x: Int) = x * x
squareMethod: (x: Int)Int
{% endhighlight %}

and apply them

{% highlight scala %}
scala> squareMethod(2)
res7: Int = 4
{% endhighlight %}

just like a function. However a method, unlike a function, is *not* a value!

{% highlight scala %}
scala> val square = (x: Int) => x * x
square: Int => Int = <function1>

scala> square
res8: Int => Int = <function1>

scala> squareMethod
<console>:9: error: missing arguments for method squareMethod in object $iw;
follow this method with `_' if you want to treat it as a partially applied function
              squareMethod
              ^
{% endhighlight %}

We cannot pass a method to a function (or a method) nor can we return a method from a function (or a method). However we can convert a method to a function using the all powerful underscore!

{% highlight scala %}
scala> squareMethod _
res11: Int => Int = <function1>
{% endhighlight %}

Given that methods are like restricted functions, why do we have them? The answer is for compatibility with Java. Java has a builtin concept of methods which are heavily optimised. By allowing methods we get both performance and easier interoperation with Java.


## Object literals

The fourth kind of definition in Scala is an object literal. We define objects using the `object` keyword. An object can contain other definitions, and we can call these using the usual method syntax.

{% highlight scala %}
scala> object anObject {
     |   val myName = "anObject"
     |   val myAge  = 42
     |   def myMethod(x: String) = x + " with icecream!"
     | }
defined module anObject

scala> anObject.myName
res19: java.lang.String = anObject

scala> anObject.myMethod("Le Beouf")
res20: java.lang.String = Le Beouf with icecream!
{% endhighlight %}

Note that we refer to `val`s, `var`s, or `def`s within an object using the same syntax. This is called the *universal access property*. In fact, if we define a method with no arguments it is indistinguishable from a `val`.

{% highlight scala %}
scala> object tricky {
     |   val theVal = "I'm a method!"
     |   def theMethod = "I'm a var!"
     | }
defined module tricky

scala> tricky.theVal
res21: java.lang.String = I'm a method!

scala> tricky.theMethod
res22: java.lang.String = I'm a var!
{% endhighlight %}

Methods with no arguments and method with a single empty argument are different things in Scala as the example below demonstrates.

{% highlight scala %}
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
{% endhighlight %}

In summary you can call an argument with an empty argument list with either an empty list or by omitting the list entirely. The same cannot be done for a method with no arguments.

There is a convention within Scala that methods with no arguments are reserved for methods that don't produce side-effects, while methods with empty arguments do. Furthermore, by convention calls to empty arguments methods (that produce side-effects) should always include the empty argument list.


## Generic Types

How can we create a method that accepts an object of any type and returns it? Recalling that `Any` is the top of the type hierarchy we could write:

{% highlight scala %}
scala> def foo(x: Any) = x
foo: (x: Any)Any
{% endhighlight %}

This works but it loses type information. For example, when we pass in an `Int` the result has type `Any` and as such we can't use it in arthimetic expressions.

{% highlight scala %}
scala> foo(1)
res32: Any = 1

scala> foo(1) - 1
<console>:9: error: value - is not a member of Any
              foo(1) - 1
                     ^
{% endhighlight %}

What we want is a *generic type*, so we can say our method accepts a value of some type `A` and returns the same type. Here's how we write it.

{% highlight scala %}
scala> def foo[A](x: A) = x
foo: [A](x: A)A

scala> foo(1)
res34: Int = 1

scala> foo(1) - 1
res35: Int = 0

scala> foo("hi!")
res36: java.lang.String = hi!
{% endhighlight %}

As we can see above, when we actually use `foo` the concrete type of it's argument is substituted for `A`.

### Type Bounds

We've seen how to define generics that match any type. Sometimes we want to restrict the type to be a subtype or supertype of some other type. This is known as a type bound. So we want to implement a function that can handle only subtypes of `Enumeration`. We can use a type bounds like `[A <: Enumeration]` to do this.

{% highlight scala %}
scala> def foo[A <: Enumeration](enum: Enumeration) = enum.maxId
foo: [A <: Enumeration](enum: Enumeration)Int

scala> foo(scala.swing.Dialog.Result)
res3: Int = 3
{% endhighlight %}


## Recursive Functions

Now that we know how to bind a name to a value we can define recursive functions. A recursive function is one that calls itself. Here is an example:

{% highlight scala %}
scala> val foo = (x: Int) => if (x > 0) foo(x - 1) else x
<console>:8: error: recursive value foo needs type
       val foo = (x: Int) => if (x > 0) foo(x - 1) else x
                                        ^
{% endhighlight %}

Note that it does not compile. Up to now we've been fairly lax about defining types. Scala performs a process called *type inference*, which means it works out most of the types for us. This process doesn't work for recursive functions so we must specify the type up-front.

{% highlight scala %}
scala> val foo: Int => Int = (x: Int) => if (x > 0) foo(x - 1) else x
foo: Int => Int = <function1>

scala> foo(4)
res31: Int = 0
{% endhighlight %}

## Tail-recursive Loops

You've probably been taught that recursion is problematics because it consumes stack space. This is indeed true in many cases.

{% highlight scala %}
scala> val fib: Int => Int = (n: Int) => if(n == 0) 1 else n + fib(n-1)
fib: Int => Int = <function1>

scala> fib(10)
res1: Int = 56

scala> fib(100)
res2: Int = 5051

scala> fib(1000)
res3: Int = 500501

scala> fib(10000)
java.lang.StackOverflowError
	at $anonfun$1.apply$mcII$sp(<console>:7)
{% endhighlight %}

If the last expression is a function or method is a function call this call is said to be in *tail position* and can be optimised to not use stack space. Due to JVM limitations Scala can only optimise self calls in final methods and in local functions. For these cases the recursive call will *not* use any extra stack space.


Tail recursion and `@tailrec`.
