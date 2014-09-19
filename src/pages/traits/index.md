---
layout: page
title: "Modelling Data with Traits"
---

We looked in depth at classes in the previous section. Classes provide us with a way to abstract over objects that have similar properties. Allowing us to write code that works with any object in a class.

In this section we explore **abstraction over classes**. Allowing us to write code that works with objects of different classes. We achieve this with a mechanism called **traits**.

This section also marks a change in our focus. In previous sections we have  addressed the technical aspects of constructing Scala code. In this section we will initially focus on the technical aspects of traits, but our focus will then change to using Scala as a **medium to express our thoughts**.

We will see how we can mechanically transform a description of data, called an **algebraic datatype**, in code. Using **structural recursion** we can mechanically write code that transforms an algebraic datatype.
