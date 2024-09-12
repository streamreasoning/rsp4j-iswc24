# Examples

This branch contains some implementation of stream processors created with the Polyflow APIs.
There are two main examples: [Relational](src/main/java/relational) and [Graph](src/main/java/graph), although [Document](src/main/java/document) and
[Custom](src/main/java/custom) are also available

The modules have a similar structure:

- content: implementation of the Content interface, used by the windows to store data points
- datatypes: custom data types used in the examples
- examples: the entry point of the examples, in which components are instantiated and linked together
- operatorsimpl: specific operators implementations used in the examples
- sds: SDS and Time Varying implementations
- stream: data stream generators used in the examples
