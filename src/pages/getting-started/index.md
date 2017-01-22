# Getting Started

Throughout this book we will be working with short examples of Scala code. There are two recommended ways of doing this:

 1. Using the *Scala console* (better for people who like command lines)

 2. Using *Worksheets* feature of *Scala IDE* (better for people who like IDEs)

We'll walk through the setup for each process here.

## Setting up the Scala Console

Follow the instructions on [http://scala-lang.org](http://scala-lang.org) to set Scala up on your computer. Once Scala is installed, you should be able to run an interactive console by typing `scala` at your command line prompt. Here's an example from OS X:

```bash
dave@Jade ~> scala
Welcome to Scala version 2.11.4 (Java HotSpot(TM) 64-Bit Server VM, Java 1.7.0_45).
Type in expressions to have them evaluated.
Type :help for more information.

scala>
```

You can enter individual expressions at the `scala>` prompt and press *Enter* to compile and execute them:

```tut
"Hello world!"
```

### Entering Single-Line Expressions

Let's try entering a simple expression:

```tut
1 + 2 + 3
```

When we press Enter, the console responds with three things:

 - an *identifier* `res1`;
 - a *type* `Int`;
 - a *value* `6`.

As we will see in the next chapter, every expression in Scala has a *type* and a *value*. The type is determined at compile time and the value is determined by executing the expression. Both of these are reported here.

The identifier `res1` is a convenience provided by the console to allow us to refer to the result of the expression in future expressions. For example, we can multiply our result by two as folllows:

```tut
res1 * 2
```

If we enter an expression that doesn't yield a useful value, the console won't print anything in response:

```tut
println("Hello world!")
```

Here, the output `"Hello world!"` is from our `println` statement---the expression we entered doesn't actually return a value. The console doesn't provide output similar to the output we saw above.

### Entering Multi-Line Expressions

We can split long expressions across multiple lines quite simply. If we press enter before the end of an expression, the console will print a `|` character to indicate that we can continue on the next line:

```tut
for(i <- 1 to 3) {
  println(i)
}
```

Sometimes we want to enter multiple expressions at once. In these cases we can use the `:paste` command. We simply type `:paste`, press Enter, and write (or copy-and-paste) our code. When we're done we press `Ctrl+D` to compile and execute the code as normal. The console prints output for every expression in one big block at the end of the input:

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

val x = 1
val y = 2
x + y

// Exiting paste mode, now interpreting.

x: Int = 1
y: Int = 2
res6: Int = 3
```

If we have Scala code in a file, we can use `:paste` to paste the contents of the file into the console. This is much more convenient than re-entering expressions in the console. For example, with a file named `example.txt` containing `1 + 2 + 3` we can use `:paste` like so:

```scala
scala> :paste example.scala
Pasting file example.scala...
res0: Int = 6
```

### Printing the Type of an Expression

One final tip for using the console. Occasionally we want to know the *type* of an expression without actually running it. To do this we can use the `:type` command:

```scala
scala> :type println("Hello world!")
Unit
```

Notice that the console doesn't execute our `println` statement in this expression. It simply compiles it and prints out its type, which in this case is something called `Unit`.

`Unit` is Scala's equivalent of `void` from Java and C. Read Chapter 1 to find out more.

## Setting up Scala IDE

*Scala IDE* is a plugin that adds Scala language support to [Eclipse](http://eclipse.org). A complete version of Scala IDE with Eclipse is also available as a standalone bundle from [http://scala-ide.org](). This is the easiest way to install the software so we recommend you install the standalone bundle for this course.

Go to [http://scala-ide.org](http://scala-ide.org) now, click the **Get the Bundle** button, and follow the on-screen instructions to download Scala IDE for your operating system:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Main website](src/pages/getting-started/scala-ide-website.png)\


Once you have downloaded and uncompressed the bundle you should find an application called **Eclipse**. Launch this. You will be asked to choose a folder for your *workspace*:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Choose a workspace location](src/pages/getting-started/scala-ide-workspace-chooser.png)\


Accept the default location and you will see an empty main Eclipse window:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Empty workspace](src/pages/getting-started/scala-ide-empty-workspace.png)\


### Creating your First Application

Your *Eclipse workspace* is two things: a folder containing files and settings, and a main window where you will be doing most of your Scala programming. In your workspace you can find *projects* for each Scala application you work on.

Let's create a project for the book exercises now. Select the **File menu** and choose **New > Scala Project**:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Create a new Scala project](src/pages/getting-started/scala-ide-new-project.png)\


Enter a **Project name** of `essential-scala` and click **Finish**. The tree view on the left of your workspace should now contain an empty project:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Empty project](src/pages/getting-started/scala-ide-empty-project.png)\


A project is no good without code to run! Let's create our first simple Scala application - the obligatory *Hello World* app. Select the **File Menu** and choose **New > Scala Object**:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Create a Scala object](src/pages/getting-started/scala-ide-new-object.png)\


**Name** your object `HelloWorld` and click **Finish**. A new file called `HelloWorld.scala` should appear in the tree view on the left of the main window. Eclipse should open the new file in the main editor ready for you to start coding:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Single Scala file](src/pages/getting-started/scala-ide-single-file.png)\


The content of the file should read as follows:

```tut:book:silent
object HelloWorld {

}
```

Replace this text with the following minimalist application:

```tut:book:silent
object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
  }
}
```

Select the **Run Menu** and choose **Run**. This should execute the code in your application, resulting in the words `Hello world!` appearing in the *Console* pane at the bottom of the window. Congratulations - you just ran your first Scala application!

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Hello World](src/pages/getting-started/scala-ide-hello-world.png)\


Developers with Java experience will notice the resemblance of the code above to the Java hello world app:

```java
public class HelloWorld {
  public static void main(String[] args) {
    System.out.println("Hello world!");
  }
}
```

The resemblance is, of course, no coincidence. These two applications compile to more or less the same bytecode and have exactly the same semantics. We will learn much more about the similarities and differences between Scala and Java as the course continues.

### Creating your First Worksheet

Compiling and running code whenever you make a change is a time consuming process that isn't particularly suitable to a learning environment.

Fortunately, Scala IDE allows us to create special files called *Scala Worksheets* that are specifically designed for training and experimentation. Every time you save a Worksheet, Eclipse automatically compiles and runs your code and displays the output on the right-hand side of your editor. This provides instant feedback, which is exactly what we need when investigating new concepts!

Create your first Scala Worksheet by selecting the **File Menu** and choosing **New > Scala Worksheet**:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: New Scala worksheet](src/pages/getting-started/scala-ide-new-worksheet.png)\


Enter a **Worksheet name** of `FirstSteps` and click **Finish**. A new file called `FirstSteps.sc` should appear in the tree view on the left of the main window, and should open it in the main editor in the middle:

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Empty Scala worksheet](src/pages/getting-started/scala-ide-empty-worksheet.png)\


Note that the object on the left contains a single line of Scala code:

```tut:book:silent
println("Welcome to the Scala worksheet")
```

for which Eclipse is displaying the corresponding output on the right:

```
Welcome to the Scala worksheet
```

Any expression you add to the left of the editor is evaluated and printed on the right. To demonstrate this, change the text in the editor to the following:

```scala
object FirstSteps {
  println("Welcome to the Scala worksheet")

  1 + 1

  if(20 > 10) "left" else "right"

  println("The ultimate answer is " + 42)
}
```

Save your work by selecting the **File Menu** and choosing **Save** (or better still by pressing **Ctrl+S**). Eclipse should automatically evaluate each line of code and print the results on the right of the editor:

```scala
object FirstSteps {
  println("Welcome to the Scala worksheet")   //> Welcome to the Scala worksheet

  1 + 1                                       //> res0: Int(2) = 2

  if(20 > 10) "left" else "right"             //> res1: String = left

  println("The ultimate answer is " + 42)     //> The ultimate answer is 42
}
```

<!-- Trailing slash and double newline are REQUIRED to prevent LaTeX repositioning this -->
![Scala IDE: Completed Scala worksheet](src/pages/getting-started/scala-ide-completed-worksheet.png)\


We'll dive into what all of the text on the right means as we proceed with the course ahead. For now you're all set to start honing your Scala skills!
