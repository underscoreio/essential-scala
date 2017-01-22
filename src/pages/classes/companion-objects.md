## Companion Objects

Sometimes we want to create a method that logically belongs to a class but is independent of any particular object. In Java we would use a *static method* for this, but Scala has a simpler solution that we've seen already: singleton objects.

One common use case is auxiliary constructors. Although Scala does have syntax that lets us define multiple constructors for a class, Scala programmers almost always prefer to implement additional constructors as `apply` methods on an object with the same name as the class. We refer to the object as the *companion object* of the class. For example:

```tut:book:silent
class Timestamp(val seconds: Long)

object Timestamp {
  def apply(hours: Int, minutes: Int, seconds: Int): Timestamp =
    new Timestamp(hours*60*60 + minutes*60 + seconds)
}
```

```tut:book
Timestamp(1, 1, 1).seconds
```

<div class="callout callout-info">
#### Using the Console Effectively {-}

Note our use of the `:paste` command in the transcript above. Companion objects must be defined in the same compilation unit as the classes they support. In a normal codebase this simply means defining the class and object in the same file, but on the REPL we have to enter then in one command using `:paste`.

You can enter `:help` on the REPL to find out more.
</div>

As we saw earlier, Scala has two namespaces: a space of *type names* and a space of *value names*. This separation allows us to name our class and companion object the same thing without conflict.

It is important to note that *the companion object is not an instance of the class*---it is a singleton object with its own type:

```tut:book
Timestamp // note that the type is `Timestamp.type`, not `Timestamp`
```

<div class="callout callout-info">
#### Companion Object Syntax {-}

To define a companion object for a class, in the *same file* as the class define an object with the same name.

```scala
class Name {
  ...
}

object Name {
  ...
}
```
</div>

### Take home points

*Companion objects* provide us with a means to associate functionality with a class without associating it with any instance of that class. They are commonly used to provide additional constructors.

Companion objects *replace Java's static methods*. They provide equivalent functionality and are more flexible.

*A companion object has the same name as its associated class.* This doesn't cause a naming conflict because Scala has two namespaces: the namespace of values and the namespace of types.

*A companion object must be defined in the same file as the associated class.* When typing on the REPL, the class and companion object must be entered in the same block of code using `:paste` mode.

### Exercises

#### Friendly Person Factory

Implement a companion object for `Person` containing an `apply` method that accepts a whole name as a single string rather than individual first and last names.

Tip: you can split a `String` into an `Array` of components as follows:

```tut:book
val parts = "John Doe".split(" ")
parts(0)
```

<div class="solution">
Here is the code:

```tut:book:silent
class Person(val firstName: String, val lastName: String) {
  def name: String =
    s"$firstName $lastName"
}

object Person {
  def apply(name: String): Person = {
    val parts = name.split(" ")
    new Person(parts(0), parts(1))
  }
}
```

And here it is in use:

```tut:book
Person.apply("John Doe").firstName // full method call
Person("John Doe").firstName // sugared apply syntax
```
</div>

#### Extended Body of Work

Write companion objects for `Director` and `Film` as follows:

 - the `Director` companion object should contain:
    - an `apply` method that accepts the same parameters as the constructor of the class
      and returns a new `Director`;
    - a method `older` that accepts two `Directors` and returns the oldest of the two.

 - the `Film` companion object should contain:
    - an `apply` method that accepts the same parameters as the constructor of the class
      and returns a new `Film`;
    - a method `highestRating` that accepts two `Films` and returns the highest
      `imdbRating` of the two;
    - a method `oldestDirectorAtTheTime` that accepts two `Films` and returns the `Director`
      who was oldest at the respective time of filming.

<div class="solution">

This exercise is inteded to provide more practice writing code. The model solution, including the class definitions from the previous section, is now:

```tut:book:silent
class Director(
  val firstName: String,
  val lastName: String,
  val yearOfBirth: Int) {

  def name: String =
    s"$firstName $lastName"

  def copy(
    firstName: String = this.firstName,
    lastName: String = this.lastName,
    yearOfBirth: Int = this.yearOfBirth) =
    new Director(firstName, lastName, yearOfBirth)
}

object Director {
  def apply(firstName: String, lastName: String, yearOfBirth: Int): Director =
    new Director(firstName, lastName, yearOfBirth)

  def older(director1: Director, director2: Director): Director =
    if (director1.yearOfBirth < director2.yearOfBirth) director1 else director2
}

class Film(
  val name: String,
  val yearOfRelease: Int,
  val imdbRating: Double,
  val director: Director) {

  def directorsAge =
    director.yearOfBirth - yearOfRelease

  def isDirectedBy(director: Director) =
    this.director == director

  def copy(
    name: String = this.name,
    yearOfRelease: Int = this.yearOfRelease,
    imdbRating: Double = this.imdbRating,
    director: Director = this.director) =
    new Film(name, yearOfRelease, imdbRating, director)
}

object Film {
  def apply(
    name: String,
    yearOfRelease: Int,
    imdbRating: Double,
    director: Director): Film =
    new Film(name, yearOfRelease, imdbRating, director)

  def newer(film1: Film, film2: Film): Film =
    if (film1.yearOfRelease < film2.yearOfRelease) film1 else film2

  def highestRating(film1: Film, film2: Film): Double = {
    val rating1 = film1.imdbRating
    val rating2 = film2.imdbRating
    if (rating1 > rating2) rating1 else rating2
  }

  def oldestDirectorAtTheTime(film1: Film, film2: Film): Director =
    if (film1.directorsAge > film2.directorsAge) film1.director else film2.director
}
```

</div>

#### Type or Value?

The similarity in naming of classes and companion objects tends to cause confusion for new Scala developers. When reading a block of code it is important to know which parts refer to a class or *type* and which parts refer to a singleton object or *value*.

This is the inspiration for the new hit quiz, *Type or Value?*, which we will be piloting below. In each case identify whether the word `Film` refers to the type or value:

```scala
val prestige: Film = bestFilmByChristopherNolan()
```

<div class="solution">
**Type!**---this code is defining a value `prestige` of type `Film`.
</div>

```scala
new Film("Last Action Hero", 1993, mcTiernan)
```

<div class="solution">
*Type!*---this is a reference to the *constructor* of `Film`. The constructor is part of the *class* `Film`, which is a *type*.
</div>

```scala
Film("Last Action Hero", 1993, mcTiernan)
```

<div class="solution">
*Value!*---this is shorthand for:

```scala
Film.apply("Last Action Hero", 1993, mcTiernan)
```

`apply` is a method defined on the *singleton object* (or value) `Film`.
</div>

```scala
Film.newer(highPlainsDrifter, thomasCrownAffair)
```

<div class="solution">
*Value!*---`newer` is another method defined on the *singleton object* `Film`.
</div>

Finally a tough one...

```scala
Film.type
```

<div class="solution">
*Value!*---This is tricky! You'd be forgiven for getting this one wrong.

`Film.type` refers to the type of the singleton object `Film`, so in this case `Film` is a reference to a value. However, the whole fragment of code is a type.
</div>
