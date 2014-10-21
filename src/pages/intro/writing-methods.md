---
layout: page
title: Writing Methods
---

In the previous section we saw the syntax of methods. One of our main goals in this course is to go beyond syntax and give you a systematic method for constructing Scala programs. This is our first section dealing with such matters. In this section we're going to look at a systematic method for constructing methods. As you gain experience with Scala you can drop some of the steps of this method, but we **strongly** suggest you follow this method during the course.

To make the advice concrete we'll use this exercise from the previous section as an example:

Define an object called `calc` with a method `square` that accepts a `Double` as an argument and... you guessed it... squares its input. Add a method called `cube` that cubes its input *calling `square`* as part of its result calculation.

## Identify the Input and Output

Your first step is to identify the types of the input parameters, if any, and the result of the method.

In many cases the exercises will tell you the types and you can just read them straight from the description. In the example above the input type is given as `Double`. The result type we can infer is also `Double`.

## Prepare Test Cases

Types alone don't tell all the story. There are many `Double` to `Double` functions, but few that implement squaring. Thus we should prepare some test cases that illustrate the expected behaviour of the method.

We're not going to use a testing library in this course, as we're trying to avoid external dependencies. We can implement a poor-man's testing library using the `assert` function that Scala provides. For our `square` example we might have test cases like

~~~ scala
assert(square(2.0) == 4.0)
assert(square(3.0) == 9.0)
assert(square(-2.0) == 4.0)
~~~

## Write the Declaration

With types and test cases ready we can now write the method declaration. We haven't developed the body yet so use `???`, another nifty Scala feature, in its place.

~~~ scala
def square(in: Double): Double =
  ???
~~~

This step should be mechanical.

## Run the Code

Run the code and check it compiles (and thus we haven't made any typos) but also that our tests fail. You may need to place the tests after the method declaration.

## Write the Body

We're now ready to write the body of our method. We will develop a number of techniques for this throughout the course. For now, we're going to look at two techniques.

#### Consider the Result Type

The first technique is to look at the result type, in this case `Double`. How can we create `Double` values? We could write a literal, but that obviously won't be correct in this case. The other way we know to create a `Double` is to call a method on some object, which brings us to the next technique.

#### Consider the Input Type

Our next technique is to look at the type of input parameters to the method. In this case we have a `Double`. We have established we need to create `Double`, so what methods can we call to create `Double` from out input. There are many such methods, and here we have to use our domain knowledge to select `*` as the correct method to call.

Thus we can write our complete method as

~~~ scala
def square(in: Double): Double =
  in * in
~~~

## Run the Code, Again

Finally we should run the code again and check that the tests all pass in this case.

This is very simple example but practicing the process now will serve you well for the more complicated examples we will encounter later.

<div class="callout callout-info">
#### Process for Writing Methods

We have a six-step process for writing methods in a systematic way.

1. Identify the type of the inputs and output of the method.
2. Write some test cases for the expected output of the method given example input. We can use the `assert` function to write down these cases.
3. Write the method declaration using `???` for the body like so:

   ~~~ scala
   def name(parameter: type, ...): resultType =
     ???
   ~~~
4. Run the code to check the test cases do in fact fail.
5. Write the body of the method. We currently have two techniques to apply here:
   - consider the result type and how we can create an instance of it; and
   - consider the input type and methods we can call to transform it to the result type.
6. Run the code again and check the test cases pass.
</div>
