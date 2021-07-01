enablePlugins(MdocPlugin)

import scala.sys.process._

ThisBuild / name               := "essential-scala"
ThisBuild / organization       := "com.essentialscala"
ThisBuild / version            := "0.0.1"

ThisBuild / scalaVersion       := "2.13.5"

ThisBuild / useSuperShell      := false
Global    / logLevel           := Level.Warn

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  // Use `scalac -Wconf:help` for full documentation:
  List(
    "-Wconf",
    List(
      "cat=lint-type-parameter-shadow:silent", // mdoc's nested scope semantics cause lots of these
      "cat=unchecked:warning-summary",
      "any:error" // anything else is an error
    ).mkString(",")
  ).mkString(":")
)

mdocIn := sourceDirectory.value / "pages"
mdocOut := target.value / "pages"

lazy val pages = List(
  "index.md",
  "thanks.md",
  "getting-started/index.md",

  "intro/index.md",
  "intro/objects.md",
  "intro/literals.md",
  "intro/object-literals.md",
  "intro/writing-methods.md",
  "intro/expressions.md",
  "intro/conclusion.md",

  "classes/index.md",
  "classes/classes.md",
  "classes/apply.md",
  "classes/companion-objects.md",
  "classes/case-classes.md",
  "classes/pattern-matching.md",
  "classes/conclusions.md",

  "traits/index.md",
  "traits/traits.md",
  "traits/sealed-traits.md",
  "traits/modelling-data-with-traits.md",
  "traits/working-with-data.md",
  "traits/recursive-data.md",
  "traits/extended-examples.md",
  "traits/conclusions.md",

  "sequencing/index.md",
  "sequencing/generics.md",
  "sequencing/functions.md",
  "sequencing/working-with-data.md",
  "sequencing/modelling-data.md",
  "sequencing/sequencing-computation.md",
  "sequencing/variance.md",
  "sequencing/conclusions.md",

  "collections/index.md",
  "collections/seq.md",
  "collections/working-with-seq.md",
  "collections/for-comprehensions.md",
  "collections/options.md",
  "collections/meeting-monads.md",
  "collections/for-comprehensions-redux.md",
  "collections/map-and-set.md",
  "collections/ranges.md",
  "collections/random-data.md",

  "implicits/index.md",
  "implicits/instances.md",
  "implicits/organising.md",
  "implicits/creating.md",
  "implicits/implicit-parameters.md",
  "implicits/enrichment.md",
  "implicits/using-type-classes.md",
  "implicits/conversions.md",
  "implicits/json.md",

  "conclusions.md",

  "start-of-appendix.md",

  "pattern-matching/index.md",
  "pattern-matching/extractors.md",

  "collections-redux/index.md",
  "collections-redux/seq-implementations.md",
  "collections-redux/arrays-and-strings.md",
  "collections-redux/iterators.md",
  "collections-redux/traversable.md",
  "collections-redux/java-interop.md",
  "collections-redux/mutable-seq.md",

  "solutions.md",
  "links.md",
).map(page => s"target/pages/$page")

/*

The code below outlines steps to build the book:

Each build is independent (even the TeX and PDF builds).
The PDF, HTML, and ePub builds are the important ones.
Others are for debugging.

Each build involves three steps: a setup step, mdoc, and a Pandoc step.
You can run each of these steps indepdendently. For example,
running `pdf` is equivalent to `;pdfSetup; mdoc; pdfPandoc`.

The `all` task is equivalent to `;pdf ;html ;epub`,
except that it only runs `mdoc` once.

The code to build the Pandoc command-line is in `project/Pandoc.scala`.

*/

lazy val pdfSetup  = taskKey[Unit]("Pre-mdoc component of the PDF build")
lazy val htmlSetup = taskKey[Unit]("Pre-mdoc component of the HTML build")
lazy val epubSetup = taskKey[Unit]("Pre-mdoc component of the ePub build")

lazy val texSetup  = taskKey[Unit]("Pre-mdoc component of the TeX debug build")
lazy val jsonSetup = taskKey[Unit]("Pre-mdoc component of the JSON AST debug build")

pdfSetup := {
  "mkdir -p dist".!
}

htmlSetup := {
  "mkdir -p dist src/temp".!
}

epubSetup := {
  "mkdir -p dist src/temp".!
  "npm install".!
  "npx lessc --include-path=node_modules --strict-imports src/less/epub.less src/temp/epub.css".!
}

texSetup := {
  "mkdir -p dist".!
}

jsonSetup := {
  "mkdir -p dist".!
}

lazy val pdfPandoc  = taskKey[Unit]("Pandoc component of the PDF build")
lazy val htmlPandoc = taskKey[Unit]("Pandoc component of the HTML build")
lazy val epubPandoc = taskKey[Unit]("Pandoc component of the ePub build")

lazy val texPandoc  = taskKey[Unit]("Pandoc component of the TeX debug build")
lazy val jsonPandoc = taskKey[Unit]("Pandoc component of the JSON AST debug build")

pdfPandoc  := {
  Pandoc.commandLine(
    pages = pages,
    output = "dist/essential-scala.pdf",
    template = Some("src/templates/template.tex"),
    metadata = List(
      s"src/meta/metadata.yaml",
      s"src/meta/pdf.yaml",
    ),
    filters = List(
      s"pandoc-crossref",
      // s"src/filters/merge-code.sc",
      // s"src/filters/callout.sc",
      // s"src/filters/columns.sc",
      s"src/filters/vector-images.sc",
      s"src/filters/solutions-in-appendix.sc",
      // s"src/filters/listings.sc",
    ),
    extraArgs = List(
      s"--include-before-body=src/templates/cover-notes.tex",
    ),
  ).!
}

htmlPandoc := {
  Pandoc.commandLine(
    pages = pages,
    output = "dist/essential-scala.html",
    // template = Some("src/templates/template.html"),
    metadata = List(
      s"src/meta/metadata.yaml",
    ),
    filters = List(
      s"pandoc-crossref",
      // s"src/filters/merge-code.sc",
      s"src/filters/responsive-tables.sc",
      s"src/filters/solutions-in-disclosures.sc",
      s"src/filters/vector-images.sc",
    ),
    extraArgs = List(
      s"--include-before-body=src/templates/cover-notes.html",
    ),
  ).!
}

epubPandoc := {
  Pandoc.commandLine(
    pages = pages,
    output = "dist/essential-scala.epub",
    template = Some("src/templates/template.epub.html"),
    metadata = List(
      s"src/meta/metadata.yaml",
    ),
    filters = List(
      s"pandoc-crossref",
      // s"src/filters/merge-code.sc",
      s"src/filters/responsive-tables.sc",
      s"src/filters/solutions-in-appendix.sc",
      s"src/filters/vector-images.sc",
    ),
    extraArgs = List(
      s"--css=src/temp/epub.css",
      s"--epub-cover-image=src/covers/epub-cover.png",
      s"--include-before-body=src/templates/cover-notes.tex",
    ),
  ).!
}

jsonPandoc := {
  Pandoc.commandLine(
    pages = pages,
    output = "dist/essential-scala.json",
    metadata = List(
      s"src/meta/metadata.yaml",
    ),
    filters = List(
      s"pandoc-crossref",
      // s"src/filters/merge-code.sc",
      s"src/filters/responsive-tables.sc",
      s"src/filters/solutions-in-disclosures.sc",
      s"src/filters/vector-images.sc",
    ),
    extraArgs = List(
      s"--include-before-body=src/templates/cover-notes.html",
    ),
  ).!
}

lazy val pdf  = taskKey[Unit]("Build the PDF version of the book")
lazy val html = taskKey[Unit]("Build the HTML version of the book")
lazy val epub = taskKey[Unit]("Build the ePub version of the book")

pdf  := {
  pdfSetup.value
  mdoc.toTask("").value
  pdfPandoc.value
}

html := {
  htmlSetup.value
  mdoc.toTask("").value
  htmlPandoc.value
}

epub := {
  epubSetup.value
  mdoc.toTask("").value
  epubPandoc.value
}

lazy val all  = taskKey[Unit]("Build the PDF, HTML, and ePub versions of the book")

all := {
  pdfSetup.value
  htmlSetup.value
  epubSetup.value

  mdoc.toTask("").value

  pdfPandoc.value
  htmlPandoc.value
  epubPandoc.value
}
