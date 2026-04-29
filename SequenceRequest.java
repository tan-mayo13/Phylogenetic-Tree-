package com.phylotree.model;

import java.util.List;

/**
 * JSON request body sent by the frontend.
 *
 * {
 *   "algorithm": "upgma",   ← "upgma" or "nj"
 *   "sequences": [
 *     { "name": "Human", "sequence": "ATCGATCG" },
 *     ...
 *   ]
 * }
 */
public class SequenceRequest {

    private String algorithm = "upgma";   // default
    private List<SequenceEntry> sequences;

    public String getAlgorithm()                      { return algorithm; }
    public void   setAlgorithm(String algorithm)      { this.algorithm = algorithm; }

    public List<SequenceEntry> getSequences()         { return sequences; }
    public void setSequences(List<SequenceEntry> s)   { this.sequences = s; }

    // ── inner DTO ────────────────────────────────────────────────────────────
    public static class SequenceEntry {
        private String name;
        private String sequence;

        public String getName()                  { return name; }
        public void   setName(String name)       { this.name = name; }
        public String getSequence()              { return sequence; }
        public void   setSequence(String seq)    { this.sequence = seq; }
    }
}
