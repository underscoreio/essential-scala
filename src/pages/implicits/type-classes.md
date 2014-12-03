## Type Classes

We have now seen all the fundamental pieces of type classes. They are quite involved, so let's recap the steps needed to construct and use a type class.

<div class="callout callout-info">
#### Type Class Pattern

To implement a type class we must implement three things:

1. a trait, defining the interface we will support;
2. some implicit instances of the trait; and
3. an object with one or more methods using implicit parameters to select the appropriate instance.

The trait is *the* type class and always has a generic type parameter

~~~ scala
trait TypeClass[A] {
  declarationOrExpression ...
}
~~~

Instances implement the type class for a particular type.

~~~ scala
object AnInstance {
  implicit object theInstance extends TypeClass[TheClass] {
    ...
  }
}
~~~

The convenience interface provides methods using implicit parameters to require instances. Methods have a generic type that controls type class instance selection.

~~~ scala
object Interface {
  def method[A](implicit instance: TypeClass[A]) = ...
  ...
}
~~~
</div>

Now we have type classes down let's look in more detail at the implicit resolution rules and type class instance packaging.

#### Implicit Resolution Rules

Scala has three types of implicits---implicit classes, implicit values, and implicit conversions---of which we've only seen one. Each works in the same way---the compiler detects a type error in our code, locates a matching implicit, and applies it to fix the error. This is a powerful mechanism, but we need to control it very carefully to prevent the compiler changing our code in ways we don't expect. For this reason, there is a strict set of **implicit resolution rules** that we can use to dictate the compiler's behaviour:

 1. **Explicits first rule**---if the code already type checks, the compiler ignores implicits altogether;
 2. **Marking rule**---the compiler only uses definitions marked with the `implicit` keyword;
 3. **Scope rule**---the compiler only uses definitions that are *in scope* at the current location in the code (see below);
 4. **Non-ambiguity rule**---the compiler only applies an implicit if it is the only candidate available;
 5. **One-at-a-time rule**---the compiler never chains implicits together to fix type errors---doing so would drastically increase compile times;

Note that the name of the implicit doesn't come into play in this process.

#### Implicit Scope

The *scope rule* of implicit resolution uses a special set of scoping rules that allow us to package implicits in useful ways. These rules, collectively referred to as **implicit scope**, form a search path that the compiler uses to locate implicits:

 1. **Local scope**---First look locally for any identifier that is tagged as `implicit`. This must be a single identifier (i.e. `a`, not `a.b`), and can be defined locally or in the surrounding class, object, or trait, or `imported` from elsewhere.

 2. **Companion objects**---If an implicit cannot be found locally, the compiler looks in the companion objects of types involved in the type error. Will see more of this rule in the next section.

### Packaging Implicit Values

We are going to look at two methods for packaging our implicit value: in traits, and in companion objects.

#### Packaging in Traits

We've seen we can package implicits in an object that we then import into the scope where we need the implicits. A more sophisticed way of packaging an implicit value is to define it inside a trait called `SomethingImplicits` and extend that trait to create a singleton of the same name:

~~~ scala
trait EmailImplicits {
  implicit object EmailEqual extends Equal[Person] {
    def equal(v1: Person, v2: Person): Boolean =
      v1.email == v2.email
  }
}

object EmailImplicits extends EmailImplicits
~~~

This gives developers two convenient ways of using our code:

 1. quickly bring our implicit into scope via the singleton object using an `import`:

    ~~~ scala
    // `EmailEqual` is not in scope here

    def testMethod = {
      import EmailImplicits._

      // `EmailEqual` is in scope here

      Eq(Person("Noel", "noel@example.com"), Person("Dave", "noel@example.com"))
    }

    // `EmailEqual` is no longer in scope here
    ~~~

 2. stack our trait with a set of other traits to produce a library of implicits that can be brought into scope using inheritance or an `import`:

    ~~~ scala
    object AllTheImplicits extends EmailImplicits
      with MoreImplicits
      with YetMoreImplicits

    import AllTheImplicits._

    // `EmailEqual` is in scope here
    // along with other implicit classes
    ~~~

<div class="alert alert-info">
**Implicits tip:** Some Scala developers dislike implicits because they can be hard to debug. The reason for this is that an implicit definition at one point in our codebase can have an invisible affect on the meaning of a line of code written elsewhere.

While this is a valid criticism of implicits, the solution is not to abandon them altogether but to apply strict design principles to regulate their use. Here are some tips:

 1. Keep tight control over the scope of your implicits. Package them into traits and objects and only import them where you want to use them.

 2. Package all your implicits in traits/objects with names ending in `Implicits`. This makes them easy to find using a global search across your codebase.

 3. Only use implicits on specific types. Defining an implicit class on a general type like `Any` is more likely to cause problems than defining it on a specific type like `WebSiteVisitor`.
</div>

Let's redefine our adapters for `HtmlWriter` so we can bring them all into scope. Note that outside the Scala console implicit values are subject to the same packaging restrictions as implicit classes---they have to be defined inside another class, object, or trait. We'll use the packaging convention we discussed above:

~~~ scala
trait HtmlImplicits {
  implicit object PersonWriter extends HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"
  }

  implicit object DateWriter extends HtmlWriter[Date] {
    def write(in: Date) = s"<span>${in.toString}</span>"
  }
}

object HtmlImplicits extends HtmlImplicits
~~~

We can now use our adapters with `htmlify`:

~~~ scala
scala> import HtmlImplicits._
import HtmlImplicits._

scala> HtmlUtil.htmlify(Person("John", "john@example.com"))
res4: String = <span>John &lt;john@example.com&gt;</span>
~~~

This version of the code has much lighter syntax requirements than its predecessor. We have now assembled the complete type class pattern: `HtmlUtil` specifies our HTML rendering functionality, `HtmlWriter` and `HtmlWriters` implement the functionality as a set of adapters, and the implicit argument to `htmlify` implicitly selects the correct adapter for any given argument. However, we can take things one step further to really simplify things.

### Packaging in Companion Objects

We can package type classes in two ways: using the trait/singleton approach we introduced for implicit classes, or using the companion objects of the relevant types.

Remember that one of the locations in implicit scope is *the companion objects of types involved in the type error*. This is particularly relevant to type class instances---if the compiler is searching for a `HtmlWriter[Person]`, it will look in the companion objects of `HtmlWriter`, `Person`, and all of their superclasses:

We can use this behaviour to implement type class instances in the companion objects of the main data types in our application, as opposed to packaging them all into one big trait. For example:

~~~ scala
case class Person(name: String, email: String)

object Person {
  implicit val htmlWriter = new HtmlWriter[Person] {
    def write(person: Person) =
      s"<span>${person.name} &lt;${person.email}&gt;</span>"
  }
}
~~~

{% comment %}
The compiler searches for implicits in local scope *first* before it looks at companion objects. We can therefore override companion object implicits with explicit imports:

~~~ scala
val person = Person("Dave", "dave@example.com")

HtmlUtil.htmlify(person) // uses Person.htmlWriter

import HtmlImplicits._

HtmlUtil.htmlify(person) // uses HtmlImplicits.PersonWriter
~~~
{% endcomment %}

### Take Home Points

Like type enrichment, **type classes** are a general programming pattern. Type classes allow us to define a piece of functionality for a wide range of classes without modifying the source code for those classes.

The type class pattern consists of three things: a *type class*, a set of *type class instances*, and a method of associating the type class instance for any particular type.

The Scala implementation of type classes uses **implicit values** to define type class instances and **implicit parameter lists** as the method of instance resolution.

We can package type class instances together in traits/singletons or separately in the **companion objects** of individual types.

We can combine type classes with type enrichment to add new behaviour to existing types across a whole application.

### Exercises

These exercises involve serializing Scala data to JSON, which is one of the classic use cases for type classes. The typical process for converting data to JSON in Scala involves two steps. First we convert our data types to an intermediate case class representation, then we serialize the intermediate representation to a string.

Here is a suitable case class representation of a subset of the JSON language. We have a `sealed trait JsValue` that defines a `stringify` method, and a set of subtypes for two of the main JSON data types---objects and strings:

~~~ scala
sealed trait JsValue {
  def stringify: String
}

final case class JsObject(values: Map[String, JsValue]) extends JsValue {
  def stringify = values
    .map { case (name, value) => "\"" + name + "\":" + value.stringify }
    .mkString("{", ",", "}")
}

final case class JsString(value: String) extends JsValue {
  def stringify = "\"" + value.replaceAll("\\|\"", "\\\\$1") + "\""
}
~~~

We can construct JSON objects and serialize them as follows:

~~~ scala
scala> JsObject(Map("foo" -> JsString("a"), "bar" -> JsString("b"), "baz" -> JsString("c")))
res2: JsObject = JsObject(Map(a -> JsArray(List(JsString(a), JsString(b), JsString(c)))))
scala> JsObject(Map("foo" -> JsString("a"), "bar" -> JsString("b"), "baz" -> JsString("c")))
res4: JsObject = JsObject(Map(foo -> JsString(a), bar -> JsString(b), baz -> JsString(c)))

scala> res4.stringify
res5: String = {"foo":"a","bar":"b","baz":"c"}
~~~

#### Convert X to JSON

Let's create a type class for converting Scala data to JSON. Implement a `JsWriter` trait containing a single abstract method `write` that converts a value to a `JsValue`.

<div class="solution">
The *type class* is generic in a type `A`. The `write` method converts a value of type `A` to some kind of `JsValue`.

~~~ scala
trait JsWriter[A] {
  def write(value: A): JsValue
}
~~~
</div>

Now let's create the dispatch part of our type class. Write a `JsUtil` object containing a single method `toJson`. The method should accept a value of an arbitrary type `A` and convert it to JSON.

Tip: your method will have to accept an implicit `JsWriter` to do the actual conversion.

<div class="solution">
~~~ scala
object JsUtil {
  def toJson[A](value: A)(implicit writer: JsWriter[A]) =
    writer write value
}
~~~
</div>

Now, let's revisit our data types from the web site visitors example in the [Sealed traits](/objects/sealed-traits.html) section:

~~~ scala
import java.util.Date

sealed trait Visitor {
  def id: String
  def createdAt: Date
  def age: Long = new Date().getTime() - createdAt.getTime()
}

final case class Anonymous(
  val id: String,
  val createdAt: Date = new Date()
) extends Visitor

final case class User(
  val id: String,
  val email: String,
  val createdAt: Date = new Date()
) extends Visitor
~~~

Write `JsWriter` instances for `Anonymous` and `User`.

<div class="solution">
~~~ scala
implicit object AnonymousWriter extends JsWriter[Anonymous] {
  def write(value: Anonymous) = JsObject(Map(
    "id"           -> JsString(value.id),
    "createdAt"    -> JsString(value.createdAt.toString)
  ))
}

implicit object UserWriter extends JsWriter[User] {
  def write(value: User) = JsObject(Map(
    "id"           -> JsString(value.id),
    "email"        -> JsString(value.email),
    "createdAt"    -> JsString(value.createdAt.toString)
  ))
}
~~~
</div>

Given these two definitions we can implement a `JsWriter` for `Visitor` as follows. This uses a new type of pattern---`a: B`---which matches any value of type `B` and binds it to a variable `a`:

~~~ scala
implicit object VisitorWriter extends JsWriter[Visitor] {
  def write(value: Visitor) = value match {
    case anon: Anonymous => JsUtil.toJson(anon)
    case user: User      => JsUtil.toJson(user)
  }
}
~~~

Finally, verify that your code works by converting the following list of users to JSON:

~~~ scala
val visitors: Seq[Visitor] = Seq(Anonymous("001", new Date), User("003", "dave@xample.com", new Date))
~~~

<div class="solution">
~~~ scala
visitors.map(visitor => JsUtil.toJson(visitor))
~~~
</div>
