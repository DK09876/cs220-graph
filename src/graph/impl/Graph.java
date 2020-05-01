package graph.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import graph.IGraph;
import graph.INode;

import graph.NodeVisitor;

/**
 * A basic representation of a graph that can perform BFS, DFS, Dijkstra, and
 * Prim-Jarnik's algorithm for a minimum spanning tree.
 * 
 * @author jspacco
 *
 */
public class Graph implements IGraph {
	private Map<String, INode> nodes = new HashMap<>();

	/**
	 * Return the {@link Node} with the given name.
	 * 
	 * If no {@link Node} with the given name exists, create a new node with the
	 * given name and return it. Subsequent calls to this method with the same name
	 * should then return the node just created.
	 * 
	 * @param name
	 * @return
	 */
	public INode getOrCreateNode(String name) {
		if (nodes.containsKey(name))
			return nodes.get(name);
		nodes.put(name, new Node(name));
		return nodes.get(name);
	}

	/**
	 * Return true if the graph contains a node with the given name, and false
	 * otherwise.
	 * 
	 * @param name
	 * @return
	 */
	public boolean containsNode(String name) {
		return (nodes.containsKey(name));
	}

	/**
	 * Return a collection of all of the nodes in the graph.
	 * 
	 * @return
	 */
	public Collection<INode> getAllNodes() {
		return nodes.values();
	}

	/**
	 * Perform a breadth-first search on the graph, starting at the node with the
	 * given name. The visit method of the {@link NodeVisitor} should be called on
	 * each node the first time we visit the node.
	 * 
	 * 
	 * @param startNodeName
	 * @param v
	 */
	public void breadthFirstSearch(String startNodeName, NodeVisitor v) {
		Set<INode> visited= new HashSet<INode>();
		Queue<INode> tovisit=new LinkedList<>();
		tovisit.add(nodes.get(startNodeName));
		while(tovisit.size()!=0) {
			INode x=tovisit.remove();
			//check if the node has already and skip if it has been visited because if not it will result to an infinite loop where we keep visiting the same thing 
			if (visited.contains(x))
				continue;
			v.visit(x);
			visited.add(tovisit.remove());
			for (INode y: x.getNeighbors()) {
				//going to adjacent nodes and adding them if they havent been visited to the queue
				if (!visited.contains(y))
					tovisit.add(y);
			}
		}	
	}

	/**
	 * Perform a depth-first search on the graph, starting at the node with the
	 * given name. The visit method of the {@link NodeVisitor} should be called on
	 * each node the first time we visit the node.
	 * 
	 * 
	 * @param startNodeName
	 * @param v
	 */
	public void depthFirstSearch(String startNodeName, NodeVisitor v) {
		//same as BFS but with a stack because we want the stuff that comes in later aka the neighbors of neighbors to come out before the first neighbours from the start
		Set<INode> visited = new HashSet<>();
		Stack<INode> tovisit=new Stack<INode>();
		tovisit.push(nodes.get(startNodeName));
		while(!tovisit.empty()) {
			INode x=tovisit.pop();
			if (visited.contains(x))
				continue;
			v.visit(x);
			for (INode y: x.getNeighbors()) {
				if (!visited.contains(y))
					tovisit.push(y);
			}
		}
	}

	/**
	 * Perform Dijkstra's algorithm for computing the cost of the shortest path to
	 * every node in the graph starting at the node with the given name. Return a
	 * mapping from every node in the graph to the total minimum cost of reaching
	 * that node from the given start node.
	 * 
	 * <b>Hint:</b> Creating a helper class called Path, which stores a destination
	 * (String) and a cost (Integer), and making it implement Comparable, can be
	 * helpful. Well, either than or repeated linear scans.
	 * 
	 * @param startName
	 * @return
	 */
	public Map<INode, Integer> dijkstra(String startName) {
		
		//helper class path to help the priorityqueue
		class Path implements Comparable<Path>{
			private String destination;
			private int cost;
			Path(String dest,int cost){
				this.destination=dest;
				this.cost=cost;		
			}
			public String getdest() {
				return this.destination;
			}
			public Integer getcost() {
				//had to return Integer for compareto method
				return this.cost;
			}
			@Override
			public int compareTo(Path pt) {
				return this.getcost().compareTo(pt.getcost());
			}
		}
		
		Map<INode,Integer> result= new HashMap<INode,Integer>();
		PriorityQueue<Path> todo=new PriorityQueue<>();
		todo.add(new Path(startName,0));
		while (result.size()<this.getAllNodes().size()) {
			Path nextpath= todo.poll();
			INode node=nodes.get(nextpath.destination);
			if (result.containsKey(node))
				continue;
			int cost= nextpath.getcost();
			result.put(node,cost);
			for (INode n : node.getNeighbors()) {
				todo.add(new Path(n.getName(), cost + node.getWeight(n)));
			}	
		}
		return result;
	}

	/**
	 * Perform Prim-Jarnik's algorithm to compute a Minimum Spanning Tree (MST).
	 * 
	 * The MST is itself a graph containing the same nodes and a subset of the edges
	 * from the original graph.
	 * 
	 * @return
	 */
	public IGraph primJarnik() {
		class Edge implements Comparable <Edge>{
			public final String src;
			public final String dst;
			public final int weight;
			
			public Edge(String src,String dst,int weight) {
				this.src=src;
				this.dst=dst;
				this.weight=weight;
			}
			
			public int compareTo(Edge ed) {
				return this.weight-ed.weight;
			}
		}
		IGraph graph=new Graph();
		INode start=this.getAllNodes().iterator().next();
		PriorityQueue<Edge> todo= new PriorityQueue<>();
		
		for (INode n : start.getNeighbors()) 
			todo.add(new Edge(start.getName(),n.getName(),n.getWeight(start)));
			
			while (graph.getAllNodes().size()!=this.getAllNodes().size()) {
				Edge e= todo.poll();
				if (graph.containsNode(e.dst))
					continue;
				INode dst=graph.getOrCreateNode(e.dst);
				INode src=graph.getOrCreateNode(e.src);
				src.addUndirectedEdgeToNode(dst, e.weight);
				
				//adding stuff back to the PQ
				INode edgestoadd=nodes.get(e.dst);
				for (INode n: edgestoadd.getNeighbors())
					todo.add(new Edge(edgestoadd.getName(),n.getName(),n.getWeight(edgestoadd)));
			}
		return graph;
	}
}