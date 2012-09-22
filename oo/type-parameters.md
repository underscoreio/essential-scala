---
layout: page
---

# Type parameters

*Type parameters* allow us to create *generic* classes and traits that can be used with a variety of types of data. The classic example is a collection such as a *stack*:

{% highlight scala %}
scala> class Stack[T] {
     |   var items: List[T] = Nil
     |
     |   def push(item: T): Unit = {
     |     items = item :: items
     |   }
     |
     |   def pop(): T = {
     |     val answer = items.head
     |     items = items.tail
     |     answer
     |   }
     | }
{% endhighlight %}

Scala uses square brackets in place of Java's echelons to denote type parameters: `Stack[T]` is a generic class with a single type parameter, `T`.

We can instantiate `Stack` by specifying a concrete type value for `T`. Scala's unified type hierarchy allows us to use `Stack` with any value type as well as any reference type. Here we create a `Stack[Int]`:

{% highlight scala %}
scala> val intStack = new Stack[Int]
intStack: Stack[Int] = Stack@7894b886

scala> :paste
// Entering paste mode (ctrl-D to finish)

intStack.push(1)
intStack.push(2)
intStack.push(3)
List(intStack.pop(), intStack.pop(), intStack.pop())

// Exiting paste mode, now interpreting.

res1: List[Int] = List(3, 2, 1)

scala> intStack.push(1.0)
<console>:10: error: type mismatch;
 found   : Double(1.0)
 required: Int
              intStack.push(1.0)
                            ^
{% endhighlight %}

## Type inference

We can sometimes omit type parameters when creating classes and allow Scala to infer them for us. For example, we can modify the definition of `Stack` as follows:

{% highlight scala %}
scala> class Stack[T](initial: T) {
     |   var items: List[T] = initial :: Nil
     |
     |   def push(item: T): Unit = {
     |     items = item :: items
     |   }
     |
     |   def pop(): T = {
     |     val answer = items.head
     |     items = items.tail
     |     answer
     |   }
     |
     |   def toString = {
     |     "Stack(" + items + ")""
     |   }
     | }
{% endhighlight %}

Now the type checker is able to infer `T` from the type of argument we pass to the constructor:

{% highlight scala %}
scala> val doubleStack = new Stack(0.0)
doubleStack: Stack[Double] = Stack@9a4d5c6
{% endhighlight %}

While the type checker is often able to infer the types we need, we sometimes need to give it a helping hand. We can manually specify the type bindings we need by adding a set of square brackets to our constructor call. For example:

{% highlight scala %}
scala> val somethingStack = new Stack(null)
somethingStack: Stack[Null] = Stack@3a51ce0d

scala> val stringStack = new Stack[String](null)
stringStack: Stack[String] = Stack@52c751fd
{% endhighlight %}

In this example, we start by creating `somethingStack` with the value `null`. Because we leave Scala to infer the value of `T`, we get a `Stack[Null]`. This is technically correct, but probably not what we want. In the creation of `stringStack`, we manually bind `T` to `String`. Even though the constructor argument is not a `String` the expression type checks and we get a `Stack[String]` as required.

## Type parameters on methods

In addition to placing type parameters on classes and traits, Scala allows us to specify type parameters on individual methods. This is useful, for example, when we want a method to return the same type of data that we pass to it:

{% highlight scala %}
scala> object Debug {
     |   def printValue[T](arg: T): T = {
     |     println(arg)
     |     arg
     |   }
     | }

scala> printValue(123)
123
res0: Int = 123

scala> printValue("123")
123
res1: java.lang.String = 123
{% endhighlight %}

In this example, the type parameter `T` is scoped to the `printValue` method. Scala's type checker attempts to infer a suitable binding for `T` for each call location. The first time we call `printValue` we pass an `Int` as an argument. Scala infers that `T` should be bound to `Int`, and so the return type of the method is `Int`. The second time we call `printValue` we pass a `String` as an argument, so the return type is also `String`.

By choosing appropriate bindings at compile time, Scala is able to statically detect potential errors without running our code. Here, for example, the type checker reveals a discrepancy between the argument type of `printValue` and the return type we need to populate `x`:

{% highlight scala %}
scala> val x: Int = printValue("123")
<console>:11: error: type mismatch;
 found   : java.lang.String("123")
 required: Int
       val x: Int = printValue("123")
                               ^
{% endhighlight %}

We can manually specify type arguments to methods in the same way we did for constructors:

{% highlight scala %}
scala> val x = printValue(null)
null
x: Null = null

scala> val x = printValue[String](null)
null
x: String = null
{% endhighlight %}

{% comment %}

## Upper type bounds

TODO: COMPLETE THIS BIT

It is sometimes useful to put *bounds* on the type parameters to a class, trait, or method. This restricts the set of types we can pass, and allows us to rely on certain functionality being present in the type parameter.

For example, suppose we are designing a software application for access by staff and students at a University:

{% highlight scala %}
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait User {
  def username: String
  def isAdmin: Boolean
}

class Staff(val username: String, val isAdmin: Boolean) extends User

class Student(val username: String) extends User {
  val isAdmin = false
}

// Exiting paste mode, now interpreting.

defined trait User
defined class Student
defined class Staff
{% endhighlight %}

As part of our authentication system, we want to write a function that asserts whether a user is an administrator. We want the authentication function to work with `Staff` or `Student` objects and retain the type information:

{% highlight scala %}
scala> object Auth {
     |   def adminOnly(user: U): U = {
     |     if(user.isAdmin) {
     |       user
     |     } else {
     |       throw new Exception("Acccess denied!")
     |     }
     |   }
     | }
<console>:12: error: value saySomething is not a member of type parameter T
         println(arg.saySomething.toUpperCase)
{% endhighlight %}

The class `Echo` won't compile because the type checker can't guarantee that `T` has a method `saySomething`. We can place an *upper type bound* on `T` using Scala's `<:` operator to tell the type checker that `T` has to be a subtype of `Vocal`:

{% highlight scala %}
scala> class Echo[T <: Vocal](arg: T) {
     |   println(arg.saySomething.toUpperCase)
     |   println(arg.saySomething)
     |   println(arg.saySomething.toLowerCase)
     | }
defined class Echo
{% endhighlight %}

## Covariance

## Contravariance

## Lower type bounds

{% endcomment %}
