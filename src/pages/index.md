# Foreword #{-}

This book is aimed at programmers learning Scala for the first time. We assume you have some familiarity with an object-oriented programming language such as Java, but little or no experience with functional programming.

Our goal is to describe how to use Scala in-the-small. To this end our focus is on the core patterns used in idiomatic Scala code, and we introduce Scala's features in the context of the patterns they enable. We are not aiming for exhaustive coverage of Scala's features, and this text is not a reference manual.

Except for a few exercises we don't rely on any external libraries. You should be able to complete all the problems inside with only a text editor and Scala's REPL, or an IDE such as the [Scala IDE for Eclipse](http://scala-ide.org/) or [IntelliJ IDEA](http://www.jetbrains.com/idea/).

Essential Scala was created by [Noel Welsh](http://noelwelsh.com) and [Dave Gurnell](http://davegurnell.com/) of [Underscore](http://underscore.io). It was built using [Underscore's eBook Template](https://github.com/underscoreio/underscore-ebook-template), plain text, and a deep and profound love of functional programming.

## Conventions Used in This Book #{-}

This book contains a lot of technical information and program code. We use the following typographical conventions to reduce ambiguity and highlight important concepts:

### Typographical Conventions #{-}

New terms and phrases are introduced in *italics*. After their initial introduction they are written in normal roman font.

Terms from program code, filenames, and file contents, are written in `monospace font`. Note that we do not distinguish between singular and plural forms. For example, might write `String` or `Strings` to refer to the `java.util.String` class or objects of that type.

References to external resources are written as [hyperlinks][link-underscore]. References to API documentation are written using a combination of hyperlinks and monospace font, for example: [`Option`][scala.Option].

### Source Code #{-}

Source code blocks are written as follows. Syntax is highlighted appropriately where applicable:

```scala
object MyApp extends App {
  println("Hello world!") // Print a fine message to the user!
}
```

Some lines of program code are too wide to fit on the page. In these cases we use a *continuation character* (curly arrow) to indicate that longer code should all be written on one line. For example, the following code:

```scala
println("This code should all be written ↩
  on one line.")
```

should actually be written as follows:

```scala
println("This code should all be written on one line.")
```

### Callout Boxes #{-}

We use three types of *callout box* to highlight particular content:

<div class="callout callout-info">
Tip callouts indicate handy summaries, recipes, or best practices.
</div>

<div class="callout callout-warning">
Advanced callouts provide additional information on corner cases or underlying mechanisms. Feel free to skip these on your first read-through---come back to them later for extra information.
</div>

<div class="callout callout-danger">
Warning callouts indicate common pitfalls and gotchas. Make sure you read these to avoid problems, and come back to them if you're having trouble getting your code to run.
</div>
