/*
 * Welcome To Our Maze Game! 
 * 
 * UP/DOWN/LEFT/RIGHT - Player Movement Keys
 * 'r' start a new random maze with the same size as the original
 * 't' toggle the Player's path *extra credit*
 * 'b' perform a breadth-first search (Path shown in Blue)
 * 'd' perform a depth-first search (Path shown in Blue)
 * 
 * Upon Arrival at the end of the maze, press 'b' or 'd' to display the shortest path
 * 
 * GOODLUCK AND MAY THE ODDS FOREVER BE IN YOUR FAVOR c:
 *
 */

import java.awt.Color;
import java.util.*;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;

// to represent an edge in the maze
class Edge implements Comparable<Edge> {
  Vertex from;
  Vertex to;
  int weight;

  // to initialize all the fields of the edge
  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  // checks if this edge is equal to the given object
  public boolean equals(Object other) {
    if (!(other instanceof Edge)) {
      return false;
    }
    Edge that = (Edge) other;
    return (this.from.equals(that.from) || this.from.equals(that.to))
        && (this.to.equals(that.to) || this.to.equals(that.from));
  }

  // checks if this edge's weight is smaller than the given edge's weight
  public int compareTo(Edge that) {
    if (this.weight < that.weight) {
      return -1;
    }
    if (this.weight > that.weight) {
      return 1;
    }
    else {
      return 0;
    }
  }

  // renders an image of this edge
  WorldImage drawEdge() {
    return new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray);
  }
}


// to represent the player of the maze game
class Player {
  // in logical coordinates, the position of the player
  int x;
  int y;
  
  // to initialize all the fields of the player
  Player(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  // draws the player
  WorldImage drawPlayer() {
    return new RectangleImage(9, 9, OutlineMode.SOLID, Color.green);
  }
}

// to represent a vertex in the maze
class Vertex {
  int x;
  int y;
  ArrayList<Edge> outEdges;

  // Has our search function scanned this vertex?
  Boolean isScanned;
  //Is this vertex in the solution path?
  Boolean isInSolution;

  // to initialize all the fields of the vertex
  Vertex(int x, int y, ArrayList<Edge> outEdges) {
    this.x = x;
    this.y = y;
    this.outEdges = outEdges;
    this.isScanned = false;
    this.isInSolution = false;
  }

  // checks if this vertex is equal to the given object
  public boolean equals(Object other) {
    if (!(other instanceof Vertex)) {
      return false;
    }
    Vertex that = (Vertex) other;
    return this.x == that.x 
        && this.y == that.y;
  }

  // draws a grid square out of this vertex
  WorldImage drawVertex() {
    return new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray);
  }

  // draws the grid square of this vertex if it has been searched
  WorldImage drawExploredVertex() {
    if (this.isScanned) {
      return new RectangleImage(9, 9, OutlineMode.SOLID, Color.cyan);
    }
    else {
      return new EmptyImage();
    }
  }

  // draws a vertex in the shortest maze path
  WorldImage drawShortestPath() {
    if (this.isInSolution) {
      return new RectangleImage(9, 9, OutlineMode.SOLID, Color.blue);
    }
    else {
      return new EmptyImage();
    }

  }

  // draws the player's path as they play the game. Can be toggled with 't' keypress 
  WorldImage drawPlayerPath() {
    return new RectangleImage(9, 9, OutlineMode.SOLID, Color.pink);
  }

  // creates the hash code for this vertex
  public int hashCode() {
    return this.x * this.y * 1000;
  }
}

//to represents the MazeWorld Class
class MazeWorld extends World {
  Random rand = new Random();
  int height;
  int width;

  // counter for the game. used throughout the game to track iterations.
  int counter;
  // the player's score
  int score;
  
  
  ArrayList<ArrayList<Vertex>> arrOfVertices;

  HashMap<Vertex, Vertex> representatives;
  List<Edge> edgesInTree;

  // all edges in graph, sorted by edge weights
  List<Edge> worklist;

  // the player
  Player player;

  // a list of seen vertices
  ArrayList<Vertex> explored;

  // a list of vertices that are part of the path
  ArrayList<Vertex> path;

  //player's path
  ArrayList<Vertex> playerPath;

  // Does the user want to display their path?
  boolean displayPlayerPath;

  MazeWorld() {
    this.displayPlayerPath = true;
    
    // smallest maze size is 3x3
    this.width = (rand.nextInt(58) + 3);
    this.height = (rand.nextInt(98) + 3);

    this.counter = -1;
    this.arrOfVertices = this.initVertices();
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = this.sortByEdgeWeight(this.initEdges());
    this.kruskal(this.worklist, this.arrOfVertices);
    this.configureEdges();
    this.player = new Player(0, 0);
    this.explored = new ArrayList<Vertex>();
    this.path = new ArrayList<Vertex>();
    this.playerPath = new ArrayList<Vertex>(Arrays.asList(this.arrOfVertices.get(0).get(0)));
    this.score = 0;
  }

  //Creates a new maze with the same width and height as the original
  void initMaze() {
    this.score = 0;
    this.arrOfVertices = this.initVertices();
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = this.sortByEdgeWeight(this.initEdges());
    this.kruskal(this.worklist, this.arrOfVertices);
    this.configureEdges();
    this.player = new Player(0, 0);
    this.explored = new ArrayList<Vertex>();
    this.path = new ArrayList<Vertex>();
    this.counter = -1;
    this.playerPath = new ArrayList<Vertex>(Arrays.asList(this.arrOfVertices.get(0).get(0)));
    this.displayPlayerPath = true;
  }

  // makes a grid of vertices
  ArrayList<ArrayList<Vertex>> initVertices() {
    ArrayList<ArrayList<Vertex>> cArray = new ArrayList<ArrayList<Vertex>>();
    for (int c = 0; c < this.width; c++) {
      ArrayList<Vertex> rArray = new ArrayList<Vertex>();
      for (int r = 0; r < this.height; r++) {
        ArrayList<Edge> edges = new ArrayList<Edge>();
        Vertex v = new Vertex(c, r, edges);
        rArray.add(v);
      }
      cArray.add(rArray);
    }
    return cArray;
  }

  // creates a list of all edges in a maze
  ArrayList<Edge> initEdges() {
    ArrayList<Edge> result = new ArrayList<Edge>();
    for (int c = 0; c < this.arrOfVertices.size(); c++) {
      for (int r = 0; r < this.arrOfVertices.get(c).size(); r++) {

        // add right-edges where appropriate
        if (c < this.width - 1) {
          Vertex vLeft = arrOfVertices.get(c).get(r);
          Vertex vRight = arrOfVertices.get(c + 1).get(r);
          Edge rEdge = new Edge(vLeft, vRight, new Random().nextInt());
          result.add(rEdge);
        }

        // add bottom-edges where appropriate
        if (r < this.height - 1) {
          Vertex vTop = arrOfVertices.get(c).get(r);
          Vertex vBottom = arrOfVertices.get(c).get(r + 1);
          Edge bEdge = new Edge(vTop, vBottom, new Random().nextInt());
          result.add(bEdge);
        }
      }
    }
    return result;

  }

  // sorts a list of edges by weight
  List<Edge> sortByEdgeWeight(ArrayList<Edge> edges) {
    Collections.sort(edges);
    return edges;
  }

  // makes each vertex a representative of itself first and then connects the edges
  // using kruskal's algorithm
  List<Edge> kruskal(List<Edge> worklist, ArrayList<ArrayList<Vertex>> grid) {
    HashMap<Vertex, Vertex> result = new HashMap<Vertex, Vertex>();
    for (ArrayList<Vertex> arrV : grid) {
      for (Vertex v : arrV) {
        result.put(v, v);
      }
    }
    while (this.edgesInTree.size() < result.size() - 1) {

      Edge cur = worklist.remove(0);
      if (!find(result, cur.to).equals(find(result, cur.from))) {
        this.edgesInTree.add(cur);
        union(result, find(result, cur.to), (find(result, cur.from)));
      }
    }
    worklist.removeAll(this.edgesInTree);
    this.representatives = result;
    return this.edgesInTree;
  }

  // finds the first vertex with the given key
  Vertex find(HashMap<Vertex, Vertex> r, Vertex v) {
    if (r.get(v).equals(v)) {
      return r.get(v);
    }
    else {
      return find(r, r.get(v));
    }

  }

  // changes the vertex to the given vertex
  void union(HashMap<Vertex, Vertex> rep, Vertex v1, Vertex v2) {
    rep.put(v1, v2);
  }

  // configure edges in the edgesInTree list to the vertices in the maze
  void configureEdges() {
    for (Edge e : this.edgesInTree) {
      e.from.outEdges.add(e);
      e.to.outEdges.add(e);
    }
  }

  // draws the Maze on the World Canvas
  public WorldScene makeScene() {
    WorldScene world = new WorldScene(this.width + 10, this.height + 10);
    // draws the maze
    for (ArrayList<Vertex> av : this.arrOfVertices) {
      for (Vertex v : av) {
        world.placeImageXY(v.drawVertex(), v.x * 10 + 5, v.y * 10 + 5);
      }
    }
    // draws the edges
    for (Edge e : this.edgesInTree) {
      world.placeImageXY(e.drawEdge(), (e.to.x + e.from.x) * 5 + 5,
          (e.to.y + e.from.y) * 5 + 5);
    }
    // draws the end point
    world.placeImageXY(new RectangleImage(9, 9, OutlineMode.SOLID, Color.red),
        this.width * 10 - 5, this.height * 10 - 5);

    // draws the explored vertices
    for (Vertex v : this.explored) {
      world.placeImageXY(v.drawExploredVertex(), v.x * 10 + 5, v.y * 10 + 5);
    }

    // draws the player's path
    if (this.displayPlayerPath) {
      for (Vertex v : this.playerPath) {
        world.placeImageXY(v.drawPlayerPath(), v.x * 10 + 5, v.y * 10 + 5);
      }
      
    }
    // draws the shortest path
    if (this.counter >= this.explored.size()) {
      for (Vertex v : this.path) {
        world.placeImageXY(v.drawShortestPath(), v.x * 10 + 5, v.y * 10 + 5);
      }
    }
    
    // draws the player
    world.placeImageXY(this.player.drawPlayer(), player.x * 10 + 5, player.y * 10 + 5);
    
    // displays the player's score if they reach the end of the maze
    if (this.player.x == this.arrOfVertices.get(this.width - 1).get(this.height - 1).x
        && this.player.y == this.arrOfVertices.get(this.width - 1)
        .get(this.height - 1).y) {
      world.placeImageXY(new OverlayImage(
          new TextImage("You Win! Score: " + Integer.toString(this.score - this.path.size()),
              Color.GREEN),
          new RectangleImage(this.width * 10, this.height * 2, OutlineMode.SOLID,
              Color.WHITE)),
          this.width * 10 / 2, this.height * 10 / 2);
    }
    return world;
  }

  // determines if the player can travel to a position
  boolean canTravelTo(int toX, int toY, int fromX, int fromY) {
    return this.edgesInTree.contains(new Edge(new Vertex(toX, toY, new ArrayList<Edge>()),
        new Vertex(fromX, fromY, new ArrayList<Edge>()), 0));
  }

  // Allow for user interaction with the game (ie. movement, toggle player path, search)
  public void onKeyEvent(String keyPress) {
    if (keyPress.equals("up")) {
      if (this.canTravelTo(this.player.x, this.player.y - 1, this.player.x, this.player.y)) {
        this.player.y = this.player.y - 1;
        this.playerPath.add(this.arrOfVertices.get(this.player.x).get(this.player.y));
        this.score = this.score + 1;
      }
    }
    if (keyPress.equals("down")) {
      if (this.canTravelTo(this.player.x, this.player.y + 1, this.player.x, this.player.y)) {
        this.player.y = this.player.y + 1;
        this.playerPath.add(this.arrOfVertices.get(this.player.x).get(this.player.y));
        this.score = this.score + 1;
      }
    }
    if (keyPress.equals("left")) {
      if (this.canTravelTo(this.player.x - 1, this.player.y, this.player.x, this.player.y)) {
        this.player.x = this.player.x - 1;
        this.playerPath.add(this.arrOfVertices.get(this.player.x).get(this.player.y));
        this.score = this.score + 1;
      }
    }
    if (keyPress.equals("right")) {
      if (this.canTravelTo(this.player.x + 1, this.player.y, this.player.x, this.player.y)) {
        this.player.x = this.player.x + 1;
        this.playerPath.add(this.arrOfVertices.get(this.player.x).get(this.player.y));
        this.score = this.score + 1;
      }
    }

    if (keyPress.equals("b")) {
      this.counter = 0;
      this.performSearch(keyPress);
    }
    if (keyPress.equals("d")) {
      this.counter = 0;
      this.performSearch(keyPress);
    }
    if (keyPress.equals("r")) {
      this.initMaze();
    }

    // Allow the user to toggle view their path
    if (keyPress.equals("t")) {
      this.displayPlayerPath = !this.displayPlayerPath;
    }
  }

  // solves the maze through breadth first search or depth first search
  void performSearch(String keypress) {
    HashMap<Vertex, Vertex> cameFromEdge = new HashMap<Vertex, Vertex>();
    ArrayList<Vertex> worklist = new ArrayList<Vertex>();
    worklist.add(this.arrOfVertices.get(0).get(0));
    this.explored.clear();

    while (worklist.size() > 0) {
      Vertex next = worklist.remove(0);
      Vertex finalNode = this.arrOfVertices.get(this.arrOfVertices.size() 
          - 1).get(this.arrOfVertices.get(0).size() - 1);
      if (next.equals(finalNode)) {
        this.reversePath(cameFromEdge, next);
        return;
      }
      for (Edge e : next.outEdges) {
        if (!this.explored.contains(e.to) && next.equals(e.from)) {
          if (keypress.equals("b")) {
            worklist.add(e.to);
          }
          if (keypress.equals("d")) {
            worklist.add(0, e.to);
          }
          this.explored.add(next);
          cameFromEdge.put(e.to, next);
        }
        else if (!this.explored.contains(e.from) && next.equals(e.to)) {
          if (keypress.equals("b")) {
            worklist.add(e.from);
          }
          if (keypress.equals("d")) {
            worklist.add(0, e.from);
          }
          this.explored.add(next);
          cameFromEdge.put(e.from, next);
        }
      }
    }
  }

  // Effect: reverses the Path so that it may viewed from end to beginning
  void reversePath(HashMap<Vertex, Vertex> cameFromEdge, Vertex next) {
    this.path.add(this.arrOfVertices.get(this.arrOfVertices.size() 
        - 1).get(this.arrOfVertices.get(0).size() - 1));
    Vertex start = this.arrOfVertices.get(0).get(0);
    while (start != next) {
      this.path.add(cameFromEdge.get(next));
      next = cameFromEdge.get(next);
    }
  }

  // draws a vertex of the explored and then the path
  public void onTick() {
    if (this.counter > -1) {
      this.counter += 1;
    }
    if (this.explored.size() > 0) {
      if (this.counter < this.explored.size()) {
        Vertex s = this.explored.get(this.counter);
        s.isScanned = true;
      }
    }
    if (this.path.size() > 0 && this.counter > this.explored.size()) {
      if (this.counter - this.explored.size() < this.path.size()) {
        Vertex p = this.path.get(this.counter - this.explored.size());
        p.isInSolution = true;
      }
    }
  }
}


//to represent Examples of Mazes
class ExamplesMazeWorld {
  MazeWorld world;

  ArrayList<ArrayList<Vertex>> grid;
  ArrayList<ArrayList<Vertex>> grid1;
  ArrayList<ArrayList<Vertex>> grid3;

  ArrayList<Vertex> reconstructedpath;

  ArrayList<Vertex> list1;
  ArrayList<Vertex> list2;
  ArrayList<Vertex> list3;
  ArrayList<Vertex> list4;

  ArrayList<Vertex> gridlist1;
  ArrayList<Vertex> gridlist2;
  ArrayList<Vertex> gridlist3;

  ArrayList<Edge> edges;
  ArrayList<Edge> edges2;
  ArrayList<Edge> sortededges;
  ArrayList<Edge> edgesintree;

  Vertex a;
  Vertex b;
  Vertex c;
  Vertex d;
  Vertex e;
  Vertex f;

  Vertex g;
  Vertex h;
  Vertex i;
  Vertex j;
  Vertex k;
  Vertex l;

  Vertex a1;
  Vertex a2;
  Vertex a3;
  Vertex a4;
  Vertex a5;
  Vertex a6;
  Vertex a7;
  Vertex a8;
  Vertex a9;

  Edge eToC;
  Edge cToD;
  Edge aToB;
  Edge bToE;
  Edge bToC;
  Edge fToD;
  Edge aToE;
  Edge bToF;

  Edge gToH;
  Edge gToJ;
  Edge hToI;
  Edge hToK;
  Edge jToI;
  Edge jToK;
  Edge kToI;
  Edge iToL;

  HashMap<Vertex, Vertex> representatives;
  HashMap<Vertex, Vertex> linkedrepresentatives;
  HashMap<Vertex, Vertex> cameFromEdge;

  Player player;

  // initializes the data
  void initData() {
    this.world = new MazeWorld();
    
    
    // vertices of map
    this.a = new Vertex(0, 0, new ArrayList<Edge>());
    this.b = new Vertex(0, 1, new ArrayList<Edge>());
    this.c = new Vertex(0, 2, new ArrayList<Edge>());
    this.d = new Vertex(1, 0, new ArrayList<Edge>());
    this.e = new Vertex(1, 1, new ArrayList<Edge>());
    this.f = new Vertex(1, 2, new ArrayList<Edge>());

    // example of edges in a map
    this.eToC = new Edge(this.e, this.c, 15);
    this.cToD = new Edge(this.c, this.d, 25);
    this.aToB = new Edge(this.a, this.b, 30);
    this.bToE = new Edge(this.b, this.e, 35);
    this.bToC = new Edge(this.b, this.c, 40);
    this.fToD = new Edge(this.f, this.d, 50);
    this.aToE = new Edge(this.a, this.e, 50);
    this.bToF = new Edge(this.b, this.f, 50);

    // examples of vertices
    this.g = new Vertex(0, 0, new ArrayList<Edge>());
    this.h = new Vertex(0, 1, new ArrayList<Edge>());
    this.i = new Vertex(0, 2, new ArrayList<Edge>());
    this.j = new Vertex(1, 0, new ArrayList<Edge>());
    this.k = new Vertex(1, 1, new ArrayList<Edge>());
    this.i = new Vertex(1, 2, new ArrayList<Edge>());

    // examples of vertices
    this.g = new Vertex(0, 0, new ArrayList<Edge>());
    this.h = new Vertex(0, 1, new ArrayList<Edge>());
    this.i = new Vertex(0, 2, new ArrayList<Edge>());
    this.j = new Vertex(1, 0, new ArrayList<Edge>());
    this.k = new Vertex(1, 1, new ArrayList<Edge>());
    this.i = new Vertex(1, 2, new ArrayList<Edge>());

    // examples of vertices
    this.a1 = new Vertex(0, 0, new ArrayList<Edge>());
    this.a2 = new Vertex(0, 1, new ArrayList<Edge>());
    this.a3 = new Vertex(0, 2, new ArrayList<Edge>());
    this.a4 = new Vertex(1, 0, new ArrayList<Edge>());
    this.a5 = new Vertex(1, 1, new ArrayList<Edge>());
    this.a6 = new Vertex(1, 2, new ArrayList<Edge>());
    this.a7 = new Vertex(1, 0, new ArrayList<Edge>());
    this.a8 = new Vertex(1, 1, new ArrayList<Edge>());
    this.a9 = new Vertex(1, 2, new ArrayList<Edge>());

    // examples of edges
    this.gToH = new Edge(this.g, this.h, 1);
    this.gToJ = new Edge(this.g, this.j, 1);
    this.hToI = new Edge(this.h, this.i, 1);
    this.hToK = new Edge(this.h, this.k, 1);
    this.iToL = new Edge(this.i, this.l, 1);
    this.jToK = new Edge(this.j, this.k, 1);
    this.kToI = new Edge(this.k, this.i, 1);

    this.list3 = new ArrayList<Vertex>(Arrays.asList(this.g, this.h, this.i));
    this.list4 = new ArrayList<Vertex>(Arrays.asList(this.j, this.k, this.l));
    this.grid1 = new ArrayList<ArrayList<Vertex>>(Arrays.asList(this.list3, this.list4));

    this.edges2 = new ArrayList<Edge>(
        Arrays.asList(this.gToH, this.gToJ, this.hToI, this.hToK, this.iToL, this.jToK, this.kToI));

    this.a.outEdges.add(this.aToB);
    this.a.outEdges.add(this.aToE);
    this.b.outEdges.add(this.bToC);
    this.b.outEdges.add(this.bToE);
    this.b.outEdges.add(this.bToF);
    this.c.outEdges.add(this.cToD);
    this.f.outEdges.add(this.fToD);
    this.e.outEdges.add(this.eToC);

    this.list1 = new ArrayList<Vertex>(Arrays.asList(this.a, this.b, this.c));
    this.list2 = new ArrayList<Vertex>(Arrays.asList(this.d, this.e, this.f));
    this.grid = new ArrayList<ArrayList<Vertex>>(Arrays.asList(this.list1, this.list2));

    this.edges = new ArrayList<Edge>(Arrays.asList(this.aToB, this.bToC, this.bToF, this.eToC,
        this.aToE, this.bToE, this.cToD, this.fToD));

    this.sortededges = new ArrayList<Edge>(Arrays.asList(this.eToC, this.cToD, this.aToB, this.bToE,
        this.bToC, this.bToF, this.aToE, this.fToD));

    this.representatives = new HashMap<Vertex, Vertex>();
    this.representatives.put(this.a, this.a);
    this.representatives.put(this.b, this.b);
    this.representatives.put(this.c, this.c);
    this.representatives.put(this.d, this.d);
    this.representatives.put(this.e, this.f);
    this.representatives.put(this.f, this.f);

    this.linkedrepresentatives = new HashMap<Vertex, Vertex>();
    this.linkedrepresentatives.put(this.a, this.e);
    this.linkedrepresentatives.put(this.b, this.a);
    this.linkedrepresentatives.put(this.c, this.e);
    this.linkedrepresentatives.put(this.d, this.e);
    this.linkedrepresentatives.put(this.e, this.e);
    this.linkedrepresentatives.put(this.f, this.d);

    this.edgesintree = new ArrayList<Edge>(
        Arrays.asList(this.eToC, this.cToD, this.aToB, this.bToE, this.fToD));

    this.player = new Player(0, 0);

    this.gridlist1 = new ArrayList<Vertex>(Arrays.asList(this.a1, this.a2, this.a3));
    this.gridlist2 = new ArrayList<Vertex>(Arrays.asList(this.a4, this.a5, this.a6));
    this.gridlist3 = new ArrayList<Vertex>(Arrays.asList(this.a7, this.a8, this.a9));
    this.grid3 = new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(this.gridlist1, this.gridlist2, this.gridlist3));

    this.cameFromEdge = new HashMap<Vertex, Vertex>();
    this.cameFromEdge.put(this.a2, this.a1);
    this.cameFromEdge.put(this.a3, this.a2);
    this.cameFromEdge.put(this.a4, this.a3);
    this.cameFromEdge.put(this.a5, this.a4);
    this.cameFromEdge.put(this.a6, this.a4);

    this.reconstructedpath = new ArrayList<Vertex>(
        Arrays.asList(this.a9, this.a3, this.a2, this.a1));

  }
 
  void testGame(Tester t) {
    world = new MazeWorld();
    world.bigBang(world.width * 10, world.height * 10, 0.001);
  }

  // tests initMaze
  void testInitMaze(Tester t) {
    this.initData();
    for (int x = 0; x < this.world.initVertices().size(); x++) {
      for (int y = 0; y < this.world.initVertices().get(x).size(); y++) {
        Vertex v = this.world.initVertices().get(x).get(y);
        ArrayList<Edge> e = new ArrayList<Edge>();
        t.checkExpect(v, new Vertex(x, y, e));
      }
    }
  }

  // tests sortbyEdgeWeight
  void testSortByEdgeWeight(Tester t) {
    this.initData();
    t.checkExpect(this.world.sortByEdgeWeight(this.edges), this.sortededges);
  }

  // tests kruskal
  void testKruskal(Tester t) {
    this.initData();
    t.checkExpect(this.world.edgesInTree.size(), world.height * world.width - 1);
    // edgesinTree are sorted
    for (int i = 0; i < this.world.edgesInTree.size() - 1; i++) {
      Edge first = this.world.edgesInTree.get(i);
      Edge second = this.world.edgesInTree.get(i + 1);
      t.checkExpect(first.weight < second.weight, true);
    }
  }

  // tests find
  void testFind(Tester t) {
    this.initData();
    t.checkExpect(this.world.find(this.linkedrepresentatives, this.a), this.e);
  }

  // tests union
  void testUnion(Tester t) {
    this.initData();
    this.world.union(this.representatives, this.a, this.b);
    t.checkExpect(this.representatives.get(this.a), this.b);
  }

  // tests configureEdges
  void testConfigureEdges(Tester t) {
    this.initData();
    this.world.configureEdges();
    for (Edge e : this.world.edgesInTree) {
      t.checkExpect(e.from.outEdges.contains(e), true);
      t.checkExpect(e.to.outEdges.contains(e), true);
    }
  }

  // tests canTravelTo
  void testCanTravelTo(Tester t) {
    this.initData();
    t.checkExpect(this.world.canTravelTo(0, 0, 0, 0), true);
  }

  // tests onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.initData();
    this.world.onKeyEvent("up");
    t.checkOneOf(this.player.y, -1, 0);
    this.world.onKeyEvent("down");
    t.checkOneOf(this.player.y, 0, 1);
    this.world.onKeyEvent("left");
    t.checkOneOf(this.player.x, -1, 0);
    this.world.onKeyEvent("right");
    t.checkOneOf(this.player.x, 0, 1);
    this.world.onKeyEvent("b");
    t.checkExpect(this.world.explored.size() > 0, true);
    t.checkExpect(this.world.path.size() > 0, true);
    this.world.onKeyEvent("n");
    t.checkExpect(this.world.explored.size() == 0, false);
    t.checkExpect(this.world.path.size() == 0, false);
    t.checkExpect(this.world.counter == -1, false);
    this.world.onKeyEvent("d");
    t.checkExpect(this.world.explored.size() > 0, true);
    t.checkExpect(this.world.path.size() > 0, true);
  }

  // tests breadth
  void testFindPath(Tester t) {
    this.initData();
    this.world.performSearch("d");
    t.checkExpect(this.world.explored.size() > 0, true);
    t.checkExpect(this.world.explored.size() < world.height * world.width, true);
    t.checkExpect(this.world.path.get(0),
        this.world.arrOfVertices.get(this.world.arrOfVertices.size() 
            - 1).get(this.world.arrOfVertices.get(0).size() - 1));
    t.checkExpect(this.world.path.get(this.world.path.size() 
        - 1), this.world.arrOfVertices.get(0).get(0));
    this.world.performSearch("b");
    t.checkExpect(this.world.explored.size() > 0, true);
    t.checkExpect(this.world.explored.size() < world.height * world.width, true);
    t.checkExpect(this.world.path.get(0),
        this.world.arrOfVertices.get(this.world.arrOfVertices.size() 
            - 1).get(this.world.arrOfVertices.get(0).size() - 1));
    t.checkExpect(this.world.path.get(this.world.path.size() - 1), 
        this.world.arrOfVertices.get(0).get(0));
  }

  // tests reversePath
  void testReversePath(Tester t) {
    this.initData();
    this.world.arrOfVertices = this.grid3;
    this.world.reversePath(this.cameFromEdge, this.a4);
    t.checkExpect(this.world.path, this.reconstructedpath);
  }

  // tests equals
  boolean testEquals(Tester t) {
    this.initData();
    return t.checkExpect(this.a.equals(this.a), true) && t.checkExpect(this.a.equals(this.b), false)
        && t.checkExpect(this.aToB.equals(this.aToB), true)
        && t.checkExpect(this.aToB.equals(this.bToC), false);
  }

  // tests drawVertex
  boolean testDrawVertex(Tester t) {
    this.initData();
    return t.checkExpect(this.a.drawVertex(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
  }

  // tests drawExploredVertex
  boolean testDrawExploredVertex(Tester t) {
    this.initData();
    this.a.isScanned = true;
    return t.checkExpect(this.a.drawExploredVertex(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.CYAN))
        && t.checkExpect(this.b.drawExploredVertex(), new EmptyImage());
  }

  // tests drawShortestPath
  boolean testDrawShortestPath(Tester t) {
    this.initData();
    this.a.isInSolution = true;
    return t.checkExpect(this.a.drawShortestPath(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.blue))
        && t.checkExpect(this.b.drawShortestPath(), new EmptyImage());
  }

  // tests drawPlayerPath
  boolean testDrawPlayerPath(Tester t) {
    this.initData();
    return t.checkExpect(this.a.drawPlayerPath(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.pink));
  }

  // tests DrawEdge
  boolean testDrawEdge(Tester t) {
    this.initData();
    return t.checkExpect(this.aToB.drawEdge(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.gray));
  }

  // tests hashCode
  void testHashCode(Tester t) {
    this.initData();
    Object o = new Vertex(0, 0, new ArrayList<Edge>());
    t.checkExpect(this.a.equals(o), true);
    t.checkExpect(this.a.hashCode() == o.hashCode(), true);
    t.checkExpect(this.b.equals(o), false);
  }
  
  // tests compareTo
  void testCompareTo(Tester t) {
    t.checkExpect(this.eToC.compareTo(cToD), -1);
    t.checkExpect(this.aToB.compareTo(cToD), 1);
    t.checkExpect(this.aToE.compareTo(bToF), 0);
  }
}

