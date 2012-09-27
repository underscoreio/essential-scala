---
layout: page
---

# More Expressions

We're now going to look at more complicated expressions, in particular conditionals, tuples, and functions. We will then look at generic types, which are motivated by tuples and functions.

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

You can also declare bounds in the other direction (an upper bound) using `[A >: Enumeration]` and also declare upper and lower bounds.

## Complex Literals

We're now going to look at some more interesting values that have literal representations.

### Tuples

Tuples are a container for a *fixed number* of other values. They allow us to easily store a values of different types without having to create a class for them. For example, to define a tuple containing an `Int`, and `String`, and a `Boolean` we could write:

{% highlight scala %}
scala> (1, "tuple", true)
res48: (Int, java.lang.String, Boolean) = (1,tuple,true)
{% endhighlight %}

We can access the elements of tuples using the `_n` method, where `n` is the index (starting from 1) of the element.

{% highlight scala %}
scala> (1, "tuple", true)._1
res49: Int = 1

scala> (1, "tuple", true)._2
res50: java.lang.String = tuple

scala> (1, "tuple", true)._3
res51: Boolean = true
{% endhighlight %}

We will see more elegant ways of doing this later.

The `->` method provides a shortcut way of constructing tuples of two elements, sometimes called pairs.

{% highlight scala %}
scala> 1 -> "foo"
res52: (Int, java.lang.String) = (1,foo)

scala> 1.->"foo"
res53: (Double, java.lang.String) = (1.0,foo)
{% endhighlight %}

### Functions

Functions allow us to abstract over values, plugging a value into an expression. For example, here is a function that squares any `Int` it is passed as a parameter.

{% highlight scala %}
scala> (x: Int) => x * x
res16: Int => Int = <function1>
{% endhighlight %}

A function literal consists of two parts, a parameter list and a body, separated by `=>`. Note we must give types to the parameters. If the body is a single expression we don't have to enclose it in braces but we must do so if it contains more than one expression.

{% highlight scala %}
scala> (x: Int) => { x * x }
res17: Int => Int = <function1>

scala> (x: Int) => {
     |   x + 1 // This has no useful purpose
     |   x * x
     | }
res18: Int => Int = <function1>
{% endhighlight %}

Remember that a function is just an object with a method called `apply`. We can call (or apply) a function in the same way we'd call such an object.

{% highlight scala %}
scala> ((x: Int) => { x * x }).apply(2)
res20: Int = 4

scala> ((x: Int) => { x * x })(2)
res21: Int = 4

scala> ((x: Int) => { x * x }) apply 2
res22: Int = 4
{% endhighlight %}
