## Monads

We've seen that by implementing a few methods (`map`, `flatMap`, and optionally `filter` and `foreach`), we can use any class with a *for comprehension*. In the previous chapter we learned this such a class is called a *monad*. Here we are going to look in a bit more depth at monads.

### What's in a Monad?

The concept of a monad is notoriously difficult to explain because it is so general. We can get a good intuitive understanding by comparing some of the types of monad that we will deal with on a regular basis.

Broadly speaking, a monad is a generic type that allows us to sequence computations while abstracting away some technicality. We do the sequencing using *for comprehensions*, worrying only about the programming logic we care about. The code hidden in the monad's `map` and `flatMap` methods does all of the plumbing for us. For example:

 - `Option` is a monad that allows us to sequence computations on optional values without worrying about the fact that they may or may not be present;

 - `Seq` is a monad that allows us to sequence computations that return multiple possible answers without worrying about the fact that there are lots of possible combinations involved;

 - `Future` is another popular monad that allows us to sequence asynchronous computations without worrying about the fact that they are asynchronous.

To demonstrate the generality of this principle, here are some examples. This first example calculates the sum of two numbers that may or may not be there:

```tut:book:invisible
def getFirstNumber: Option[Int] = Some(2)
def getSecondNumber: Option[Int] = Some(5)
def getFirstNumbers: Seq[Int] = Seq(2, 3)
def getSecondNumbers: Seq[Int] = Seq(5, 6)
```

```tut:book:silent
for {
  a <- getFirstNumber  // getFirstNumber  returns Option[Int]
  b <- getSecondNumber // getSecondNumber returns Option[Int]
} yield a + b

// The final result is an Option[Int]---the result of
// applying `+` to `a` and `b` if both values are present
```

This second example calculate the sums of all possible pairs of numbers from two sequences:

```tut:book:silent
for {
  a <- getFirstNumbers  // getFirstNumbers  returns Seq[Int]
  b <- getSecondNumbers // getSecondNumbers returns Seq[Int]
} yield a + b

// The final result is a Seq[Int]---the results of
// applying `+` to all combinations of `a` and `b`
```

This third example asynchronously calculates the sum of two numbers that can only be obtained asynchronously (all without blocking):

```tut:book:silent
for {
  a <- getFirstNumber   // getFirstNumber  returns Future[Int]
  b <- getSecondNumber  // getSecondNumber returns Future[Int]
} yield a + b

// The final result is a Future[Int]---a data structure
// that will eventually allow us to access the result of
// applying `+` to `a` and `b`
```

The important point here is that, if we ignore the comments, *these three examples look identical*. Monads allow us to forget about one part of the problem at hand---optional values, multiple values, or asynchronously available values---and focus on just the part we care about---adding two numbers together.

There are many other monads that can be used to simplify problems in different circumstances. You may come across some of them in your future use of Scala. In this course we will concentrate entirely on `Seq` and `Option`.

### Exercises

#### Adding All the Things ++

We've already seen how we can use a for comprehension to neatly add together three optional values. Let's extend this to other monads. Use the following definitions:

```tut:book:silent
import scala.util.Try

val opt1 = Some(1)
val opt2 = Some(2)
val opt3 = Some(3)

val seq1 = Seq(1)
val seq2 = Seq(2)
val seq3 = Seq(3)

val try1 = Try(1)
val try2 = Try(2)
val try3 = Try(3)
```

Add together all the options to create a new option. Add together all the sequences to create a new sequence. Add together all the trys to create a new try. Use a for comprehension for each. It shouldn't take you long!

<div class="solution">
```tut:book:silent
for {
 x <- opt1
 y <- opt2
 z <- opt3
} yield x + y + z

for {
 x <- seq1
 y <- seq2
 z <- seq3
} yield x + y + z

for {
 x <- try1
 y <- try2
 z <- try3
} yield x + y + z
```

How's that for a cut-and-paste job?
</div>
