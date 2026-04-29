package com.phylotree.service;

import com.phylotree.model.SequenceRequest.SequenceEntry;
import com.phylotree.model.TreeNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * PhyloTreeService — bridges the Spring REST layer to the core algorithms.
 *
 * The actual algorithm logic lives in two self-contained inner classes that
 * mirror the logic from the uploaded com.phylo project:
 *
 *   UPGMAAlgorithm      — simple, assumes constant evolution rate
 *   NeighborJoiningAlgorithm — corrects for rate variation (more accurate)
 *
 * Distance metric: Hamming / p-distance (fraction of differing positions).
 */
@Service
public class PhyloTreeService {

    // ── public entry point ────────────────────────────────────────────────────

    public TreeNode buildTree(List<SequenceEntry> sequences, String algorithm) {
        int n = sequences.size();
        if (n == 1) return new TreeNode(sequences.get(0).getName());

        String[] names = new String[n];
        double[][] dist = new double[n][n];

        for (int i = 0; i < n; i++) names[i] = sequences.get(i).getName();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double d = pDistance(
                    sequences.get(i).getSequence(),
                    sequences.get(j).getSequence()
                );
                dist[i][j] = d;
                dist[j][i] = d;
            }
        }

        return algorithm.equals("nj")
            ? new NeighborJoiningAlgorithm().build(names, dist)
            : new UPGMAAlgorithm().build(names, dist);
    }

    // ── p-distance (Hamming) ──────────────────────────────────────────────────

    private static double pDistance(String a, String b) {
        a = a.toUpperCase().replaceAll("\\s", "");
        b = b.toUpperCase().replaceAll("\\s", "");
        int len = Math.max(a.length(), b.length());
        if (len == 0) return 0.0;
        int diffs = 0;
        for (int k = 0; k < len; k++) {
            char ca = k < a.length() ? a.charAt(k) : '-';
            char cb = k < b.length() ? b.charAt(k) : '-';
            if (ca != cb) diffs++;
        }
        return (double) diffs / len;
    }

    // ── UPGMA ─────────────────────────────────────────────────────────────────

    /**
     * UPGMA — Unweighted Pair Group Method with Arithmetic Mean.
     *
     * Steps:
     *  1. Each sequence starts as its own cluster.
     *  2. Find the two closest clusters by distance.
     *  3. Merge them: new parent node, branch lengths = half-merge-distance minus
     *     existing height of each cluster.
     *  4. Update the distance matrix using weighted averages.
     *  5. Repeat until one cluster remains.
     *
     * Assumes all sequences evolve at the same rate (molecular clock).
     */
    private static class UPGMAAlgorithm {

        TreeNode build(String[] names, double[][] initDist) {
            int n = names.length;
            List<TreeNode> clusters = new ArrayList<>();
            List<Integer>  sizes    = new ArrayList<>();
            List<Double>   heights  = new ArrayList<>();
            double[][] d = deepCopy(initDist, n);

            for (int i = 0; i < n; i++) {
                clusters.add(new TreeNode(names[i]));
                sizes.add(1);
                heights.add(0.0);
            }

            while (clusters.size() > 1) {
                // Find closest pair
                int bI = 0, bJ = 1;
                double bD = d[0][1];
                for (int i = 0; i < clusters.size(); i++)
                    for (int j = i + 1; j < clusters.size(); j++)
                        if (d[i][j] < bD) { bD = d[i][j]; bI = i; bJ = j; }

                double newHeight = bD / 2.0;
                TreeNode parent = new TreeNode(null);
                TreeNode left   = clusters.get(bI);
                TreeNode right  = clusters.get(bJ);
                left.setBranchLength(newHeight  - heights.get(bI));
                right.setBranchLength(newHeight - heights.get(bJ));
                parent.addChild(left);
                parent.addChild(right);

                int sI = sizes.get(bI), sJ = sizes.get(bJ);
                int newN = clusters.size() - 1;
                double[][] nd = new double[newN][newN];
                List<TreeNode> nc = new ArrayList<>();
                List<Integer>  ns = new ArrayList<>();
                List<Double>   nh = new ArrayList<>();

                int[] map = new int[clusters.size()];
                int idx = 0;
                for (int k = 0; k < clusters.size(); k++) {
                    if (k == bI || k == bJ) { map[k] = -1; continue; }
                    map[k] = idx++;
                    nc.add(clusters.get(k));
                    ns.add(sizes.get(k));
                    nh.add(heights.get(k));
                }
                int mi = idx;
                nc.add(parent); ns.add(sI + sJ); nh.add(newHeight);

                for (int a = 0; a < clusters.size(); a++) {
                    if (map[a] == -1) continue;
                    for (int b = a + 1; b < clusters.size(); b++) {
                        if (map[b] == -1) continue;
                        nd[map[a]][map[b]] = d[a][b];
                        nd[map[b]][map[a]] = d[a][b];
                    }
                    double avg = (sI * d[a][bI] + sJ * d[a][bJ]) / (sI + sJ);
                    nd[map[a]][mi] = avg;
                    nd[mi][map[a]] = avg;
                }

                clusters = nc; sizes = ns; heights = nh; d = nd;
            }
            return clusters.get(0);
        }
    }

    // ── Neighbor-Joining ──────────────────────────────────────────────────────

    /**
     * Neighbor-Joining (Saitou & Nei, 1987).
     *
     * Unlike UPGMA, NJ does NOT assume a molecular clock.
     * It picks merges based on the Q-criterion:
     *
     *   Q(i,j) = (n-2)*d(i,j) - sum_of_row(i) - sum_of_row(j)
     *
     * This corrects for branches that evolve faster than others,
     * making it more accurate for real biological data.
     */
    private static class NeighborJoiningAlgorithm {

        TreeNode build(String[] names, double[][] initDist) {
            int n = names.length;
            List<TreeNode> nodes = new ArrayList<>();
            for (int i = 0; i < n; i++) nodes.add(new TreeNode(names[i]));
            double[][] d = deepCopy(initDist, n);

            while (nodes.size() > 2) {
                int m = nodes.size();
                double[] r = new double[m];
                for (int i = 0; i < m; i++)
                    for (int j = 0; j < m; j++) r[i] += d[i][j];

                int bI = 0, bJ = 1;
                double bQ = Double.POSITIVE_INFINITY;
                for (int i = 0; i < m; i++)
                    for (int j = i + 1; j < m; j++) {
                        double q = (m - 2) * d[i][j] - r[i] - r[j];
                        if (q < bQ) { bQ = q; bI = i; bJ = j; }
                    }

                double dij = d[bI][bJ];
                double bi  = 0.5 * dij + (r[bI] - r[bJ]) / (2.0 * (m - 2));
                double bj  = dij - bi;
                if (bi < 0) bi = 0;
                if (bj < 0) bj = 0;

                TreeNode parent = new TreeNode(null);
                TreeNode left   = nodes.get(bI);
                TreeNode right  = nodes.get(bJ);
                left.setBranchLength(bi);
                right.setBranchLength(bj);
                parent.addChild(left);
                parent.addChild(right);

                int newM = m - 1;
                double[][] nd = new double[newM][newM];
                List<TreeNode> nn = new ArrayList<>();
                int[] map = new int[m];
                int idx = 0;
                for (int k = 0; k < m; k++) {
                    if (k == bI || k == bJ) { map[k] = -1; continue; }
                    map[k] = idx++;
                    nn.add(nodes.get(k));
                }
                int mi = idx;
                nn.add(parent);

                for (int a = 0; a < m; a++) {
                    if (map[a] == -1) continue;
                    for (int b = a + 1; b < m; b++) {
                        if (map[b] == -1) continue;
                        nd[map[a]][map[b]] = d[a][b];
                        nd[map[b]][map[a]] = d[a][b];
                    }
                    double dau = 0.5 * (d[a][bI] + d[a][bJ] - dij);
                    nd[map[a]][mi] = dau;
                    nd[mi][map[a]] = dau;
                }
                nodes = nn; d = nd;
            }

            TreeNode root = new TreeNode(null);
            TreeNode a = nodes.get(0), b = nodes.get(1);
            double dist = d[0][1];
            a.setBranchLength(dist / 2.0);
            b.setBranchLength(dist / 2.0);
            root.addChild(a);
            root.addChild(b);
            return root;
        }
    }

    // ── shared util ───────────────────────────────────────────────────────────

    private static double[][] deepCopy(double[][] m, int n) {
        double[][] out = new double[n][n];
        for (int i = 0; i < n; i++) System.arraycopy(m[i], 0, out[i], 0, n);
        return out;
    }
}
