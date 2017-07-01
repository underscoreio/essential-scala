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
  def discrete[A](events: List[(A,Double)]): Distribution[A] =
    Distribution(events).compact.normalize
}

sealed trait Coin
case object Heads extends Coin
case object Tails extends Coin

val fairCoin: Distribution[Coin] = Distribution.uniform(List(Heads, Tails))
val threeFlips =
  for {
    c1 <- fairCoin
    c2 <- fairCoin
    c3 <- fairCoin
  } yield (c1, c2, c3)

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

// Probability the cat is harassing me
val pHarassing: Double =
  foodModel.events filter {
    case ((_, Harassing), _) => true
    case ((_, Asleep), _) => false
  } map { case (a, p) => p } sum

// Probability the food is cooked given the cat is harassing me
val pCookedGivenHarassing: Option[Double] =
  foodModel.events collectFirst[Double] {
    case ((Cooked, Harassing), p) => p
  } map (_ / pHarassing)
