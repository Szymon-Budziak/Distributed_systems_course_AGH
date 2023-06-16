# ZooKeeper application

## Dependencies

- ZooKeeper >= 3.7.0
- Java 18
- docker-compose

## Files explanation

### DataMonitor

It implements **AsyncCallback.StatCallback** and **AsyncCallback.Children2Callback** interfaces. It uses the `Apache
ZooKeeper` library to monitor changes in a specified file in a distributed system. The class starts watching for changes
by calling the **startWatch()** method, which sets up a watch on the specified file in the ZooKeeper cluster. It also
subscribes to changes in the children of the file. When changes occur, the **processResult()** methods are called.

The first method is invoked when the status of the watched file changes, and the second method is called when the
children of the file change. These methods perform actions based on the changes detected.

If the number of children changes, the program prints a message indicating the new count. If a new child is added, it
starts a program specified by the exec parameter using ProcessBuilder. If a child is deleted, it stops the program.

### Executor

This class implements the **Watcher** interface from the `Apache ZooKeeper` library. It sets up a connection to the
ZooKeeper cluster and creates an instance of the DataMonitor class to monitor a specified file. The Executor class has a
main method that takes command-line arguments, including the ZooKeeper connection string, the name of the program to
execute, and optional arguments. It initializes an Executor instance with the provided arguments and starts listening
for user input.

When the user presses any key, the **listChildren()** method is called, which retrieves and prints the list of children
nodes under the specified file in the ZooKeeper cluster.

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

3. Run `main` function of Executor class. Before that pass in IntelliJ `localhost:2181 ./script.sh` to Executor
   configuration.

## Runtime

- In **Client 1** type: `create /z`
- In **Client 2** type: `ls /`
- Check if **script.sh** is running and logging: `tail -f logfile.txt`
- In **Client 1** type: `create /z/z1` `create /z/z1/z11`
- In **Executor app** `press any key` to see the tree of z
- In **Client 1** type: `deleteall /z` to stop the program

#### Credits

This project was created by Szymon Budziak.