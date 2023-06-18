import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Executor implements Watcher {
    public Executor(String connectionString, String znode) {
        try {
            ZooKeeper zk = new ZooKeeper(connectionString, 5000, this);
            DataMonitor dataMonitor = new DataMonitor(zk, znode);
            dataMonitor.startWatch();
        } catch (IOException e) {
            System.out.println("Error when creating DataMonitor.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong number of arguments. USAGE: Executor <connectionString>.");
            System.exit(1);
        }
        String connectionString = args[0];
        String znode = "/z";

        new Executor(connectionString, znode);

        System.out.println("Executor ready. When `z` node will be created, new window will pop up. When `z` node will be deleted, teh window will be closed." +
                "The messages will be logged into a terminal.");
        while (true) {
            //
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        //
    }
}