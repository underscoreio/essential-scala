---
layout: page
title: Working With Data
---

In the previous section we saw how to define algebraic data types using a combination of the sum- (or) and product-type (and) patterns. In this section we'll a pattern for using algebraic data types, known as **structural recursion**. We'll actually see two variants of this pattern: one using **polymorphism** and one using **pattern matching**.

Structural recursion is the precise opposite of the process of building an algebraic data type. If `A` has a `B` and `C` (the product-type pattern), to construct an `A` we must have a `B` and a `C`. The sum- and product-type patterns tell us how to combine data to make bigger data. Structural recursion says that if we have an `A` as defined before, we must break it into its constituent `B` and `C` that we then combine in some way to get closer to our desired answer. Structural recursion is essentially the process of breaking down data into smaller pieces.

Just as we have two paterns for building algebraic data types, we will have two patterns for decomposing them using structural recursion. We will actually have two variants of each pattern, one using polymorphism, which is the typical object-oriented style, and one using pattern matching, which is typical functional style. We'll end this section with some rules for choosing which pattern to use.

## Structural Recursion using Polymorphism

## Structural Recursion using Pattern Matching

## Choosing Which Pattern to Use

## Exercises
