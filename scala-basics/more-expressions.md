---
layout: page
---

# More Expressions

We're now going to look at more complicated expressions, in particular conditionals, tuples, and functions. We will then look at generic types, which are motivated by tuples and functions.

## Conditionals

Conditionals are an essential part of any programming language. Scala's `if` statement has the same syntax as Java's. One difference in Scala is that a conditional returns a value.

{% highlight scala %}
scala> if(true) {
         42
       } else {
         40
       }
res45: Int = 42
{% endhighlight %}

You can drop the brackets if a single expression follows an arm, and even write a conditional on one line.

{% highlight scala %}
scala> if(true) 42 else 40
res47: Int = 42
{% endhighlight %}


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
