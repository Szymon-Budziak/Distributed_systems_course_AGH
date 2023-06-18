# ZooKeeper application

## Dependencies

- Java 18
- ZooKeeper >= 3.8.0 (Java library)
- slf4j >= 1.7.30 (Java library)
- javafx (Java library)
- docker-compose

## Files explanation

### DataMonitor

It implements **AsyncCallback.StatCallback** and **AsyncCallback.Children2Callback** interfaces. It uses the `Apache
ZooKeeper` library to monitor changes in a specified node (`z` node) in a distributed system. The class starts watching
for changes by calling the **startWatch()** method. that initiates the monitoring process. The method first checks if
the znode exists and subscribes to its children's changes using the **subscribeChildrenAndGetCount()** method.

The class also overrides the **processResult()** methods from the callback interfaces to handle events and updates
related to the znode and its children. When the children of the znode change, the **processResult()** method calculates
the new count of children and prints a message indicating the change. If the znode is added or deleted, it opens or
closes a graphical user interface (GUI) respectively. The GUI displays information about the number of children and
provides a button to display the tree structure of the children.

The **displayChildrenTree()** method retrieves and displays the children of the znode in a tree-like structure within
the GUI. The **printChildren()** method recursively prints the children and their nested children, updating the GUI
accordingly. The **clearChildLabels()** method clears the child labels displayed in the GUI.

If there are any changes detected by the DataMonitor, it prints them to the console and updates the GUI accordingly.

### Executor

This class implements the **Watcher** interface from the `Apache ZooKeeper` library. It sets up a connection to the
ZooKeeper cluster. It initializes a ZooKeeper client with a given connection string and establishes a Watcher to track
changes to the znode. The Executor class is responsible for creating a DataMonitor and starting its watch. If the
initialization is successful, it enters into an infinite loop, waiting for events to be processed.

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

2. Start clients using ZooKeeper CLI:

```shell
zkCli.sh -server localhost:2181
```

```shell
zkCli.sh -server localhost:2182
```

3. Run `main` function of Executor class. Before that pass in IntelliJ `localhost:2181` to Executor configuration.

## Runtime

- In **Client 1** type: `create /z` (GUI window should pop up)
- In **Client 1** type: `create /z/z1` `create /z/z1/z11` `create /z/z2`
- Notice how in GUI the `Current number of children` is changing while adding new children
- In **Client 2** type: `ls /z` `ls /z/z1` `ls /z/z2`
- Click `Display children tree` button and notice that displayed number of children is the same as the sum of children
  from the previous command
- In **Client 1** type: `deleteall /z` to close the GUI

#### Credits

This project was created by Szymon Budziak.