## Recursive Data

A particular use of algebraic data types that comes up very often is defining *recursive data*. This is data that is defined in terms of itself, and allows us to create data of potentially unbounded size (though any concrete instance will be finite).

We can't define recursive data like[^lazy-data]

```scala
final case class Broken(broken: Broken)
```

as we could never actually create an instance of such a type---the recursion never ends.
To define valid recursive data we must define a *base case*, which is the case that ends the recursion.

[^lazy-data]: We actually can define data in this manner if we delay the construction of the recursive case, like `final case class LazyList(head: Int, tail: () => LazyList)`. This uses a feature of Scala, functions, that we haven't seen yet. We can do some fairly mind-bending things with this construction, such as defining an infinite stream of ones with the declaration `val ones: LazyList = LazyList(1, () => ones)`. Since we only ever realise a finite amount of this list we can use it to implement certain types of data that would be difficult to implement in other ways. If you're interested in exploring this area further, what we have implemented in called a lazy list, and an "odd lazy list" in particular. The "even list", described in [How to add laziness to a strict language wihtout even being odd](http://www.cs.rice.edu/~taha/publications/conference/sml98.pdf), is a better implementation. If you wish to explore further, there is a rich literature on lazy datastructures and more mind melting theory under the name of "coinductive data".

Here is a more useful recursive definition: an `IntList` is either the empty list `End`, or a `Pair`[^pair] containing an `Int` and an `IntList`. We can directly translate this to code using our familiar patterns:

```scala
sealed trait IntList
final case object End extends IntList
final case class Pair(head: Int, tail: IntList) extends IntList
```

[^pair]: The traditional name this element is a `Cons` cell. We don't use this name as it's a bit confusing if you don't know the story behind it.

Here `End` is the base case. We construct the list containing `1`, `2`, and `3` as follows:

```scala
Pair(1, Pair(2, Pair(3, End)))
```

This data structure is known as a singly-linked list. In this example we have four links in our chain. We can write this out in a longer form to better understand the structure of the list. Below, `d` represents an empty list, and `a`, `b`, and `c` are pairs built on top of it.

```scala
val d = End()
val c = Pair(3, d)
val b = Pair(2, c)
val a = Pair(1, b)
```

In addition to being links in a chain, these data structures all represent complete sequences of integers:

 - `a` represents the sequence `1, 2, 3`
 - `b` represents the sequence `2, 3`
 - `c` represents the sequence `3` (only one element)
 - `d` represents an empty sequence

Using this implementation, we can build lists of arbitrary length by repeatedly taking an existing list and prepending a new element[^list].

[^list]: This is how Scala's built-in `List` data structure works. We will be introduced to `List` in the chapter on *Collections*.

We can apply the same structural recursion patterns to process a recursive algebraic data type. The only wrinkle is that we must make a recursive call when the data definition is recursion.

Let's add together all the elements of an `IntList`. We'll use pattern matching, but as we know the same process applies to using polymorphism.

Start with the tests and method declaration.

```scala
val example = Pair(1, Pair(2, Pair(3, End)))
assert(sum(example) == 6)
assert(sum(example.tail) == 5)
assert(sum(End) == 0)

def sum(list: IntList): Int = ???
```

Note how the tests define `0` to be the sum of the elements of an `End` list. It is important that we define an appropriate base case for our method as we will build our final result of this base case.

Now we apply our structural recursion pattern to fill out the body of the method.

```scala
def sum(list: IntList): Int =
  list match {
    case End => ???
    case Pair(hd, tl) => ???
  }
```

Finally we have to decide on the bodies of our cases. We have already decided that `0` is answer for `End`. For `Pair` we have two bits of information to guide us. We know we need to return an `Int` and we know that we need to make a recursive call on `tl`. Let's fill in what we have.

```scala
def sum(list: IntList): Int =
  list match {
    case End => 0
    case Pair(hd, tl) => ??? sum(tl)
  }
```

The recursive call will return the sum of the tail of the list, by definition. Thus the correct thing to do is to add `hd` to this result. This gives us our final result:

```scala
def sum(list: IntList): Int =
  list match {
    case End => 0
    case Pair(hd, tl) => hd + sum(tl)
  }
```

### Understanding the Base Case and Recursive Case

Our patterns will carry us most of the way to a correct answer, but we still need to supply the method bodies for the base and recursive cases. There is some general guidance we can use:

- For the base case we should generally return the *identity* for the function we're trying to compute. The identity is an element that doesn't change the result. E.g. 0 is the identity for addition, because `a + 0 == a` for any `a`. If we were calculating the product of elements the identity would be 1 as `a * 1 == a` for all `a`.

- For the recursive case, assume the recursion will return the correct result and work out what you need to add to get the correct answer. We saw this for `sum`, where we assume the recursive call will give us the correct result for the tail of the list and we then just add on the head.

<div class="callout callout-info">
#### Recursive Algebraic Data Types Pattern {-}

When defining recursive algebraic data types, there must be at least two cases: one that is recursive, and one that is not. Cases that are not recursive are known as base cases. In code, the general skeleton is:

```scala
sealed trait RecursiveExample
final case class RecursiveCase(recursion: RecursiveExample) extends RecursiveExample
final case object BaseCase extends RecursiveExample
```
</div>

<div class="callout callout-info">
#### Recursive Structural Recursion Pattern {-}

When writing structurally recursive code on a recursive algebraic data type:

- whenever we encounter a recursive element in the data we make a recursive call to our method; and
- whenever we encounter a base case in the data we return the identity for the operation we are performing.
</div>

### Tail Recursion

You may be concerned that recursive calls will consume excessive stack space. Scala can apply an optimisation, called *tail recursion*, to many recursive functions to stop them consuming stack space.

A tail call is a method call where the caller immediately returns the value. So this is a tail call

```scala
def method1: Int =
  1

def tailCall: Int =
  method1
```

because `tailCall` immediately returns the result of calling `method1` while

```scala
def notATailCall: Int =
  method1 + 2
```

because `notATailCall` does not immediatley return---it adds an number to the result of the call.

A tail call can be optimised to not use stack space. Due to limitations in the JVM, Scala only optimises tail calls where the caller calls itself. Since tail recursion is an important property to maintain, we can use the `@tailrec` annotation to ask the compiler to check that methods we believe are tail recursion really are. Here we have two versions of `sum` annotated. One is tail recursive and one is not. You can see the compiler complains about the method that is not tail recursive.

```scala
scala> import scala.annotation.tailrec
import scala.annotation.tailrec

scala> @tailrec
       def sum(list: IntList): Int =
         list match {
           case End => 0
           case Pair(hd, tl) => hd + sum(tl)
         }
<console>:15: error: could not optimize @tailrec annotated method sum: it contains a recursive call   ↩
                     not in tail position
         list match {
         ^

scala> @tailrec
       def sum(list: IntList, total: Int = 0): Int =
         list match {
           case End => total
           case Pair(hd, tl) => sum(tl, total + hd)
         }
sum: (list: IntList, total: Int)Int
```~

Any non-tail recursion function can be transformed into a tail recursive version by adding an accumulator as we have done with `sum` above. This transforms stack allocation into heap allocation, which sometimes is a win, and other times is not.

In Scala we tend not to work directly with tail recursive functions as there is a rich collections library that covers the most common cases where tail recursion is used. Should you need to go beyond this, because you're implementing your own datatypes or are optimising code, it is useful to know about tail recursion.

### Exercises

#### A List of Methods

Using our definition of `IntList`

```scala
sealed trait IntList
final case object End extends IntList
final case class Pair(head: Int, tail: IntList) extends IntList
```

define a method `length` that returns the length of the list. There is test data below you can use to check your solution. For this exercise it is best to use pattern matching in the base trait.

```scala
val example = Pair(1, Pair(2, Pair(3, End)))

assert(example.length == 3)
assert(example.tail.length == 2)
assert(End.length == 0)
```

<div class="solution">
```scala
sealed trait IntList {
  def length: Int =
    this match {
      case End => 0
      case Pair(hd, tl) => 1 + tl.length
    }
}
final case object End extends IntList
final case class Pair(head: Int, tail: IntLIst) extends IntList
```
</div>

Define a method to compute the product of the elements in an `IntList`. Test cases are below.

```scala
assert(example.product == 6)
assert(example.tail.product == 6)
assert(End.product == 1)
```

<div class="solution">
```scala
sealed trait IntList {
  def product: Int =
    this match {
      case End => 1
      case Pair(hd, tl) => hd * tl.product
    }
}
final case object End extends IntList
final case class Pair(head: Int, tail: IntList) extends IntList
```
</div>

Define a method to double the value of each element in an `IntList`, returning a new `IntList`. The following test cases should hold:

```scala
assert(example.double == Pair(2, Pair(4, Pair(6, End))))
assert(example.tail.double == Pair(4, Pair(6, End)))
assert(End.double == End)
```

<div class="solution">
```scala
sealed trait IntList {
  def double: IntList =
    this match {
      case End => End
      case Pair(hd, tl) => Pair(hd * 2, tl.double)
    }
}
final case object End extends IntList
final case class Pair(head: Int, tail: IntList) extends IntList
```
</div>

#### The Forest of Trees

A binary tree of integers can be defined as follows:

A `Tree` is a `Node` with a left and right `Tree` or a `Leaf` with an element of type `Int`.

Implement this algebraic data type.

<div class="solution">
```scala
sealed trait Tree
final case class Node(val l: Tree, val r: Tree) extends Tree
final case class Leaf(val elt: Int) extends Tree
```
</div>

Implement `sum` and `double` on `Tree` using polymorphism and pattern matching.

<div class="solution">
```scala
object TreeOps {
  def sum(tree: Tree): Int =
    tree match {
      case Leaf(elt) => elt
      case Node(l, r) => sum(l) + sum(r)
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
```
</div>

