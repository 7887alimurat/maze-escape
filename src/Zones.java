import java.util.Arrays;

/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] forward = Graphs.slidesFrom(s);
        int[][] reverse = Graphs.slidesInto(s);

        // Первый проход: DFS по прямым рёбрам, записываем порядок завершения.
        boolean[] seen = new boolean[n];
        int[] finish = new int[n];
        int finished = 0;
        int[] stack = new int[n];
        int[] next = new int[n];

        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;

            int top = 0;
            stack[0] = start;
            next[0] = 0;
            seen[start] = true;

            while (top >= 0) {
                int u = stack[top];

                if (next[top] < forward[u].length) {
                    int edge = forward[u][next[top]++];
                    int v = s.to()[edge];

                    if (!seen[v]) {
                        seen[v] = true;
                        stack[++top] = v;
                        next[top] = 0;
                    }
                } else {
                    finish[finished++] = u;
                    top--;
                }
            }
        }

        // Второй проход: в обратном порядке завершения по обратным рёбрам.
        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zone = 0;
        int[] todo = new int[n];

        for (int i = finished - 1; i >= 0; i--) {
            int start = finish[i];
            if (zoneOf[start] != -1) continue;

            int top = 0;
            todo[0] = start;
            zoneOf[start] = zone;

            while (top >= 0) {
                int u = todo[top--];

                for (int edge : reverse[u]) {
                    int v = s.from()[edge];
                    if (zoneOf[v] == -1) {
                        zoneOf[v] = zone;
                        todo[++top] = v;
                    }
                }
            }
            zone++;
        }

        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        long[][] edgeCost = new long[zoneCount][zoneCount];
        for (long[] row : edgeCost) Arrays.fill(row, INF);

        for (int i = 0; i < s.count(); i++) {
            int fromZone = zoneOf[s.from()[i]];
            int toZone = zoneOf[s.to()[i]];

            if (fromZone != toZone) {
                edgeCost[fromZone][toZone] =
                        Math.min(edgeCost[fromZone][toZone], s.time()[i]);
            }
        }

        int start = zoneOf[0];
        int target = zoneOf[s.n() - 1];

        long[] distance = new long[zoneCount];
        Arrays.fill(distance, INF);
        distance[start] = 0;

        // Длина самой длинной цепочки зон в DAG.
        long[] longest = new long[zoneCount];
        Arrays.fill(longest, 1);

        // Номера зон уже идут в топологическом порядке.
        for (int from = 0; from < zoneCount; from++) {
            for (int to = from + 1; to < zoneCount; to++) {
                if (edgeCost[from][to] == INF) continue;

                if (distance[from] != INF) {
                    distance[to] = Math.min(
                            distance[to],
                            distance[from] + edgeCost[from][to]
                    );
                }

                longest[to] = Math.max(longest[to], longest[from] + 1);
            }
        }

        long stages = 0;
        for (long length : longest) {
            stages = Math.max(stages, length);
        }

        return new long[]{stages, distance[target]};
    }
}
