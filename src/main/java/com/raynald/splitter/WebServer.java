package com.raynald.splitter;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A small web server, using the one built into Java, that connects the browser to a Group. */
public class WebServer {

    private final Group group;
    private final HttpServer server;

    public WebServer(Group group, int port) throws IOException {
        this.group = group;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);  // every request goes to handle()
    }

    public void start() {
        server.start();
    }

    /** Looks at the method and path of each request and decides what to do. */
    private void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            if (method.equals("GET") && path.equals("/")) {
                Map<String, List<String>> query = parseForm(exchange.getRequestURI().getRawQuery());
                String html = HtmlPage.render(group, emptyToNull(first(query, "message")),
                        emptyToNull(first(query, "error")));
                sendHtml(exchange, 200, html);

            } else if (method.equals("POST") && path.equals("/members")) {
                Map<String, List<String>> form = readForm(exchange);
                String name = first(form, "name");
                group.addMember(name);
                redirect(exchange, "message", "Added " + name.trim());

            } else if (method.equals("POST") && path.equals("/expenses")) {
                Map<String, List<String>> form = readForm(exchange);
                Expense expense = new Expense(
                        first(form, "description"),
                        first(form, "paidBy"),
                        Money.parse(first(form, "amount")),
                        form.getOrDefault("sharedBy", List.of()));
                group.addExpense(expense);
                redirect(exchange, "message", "Added " + expense.getDescription());

            } else if (method.equals("POST") && path.equals("/expenses/delete")) {
                Map<String, List<String>> form = readForm(exchange);
                group.removeExpense(Integer.parseInt(first(form, "index")));
                redirect(exchange, "message", "Expense deleted");

            } else {
                sendHtml(exchange, 404, "<h1>Page not found</h1>");
            }
        } catch (IllegalArgumentException e) {
            // Any validation error from Money, Expense or Group is shown on the page
            redirect(exchange, "error", e.getMessage());
        }
    }

    /** Sends the browser back to the main page, with a message to display. */
    private void redirect(HttpExchange exchange, String kind, String text) throws IOException {
        String location = "/?" + kind + "=" + URLEncoder.encode(text, UTF_8);
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);  // 303 See Other: "now go GET this page"
        exchange.close();
    }

    private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(bytes);
        }
    }

    private static Map<String, List<String>> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
        return parseForm(body);
    }

    static Map<String, List<String>> parseForm(String encoded) {
        Map<String, List<String>> fields = new HashMap<>();
        if (encoded == null || encoded.isEmpty()) {
            return fields;
        }
        for (String pair : encoded.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], UTF_8) : "";
            fields.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return fields;
    }

    private static String first(Map<String, List<String>> fields, String key) {
        List<String> values = fields.get(key);
        return (values == null || values.isEmpty()) ? "" : values.get(0);
    }

    private static String emptyToNull(String text) {
        return text.isEmpty() ? null : text;
    }
}