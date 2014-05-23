---
layout: page
title: Collections
---

We hardly need to state how important collection classes are. The Collections API was one of the most significant additions to Java, and Scala's collections framework, completely revised and updated in 2.8, is an equally important addition to Scala.

In this section we're going to look at three key datastructures in Scala's collection library: **options, sequences, and maps**.

We will start with **sequences**. We begin with basic operations on sequences, and then briefly examine the distinction Scala makes between interface and implementation, and mutable and immutable sequences. We then explore in depth the methods Scala provides to transform sequences.

We will then move onto **options**, which are used frequently in the APIs for sequences and maps. Options provide a means for avoiding `nulls` in our code, and are a way to chain together computations that may or may not produce a valid result.

After covering the main collection types we turn to **for comprehensions**, a syntax that allows convenient specification of operations on collections. We'll also briefly look at a generalisation of collections, called **monads**, that work well with for comprehensions.

Next we will cover the other main collection classes: **maps and sets**. We will discover that they share a great deal in common with sequences, so most of our knowledge transfers directly.

We finish with discussion of **ranges**, which can represent large sequences of integers without storing every intermediate value in memory.
