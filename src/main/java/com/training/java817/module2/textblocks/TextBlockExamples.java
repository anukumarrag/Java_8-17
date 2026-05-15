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
    public String buildEmployeeQuery_Before(String status) {
        return "SELECT e.employee_id, e.name, e.salary, " +
               "       e.department_id, e.hire_date " +
               "FROM   employees e " +
               "JOIN   departments d ON d.id = e.department_id " +
               "WHERE  e.status = '" + status + "' " +
               "  AND  e.hire_date >= CURRENT_DATE " +
               "ORDER  BY e.hire_date ASC";
    }

    // =========================================================================
    // AFTER – Text block SQL
    // =========================================================================

    /** Clean, readable, and indented to match the surrounding code. */
    public String buildEmployeeQuery_After(String status) {
        return """
                SELECT e.employee_id,
                       e.name,
                       e.salary,
                       e.department_id,
                       e.hire_date
                FROM   employees e
                JOIN   departments d ON d.id = e.department_id
                WHERE  e.status = '%s'
                  AND  e.hire_date >= CURRENT_DATE
                ORDER  BY e.hire_date ASC
                """.formatted(status);
    }

    // =========================================================================
    // JSON payload
    // =========================================================================

    public String buildEmployeeJson_Before(String employeeId, String name, double salary) {
        return "{\n" +
               "  \"employeeId\": \"" + employeeId + "\",\n" +
               "  \"name\": \"" + name + "\",\n" +
               "  \"salary\": " + salary + ",\n" +
               "  \"status\": \"ONBOARDING\"\n" +
               "}";
    }

    public String buildEmployeeJson_After(String employeeId, String name, double salary) {
        return """
                {
                  "employeeId": "%s",
                  "name": "%s",
                  "salary": %s,
                  "status": "ONBOARDING"
                }
                """.formatted(employeeId, name, salary);
    }

    // =========================================================================
    // HTML template
    // =========================================================================

    public String buildEmployeeReportHtml_Before(String employeeId, String status) {
        return "<html>\n" +
               "  <body>\n" +
               "    <h1>Employee Report</h1>\n" +
               "    <p>Employee ID: " + employeeId + "</p>\n" +
               "    <p>Status: " + status + "</p>\n" +
               "  </body>\n" +
               "</html>";
    }

    public String buildEmployeeReportHtml_After(String employeeId, String status) {
        return """
                <html>
                  <body>
                    <h1>Employee Report</h1>
                    <p>Employee ID: %s</p>
                    <p>Status: %s</p>
                  </body>
                </html>
                """.formatted(employeeId, status);
    }

    // =========================================================================
    // XML document
    // =========================================================================

    public String buildEmployeeXml_After(String employeeId, String name, double salary) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <employees>
                  <employee>
                    <employeeHeader>
                      <employeeIdentifier>
                        <id>%s</id>
                      </employeeIdentifier>
                    </employeeHeader>
                    <details>
                      <profile>
                        <name>%s</name>
                        <salary>%s</salary>
                      </profile>
                    </details>
                  </employee>
                </employees>
                """.formatted(employeeId, name, salary);
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
                FROM employees
                WHERE status = 'ACTIVE'
                """;
        return raw.indent(4);   // add 4 extra spaces to each line
    }

    /**
     * Line continuation escape \<newline> – prevents a newline from being added.
     */
    public String singleLineFromBlock() {
        return """
                SELECT employee_id, name, salary \
                FROM employees \
                WHERE status = 'ACTIVE'
                """;
    }

    // demo main
    public static void main(String[] args) {
        TextBlockExamples ex = new TextBlockExamples();
        System.out.println("=== SQL Before ===");
        System.out.println(ex.buildEmployeeQuery_Before("ACTIVE"));
        System.out.println("=== SQL After ===");
        System.out.println(ex.buildEmployeeQuery_After("ACTIVE"));
        System.out.println("=== JSON After ===");
        System.out.println(ex.buildEmployeeJson_After("E001", "Alice", 95_000.0));
    }
}
