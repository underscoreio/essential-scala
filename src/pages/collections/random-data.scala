val subjects = List("Noel", "The cat", "The dog")
val verbs = List("wrote", "chased", "slept on")
val objects = List("the book", "the ball", "the bed")

def allSentences: List[(String, String, String)] =
  for {
    subject <- subjects
    verb <- verbs
    obj <- objects
  } yield (subject, verb, obj)

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
