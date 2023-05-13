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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.Math.max;

/**
 * Standard functions to format help messages.
 */

public final class QCommandHelpFormatting
{
  private QCommandHelpFormatting()
  {

  }

  /**
   * Format a help message for the given group to the given output.
   *
   * @param valueConverters The value converters
   * @param localization    The localization context
   * @param applicationName The application name
   * @param output          The output
   * @param group           The group
   * @param path            The group's path
   */

  public static void formatGroup(
    final QValueConverterDirectoryType valueConverters,
    final QLocalizationType localization,
    final String applicationName,
    final PrintWriter output,
    final QCommandGroupType group,
    final List<String> path)
  {
    Objects.requireNonNull(valueConverters, "valueConverters");
    Objects.requireNonNull(localization, "localization");
    Objects.requireNonNull(applicationName, "applicationName");
    Objects.requireNonNull(output, "output");
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(path, "path");

    showCommandGroup(localization, output, applicationName, group, path);

    output.println();
    output.flush();
  }

  /**
   * Format a help message for the given command to the given output.
   *
   * @param valueConverters The value converters
   * @param localization    The localization context
   * @param applicationName The application name
   * @param output          The output
   * @param command         The command
   */

  public static void formatCommand(
    final QValueConverterDirectoryType valueConverters,
    final QLocalizationType localization,
    final String applicationName,
    final PrintWriter output,
    final QCommandType command)
  {
    Objects.requireNonNull(valueConverters, "valueConverters");
    Objects.requireNonNull(localization, "localization");
    Objects.requireNonNull(applicationName, "applicationName");
    Objects.requireNonNull(output, "output");
    Objects.requireNonNull(command, "command");

    final var metadata =
      command.metadata();
    final var named =
      new ArrayList<>(command.onListNamedParameters());
    final var positional =
      command.onListPositionalParameters();

    formatUsageHeader(
      localization,
      applicationName,
      output,
      command.metadata(),
      named,
      positional
    );
    formatDescriptionShort(localization, output, metadata);
    formatParametersNamed(valueConverters, localization, output, named);
    formatParametersPositional(
      valueConverters,
      localization,
      output,
      positional
    );
    formatDescriptionLong(localization, output, metadata);

    output.println();
    output.flush();
  }

  private static void formatDescriptionLong(
    final QLocalizationType ctx,
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
    final QValueConverterDirectoryType converters,
    final QLocalizationType ctx,
    final PrintWriter writer,
    final List<QParameterNamedType<?>> named)
  {
    if (!named.isEmpty()) {
      named.sort(Comparator.comparing(QParameterType::name));

      var maxLength = 0;

      final var descriptionName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.description.word"));
      final var syntaxName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.syntax"));
      final var defaultName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.default"));
      final var cardinalityName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.cardinality"));
      final var alternativesName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.alternatives"));
      final var typeName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.type"));

      maxLength = max(maxLength, alternativesName.length());
      maxLength = max(maxLength, cardinalityName.length());
      maxLength = max(maxLength, defaultName.length());
      maxLength = max(maxLength, descriptionName.length());
      maxLength = max(maxLength, syntaxName.length());
      maxLength = max(maxLength, typeName.length());
      maxLength = maxLength + 1;

      writer.print("  ");
      writer.print(ctx.localize(new QStringType.QLocalize("quarrel.help.named")));
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
      writer.print(ctx.localize(new QStringType.QLocalize(
        "quarrel.help.named.none")));
      writer.println();
      writer.println();
    }
  }

  private static void showParameterDescription(
    final QLocalizationType ctx,
    final PrintWriter writer,
    final int maxLength,
    final String descriptionName,
    final QParameterType<?> parameter)
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
    final QParameterType<?> parameter)
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
    final QParameterType<?> parameter)
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
    final QLocalizationType ctx,
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
        writer.print(ctx.localize(new QStringType.QLocalize(
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
        writer.print(ctx.localize(new QStringType.QLocalize(
          "quarrel.help.cardinality.1n.noDefault")));
        writer.println();
      }
    }
  }

  private static void showParameterCardinalityNamed0N(
    final QLocalizationType ctx,
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
        writer.print(ctx.localize(new QStringType.QLocalize(
          "quarrel.help.cardinality.0n")));
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
        writer.print(ctx.localize(new QStringType.QLocalize(
          "quarrel.help.cardinality.0n.noDefault")));
        writer.println();
      }
    }
  }

  private static void showParameterCardinalityNamed01(
    final QLocalizationType ctx,
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
        writer.print(ctx.localize(new QStringType.QLocalize(
          "quarrel.help.cardinality.01")));
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
        writer.print(ctx.localize(new QStringType.QLocalize(
          "quarrel.help.cardinality.01.noDefault")));
        writer.println();
      }
    }
  }

  private static void showParameterCardinalityNamed1(
    final QLocalizationType ctx,
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
        writer.print(ctx.localize(new QStringType.QLocalize(
          "quarrel.help.cardinality.1")));
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
        writer.print(ctx.localize(new QStringType.QLocalize(
          "quarrel.help.cardinality.1.noDefault")));
        writer.println();
      }
    }
  }

  private static void printUnsafeList(
    final PrintWriter writer,
    final QValueConverterType<?> converter,
    final Collection<?> def)
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
    final CharSequence name)
  {
    return " ".repeat(maxLength - name.length());
  }

  private static void formatParametersPositional(
    final QValueConverterDirectoryType converters,
    final QLocalizationType ctx,
    final PrintWriter writer,
    final QParametersPositionalType positional)
  {
    if (positional instanceof final QParametersPositionalTyped typed) {
      final List<QParameterPositional<?>> parameters = new ArrayList<>(typed.parameters());
      parameters.sort(Comparator.comparing(QParameterType::name));

      var maxLength = 0;

      final var descriptionName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.description.word"));
      final var syntaxName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.syntax"));
      final var defaultName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.default"));
      final var cardinalityName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.cardinality"));
      final var alternativesName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.alternatives"));
      final var typeName =
        ctx.localize(new QStringType.QLocalize("quarrel.help.type"));

      maxLength = max(maxLength, alternativesName.length());
      maxLength = max(maxLength, cardinalityName.length());
      maxLength = max(maxLength, defaultName.length());
      maxLength = max(maxLength, descriptionName.length());
      maxLength = max(maxLength, syntaxName.length());
      maxLength = max(maxLength, typeName.length());
      maxLength = maxLength + 1;

      writer.print("  ");
      writer.print(ctx.localize(new QStringType.QLocalize(
        "quarrel.help.positional")));
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
      writer.print(ctx.localize(new QStringType.QLocalize(
        "quarrel.help.positional.none")));
      writer.println();
    } else if (positional instanceof QParametersPositionalAny) {
      writer.print("  ");
      writer.print(ctx.localize(new QStringType.QLocalize(
        "quarrel.help.positional.any")));
      writer.println();
    }
  }

  private static void showParameterDescription(
    final QLocalizationType ctx,
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
    final QLocalizationType context,
    final PrintWriter w,
    final QCommandMetadata metadata)
  {
    w.print("  ");
    w.print(context.localize(metadata.shortDescription()));
    w.println();
    w.println();
  }

  private static void formatGroupCommands(
    final QLocalizationType context,
    final PrintWriter w,
    final QCommandGroupType group)
  {
    final List<QCommandOrGroupType> items = new ArrayList<>(group.commandTree().values());
    items.sort(Comparator.comparing(o -> o.metadata().name()));

    w.print("  ");
    w.println(context.localize(new QStringType.QLocalize(
      "quarrel.usage.commands")));

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

  private static void formatGroupUsageHeader(
    final QLocalizationType context,
    final String applicationName,
    final PrintWriter writer,
    final QCommandGroupType group,
    final Iterable<String> path)
  {
    writer.print(
      context.format(
        new QStringType.QLocalize("quarrel.usage.group"),
        applicationName,
        String.join(" ", path)
      )
    );
    writer.println();
    writer.println();
  }

  private static void showCommandGroup(
    final QLocalizationType ctx,
    final PrintWriter w,
    final String applicationName,
    final QCommandGroupType group,
    final List<String> path)
  {
    final var metadata = group.metadata();
    formatGroupUsageHeader(ctx, applicationName, w, group, path);
    formatDescriptionShort(ctx, w, metadata);
    formatGroupCommands(ctx, w, group);
    formatDescriptionLong(ctx, w, metadata);
  }

  private static void formatUsageHeader(
    final QLocalizationType context,
    final String applicationName,
    final PrintWriter writer,
    final QCommandMetadata metadata,
    final Collection<QParameterNamedType<?>> namedParameters,
    final QParametersPositionalType positionalParameters)
  {
    final QStringType usage;
    var positionalNames = "";
    if (namedParameters.isEmpty()) {
      if (positionalParameters instanceof QParametersPositionalNone) {
        usage = new QStringType.QLocalize("quarrel.usage.command.none");
      } else if (positionalParameters instanceof final QParametersPositionalTyped typed) {
        usage = new QStringType.QLocalize("quarrel.usage.command.no_named");
        positionalNames =
          typed.parameters()
            .stream()
            .map(QParameterPositional::name)
            .map("<%s>"::formatted)
            .collect(Collectors.joining(" ")
            );
      } else {
        usage = new QStringType.QLocalize("quarrel.usage.command.no_named");
        positionalNames = context.format(new QStringType.QLocalize(
          "quarrel.positional.any"));
      }
    } else {
      if (positionalParameters instanceof QParametersPositionalNone) {
        usage = new QStringType.QLocalize("quarrel.usage.command.no_positional");
      } else if (positionalParameters instanceof final QParametersPositionalTyped typed) {
        usage = new QStringType.QLocalize("quarrel.usage.command");
        positionalNames =
          typed.parameters()
            .stream()
            .map(QParameterPositional::name)
            .map("<%s>"::formatted)
            .collect(Collectors.joining(" ")
            );
      } else {
        usage = new QStringType.QLocalize("quarrel.usage.command");
        positionalNames = context.format(new QStringType.QLocalize(
          "quarrel.positional.any"));
      }
    }

    writer.print(
      context.format(usage, applicationName, metadata.name(), positionalNames)
    );
    writer.println();
    writer.println();
  }
}
