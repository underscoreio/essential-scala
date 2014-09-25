---
layout: page
title: Implicit Parameters
---

We've seen the basics of the type class pattern. Now let's look at how we can make it easier to use. Recall our starting point is a trait `HtmlWriter` which allows us to implement HTML rendering for classes without requiring access to their source code, and allows us to render the same class in different ways.

~~~ scala
trait HtmlWriter[T] {
  def write(in: T): String
}

object PersonWriter extends HtmlWriter[Person] {
  def write(person: Person) = s"<span>${person.name} &lt;${person.email}&gt;</span>"
}
~~~

This issue with this code is that we need manage a lot of `HtmlWriter` instances when we render any complex data. Most classes will only have a single instance of `HtmlWriter` but we still need to call the correct object whenever we want to render a particular class. It would be nice it Scala would just pick the right instance for us whenever there is no ambiguity, and that's exactly what we can get Scala to do using **implicit parameters**.

## Implicit Parameter Lists

Here is an example of an implicit parameter list:

~~~ scala
object HtmlUtil {
  def htmlify[T](data: T)(implicit writer: HtmlWriter[T]): String = {
    writer.write(data)
  }
}
~~~

The `htmlify` method accepts two arguments: some `data` to convert to HTML and a `writer` to do the conversion. The `writer` is an implicit parameter.

The `implicit` keyword applies to the *whole parameter list*, not just an individual parameter. This makes the parameter list optional -- when we call `HtmlUtil.htmlify` we can either specify the list as normal:

~~~ scala
scala> HtmlUtil.htmlify(Person("John", "john@example.com"))(PersonWriter)
res2: String = <span>John &lt;john@example.com&gt;</span>
~~~

or we can omit the implicit parameter list. The compiler searches for **implicit values** of the correct type it can use to fill in the missing arguments. For example we can declare an implicit value like so:

~~~ scala
implicit object ApproximationWriter extends HtmlWriter[Int] {
  def write(in: Int): String =
    s"It's definitely less than ${((in / 10) + 1) * 10}"
}
~~~

When we use `HtmlUtil` we don't have tp specify the implicit value if one can be found.

~~~ scala
scala> HtmlUtil.htmlify(2)
res4: String = It's definitely less than 10
~~~
