---
layout: page
title: Core Scala
---

This book is aimed to programmer learning Scala for the first time. We assume you have some familiarity with an object-oriented programming language such as Java, but little or no experience with functional programming.

Our goal is to describe how to build programs using Scala, and we introduce Scala's features in the context of the problems they solve. We are not aiming for exhaustive coverage of Scala's features, and this text is not a reference manual.

Except for a few exercises we don't rely on any external libraries. You should be able to complete all the problems inside with only a text editor and Scala's REPL. If you wish to use an IDE such as the [Scala IDE for Eclipse](http://scala-ide.org/) or [IntelliJ IDEA](http://www.jetbrains.com/idea/) they will work as well.


## Table of Contents

- Introducing Scala
  - [History](intro/history.html)
  - [Guiding principles](intro/guiding-principles.html)
    - Separate that which is different
    - Unite that which is the same
    - Catch errors at compile time
  - [Course overview](intro/course-overview.html)
    - Assumptions: You have some experience with Java
    - Goals
- The REPL
  - Using the REPL
  - REPL tricks
  - Self help
- ScalaDoc
  - The secret trait/object switcher
- [SBT](sbt/index.html)
  - The shortest introduction to SBT
  - Starting the REPL
  - Troubleshooting SBT
- [Scala Basics](scala-basics/index.html)
  - [Introducing Expressions](scala-basics/expressions.html)
    - Simple Literals
      - Numbers, Strings, Booleans, Null
    - Compound expressions
      - Conditional expressions
      - Operators vs methods
      - Shortcuts for operators
        - Apply and update
  - [Introducing Scala's Type System](scala-basics/types.html)
    - Scala's type hierarchy
      - Everything is an object
      - Any, AnyVal, AnyRef, ScalaObject, and Nothing
      - Scala's primitives and their relation to Java primitives
    - Type Declarations
  - [Introducing Definitions](scala-basics/definitions.html)
    - val
    - var
    - def
      - Calling methods: brackets and braces
    - Object literals
    - Recursive functions
      - @tailrec
  - [Introducing Statements](scala-basics/statements.html)
    - Assignment
    - Packages and Imports
  - [More Complex Programs](scala-basics/more-expressions.html)
    - Tuples
    - Functions
      - Functions are objects: apply
    - Generic Types
      - Type bounds
- [Object Oriented Programming in Scala](oo/index.html)
  - [Defining classes](oo/defining.html)
    - Instance variables
    - Methods
      - Uniform access principle
    - Constructors and constructor arguments
    - Creating instances
    - Companion objects
  - [Inheritance](oo/inheritance.html)
    - The type hierarchy, in more depth
    - Inheritance
    - Overriding
    - Super constructors
    - Casts
  - [Traits](oo/traits.html)
    - Traits
    - Trait composition
    - Self types
  - [Type Parameters](oo/type-parameters.html)
{% comment %}
- Object-Functional Programming
  - Case classes redux
    - Option
  - Pattern matching
    - unapply
  - Higher-order functions
  - Structural recursion
{% endcomment %}
- [Collections](collections/index.html)
  - [Introducing Sequences](collections/seq.html)
    - Defining a sequence (Seq)
    - Interfaces vs implementations (Seq vs List)
    - Some basic operations on sequences
      - length, indexOf, apply, +:, :+, sorted, reverse, ++
    - Mutable and immutable collections
      - Updating an element
  - [Working with Sequences](collections/working-with-seq.html)
    - map
    - flatMap
    - fold
    - foreach
    - Algebra of transformations
    - filter
    - find
  - [Sequence Implementations](collections/seq-implementations.html)
    - Performance characteristics of sequences
      - IndexedSeq and LinearSeq
    - Immutable sequences
    - Mutable sequences
  - [Maps and Sets](collections/map-and-set.html)
  - [Introducing For Comprehensions](collections/for-comprehensions.html)
  - [Working with For Comprehensions](collections/working-with-for-comprehensions.html)
    - Ranges
    - val
    - Filtering
    - Monads
  - [Arrays and Strings](collections/arrays-and-strings.html)
  - [Iterators and Views](collections/iterators.html)
  - [Traversable and Iterable](collections/traversable.html)
    - TraversableOnce
  - [Interoperating with Java](collections/java-interop.html)
- [Growing the Language](dsl/index.html)
  - [Operators](dsl/operators.html)
  - [Custom Control Structures](dsl/control.html)
  - [Open Extension of Classes](dsl/implicits.html)
  - [Type Classes](dsl/type-classes.html)
  - [Pimp My Type Class](dsl/pimping.html)
  - [Syntax Extensions](dsl/macros.html)
