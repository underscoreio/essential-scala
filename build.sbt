lazy val root = project.in(file("."))
  .settings(tutSettings)

tutSourceDirectory := sourceDirectory.value / "pages"

tutTargetDirectory := target.value / "pages"

// scalaOrganization in ThisBuild := "org.typelevel"
scalaVersion in ThisBuild := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings",
  "-Ywarn-dead-code"
  //"-Yliteral-types"
)

resolvers ++= Seq(Resolver.sonatypeRepo("snapshots"))

libraryDependencies ++= Seq()

lazy val pdf  = taskKey[Unit]("Build the PDF version of the book")
lazy val html = taskKey[Unit]("Build the HTML version of the book")
lazy val epub = taskKey[Unit]("Build the ePub version of the book")
lazy val all  = taskKey[Unit]("Build all versions of the book")

pdf  := { tutQuick.value ; "grunt pdf".!  }
html := { tutQuick.value ; "grunt html".! }
epub := { tutQuick.value ; "grunt epub".! }

all  := {
  tutQuick.value
  "grunt pdf".!
  "grunt html".!
  "grunt epub".!
}
