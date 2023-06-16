import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Executor implements Watcher {
    private ZooKeeper zk;
    private final String watchFileName;

    public Executor(String connectionString, String watchFileName, String[] exec) {
        this.watchFileName = watchFileName;

        try {
            this.zk = new ZooKeeper(connectionString, 5000, this);
            DataMonitor dataMonitor = new DataMonitor(this.zk, watchFileName, exec);
            dataMonitor.startWatch();
        } catch (IOException e) {
            System.out.println("Error when creating DataMonitor.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Wrong number of arguments. USAGE: Executor <connectionString> program [args ...]");
            System.exit(1);
        }
        String connectionString = args[0];
        String[] exec = new String[args.length - 1];
        String watchFileName = "/z";
        System.arraycopy(args, 1, exec, 0, exec.length);

        Executor executor = new Executor(connectionString, watchFileName, exec);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Executor ready. Press any key to see the list of nodes under z.");
        while (true) {
            try {
                br.readLine();
                executor.listChildren();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listChildren() {
        try {
            List<String> children = this.zk.getChildren(this.watchFileName, false);
            System.out.println("Listing children for: " + this.watchFileName);
            printChildren(children, this.watchFileName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            System.out.println("Node does not exist.");
        }
    }

    private void printChildren(List<String> children, String base) throws InterruptedException, KeeperException {
        for (String child : children) {
            String path = base + "/" + child;
            System.out.println(path);
            List<String> nestedChildren = this.zk.getChildren(path, false);
            printChildren(nestedChildren, path);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        //
    }
}