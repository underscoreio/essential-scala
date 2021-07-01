#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.5.0")

@
import cats.Id
import spandoc._
import spandoc.ast._
import spandoc.transform._

/*
This script supports embedding PDF/SVG images in documents.
The user specifies an image filename with the extension `pdf+svg`:

  path/to/file.pdf+svg

This script replaces the extension with `pdf` when compiling a PDF
and `svg` when compiling an HTML or ePub.
*/

val PdfSvgRegex = "^(.*)[.]pdf[+]svg$".r

def transform(format: String): Transform[Id] = {
  val extension = if(format == "pdf") "pdf" else "svg"

  BottomUp.inline {
    case Image(attr, caption, Target(PdfSvgRegex(filenameStem), title)) =>
      Image(attr, caption, Target(s"${filenameStem}.${extension}", title))
  }
}


@main
def main(format: String) =
  transformStdin(transform(format))
