# Type Classes

**Type classes** are a powerful feature of Scala that allow us to extend existing libraries with new functionality, without traditional inheritance, and without having access to original library source code. In this chapter we will learn how to use and implement type classes, using a Scala feature called **implicits**.

In the [section on traits](../traits/working-with-data.html) we compared object oriented and functional style in terms of extensibility, using this table.

+--------+-------------------------+-------------------------+
|        | Add new method          | Add new data            |
+========+=========================+=========================+
| **OO** | Change existing code    | Existing code unchanged |
+--------+-------------------------+-------------------------+
| **FP** | Existing code unchanged | Change existing code    |
+--------+-------------------------+-------------------------+


Type classes give us a third implementation technique which is more flexible than either. A type class is like a trait, defining an interface. However, with type classes we can:

- plug in different implementations of an interface for a given class; and
- implement an interface without modifying existing code.

It's difficult to understand these concepts without an example. We'll start this section by exploring how we can use type classes. We'll then turn to implementing them ourselves. We'll finish with a discussion of best practices.
