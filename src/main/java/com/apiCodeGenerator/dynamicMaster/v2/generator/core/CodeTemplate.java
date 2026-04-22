package com.apiCodeGenerator.dynamicMaster.v2.generator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ============================================================
 * V2 CodeTemplate — Pure Functional Code Builder
 * ============================================================
 *
 * REPLACES StringBuilder with a composable line-list model.
 *
 * Design Principles:
 *  1. Immutability-friendly: each method returns a new template
 *     or appends to an internal list (controlled mutation)
 *  2. Fluent API for readability
 *  3. Supports conditional blocks, loops, and nested templates
 *  4. Renders to String via String.join("\n", lines)
 *     — zero manual string concatenation
 *
 * Usage:
 *   String code = CodeTemplate.of()
 *       .line("package com.example;")
 *       .blank()
 *       .line("public class Foo {")
 *       .indent(t -> t.line("private String name;"))
 *       .line("}")
 *       .render();
 */
public final class CodeTemplate {

    // Internal storage: ordered list of rendered lines
    private final List<String> lines;

    // Current indentation level (4 spaces per level)
    private final int indentLevel;

    // ─────────────────────────────────────────────────────────
    // Constructors / Factories
    // ─────────────────────────────────────────────────────────

    private CodeTemplate(int indentLevel) {
        this.lines = new ArrayList<>();
        this.indentLevel = indentLevel;
    }

    private CodeTemplate(List<String> lines, int indentLevel) {
        this.lines = new ArrayList<>(lines);
        this.indentLevel = indentLevel;
    }

    /** Create a fresh empty template at indent=0 */
    public static CodeTemplate of() {
        return new CodeTemplate(0);
    }

    /** Create with a specific indent level */
    public static CodeTemplate of(int indentLevel) {
        return new CodeTemplate(indentLevel);
    }

    // ─────────────────────────────────────────────────────────
    // Core Line Operations
    // ─────────────────────────────────────────────────────────

    /** Append a single line (auto-indented) */
    public CodeTemplate line(String content) {
        lines.add(indent() + content);
        return this;
    }

    /** Append a raw line (no auto-indent) */
    public CodeTemplate raw(String content) {
        lines.add(content);
        return this;
    }

    /** Append a blank line */
    public CodeTemplate blank() {
        lines.add("");
        return this;
    }

    /** Append multiple lines at once */
    public CodeTemplate lines(String... contents) {
        Arrays.stream(contents).forEach(this::line);
        return this;
    }

    /** Append all lines from another template */
    public CodeTemplate append(CodeTemplate other) {
        lines.addAll(other.lines);
        return this;
    }

    // ─────────────────────────────────────────────────────────
    // Indented Blocks
    // ─────────────────────────────────────────────────────────

    /**
     * Execute a block at +1 indent level.
     * The inner template's lines are added to this template.
     *
     * Example:
     *   .indent(t -> t
     *       .line("private String name;")
     *       .line("private Integer age;")
     *   )
     */
    public CodeTemplate indent(Function<CodeTemplate, CodeTemplate> block) {
        CodeTemplate inner = new CodeTemplate(indentLevel + 1);
        block.apply(inner);
        lines.addAll(inner.lines);
        return this;
    }

    /**
     * Execute a block at +2 indent levels (for nested structures).
     */
    public CodeTemplate indent2(Function<CodeTemplate, CodeTemplate> block) {
        CodeTemplate inner = new CodeTemplate(indentLevel + 2);
        block.apply(inner);
        lines.addAll(inner.lines);
        return this;
    }

    // ─────────────────────────────────────────────────────────
    // Conditional Operations
    // ─────────────────────────────────────────────────────────

    /**
     * Append a line only if condition is true.
     */
    public CodeTemplate lineIf(boolean condition, String content) {
        if (condition) line(content);
        return this;
    }

    /**
     * Execute a block only if condition is true.
     */
    public CodeTemplate when(boolean condition, Function<CodeTemplate, CodeTemplate> block) {
        if (condition) block.apply(this);
        return this;
    }

    /**
     * Execute one of two blocks based on condition.
     */
    public CodeTemplate whenElse(boolean condition,
                                 Function<CodeTemplate, CodeTemplate> ifBlock,
                                 Function<CodeTemplate, CodeTemplate> elseBlock) {
        return condition ? ifBlock.apply(this) : elseBlock.apply(this);
    }

    // ─────────────────────────────────────────────────────────
    // Collection / Loop Operations
    // ─────────────────────────────────────────────────────────

    /**
     * Iterate a collection, applying a block for each item.
     *
     * Example:
     *   .forEach(config.getFields(), (t, field) ->
     *       t.line("private " + field.getType() + " " + field.getName() + ";")
     *   )
     */
    public <T> CodeTemplate forEach(Collection<T> items, java.util.function.BiFunction<CodeTemplate, T, CodeTemplate> block) {
        for (T item : items) {
            block.apply(this, item);
        }
        return this;
    }

    /**
     * Map a collection to lines using a converter function.
     * Lines are joined with newline and appended.
     */
    public <T> CodeTemplate mapLines(Collection<T> items, Function<T, String> mapper) {
        items.stream()
                .map(item -> indent() + mapper.apply(item))
                .forEach(lines::add);
        return this;
    }

    /**
     * Map a filtered collection to lines.
     */
    public <T> CodeTemplate mapLinesIf(Collection<T> items,
                                       Predicate<T> filter,
                                       Function<T, String> mapper) {
        items.stream()
                .filter(filter)
                .map(item -> indent() + mapper.apply(item))
                .forEach(lines::add);
        return this;
    }

    // ─────────────────────────────────────────────────────────
    // Import Helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Append an import statement.
     * Example: .importLine("java.util.UUID")
     */
    public CodeTemplate importLine(String fqn) {
        lines.add("import " + fqn + ";");
        return this;
    }

    /**
     * Append multiple import statements.
     */
    public CodeTemplate imports(String... fqns) {
        Arrays.stream(fqns).forEach(this::importLine);
        return this;
    }

    /**
     * Conditionally append an import.
     */
    public CodeTemplate importIf(boolean condition, String fqn) {
        if (condition) importLine(fqn);
        return this;
    }

    // ─────────────────────────────────────────────────────────
    // Inline Join Helpers
    // ─────────────────────────────────────────────────────────

    /**
     * Join items with a delimiter into a single line.
     * Useful for method parameter lists, annotation attributes, etc.
     *
     * Example:
     *   joinLine(List.of("String a", "Long b"), ", ")
     *   → appended as "String a, Long b"
     */
    public <T> String joinOf(Collection<T> items, String delimiter, Function<T, String> mapper) {
        return items.stream().map(mapper).collect(Collectors.joining(delimiter));
    }

    // ─────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────

    /**
     * Render all lines to a final String.
     * Uses String.join — zero string concatenation in loops.
     */
    public String render() {
        return String.join("\n", lines);
    }

    /**
     * Render and append a trailing newline.
     */
    public String renderWithNewline() {
        return render() + "\n";
    }

    // ─────────────────────────────────────────────────────────
    // Internal
    // ─────────────────────────────────────────────────────────

    /** Build the indent prefix string for the current level */
    private String indent() {
        return "    ".repeat(indentLevel);
    }

    /** Get current indent level */
    public int getIndentLevel() {
        return indentLevel;
    }

    /** Return a copy of this template's lines (for inspection/testing) */
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    /** Return number of lines generated */
    public int lineCount() {
        return lines.size();
    }

    @Override
    public String toString() {
        return render();
    }
}