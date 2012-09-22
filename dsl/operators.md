---
layout: page
---

# Operators

Recall that is Scala we can use any method as an operator. In particular

{% highlight scala %}
a b c
{% endhighlight %}

is equivalent to

{% highlight scala %}
a.b(c)
{% endhighlight %}

This simple feature allows us to create a more natural notation in many domains. A straightforward example is representing mathematical notation for extended number types. Here's how we can implement some basic operations on complex numbers.

{% highlight scala %}
case class Complex(re: Double, im: Double) {

  def +(other: Complex) = Complex(re + other.re, im + other.im)

  def *(other: Complex) =
    Complex(
      (re * other.re) - (im * other.im),
      (re * other.im) + (other.re * im)
    )

}

scala> Complex(1, 2) * Complex(3, 4)
res10: Complex = Complex(-5.0,10.0)
{% endhighlight %}

### Exercise

Implement a `Vector` type that wraps an `Array` and provides element-wise addition and multiplication. You can assume an `Array[Double]`. There is no need to check vectors have matching lengths in your implementation, but feel free to do so if you have time.

## Special Methods

Scala makes heavy use of convention in method names to reduce boilerplate syntax. We've already seen some examples of this:

- Use of single-argument methods as infix operators
- argumentless methods for field accessor syntax
- `foo_=` methods for assignment syntax
- `apply` for function application syntax `foo(...)`
- `update` for function application update `foo(...) = bar`

There are some more special method names you should be aware of

- `unapply`
- `unary_foo` methods where `foo` can be `+`, `-`, `~`, or `!`.

*TODO*

### Exercise

Extend your `Vector` implementation to support

- an `apply` method providing element access.
- an `update` method supporting element mutation.
- a `unary_-` method to perform element-wise negation of the `Vector`.


## Operator Associativity

Operator associativity is the final wrinkle in Scala's support for operators. Any methods that ends with a colon (`:`) is right-associative. That means `a op: b` is equivalent to `b.op:(a)` rather than `a.op:(b)`. We've seen this used extensively in the collection classes. For example, `+:` is the prepend operation on `Seq`.

## Final Words

Operator syntax, and particularly symbolic operators, can be powerful way for creating a concise syntax to express domain specific operations. However it can also be a great way to obscure your code. When creating symbolic operators you must consider the costs and benefits. Generally, if an operator has a well known meaning (e.g. the arthimetic operators) or is commonly known within a specific domain *and* dealing with the domain is a major part of the program you're writing then symbolic operators can be a good approach. In other cases caution is advised.
