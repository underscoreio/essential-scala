---
layout: page
---

# Monadic Error Handling

In this section we're going to cover an extended example of monadic error handling. This will help consolidate several of the object-functional design patterns we've already seen, and provide a more indepth view of monads which we first encountered when discussing [../collections/index.md](collections).


## Java-style Error Handling

Let's start by quickly going over error handling using a familiar imperative/Java style in Scala. The first approach to error handling, which goes back at least as far as C, is to return a specific code (often `null`) on error. This leads to code which looks like

{% highlight scala %}
val code1 = doSomething()

if(code1 == null) {
  handleError1()
}

val code2 = doSomethingElse()

if(code2 == null) {
  handleError2()
}
{% endhighlight %}

The normal flow of the program is completely obscured by the error handling code. Furthermore the error handling code is fragile. There are no checks that we're checking for errors when we should and as this kind of code evolves it is easy for the error handling code to get out of sync with the rest of the code.

The standard alternative is to introduce exceptions. This allows us to write straight-line code and only introduce error handling where it's appropriate.

{% highlight scala %}
try {
  doSomething()
  doSomethingElse()
} catch {
  case exn: Exception => handleErrors(exn)
}
{% endhighlight %}

There are still several problems with this approach. Firstly, there is no static check that we've doing error handling correctly. In Java we'd have to at least declare which exceptions our methods throw (generally considered a design mistake) but this is not the case in Scala. Either way we're free to ignore this information (beyond annotating a method as `throws Exception` in Java) and carry on as if errors never occurred.

More insidious is the case in a concurrent program. Here we often delegate work to threads we obtain from a pool (an `ExecutorService` for example). Remember that exceptions are propagated up the stack. When we hand off work to another thread its stack is not our stack! This means:

1. We might not receive exceptions that happened in the delegated thread.
2. Even if we do receive an exception, the stack trace is often useless for debugging as it often contains no context information (since it doesn't share the stack of the thread that invoked the worker thread).

Now we've seen the justification for a different method of error handling, let's set out our desiderata:

1. We want the type system to enforce error handling -- we must either explicitly propagate errors or handle them.
2. We don't want the normal flow of the code to be obscured by error handling.
3. We want useful information on error.
4. We're prepared to learn a new concept to achieve this.


## Error Handling with Options

Let's start by looking at how we might handle errors using `Option`. Let's imagine that we have methods `doSomething` and `doSomethingElse` that each return an `Option[Int]`, with `Some[Int]` on success and `None` on failure. We could write code like this:

{% highlight scala %}
// Some silly methods for illustrative purposes
def doSomething(x: Int): Option[Int] =
  if(x < 0) None else Some(x * x)

def doSomethingElse(y: Int): Option[Int] =
  if(y % 2 == 0) None else Some(y / 2)

// Success
scala> doSomething(3) flatMap {
  y => doSomethingElse(y) map {
    z => z * 3
  }
}
res8: Option[Int] = Some(12)

// Failure
scala> doSomething(-3) flatMap {
  y => doSomethingElse(y) map {
    z => z * 3
  }
}
res9: Option[Int] = None
{% endhighlight %}

This has the desired effect. If our code fails at any point we return immediately with `None`, otherwise we continue processing a `Some[Int]`. We have achieved goal 1 above. It is rather verbose though; we haven't achieved our goal of clarity,.

The next step is to recognise that the pattern of `flatMap`/`map` above matches exactly the pattern of a for comprehension. We can write the code as

{% highlight scala %}
def errorHandlingExample(x: Int): Option[Int] =
  for {
    y <- doSomething(x)
    z <- doSomethingElse(y)
  } yield z * 3

scala> errorHandlingExample(3)
res10: Option[Int] = Some(12)

scala> errorHandlingExample(-3)
res11: Option[Int] = None
{% endhighlight %}

The code is much clearer now, and the type system enforces error handling. If we want to "break out" of the `Option` we have to specify what we're going to do with the error case:

{% highlight scala %}
scala> val result = errorHandlingExample(-3)

scala> result.getOrElse(0)
res12: Int = 0
{% endhighlight %}

This pattern is good, but we still don't have useful information on error -- `None` is not going to help us debug our programs. Our next step is to add this.


## Try Either or Validation

Instead of using `Option` to propagate errors we need a type that can carry information in the error case. There is a type called `Either` in the standard library, but it not specialised to error handling and there a little bit involved to use here. It's also instructive to create our own. Our type, called `Validation`, is loosely based on the type of the same name in the Scalaz library. Here's the basic definition:

{% highlight scala %}
sealed trait Validation[E,A]

case class Success[E,A](val success: A) extends Validation[E,A]

case class Failure[E,A](val failure: E) extends Validation[E,A]
{% endhighlight %}

Our base trait `Validation` is generic over two types, the failure type `E` and the success type `A`. It is sealed so we can be sure only the two concrete cases `Success` and `Failure` exist.

To make the definition above useful we need to define `flatMap` and `map` so we can use `Validation` in a for comprehension, and a way of getting a value out of a `Validation` (we used `getOrElse` on `Option` above for this purpose). If you recall from [../collections/index.md](collections) the generic traversal operator is conventionally called `fold`, so that we're going to call our "get a value out" method. The defintions are straightforward. We just need to make sure that `flatMap` and `map` don't do any further processing once a `Failure` has occurred.

{% highlight scala %}
sealed trait Validation[E,A] {
  def map[B](f: A => B): Validation[E,B]
  def flatMap[B](f: A => Validation[E,B]): Validation[E,B]
  def fold[X](success: A => X, failure: E => X): X
}

case class Success[E,A](val success: A) extends Validation[E,A] {
  def map[B](f: A => B): Validation[E,B] =
    Success(f(this.success))

  def flatMap[B](f: A => Validation[E,B]): Validation[E,B] =
    f(this.success)

  def fold[X](success: A => X, failure: E => X): X =
    success(this.success)
}

case class Failure[E,A](val failure: E) extends Validation[E,A] {
  def map[B](f: A => B): Validation[E,B] =
    Failure[E,B](this.failure)

  def flatMap[B](f: A => Validation[E,B]): Validation[E,B] =
    Failure[E,B](this.failure)

  def fold[X](success: A => X, failure: E => X): X =
    failure(this.failure)
}
{% endhighlight %}

We can now rewrite our original example using `Validation` and receive useful information on error. *Note the implementation of `errorHandling` has not changed!*

{% highlight scala %}
def doSomething(x: Int): Validation[String, Int] =
  if(x < 0) Failure("Cannot be zero") else Success(x * x)

def doSomethingElse(y: Int): Validation[String, Int] =
  if(y % 2 == 0) Failure("Cannot be even") else Success(y / 2)

def errorHandlingExample(x: Int): Validation[String, Int] =
  for {
    y <- doSomething(x)
    z <- doSomethingElse(y)
  } yield z * 3

scala> errorHandlingExample(3)
res15: Validation[String,Int] = Success(12)

scala> errorHandlingExample(-3)
res16: Validation[String,Int] = Failure(Cannot be zero)
{% endhighlight %}


## Monads Again
