Interface Maven Plugin
======================

Interface Maven Plugin detects public Java classes which reference unintended
external types.  For example, a method may return a Guava `ImmutableMap` instead
of a `Map`, or have a Guava `HashCode` parameter instead of a `byte[]`.  Uses of
these types can unnecessarily tie callers to unwanted third-party libraries.

TODO
----

* inner classes
* ignore classes marked as Beta?
* how to ignore classes visible for subpackage sharing?
