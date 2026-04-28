package com.training.java817.module2.textblocks;

/**
 * =============================================================================
 * MODULE 2 – TEXT BLOCKS (JEP 378, Java 15 GA)
 * =============================================================================
 *
 * THEORY
 * ------
 * A text block is a multi-line string literal that begins with """ (on a line
 * by itself) and ends with """.  The compiler:
 *   1. Normalises line endings to \n.
 *   2. Strips common leading whitespace (incidental whitespace).
 *   3. Interprets escape sequences in the content.
 *
 * PROBLEM SOLVED
 * --------------
 * 1. Eliminates the \n, + and \" noise from embedded strings.
 * 2. Readable SQL, JSON, HTML, and XML directly in the source file.
 * 3. Reduces the risk of accidentally corrupting a multi-line string during
 *    maintenance (missing +, extra space before line continuation).
 * 4. Consistent indentation: leading spaces relative to the opening delimiter
 *    are removed automatically.
 *
 * EXTRA ESCAPE SEQUENCES (Java 14+)
 *   \<line-terminator>  – suppress newline (join continuation line)
 *   \s                  – explicit space (prevents trailing-whitespace trimming)
 */
public class TextBlockExamples {

    // =========================================================================
    // BEFORE – Traditional concatenated SQL string
    // =========================================================================

    /** Fragile: easy to forget a space at start/end of each fragment. */
    public String buildTradeQuery_Before(String status) {
        return "SELECT t.trade_id, t.symbol, t.notional, " +
               "       t.counterparty_id, t.settlement_date " +
               "FROM   trades t " +
               "JOIN   counterparties c ON c.id = t.counterparty_id " +
               "WHERE  t.status = '" + status + "' " +
               "  AND  t.settlement_date >= CURRENT_DATE " +
               "ORDER  BY t.settlement_date ASC";
    }

    // =========================================================================
    // AFTER – Text block SQL
    // =========================================================================

    /** Clean, readable, and indented to match the surrounding code. */
    public String buildTradeQuery_After(String status) {
        return """
                SELECT t.trade_id,
                       t.symbol,
                       t.notional,
                       t.counterparty_id,
                       t.settlement_date
                FROM   trades t
                JOIN   counterparties c ON c.id = t.counterparty_id
                WHERE  t.status = '%s'
                  AND  t.settlement_date >= CURRENT_DATE
                ORDER  BY t.settlement_date ASC
                """.formatted(status);
    }

    // =========================================================================
    // JSON payload
    // =========================================================================

    public String buildTradeJson_Before(String tradeId, String symbol, double notional) {
        return "{\n" +
               "  \"tradeId\": \"" + tradeId + "\",\n" +
               "  \"symbol\": \"" + symbol + "\",\n" +
               "  \"notional\": " + notional + ",\n" +
               "  \"status\": \"PENDING\"\n" +
               "}";
    }

    public String buildTradeJson_After(String tradeId, String symbol, double notional) {
        return """
                {
                  "tradeId": "%s",
                  "symbol": "%s",
                  "notional": %s,
                  "status": "PENDING"
                }
                """.formatted(tradeId, symbol, notional);
    }

    // =========================================================================
    // HTML template
    // =========================================================================

    public String buildTradeConfirmationHtml_Before(String tradeId, String status) {
        return "<html>\n" +
               "  <body>\n" +
               "    <h1>Trade Confirmation</h1>\n" +
               "    <p>Trade ID: " + tradeId + "</p>\n" +
               "    <p>Status: " + status + "</p>\n" +
               "  </body>\n" +
               "</html>";
    }

    public String buildTradeConfirmationHtml_After(String tradeId, String status) {
        return """
                <html>
                  <body>
                    <h1>Trade Confirmation</h1>
                    <p>Trade ID: %s</p>
                    <p>Status: %s</p>
                  </body>
                </html>
                """.formatted(tradeId, status);
    }

    // =========================================================================
    // XML document
    // =========================================================================

    public String buildFpmlMessage_After(String tradeId, String symbol, double notional) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <FpML version="5.11">
                  <trade>
                    <tradeHeader>
                      <partyTradeIdentifier>
                        <tradeId>%s</tradeId>
                      </partyTradeIdentifier>
                    </tradeHeader>
                    <product>
                      <equity>
                        <underlyer>
                          <instrument>%s</instrument>
                        </underlyer>
                        <notionalAmount>%s</notionalAmount>
                      </equity>
                    </product>
                  </trade>
                </FpML>
                """.formatted(tradeId, symbol, notional);
    }

    // =========================================================================
    // Indentation control
    // =========================================================================

    /**
     * indent(n) – Java 12: adjusts indentation by n spaces and ensures \n at end.
     * stripIndent() – Java 15: strips incidental leading whitespace (same as text block).
     * translateEscapes() – Java 15: translates escape sequences in a regular string.
     */
    public String indentedQuery() {
        String raw = """
                SELECT *
                FROM trades
                WHERE status = 'PENDING'
                """;
        return raw.indent(4);   // add 4 extra spaces to each line
    }

    /**
     * Line continuation escape \<newline> – prevents a newline from being added.
     */
    public String singleLineFromBlock() {
        return """
                SELECT trade_id, symbol, notional \
                FROM trades \
                WHERE status = 'EXECUTED'
                """;
    }

    // demo main
    public static void main(String[] args) {
        TextBlockExamples ex = new TextBlockExamples();
        System.out.println("=== SQL Before ===");
        System.out.println(ex.buildTradeQuery_Before("EXECUTED"));
        System.out.println("=== SQL After ===");
        System.out.println(ex.buildTradeQuery_After("EXECUTED"));
        System.out.println("=== JSON After ===");
        System.out.println(ex.buildTradeJson_After("T001", "AAPL", 1_500_000.0));
    }
}
