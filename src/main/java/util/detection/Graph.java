package util.detection;

import util.TopologyFactory;
import io.jbotsim.core.Link;
import io.jbotsim.core.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Graph {
    private TopologyFactory.TopologyType type;
    private final Set<Node> nodes = new HashSet<>();
    private final Set<Link> links = new HashSet<>();

    public Graph() {
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public boolean isTree() {
        // The number of edges equals the number of nodes minus 1.
        return this.getLinks().size() == this.getNodes().size() - 1;
    }

    public boolean isRing() {
        AtomicReference<Boolean> isRing = new AtomicReference<>();
        // The degree of all the nodes has to be 2.
        this.nodes.parallelStream().forEach(node -> isRing.set(node.getNeighbors().size() == 2));
        // and the number of edges has to equal the number of nodes.
        return isRing.get() && this.getLinks().size() == this.getNodes().size();
    }

    @Override
    public String toString() {
        return "{nodes: " + nodes.toString() + ", edges: " + links.toString() + "}";
    }

    public Map<Node, List<Node>> getAllPath(Node from) {
        if (!nodes.contains(from)) {
            return null;
        }
        Map<Node, List<Node>> pathMap = new HashMap<>();
        // Use BFS to search path
        Set<Node> checkedNode = new HashSet<>();
        LinkedList<Link> linkList = getAllLinkStartFrom(from);
        checkedNode.add(from);
        while (!linkList.isEmpty()) {
            Link l = linkList.poll();
            if (!checkedNode.contains(l.destination)) {
                checkedNode.add(l.destination);

                if (!pathMap.containsKey(l.destination) && !pathMap.containsKey(l.source)) {
                    pathMap.put(l.destination, Arrays.asList(from, l.destination));
                } else if (pathMap.containsKey(l.source) && !pathMap.containsKey(l.destination)) {
                    List<Node> p = new ArrayList<>(pathMap.get(l.source));
                    p.add(l.destination);
                    pathMap.put(l.destination, p);
                }

                linkList.addAll(getAllLinkStartFrom(l.destination));

            }
        }

        return pathMap;
    }

    public LinkedList<Link> getAllLinkStartFrom(Node from) {
        if (!nodes.contains(from)) {
            return null;
        }

        LinkedList<Link> res = new LinkedList<>();
        for (Link l : links) {
            if (l.source.equals(from)) {
                res.add(new Link(from, l.destination));
            } else if (l.destination.equals(from)) {
                res.add(new Link(from, l.source));
            }
        }

        return res;
    }
}
