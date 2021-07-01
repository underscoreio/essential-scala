## Ranges

So far we've seen lots of ways to iterate over sequences but not much in the way of iterating over numbers. In Java and other languages it is common to write code like

```java
for(i = 0; i < array.length; i++) {
  doSomething(array[i])
}
```

We've seen that for comprehensions provide a succinct way of implementing these programs. But what about classics like this?

```java
for(i = 99; i > 0; i--) {
  System.out.println(i + "bottles of beer on the wall!")
  // Full text omitted for the sake of brevity
}
```

Scala provides the `Range` class for these occasions. A `Range` represents a sequence of integers from some starting value to less than the end value with a non-zero step. We can construct a `Range` using the `until` method on `Int`.

```scala mdoc
1 until 10
```

By default the step size is 1, so trying to go from high to low gives us an empty `Range`.

```scala mdoc
10 until 1
```

We can rectify this by specifying a different step, using the `by` method on `Range`.

```scala mdoc
10 until 1 by -1
```

Now we can write the Scala equivalent of our Java program.

```scala mdoc:silent
for(i <- 99 until 0 by -1) println(i + " bottles of beer on the wall!")
// 99 bottles of beer on the wall!
// 98 bottles of beer on the wall!
// 97 bottles of beer on the wall!
```

This gives us a hint of the power of ranges. Since they are sequences we can combine them with other sequences in interesting ways. For example, to create a range with a gap in the middle we can concatenate two ranges:

```scala mdoc
(1 until 10) ++ (20 until 30)
```

Note that the result is a `Vector` not a `Range` but this doesn't matter. As they are both sequences we can use both them in a for comprehension without any code change!
