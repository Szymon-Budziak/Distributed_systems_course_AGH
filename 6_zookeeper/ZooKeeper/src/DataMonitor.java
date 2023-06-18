import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DataMonitor implements AsyncCallback.StatCallback, AsyncCallback.Children2Callback {
    private final ZooKeeper zk;
    private final String znode;
    private int lastSum = -1;
    private boolean guiOpened = false;
    private JFrame frame;
    private JLabel numberOfChildrenLabel;
    private int currentY;
    private final List<JLabel> childLabels = new ArrayList<>();

    public DataMonitor(ZooKeeper zk, String znode) {
        this.zk = zk;
        this.znode = znode;
    }

    public void startWatch() {
        this.zk.exists(this.znode, true, this, null);
        subscribeChildrenAndGetCount(this.znode);
    }

    private int subscribeChildrenAndGetCount(String childrenName) {
        this.zk.getChildren(childrenName, true, this, null);

        int sum = 1;
        try {
            List<String> children = this.zk.getChildren(childrenName, false);
            for (String child : children) {
                sum += subscribeChildrenAndGetCount(childrenName + "/" + child);
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
        int sum = subscribeChildrenAndGetCount(this.znode);
        if (sum != this.lastSum) {
            this.lastSum = sum;
            if (rc == KeeperException.Code.OK.intValue()) {
                System.out.println("Changed child of `z` node. There are: " + (sum - 1) + " children now.");
                this.numberOfChildrenLabel.setText("Current number of children: " + (sum - 1));
            }
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (rc == KeeperException.Code.OK.intValue()) {
            if (!this.guiOpened) {
                System.out.println("Added `z` node. Opening GUI.");
                this.zk.getChildren(this.znode, true, this, null);

                this.frame = new JFrame("My GUI");
                this.frame.setLayout(null);

                this.numberOfChildrenLabel = new JLabel("Current number of children: " + (this.lastSum - 1));
                this.numberOfChildrenLabel.setBounds(20, 20, 200, 30);
                this.frame.add(this.numberOfChildrenLabel);

                JButton displayChildrenTreeButton = new JButton("Display children tree");
                displayChildrenTreeButton.setBounds(20, 60, 200, 40);
                displayChildrenTreeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        displayChildrenTree();
                    }
                });
                this.frame.add(displayChildrenTreeButton);

                this.frame.setSize(700, 500);
                this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                this.frame.setVisible(true);
                this.guiOpened = true;
            }
        } else if (rc == KeeperException.Code.NONODE.intValue()) {
            if (this.guiOpened) {
                System.out.println("Deleted `z` node. Closing GUI.");
                this.currentY = 120;
                this.frame.setVisible(false);
                this.guiOpened = false;
            }
        } else
            System.err.println("Exception detected " + rc);

        this.zk.exists(this.znode, true, this, null);
    }

    private void displayChildrenTree() {
        try {
            clearChildLabels();
            List<String> children = this.zk.getChildren(this.znode, false);
            System.out.println("Listing children for: " + this.znode);

            this.currentY = 120;
            printChildren(children, this.znode);
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

            JLabel childLabel = new JLabel(path);
            childLabel.setBounds(20, this.currentY, 200, 30);
            this.frame.add(childLabel);
            this.childLabels.add(childLabel);
            this.frame.update(this.frame.getGraphics());
            this.currentY += 20;

            List<String> nestedChildren = this.zk.getChildren(path, false);
            printChildren(nestedChildren, path);
        }
    }

    private void clearChildLabels() {
        for (JLabel childLabel : this.childLabels) {
            this.frame.remove(childLabel);
        }
        this.childLabels.clear();
    }
}