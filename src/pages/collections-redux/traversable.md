## Traversable and Iterable

So far we've avoided discussing the finer details of the collection class hierarchy. As we near the end of this section it is time to quickly go over some of the intricacies.

### Traversable

The trait `Traversable` sits at the top of the collection hierarchy and represents a collection that allows traversal of its contents. The only abstract operation is `foreach`. Most of the collection methods are implemented in `Traversable` though classes extending it may reimplement methods for performance.

#### TraversableOnce

`TraversableOnce` represents a collection that can be traversed one or more times. It is primarily used to reduce code duplication between `Iterator`s and `Traversable`.

### Iterable

`Iterable` is the next trait below `Traversable`. It has a single abstract method `iterator` that should return an `Iterator` over the collection's contents. The `foreach` method is implemented in terms of this. It adds a few methods to `Traversable` that can only be efficiently implemented when an iterator is available.
