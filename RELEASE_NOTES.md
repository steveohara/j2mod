## Version 2.0-rc1
* RxTxComm has been successfully replaced with jSerialComm and tested with RTU
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
* Added ability to specify timeout for Facade classes 'ModbusTCPMaster', 'ModbusUDPMaster' and 'ModbusSerialMaster'
* Fixed all the Unit ID checking when running as a Slave (myriad of problems - cannot see how it has ever worked)

## Version 2.0-rc3
* Fixed the timeouts for UDP/TCP and Serial listeners
* Added more test cases

## Version 2.0-rc4
* Fixed an issue with the TCPTransaction class not initialising the Transport correctly
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
* Facilitate multiple processimages in ModbusCoupler. #11 - thanks martentamerius
* WriteMultipleRegisters (Function Code : 0x10) does not work. #14 - chnaged use of signed shorts to unsigned
* Serial/RTU slower on 2.x #16 - worked around an issue with jSerialComm where setting timeouts on an open port injected a 200ms delay

## Version 2.1.1
* Corrected a problem with applying the timeouts when using the facade pattern

## Version 2.1.2
* Retrieve the localPort from opened socket if port is set to 0 #19
* CR characters lost on ModbusSerialTransport #18
* Corrected timeout setting issues on serial ports and harmonised setting timeout with IP methods