---
layout: page
title: Expressions, Types, and Values
---

In this chapter we look at the fundamentals of Scala: **expressions, types, and values**. Understanding these concepts is necessary to build a mental model of how Scala programs work.

Let's get started. In the Scala console or worksheet enter `"Hello world!"` and press return (in the console) or save the worksheet. You should see an interaction similar to this:

~~~ scala
scala> "Hello world!"
res0: String = Hello world!
~~~

There is a lot to say about this program. The program we wrote is called an *expression*. Scala runs, or *evaluates*, our program. When we evalute a program in the Scala console or worksheet we are told two pieces of information: the *type* of the program, and the *value* it evaluates to. In this case the type is `String` and the value is `"Hello world!"`.

Let's look at a slightly more complex program

~~~ scala
scala> "Hello world!".toUpperCase
res5: String = HELLO WORLD!
~~~

Once again, the type of this program is `String`, but in this case it evaluates to `"HELLO WORLD!"`

## Compile-time and Run-time

There are two distinct stages that a Scala program goes through: first it is *compiled*, and if it compiles successfully it can then be *run* or evaluated. We refer to the first stage as *compile-time* and the latter as *run-time*.

When using the Scala console our programs are evaluated as soon as they compile, which gives the appearance that there is only one stage. It is important to understand that compile- and run-time really are distinct, as it is this distinction that allows us to properly understand the difference between types and values.

Compilation is a process of checking that a program makes sense. There are two ways in which a program must "make sense":

1. It must be *syntactically correct*, meaning the parts of the program must be arranged according to the grammar of the language. An example English sentence that is not syntactically correct is "on cat mat sat the". An example syntactically incorrect Scala program is

   ~~~ scala
   scala> toUpperCase."Hello world!"
   <console>:1: error: identifier expected but string literal found.
           toUpperCase."Hello world!"
                       ^
   ~~~

2. It must *type check*, meaning it must obey certain constraints on what a sensible program is. An example English sentence that is syntactically correct but fails to make sense is "the mat sat on the cat". A simple program that would fail to type check is trying to convert a number to uppercase.

   ~~~ scala
   scala> 2.toUpperCase
   <console>:8: error: value toUpperCase is not a member of Int
                 2.toUpperCase
                   ^
   ~~~

   The concept of upper and lowercase doesn't make sense for numbers, and the type system will catch this error.

If a program passes the checks at compile-time, it may then be run. This is the process of the computer performing the instructions in the program. All Scala programs evaluate to a value.

Even though a program successfully compiles it can still fail at run-time. In Scala dividing an integer by zero causes a run-time error.

~~~ scala
scala> 2 / 0
java.lang.ArithmeticException: / by zero
~~~

The type of integers, `Int`, allows division so the program type checks. At run-time the program fails because there is no `Int` that can represent the result of the division.


## Expressions, Types, and Values

So what exactly are expressions, types, and values?

Expressions are part of a program's text -- what we type into a file, or the console or worksheet. They are the main component of a Scala program. We will see other components, namely *definitions* and *statements*, in due course. Expressions exist at compile-time.

The defining characteristic of an expression is that it evaluates to a value. A value is information stored in the computer's memory which exists at run-time. For example, the expression `2` evaluates to a particular sequence of bits in a particular location in the computer's memory. We compute with values. They are entities that our programs can pass around and manipulate. For example, to compute the minimum of two numbers we might write a program like

~~~ scala
scala> 2.min(3)
res13: Int = 2
~~~

Here we have two values, `2` and `3`, and we combine them into a larger program that evaluates to `2`.

In Scala all values are **objects**, which has a particular meaning we will see shortly.

Now let's turn to types. Types are restrictions on our programs that limit how we can manipulate objects. We have already seen two types, `String` and `Int`, and seen that we can perform different operations depending on the type.

At this point the most important point about types is that **expressions have types but values do not**. We cannot inspect an arbitrary piece of the computer's memory and divine how to interpret it without knowing the program that created it. For example, in Scala the `Int` and `Float` types are both represented by 32-bits of memory. There are no tags or other indications that a given 32-bits should be interpreted as an `Int` or a `Float`.

We can show that types exist at compile-time by asking the Scala console to tell us the type of an expression that causes a run-time error.

~~~ scala
scala> :type 2 / 0
Int

scala> 2 / 0
java.lang.ArithmeticException: / by zero
~~~

We see that the expression `2 / 0` has type `Int` even though this expression fails when we evaluate it.

Types, which exist at compile-time, restrict us to writing programs that give a consistent interpretation to values. We cannot claim that a particular 32-bits is at one point an `Int` and another a `Float`. When a program type checks, Scala guarantees that all values are used consistently and thus it does not need to record type information in a value's representation. This process of removing type information is called *type erasure*.

Types necessarily do not contain all possible information about the values that conform to the type. We have already seen that the type system will not prevent us from dividing an `Int` by zero, which leads to a run-time error. We cannot get around this restriction but we will see that we can express many useful constraints in the type system, which improves the reliability of our programs. Using the type system well is one of the main themes of this book.


## Take Home Points

We must build a mental model of Scala programs if we are to use Scala. Three fundamental components of this model are *expressions*, *types*, and *values*.

Expressions are the parts of a program that evaluate to a value. They are the major part of a Scala program.

Expressions have types, which express some restrictions on programs. During *compile-time* the types of our programs are checked. If they are inconsistent then compilation fails and we cannot evaluate, or run, our program.

Values exist in the computer's memory, and are what a running program manipulates. In Scala, all values are *objects*, the meaning of which we will discuss soon.
