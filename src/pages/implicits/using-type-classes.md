## Using Type Classes

We have seen how to define type classes. In this section we'll see some conveniences for using them: *context bounds* and the *implicitly* method.

### Context Bounds

When we use type classes we often end up requiring implicit parameters that we pass onward to a type class interface. For example, using our `HtmlWriter` example we might want to define some kind of page template that accepts content rendered by a writer.

```tut:invisible
trait HtmlWriter[A] {
  def write(in: A): String
}
implicit class HtmlUtil[A](value: A) {
  def toHtml(implicit writer: HtmlWriter[A]) =
    writer write value
}
```

```tut:book:silent
def pageTemplate[A](body: A)(implicit writer: HtmlWriter[A]): String = {
  val renderedBody = body.toHtml

  s"<html><head>...</head><body>${renderedBody}</body></html>"
}
```

We don't explicitly use the implicit `writer` in our code, but we need it in scope so the compiler can insert it for the `toHtml` enrichment.

Context bounds allow us to write this more compactly, with a notation that is reminiscent of a type bound.

```tut:book:silent
def pageTemplate[A : HtmlWriter](body: A): String = {
  val renderedBody = body.toHtml

  s"<html><head>...</head><body>${renderedBody}</body></html>"
}
```

The context bound is the notation `[A : HtmlWriter]` and it expands into the equivalent implicit parameter list in the prior example.

<div class="callout callout-info">
#### Context Bound Syntax {-}

A context bound is an annotation on a generic type variable like so:

```scala
[A : Context]
```

It expands into a generic type parameter `[A]` along with an implicit parameter for a `Context[A]`.
</div>

### Implicitly

Context bounds give us a short-hand syntax for declaring implicit parameters, but since we don't have an explicit name for the parameter we cannot use it in our methods. Normally we use context bounds when we don't need explicit access to the implicit parameter, but rather just implicitly pass it on to some other method. However if we do need access for some reason we can use the `implicitly` method.

```tut:book:silent
case class Example(name: String)
implicit val implicitExample = Example("implicit")
```

```tut:book
implicitly[Example]

implicitly[Example] == implicitExample
```

The `implicitly` method takes no parameters but has a generic type parameter. It returns the implicit matching the given type, assuming there is no ambiguity.
