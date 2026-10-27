import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet sets = new DisjointSet(w.cells());
        int[] chosen = new int[Math.max(0, w.cells() - 1)];
        int size = 0;

        for (int k = 0; k < w.count() && size < chosen.length; k++) {
            if (sets.union(w.a()[k], w.b()[k])) {
                chosen[size++] = k;
            }
        }
        return Arrays.copyOf(chosen, size);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        DisjointSet sets = new DisjointSet(w.cells());
        Integer[] order = new Integer[w.count()];
        for (int k = 0; k < order.length; k++) order[k] = k;
        Arrays.sort(order, (x, y) -> {
            int byCost = Integer.compare(w.cost()[x], w.cost()[y]);
            return byCost != 0 ? byCost : Integer.compare(x, y);
        });

        int[] chosen = new int[Math.max(0, w.cells() - 1)];
        int size = 0;
        for (int k : order) {
            if (sets.union(w.a()[k], w.b()[k])) {
                chosen[size++] = k;
                if (size == chosen.length) break;
            }
        }
        return Arrays.copyOf(chosen, size);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    private record Edge(int from, int to, int cost) {}

    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n <= 1) return 0;

        int[] degree = new int[n];
        for (int k = 0; k < w.count(); k++) {
            degree[w.a()[k]]++;
            degree[w.b()[k]]++;
        }
        int[][] incident = new int[n][];
        for (int v = 0; v < n; v++) incident[v] = new int[degree[v]];
        Arrays.fill(degree, 0);
        for (int k = 0; k < w.count(); k++) {
            incident[w.a()[k]][degree[w.a()[k]]++] = k;
            incident[w.b()[k]][degree[w.b()[k]]++] = k;
        }

        boolean[] added = new boolean[n];
        PriorityQueue<Edge> queue = new PriorityQueue<>((x, y) -> {
            int byCost = Integer.compare(x.cost(), y.cost());
            if (byCost != 0) return byCost;
            int byFrom = Integer.compare(x.from(), y.from());
            return byFrom != 0 ? byFrom : Integer.compare(x.to(), y.to());
        });
        added[0] = true;
        for (int k : incident[0]) {
            int other = w.a()[k] == 0 ? w.b()[k] : w.a()[k];
            queue.add(new Edge(0, other, w.cost()[k]));
        }

        long total = 0;
        int reached = 1;
        while (!queue.isEmpty() && reached < n) {
            Edge e = queue.remove();
            if (added[e.to()]) continue;
            added[e.to()] = true;
            reached++;
            total += e.cost();
            for (int k : incident[e.to()]) {
                int other = w.a()[k] == e.to() ? w.b()[k] : w.a()[k];
                if (!added[other]) queue.add(new Edge(e.to(), other, w.cost()[k]));
            }
        }
        if (reached != n) throw new IllegalArgumentException("Wall graph is disconnected");
        return total;
    }
}
