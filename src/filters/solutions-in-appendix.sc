#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.5.0")

@
import cats.data.State
import cats.implicits._
import spandoc._
import spandoc.ast._
import spandoc.transform._

/*
This script locates divs like `<div class="solution">...</div>`
and removes them from the main flow of the document,
depositing them inside a `<div class="solutions">...</div>`
(located in an appendix at the end of the book).
*/

case class Solution(exerciseHeader: Header, content: Block) {
  def render: Vector[Block] =
    Vector(exerciseHeader, content)
}

case class Accum(
  lastHeader: Header,
  solutions: List[Solution],
) {
  def lastHeader(header: Header): Accum =
    copy(lastHeader = header)

  def appendSolution(block: Div): Accum =
    copy(solutions = solutions :+ Solution(lastHeader, block))
}

type F[A] = State[Accum, A]

object transform extends TopDown[F] {

  def blockTransform: BlockTransform = {
    case header @ Header(level, attr, body) =>
      State.modify[Accum](accum => accum.copy(lastHeader = header)).map(_ => header)

    case div @ Div(Attr(_, classes, _), _) if classes.contains("solution") =>
      State.modify[Accum](accum =>  :+ Solution).map(_ => Div.empty)

    case div @ Div(attr @ Attr(_, classes, _), children) if classes.contains("solutions") =>
      State.get[Accum].map(solutions => Div(attr, children ++ solutions.flatMap(_.render)))
    }

  def inlineTransform: InlineTransform = {
    case inline => inline.pure[F]
  }

}

transformStdin(doc => transform(doc).runA(Nil).value)
