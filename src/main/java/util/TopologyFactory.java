package util;

import io.jbotsim.core.Link;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;
import io.jbotsim.io.FileManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TopologyFactory {

    public enum TopologyType {
        RANDOM, RING, TREE, X_TREE, STAR, COMPLETE, MESH, TORUS, UNDETERMINED
    }


    public static <T extends Node> Topology readFromFilePath(String path, Supplier<T> supplier) {
        return readFromFile(new File(path), supplier);
    }

    private static <T extends Node> Topology readFromFile(File file, Supplier<T> supplier) {
        // Open file
        String fileContent = new FileManager().read(file.getAbsolutePath());
        String[] lines = fileContent.split("\n");
        int numOfNode = Integer.parseInt(lines[0]);

        // Node List
        List<T> nodes = new ArrayList<>();

        // Initial Topology
        Topology tp = new Topology();
        tp.disableWireless();
        tp.setTimeUnit(1);
        tp.setDimensions(1000, 1000);

        addRandomNode(tp, numOfNode, nodes, supplier);
        for (int l = 1; l < lines.length; l++) {
            String[] twoNodes = lines[l].split(" ");
            tp.addLink(
                    new Link(
                            nodes.get(Integer.parseInt(twoNodes[0])),
                            nodes.get(Integer.parseInt(twoNodes[1]))));
        }
        System.out.println("Total nodes: " + nodes.size());
        return tp;
    }

    public static <T extends Node> Topology generateUnidirectionalRing(Supplier<T> supplier, int... params) {
        // Node List
        List<T> nodes = new ArrayList<>();

        Topology tp = new Topology();
        tp.setOrientation(Link.Orientation.DIRECTED);
        tp.disableWireless();
        tp.setTimeUnit(1);
        tp.setDimensions(500, 500);
        int numOfNode;
        if (params.length == 1) {
            numOfNode = params[0];
        } else {
            numOfNode = 10;
        }

        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double radius = (Math.min(maxHeight, maxWidth) / 2) - 40;

        for (int i = 0; i < numOfNode; i++) {
            double beta = i * 2 * Math.PI / numOfNode;
            nodes.add(supplier.get());
            nodes.get(i)
                    .setLocation(
                            (maxWidth / 2) + (Math.sin(beta) * radius),
                            (maxHeight / 2) + (Math.cos(beta) * radius));
            tp.addNode(nodes.get(i));
            if (i > 0) {
                tp.addLink(
                        new Link(
                                nodes.get(i),
                                nodes.get(i - 1),
                                Link.Orientation.DIRECTED, Link.Mode.WIRED));
            }
            if (i == numOfNode - 1) {
                tp.addLink(
                        new Link(
                                nodes.get(0),
                                nodes.get(i),
                                Link.Orientation.DIRECTED, Link.Mode.WIRED));
            }
        }

        return tp;
    }

    public static <T extends Node> Topology generateTopology(Supplier<T> supplier, TopologyType tpType, boolean shuffleId, int... params) {
        // Node List
        List<T> nodes = new ArrayList<>();

        // Initial Topology
        Topology tp = new Topology();
        tp.disableWireless();
        tp.setTimeUnit(1);
        tp.setDimensions(750, 750);

        switch (tpType) {
            case RANDOM:
                break;
            case RING:
                if (params.length == 1) {
                    addRingTopology(tp, params[0], nodes, supplier);
                } else {
                    addRingTopology(tp, 10, nodes, supplier);
                }
                break;
            case TREE:
                if (params.length == 1) {
                    addTreeTopology(tp, params[0], nodes, supplier);
                } else {
                    addTreeTopology(tp, 3, nodes, supplier);
                }
                break;
            case X_TREE:
                if (params.length == 1) {
                    addXTreeTopology(tp, params[0], nodes, supplier);
                } else {
                    addXTreeTopology(tp, 8, nodes, supplier);
                }
                break;
            case STAR:
                if (params.length == 1) {
                    addStarTopology(tp, params[0], nodes, supplier);
                } else {
                    addStarTopology(tp, 201, nodes, supplier);
                }
                break;
            case COMPLETE:
                if (params.length == 1) {
                    addCompleteTopology(tp, params[0], nodes, supplier);
                } else {
                    addCompleteTopology(tp, 200, nodes, supplier);
                }
                break;
            case MESH:
                if (params.length == 2) {
                    addMeshTopology(tp, params[0], params[1], nodes, supplier);
                } else {
                    addMeshTopology(tp, 15, 15, nodes, supplier);
                }
                break;
            case TORUS:
                if (params.length == 2) {
                    addTorusTopology(tp, params[0], params[1], nodes, supplier);
                } else {
                    addTorusTopology(tp, 15, 15, nodes, supplier);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tpType);
        }
        System.out.println("Total nodes: " + nodes.size());

        if (shuffleId) {
            List<Integer> idList = nodes.stream().map(Node::getID).collect(Collectors.toList());
            Collections.shuffle(idList);
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setID(idList.get(i));
            }
        }

        return tp;
    }

    private static <T extends Node> void addRandomNode(Topology tp, int numOfNode, List<T> nodes, Supplier<T> supplier) {
        int maxWidth = tp.getWidth();
        int maxHeight = tp.getHeight();
        Random random = new Random();

        for (int i = 0; i < numOfNode; i++) {
            nodes.add(supplier.get());
            nodes.get(i)
                    .setLocation(
                            random.nextInt(maxWidth),
                            random.nextInt(maxHeight));
            tp.addNode(nodes.get(i));
        }
    }

    private static <T extends Node> void addRingNode(Topology tp, int numOfNode, List<T> nodes, Supplier<T> supplier) {
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double radius = (Math.min(maxHeight, maxWidth) / 2) - 40;
        for (int i = 0; i < numOfNode; i++) {
            double beta = i * 2 * Math.PI / numOfNode;
            nodes.add(supplier.get());
            nodes.get(i)
                    .setLocation(
                            (maxWidth / 2) + (Math.sin(beta) * radius),
                            (maxHeight / 2) + (Math.cos(beta) * radius));
            tp.addNode(nodes.get(i));
        }
    }

    private static <T extends Node> void addRingTopology(Topology tp, int numOfNode, List<T> nodes, Supplier<T> supplier) {
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double radius = (Math.min(maxHeight, maxWidth) / 2) - 40;
        for (int i = 0; i < numOfNode; i++) {
            double beta = i * 2 * Math.PI / numOfNode;
            nodes.add(supplier.get());
            nodes.get(i)
                    .setLocation(
                            (maxWidth / 2) + (Math.sin(beta) * radius),
                            (maxHeight / 2) + (Math.cos(beta) * radius));
            tp.addNode(nodes.get(i));
            if (i > 0) {
                tp.addLink(
                        new Link(
                                nodes.get(i),
                                nodes.get(i - 1)));
            }
            if (i == numOfNode - 1) {
                tp.addLink(
                        new Link(
                                nodes.get(i),
                                nodes.get(0)));
            }
        }
    }

    private static <T extends Node> void addTreeTopology(Topology tp, int height, List<T> nodes, Supplier<T> supplier) {
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double start_x;
        double start_y = 15;

        int index = 0;
        for (int i = 0; i < height; i++) {
            start_x = maxWidth / (Math.pow(2, i) + 1);
            for (int j = 0; j < Math.pow(2, i); j++) {
                nodes.add(supplier.get());
                nodes.get(index)
                        .setLocation(
                                start_x + (j * maxWidth / (Math.pow(2, i) + 1)),
                                start_y + (i * maxHeight / height));
                tp.addNode(nodes.get(index));
                if (index > 0) {
                    tp.addLink(
                            new Link(
                                    nodes.get((index - 1) / 2),
                                    nodes.get(index)));
                }
                index++;
            }

        }
    }

    private static <T extends Node> void addXTreeTopology(Topology tp, int height, List<T> nodes, Supplier<T> supplier) {
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double start_x;
        double start_y = 15;

        for (int i = 0, index = 0; i < height; i++) {
            start_x = maxWidth / (Math.pow(2, i) + 1);
            for (int j = 0; j < Math.pow(2, i); j++, index++) {
                nodes.add(supplier.get());
                nodes.get(index)
                        .setLocation(
                                start_x + (j * maxWidth / (Math.pow(2, i) + 1)),
                                start_y + (i * maxHeight / height));
                tp.addNode(nodes.get(index));
                if (index > 0) {
                    // Child with parent
                    tp.addLink(
                            new Link(
                                    nodes.get((index - 1) / 2),
                                    nodes.get(index)));
                    // Child with next child
                    if (j > 0) {
                        tp.addLink(
                                new Link(
                                        nodes.get((index - 1)),
                                        nodes.get(index)));
                    }
                }
            }

        }
    }

    private static <T extends Node> void addStarTopology(Topology tp, int numOfNode, List<T> nodes, Supplier<T> supplier) {
        if (numOfNode == 0) {
            return;
        }
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double radius = (Math.min(maxHeight, maxWidth) / 2) - 40;

        // Center
        nodes.add(supplier.get());
        nodes.get(0)
                .setLocation(
                        (maxWidth / 2),
                        (maxHeight / 2));
        tp.addNode(nodes.get(0));

        for (int i = 1; i < numOfNode; i++) {
            double beta = i * 2 * Math.PI / numOfNode;
            nodes.add(supplier.get());
            nodes.get(i)
                    .setLocation(
                            (maxWidth / 2) + (Math.sin(beta) * radius),
                            (maxHeight / 2) + (Math.cos(beta) * radius));
            tp.addNode(nodes.get(i));
            tp.addLink(
                    new Link(
                            nodes.get(0),
                            nodes.get(i)));
        }
    }

    private static <T extends Node> void addCompleteTopology(Topology tp, int numOfNode, List<T> nodes, Supplier<T> supplier) {
        if (numOfNode == 0) {
            return;
        }
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double radius = (Math.min(maxHeight, maxWidth) / 2) - 40;

        for (int i = 0; i < numOfNode; i++) {
            double beta = i * 2 * Math.PI / numOfNode;
            nodes.add(supplier.get());
            nodes.get(i)
                    .setLocation(
                            (maxWidth / 2) + (Math.sin(beta) * radius),
                            (maxHeight / 2) + (Math.cos(beta) * radius));
            tp.addNode(nodes.get(i));
        }

        for (int i = 0; i < numOfNode; i++) {
            for (int j = (i + 1) % numOfNode; j != i; j = (j + 1) % numOfNode) {
                tp.addLink(
                        new Link(
                                nodes.get(i),
                                nodes.get(j)));
            }
        }
    }

    private static <T extends Node> void addMeshTopology(Topology tp, int width, int height, List<T> nodes, Supplier<T> supplier) {
        if (width == 0 || height == 0) {
            return;
        }
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double start_x = maxWidth / (width + 1);
        double start_y = 15;

        for (int i = 0, index = 0; i < height; i++) {
            for (int j = 0; j < width; j++, index++) {
                nodes.add(supplier.get());
                nodes.get(index)
                        .setLocation(
                                start_x + (j * maxWidth / (width + 1)),
                                start_y + (i * maxHeight / height));
                tp.addNode(nodes.get(index));
            }
        }

        for (int i = 0, index = 0; i < height; i++) {
            for (int j = 0; j < width; j++, index++) {
                if (j != width - 1) {
                    tp.addLink(
                            new Link(
                                    nodes.get(i * width + j),
                                    nodes.get(i * width + j + 1)));
                }
                if (i != height - 1) {
                    tp.addLink(
                            new Link(
                                    nodes.get(i * width + j),
                                    nodes.get(i * width + j + width)));
                }
            }
        }
    }

    private static <T extends Node> void addTorusTopology(Topology tp, int width, int height, List<T> nodes, Supplier<T> supplier) {
        if (width == 0 || height == 0) {
            return;
        }
        double maxWidth = tp.getWidth();
        double maxHeight = tp.getHeight();
        double start_x = maxWidth / (width + 1);
        double start_y = 15;

        for (int i = 0, index = 0; i < height; i++) {
            for (int j = 0; j < width; j++, index++) {
                nodes.add(supplier.get());
                nodes.get(index)
                        .setLocation(
                                start_x + (j * maxWidth / (width + 1)),
                                start_y + (i * maxHeight / height));
                tp.addNode(nodes.get(index));
            }
        }

        for (int i = 0, index = 0; i < height; i++) {
            for (int j = 0; j < width; j++, index++) {
                if (j != width - 1) {
                    tp.addLink(
                            new Link(
                                    nodes.get(i * width + j),
                                    nodes.get(i * width + j + 1)));
                } else {
                    tp.addLink(
                            new Link(
                                    nodes.get(i * width + j),
                                    nodes.get(i * width + j - width + 1)));
                }
                if (i != height - 1) {
                    tp.addLink(
                            new Link(
                                    nodes.get(i * width + j),
                                    nodes.get(i * width + j + width)));
                } else {
                    tp.addLink(
                            new Link(
                                    nodes.get(i * width + j),
                                    nodes.get(i * width + j - (i * width))));
                }
            }
        }
    }

    public static void generateTxt(String path, TopologyType tpType) throws IOException {
        switch (tpType) {
            case RANDOM:
                break;
            case RING:
                generateRingTopology(path, 200);
                break;
            case TREE:
                generateTreeTopology(path, 3);
                break;
            case X_TREE:
                generateXTreeTopology(path, 8);
                break;
            case STAR:
                generateStarTopology(path, 201);
                break;
            case COMPLETE:
                generateCompleteTopology(path, 200);
                break;
            case MESH:
                generateMeshTopology(path, 15, 15);
                break;
            case TORUS:
                generateTorusTopology(path, 2, 2);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + tpType);
        }
    }

    private static void generateTorusTopology(String path, int width, int height) throws IOException {
        if (width == 0 || height == 0) {
            return;
        }
        FileWriter fw = new FileWriter(path);
        fw.write((width * height) + "\n");

        for (int i = 0, index = 0; i < height; i++) {
            for (int j = 0; j < width; j++, index++) {
                if (j != width - 1) {
                    fw.write((i * width + j) + " " + (i * width + j + 1) + "\n");
                } else {
                    fw.write((i * width + j) + " " + (i * width + j - width + 1) + "\n");
                }
                if (i != height - 1) {
                    fw.write((i * width + j) + " " + (i * width + j + width) + "\n");
                } else {
                    fw.write((i * width + j) + " " + (i * width + j - (i * width)) + "\n");
                }
            }
        }

        fw.close();
    }

    private static void generateMeshTopology(String path, int width, int height) throws IOException {
        if (width == 0 || height == 0) {
            return;
        }
        FileWriter fw = new FileWriter(path);
        fw.write((width * height) + "\n");


        for (int i = 0, index = 0; i < height; i++) {
            for (int j = 0; j < width; j++, index++) {
                if (j != width - 1) {
                    fw.write((i * width + j) + " " + (i * width + j + 1) + "\n");
                }
                if (i != height - 1) {
                    fw.write((i * width + j) + " " + (i * width + j + width) + "\n");
                }
            }
        }

        fw.close();
    }

    private static void generateCompleteTopology(String path, int numOfNode) throws IOException {
        if (numOfNode == 0) {
            return;
        }
        FileWriter fw = new FileWriter(path);
        fw.write(numOfNode + "\n");
        for (int i = 0; i < numOfNode; i++) {
            for (int j = (i + 1) % numOfNode; j != i; j = (j + 1) % numOfNode) {
                fw.write(i + " " + j + "\n");
            }
        }
        fw.close();
    }

    private static void generateStarTopology(String path, int numOfNode) throws IOException {
        if (numOfNode == 0) {
            return;
        }
        FileWriter fw = new FileWriter(path);
        fw.write(numOfNode + "\n");

        for (int i = 1; i < numOfNode; i++) {
            fw.write(0 + " " + i + "\n");
        }
        fw.close();
    }

    private static void generateXTreeTopology(String path, int height) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(((int) Math.pow(2, height) - 1) + "\n");
        for (int i = 0, index = 0; i < height; i++) {
            for (int j = 0; j < Math.pow(2, i); j++, index++) {
                if (index > 0) {
                    // Child with parent
                    fw.write(((int) (index - 1) / 2) + " " + index + "\n");
                    // Child with next child
                    if (j > 0) {
                        fw.write((index - 1) + " " + index + "\n");
                    }
                }
            }
        }
        fw.close();
    }

    private static void generateTreeTopology(String path, int height) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(((int) Math.pow(2, height) - 1) + "\n");
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < Math.pow(2, i); j++) {
                if (index > 0) {
                    fw.write(((int) (index - 1) / 2) + " " + index + "\n");
                }
                index++;
            }
        }
        fw.close();
    }

    private static void generateRingTopology(String path, int numOfNode) throws IOException {
        FileWriter fw = new FileWriter(path);
        fw.write(numOfNode + "\n");
        for (int i = 0; i < numOfNode; i++) {
            if (i > 0) {
                fw.write(i + " " + (i - 1) + "\n");
            }
            if (i == numOfNode - 1) {
                fw.write(i + " " + 0 + "\n");
            }
        }

        fw.close();
    }
}
