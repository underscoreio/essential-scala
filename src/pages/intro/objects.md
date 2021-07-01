## Interacting with Objects

In the previous section we saw the fundamental components of Scala programs: expressions, types, and values. We learned that _all values are objects_. In this section we will learn more about objects and how we can interact with them.

### Objects

An object is a grouping of data and operations on that data. For example, `2` is an object. The data is the integer 2, and the operations on that data are familiar operations like `+`, `-`, and so on.

We have some special terminology for the data and operations of an object. The operations are known as _methods_. The data is stored in _fields_.

### Method Calls

We interact with objects by _calling_ methods[^patterns]. We have already seen some examples of calling methods. For example, we have seen we can get the uppercase version of a `String` by calling its `toUpperCase` method.

```scala mdoc
"hello".toUpperCase
```

Some methods accept _parameters_ or _arguments_, which control how the method works. The `take` method, for example, takes characters from a `String`. We must pass a parameter to `take` to specify how many characters we want.

```scala mdoc
"abcdef".take(3)
"abcdef".take(2)
```

<div class="callout callout-info">
#### Method Call Syntax {-}

The syntax for a method call is

```scala
anExpression.methodName(param1, ...)
```

or

```scala
anExpression.methodName
```

where

- `anExpression` is any expression (which evaluates to an object)
- `methodName` is the name of the method
- the optional `param1, ...` is one or more expressions evaluating to the parameters to the method.
</div>

A method call is an expression, and thus evaluates to an object. This means we can chain method calls together to make more complex programs:

```scala mdoc
"hello".toUpperCase.toLowerCase
```

In what order are the various expressions in a method call evaluated? Method parameters are evaluated left-to-right, before the method is called. So in the expression

```scala mdoc
"Hello world!".take(2 + 3)
```

the expression `"Hello world!"` is evaluated first, then `2 + 3` (which requires evaluating `2` and then `3` first), then finally `"Hello world!".take(5)`.

### Operators

Because every value in Scala is an object we can also call methods on primitive types such as `Int` and `Boolean`. This is in contrast to Java where `int` and `boolean` are not objects:

```scala mdoc
123.toShort // this is how we define a `Short` in Scala
123.toByte // this is how we define a `Byte`
```

But if an `Int` is an object, what are the basic mathematical operators such as `+` and `-`? Are they also methods? Yes---Scala methods can have symbolic names as well as alphanumeric ones!

```scala mdoc
43 - 3 + 2
43.-(3).+(2)
```

(Note that in Scala 2.10 and earlier you would have to write `(43).-(3).+(2)` to prevent `43.` being interpreted as a `Double`.)

<div class="callout callout-info">
#### Infix Operator Notation {-}

Any Scala expression written `a.b(c)` can also be written `a b c`.

Note that `a b c d e` is equivalent to `a.b(c).d(e)`, not `a.b(c, d, e)`.

</div>

We can use _infix operator notation_ with any method that takes one parameter, regardless of whether it has a symbolic or alphanumeric name:

```scala mdoc:silent
"the quick brown fox" split " "
// res: Array[String] = Array(the, quick, brown, fox)
```

Infix notation is one of several syntactic shorthands that allow us to write simple operator expressions instead of verbose method calls. There are also notations for _prefix_, _postfix_, _right-associative_, and _assignment-style operators_, but there are much less common than infix notation.

A question poses itself---what precedence rules should we associate with infix operators? Scala uses a set of [precedence rules][link-precedence-rules] derived from the identifiers we use as method names that follow our intuitive understanding from mathematics and logic:

```scala mdoc
2 * 3 + 4 * 5
(2 * 3) + (4 * 5)
2 * (3 + 4) * 5
```

### Take home points

All Scala values are objects. We _interact with objects by calling methods_ on them. If you come from a Java background note we can call methods on `Int` or any other primitive value.

The syntax for a method call is

```scala
anExpression.methodName(parameter, ...)
```

or

```scala
anExpression methodName parameter
```

_Scala has very few operators - almost everything is a method call._ We use syntactic conventions like infix operator notation to keep our code simple and readable, but we can always fall back to standard method notation where it makes sense.

As we will see, Scala's focus on programming with expressions allows us to write much shorter code than we can in Java. It also allows us to reason about code in a very intuitive way using values and types.

### Exercises

#### Operator Style

Rewrite in operator-style

```scala mdoc
"foo".take(1)
```

<div class="solution">
```scala mdoc
"foo" take 1
```
</div>

Rewrite in method call style

```scala mdoc
1 + 2 + 3
```

<div class="solution">
```scala mdoc
1.+(2).+(3)
```
</div>

#### Substitution

What is the difference between the following expressions? What are the similarities?

```scala mdoc:silent
1 + 2 + 3

6
```

<div class="solution">
The expressions have the same result type and return value. However, they arrive at their results in different ways. The first computes its result through a series of additions, while the later is simply a literal.

As neither expression has any side-effects, they are interchangeable from a user's point of view. Anywhere you can write `1 + 2 + 3` you can also write `6`, and vice versa, without changing the meaning of any program. This is known as _substitution_ and you may remember the principle from simplifying algebraic formulae at school.

As programmers we must develop a mental model of how our code operates. The _substitution model of evaluation_ is a particuarly simple model that says anywhere we see an expression we may substitute its result. In the absence of side-effects, the substitution model always works[^side-effects]. If we know the types and values of each component of an expression, we know the type and value of the expression as a whole. In functional programming we aim to avoid side-effects for this reason: it makes our programs easier to understand.

</div>

[^side-effects]: What exactly is a side-effect? One workable definition is anything that causes substitution to yield an incorrect result. Does substitution _always_ work, in the absence of side-effects? To truly have a correct model of Scala we must define the order in which we apply substitutions. There are a number of possible orders. (For example, we perform substitution left-to-right, or right-to-left? Do we substitute as soon as possible or delay until we need a value?) Most of the time order of substitution doesn't matter, but there are cases where it does. Scala always applies substitution from left-to-right and at the earliest possible time.
[^patterns]: There is another way of interacting with objects, called pattern matching. We will introduce pattern matching later.
