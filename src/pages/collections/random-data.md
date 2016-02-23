## Generating Random Data

In this section we have an extended case study generating random data. The ideas here have many applications. For example, in generating data for testing, as used in *property based testing*, in *probabilistic programming*, a new area of machine learning, and, if you're going through the extended case study, in *generative art*.

### Random Words

We'll start by generating text. Imagine we wanted to generate (somewhat) realistic text, perhaps to use as a placeholder to fill in parts of a website design. If we took a large amount of real text we could analyse to work out for each word what the most common words following it are. Such a model is known as a *Markov chain*. 

To keep this example to a reasonable size we're going to deal with a really simplified version of the problem, where all sentences have the form *subject*-*verb*-*object*. For example, "Noel wrote code".

Write a program to generate all possible sentences given the following model:

- subjects are `List("Noel", "The cat", "The dog")`;
- verbs are `List("wrote", "chased", "slept on")`; and
- objects are `List("the book", "the ball", "the bed")`.

<div class="solution">
The following code will compute all possible sentences. The equivalent with explicit `flatMap` and `map` would also work.

Note that `flatMap` has more power than we need for this example. We could use the `subject` to alter how we choose the `verb`, for example. We'll use this ability in the next exercise.

```scala
val subjects = List("Noel", "The cat", "The dog")
val verbs = List("wrote", "chased", "slept on")
val objects = List("the book", "the ball", "the bed")

def allSentences: List[(String, String, String)] =
  for {
    subject <- subjects
    verb <- verbs
    obj <- objects
  } yield (subject, verb, obj)
```
</div>

This model creates some clearly nonsensical sentences. We can do better by making the choice of verb dependend on the subject, and the object depend on the verb.

Let's use the following model:

- The subjects are as before.
- For the verbs:
  - If the subject is "Noel" the possible verbs are "wrote", "chased", and "slept on".
  - If the subject is "The cat" the possible verbs are "meowed at", "chased", and "slept on".
  - If the subject is "The dog" the possible verbs are "barked at", "chased", and "slept on".
- For the objects:
  - If the verb is "wrote" the possible objects are "the book", "the letter", and "the code".
  - If the verb is "chased" the possible objects are "the ball", "the dog", and "the cat".
  - If the verb is "slept on" the possible objects are "the bed", "the mat", and "the train".
  - If the verb is "meowed at" the possible objects are "Noel", "the door", "the food cupboard".
  - If the verb is "barked at" the possible objects are "the postman", "the car", and "the cat".

Implement this.

<div class="solution">
We're now using the full power of `flatMap` to make decisions in our code that are dependent on what has happened before.

```scala
def verbsFor(subject: String): List[String] =
  subject match {
    case "Noel" => List("wrote", "chased", "slept on")
    case "The cat" => List("meowed at", "chased", "slept on")
    case "The dog" => List("barked at", "chased", "slept on")
  }

def objectsFor(verb: String): List[String] =
  verb match {
    case "wrote" => List("the book", "the letter", "the code")
    case "chased" => List("the ball", "the dog", "the cat")
    case "slept on" => List("the bed", "the mat", "the train")
    case "meowed at" => List("Noel", "the door", "the food cupboard")
    case "barked at" => List("the postman", "the car", "the cat")
  }

def allSentencesConditional: List[(String, String, String)] =
  for {
    subject <- subjects
    verb <- verbsFor(subject)
    obj <- objectsFor(verb)
  } yield (subject, verb, obj)
```
</div>

This model has all the features we need for our full random generation model. In particular we have *conditional distributions*, meaning the choice of, say, verb is dependent or conditional on what has come before.

### Probabilities

We now have a model that we can imagine making arbitrarily complex to generate more and more realistic data, but we're missing the element of probability that would allow us to weight the data generation towards more common outcomes. 

Let's extend our model to work on `List[(A, Double)]`, where `A` is the type of data we are generating and the `Double` is a probability. We're still enumerating all possibilities but we're now associating a probability with each possible outcome.

Start by defining a class `Distribution` that will wrap a `List[(A, Double)]`. (Why?)

<div class="solution">
There are no subtypes involved here, so a simple `final case class` will do. We wrap the `List[(A, Double)]` within a class so we can encapsulate manipulating the probabilities---external code can view the probabilities but probably shouldn't be directly working with them.

```scala
final case class Distribution[A](events: List[(A, Double)])
```
</div>

We should create some convenience constructors for `Distribution`. A useful one is `uniform` which will accept a `List[A]` and create a `Distribution[A]` where each element has equal probability. Make it so.

<div class="solution">
As per Scala convention, convenience constructors should live on the companion object. 

```scala
object Distribution {
  def uniform[A](atoms: List[A]): Distribution[A] = {
    val p = 1.0 / atoms.length
    Distribution(atoms.map(a => a -> p))
  }
}
```
</div>

What are the other methods we must add to implement the models we've seen so far? What are their signatures?

<div class="solution">
We need `flatMap` and `map`. The signatures follow the patterns that `flatMap` and `map` always have:

`def flatMap[B](f: A => Distribution[B]): Distribution[B]`

and

`def map[B](f: A => B): Distribution[B]`
</div>

Now implement these methods. Start with `map`, which is simpler. We might end up with elements appearing multiple times in the list of events after calling `map`. That's absolutely ok.

<div class="solution">
Implementing `map` merely requires we follow the types.

```scala
final case class Distribution[A](events: List[(A, Double)]) {
  def map[B](f: A => B): Distribution[B] =
    Distribution(events map { case (a, p) => f(a) -> p })
}
```
</div>

Now implement `flatMap`. To do so you'll need to combine the probability of an event with the probability of the event it depends on. The correct way to do so is to multiply the probabilities together. The may lead to *unnormalised* probabilities---probabilities that do not sum up to 1. You might find the following two utilities useful, though you don't need to normalise probabilities or ensure that elements are unique for the model to work.

```scala
def normalize: Distribution[A] = {
  val totalWeight = (events map { case (a, p) => p }).sum
  Distribution(events map { case (a,p) => a -> (p / totalWeight) })
}

def compact: Distribution[A] = {
  val distinct = (events map { case (a, p) => a }).distinct
  def prob(a: A): Double =
    (events filter { case (x, p) => x == a } map { case (a, p) => p }).sum

  Distribution(distinct map { a => a -> prob(a) })
}
```

<div class="solution">
Once we know how to combine probabilities we just have to follow the types. I've decided to normalise the probabilities after `flatMap` as it helps avoid numeric underflow, which can occur in complex models. An alternative is to use log-probabilities, replacing multiplication with addition.

```scala
final case class Distribution[A](events: List[(A, Double)]) {
  def map[B](f: A => B): Distribution[B] =
    Distribution(events map { case (a, p) => f(a) -> p })

  def flatMap[B](f: A => Distribution[B]): Distribution[B] =
    Distribution(events flatMap { case (a, p1) =>
                   f(a).events map { case (b, p2) => b -> (p1 * p2) }
                 }).compact.normalize

  def normalize: Distribution[A] = {
    val totalWeight = (events map { case (a, p) => p }).sum
    Distribution(events map { case (a,p) => a -> (p / totalWeight) })
  }

  def compact: Distribution[A] = {
    val distinct = (events map { case (a, p) => a }).distinct
    def prob(a: A): Double =
      (events filter { case (x, p) => x == a } map { case (a, p) => p }).sum

    Distribution(distinct map { a => a -> prob(a) })
  }
}
object Distribution {
  def uniform[A](atoms: List[A]): Distribution[A] = {
    val p = 1.0 / atoms.length
    Distribution(atoms.map(a => a -> p))
  }
}
```
</div>

