---
layout: page
title: Functional Programming
---

In this section we'll look in more depth at functional programming. We have already seen uses for first class functions. We're going to see more in this section, as well as other topics such as structural recursion, and combinator libraries.

The theme that unites functional programming is avoiding side-effects. Informally, this can be seen as maintaining substitution. When we see a simple expression like `3 + 2` we know we can substitute in `5` wherever is occurs. We cannot do the same with side-effects. For example, we cannot substitute `()` for `println("Hello")` as the latter has an effect not captured in its return type. The substitution model makes it very easy to reason about programs, which is one benefit. It also has the effect of forcing us to encode many side-effects, for example errors, in the type system. This allows us to leverage the type system to give a greater degree of confidence in the correctness of our programs.
