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

This simple feature allows us to create a more natural notation in many domains. A straightforward example is representing mathematical notation for extended number types (e.g. complex numbers).

Complex number example here

### Exercise

Implement a vector type allowing element-wise addition and multiplication

## Operator Associativity

## Special Methods

 - scala makes heavy use of convention in method names to reduce boilerplate syntax
 - we've already seen three examples of this:
    - use of single-argument methods as infix operators
    - argumentless methods for field accessor syntax
    - foo_= methods for assignment syntax
 - there are some more special method names you should be aware of
 - apply
 - update
 - unapply
 - unary_foo
