import org.apache.zookeeper.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Executor implements Watcher {
    private static final String ZNODE = "/z";

    private final ZooKeeper zk;
    private final String externalApplicationName;
    private Process externalApplicationProcess = null;
    private JFrame frame;
    private JLabel numberOfChildrenLabel = null;
    private int currentY = 120;
    private final List<JLabel> childLabels = new ArrayList<>();

    public Executor(String connectionString, String externalApplicationName) {
        try {
            this.zk = new ZooKeeper(connectionString, 5000, this);
            this.externalApplicationName = externalApplicationName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Wrong number of arguments. USAGE: Executor <connectionString> <external_application_name>.");
            System.exit(1);
        }
        String connectionString = args[0];
        String externalApplicationName = args[1];

        Executor executor = new Executor(connectionString, externalApplicationName);

        try {
            executor.zk.addWatch(ZNODE, AddWatchMode.PERSISTENT_RECURSIVE);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Executor ready. When `z` node will be created, external graphic application will pop up. " +
                "When child of `z` node will be created GUI window will pop up. When `z` node will be deleted, both " +
                "windows will be closed.");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        Event.EventType eventType = watchedEvent.getType();
        String nodePath = watchedEvent.getPath();

        if (eventType == Event.EventType.NodeCreated && nodePath.equals(ZNODE)) {
            System.out.println("Added `z` node. Opening external graphic application.");
            openExternalGraphicApplication();
        } else if (eventType == Event.EventType.NodeCreated && nodePath.startsWith(ZNODE)) {
            System.out.println("Created new child of `z` node. Opening GUI if it is not already open.");
            openGUI();
        } else if (eventType == Event.EventType.NodeDeleted && nodePath.equals(ZNODE)) {
            System.out.println("Removed `z` node. Closing external graphic application and GUI.");
            closeExternalGraphicApplication();
            closeGUI();
        } else if (eventType == Event.EventType.NodeDeleted && nodePath.startsWith(ZNODE)) {
            System.out.println("Removed child of `z` node.");
            if (this.numberOfChildrenLabel != null)
                this.numberOfChildrenLabel.setText("Current number of children: " + (getCountOfChildren(ZNODE) - 1));
        }
    }

    private void openExternalGraphicApplication() {
        if (this.externalApplicationProcess == null) {
            try {
                ProcessBuilder pb = new ProcessBuilder();
                pb.command(this.externalApplicationName);
                this.externalApplicationProcess = pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("External graphic application is already running.");
        }
    }

    private void closeExternalGraphicApplication() {
        if (this.externalApplicationProcess != null) {
            this.externalApplicationProcess.destroy();
            this.externalApplicationProcess = null;
        } else {
            System.out.println("External graphic application is not running.");
        }
    }

    private void openGUI() {
        if (this.frame == null) {
            this.frame = new JFrame("My GUI");
            this.frame.setLayout(null);

            this.numberOfChildrenLabel = new JLabel("Current number of children: " + (getCountOfChildren(ZNODE) - 1));
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
        } else {
            if (this.numberOfChildrenLabel != null)
                this.numberOfChildrenLabel.setText("Current number of children: " + (getCountOfChildren(ZNODE) - 1));
            System.out.println("GUI is already opened.");
        }
    }

    private void closeGUI() {
        if (this.frame != null) {
            this.frame.setVisible(false);
            this.frame = null;
        } else {
            System.out.println("GUI is not opened.");
        }
    }

    private int getCountOfChildren(String childrenName) {
        int sum = 1;
        try {
            List<String> children = this.zk.getChildren(childrenName, false);
            for (String child : children) {
                sum += getCountOfChildren(childrenName + "/" + child);
            }
        } catch (KeeperException.NoNodeException e) {
            //
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }

    private void displayChildrenTree() {
        try {
            clearChildLabels();
            List<String> children = this.zk.getChildren(ZNODE, false);
            printChildren(children, ZNODE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            System.out.println("Node does not exist.");
        }
    }

    private void printChildren(List<String> children, String base) throws InterruptedException, KeeperException {
        for (String child : children) {
            String path = base + "/" + child;

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
            this.currentY -= 20;
        }
        this.childLabels.clear();
    }
}