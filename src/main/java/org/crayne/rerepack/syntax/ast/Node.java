package org.crayne.rerepack.syntax.ast;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.syntax.lexer.LexerSpecification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Node {

    @NotNull
    private final List<Node> children;

    @NotNull
    private final NodeType type;

    @Nullable
    private Token value;

    @Nullable
    private String name;

    @NotNull
    public static Node of(@NotNull final NodeType type) {
        return new Node(type);
    }

    @NotNull
    public static Node of(@NotNull final NodeType type, @NotNull final String name) {
        return new Node(type, name);
    }

    public Node(@NotNull final NodeType type) {
        this.type = type;
        this.value = null;
        this.name = null;
        children = new ArrayList<>();
    }

    public Node(@NotNull final NodeType type, @Nullable final String name) {
        this.type = type;
        this.name = name;
        this.value = null;
        children = new ArrayList<>();
    }

    public Node(@NotNull final Token token,
                @NotNull final LexerSpecification lexerSpecification) {
        this.type = NodeType.of(token, lexerSpecification);
        this.value = token;
        this.name = lexerSpecification.removeStringLiterals(token.token());
        children = new ArrayList<>();
    }

    @NotNull
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public void name(@Nullable final String name) {
        this.name = name;
    }

    public void addChildren(@NotNull final Collection<Node> children) {
        this.children.addAll(children);
    }

    public void addChildren(@NotNull final Node... children) {
        this.children.addAll(List.of(children));
    }

    @NotNull
    public Token valueClean() {
        final Token rawToken = value();
        assert rawToken != null;

        return Token.of(name().orElseThrow(), rawToken);
    }

    @NotNull
    public Node child(final int index) {
        return children.get(index);
    }

    public void child(final int index, @NotNull final Node child) {
        children.set(index, child);
    }

    @Nullable
    public Token value() {
        return value;
    }

    public void value(@NotNull final Token token) {
        this.value = token;
    }

    @NotNull
    public NodeType type() {
        return type;
    }

    @NotNull
    public List<Node> children() {
        return children;
    }

    @NotNull
    public List<Node> children(@NotNull final String nameFilter) {
        return children
                .stream()
                .filter(n -> n.name().isPresent() && n.name().get().equals(nameFilter))
                .toList();
    }

    @NotNull
    public String toString() {
        final StringBuilder result = new StringBuilder(type.name().toLowerCase());
        if (name != null) result.append("(").append(name).append(")");
        if (!children.isEmpty()) {
            result.append(" [ \n");
            for (final Node child : children) {
                result.append(child.toString().indent(4));
            }
            result.append("]");
            return result.toString();
        }
        if (value == null && type != NodeType.LITERAL) result.append(" []");
        return result.toString();
    }

}
