# Monadic Error Handling

In this section we're going to cover an extended example of monadic error handling. This will help consolidate several of the object-functional design patterns we've already seen, and provide a more indepth view of monads which we first encountered when discussing [../collections/index.md](collections).


## Java-style Error Handling

Let's start by quickly going over error handling using a familiar imperative/Java style in Scala. The first approach to error handling, which goes back at least as far as C, is to return a specific code (often `null`) on error. This leads to code which looks like

```scala
val code1 = doSomething()

if(code1 == null) {
  handleError1()
}

val code2 = doSomethingElse()

if(code2 == null) {
  handleError2()
}
```

The normal flow of the program is completely obscured by the error handling code. Furthermore the error handling code is fragile. There are no checks that we're checking for errors when we should and as this kind of code evolves it is easy for the error handling code to get out of sync with the rest of the code.

The standard alternative is to introduce exceptions. This allows us to write straight-line code and only introduce error handling where it's appropriate.

```scala
try {
  doSomething()
  doSomethingElse()
} catch {
  case exn: Exception => handleErrors(exn)
}
```

There are still several problems with this approach. Firstly, there is no static check that we're doing error handling correctly. In Java we'd have to at least declare which exceptions our methods throw but this is not the case in Scala. Either way we're free to ignore this information (beyond annotating a method as `throws Exception` in Java) and carry on as if errors never occurred.

More insidious is the case in a concurrent program. Here we often delegate work to threads we obtain from a pool (an `ExecutorService` for example). Remember that exceptions are propagated up the stack. When we hand off work to another thread its stack is not our stack! This means:

1. We might not receive exceptions that happened in the delegated thread.
2. Even if we do receive an exception, the stack trace is often useless for debugging as it often contains little context information (since it doesn't share the stack of the thread that invoked the worker thread).

Now we've seen the justification for a different method of error handling, let's set out our desiderata:

1. We want the type system to enforce error handling---we must either explicitly propagate errors or handle them.
2. We don't want the normal flow of the code to be obscured by error handling.
3. We want useful information on error.
4. We're prepared to learn a new concept to achieve this.


## Error Handling with Options

Let's start by looking at how we might handle errors using `Option`. Let's imagine that we have methods `doSomething` and `doSomethingElse` that each return an `Option[Int]`, with `Some[Int]` on success and `None` on failure. We could write code like this:

```scala
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
```

This has the desired effect. If our code fails at any point we return immediately with `None`, otherwise we continue processing a `Some[Int]`. We have achieved goal 1 above. It is rather verbose though; we haven't achieved our goal of clarity,.

The next step is to recognise that the pattern of `flatMap`/`map` above matches exactly the pattern of a for comprehension. We can write the code as

```scala
def errorHandlingExample(x: Int): Option[Int] =
  for {
    y <- doSomething(x)
    z <- doSomethingElse(y)
  } yield z * 3

scala> errorHandlingExample(3)
res10: Option[Int] = Some(12)

scala> errorHandlingExample(-3)
res11: Option[Int] = None
```

The code is much clearer now, and the type system enforces error handling. If we want to "break out" of the `Option` we have to specify what we're going to do with the error case:

```scala
scala> val result = errorHandlingExample(-3)

scala> result.getOrElse(0)
res12: Int = 0
```

This pattern is good, but we still don't have useful information on error---`None` is not going to help us debug our programs. Our next step is to add this.


## Try Either or Validation

Instead of using `Option` to propagate errors we need a type that can carry information in the error case. There is a such type called `Either` in the standard library, but it not specialised to error handling and so is a little bit involved to use. It's also instructive to roll our own. Our type, called `Validation`, is loosely based on the type of the same name in the Scalaz library. Here's the basic definition:

```scala
sealed trait Validation[E,A]

case class Success[E,A](success: A) extends Validation[E,A]

case class Failure[E,A](failure: E) extends Validation[E,A]
```

Our base trait `Validation` is generic over two types, the failure type `E` and the success type `A`. It is sealed so we can be sure only the two concrete cases `Success` and `Failure` exist.

To make the definition above useful we need to define `flatMap` and `map` so we can use `Validation` in a for comprehension, and a way of getting a value out of a `Validation` (we used `getOrElse` on `Option` above for this purpose). If you recall from [../collections/index.md](collections) the generic traversal operator is conventionally called `fold`, so that we're going to call our "get a value out" method. The definitions are straightforward. We just need to make sure that `flatMap` and `map` don't do any further processing once a `Failure` has occurred.

```scala
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
```

We can now rewrite our original example using `Validation` and receive useful information on error.

*Note the implementation of `errorHandling` has not changed!* This is a consequence of using a high-level general abstraction. We can swap out the implementation but keep the code the same.

```scala
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
```

### Real World Usage

For real code [Scalaz's](https://github.com/scalaz/scalaz) Validation class is a good implementation. In Scala 2.10 the [Try](http://www.scala-lang.org/api/milestone/index.html#scala.util.Try) type is a credible alternative.

Monadic error handling is a pattern we use extensively in real deployed code. An example is a high-performance REST service. In this scenario the API is the user interface, so returning good error messages is very important.

We represent errors using a type called [`Problem`](https://github.com/bigtop/bigtop/blob/master/core/src/main/scala/bigtop/problem/Problem.scala). The key idea here is that we can convert a `Problem` to an HTTP response.

Here's a snippet of actual deployed code:

```scala
def newVariant(user: User, expt: String, variant: String): FutureValidation[Problem, Unit] =
  for {
    agent <- get(expt)
    good  <- addVariant(agent, user, variant)
  } yield ()

def deleteVariant(user: User, expt: String, variant: String): FutureValidation[Problem, Unit] =
  for {
    agent <- get(expt)
    good  <- removeVariant(agent, user, variant)
  } yield ()

def newExperiment(user: User, name: String): FutureValidation[Problem, JValue] =
  for {
    expt    <- addExperiment(user, name)
    val info = Experiment.externalFormat.write(expt)
  } yield info
```

You can see that this code is incredibly regular. It is very easy to read and very easy to write. Nonetheless it is a work in progress. We've discovered that it is difficult to localise errors without line numbers, so in the future we'll probably change `Problem` to be a subtype of `Exception`. Note I'm not saying we'll switch to using exceptions instead of monadic error handling! We will solely be using exceptions for their stack traces.


## Monads Again

So far we've looked at monads very informally. Now we will formalise the notation and talk a bit about the kind of things we can model witht e monad abstraction.

A monad implements `map` and `flatMap` with the following signature:

```scala
trait Monad[A] {
  def map[B](f: A => B): Monad[B]
  def flatMap[B](f: A => Monad[B]): Monad[B]
}
```

We also need a constructor, which in scala we'd typically implement a companion class `apply` method with signature

```scala
object Monad {
  def apply[A](in: A): Monad[A]
}
```

Finally there are some laws that monads should obey. They are:

### Left Identity

```scala
Monad(a) flatMap { x => f(x) } === f(a)
```

### Right Identity

```scala
Monad(a) flatMap { x => Monad(x) } === Monad(a)
```

### Associativity

```scala
(Monad(a) flatMap { x => f(x) }) flatMap { y => g(y) } === Monad(a) flatMap { x => f(x) flatMap { y => g(y) } }
```

A large number of abstractions can be modelled as monads. We've already seen collections of data and error-prone computations. Other common abstractions include state and IO.
