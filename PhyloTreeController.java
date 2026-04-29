package com.phylotree.controller;

import com.phylotree.model.SequenceRequest;
import com.phylotree.model.TreeNode;
import com.phylotree.service.PhyloTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller – exposes two endpoints:
 *
 *   POST /api/tree          → build tree (UPGMA or Neighbor-Joining)
 *   GET  /api/health        → quick sanity check
 *
 * The "algorithm" field in the request body selects which method to use.
 * If omitted it defaults to "upgma".
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PhyloTreeController {

    @Autowired
    private PhyloTreeService phyloTreeService;

    /**
     * Build a phylogenetic tree.
     *
     * Request JSON:
     * {
     *   "algorithm": "upgma",          ← or "nj" for Neighbor-Joining
     *   "sequences": [
     *     { "name": "Human",  "sequence": "ATCGATCG" },
     *     { "name": "Chimp",  "sequence": "ATCGTTCG" }
     *   ]
     * }
     */
    @PostMapping("/tree")
    public ResponseEntity<?> buildTree(@RequestBody SequenceRequest request) {

        if (request.getSequences() == null || request.getSequences().size() < 2) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please provide at least 2 sequences."));
        }

        String algo = request.getAlgorithm() == null ? "upgma"
                      : request.getAlgorithm().toLowerCase().trim();

        if (!algo.equals("upgma") && !algo.equals("nj")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "algorithm must be 'upgma' or 'nj'."));
        }

        try {
            TreeNode root = phyloTreeService.buildTree(request.getSequences(), algo);
            return ResponseEntity.ok(root);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Tree building failed: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PhyloTree backend is running ✅");
    }
}
