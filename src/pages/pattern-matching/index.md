# Pattern Matching

We have seen the duality between algebraic data types and pattern matching. Armed with this information, we are in a good position to return to pattern matching and see some of its more powerful features.

As we discussed earlier, patterns are written in their own DSL that only superficially resembles regular Scala code. _Patterns serve as tests that match a specific set of Scala values_. The `match` expression compares a value to each pattern in turn, finds the first pattern that matches, and executes the corresponding block of Scala code.

_Some patterns bind values to variables_ that can be used on the right hand side of the corresponding `=>` symbol, and _some patterns contain other patterns_, allowing us to build complex tests that simultaneously examine many parts of a value. Finally, _we can create our own custom patterns_, implemented in Scala code, to match any cross-section of values we see fit.

We have already seen case class patterns and certain types of sequence patterns. Each of the remaining types of pattern is described below together with an example of its use.

## Standard patterns

### Literal patterns

Literal patterns match a particular value. Any Scala literals work except function literals: primitive values, `Strings`, `nulls`, and `()`:

```scala mdoc:invisible
case class Person(name: String, surname: String)
```

```scala mdoc
(1 + 1) match {
  case 1 => "It's one!"
  case 2 => "It's two!"
  case 3 => "It's three!"
}

Person("Dave", "Gurnell") match {
  case Person("Noel", "Welsh") => "It's Noel!"
  case Person("Dave", "Gurnell") => "It's Dave!"
}

println("Hi!") match {
  case () => "It's unit!"
}
```

### Constant patterns

Identifiers starting with an uppercase letter are _constants_ that match a single predefined constant value:

```scala mdoc
val X = "Foo"

val Y = "Bar"

val Z = "Baz"

"Bar" match {
  case X => "It's foo!"
  case Y => "It's bar!"
  case Z => "It's baz!"
}
```

### Alternative patterns

Vertical bars can be used to specify alternatives:

```scala mdoc
"Bar" match {
  case X | Y => "It's foo or bar!"
  case Z     => "It's baz!"
}
```

### Variable capture

Identifiers starting with lowercase letters bind values to variables. The variables can be used in the code to the right of the `=>`:

```scala mdoc
Person("Dave", "Gurnell") match {
  case Person(f, n) => f + " " + n
}
```

The `@` operator, written `x @ y`, allows us to capture a value in a variable `x` while also matching it against a pattern `y`. `x` must be a variable pattern and `y` can be any type of pattern. For example:

```scala mdoc
Person("Dave", "Gurnell") match {
  case p @ Person(_, s) => s"The person $p has the surname $s"
}
```

### Wildcard patterns

The `_` symbol is a pattern that matches any value and simply ignores it. This is useful in two situations: when nested inside other patterns, and when used on its own to provide an "else" clause at the end of a match expression:

```scala mdoc
Person("Dave", "Gurnell") match {
  case Person("Noel", _) => "It's Noel!"
  case Person("Dave", _) => "It's Dave!"
}

Person("Dave", "Gurnell") match {
  case Person(name, _) => s"It's $name!"
}

Person("John", "Doe") match {
  case Person("Noel", _) => "It's Noel!"
  case Person("Dave", _) => "It's Dave!"
  case _ => "It's someone else!"
}
```

### Type patterns

A type pattern takes the form `x: Y` where `Y` is a type and `x` is a wildcard pattern or a variable pattern. The pattern matches any value of type `Y` and binds it to `x`:

```scala mdoc:invisible
trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(side1: Double, side2: Double) extends Shape
case class Square(side: Double) extends Shape
```

```scala mdoc
val shape: Shape = Rectangle(1, 2)

shape match {
  case c : Circle    => s"It's a circle: $c!"
  case r : Rectangle => s"It's a rectangle: $r!"
  case s : Square    => s"It's a square: $s!"
}
```

### Tuple patterns

Tuples of any arity can be matched with parenthesised expressions as follows:

```scala mdoc
(1, 2) match {
  case (a, b) => a + b
}
```

### Guard expressions

This isn't so much a pattern as a feature of the overall `match` syntax. We can add an extra condition to any `case` clause by suffixing the pattern with the keyword `if` and a regular Scala expression. For example:

```scala mdoc
123 match {
  case a if a % 2 == 0 => "even"
  case _ => "odd"
}
```

To reiterate, the code between the `if` and `=>` keywords is a regular Scala expression, not a pattern.
