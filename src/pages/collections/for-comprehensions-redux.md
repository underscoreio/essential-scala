## For Comprehensions Redux

Earlier we looked at the fundamentals of for comprehensions. In this section we're going to looking at some handy additional features they offer, and at idiomatic solutions to common problems.

### Filtering

It's quite common to only process selected elements. We can do this with comprehensions by adding an `if` clause after the generator expression. So to process only the positive elements of sequence we could write

~~~ scala
for(x <- Seq(-2, -1, 0, 1, 2) if x > 0) yield x
// res: Seq[Int] = List(1, 2)
~~~

The code is converted to a `withFilter` call, or if that doesn't exist to `filter`.

Note that, unlike the normal `if` expression, an `if` clause in a generator does not have parentheses around the condition. So we write `if x > 0` not `if(x > 0)` in a for comprehension.

### Parallel Iteration

Another common problem is to iterate over two or more collections in parallel. For example, say we have the sequences `Seq(1, 2, 3)` and `Seq(4, 5, 6)` and we want to add together elements with the same index yielding `Seq(5, 7 , 9)`. If we write

~~~ scala
for {
  x <- Seq(1, 2, 3)
  y <- Seq(4, 5, 6)
} yield x + y
// res: Seq[Int] = List(5, 6, 7, 6, 7, 8, 7, 8, 9)
~~~

we see that iterations are nested. We traverse the first element from the first sequence and then all the elements of the second sequence, then the second element from the first sequence and so on.

The solution is to `zip` together the two sequences, giving a sequence containing pairs of corresponding elements

~~~ scala
Seq(1, 2, 3).zip(Seq(4, 5, 6))
// res: Seq[(Int, Int)] = List((1,4), (2,5), (3,6))
~~~

With this we can easily compute the result we wanted

~~~ scala
for(x <- Seq(1, 2, 3).zip(Seq(4, 5, 6))) yield { val (a, b) = x; a + b }
// res: Seq[Int] = List(5, 7, 9)
~~~

Sometimes you want to iterate over the values in a sequence and their indices. For this case the `zipWithIndex` method is provided.

~~~ scala
for(x <- Seq(1, 2, 3).zipWithIndex) yield x
// res: Seq[(Int, Int)] = List((1,0), (2,1), (3,2))
~~~

Finally note that `zip` and `zipWithIndex` are available on all collection classes, including `Map` and `Set`.

### Pattern Matching

The pattern on the left hand side of a generator is not named accidentally. We can include any pattern there and only process results matching the pattern. This provides another way of filtering results. So instead of:

~~~ scala
for(x <- Seq(1, 2, 3).zip(Seq(4, 5, 6))) yield { val (a, b) = x; a + b }
// res: Seq[Int] = List(5, 7, 9)
~~~

we can write:

~~~ scala
for((a, b) <- Seq(1, 2, 3).zip(Seq(4, 5, 6))) yield a + b
// res: Seq[Int] = List(5, 7, 9)
~~~

### Intermediate Results

It is often useful to create an intermediate result within a sequence of generators. We can do this by inserting an assignment expression like so:

~~~ scala
for {
  x     <- Seq(1, 2, 3)
  square = x * x
  y     <- Seq(4, 5, 6)
} yield square * y
// res: Seq[Int] = List(4, 5, 6, 16, 20, 24, 36, 45, 54)
~~~
