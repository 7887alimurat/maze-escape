import java.util.Arrays;

/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int[][] graph = Graphs.open(w, broken);
        int n = w.cells();
        int start = 0;
        int exit = n - 1;

        int deadEnds = 0;
        for (int[] neighbours : graph) {
            if (neighbours.length == 1) deadEnds++;
        }

        boolean[] seen = new boolean[n];
        int[] parent = new int[n];
        Arrays.fill(parent, -1);

        // Итеративный DFS: stack — клетки, next — индекс следующего направления.
        int[] stack = new int[n];
        int[] next = new int[n];
        int top = 0;
        int visited = 1;
        stack[0] = start;
        seen[start] = true;

        while (top >= 0 && !seen[exit]) {
            int cell = stack[top];
            int row = cell / w.cols();
            int col = cell % w.cols();
            int[] wanted = {
                    cell - w.cols(), // вверх
                    cell + 1,        // вправо
                    cell + w.cols(), // вниз
                    cell - 1         // влево
            };

            boolean moved = false;
            while (next[top] < 4) {
                int direction = next[top]++;
                int candidate = wanted[direction];

                if (direction == 0 && row == 0) continue;
                if (direction == 1 && col == w.cols() - 1) continue;
                if (direction == 2 && row == w.rows() - 1) continue;
                if (direction == 3 && col == 0) continue;

                if (!contains(graph[cell], candidate) || seen[candidate]) continue;

                seen[candidate] = true;
                visited++;
                parent[candidate] = cell;
                stack[++top] = candidate;
                next[top] = 0;
                moved = true;
                break;
            }

            if (!moved && next[top] == 4) top--;
        }

        if (!seen[exit]) {
            return new Result(new int[0], visited, deadEnds);
        }

        int length = 1;
        for (int v = exit; v != start; v = parent[v]) length++;

        int[] path = new int[length];
        int v = exit;
        for (int i = length - 1; i >= 0; i--) {
            path[i] = v;
            if (v != start) v = parent[v];
        }

        return new Result(path, visited, deadEnds);
    }

    private static boolean contains(int[] values, int target) {
        for (int value : values) {
            if (value == target) return true;
        }
        return false;
    }
}