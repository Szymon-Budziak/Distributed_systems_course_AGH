# Chat Application

This is a simple chat application that allows clients to connect to a server and send messages to each other.

## Prerequisites

- Python 3.x

## Getting Started

1. Clone the repository
2. Run the server by executing python server.py in the command line.
3. Run a client by executing python client.py in a new command line window (you can run multiple clients).

## How to Use

- When running a client, enter a name to identify yourself in the chat.
- To send a message to all connected clients, simply type the message and hit enter.
- To send a multicast message to all connected clients, type "M " followed by your message.
- To send a UDP message to all connected clients, type "U " followed by your message.
- To quit the chat, type "quit" and hit enter.

### Files

- **server.py**

  This file contains the server-side code. It listens for incoming connections from clients and handles messages
  received from them.

- **client.py**

  This file contains the client-side code. It allows the user to connect to the server and send messages to other
  connected clients.

#### Credits

This project was created by Szymon Budziak.