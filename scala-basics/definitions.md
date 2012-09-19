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

Sometimes (though very rarely in Scala) we will want to change the value that a name is bound to. This is called *mutation* or a *destructive* operation. A `val` does not allow mutation.

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

There is a third kind of definition in Scala, `def`. This defines a method. In the REPL a method behaves much like a function. We can define them:

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
