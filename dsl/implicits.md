---
layout: page
---

# Open Extension of Classes

Multiple parameter lists

implicits parameters

Example: RichInt

extending a class

implicit resolution order

Rules for implicits
[23/09/2012 20:08:33] Dave Gurnell: Marking Rule: Only definitions marked implicit are available.
[23/09/2012 20:08:39] Dave Gurnell: Scope Rule: An inserted implicit conversion must be in scope as a single identifier, or be associated with the source or target type of the conver- sion.
[23/09/2012 20:08:51] Dave Gurnell: Non-Ambiguity Rule: An implicit conversion is only inserted if there is no other possible conversion to insert.
[23/09/2012 20:08:57] Dave Gurnell: One-at-a-time Rule: Only one implicit is tried.
[23/09/2012 20:09:05] Dave Gurnell: Explicits-First Rule: Whenever code type checks as it is written, no implicits are attempted.

Must be in scope as a single identifier (i.e. not a.b)
[23/09/2012 20:12:02] Dave Gurnell: Except...
[23/09/2012 20:12:14] Dave Gurnell: The compiler will look for definitions in the companion object.
[23/09/2012 20:12:25] Dave Gurnell: ...
[23/09/2012 20:12:28] Dave Gurnell: sorry
[23/09/2012 20:12:43] Dave Gurnell: ...in the companion objects for the source and target types for conversion

explicit implicits

## Exercise

Extend `Int` with a method called `times` that executes the body `n` times, where `n` is the `Int`. Bonus marks for using a call-by-name parameter.




## Typeclasses

the typeclass pattern

Example: customised printing
