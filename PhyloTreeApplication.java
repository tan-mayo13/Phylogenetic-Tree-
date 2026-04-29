package com.phylotree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PhyloTreeApplication {
    public static void main(String[] args) {
        SpringApplication.run(PhyloTreeApplication.class, args);
        System.out.println("✅ PhyloTree v2 running → http://localhost:8080");
        System.out.println("   Algorithms available: UPGMA, Neighbor-Joining");
    }
}
