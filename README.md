# Overview
This is a project to bring the [j2mod](https://sourceforge.net/projects/j2mod/) library up to date.

j2mod has been actively maintained by [Julie Haugh](https://sourceforge.net/u/jfhaugh/) after forking it from [jamod](http://jamod.sourceforge.net/) and she has done 
absolutely sterling work in trying to iron out the myriad of wrinkles in this piece of work. It's about time she had some help so this 
project will endeavour to bring more collaboration and expertise to the fray.

The main driver for doing this work is to get away from the RxTxComm library and to use something actively supported and that brings its own native implementations.

The weapon of choice is the [jSerialComm](http://fazecast.github.io/jSerialComm/) library which is extremely well supported by Will Hedgecock and is actively developed.

The other goal of this project is to bring the codebase into line with JDK 1.6 and to fix all the known bugs.

# Releases
Stable releases can be downloaded here 

http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22j2mod%22

Snapshot releases can be downloaded here 

https://oss.sonatype.org/content/repositories/snapshots/com/ghgande/j2mod

# Roadmap

Not very much to add to the system but here are a couple of possibles;

* Register aggregator - at 4NG we have created an extended Transaction wrapper that consolidates multiple single register requests into a single, 
multi-register request by working out the 'distance' between registers and creating larger requests to reduce the comms overhead of multiple requests
* Handling ASCII payloads - some industrial Modbus slaves use an ASCII payload to represent their data i.e. a sequence of registers represent a floating point number in ASCII

# Known Issues

* The jSerialComm has some oddities with regards to Thread.sleep() calls that slowdown operation of the comms port. Every port open call incurs a 500ms delay and every settings change (baud rate, stop bit etc.) to an open port incurs a 200ms delay
* There are no unit tests for the RTU over TCP transport