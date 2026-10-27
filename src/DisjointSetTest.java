/**
 * Checks of Mission 0. Run:  java -cp out DisjointSetTest
 * Add your own checks below: a large chain, a union of a set with itself, count after many unions.
 */
public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        // 1. Union of a set with itself
        {
            DisjointSet s = new DisjointSet(4);
            check(!s.union(2, 2), "union(2, 2) returns false");
            check(s.count() == 4, "count is still 4 after union(2, 2)");
            s.union(0, 1);
            check(!s.union(0, 1), "union(0, 1) on the same pair returns false");
            check(s.count() == 3, "count did not change after a failed union");
        }

        // 2. Count after many unions
        {
            DisjointSet m = new DisjointSet(10);
            m.union(0, 1); m.union(2, 3); m.union(4, 5); m.union(6, 7); m.union(8, 9);
            check(m.count() == 5, "5 sets after 5 unions of pairs");
            m.union(1, 2); m.union(5, 6);
            check(m.count() == 3, "3 sets: {0,1,2,3} {4,5,6,7} {8,9}");
            check(!m.connected(0, 5), "0 and 5 are in different sets");
            m.union(3, 8);
            check(m.count() == 2, "2 sets after joining {0..3} and {8,9}");
            check(m.union(0, 7), "union(0, 7) joins the last two sets");
            check(m.count() == 1, "1 set at the end");
            check(!m.union(4, 9), "no more successful unions: everything is one set");
        }

        // 3. Large chain (also checks that find does not overflow the stack)
        {
            int n = 1_000_000;
            DisjointSet c = new DisjointSet(n);
            boolean allTrue = true;
            for (int i = 0; i < n - 1; i++) {
                if (!c.union(i, i + 1)) allTrue = false;
            }
            check(allTrue, "every union in the chain returned true");
            check(c.count() == 1, "chain of 1,000,000 cells is one set");
            check(c.connected(0, n - 1), "first and last cell are connected");
            check(c.find(0) == c.find(n - 1), "first and last cell have the same root");
        }

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
    }
}
