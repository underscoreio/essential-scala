#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.5.0")

@
import cats.data.State
import cats.implicits._
import spandoc._
import spandoc.ast._
import spandoc.transform._

val transform = TopDown.block {
  case Header(_, attr @ Attr("solutions", _, _), _) =>
    Div.empty

  case Div(attr @ Attr(_, classes, _), _) if classes.contains("solutions") =>
    Div.empty
}

@main
def main(format: String) =
  transformStdin(transform)
