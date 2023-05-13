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
import com.io7m.quarrel.core.QCommandOrGroupType;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QLocalizationType;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed0N;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamed1N;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParameterPositional;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QParametersPositionalTyped;
import com.io7m.quarrel.core.QStringType;
import com.io7m.quarrel.core.QValueConverterDirectoryType;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;

/**
 * A basic command context implementation.
 */

public final class QCommandContext implements QCommandContextType
{
  private final QCommandType command;
  private final QLocalizationType localization;
  private final Map<QParameterNamedType<?>, List<Object>> parsedValues;
  private final QParametersPositionalType positionalParameters;
  private final List<Object> parsedPositionalValues;
  private final PrintWriter output;
  private final List<String> rawPositionalValues;
  private final QValueConverterDirectoryType valueConverters;
  private final SortedMap<String, QCommandOrGroupType> tree;

  /**
   * A basic command context implementation.
   *
   * @param inTree                   The command tree
   * @param inConverters             The value converters
   * @param inOutput                 The output
   * @param inCommand                The command
   * @param inLocalization           A localizer
   * @param inParsedNamedValues      The parsed named values
   * @param inPositionalParameters   The positional parameters
   * @param inParsedPositionalValues The parsed positional values
   * @param inRawPositionalValues    The raw positional values
   */

  public QCommandContext(
    final SortedMap<String, QCommandOrGroupType> inTree,
    final QValueConverterDirectoryType inConverters,
    final PrintWriter inOutput,
    final QCommandType inCommand,
    final QLocalizationType inLocalization,
    final Map<QParameterNamedType<?>, List<Object>> inParsedNamedValues,
    final QParametersPositionalType inPositionalParameters,
    final List<Object> inParsedPositionalValues,
    final List<String> inRawPositionalValues)
  {
    this.tree =
      Objects.requireNonNull(inTree, "tree");
    this.valueConverters =
      Objects.requireNonNull(inConverters, "converters");
    this.output =
      Objects.requireNonNull(inOutput, "output");
    this.command =
      Objects.requireNonNull(inCommand, "inCommand");
    this.localization =
      Objects.requireNonNull(inLocalization, "inLocalization");
    this.parsedValues =
      Objects.requireNonNull(inParsedNamedValues, "parsedValues");
    this.positionalParameters =
      Objects.requireNonNull(inPositionalParameters, "positionalParameters");
    this.parsedPositionalValues =
      Objects.requireNonNull(
        inParsedPositionalValues,
        "parsedPositionalValues"
      );
    this.rawPositionalValues =
      Objects.requireNonNull(
        inRawPositionalValues,
        "rawPositionalValues"
      );
  }

  @Override
  public SortedMap<String, QCommandOrGroupType> commandTree()
  {
    return this.tree;
  }

  @Override
  public PrintWriter output()
  {
    return this.output;
  }

  @Override
  public QValueConverterDirectoryType valueConverters()
  {
    return this.valueConverters;
  }

  @Override
  public QCommandType command()
  {
    return this.command;
  }

  @Override
  public List<String> parametersPositionalRaw()
  {
    return this.rawPositionalValues;
  }

  @Override
  public <T> T parameterValue(
    final QParameterPositional<T> parameter)
  {
    if (this.positionalParameters instanceof QParametersPositionalTyped typed) {
      final var parameters = typed.parameters();
      for (int index = 0; index < parameters.size(); ++index) {
        if (Objects.equals(parameters.get(index), parameter)) {
          return (T) this.parsedPositionalValues.get(index);
        }
      }
    }

    throw new IllegalArgumentException(
      "No such typed positional parameter '%s'".formatted(parameter.name())
    );
  }

  @Override
  public <T> T parameterValue(
    final QParameterNamed1<T> parameter)
  {
    return this.valueList(parameter).get(0);
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> valueList(
    final QParameterNamedType<T> parameter)
  {
    return (List<T>) Optional.ofNullable(this.parsedValues.get(parameter))
      .orElseThrow(() -> {
        return new IllegalArgumentException(
          "No such parameter '%s'".formatted(parameter.name())
        );
      });
  }

  @Override
  public <T> Optional<T> parameterValue(
    final QParameterNamed01<T> parameter)
  {
    final var xs = this.valueList(parameter);
    if (xs.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(xs.get(0));
  }

  @Override
  public <T> List<T> parameterValues(
    final QParameterNamed1N<T> parameter)
  {
    return this.valueList(parameter);
  }

  @Override
  public <T> List<T> parameterValues(
    final QParameterNamed0N<T> parameter)
  {
    return this.valueList(parameter);
  }

  @Override
  public QCommandStatus execute()
    throws Exception
  {
    return this.command.onExecute(this);
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
}
