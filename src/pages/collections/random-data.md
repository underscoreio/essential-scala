## Generating Random Data

In this section we have an extended case study generating random data. The ideas here have many applications. For example, in generating data for testing, as used in *property based testing*, in *probabilistic programming*, a new area of machine learning, and, if you're going through the extended case study, in *generative art*.

List[String] flatMap to generate strings conditionally. Example: generating text.

Now add probability. List[(String, Double)]. Need to hide this in a type. being a proper flatMap is A => F[B]

What does it mean to combine probabilities in this way? Multiply them. 

Here are some handy utilities for normalisation and deduplicating.

