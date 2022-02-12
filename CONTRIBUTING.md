# Contributing

All contributions are very welcome!

You can contribute simply by asking a question or by suggesting a change/fix without having to commit code, but feel 
free to contribute code via the Pull Request mechanism.

## Contributing Code

There are 2 branches `development` and `master`.  

Your code changes should be cloned from the `development` branch, this is the branch that the SNAPSHOT releases are built from and 
from which the online GitHub pages are sourced.

The `master` branch is reserved for production releases to the Maven Central repo. All changes in the `development` branch are 
merged into a single commit each time a production release is done.

## Guidelines

For the best chance of getting your changes incorporated into the code base, please follow these guidelines;

* Follow the coding style already established - same indentation, line spacing, naming convention etc.
* ALWAYS provide JUnit tests for your changes - copy existing ones, modify the current versions but there must be supporting unit tests
* Thoroughly comment your code and provide proper JavaDoc headers, even for private methods

