# Overview
This project is a fork of the [j2mod](https://sourceforge.net/projects/j2mod/) library which began life as [jamod](http://jamod.sourceforge.net/). 
A huge amount of refactoring and code fixing has been carried out on this library, with the addition of supporting JUnit tests, to ensure the library is fit for production use.

This implementation supports Modbus TCP, UDP, RTU over TCP, Serial RTU and Serial ASCII in both Master and Slave configurations.
The serial comms is implemented using [jSerialComm](http://fazecast.github.io/jSerialComm/) and does not require any outside dependencies over and above the logging facade [slf4j](https://www.slf4j.org/).

For instructions on how to use the library, visit the wiki [here](https://github.com/steveohara/j2mod/wiki) 

# Releases
Stable releases can be downloaded here 

http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22j2mod%22

Snapshot releases can be downloaded here 

https://oss.sonatype.org/content/repositories/snapshots/com/ghgande/j2mod

# Known Issues

* There are no unit tests for the RTU over TCP transport
* There is no way of adding `AbstractSerialTransportListener` to a `ModbusSlave` which means you cannot get informed of when the library is switching between send and receive
* A refactor is overdue to hide package components to encourage best practise usage patterns

# Dependencies

* [jSerialComm](http://fazecast.github.io/jSerialComm/)
The serial comms is handled by JSerialComm that includs native implementations for most platforms.
* [slf4j](https://www.slf4j.org/)
Logging facade to fit in with your application logging framework

# Including j2mod

    <dependency>
        <groupId>com.ghgande</groupId>
        <artifactId>j2mod</artifactId>
        <version>LATEST</version>
    </dependency>