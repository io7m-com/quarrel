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


package com.io7m.quarrel.core.internal;

import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandGroupType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandOrGroupType;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandTreeResolver;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed0N;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamed1N;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParameterPositional;
import com.io7m.quarrel.core.QParameterType;
import com.io7m.quarrel.core.QParametersPositionalAny;
import com.io7m.quarrel.core.QParametersPositionalNone;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QParametersPositionalTyped;
import com.io7m.quarrel.core.QStringType;
import com.io7m.quarrel.core.QStringType.QLocalize;
import com.io7m.quarrel.core.QValueConverterType;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionErrorDoesNotExist;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionOKCommand;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionRoot;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;
import static java.lang.Math.max;

/**
 * The help command.
 */

public final class QCommandHelp implements QCommandType
{
  private final String applicationName;
  private final Map<String, QCommandOrGroupType> commandTree;

  /**
   * The help command.
   *
   * @param inApplicationName The application name
   * @param inCommandTree     The tree of commands
   */

  public QCommandHelp(
    final String inApplicationName,
    final Map<String, QCommandOrGroupType> inCommandTree)
  {
    this.applicationName =
      Objects.requireNonNull(inApplicationName, "inApplicationName");
    this.commandTree =
      Objects.requireNonNull(inCommandTree, "commandTree");
  }

  private static void formatDescriptionLong(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final QCommandMetadata metadata)
  {
    metadata.longDescription()
      .ifPresent(text -> {
        final var formatted =
          ctx.localize(text);
        final var lines =
          formatted.lines().toList();

        for (final var line : lines) {
          writer.print("  ");
          writer.println(line);
        }
      });
  }

  private static void formatParametersNamed(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final List<QParameterNamedType<?>> named)
  {
    if (!named.isEmpty()) {
      final var converters = ctx.valueConverters();
      named.sort(Comparator.comparing(QParameterType::name));

      var maxLength = 0;

      final var descriptionName =
        ctx.localize(new QLocalize("quarrel.help.description.word"));
      final var syntaxName =
        ctx.localize(new QLocalize("quarrel.help.syntax"));
      final var defaultName =
        ctx.localize(new QLocalize("quarrel.help.default"));
      final var cardinalityName =
        ctx.localize(new QLocalize("quarrel.help.cardinality"));
      final var alternativesName =
        ctx.localize(new QLocalize("quarrel.help.alternatives"));
      final var typeName =
        ctx.localize(new QLocalize("quarrel.help.type"));

      maxLength = max(maxLength, alternativesName.length());
      maxLength = max(maxLength, cardinalityName.length());
      maxLength = max(maxLength, defaultName.length());
      maxLength = max(maxLength, descriptionName.length());
      maxLength = max(maxLength, syntaxName.length());
      maxLength = max(maxLength, typeName.length());
      maxLength = maxLength + 1;

      writer.print("  ");
      writer.print(ctx.localize(new QLocalize("quarrel.help.named")));
      writer.println();

      for (final var parameter : named) {
        final var converter =
          converters.converterFor(parameter.type())
            .orElseThrow();

        showParameterName(writer, parameter);
        showParameterDescription(
          ctx,
          writer,
          maxLength,
          descriptionName,
          parameter);
        showParameterType(writer, maxLength, typeName, parameter);
        showParameterCardinalityNamed1(
          ctx,
          writer,
          maxLength,
          defaultName,
          cardinalityName,
          parameter,
          converter);
        showParameterCardinalityNamed01(
          ctx,
          writer,
          maxLength,
          defaultName,
          cardinalityName,
          parameter,
          converter);
        showParameterCardinalityNamed0N(
          ctx,
          writer,
          maxLength,
          defaultName,
          cardinalityName,
          parameter,
          converter);
        showParameterCardinalityNamed1N(
          ctx,
          writer,
          maxLength,
          defaultName,
          cardinalityName,
          parameter,
          converter);
        showParameterSyntax(writer, maxLength, syntaxName, converter);
        showParameterAlternatives(
          writer,
          maxLength,
          alternativesName,
          parameter
        );
      }

      writer.println();
    } else {
      writer.print("  ");
      writer.print(ctx.localize(new QLocalize("quarrel.help.named.none")));
      writer.println();
      writer.println();
    }
  }

  private static void showParameterDescription(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final int maxLength,
    final String descriptionName,
    final QParameterNamedType<?> parameter)
  {
    writer.print("      ");
    writer.print(descriptionName);
    writer.print(pad(maxLength, descriptionName));
    writer.print(": ");
    writer.print(ctx.localize(parameter.description()));
    writer.println();
  }

  private static void showParameterType(
    final PrintWriter writer,
    final int maxLength,
    final String typeName,
    final QParameterNamedType<?> parameter)
  {
    writer.print("      ");
    writer.print(typeName);
    writer.print(pad(maxLength, typeName));
    writer.print(": ");
    writer.print(parameter.type().getSimpleName());
    writer.println();
  }

  private static void showParameterSyntax(
    final PrintWriter writer,
    final int maxLength,
    final String syntaxName,
    final QValueConverterType<?> converter)
  {
    writer.print("      ");
    writer.print(syntaxName);
    writer.print(pad(maxLength, syntaxName));
    writer.print(": ");
    writer.print(converter.syntax());
    writer.println();
  }

  private static void showParameterAlternatives(
    final PrintWriter writer,
    final int maxLength,
    final String alternativesName,
    final QParameterNamedType<?> parameter)
  {
    final var nameAlternatives = parameter.nameAlternatives();
    if (!nameAlternatives.isEmpty()) {
      writer.print("      ");
      writer.print(alternativesName);
      writer.print(pad(maxLength, alternativesName));
      writer.print(": ");
      writer.print(String.join(", ", nameAlternatives));
      writer.println();
    }
  }

  private static void showParameterName(
    final PrintWriter writer,
    final QParameterNamedType<?> parameter)
  {
    if (parameter instanceof final QParameterNamed1<?> n
        && n.defaultValue().isEmpty()) {
      writer.print("  * ");
      writer.print(parameter.name());
      writer.println();
    } else {
      writer.print("    ");
      writer.print(parameter.name());
      writer.println();
    }
  }

  private static void showParameterCardinalityNamed1N(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final int maxLength,
    final String defaultName,
    final String cardinalityName,
    final QParameterNamedType<?> parameter,
    final QValueConverterType<?> converter)
  {
    if (parameter instanceof final QParameterNamed1N<?> p1n) {
      final var def = p1n.defaultValue();
      if (def.isPresent()) {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize(
          "quarrel.help.cardinality.1n")));
        writer.println();

        writer.print("      ");
        writer.print(defaultName);
        writer.print(pad(maxLength, defaultName));
        writer.print(": ");
        printUnsafe(writer, converter, def.get());
        writer.println();
      } else {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize(
          "quarrel.help.cardinality.1n.noDefault")));
        writer.println();
      }
    }
  }

  private static void showParameterCardinalityNamed0N(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final int maxLength,
    final String defaultName,
    final String cardinalityName,
    final QParameterNamedType<?> parameter,
    final QValueConverterType<?> converter)
  {
    if (parameter instanceof final QParameterNamed0N<?> p0n) {
      final var def = p0n.defaultValue();
      if (!def.isEmpty()) {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize("quarrel.help.cardinality.0n")));
        writer.println();

        writer.print("      ");
        writer.print(defaultName);
        writer.print(pad(maxLength, defaultName));
        writer.print(": ");
        printUnsafeList(writer, converter, def);
        writer.println();
      } else {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize(
          "quarrel.help.cardinality.0n.noDefault")));
        writer.println();
      }
    }
  }

  private static void showParameterCardinalityNamed01(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final int maxLength,
    final String defaultName,
    final String cardinalityName,
    final QParameterNamedType<?> parameter,
    final QValueConverterType<?> converter)
  {
    if (parameter instanceof final QParameterNamed01<?> p01) {
      final var def = p01.defaultValue();
      if (def.isPresent()) {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize("quarrel.help.cardinality.01")));
        writer.println();

        writer.print("      ");
        writer.print(defaultName);
        writer.print(pad(maxLength, defaultName));
        writer.print(": ");
        printUnsafe(writer, converter, def.get());
        writer.println();
      } else {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize(
          "quarrel.help.cardinality.01.noDefault")));
        writer.println();
      }
    }
  }

  private static void showParameterCardinalityNamed1(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final int maxLength,
    final String defaultName,
    final String cardinalityName,
    final QParameterNamedType<?> parameter,
    final QValueConverterType<?> converter)
  {
    if (parameter instanceof final QParameterNamed1<?> p1) {
      final var def = p1.defaultValue();
      if (def.isPresent()) {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize("quarrel.help.cardinality.1")));
        writer.println();

        writer.print("      ");
        writer.print(defaultName);
        writer.print(pad(maxLength, defaultName));
        writer.print(": ");
        printUnsafe(writer, converter, def.get());
        writer.println();
      } else {
        writer.print("      ");
        writer.print(cardinalityName);
        writer.print(pad(maxLength, cardinalityName));
        writer.print(": ");
        writer.print(ctx.localize(new QLocalize(
          "quarrel.help.cardinality.1.noDefault")));
        writer.println();
      }
    }
  }

  private static void printUnsafeList(
    final PrintWriter writer,
    final QValueConverterType<?> converter,
    final List<?> def)
  {
    final var cUnsafe =
      (QValueConverterType<Object>) converter;
    final var text =
      def.stream()
        .map(cUnsafe::convertToString)
        .toList();
    writer.print(text);
  }

  private static void printUnsafe(
    final PrintWriter writer,
    final QValueConverterType<?> converter,
    final Object def)
  {
    final var cUnsafe = (QValueConverterType<Object>) converter;
    writer.print(cUnsafe.convertToString(def));
  }

  private static String pad(
    final int maxLength,
    final String name)
  {
    return " ".repeat(maxLength - name.length());
  }

  private static void formatParametersPositional(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final QParametersPositionalType positional)
  {
    if (positional instanceof final QParametersPositionalTyped typed) {
      final var converters = ctx.valueConverters();
      final var parameters = new ArrayList<>(typed.parameters());
      parameters.sort(Comparator.comparing(QParameterType::name));

      var maxLength = 0;

      final var descriptionName =
        ctx.localize(new QLocalize("quarrel.help.description.word"));
      final var syntaxName =
        ctx.localize(new QLocalize("quarrel.help.syntax"));
      final var defaultName =
        ctx.localize(new QLocalize("quarrel.help.default"));
      final var cardinalityName =
        ctx.localize(new QLocalize("quarrel.help.cardinality"));
      final var alternativesName =
        ctx.localize(new QLocalize("quarrel.help.alternatives"));
      final var typeName =
        ctx.localize(new QLocalize("quarrel.help.type"));

      maxLength = max(maxLength, alternativesName.length());
      maxLength = max(maxLength, cardinalityName.length());
      maxLength = max(maxLength, defaultName.length());
      maxLength = max(maxLength, descriptionName.length());
      maxLength = max(maxLength, syntaxName.length());
      maxLength = max(maxLength, typeName.length());
      maxLength = maxLength + 1;

      writer.print("  ");
      writer.print(ctx.localize(new QLocalize("quarrel.help.positional")));
      writer.println();

      for (final var parameter : parameters) {
        final var converter =
          converters.converterFor(parameter.type()).orElseThrow();

        writer.print("    ");
        writer.print(parameter.name());
        writer.println();

        showParameterType(writer, maxLength, typeName, parameter);
        showParameterDescription(
          ctx,
          writer,
          maxLength,
          descriptionName,
          parameter);
        showParameterSyntax(writer, maxLength, syntaxName, converter);
      }

      writer.println();
    } else if (positional instanceof QParametersPositionalNone) {
      writer.print("  ");
      writer.print(ctx.localize(new QLocalize("quarrel.help.positional.none")));
      writer.println();
    } else if (positional instanceof QParametersPositionalAny) {
      writer.print("  ");
      writer.print(ctx.localize(new QLocalize("quarrel.help.positional.any")));
      writer.println();
    }
  }

  private static void showParameterDescription(
    final QCommandContextType ctx,
    final PrintWriter writer,
    final int maxLength,
    final String descriptionName,
    final QParameterPositional<?> parameter)
  {
    writer.print("      ");
    writer.print(descriptionName);
    writer.print(pad(maxLength, descriptionName));
    writer.print(": ");
    writer.print(ctx.localize(parameter.description()));
    writer.println();
  }

  private static void showParameterType(
    final PrintWriter writer,
    final int maxLength,
    final String typeName,
    final QParameterPositional<?> parameter)
  {
    writer.print("      ");
    writer.print(typeName);
    writer.print(pad(maxLength, typeName));
    writer.print(": ");
    writer.print(parameter.type().getSimpleName());
    writer.println();
  }

  private static void formatDescriptionShort(
    final QCommandContextType context,
    final PrintWriter w,
    final QCommandMetadata metadata)
  {
    w.print("  ");
    w.print(context.localize(metadata.shortDescription()));
    w.println();
    w.println();
  }

  private static QException errorReachedCommandBeforeFullPath(
    final QCommandContextType context,
    final List<String> pathSoFar,
    final List<String> pathFull)
  {
    final var errorMessage =
      context.localize(new QLocalize("quarrel.errorYieldedCommandUnexpectedly"));
    final var suggestMessage =
      context.localize(new QLocalize("quarrel.errorSuggestRightPath"));
    final var commandFound =
      context.localize(new QLocalize("quarrel.commandFound"));
    final var fullPath =
      context.localize(new QLocalize("quarrel.fullPath"));

    return new QException(
      errorMessage,
      "command-path-error",
      Map.ofEntries(
        Map.entry(commandFound, String.join(" ", pathSoFar)),
        Map.entry(fullPath, String.join(" ", pathFull))
      ),
      Optional.of(suggestMessage),
      List.of()
    );
  }

  private static boolean hasRemaining(
    final List<String> items,
    final int index)
  {
    return index + 1 < items.size();
  }

  private static QException errorNonexistentCommand(
    final QCommandContextType context,
    final List<String> items)
  {
    final var errorCommandNonexistent =
      context.localize(new QLocalize("quarrel.errorCommandNonexistent"));
    final var command =
      context.localize(new QLocalize("quarrel.command"));
    final var suggestMessage =
      context.localize(new QLocalize("quarrel.errorSuggestRightPath"));

    return new QException(
      errorCommandNonexistent,
      "command-nonexistent",
      Map.ofEntries(
        Map.entry(command, String.join(" ", items))
      ),
      Optional.of(suggestMessage),
      List.of()
    );
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return List.of();
  }

  @Override
  public QParametersPositionalType onListPositionalParameters()
  {
    return new QParametersPositionalAny();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType context)
    throws Exception
  {
    final var raw =
      context.parametersPositionalRaw();
    final var resolved =
      QCommandTreeResolver.resolve(this.commandTree, raw);

    if (resolved instanceof QResolutionRoot) {
      return this.showCommand(context, this);
    }

    if (resolved instanceof QResolutionErrorDoesNotExist) {
      throw errorNonexistentCommand(context, raw);
    }

    if (resolved instanceof QResolutionOKCommand cmd) {
      return this.showCommand(context, cmd.command());
    }

    if (resolved instanceof QCommandTreeResolver.QResolutionOKGroup group) {
      return this.showCommandGroup(context, group.target(), group.path());
    }

    throw new IllegalStateException("Unreachable code.");
  }

  private QCommandStatus showCommand(
    final QCommandContextType ctx,
    final QCommandType cmd)
  {
    final var w = ctx.output();
    final var metadata = cmd.metadata();

    final var named =
      new ArrayList<>(cmd.onListNamedParameters());
    final var positional =
      cmd.onListPositionalParameters();

    this.formatUsageHeader(ctx, w, metadata, named, positional);
    formatDescriptionShort(ctx, w, metadata);
    formatParametersNamed(ctx, w, named);
    formatParametersPositional(ctx, w, positional);
    formatDescriptionLong(ctx, w, metadata);

    w.println();
    w.flush();
    return SUCCESS;
  }

  private QCommandStatus showCommandGroup(
    final QCommandContextType ctx,
    final QCommandGroupType group,
    final List<String> path)
  {
    final var w = ctx.output();
    final var metadata = group.metadata();

    this.formatGroupUsageHeader(ctx, w, group, path);
    formatDescriptionShort(ctx, w, metadata);
    formatGroupCommands(ctx, w, group);
    formatDescriptionLong(ctx, w, metadata);

    w.println();
    w.flush();
    return SUCCESS;
  }

  private void formatGroupCommands(
    final QCommandContextType context,
    final PrintWriter w,
    final QCommandGroupType group)
  {
    final var items = new ArrayList<>(group.commandTree().values());
    items.sort(Comparator.comparing(o -> o.metadata().name()));

    w.print("  ");
    w.println(context.localize(new QLocalize("quarrel.usage.commands")));

    final var longest =
      4 + items.stream()
        .mapToInt(g -> g.metadata().name().length())
        .reduce(0, Integer::max);

    for (final var command : items) {
      if (command.isHidden()) {
        continue;
      }

      final var metadata =
        command.metadata();
      final var name =
        metadata.name();
      final var pad =
        longest - name.length();

      w.print("    ");
      w.print(name);
      w.print(" ".repeat(pad));
      w.print(context.localize(metadata.shortDescription()));
      w.println();
    }
    w.println();
  }

  private void formatGroupUsageHeader(
    final QCommandContextType context,
    final PrintWriter writer,
    final QCommandGroupType group,
    final List<String> path)
  {
    writer.print(
      context.format(
        new QLocalize("quarrel.usage.group"),
        this.applicationName,
        String.join(" ", path)
      )
    );
    writer.println();
    writer.println();
  }

  private void formatUsageHeader(
    final QCommandContextType context,
    final PrintWriter writer,
    final QCommandMetadata metadata,
    final Collection<QParameterNamedType<?>> namedParameters,
    final QParametersPositionalType positionalParameters)
  {
    final QStringType usage;
    var positionalNames = "";
    if (namedParameters.isEmpty()) {
      if (positionalParameters instanceof QParametersPositionalNone) {
        usage = new QLocalize("quarrel.usage.command.none");
      } else if (positionalParameters instanceof final QParametersPositionalTyped typed) {
        usage = new QLocalize("quarrel.usage.command.no_named");
        positionalNames =
          typed.parameters()
            .stream()
            .map(QParameterPositional::name)
            .map("<%s>"::formatted)
            .collect(Collectors.joining(" ")
            );
      } else {
        usage = new QLocalize("quarrel.usage.command.no_named");
        positionalNames = context.format(new QLocalize("quarrel.positional.any"));
      }
    } else {
      if (positionalParameters instanceof QParametersPositionalNone) {
        usage = new QLocalize("quarrel.usage.command.no_positional");
      } else if (positionalParameters instanceof final QParametersPositionalTyped typed) {
        usage = new QLocalize("quarrel.usage.command");
        positionalNames =
          typed.parameters()
            .stream()
            .map(QParameterPositional::name)
            .map("<%s>"::formatted)
            .collect(Collectors.joining(" ")
            );
      } else {
        usage = new QLocalize("quarrel.usage.command");
        positionalNames = context.format(new QLocalize("quarrel.positional.any"));
      }
    }

    writer.print(
      context.format(
        usage,
        this.applicationName,
        metadata.name(),
        positionalNames)
    );
    writer.println();
    writer.println();
  }

  @Override
  public QCommandMetadata metadata()
  {
    return new QCommandMetadata(
      "help",
      new QLocalize("quarrel.help.description"),
      Optional.of(new QLocalize("quarrel.help.description.long"))
    );
  }
}
