# Dynamic invocation in ZeroC Ice

## Dependencies

- Java 18
- C++11
- ZeroC Ice

## Directories explanation

### client

Client directory contains **IceClient.java** file which is Java program that acts as a client for a remote object that
provides a calculator service. It uses the ZeroC Ice middleware to communicate with the remote object.

When run, the program initializes a communication object with the specified command-line arguments. It then reads user
input from the command line and sends it to the remote object to perform the corresponding calculation. The user can
choose to *add*, *subtract*, or *multiply* two or more numbers by entering the corresponding command. For each command
entered by the user, the program creates an output stream to serialize the input parameters and send them to the remote
object using the ice_invoke method. The result returned by the remote object is then deserialized and printed to the
console.

### slice

Inside slice directory there is **calculator.ice** file which is an Ice Slice definition file that defines a module
named "Math" containing an interface named "Calculator".

The "Math" module defines a struct named "ThreeNumbers" that consists of three numeric fields - a short integer, an
integer, and a long integer.

The **Calculator** interface defines three methods:

- "add" method takes two integers as input parameters and returns their sum as a long integer.
- "subtract" method takes two integers as input parameters and returns their difference as a long integer.
- "multiply" method takes an object of type "ThreeNumbers" and a float value as input parameters and returns their
  product as a float value.

### server

Server directory contains **server.cpp** file. It is a C++ program that uses the Ice middleware to create a server that
implements the Calculator interface. The server program implements all three *calculator.ice* methods and prints out the
parameters and results to the console.

The main function creates an Ice communicator and an object adapter, which is used to register the CalculatorI object.
The server is given a name, "new_calc", which can be used by clients to connect to it. The object adapter is activated,
which starts listening for incoming client requests. The program then enters an event processing loop, waiting for
incoming requests to be processed.

### Running the whole project

To run the whole project, you have to follow these steps:

1. Generate necessary files for server and then run it:

```bash
slice2cpp --output-dir src/sr/ice/server/ slice/calculator.ice
cd src/sr/ice/server/
c++ -I. -c calculator.cpp server.cpp
c++ -o server calculator.o server.o -lIce
./server
```

2. Run client:

Ideally run client in **InteliJ IDEA** by clicking on the green arrow next to the main method in the Client class.

#### Credits

This project was created by Szymon Budziak.