# ZooKeeper application

## Dependencies

- Java 18
- ZooKeeper >= 3.8.0 (Java library)
- slf4j >= 1.7.30 (Java library)
- javafx (Java library)
- docker-compose

## Files explanation

### Executor

This class implements the **Watcher** interface from the **Apache ZooKeeper** library. The Executor class sets up a
connection to the ZooKeeper service using a provided connection string and initializes a ZooKeeper object. It has a main
method that expects two command-line arguments: a connection string and the name of an external application. The
**Watcher** interface is implemented, and the process method is overridden to handle various types of events received
from ZooKeeper. The process method checks the type of event and the path associated with it and performs specific
actions based on the event type and path. The code tracks the creation and deletion of a specific znode ("/z") and its
children. It performs actions such as launching an external graphic application, opening a GUI window, and updating
labels and child nodes in the GUI based on the events. The code also includes methods for managing the external graphic
application and GUI, as well as for counting and displaying the tree of child nodes.

### docker-compose.yml

Inside the file there is configuration, running `docker-compose up` will create a ZooKeeper cluster with three
instances, each accessible on different ports on the host machine. The cluster is set up to handle leader election and
client connections using the specified ports and hostnames. The content of the file has been copied
from [ZooKeeper Docker](https://hub.docker.com/_/zookeeper)

## Running the whole project

1. Start servers:

```shell
docker-compose up
```

2. Start 3 clients using ZooKeeper CLI so that replicated ZooKeeper can be tested:

```shell
zkCli.sh -server localhost:2181
```

```shell
zkCli.sh -server localhost:2182
```

```shell
zkCli.sh -server localhost:2183
```

3. Run `main` function of Executor class. Before that pass in IntelliJ `localhost:2181 pinta` to Executor configuration.

## Runtime

- In **Client** type: `create /z` (external graphic application passed as argument should pop up)
- In **Client** type: `create /z/z1` (GUI window should pop up)
- In **Client** type: `create /z/z1/z11` `create /z/z2`
- Notice how in GUI the `Current number of children` is changing while adding new children
- Click `Display children tree` button to display children tree (notice that count of children in displayed children
  tree is the same as in `Current number of children`)
- In **Client** type: `deleteall /z` to close the external graphic application and GUI

#### Credits

This project was created by Szymon Budziak.