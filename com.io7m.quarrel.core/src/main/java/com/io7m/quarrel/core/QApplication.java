/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.quarrel.core;

import com.io7m.quarrel.core.QStringType.QLocalize;
import com.io7m.quarrel.core.internal.QCommandApplicationUsage;
import com.io7m.quarrel.core.internal.QCommandContext;
import com.io7m.quarrel.core.internal.QCommandHelp;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionErrorDoesNotExist;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionOKCommand;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionOKGroup;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionRoot;
import com.io7m.quarrel.core.internal.QCommandVersion;
import com.io7m.quarrel.core.internal.QEmptyResources;
import com.io7m.quarrel.core.internal.QLocalization;
import com.io7m.quarrel.core.internal.QStrings;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * The default application configuration.
 */

public final class QApplication implements QApplicationType
{
  private static final Pattern AT = Pattern.compile("@");

  private final PrintWriter writer;
  private final QApplicationMetadata metadata;
  private final SortedMap<String, QCommandOrGroupType> commandTree;
  private final QValueConverterDirectoryType valueConverters;
  private final ResourceBundle applicationResources;
  private final ResourceBundle internalResources;
  private final QCommandParsers parsers;
  private final QLocalization localization;

  private QApplication(
    final PrintWriter inWriter,
    final QApplicationMetadata inMetadata,
    final SortedMap<String, QCommandOrGroupType> inCommandTree,
    final QValueConverterDirectoryType inConverters,
    final ResourceBundle inApplicationResources,
    final ResourceBundle inInternalResources)
  {
    this.writer =
      Objects.requireNonNull(inWriter, "writer");
    this.metadata =
      Objects.requireNonNull(inMetadata, "metadata");
    this.commandTree =
      Objects.requireNonNull(inCommandTree, "commandTree");
    this.valueConverters =
      Objects.requireNonNull(inConverters, "converters");
    this.applicationResources =
      Objects.requireNonNull(inApplicationResources, "applicationResources");
    this.internalResources =
      Objects.requireNonNull(inInternalResources, "internalResources");
    this.parsers =
      new QCommandParsers();
    this.localization =
      new QLocalization(inInternalResources, inApplicationResources);
  }

  /**
   * Create a new application builder.
   *
   * @param metadata The application metadata
   *
   * @return A new builder
   */

  public static QApplicationBuilderType builder(
    final QApplicationMetadata metadata)
  {
    try {
      return new ApplicationBuilder(metadata);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public QApplicationMetadata metadata()
  {
    return this.metadata;
  }

  @Override
  public SortedMap<String, QCommandOrGroupType> commandTree()
  {
    return this.commandTree;
  }

  @Override
  public ResourceBundle internalResources()
  {
    return this.internalResources;
  }

  @Override
  public ResourceBundle applicationResources()
  {
    return this.applicationResources;
  }

  @Override
  public QValueConverterDirectoryType valueConverters()
  {
    return this.valueConverters;
  }

  @Override
  public QCommandContextType parse(
    final List<String> arguments)
    throws QException
  {
    return this.parseExpanded(this.expandArguments(arguments));
  }

  private List<String> expandArguments(
    final List<String> arguments)
    throws QException
  {
    if (!arguments.isEmpty()) {
      final var first = arguments.get(0);
      if (first.startsWith("@")) {
        final var argumentsRest = new ArrayList<>(arguments);
        argumentsRest.remove(0);
        return this.expandArgument(first, argumentsRest);
      }
    }
    return arguments;
  }

  private List<String> expandArgument(
    final String first,
    final List<String> remaining)
    throws QException
  {
    final var withoutAt =
      AT.matcher(first).replaceFirst("");
    final var path =
      Paths.get(withoutAt);

    final List<String> lines;
    try {
      try (var stream = Files.lines(path)) {
        lines = stream
          .filter(line -> !line.startsWith("#"))
          .map(String::trim)
          .filter(line -> !line.isEmpty())
          .toList();
      }
    } catch (final IOException e) {
      throw new QException(
        this.localize(new QLocalize("quarrel.errorIOFile")),
        e,
        "io",
        Map.ofEntries(
          Map.entry(
            this.localize(new QLocalize("quarrel.file")),
            path.toAbsolutePath().toString())
        ),
        Optional.empty(),
        List.of()
      );
    }

    final var output =
      new ArrayList<String>(lines.size() + remaining.size());
    output.addAll(lines);
    output.addAll(remaining);
    return List.copyOf(output);
  }

  private QCommandContextType parseExpanded(
    final List<String> arguments)
    throws QException
  {
    final var resolved =
      QCommandTreeResolver.resolve(this.commandTree, arguments);

    if (resolved instanceof QResolutionRoot) {
      return new QCommandContext(
        this.commandTree,
        this.valueConverters(),
        this.writer,
        new QCommandApplicationUsage(this),
        this.localization,
        Map.of(),
        new QParametersPositionalAny(),
        List.of(),
        List.of()
      );
    }

    if (resolved instanceof QResolutionErrorDoesNotExist) {
      final var path = String.join(" ", arguments);
      throw new QException(
        this.localization.localize(new QLocalize(
          "quarrel.errorCommandNonexistent")),
        "command-nonexistent",
        Map.ofEntries(
          Map.entry(this.localize(new QLocalize("quarrel.command")), path)
        ),
        Optional.empty(),
        List.of()
      );
    }

    if (resolved instanceof final QResolutionOKCommand cmd) {
      final var configuration =
        new QCommandParserConfiguration(
          this.valueConverters(),
          this.applicationResources()
        );
      return this.parsers.create(configuration)
        .execute(this.commandTree, this.writer, cmd.command(), cmd.remaining());
    }

    if (resolved instanceof final QResolutionOKGroup group) {
      return new QCommandContext(
        this.commandTree,
        this.valueConverters(),
        this.writer,
        new QCommandHelp(this.metadata.applicationName(), this.commandTree),
        this.localization,
        Map.of(),
        new QParametersPositionalAny(),
        List.of(),
        group.path()
      );
    }

    throw new IllegalStateException("Unreachable code.");
  }

  @Override
  public String localize(
    final QStringType string)
  {
    return this.localization.localize(string);
  }

  @Override
  public String format(
    final QStringType string,
    final Object... arguments)
  {
    return this.localization.format(string, arguments);
  }

  private sealed interface BuilderType
  {

  }

  private static final class ApplicationBuilder
    implements QApplicationBuilderType, BuilderType
  {
    private final HashMap<String, CommandGroupBuilder> commandGroups;
    private final HashMap<String, QCommandType> commands;
    private final QApplicationMetadata metadata;
    private final QCommandVersion versionCommand;
    private final QCommandHelp helpCommand;
    private PrintWriter writer;
    private QValueConverterDirectoryType converters;
    private ResourceBundle internalResources;
    private ResourceBundle applicationResources;

    ApplicationBuilder(
      final QApplicationMetadata inMetadata)
      throws IOException
    {
      this.metadata =
        Objects.requireNonNull(inMetadata, "metadata");
      this.commandGroups =
        new HashMap<>();
      this.commands =
        new HashMap<>();
      this.converters =
        QValueConverterDirectory.core();
      this.writer =
        new PrintWriter(System.out, true);
      this.internalResources =
        new QStrings(Locale.getDefault())
          .resources();
      this.applicationResources =
        new QEmptyResources();

      this.versionCommand =
        new QCommandVersion(this.metadata);
      this.helpCommand =
        new QCommandHelp(this.metadata.applicationName(), Map.of());

      this.commands.put(
        this.versionCommand.metadata().name(),
        this.versionCommand
      );
      this.commands.put(
        this.helpCommand.metadata().name(),
        this.helpCommand
      );
    }

    @Override
    public QApplicationBuilderType setValueConverters(
      final QValueConverterDirectoryType newConverters)
    {
      this.converters = Objects.requireNonNull(newConverters, "converters");
      return this;
    }

    @Override
    public QApplicationBuilderType setOutput(
      final PrintWriter newWriter)
    {
      this.writer = Objects.requireNonNull(newWriter, "writer");
      return this;
    }

    @Override
    public QApplicationBuilderType setApplicationResources(
      final ResourceBundle resources)
    {
      this.applicationResources =
        Objects.requireNonNull(resources, "resources");
      return this;
    }

    @Override
    public QApplicationBuilderType setInternalResources(
      final ResourceBundle resources)
    {
      this.internalResources =
        Objects.requireNonNull(resources, "resources");
      return this;
    }

    @Override
    public QApplicationCommandGroupBuilderType createCommandGroup(
      final QCommandMetadata meta)
    {
      final var name = meta.name();
      this.checkNameOK(name);
      final var builder = new CommandGroupBuilder(meta);
      this.commandGroups.put(name, builder);
      return builder;
    }

    @Override
    public QApplicationBuilderType addCommand(
      final QCommandType command)
    {
      final var name = command.metadata().name();
      this.checkNameOK(name);
      this.commands.put(name, command);
      return this;
    }

    @Override
    public QApplicationType build()
    {
      final var tree = new TreeMap<String, QCommandOrGroupType>();
      for (final var command : this.commands.values()) {
        tree.put(command.metadata().name(), command);
      }
      for (final var group : this.commandGroups.values()) {
        tree.put(group.meta.name(), group.build());
      }

      final var newHelp =
        new QCommandHelp(this.metadata.applicationName(), tree);
      tree.put(newHelp.metadata().name(), newHelp);

      return new QApplication(
        this.writer,
        this.metadata,
        Collections.unmodifiableSortedMap(tree),
        this.converters,
        this.applicationResources,
        this.internalResources
      );
    }

    private void checkNameOK(
      final String name)
    {
      if (this.commands.containsKey(name)) {
        throw new IllegalArgumentException(
          "A command exists with the name %s".formatted(name)
        );
      }

      if (this.commandGroups.containsKey(name)) {
        throw new IllegalArgumentException(
          "A command group exists with the name %s".formatted(name)
        );
      }
    }
  }

  private static final class CommandGroup
    implements QCommandGroupType
  {
    private final SortedMap<String, QCommandOrGroupType> commandTree;
    private final QCommandMetadata metadata;

    private CommandGroup(
      final SortedMap<String, QCommandOrGroupType> inCommandTree,
      final QCommandMetadata inMeta)
    {
      this.commandTree =
        Objects.requireNonNull(inCommandTree, "commandTree");
      this.metadata =
        Objects.requireNonNull(inMeta, "name");
    }

    @Override
    public SortedMap<String, QCommandOrGroupType> commandTree()
    {
      return this.commandTree;
    }

    @Override
    public QCommandMetadata metadata()
    {
      return this.metadata;
    }
  }

  private static final class CommandGroupBuilder
    implements QApplicationCommandGroupBuilderType, BuilderType
  {
    private final HashMap<String, CommandGroupBuilder> commandGroups;
    private final HashMap<String, QCommandType> commands;
    private final QCommandMetadata meta;

    CommandGroupBuilder(
      final QCommandMetadata inMeta)
    {
      this.meta =
        Objects.requireNonNull(inMeta, "meta");
      this.commandGroups =
        new HashMap<>();
      this.commands =
        new HashMap<>();
    }

    private void checkNameOK(
      final String newName)
    {
      if (this.commands.containsKey(newName)) {
        throw new IllegalArgumentException(
          "A command exists with the name %s".formatted(newName)
        );
      }

      if (this.commandGroups.containsKey(newName)) {
        throw new IllegalArgumentException(
          "A command group exists with the name %s".formatted(newName)
        );
      }
    }

    @Override
    public QApplicationCommandGroupBuilderType createCommandGroup(
      final QCommandMetadata metadata)
    {
      final var newName = metadata.name();
      this.checkNameOK(newName);

      final var builder = new CommandGroupBuilder(metadata);
      this.commandGroups.put(newName, builder);
      return builder;
    }

    @Override
    public QApplicationCommandGroupBuilderType addCommand(
      final QCommandType command)
    {
      final var newName = command.metadata().name();
      this.checkNameOK(newName);
      this.commands.put(newName, command);
      return this;
    }

    public CommandGroup build()
    {
      final var tree = new TreeMap<String, QCommandOrGroupType>();
      for (final var command : this.commands.values()) {
        tree.put(command.metadata().name(), command);
      }
      for (final var group : this.commandGroups.values()) {
        tree.put(group.meta.name(), group.build());
      }

      return new CommandGroup(
        Collections.unmodifiableSortedMap(tree),
        this.meta
      );
    }
  }
}
