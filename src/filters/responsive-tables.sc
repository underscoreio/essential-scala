#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.5.0")

@
import cats.Id
import spandoc._
import spandoc.ast._
import spandoc.transform._

/*
This script wraps tables in `<div class="table-responsive">...</div>`
in HTML/ePub modes. This tag is used by Bootstrap CSS
to create horizontally scrolling tables on narrow pages.
*/

val transform: Transform[Id] =
  BottomUp.block {
    case table: Table =>
      Div(Attr("", List("table-responsive")), Vector(table))
  }

@main
def main(format: String) =
  format match {
    case "html" | "epub" =>
      transformStdin(transform)

    case _ =>
      transformStdin(identity)
  }
