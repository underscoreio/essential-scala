## Generating Random Data

In this section we have an extended case study of generating random data. The ideas here have many applications. For example, in generating data for testing, as used in _property based testing_, in _probabilistic programming_, a new area of machine learning, and, if you're going through the extended case study, in _generative art_.

### Random Words

We'll start by generating text. Imagine we wanted to generate (somewhat) realistic text, perhaps to use as a placeholder to fill in parts of a website design. If we took a large amount of real text we could analyse to work out for each word what the most common words following it are. Such a model is known as a _Markov chain_.

To keep this example to a reasonable size we're going to deal with a really simplified version of the problem, where all sentences have the form _subject_-_verb_-_object_. For example, "Noel wrote code".

Write a program to generate all possible sentences given the following model:

- subjects are `List("Noel", "The cat", "The dog")`;
- verbs are `List("wrote", "chased", "slept on")`; and
- objects are `List("the book", "the ball", "the bed")`.

<div class="solution">
The following code will compute all possible sentences. The equivalent with explicit `flatMap` and `map` would also work.

Note that `flatMap` has more power than we need for this example. We could use the `subject` to alter how we choose the `verb`, for example. We'll use this ability in the next exercise.

```scala mdoc:silent
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
We're now using the full power of `flatMap` and `map` (via our for comprehension) to make decisions in our code that are dependent on what has happened before.

```scala mdoc:silent
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

This model has all the features we need for our full random generation model. In particular we have _conditional distributions_, meaning the choice of, say, verb is dependent or conditional on what has come before.

### Probabilities

We now have a model that we can imagine making arbitrarily complex to generate more and more realistic data, but we're missing the element of probability that would allow us to weight the data generation towards more common outcomes.

Let's extend our model to work on `List[(A, Double)]`, where `A` is the type of data we are generating and the `Double` is a probability. We're still enumerating all possibilities but we're now associating a probability with each possible outcome.

Start by defining a class `Distribution` that will wrap a `List[(A, Double)]`. (Why?)

<div class="solution">
There are no subtypes involved here, so a simple `final case class` will do. We wrap the `List[(A, Double)]` within a class so we can encapsulate manipulating the probabilities---external code can view the probabilities but probably shouldn't be directly working with them.

```scala mdoc:silent
final case class Distribution[A](events: List[(A, Double)])
```

</div>

We should create some convenience constructors for `Distribution`. A useful one is `uniform` which will accept a `List[A]` and create a `Distribution[A]` where each element has equal probability. Make it so.

<div class="solution">
The convenience constructor looks like this:

```scala mdoc
def uniform[A](atoms: List[A]): Distribution[A] = {
  val p = 1.0 / atoms.length
  Distribution(atoms.map(a => a -> p))
}
```

According to Scala convention, convenience constructors should normally live on the companion object.

</div>

What are the other methods we must add to implement the models we've seen so far? What are their signatures?

<div class="solution">
We need `flatMap` and `map`. The signatures follow the patterns that `flatMap` and `map` always have:

```scala
def flatMap[B](f: A => Distribution[B]): Distribution[B]
```

and

```scala
def map[B](f: A => B): Distribution[B]
```

</div>

Now implement these methods. Start with `map`, which is simpler. We might end up with elements appearing multiple times in the list of events after calling `map`. That's absolutely ok.

<div class="solution">
Implementing `map` merely requires we follow the types.

```scala mdoc:silent
final case class Distribution[A](events: List[(A, Double)]) {
  def map[B](f: A => B): Distribution[B] =
    Distribution(events map { case (a, p) => f(a) -> p })
}
```

</div>

Now implement `flatMap`. To do so you'll need to combine the probability of an event with the probability of the event it depends on. The correct way to do so is to multiply the probabilities together. This may lead to _unnormalised_ probabilities---probabilities that do not sum up to 1. You might find the following two utilities useful, though you don't need to normalise probabilities or ensure that elements are unique for the model to work.

```scala mdoc:silent
final case class Distribution[A](events: List[(A, Double)]) {
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
```

<div class="solution">
Once we know how to combine probabilities we just have to follow the types. I've decided to normalise the probabilities after `flatMap` as it helps avoid numeric underflow, which can occur in complex models. An alternative is to use log-probabilities, replacing multiplication with addition.

```scala mdoc:silent
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
```

```scala
object Distribution {
  def uniform[A](atoms: List[A]): Distribution[A] = {
    val p = 1.0 / atoms.length
    Distribution(atoms.map(a => a -> p))
  }
}
```

</div>

### Examples

With `Distribution` we can now define some interesting model. We could do some classic problems, such as working out the probability that a coin flip gives three heads in a row.

```scala mdoc:silent
sealed trait Coin
case object Heads extends Coin
case object Tails extends Coin
```

```scala mdoc:invisible
def uniform[A](atoms: List[A]): Distribution[A] = {
  val p = 1.0 / atoms.length
  Distribution(atoms.map(a => a -> p))
}
val fairCoin: Distribution[Coin] = uniform[Coin](List(Heads, Tails)) // workaround for Tut
```

```scala
val fairCoin: Distribution[Coin] = Distribution.uniform(List(Heads, Tails))
```

```scala mdoc
val threeFlips =
  for {
    c1 <- fairCoin
    c2 <- fairCoin
    c3 <- fairCoin
  } yield (c1, c2, c3)
```

From this we can read of the probability of three heads being 0.125, as we'd expect.

Let's create a more complex model. Imagine the following situation:

I put my food into the oven and after some time it ready to eat and produces delicious smell with probability 0.3 and otherwise it is still raw and produces no smell with probability 0.7. If there are delicious smells the cat comes to harass me with probability 0.8, and otherwise it stays asleep. If there is no smell the cat harasses me for the hell of it with probability 0.4 and otherwise stays asleep.

Implement this model and answer the question: if the cat comes to harass me what is the probability my food is producing delicious smells (and therefore is ready to eat.)

I found it useful to add this constructor to the companion object of `Distribution`:

```scala mdoc:silent
def discrete[A](events: List[(A,Double)]): Distribution[A] =
  Distribution(events).compact.normalize
```

```scala mdoc:invisible
// workaround for Tut
object Distribution {
  def discrete[A](events: List[(A,Double)]): Distribution[A] =
    new Distribution(events).compact.normalize
}
```

<div class="solution">
First I constructed the model

```scala mdoc:silent
// We assume cooked food makes delicious smells with probability 1.0, and raw
// food makes no smell with probability 0.0.
sealed trait Food
case object Raw extends Food
case object Cooked extends Food

val food: Distribution[Food] =
  Distribution.discrete(List(Cooked -> 0.3, Raw -> 0.7))

sealed trait Cat
case object Asleep extends Cat
case object Harassing extends Cat

def cat(food: Food): Distribution[Cat] =
  food match {
    case Cooked => Distribution.discrete(List(Harassing -> 0.8, Asleep -> 0.2))
    case Raw => Distribution.discrete(List(Harassing -> 0.4, Asleep -> 0.6))
  }

val foodModel: Distribution[(Food, Cat)] =
  for {
    f <- food
    c <- cat(f)
  } yield (f, c)
```

From `foodModel` we could read off the probabilities of interest, but it's more fun to write some code to do this for us. Here's what I did.

```scala mdoc:silent
// Probability the cat is harassing me
val pHarassing: Double =
  foodModel.events.filter {
    case ((_, Harassing), _) => true
    case ((_, Asleep), _) => false
  }.map { case (a, p) => p }.sum

// Probability the food is cooked given the cat is harassing me
val pCookedGivenHarassing: Option[Double] =
  foodModel.events collectFirst[Double] {
    case ((Cooked, Harassing), p) => p
  } map (_ / pHarassing)
```

From this we can see the probability my food is cooked given the cat is harassing me is probably 0.46. I should probably check the oven even though it's more likely the food isn't cooked because leaving my food in and it getting burned is a far worse outcome than checking my food while it is still raw.

This example also shows us that to use this library for real we'd probably want to define a lot of utility functions, such as `filter`, directly on distribution. We also need to keep probabilities unnormalised after certain operations, such as filtering, so we can compute conditional probabilities correctly.

</div>

### Next Steps

The current library is limited to working with discrete events. If we wanted to work with continuous domains, such as coordinates in the plane, we would need a different representation as we clearly can't represent all possible outcomes. Also, we can easily run into issues when working with complex discrete models, as the number of events increases exponentially with each `flatMap`.

Instead of representing all events we can sample from the distributions of interest and maintain a set of samples. Varying the size of the set allows us to tradeoff accuracy with computational resources.

We could use the same style of implementation with a sampling representation, but this would require us to fix the number of samples in advance. It's more useful to be able to repeatedly sample from the same model, so that the user can ask for more samples if they decide they need higher accuracy. Doing so requires that we separate defining the structure of the model from the process of sampling, hence reifying the model. We're not going to go further into this implementation here, but if you're going through the case study you'll pick up the techniques needed to implement it.
