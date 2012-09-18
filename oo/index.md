---
layout: page
---

# Object Oriented Programming in Scala



## A simple class definition

Here is a simple Scala class definition:

{% highlight scala %}
scala> class Counter {
     |   var counter = 0
     |
     |   def add(num: Int) = {
     |     counter = counter + num
     |     counter
     |   }
     | }
defined class Counter
{% endhighlight %}

Let's look at what we have. The class is called `Counter`.We can create a new `Counter` object using the `new` operator as follows:

{% highlight scala %}
scala> val example = new Counter()
example: Counter = Counter@526a4268
{% endhighlight %}

The class has one field called `counter`. We have used the `var` keyword to define the field making it *mutable*. Because we initialise the field to the value `0`, Scala infers its type as `Int`. If we wanted to be more explicit we could equivalently write:

{% highlight scala %}
var value: Int = 0
{% endhighlight %}

Fields in Scala are *public* by default -- there is no `public` keyword. We can get and set our `counter` as follows:

{% highlight scala %}
scala> example.counter
res0: Int = 0

scala> example.counter = 5
example.counter: Int = 5

scala> example.counter
res1: Int = 5
{% endhighlight %}

We could have declared the field as `private` as follows:

{% highlight scala %}
private var counter = 0
{% endhighlight %}

Finally, our class has one method called `add`, that accepts a single argument `num` of type `Int`. The method adds `num` to the `counter` field and returns its new value:

{% highlight scala %}
scala> example.add(10)
res2: Int = 15

scala> example.add(12)
res3: Int = 27
{% endhighlight %}

While the `return` keyword is available in Scala, it is considered bad form to use it. Every statement in Scala is also an expression that returns a value. Methods return the value of the last statement executed -- in this case `value` -- so explicit use of `return` is often unnecessary.

We always have to specify the types of arguments to methods, but Scala is often capable of inferring the return type. In this case, because the last expression in the method is of type `Int`, Scala also infers the return type of our method as `Int`. If we wanted to be more explicit about the return type we could equivalently write:

{% highlight scala %}
def add(num: Int): Int = {
  // And so on...
}
{% endhighlight %}

{% comment %}
- Defining classes
  - Instance variables
  - Methods
    - Uniform access principle
  - Constructors and constructor arguments
  - Creating instances
  - Case classes
- Working with classes
  - Abstracting common functionality
    - Traits
    - Trait composition
    - Self types
  - Objects and modules
    - Companion objects
    - Objects as modules
{% endcomment %}
