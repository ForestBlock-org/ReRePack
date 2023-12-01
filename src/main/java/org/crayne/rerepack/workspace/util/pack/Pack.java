package org.crayne.rerepack.workspace.util.pack;

import org.crayne.rerepack.syntax.Token;
import org.crayne.rerepack.workspace.AbstractWorkspace;
import org.crayne.rerepack.workspace.Workspace;
import org.crayne.rerepack.workspace.util.def.DefinitionContainer;
import org.crayne.rerepack.workspace.util.replacement.predicate.Predicate;
import org.crayne.rerepack.workspace.util.replacement.Replacement;
import org.crayne.rerepack.workspace.util.replacement.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Pack {

    @NotNull
    private final DefinitionContainer localDefinitionContainer;

    @NotNull
    private final AbstractWorkspace abstractWorkspace;

    @NotNull
    private final String name;

    @NotNull
    private final Set<Replacement> replacements;

    public Pack(@NotNull final String name, @NotNull final AbstractWorkspace abstractWorkspace) {
        this.abstractWorkspace = abstractWorkspace;
        this.name = name;
        this.localDefinitionContainer = new DefinitionContainer(abstractWorkspace.definitionContainer());
        this.replacements = new HashSet<>();
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public Replacement useTemplate(@NotNull final Token name, @NotNull final DefinitionContainer parameters) {
        if (!(abstractWorkspace instanceof final Workspace workspace))
            throw new PackException("Cannot use template outside of the root workspace");

        final Template template = workspace.template(name);
        final Replacement replacement = template.createReplacement(name, parameters);
        replacements.add(replacement);

        return replacement;
    }

    @NotNull
    public Template createTemplate(@NotNull final Token name,
                                   @NotNull final Map<Token, Optional<Token>> parameterNames,
                                   @NotNull final Set<Predicate> matches,
                                   @NotNull final Set<Predicate> replacements) {
        if (!(abstractWorkspace instanceof final Workspace workspace))
            throw new PackException("Cannot create template outside of the root workspace");

        final Template template = new Template(this, parameterNames, matches, replacements);
        workspace.createTemplate(name, template);
        return template;
    }

    @NotNull
    public Replacement createReplacement(@NotNull final Set<Predicate> matches,
                                         @NotNull final Set<Predicate> replacements) {
        final Replacement replacement = new Replacement(this, matches, replacements);
        this.replacements.add(replacement);
        return replacement;
    }

    public void createLocalDefinition(@NotNull final Token name, @NotNull final Token value) {
        localDefinitionContainer.define(name, value);
    }

    public void initializeLocalDefinitions() {
        localDefinitionContainer.definitions().forEach((key, value) -> localDefinitionContainer.initialize(key));
    }

    @NotNull
    public Set<Replacement> replacements() {
        return Collections.unmodifiableSet(replacements);
    }

    @NotNull
    public DefinitionContainer localDefinitionContainer() {
        return localDefinitionContainer;
    }

}
