## Options

We have seen `Option`s in passing a number of times already---they represent values that may or may not be present in our code. Options are an alternative to using `null` that provide us with a means of chaining computations together without risking `NullPointerExceptions`. We have previously produced code in the spirit of `Option` with our `DivisionResult` and `Maybe` types in previous chapters.

Let's look into Scala's built-in `Option` type in more detail.

### Option, Some, and None

`Option` is a generic sealed trait with two subtypes---`Some` and `None`. Here is an abbreviated version of the code---we will fill in more methods as we go on:

~~~ scala
sealed trait Option[+A] {
  def getOrElse(default: A): A

  def isEmpty: Boolean
  def isDefined: Boolean = !isEmpty

  // other methods...
}

final case class Some[A](x: A) extends Option[A] {
  def getOrElse(default: A) = x

  def isEmpty: Boolean = false

  // other methods...
}

final case object None extends Option[Nothing] {
  def getOrElse(default: A) = default

  def isEmpty: Boolean = true

  // other methods...
}
~~~

Here is a typical item of code for generating an option---reading an integer from the user:

~~~ scala
def readInt(str: String): Option[Int] =
  if(str matches "\\d+") Some(str.toInt) else None
~~~

The `toInt` method of `String` throws a `NumberFormatException` if the string isn't a valid series of digits, so we have to guard its use with a regular expression. If the number is correctly formatted we return `Some` of the `Int` result. Otherwise we return `None`. Example usage:

~~~ scala
scala> readInt("123")
res7: Option[Int] = Some(123)

scala> readInt("abc")
res8: Option[Int] = None
~~~

### Extracting Values from Options

There are several ways to safely extract the value in an option without the risk of throwing any exceptions:

**Alternative 1: the `getOrElse` method**---useful if we want to fall back to a default value:

~~~ scala
scala> readInt("abc").getOrElse(0)
res9: Int = 0
~~~

**Alternative 2: pattern matching**---`Some` and `None` both have associated patterns that we can use in a `match` expression:

~~~ scala
scala> readInt("123") match {
         case Some(number) => number + 1
         case None         => 0
       }
res10: Int = 124
~~~

**Alternative 3: `map` and `flatMap`**---`Option` supports both of these methods, enabling us to chain off of the value within producing a new `Option`. This bears a more explanation---let's look at it in a little more detail.

### Options as Sequences

One way of thinking about an `Option` is as a sequence of 0 or 1 elements. In fact, `Option` supports many of the sequence operations we have seen so far:

~~~ scala
sealed trait Option[+A] {
  def getOrElse(default: A): A

  def isEmpty: Boolean
  def isDefined: Boolean = !isEmpty

  def filter(func: A => Boolean): Option[A]
  def find(func: A => Boolean): Option[A]

  def map[B](func: A => B): Option[B]
  def flatMap(func: A => Option[B]): Option[B]
  def foreach(func: A => Unit): Unit

  def foldLeft[B](initial: B)(func: (B, A) => B): B
  def foldRight[B](initial: B)(func: (A, B) => B): B
}
~~~

Because of the limited size of `0` or `1`, there is a bit of redundancy here: `filter` and `find` effectively do the same thing, and `foldLeft` and `foldRight` only differ int the order of their arguments. However, these methods give us a lot flexibility for manipulating optional values. For example, we can use `map` and `flatMap` to define optional versions of common operations:

~~~ scala
scala> def sum(optionA: Option[Int], optionB: Option[Int]): Option[Int] =
         optionA.flatMap(a => optionB.map(b => a + b))

scala> sum(readInt("1"), readInt("2"))
res12: Option[Int] = Some(3)

scala> sum(readInt("1"), readInt("b"))
res13: Option[Int] = None

scala> sum(readInt("a"), readInt("2"))
res14: Option[Int] = None
~~~

The implementation of `sum` looks complicated at first, so let's break it down:

 - If `optionA` is `None`, the result of `optionA.flatMap(foo)` is also `None`. The return value of `sum` is therefore `None`.

 - If `optionA` is `Some`, the result of `optionA.flatMap(foo)` is whatever value `foo` returns. This value is determined by the outcome of `optionB.map`:

    - If `optionB` is `None`, the result of `optionB.map(bar)` is also `None`. The return value of `sum` is therefore `None`.

    - If `optionB` is `Some`, the result of `optionB.map(bar)` is `Some` of the result of `bar`. In our case, the return value of `sum` is `a + b`.

Although `map` and `flatMap` don't allow us to *extract* values from our `Options`, they allow us to *compose computations together* in a safe manner. If all arguments to the computation are `Some`, the result is a `Some`. If any of the arguments are `None`, the result is `None`.

We can use `map` and `flatMap` in combination with pattern matching or `getOrElse` to combine several `Options` and yield a single non-optional result:

~~~ scala
scala> sum(readInt("1"), readInt("b")).getOrElse(0)
res17: Int = 0
~~~

It's worth noting that `Option` and `Seq` are also compatible in some sense. We can turn a `Seq[Option[A]]` into a `Seq[A]` using `flatMap`:

~~~ scala
scala> Seq(readInt("1"), readInt("b"), readInt("3")).flatMap(x => x)
res18: Seq[Int] = List(1, 3)
~~~

## Options as Flow Control

Because `Option` supports `map` and `flatMap`, it also works with for comprehensions. This gives us a nice syntax for combining values without resorting to building custom methods like `sum` to keep our code clean:

~~~ scala
val optionA = readInt("123")
val optionB = readInt("234")

for {
  a <- optionA
  b <- optionB
} yield a + b
~~~

In this code snippet `a` and `b` are both `Ints`---we can add them together directly using `+` in the `yield` block.

Let's stop to think about this block of code for a moment. There are three ways of looking at it:

 1. We can expand the block into calls to `map` and `flatMap`. You will be unsurprised to see that the resulting code is identical to our implementation of `sum` above:

    ~~~ scala
    optionA.flatMap(a => optionB.map(b => a + b))
    ~~~

 2. We can think of `optionA` and `optionB` as sequences of zero or one elements, in which case the is going to be a flattened sequence of length `optionA.length * optionB.length`[^option-length]. If either `optionA` or `optionB` is `None` then the result is of length `0`.

 3. We can think of each clause in the for comprehension as an expression that says: *if this clause results in a `Some`, extract the value and continue... if it results in a `None`, exit the for comprehension and return `None`*.

Once we get past the initial foreignness of using for comprehensions to "iterate through" options, we find a useful control structure that frees us from excessive use of `map` and `flatMap`.

[^option-length]: Note that `Option` doesn't actually have a `length` method---this example is for illustrative purposes only.

### Exercises

#### Adding Things

Write a method `addOptions` that accepts two parameters of type `Option[Int]` and adds them together. Use a for comprehension to structure your code.

<div class="solution">
We can reuse code from the text above for this:

~~~ scala
def addOptions(opt1: Option[Int], opt2: Option[Int]) =
  for {
    a <- opt1
    b <- opt2
  } yield a + b
~~~
</div>

Write a second version of your code using `map` and `flatMap` instead of a for comprehension.

<div class="solution">
The pattern is to use `flatMap` for all clauses except the innermost, which becomes a `map`:

~~~ scala
def addOptions2(opt1: Option[Int], opt2: Option[Int]) =
  opt1 flatMap { a =>
    opt2 map { b =>
      a + b
    }
  }
~~~
</div>

#### Adding All of the Things

Overload `addOptions` with another implementation that accepts three `Option[Int]` parameters and adds them all together.

<div class="solution">
For comprehensions can have as many clauses as we want so all we need to do is add an extra line to the previous solution:

~~~ scala
def addOptions(opt1: Option[Int], opt2: Option[Int], opt3: Option[Int]) =
  for {
    a <- opt1
    b <- opt2
    c <- opt3
  } yield a + b + c
~~~
</div>

Write a second version of your code using `map` and `flatMap` instead of a for comprehension.

<div class="solution">
Here we can start to see the simplicity of for comprehensions:

~~~ scala
def addOptions2(opt1: Option[Int], opt2: Option[Int], opt3: Option[Int]) =
  opt1 flatMap { a =>
    opt2 flatMap { b =>
      opt3 map { c =>
        a + b + c
      }
    }
  }
~~~
</div>

#### A(nother) Short Division Exercise

Write a method `divide` that accepts two `Int` parameters and divides one by the other. Use `Option` to avoid exceptions when the denominator is `0`.

<div class="solution">
We saw this code in the [Traits](/traits/) chapter when we wrote the `DivisionResult` class. The implementation is much simpler now we can use `Option` to do the heavy lifting:

~~~ scala
def divide(numerator: Int, denominator: Int) =
  if(denominator < 1) None else Some(numerator / denominator)
~~~
</div>

Using your `divide` method and a for comprehension, write a method called `divideOptions` that accepts two parameters of type `Option[Int]` and divides one by the other:

<div class="solution">
In this example the `divide` operation returns an `Option[Int]` instead of an `Int`. In order to process the result we need to move the calculation from the `yield` block to a for-clause:

~~~ scala
def divideOptions(numerator: Option[Int], denominator: Option[Int]) =
  for {
    a <- numerator
    b <- denominator
    c <- divide(a, b)
  } yield c
~~~
</div>

#### A Simple Calculator

A final, longer exercise. Write a method called `calculator` that accepts three string parameters:

~~~ scala
def calculator(operand1: String, operator: String, operand2: String): Unit
~~~

and behaves as follows:

 1. Convert the operands to `Ints`;

 2. Perform the desired mathematical operator on the two operands:

     - provide support for at least four operations: `+`, `-`, `*` and `/`;
     - use `Option` to guard against errors (invalid inputs or division by zero).

 3. Finally print the result or a generic error message.

**Tip:** Start by supporting just one operator before extending your method to other cases.

<div class="solution">
The trick to this one is realising that each clause in the *for* comprehension can contain an entire block of Scala code:

~~~ scala
def calculator(operand1: String, operator: String, operand2: String): Unit = {
  val result = for {
    a   <- readInt(operand1)
    b   <- readInt(operand2)
    ans <- operator match {
             case "+" => Some(a + b)
             case "-" => Some(a - b)
             case "*" => Some(a * b)
             case "/" => divide(a, b)
             case _   => None
           }
  } yield ans

  ans match {
    case Some(number) => println(s"The answer is $number!")
    case None         => println(s"Error calculating $operand1 $operator $operand2")
  }
}
~~~

Another approach involves factoring the calculation part out into its own private function:

~~~ scala
def calculator(operand1: String, operator: String, operand2: String): Unit = {
  def calcInternal(a: Int, b: Int) =
    operator match {
      case "+" => Some(a + b)
      case "-" => Some(a - b)
      case "*" => Some(a * b)
      case "/" => divide(a, b)
      case _   => None
    }

  val result = for {
    a   <- readInt(operand1)
    b   <- readInt(operand2)
    ans <- calcInternal(a, b)
  } yield ans

  ans match {
    case Some(number) => println(s"The answer is $number!")
    case None         => println(s"Error calculating $operand1 $operator $operand2")
  }
}
~~~
</div>

For the enthusiastic only, write a second version of your code using `flatMap` and `map`.

<div class="solution">
This version of the code is much clearer if we factor out the calculation part into its own function. Without this it would be very hard to read:

~~~ scala
def calculator(operand1: String, operator: String, operand2: String): Unit = {
  def calcInternal(a: Int, b: Int) =
    operator match {
      case "+" => Some(a + b)
      case "-" => Some(a - b)
      case "*" => Some(a * b)
      case "/" => divide(a, b)
      case _   => None
    }

  val result =
    readInt(operand1) flatMap { a =>
      readInt(operand2) flatMap { b =>
        calcInternal(a, b) map { result =>
          result
        }
      }
    }

  result match {
    case Some(number) => println(s"The answer is $number!")
    case None         => println(s"Error calculating $operand1 $operator $operand2")
  }
}
~~~
</div>
