---
layout: page
title: Recursive Data
---

A particular use of algebraic data types that comes up very often is defining **recursive data**. This is data that is defined in terms of itself, and allows us to create data of potentially unbounded size (though any concrete instance will be finite).

We can't define recursive data like[^lazy-data]

~~~ scala
final case class Broken(broken: Broken)
~~~

as we could never actually create an instance of such a type -- the recursion never ends.
To define valid recursive data we must define a **base case**, which is the case that ends the recursion.

[^lazy-data]: We actually can define data in this manner if we delay the construction of the recursive case, like `final case class LazyList(head: Int, tail: () => LazyList)`. This uses a feature of Scala, functions, that we haven't seen yet. We can do some fairly mind-bending things with this construction, such as defining an infinite stream of ones with the declaration `val ones: LazyList = LazyList(1, () => ones)`. Since we only ever realise a finite amount of this list we can use it to implement certain types of data that would be difficult to implement in other ways. If you're interested in exploring this area further, what we have implemented in called a lazy list, and an "odd lazy list" in particular. The "even list", described in [How to add laziness to a strict language wihtout even being odd](http://www.cs.rice.edu/~taha/publications/conference/sml98.pdf), is a better implementation. Going further, is a rich literature on lazy datastructures and more mind melting theory under the name of "coinductive data".

Here is a more useful recursive definition: an `IntList` is either the `Empty` list, or a `Cell`[^pair] containing an `Int` and an `IntList`. We can directly translate this to code using our familiar patterns:

~~~ scala
sealed trait IntList
final case object Empty extends IntList
final case class Cell(head: Int, tail: IntList) extends IntList
~~~

[^pair]: The traditional name this element is a `Pair` or a `Cons` cell. We don't use the former because Scala already defines a datatype called Pair, and we don't use the later because it would require some explanation.

Here `Empty` is the base case. We construct the list containing `1`, `2`, and `3` as follows:

~~~ scala
Cell(1, Cell(2, Cell(3, Empty)))
~~~

We can apply the same structural recursion patterns to process a recursive algebraic data type. The only wrinkle is that we must make a recursive call when the data definition is recursion.

Let's add together all the elements of an `IntList`. We'll use pattern matching, but as we know the same process applies to using polymorphism.

Start with the tests and method declaration.

~~~ scala
val example = Cell(1, Cell(2, Cell(3, Empty)))
assert(sum(example) == 6)
assert(sum(example.tail) == 5)
assert(sum(example.tail.tail == 3)
assert(sum(Empty) == 0)

def sum(list: IntList): Int = ???
~~~

Note how the tests define `0` to be the sum of the elements of an `Empty` list. It is important that we define an appropriate base case for our method as we will build our final result of this base case.

Now we apply our structural recursion pattern to fill out the body of the method.

~~~ scala
def sum(list: IntList): Int =
  list match {
    case Empty => ???
    case Cell(hd, tl) => ???
  }
~~~

Finally we have to decide on the bodies of our cases. We have already decided that `0` is answer for `Empty`. For `Cell` we have two bits of information to guide us. We know we need to return an `Int` and we know that we need to make a recursive call on `tl`. Let's fill in what we have.

~~~ scala
def sum(list: IntList): Int =
  list match {
    case Empty => 0
    case Cell(hd, tl) => ??? sum(tl)
  }
~~~

The recursive call will return the sum of the tail of the list, by definition. Thus the correct thing to do is to add `hd` to this result. This gives us our final result:

~~~ scala
def sum(list: IntList): Int =
  list match {
    case Empty => 0
    case Cell(hd, tl) => hd + sum(tl)
  }
~~~

## Understanding the Base Case and Recursive Case

Our patterns will carry us most of the way to a correct answer, but we still need to supply the method bodies for the base and recursive cases. There is some general guidance we can use:

- For the base case we should generally return the *identity* for the function we're trying to compute. The identity is an element that doesn't change the result. E.g. 0 is the identity for addition, because `a + 0 == a` for any `a`. If we were calculating the product
of elements the identity would be 1 as `a * 1 == a` for all `a`.

- For the recursive case, assume the recursion will return the correct result and work out what you need to add to get the correct answer. We saw this for `sum`, where we assume the recursive call will give us the correct result for the tail of the list and when then just add on the head.

<div class="callout callout-info">
#### Recursive Algebraic Data Types Pattern

When defining recursive algebraic data types, there must be at least two cases: one that is recursive, and one that is not. Cases that are not recursive are known as base cases. In code, the general skeleton is:

~~~ scala
sealed trait RecursiveExample
final case class RecursiveCase(recursion: RecursiveExample) extends RecursiveExample
final case class BaseCase() extends RecursiveExample
~~~
</div>

<div class="callout callout-info">
#### Recursive Structural Recursion Pattern

When writing structurally recursive code on a recursive algebraic data type:

- whenever we encounter a recursive element in the data we make a recursive call to our method; and
- whenever we encounter a base case in the data we return the identity for the operation we are performing.
</div>

## Tail Recursion

You may be concerned that recursive calls will consume excessive stack space. Scala can apply an optimisation, called **tail recursion**, to many recursive functions to stop them consuming stack space.

A tail call is a method call where the caller immediately returns the value. So this is a tail call

~~~ scala
def method1: Int =
  1

def tailCall: Int =
  method1
~~~

because `tailCall` immediately returns the result of calling `method1` while

~~~ scala
def notATailCall: Int =
  method1 + 2
~~~

because `notATailCall` does not immediatley return -- it adds an number to the result of the call.

A tail call can be optimised to not use stack space. Due to limitations in the JVM, Scala only optimises tail calls where the caller calls itself. Since tail recursion is an important property to maintain, we can use the `@tailrec` annotation to ask the compiler to check that methods we believe are tail recursion really are. Here we have two versions of `sum` annotated. One is tail recursive and one is not. You can see the compiler complains about the method that is not tail recursive.

~~~ scala
import scala.annotation.tailrec
import scala.annotation.tailrec

scala> @tailrec
       def sum(list: IntList): Int =
         list match {
           case Empty => 0
           case Cell(hd, tl) => hd + sum(tl)
         }
<console>:15: error: could not optimize @tailrec annotated method sum: it contains a recursive call not in tail position
         list match {
         ^

scala> @tailrec
       def sum(list: IntList, total: Int = 0): Int =
         list match {
           case Empty => total
           case Cell(hd, tl) => sum(tl, total + hd)
         }
sum: (list: IntList, total: Int)Int
~~~~

Any non-tail recursion function can be transformed into a tail recursive version by adding an accumulator as we have done with `sum` above. This transforms stack allocation into heap allocation, which sometimes is a win, and other times is not.

In Scala we tend not to work directly with tail recursive functions as there is a rich collections library that covers the most common cases where tail recursion is used. Should you need to go beyond this, because you're implementing your own datatypes or are optimising code, it is useful to know about tail recursion.

## Exercises

Using our definition of `IntList`

~~~ scala
sealed trait IntList
final case object Empty extends IntList
final case class Cell(head: Int, tail: IntList) extends IntList
~~~

define a method `length` that returns the length of the list using pattern matching.

<div class="solution">
def length(list: IntList): Int =
  list match {
    case Empty => 0
    case Cell(hd, tl) => 1 + length(tl)
  }
</div>

Now define `length` using pattern matching.

<div class="solution">
~~~ scala
sealed trait IntList {
  def length: Int
}
final case object Empty extends IntList {
  def length: Int =
    0
}
final case class Cell(head: Int, tail: IntList) extends IntList {
  def length: Int =
    1 + tail.length
}
~~~
</div>

Using polymorphism and pattern matching, define a method to compute the product of the elements in an `IntList`.

<div class="solution">
~~~ scala
def product(list: IntList): Int =
  list match {
    case Empty => 1
    case Cell(hd, tl) => hd * product(tl)
  }

sealed trait IntList {
  def product: Int
}
final case object Empty extends IntList {
  def product: Int =
    1
}
final case class Cell(head: Int, tail: IntList) extends IntList {
  def product: Int =
    head * tail.product
}
~~~
</div>

Using both polymorphism and pattern matching, define a method to double the value of element in an `IntList`, returning a new `IntList`. The following test cases should hold:

~~~ scala
assert(Empty.double == Empty)
assert(double(Empty) == Empty)

assert(Cell(1, Empty).double == Cell(2, Empty))
assert(double(Cell(1, Empty)) == Cell(2, Empty))

assert(Cell(2, Cell(1, Empty)).double == Cell(4, Cell(2, Empty)))
assert(double(Cell(2, Cell(1, Empty))) == Cell(4, Cell(2, Empty)))
~~~

<div class="solution">
~~~ scala
def double(list: IntList): Int =
  list match {
    case Empty => Empty
    case Cell(hd, tl) => Cell(hd * 2, double(tl))
  }

sealed trait IntList {
  def double: Int
}
final case object Empty extends IntList {
  def double: Int =
    Empty
}
final case class Cell(head: Int, tail: IntList) extends IntList {
  def double: Int =
    Cell(head * 2, tail.double)
}
~~~
</div>

### The Forest of Trees

A binary tree of integers can be defined as follows:

A `Tree` is a `Node` with a left and right `Tree` or a `Leaf` with an element of type `Int`.

Implement this algebraic data type.

<div class="solution">
~~~ scala
sealed trait Tree
final case class Node(val l: Tree, val r: Tree) extends Tree
final case class Leaf(val elt: Int) extends Tree
~~~
</div>

Implement `sum` and `double` on `Tree` using polymorphism and pattern matching.

<div class="solution">
~~~ scala
object TreeOps {
  def sum(tree: Tree): Int =
    tree match {
      case Leaf(elt) => elt
      case Node(l, r) => sum(l) + sum(r)
    }
  }

  def double(tree: Tree): Tree =
    tree match {
      case Leaf(elt) => Leaf(elt * 2)
      case Node(l, r) => Node(double(l), double(r))
    }
}

sealed trait Tree {
  def sum: Int
  def double: Tree
}
final case class Node(val l: Tree, val r: Tree) extends Tree {
  def sum: Int =
    l.sum + r.sum

  def double: Tree =
    Node(l.double, r.double)
}
final case class Leaf(val elt: Int) extends Tree {
  def sum: Int =
    elt

  def double: Tree =
    Leaf(elt * 2)
}
~~~
</div>

#### A Calculator

We're now going to work on a larger problem to implement a simple interpreter for programs containing only numeric operations.

We start by defining some types to represent the expressions we'll be operating on. In the compiler literature this is known as an *abstract syntax tree*.

Our representation is:

- An `Expression` is an `Addition`, `Subtraction`, or a `Number`;
- An Addition has a `left` and `right` Expression;
- A Subtraction has a `left` and `right` Expression; or
- A Number has a `value` of type `Double`.

Implement this in Scala.

<div class="solution">
This is a straightforward algebraic data type.

~~~ scala
sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Number(value: Int) extends Expression
~~~
</div>

Now implement a method `eval` that converts an `Expression` to a `Double`. Use polymorphism or pattern matching as you see fit. Explain your choice of implementation method.

<div class="solution">
I used polymorphism for the following reason:

- `eval` doesn't depend on any other data than that stored in `Expression`; and
- polymorphism is more idiomatic in Scala, when appropriate.

~~~ scala
sealed trait Expression {
  def eval: Double
}
final case class Addition(left: Expression, right: Expression) extends Expression {
  def eval: Double =
    (left.eval.value + right.eval.value)
}
final case class Subtraction(left: Expression, right: Expression) extends Expression {
  def eval: Double =
    (left.eval.value - right.eval.value)
}
final case class Number(value: Int) extends Expression {
  def eval: Double =
    value
}
~~~
</div>

We're now going to add some expressions that call fail: division and square root. Start by extending the abstract syntax tree to include representations for `Division` and `SquareRoot`.

<div class="solution">
~~~ scala
sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Int) extends Expression
~~~
</div>

Now we're going to change `eval` to represent that a computation can fail. (`Double` generally used `NaN` to indicate a computation failed, but we want to be helpful to the user and tell them why the computation failed.) Implement an appropriate algebraic data type.

<div class="solution">
We did this in the previous section.

~~~ scala
sealed trait Calculation
final case class Success(result: Double) extends Calculation
final case class Failure(reason: String) extends Calculation
~~~
</div>

Now change `eval` to return your result type, which I have called `Calculation` in my implementation. Here are some examples:

~~~ scala
assert(Addition(SquareRoot(Number(-1)), Number(2)).eval ==
       Failure("Square root of negative number"))
assert(Addition(SquareRoot(Number(4)), Number(2)).eval == Success(4.0))
~~~

<div class="solution">
All this repeated pattern matching gets very tedious, doesn't it! We're going to see how we can abstract this in the next section.

~~~ scala
sealed trait Expression {
  def eval: Calculation
}
final case class Addition(left: Expression, right: Expression) extends Expression {
  def eval: Calculation =
    left.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        right.eval match {
          case Failure(reason) => Failure(reason)
          case Success(r2) => Success(r1 + r2)
        }
    }
}
final case class Subtraction(left: Expression, right: Expression) extends Expression {
  def eval: Calculation =
    left.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        right.eval match {
          case Failure(reason) => Failure(reason)
          case Success(r2) => Success(r1 - r2)
        }
    }
}
final case class Division(left: Expression, right: Expression) extends Expression {
  def eval: Calculation =
    left.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        right.eval match {
          case Failure(reason) => Failure(reason)
          case Success(r2) =>
            if(r2 == 0)
              Failure("Division by zero")
            else
              Success(r1 / r2)
        }
    }
}
final case class SquareRoot(value: Expression) extends Expression {
  def eval: Calculation =
    value.eval match {
      case Failure(reason) => Failure(reason)
      case Success(r1) =>
        if(r1 < 0)
          Failure("Square root of negative number")
        else
          Success(Math.sqrt(r1))
    }
}
final case class Number(value: Int) extends Expression {
  def eval: Calculation =
    Success(value)
}
~~~
</div>
