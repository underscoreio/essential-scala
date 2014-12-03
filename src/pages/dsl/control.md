## Custom Control Structures

Consider implementing logging. Often we'll have a log statement like `logger.debug(anExpensiveOperation())` which we only want to execute if debug logging is enabled. In Java, we'd have to write something like

~~~ java
if(logger.isDebugEnabled() {
  logger.debug(anExpensiveOperation());
}
~~~

the reason being that we can't control the order of evaluation -- a function's arguments are always evaluated before the function is called. Scala allows us to delay evaluating a functions arguments, a feature known as call-by-name parameters.

### Syntax

We declare a call by name parameter by specifying it's type as `=> Result`. That is, like a function but without a parameter list. For example, here's a simple logger implementation using call-by-name parameters

~~~ scala
object Logger {
  var debugEnabled = true

  def debug(msg: => String): Unit =
    if(debugEnabled)
      println("DEBUG "+ msg)
    else
      ()
}

scala> Logger.debug("This is a debug message")
DEBUG This is a debug message

scala> Logger.debugEnabled = false
Logger.debugEnabled: Boolean = false

scala> Logger.debug("This is a debug message")

~~~

We can prove that the `msg` parameter is not being evaluated by wrapping a `println` expression in with it.

~~~ scala
scala> Logger.debug({ println("Is this thing on?"); "This is a debug message" })

scala> Logger.debugEnabled = true
Logger.debugEnabled: Boolean = true

scala> Logger.debug({ println("Is this thing on?"); "This is a debug message" })
Is this thing on?
DEBUG This is a debug message

~~~

Note that call-by-name parameters are evaluated every time they are invoked. If you're used to Haskell's call-by-need evaluation, where a parameter is evaluated once and the result stored for later use, this different may be trip you up.

### Exercises

It's time to get our Javascript on! Write a method or function `withTimeout` that invokes a call-by-name parameter after a certain time has passed. Use `Thread.sleep` to wait. You should be able to write code like

~~~ scala
withTimeout(1000, println("It's about time!"))
~~~

and `"It's about time"` will be printed after 1000 milliseconds.


Implement a ternary operator in Scala. It should be possible to write

~~~ scala
scala> ?(1 < 2) { "Numbers still work!" } { "Oh dear!" }
res13: java.lang.String = Numbers still work!

~~~

Hint: use multiple parameter lists to allow this syntax.
