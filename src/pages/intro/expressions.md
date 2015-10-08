## Compound Expressions

We have almost finished our basic introduction to Scala. In this section we are going to look at two special kinds of expressions, *conditionals* and *blocks*, we will need in more complicated programs.

### Conditionals

A conditional allows us to choose an expression to evaluate based on some condition. For example, we can choose a string based on which of two numbers is the smallest.

~~~
if(1 < 2) "Yes" else "No"
// res: String = Yes
~~~

<div class="callout callout-info">
#### Conditionals are Expressions {-}

Scala's `if` statement has the same syntax as Java's. One important difference is that *Scala's conditional is an expression*---it has a type and returns a value.
</div>

The expression that is not selected does not get evaluated. This is apparent if we use an expression with a side-effect.

~~~
if(1 < 2) println("Yes") else println("No")
// Yes
~~~

We can tell the expression `println("No")` is not evaluated because `No` is not output to the console.

<div class="callout callout-info">
#### Conditional Expression Syntax {-}

The syntax for a conditional expression is

~~~ scala
if(condition)
  trueExpression
else
  falseExpression
~~~

where

- `condition` is an expression with `Boolean` type;
- `trueExpression` is the expression evaluated if `condition` evaluates to `true`; and
- `falseExpression` is the expression evaluated if `condition` evaluates to `false`.
</div>


### Blocks

Blocks are expressions that allow us to sequence computations together. They are written as a pair of braces containing sub-expressions separated by semicolons or newlines.

~~~ scala
{ 1; 2; 3 }
// warning: a pure expression does nothing in statement position; you may be omitting       ↩
//          necessary parentheses
//  { 1; 2; 3 }
//    ^
// warning: a pure expression does nothing in statement position; you may be omitting       ↩
//          necessary parentheses
//  { 1; 2; 3 }
//       ^
// res: Int = 3
~~~

As you can see, executing this code causes the console to raise a number of warnings and return the `Int` value `3`.

A block is a sequence of expressions or declarations surrounded by braces. A block is also an expression: it executes each of its sub-expressions in order and returns the value of the last expression.

Why execute `1` and `2` if we're going to throw their values away? This is a good question, and is the reason the Scala compiler raised those warnings above.

One reason to use a block is to use code that produces side-effects before calculating a final value:

~~~ scala
{
  println("This is a side-effect")
  println("This is a side-effect as well")
  3
}
// This is a side-effect
// This is a side-effect as well
// res: Int = 3
~~~

We can also use a block when we want to name intermediate results, such as

~~~ scala
def name: String = {
  val title = "Professor"
  val name = "Funkenstein"
  title + " " + name
}

name
// res: String = Professor Funkenstein
~~~

<div class="callout callout-info">

#### Block Expression Syntax {-}

The syntax of a block expression is

~~~ scala
{
   declarationOrExpression ...
   expression
}
~~~

where

- the optional `declarationOrExpression`s are declarations or expression; and
- `expression` is an expression determining the type and value of the block expression.
</div>

### Take home points

Conditional expressions allow us to choose an expression to evaluate based on a `Boolean` condition. The syntax is

~~~ scala
if(condition)
  trueExpression
else
  falseExpression
~~~

A conditional, being an expression, has a type and evaluates to an object.


A block allows us to sequence expressions and declarations. It is commonly used when we want to sequence expressions with side-effects, or name intermediate results in a computation. The syntax is

~~~ scala
{
   declarationOrExpression ...
   expression
}
~~~

The type and value of a block is that of the last expression in the block.


### Exercises

#### A classic rivalry

What is the type and value of the following conditional?

~~~ scala
if(1 > 2) "alien" else "predator"
~~~

<div class="solution">
  It's a `String` with value `"predator"`. Predators are clearly best.

  The type is determined by the upper bound of the types in the *then* and *else* expressions. In this case both expressions are `Strings` so the result is also a `String`.

  The value is determined at runtime. `2` is greater than `1` so the conditional evaluates to the value of the *else* expression.
</div>

#### A less well known rivalry

What about this conditional?

~~~ scala
if(1 > 2) "alien" else 2001
~~~

<div class="solution">
It's a value of type `Any` with value `2001`.

This is similar to the previous exercise---the difference is the type of the result. We saw earlier that the type is the *upper bound* of the positive and negative arms of the expression. `"alien"` and `2001` are completely different types - their closest common ancestor is `Any`, which is the grand supertype of all Scala types.

This is an important observation: types are determined at compile time, before the program is run. The compiler doesn't know which of `1` and `2` is greater before running the program, so it can only make a best guess at the type of the result of the conditional. `Any` is as close as it can get in this program, whereas in the previous exercise it can get all the way down to `String`.

We'll learn more about `Any` in the following sections. Java programmers shouldn't confuse it with `Object` because it subsumes value types like `Int` and `Boolean` as well.
</div>

#### An if without an else

What about this conditional?

~~~ scala
if(false) "hello"
~~~

<div class="solution">
The result type and value are `Any` and `()` respectively.

All code being equal, conditionals without `else` expressions only evaluate to a value half of the time. Scala works around this by returning the `Unit` value if the `else` branch should be evaluated. We would usually only use these expressions for their side-effects.
</div>
