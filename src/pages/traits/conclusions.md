## Conclusions

In this chapter we have made an extremely important change in our focus, away from language features and towards the programming patterns they support. This continues for the rest of the book.

We have explored two extremely important patterns: *algebraic data types* and *structural recursion*. These patterns allow us to go from a mental model of data, to the representation and processing of that data in Scala in an almost entirely mechanical way. Not only in the structure of our code formulaic, and thus easy to comprehend, but the compiler can catch common errors for us which makes development and maintenance easier. These two tools are among the most commonly used in idiomatic functional code, and it is hard to over-emphasize their importance.

In the exercises we developed a few common data structures, but we were limited to storing a fixed type of data, and our code contained a lot of repetition. In the next section we will look at how we can abstract over types and methods, and introduce some important concepts of sequencing operations.
