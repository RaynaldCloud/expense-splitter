package com.raynald.splitter;

import java.util.List;
import java.util.Map;

/** Builds the web page as HTML text, from the group's current state. */
public final class HtmlPage {

    private HtmlPage() {}

    private static final String STYLE = """
            <style>
              body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                     background: #f5f6f8; color: #1f2933; margin: 0; }
              main { max-width: 900px; margin: 0 auto; padding: 24px 16px 48px; }
              h1 { margin: 0 0 16px; }
              h2 { font-size: 1.1rem; margin: 0 0 12px; }
              .card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px;
                      padding: 18px; margin-bottom: 16px; }
              .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 16px; }
              label { display: block; font-size: .85rem; color: #6b7280; margin-top: 8px; }
              input, select, button { font: inherit; padding: 8px 10px; border: 1px solid #e5e7eb; border-radius: 6px; }
              input[type=text], input[type=number], select { width: 100%; box-sizing: border-box; }
              button { background: #0f766e; color: #fff; border: none; cursor: pointer; margin-top: 12px; }
              button.link { background: none; color: #b91c1c; margin: 0; padding: 2px 6px; }
              .checks label { display: inline-flex; gap: 4px; margin-right: 12px; color: #1f2933; }
              .table-wrap { overflow-x: auto; }
              table { width: 100%; border-collapse: collapse; font-size: .9rem; }
              th, td { text-align: left; padding: 8px 6px; border-bottom: 1px solid #e5e7eb; }
              .owed { color: #15803d; }
              .owes { color: #b91c1c; }
              .muted { color: #6b7280; }
              .flash { padding: 10px 14px; border-radius: 8px; margin-bottom: 16px; }
              .flash.ok { background: #dcfce7; color: #166534; }
              .flash.error { background: #fee2e2; color: #991b1b; }
            </style>
            """;

    public static String render(Group group, String message, String error) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            .append("<title>Expense Splitter</title>").append(STYLE)
            .append("</head><body><main><h1>Expense Splitter</h1>");

        if (message != null) {
            html.append("<div class=\"flash ok\">").append(escape(message)).append("</div>");
        }
        if (error != null) {
            html.append("<div class=\"flash error\">").append(escape(error)).append("</div>");
        }

        html.append("<div class=\"grid\">");
        appendPeople(html, group);
        appendExpenseForm(html, group);
        html.append("</div>");

        appendExpenses(html, group);

        html.append("<div class=\"grid\">");
        appendBalances(html, group);
        appendSettleUp(html, group);
        html.append("</div>");

        html.append("</main></body></html>");
        return html.toString();
    }

    private static void appendPeople(StringBuilder html, Group group) {
        html.append("<div class=\"card\"><h2>People</h2>");
        if (group.getMembers().isEmpty()) {
            html.append("<p class=\"muted\">No one yet. Add the people sharing costs.</p>");
        } else {
            html.append("<ul>");
            for (String member : group.getMembers()) {
                html.append("<li>").append(escape(member)).append("</li>");
            }
            html.append("</ul>");
        }
        html.append("""
                <form method="post" action="/members">
                  <label>Name <input type="text" name="name" required maxlength="40"></label>
                  <button type="submit">Add person</button>
                </form></div>
                """);
    }

    private static void appendExpenseForm(StringBuilder html, Group group) {
        html.append("<div class=\"card\"><h2>Add an expense</h2>");
        if (group.getMembers().isEmpty()) {
            html.append("<p class=\"muted\">Add people first.</p></div>");
            return;
        }
        html.append("<form method=\"post\" action=\"/expenses\">")
            .append("<label>Description <input type=\"text\" name=\"description\" required maxlength=\"60\"></label>")
            .append("<label>Amount (S$) <input type=\"number\" name=\"amount\" step=\"0.01\" min=\"0.01\" required></label>")
            .append("<label>Paid by <select name=\"paidBy\">");
        for (String member : group.getMembers()) {
            String safe = escape(member);
            html.append("<option value=\"").append(safe).append("\">").append(safe).append("</option>");
        }
        html.append("</select></label><label>Split between</label><div class=\"checks\">");
        for (String member : group.getMembers()) {
            String safe = escape(member);
            html.append("<label><input type=\"checkbox\" name=\"sharedBy\" value=\"").append(safe)
                .append("\" checked> ").append(safe).append("</label>");
        }
        html.append("</div><button type=\"submit\">Add expense</button></form></div>");
    }

    private static void appendExpenses(StringBuilder html, Group group) {
        html.append("<div class=\"card\"><h2>Expenses</h2>");
        List<Expense> expenses = group.getExpenses();
        if (expenses.isEmpty()) {
            html.append("<p class=\"muted\">No expenses yet.</p></div>");
            return;
        }
        html.append("<div class=\"table-wrap\"><table>")
            .append("<tr><th>What</th><th>Paid by</th><th>Amount</th><th>Split between</th><th></th></tr>");
        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            html.append("<tr><td>").append(escape(expense.getDescription())).append("</td>")
                .append("<td>").append(escape(expense.getPaidBy())).append("</td>")
                .append("<td>").append(Money.format(expense.getAmountCents())).append("</td>")
                .append("<td>").append(escape(String.join(", ", expense.getSharedBy()))).append("</td>")
                .append("<td><form method=\"post\" action=\"/expenses/delete\">")
                .append("<input type=\"hidden\" name=\"index\" value=\"").append(i).append("\">")
                .append("<button class=\"link\" type=\"submit\">Delete</button></form></td></tr>");
        }
        html.append("</table></div></div>");
    }

    private static void appendBalances(StringBuilder html, Group group) {
        html.append("<div class=\"card\"><h2>Balances</h2>");
        if (group.getMembers().isEmpty()) {
            html.append("<p class=\"muted\">Nothing to show yet.</p></div>");
            return;
        }
        html.append("<ul>");
        for (Map.Entry<String, Long> entry : group.balances().entrySet()) {
            String name = escape(entry.getKey());
            long cents = entry.getValue();
            if (cents > 0) {
                html.append("<li class=\"owed\">").append(name).append(" is owed ")
                    .append(Money.format(cents)).append("</li>");
            } else if (cents < 0) {
                html.append("<li class=\"owes\">").append(name).append(" owes ")
                    .append(Money.format(-cents)).append("</li>");
            } else {
                html.append("<li class=\"muted\">").append(name).append(" is settled</li>");
            }
        }
        html.append("</ul></div>");
    }

    private static void appendSettleUp(StringBuilder html, Group group) {
        html.append("<div class=\"card\"><h2>Settle up</h2>");
        List<Payment> payments = SettlementCalculator.settle(group.balances());
        if (payments.isEmpty()) {
            html.append("<p class=\"muted\">Everyone is settled up.</p>");
        } else {
            html.append("<ol>");
            for (Payment payment : payments) {
                html.append("<li>").append(escape(payment.toString())).append("</li>");
            }
            html.append("</ol>");
        }
        html.append("</div>");
    }

    /** Makes user-entered text safe to put inside HTML. */
    static String escape(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}