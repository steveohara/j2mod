## Version 2.0-rc1
* `RxTxComm` has been successfully replaced with `jSerialComm` and tested with RTU
* The whole codebase has been re-vamped to use modern constructs and practises, not a single class has remained untouched
* Codebase is compatible with JDK 1.6
* Javadoc has been extensively improved and fixed
* A logging framework (log4j) has been included with a wrapper to improve performance and allow token passing
* We have some tests!! Unit tests have been added for all automation friendly protocols (UDP and TCP)
* Unit tests have been added that independently verify the j2mod TCP Slave functionality
* Threading and concurrency has been hugely improved - removed synchronisation errors and corrected the handling of thread shutdown
* Brought some sanity to the Facade package so that it is now usable in a production setting
* Removed the serial BIN transport - it was proprietary and not taken up by any hardware vendors that I could find
* Fixed the UDP transport for both Master and Slave so that it is some way approaching robust
* Fixed the code layout - everything has headers, the layout is consistent, variable names and scope make sense etc.
* Build system incorporates GPG signing and is compatible with one-click deployment to Maven Central through Sonatype

## Version 2.0-rc2
* Added ability to specify timeout for Facade classes `ModbusTCPMaster`, `ModbusUDPMaster` and `ModbusSerialMaster`
* Fixed all the Unit ID checking when running as a Slave (myriad of problems - cannot see how it has ever worked)

## Version 2.0-rc3
* Fixed the timeouts for UDP/TCP and Serial listeners
* Added more test cases

## Version 2.0-rc4
* Fixed an issue with the `TCPTransaction` class not initialising the Transport correctly
* Removed all redundant casts
* Made Transport available from Facade classes so that listeners can be added for Serial events

## Version 2.0-rc5
* Serial timeouts were being applied to a null transport

## Version 2.0
* Replaced log4j with slf4j

## Version 2.1
* Corrections to ASCII transport - thanks transparentink
* Added fix for infinite listening loop - thanks martentamerius
* Removed duplicate request handling code
* Fix unit tests on linux #9 - thanks martentamerius
* Improved logging of errors and warnings with stack trace #10 - thanks martentamerius
* Facilitate multiple processimages in `ModbusCoupler`. #11 - thanks martentamerius
* WriteMultipleRegisters (Function Code : 0x10) does not work. #14 - changed use of signed shorts to unsigned
* Serial/RTU slower on 2.x #16 - worked around an issue with `jSerialComm` where setting timeouts on an open port injected a 200ms delay

## Version 2.1.1
* Corrected a problem with applying the timeouts when using the facade pattern

## Version 2.1.2
* Retrieve the localPort from opened socket if port is set to 0 #19
* CR characters lost on `ModbusSerialTransport` #18
* Corrected timeout setting issues on serial ports and harmonised setting timeout with IP methods

## Version 2.1.3
* Added support for Modbus RTU over TCP #21 - thanks axuan25
* Code requires guards around a number of the functions #22

## Version 2.2.0
* Modbus Slave Multiple Port Support #23
* Set `ModbusCoupler` to deprecated in favour of the new `ModbusSlaveFactory` and `ModbusSlave` sub-system
* Fixed some javadoc errors - thanks jan
* Timeout error on socket listener - thanks jan
* Now allows for slaves to share same socket but different protocols

## Version 2.2.1
* Amend `TCPMasterConnection` socket timeout handling #27 - thanks David

## Version 2.2.2
* Fixed NPE in close method of `SerialConnection` if Comms port was never successfully opened

## Version 2.3.0
* Adjusted write timers with fiddle factors to allow for idiosyncratic hardware
* `jSerialComm` library decoupled from implementation #28 - thanks Felipe

## Version 2.3.1
* Length of `ReadWriteMultipleResponse` indicates 1 byte too much? #31 - thanks elasticoder
* Function code 0x17 first performs a read and then the write. #32 - thanks elasticoder
* Double socket creation in `ModbusTCP`. #33 - thanks stoorm5

## Version 2.3.2
* Increased maximum queue length for incoming connections. #38 - thanks martentamerius
* Incorrect javadoc for `ModbusSlaveFactory` #35 - thanks bertrik
* Adds setRetries and `setCheckingValidity` services into `ModbusTCPMaster` #30 - thanks ericauguie
* Added RTU over TCP #43 - thanks eli-mcgowan
* Fixed setting timeout of `TCPMaster` does not get applied to transactions #41
* Reduce an NPE - Serial port connection #40
* Fixed issue with serial write bytes delay if data bits not specified

## Version 2.3.3
* Specify IP address in `ModbusSlaveFactory` #46 - thanks MindVark
* Added `getConnection()` to `AbstractSerialConnection` - thanks nnadeau
* Reading full stream instead of parts - thanks liebehentze

## Version 2.3.4
* Fixed NullpointException at handleRequest in `AbstractModbusListener` #47
* Add checks to see if serial port is available during connect #45

## Version 2.3.5
* Fixed a typo in the failure debug message
* Fixed the omission of the bit count when reading a `DiscreteInput` request

## Version 2.3.6
* Debug logger messages causing garbage collections even when DEBUG mode is not enabled #53
* RS485 echo in RTU mode #51 - thanks david

## Version 2.3.7
* Fixed a format error in the thrown connection retry catch block
* Propagate `ModbusTCPListener` timeout to the `TCPSlaveConnection` #59 - thanks javi
* Not possible to override `handleRequest` in `ModbusSlave` #57 - thanks javi
* Extending `ModbusSlave` exceptions list

## Version 2.3.8
* Added a sleep timer to the TCP/UDP/serial transaction retry loops
* Added the timeout value to the serial write
* Responded to vulnerabilities highlighted by FindBugs

## Version 2.3.9
* Critical regression introduced in previous release whereby the sharing of stream buffers was being trampled over
* Serial CRC was not correctly checking both bytes

## Version 2.4.0
* The transport RTU over TCP was not being set correctly after a comms failure
* The Slave interface was not always correctly assigning the headers (Transaction ID, Function Code. Unit ID) for all types of requests
* Fully tested the RTU over TCP transport with some real hardware

## Version 2.4.1
* Fixed some error messages
* Added more logging and made the TCP transaction a little more robust

## Version 2.4.2
* Fixed a critical issue with RTU over TCP when the connection is set to re-connecting. 
Initial connection correctly uses `ModbusRTUTCPTransport` but subsequent connections use `ModbusTCPTransport` - RTU Over TCP Resend Bug #71

## Version 2.5.0
* Upgraded to the latest version of `JSerialComm` - this fixes issues with closing Windows COM ports and many other problems
It also allows us to manage the built-in latency timeouts within `JSerialComm`
* Add unit tests for the serial implementation of RTU and ASCII (Windows only)
* Made the closing and shutdown of a Serial Listener more robust
* Fixed an issue whereby if a Serial Slave was created using the `ModbusSlaveFactory` is closed independently, and the then the slave
is reused with different serial parameters, the old parameters would have been used instead
* Removed all JavaDoc compilation warnings 

## Version 2.5.1
##### _Note:- v2.5.1 is not backwards compatible with v2.5.0_
Some changes may be required for applications using this version. The changes bellow detail where a non-compatible change has been made.
* Removed the deprecated `ModbusCoupler` implementation - users should switch their code to use `SlaveFactory` and apply their `ProcessImage` instances to the slave directly
The new implementation means that `ProcessImage` is not coupled to a slave and can be shared across slaves and unit IDs
_(**NOT BACKWARDS COMPATIBLE**)_
* Added a method `getError` to `ModbusSlave` to enable users to get any startup errors after a slave is opened #72
* Fixed the issue whereby serial slaves will respond with error messages for requests that are targeted for them #62
* Updated the documentation and byline
* Moved the useRtuOverTcp flag to constructor #75
_(**NOT BACKWARDS COMPATIBLE**)_
* Fixed getConnection.isOpen() still true after serial-port removal #74
* Implemented inter-frame delays for serial transactions to match the Modbus specification #62
* Fixed - Why doesn't DigitalOut extend DigitalIn #86
* Made library an OSGi bundle #67 thanks amitjoy
* Removed all `final` qualifiers where it prevents extensibility and doesn't add any value #85
* Added default thread names to listeners and methods to get/set it on the slave #80
* Fixed Error in ReadInputDiscretesRequest class SetBitCount() method. Can't access the last address. #87

## Version 2.5.2
* Removed a lot of synchronized decorated methods to put the onus on the caller and speed up processing. #88 

## Version 2.5.3
* Added check of serial response to make sure it is from the same unit and function code as the request
* Return the new value of the register after writing to it (#91)
* Fix data length in MaskWriteRegisterResponse. Add facade methods for Mask Write Response function. Add unit tests. (#92)
* Upgraded jserialcomm to v2.3.0

## Version 2.5.4
* Serial slave not reading response from other slave #76
* Upgraded jserialcomm to v2.4.1

## Version 2.5.5
* Upgraded jserialcomm to v2.5.1

## Version 2.5.6
* Added a finally block to ensure that `afterMessageWrite` event handler is invoked, even if the message fails to write

## Version 2.5.7
* `ModbusTCPTransaction` Error message is not shown in a specific case #100

## Version 2.5.8
* Upgraded jserialcomm to v2.5.3
* ClassCastException when timeout?? #96 - made all Master facade methods thread safe
* Code cleanup to lower Sonar score

## Version 2.5.9
* Desensitised the serial writes to allow more time to send

## Version 2.6.0
* Sanitised the `setBaudRate` method to make it part of the serial parameters only
* Removed all the redundant test data for the old command test
* Made the default `SerialConnection` more resilient to the connection not yet being open
* Added retries to the serial port connection with a retry delay
* Fixed a serious synchronisation problem with the ope/close methods in the use of SerialPort
* Added a modpoll style command line interface to test serial connections

## Version 2.6.1
* Upgraded to jSerialComm 2.6.0

## Version 2.6.2
* Downgraded jSerialComm until [jSerialComm #277](https://github.com/Fazecast/jSerialComm/issues/277) is resolved
* Upgraded log4j to prevent security issue
* added ModPoll class to mirror ModPoll exe capabilities
* Created fat executable jar for testing ModPoll features
* **SHOULD NOT BE USED**

## Version 2.6.3
* Fix missing logging properties in assembly
* Fixed the release so that it isn't the fat jar that is released (2.6.2 error)
* **SHOULD NOT BE USED**

## Version 2.6.4
* Fixed the release so that it isn't the fat jar that is released (2.6.3 error)
* **SHOULD NOT BE USED**

## Version 2.6.5
* Upgraded to jSeralcomm 2.6.2 that contains fix for [jSerialComm #277](https://github.com/Fazecast/jSerialComm/issues/277)
* Added some code cleanups as suggested by Sonar
* Fixed issue where slave listeners (TCP/UDP/Serial) fail silently if they cannot bind to a port
* Bumped log4j version for testing
* Added `isConnected()` method for al master facade classes #103

## Version 2.7.0
* TCP Connection idle timeout #108 thankyou akochubey2004
* Changed tests to use 127.0.0.1 rather than localhost

## Version 3.0.0
* Fixed some stray documentation
* Add support for explicitly configuring RS-485 mode #117 thanks sirhcel
* Removed checks on readInputDiscretes #114 thanks kazuyatada
* Switched to scheduled executor service instead of timer #115 thanks mkurt

## Version 3.1.0
* Updated to latest version of jserialcomm and fixed log4j version 

## Version 3.1.1
* Add remaining RS-485 configuration parameters for Linux #118

## Version 3.2.0
* Move parts of DEBUG logging to TRACE #126
* Bump jSerialComm for fixed POSIX signal handling (#138)
* ModbusTCPMaster: Fix incorrect reconnect parameter documentation (#137)
* Added equals & hashcode method to ModbusTCPMaster & TCPMasterConnection (#127)
* modbus slave factory getSlave fix
* Bumped the version
