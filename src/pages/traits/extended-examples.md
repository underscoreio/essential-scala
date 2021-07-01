## Extended Examples

To test your skills with algebraic data types and structural recursion here are some larger projects to attempt.

#### A Calculator

In this exercise we'll implement a simple interpreter for programs containing only numeric operations.

We start by defining some types to represent the expressions we'll be operating on. In the compiler literature this is known as an _abstract syntax tree_.

Our representation is:

- An `Expression` is an `Addition`, `Subtraction`, or a `Number`;
- An Addition has a `left` and `right` Expression;
- A Subtraction has a `left` and `right` Expression; or
- A Number has a `value` of type `Double`.

Implement this in Scala.

<div class="solution">
This is a straightforward algebraic data type.

```scala mdoc:silent
sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Number(value: Double) extends Expression
```

</div>

Now implement a method `eval` that converts an `Expression` to a `Double`. Use polymorphism or pattern matching as you see fit. Explain your choice of implementation method.

<div class="solution">
I used pattern matching as it's more compact and I feel this makes the code easier to read.

```scala
sealed trait Expression {
  def eval: Double =
    this match {
      case Addition(l, r) => l.eval + r.eval
      case Subtraction(l, r) => l.eval - r.eval
      case Number(v) => v
    }
}
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Number(value: Int) extends Expression
```

</div>

We're now going to add some expressions that call fail: division and square root. Start by extending the abstract syntax tree to include representations for `Division` and `SquareRoot`.

<div class="solution">
```scala mdoc:silent
sealed trait Expression
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Double) extends Expression
```
</div>

Now we're going to change `eval` to represent that a computation can fail. (`Double` uses `NaN` to indicate a computation failed, but we want to be helpful to the user and tell them why the computation failed.) Implement an appropriate algebraic data type.

<div class="solution">
We did this in the previous section.

```scala mdoc:silent
sealed trait Calculation
final case class Success(result: Double) extends Calculation
final case class Failure(reason: String) extends Calculation
```

</div>

Now change `eval` to return your result type, which I have called `Calculation` in my implementation. Here are some examples:

```scala
assert(Addition(SquareRoot(Number(-1.0)), Number(2.0)).eval ==
       Failure("Square root of negative number"))
assert(Addition(SquareRoot(Number(4.0)), Number(2.0)).eval == Success(4.0))
assert(Division(Number(4), Number(0)).eval == Failure("Division by zero"))
```

<div class="solution">
All this repeated pattern matching gets very tedious, doesn't it! We're going to see how we can abstract this in the next section.

```scala
sealed trait Expression {
  def eval: Calculation =
    this match {
      case Addition(l, r) =>
          l.eval match {
            case Failure(reason) => Failure(reason)
            case Success(r1) =>
              r.eval match {
                case Failure(reason) => Failure(reason)
                case Success(r2) => Success(r1 + r2)
              }
          }
      case Subtraction(l, r) =>
          l.eval match {
            case Failure(reason) => Failure(reason)
            case Success(r1) =>
              r.eval match {
                case Failure(reason) => Failure(reason)
                case Success(r2) => Success(r1 - r2)
              }
          }
      case Division(l, r) =>
        l.eval match {
          case Failure(reason) => Failure(reason)
          case Success(r1) =>
            r.eval match {
              case Failure(reason) => Failure(reason)
              case Success(r2) =>
                if(r2 == 0)
                  Failure("Division by zero")
                else
                  Success(r1 / r2)
            }
        }
      case SquareRoot(v) =>
        v.eval match {
          case Success(r) =>
            if(r < 0)
              Failure("Square root of negative number")
            else
              Success(Math.sqrt(r))
          case Failure(reason) => Failure(reason)
        }
      case Number(v) => Success(v)
    }
}
final case class Addition(left: Expression, right: Expression) extends Expression
final case class Subtraction(left: Expression, right: Expression) extends Expression
final case class Division(left: Expression, right: Expression) extends Expression
final case class SquareRoot(value: Expression) extends Expression
final case class Number(value: Int) extends Expression
```

</div>

#### JSON

In the calculator exercise we gave you the algebraic data type representation. In this exercise we want you to design the algebraic data type yourself. We're going to work in what is hopefully a familiar domain: [JSON][link-json].

Design an algebraic data type to represent JSON. Don't go directly to code. Start by sketching out the design in terms of logical ands and ors---the building blocks of algebraic data types. You might find it useful to use a notation similar to [BNF][link-bnf]. For example, we could represent the `Expression` data type from the previous exercise as follows:

```bash
Expression ::= Addition left:Expression right:Expression
             | Subtraction left:Expression right:Expression
             | Division left:Expression right:Expression
             | SquareRoot value:Expression
             | Number value:Int
```

This simplified notation allows us to concentrate on the structure of the algebraic data type without worrying about the intricacies of Scala syntax.

Note you'll need a sequence type to model JSON, and we haven't looked at Scala's collection library yet. However we have seen how to implement a list as an algebraic data type.

Here are some examples of JSON you'll need to be able to represent

```json
["a string", 1.0, true]
{
  "a": [1,2,3],
  "b": ["a","b","c"]
  "c": { "doh":true, "ray":false, "me":1 }
}
```

<div class="solution">
There are many possible ways to model JSON. Here's one, which is a fairly direct translation of the railroad diagrams in the JSON spec.

```bash
Json ::= JsNumber value:Double
       | JsString value:String
       | JsBoolean value:Boolean
       | JsNull
       | JsSequence
       | JsObject
JsSequence ::= SeqCell head:Json tail:JsSequence
             | SeqEnd
JsObject ::= ObjectCell key:String value:Json tail:JsObject
           | ObjectEnd
```

</div>

Translate your representation to Scala code.

<div class="solution">
This should be a mechanical process. This is the point of algebraic data types---we do the work in modelling the data, and the code follows directly from that model.

```scala mdoc:silent
sealed trait Json
final case class JsNumber(value: Double) extends Json
final case class JsString(value: String) extends Json
final case class JsBoolean(value: Boolean) extends Json
case object JsNull extends Json
sealed trait JsSequence extends Json
final case class SeqCell(head: Json, tail: JsSequence) extends JsSequence
case object SeqEnd extends JsSequence
sealed trait JsObject extends Json
final case class ObjectCell(key: String, value: Json, tail: JsObject) extends JsObject
case object ObjectEnd extends JsObject
```

</div>

Now add a method to convert your JSON representation to a `String`. Make sure you enclose strings in quotes, and handle arrays and objects properly.

<div class="solution">
This is an application of structural recursion, as all transformations on algebraic data types are, with the wrinkle that we have to treat the sequence types specially. Here is my solution.

```scala mdoc:reset:book:silent
object json {
  sealed trait Json {
    def print: String = {
      def quote(s: String): String =
        '"'.toString ++ s ++ '"'.toString
      def seqToJson(seq: SeqCell): String =
        seq match {
          case SeqCell(h, t @ SeqCell(_, _)) =>
            s"${h.print}, ${seqToJson(t)}"
          case SeqCell(h, SeqEnd) => h.print
        }

      def objectToJson(obj: ObjectCell): String =
        obj match {
          case ObjectCell(k, v, t @ ObjectCell(_, _, _)) =>
            s"${quote(k)}: ${v.print}, ${objectToJson(t)}"
          case ObjectCell(k, v, ObjectEnd) =>
            s"${quote(k)}: ${v.print}"
        }

      this match {
        case JsNumber(v) => v.toString
        case JsString(v) => quote(v)
        case JsBoolean(v) => v.toString
        case JsNull => "null"
        case s @ SeqCell(_, _) => "[" ++ seqToJson(s) ++ "]"
        case SeqEnd => "[]"
        case o @ ObjectCell(_, _, _) => "{" ++ objectToJson(o) ++ "}"
        case ObjectEnd => "{}"
      }
    }
  }
  final case class JsNumber(value: Double) extends Json
  final case class JsString(value: String) extends Json
  final case class JsBoolean(value: Boolean) extends Json
  case object JsNull extends Json
  sealed trait JsSequence extends Json
  final case class SeqCell(head: Json, tail: JsSequence) extends JsSequence
  case object SeqEnd extends JsSequence
  sealed trait JsObject extends Json
  final case class ObjectCell(key: String, value: Json, tail: JsObject) extends JsObject
  case object ObjectEnd extends JsObject
}
```

</div>

Test your method works. Here are some examples using the representation I chose.

```scala mdoc:invisible
import json._
```

```scala mdoc
SeqCell(JsString("a string"), SeqCell(JsNumber(1.0), SeqCell(JsBoolean(true), SeqEnd))).print

ObjectCell(
  "a", SeqCell(JsNumber(1.0), SeqCell(JsNumber(2.0), SeqCell(JsNumber(3.0), SeqEnd))),
  ObjectCell(
    "b", SeqCell(JsString("a"), SeqCell(JsString("b"), SeqCell(JsString("c"), SeqEnd))),
    ObjectCell(
      "c", ObjectCell("doh", JsBoolean(true),
             ObjectCell("ray", JsBoolean(false),
               ObjectCell("me", JsNumber(1.0), ObjectEnd))),
      ObjectEnd
    )
  )
).print
```

#### Music

In the JSON exercise there was a well defined specification to model. In this exercise we want to work on modelling skills given a rather fuzzy specification. The goal is to model music. You can choose to interpret this how you want, making your model as simple or complex as you like. The critical thing is to be able to justify the decisions you made, and to understand the limits of your model.

You might find it easiest to use the BNF notation, introduced in the JSON exercise, to write down your model.

<div class="solution">
My solution models a very simplified version of Western music. My fundamental "atom" is the note, which consists of a pitch and a duration.

```bash
Note ::= pitch:Pitch duration:Duration
```

I'm assuming I have a data for `Pitch` representing tones on the standard musical scale from `C0` (about 16Hz) to `C8`. Something like

```bash
Pitch ::= C0 | CSharp0 | D0 | DSharp0 | F0 | FSharp0 | ... | C8 | Rest
```

Note that I included `Rest` as a pitch, so I can model silence.

We already seem some limitations. I'm not modelling notes that fall outside the scale (microtones) or music systems that use other scales. Furthermore, in most tuning systems flats and their enharmonic sharps (e.g. C-sharp and D-flat) are not the same note, but I'm ignoring that distinction here.

We could break this representation down further into a tone

```bash
Tone ::= C | CSharp | D | DSharp | F | FSharp | ... | B
```

and an octave

```bash
Octave ::= 0 | 1 | 2 | ... | 8
```

and then

```bash
Pitch ::= tone:Tone octave:Octave
```

Durations are a mess in standard musical notation. There are a bunch of named durations (semitone, quaver, etc.) along with dots and tied notes to represent other durations. We can do better by simply saying our music has an atomic unit of time, which we'll call a beat, and each duration is zero or more beats.

```bash
Duration ::= 0 | 1 | 2 | ...
```

In other words, `Duration` is a natural number. In Scala we might model this with an `Int`, or create a type to represent the additional constraint we put over `Int`.

Again, this representation comes with limitations. Namely we can't represent music that doesn't fit cleanly into some division of time---so called free time music.

Finally we should get to means of composition of notes. There are two main ways: we can play notes in sequence or at the same time.

```bash
Phrase ::= Sequence | Parallek
Sequence ::= SeqCell phrase:Phrase tail:Sequence
           | SeqEnd

Parallel ::= ParCell phrase:Phrase tail:Parallel
           | ParEnd
```

This representation allows us to arbitrarily nest parallel and sequential units of notes. We might prefer a normalised representation, such as

```bash
Sequence ::= SeqCell note:Note tail:Sequence
           | SeqEnd

Parallel ::= ParCell sequence:Sequence tail:Parallel
           | ParEnd
```

There are many things missing from this model. Some of them include:

- We don't model musical dynamics in any way. Notes can be louder or softer, and volume can change while a note is being played. Notes do not always have constant pitch, either. Pitch bends or slurs are examples of changing pitches in a single note

- We haven't modelled different instruments at all.

- We haven't modelled effects, like echo and distortion, that make up an important part of modern music.
</div>
