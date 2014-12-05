# Modelling Data with Traits

We looked in depth at classes in the previous chapter. Classes provide us with a way to abstract over objects that have similar properties, allowing us to write code that works with any object in a class.

In this chapter we explore **abstraction over classes**, allowing us to write code that works with objects of different classes. We achieve this with a mechanism called **traits**.

This chapter also marks a change in our focus. In previous chapters we have addressed the technical aspects of constructing Scala code. In this chapter we will initially focus on the technical aspects of traits. Our focus will then change to using Scala as a **medium to express our thoughts**.

We will see how we can mechanically transform a description of data, called an **algebraic datatype**, into code. Using **structural recursion** we can mechanically write code that transforms an algebraic datatype.
