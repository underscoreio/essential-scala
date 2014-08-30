---
layout: page
title: Structural Recursion
---

In the previous section we saw that traits allow us to define data representing a logical or. We also saw that traits define an interface that extending classes must implement.

If we are using the logical or pattern, it indicates we want to define different data types that have something in common -- the interface defined by the trait -- but also something different. After all, if there wasn't a difference between the data types we wouldn't bother defining them.

Concretely, consider the `Visitor` type from the previous section. We have two different cases: `User` for logged in users, and `Anonymous`. Now imagine we want to send an email to visitors. We can send an email to users, but not to anonymous visitors. At the moment we don't know how to implement this.

How can we do different things for different data types that belong to a common trait? In this section we'll see two ways to do this: *polymorphism* and *pattern matching*.

## Polymorphism

## Pattern Matching
