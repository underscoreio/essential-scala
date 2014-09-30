---
layout: page
---

# SBT

SBT, or "Simple Build Tool", is the recommended build tool for Scala. It is similar to the Maven build tool, but allows you to write configuration files using Scala code as opposed to XML. SBT projects has the benefit of being significantly terser than their Maven equivalents.

In this section we will show you the basics of setting up an SBT project. The intention is to give you a quick way of writing, compiling, and running Scala code. We won't go into the details of writing build configurations -- there is plenty of information for the budding SBT users online in the [SBT Wiki](https://github.com/harrah/xsbt/wiki) on Github and the [SBT community website](http://www.scala-sbt.org/).

# Requirements

In order to get started with Core Scala, you will need to have the following softwar installed and configured:

 - a Java 6 or 7 compatible JVM;
 - a recent version of Git.

# Getting started

To start with, clone our template SBT project from Github:

~~~ scala
bash:~$ git clone git://github.com/underscoreconsulting/essential-scala-template.git myproject
Cloning into myproject...

bash:~$ cd myproject

bash:~/myproject$
~~~

The SBT launcher script is provided for you in the file `sbt`. Run it now to start a Scala console. SBT will download JAR files for Scala and various dependencies and cache them on your hard drive. At the end of the process you will see a `scala>` command prompt:

~~~ scala
Trip:~/myproject$ ./sbt console
Detected sbt version 0.11.3
Using /Users/me/.sbt/0.11.3 as sbt dir, -sbt-dir to override.
[info] Loading project definition from /Users/me/myproject/project
[info] Updating {file:/Users/me/myproject/project/}default-02b1d7...
[info] Resolving org.scala-sbt#sbt_2.9.1;0.11.3 ...

 # .... Lots of "Resolving" lines ....

[info] Done updating.
[info] Set current project to essential-scala (in build file:/Users/me/myproject/)
[info] Updating {file:/Users/me/myproject/}default-2c3445...
[info] Resolving org.scala-lang#scala-library;2.9.2 ...
[info] Done updating.
[info] Starting scala interpreter...
[info]
Welcome to Scala version 2.9.2 (Java HotSpot(TM) 64-Bit Server VM, Java 1.6.0_35).
Type in expressions to have them evaluated.
Type :help for more information.

scala>
~~~

Congratulations - you have installed Scala! You can now start interactively playing with Scala code:

~~~ scala
scala> 1 + 1
res0: Int = 2

scala> println("Hello world!")
Hello world!
~~~

Issue the command `:quit` to leave the Scala console and return to your OS:

~~~ bash
scala> :quit

[success] Total time: 408 s, completed Sep 18, 2012 12:07:19 PM

bash:~/myproject$
~~~

# SBT: The Rebel Cut

Our template uses an [SBT Launcher script](https://github.com/paulp/sbt-extras/blob/master/sbt) written by Paul Phillips. The launcher, which helps you select and manage multiple versions of SBT and Scala from the bash command line, will become invaluable once you start working with different Scala projects that span multiple versions of Scala and SBT.

Once the course is over, we recommend you get hold of the latest version of the script from Paul's Github project and add it to your `$PATH`.
