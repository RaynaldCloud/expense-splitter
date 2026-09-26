package com.raynald.splitter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        new WebServer(new Group(), port).start();
        System.out.println("Expense Splitter is running at http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
    }
}