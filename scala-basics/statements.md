---
layout: page
---

# Statements

Statements are the final type of program component, and basically cover anything that isn't an expression or a definition. The most important kinds are assignment and package statements.

## Assignment

We've already seen assignment statements when we introduced `var`s. Here they are again.

{% highlight scala %}
scala> var theVar = 1
theVar: Int = 1

scala> theVar = 2
theVar: Int = 2
{% endhighlight %}

## Package and Imports

Packages are much like Java's packages, but there are a few critical differences.

### Imports

Let's start by looking at import statements. We can import all the members of a package, using the mighty underscore instead of the star.

{% highlight scala %}
scala> import scala.math._
import scala.math._
{% endhighlight %}

We can also import just one or a few members.

{% highlight scala %}
scala> import scala.math.sin
import scala.math.sin

scala> import scala.math.{sin, cos, tan}
import scala.math.{sin, cos, tan}
{% endhighlight %}

We can rename imports as well.

{% highlight scala %}
scala> import scala.math.{sin => Sine, cos => Cosine, tan => Tangent}
import scala.math.{sin=>Sine, cos=>Cosine, tan=>Tangent}

scala> Sine(0.0)
res3: Double = 0.0
{% endhighlight %}

We can also import just a package, and refer to members of that package using dot notation.

{% highlight scala %}
scala> import scala.math
import scala.math

scala> math.sin(1.0)
res4: Double = 0.8414709848078965
{% endhighlight %}

Note that when we import just a package it stays around in the namespace, and we can later import members of that package without specifying the full path to that package.

{% highlight scala %}
scala> import math.sin
import math.sin

scala> sin(1.0)
res6: Double = 0.8414709848078965
{% endhighlight %}

This is a fantastic way of making code unreadable, as it obscures where imports come from. Even better, we can end up aliasing other packages.

{% highlight scala %}
scala> import java.awt.event
import java.awt.event

scala> import javax.imageio.event
import javax.imageio.event

scala> import event.ActionEvent // Which event package are we importing from?
<console>:29: error: ActionEvent is not a member of event
       import event.ActionEvent // Which event package are we importing from?
              ^
{% endhighlight %}

We can get around this aliasing by specifying a complete path to a package, using the `_root_` specifier if necessary.

{% highlight scala %}
scala> import _root_.java.awt.event.ActionEvent
import _root_.java.awt.event.ActionEvent
{% endhighlight %}

Note in the example above we didn't need to specify `_root_`. However it is easy to get into a situation where we do need to. If we follow Java naming convention and our package root is, say, `uk.org` and we want to import, say, `org.apache.commons` then we must fully qualify the import to let Scala know we aren't looking for `apache.commons` within our `uk.org` package.

### Packages

Package are similar to Java but again there are some wrinkles. Note that we can't define packages at the REPL! You'll have to compile this code to try it out.

We define packages in the usual way.

{% highlight scala %}
package foo

// Code goes here. In scope of package foo.
{% endhighlight %}

We can also nest packages like in Java.

{% highlight scala %}
package foo.bar

// Code goes here. In scope of package foo.bar
{% endhighlight %}

There is another form of nesting which has different semantics.

{% highlight scala %}
package foo
package bar

// Code goes here. In scope of package foo.bar, and foo._ is imported
{% endhighlight %}

In this form everything inside package `foo` is visible to our code, as well as everyting in `foo.bar`. This is sometimes convenient and sometimes confusing. Use whichever you feel most comfortable with.
