import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class DataMonitor implements AsyncCallback.StatCallback, AsyncCallback.Children2Callback {
    private final ZooKeeper zk;
    private final String watchFileName;
    private final String[] exec;
    private int lastSum = -1;
    private Process process = null;

    public DataMonitor(ZooKeeper zk, String watchFileName, String[] exec) {
        this.zk = zk;
        this.watchFileName = watchFileName;
        this.exec = exec;
    }

    public void startWatch() {
        zk.exists(this.watchFileName, true, this, null);
        this.subscribeChildrenAndGetCount(this.watchFileName);
    }

    private int subscribeChildrenAndGetCount(String childrenName) {
        this.zk.getChildren(childrenName, true, this, null);

        int sum = 1;
        try {
            List<String> children = this.zk.getChildren(childrenName, false);
            for (String child : children) {
                sum += this.subscribeChildrenAndGetCount(childrenName + "/" + child);
            }
        } catch (KeeperException.NoNodeException e) {
            //
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }

    @Override
    public void processResult(int rc, String path, Object ctx, List<String> list, Stat stat) {
        int sum = this.subscribeChildrenAndGetCount(this.watchFileName);
        if (sum != this.lastSum) {
            this.lastSum = sum;
            if (rc == KeeperException.Code.OK.intValue()) {
                System.out.println("Changed child of z node. There are: " + (sum - 1) + " children now.");
            }
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (rc == KeeperException.Code.OK.intValue()) {
            if (this.process == null) {
                System.out.println("Added z node. Starting program.");
                this.zk.getChildren(this.watchFileName, true, this, null);

                ProcessBuilder pb = new ProcessBuilder();
                pb.command(this.exec);

                try {
                    this.process = pb.start();
                } catch (IOException e) {
                    System.err.println("Error when starting exec script.");
                    e.printStackTrace();
                }
            }
        } else if (rc == KeeperException.Code.NONODE.intValue()) {
            if (this.process != null) {
                System.out.println("Deleted z node. Stopping program.");
                this.process.destroy();
                this.process = null;
            }
        } else
            System.err.println("Exception detected" + rc);

        this.zk.exists(this.watchFileName, true, this, null);
    }
}