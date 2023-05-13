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
import com.io7m.quarrel.core.QCommandParserConfiguration;
import com.io7m.quarrel.core.QCommandParserType;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed0N;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamed1N;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParameterPositional;
import com.io7m.quarrel.core.QParametersPositionalAny;
import com.io7m.quarrel.core.QParametersPositionalNone;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QParametersPositionalTyped;
import com.io7m.quarrel.core.QStringType.QLocalize;
import com.io7m.quarrel.core.QValueConverterType;
import com.io7m.seltzer.api.SStructuredError;
import com.io7m.seltzer.api.SStructuredErrorType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;

import static java.lang.Integer.toUnsignedString;

/**
 * The command parser.
 */

public final class QCommandParser implements QCommandParserType
{
  private final QCommandParserConfiguration configuration;
  private final QStrings strings;
  private final QLocalization localization;

  /**
   * The command parser.
   *
   * @param inConfiguration The configuration
   * @param inStrings       The strings
   */

  public QCommandParser(
    final QCommandParserConfiguration inConfiguration,
    final QStrings inStrings)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.localization =
      new QLocalization(
        this.strings.resources(),
        this.configuration.applicationResources()
      );
  }

  private static List<Object> parseParametersPositionalAny(
    final Collection<String> mutableArguments)
  {
    return List.copyOf(mutableArguments);
  }

  private static void throwExceptionIfNecessary(
    final ArrayList<SStructuredErrorType<String>> errors)
    throws QException
  {
    if (!errors.isEmpty()) {
      final var first = errors.remove(0);
      throw new QException(
        first.message(),
        first.errorCode(),
        first.attributes(),
        first.remediatingAction(),
        errors
      );
    }
  }

  private static QException exceptionError(
    final SStructuredErrorType<String> error)
  {
    return new QException(
      error.message(),
      error.errorCode(),
      error.attributes(),
      error.remediatingAction(),
      List.of()
    );
  }

  private List<Object> parseParametersPositional(
    final QCommandType command,
    final ValidatedPositionalsType positionals,
    final List<String> arguments)
    throws QException
  {
    if (positionals instanceof final ValidatedPositionalsTyped typed) {
      return this.parseParametersPositionalTyped(command, arguments, typed);
    }

    if (positionals instanceof ValidatedPositionalsNone) {
      return this.parseParametersPositionalNone(command, arguments);
    }

    if (positionals instanceof ValidatedPositionalsAny) {
      return parseParametersPositionalAny(arguments);
    }

    throw new IllegalStateException("Unreachable code.");
  }

  private List<Object> parseParametersPositionalNone(
    final QCommandType command,
    final Collection<String> arguments)
    throws QException
  {
    if (!arguments.isEmpty()) {
      throw exceptionError(
        this.errorPositionalArgumentsWrongCount(
          command, 0, arguments.size())
      );
    }
    return List.of();
  }

  private List<Object> parseParametersPositionalTyped(
    final QCommandType command,
    final List<String> mutableArguments,
    final ValidatedPositionalsTyped typed)
    throws QException
  {
    final var validated = typed.values();
    if (validated.size() != mutableArguments.size()) {
      throw exceptionError(
        this.errorPositionalArgumentsWrongCount(
          command,
          validated.size(),
          mutableArguments.size()
        )
      );
    }

    final var positionalsResults = new ArrayList<>(validated.size());
    for (int index = 0; index < validated.size(); ++index) {
      final var argument =
        mutableArguments.get(index);
      final var parameter =
        validated.get(index);

      try {
        final var parsedValue =
          parameter.valueConverter.convertFromString(argument);

        positionalsResults.add(parsedValue);
      } catch (final Exception e) {
        throw exceptionError(
          this.errorValueUnparseablePositional(parameter, argument, e)
        );
      }
    }

    return positionalsResults;
  }

  private void parseParametersNamed(
    final Map<String, ValidatedNamed<?>> byName,
    final HashMap<QParameterNamedType<?>, List<Object>> parsedValues,
    final Collection<String> arguments)
    throws QException
  {
    final var iterator =
      arguments.iterator();

    while (iterator.hasNext()) {
      final var argument =
        iterator.next();

      /*
       * If there is no parameter definition, then assume we've started
       * parsing positional arguments.
       */

      final var parameter = byName.get(argument);
      if (parameter == null) {
        break;
      }

      iterator.remove();

      /*
       * Adding the parameter value might violate the cardinality of the
       * parameter.
       */

      this.checkParameterAddObeysCardinality(parameter, parsedValues);

      if (!iterator.hasNext()) {
        throw this.exceptionErrorValueMissingForParameter(parameter);
      }

      final var argumentValue = iterator.next();
      iterator.remove();

      try {
        final var parsedValue =
          parameter.valueConverter.convertFromString(argumentValue);

        final ArrayList<Object> valuesList =
          (ArrayList<Object>) parsedValues.getOrDefault(
            parameter.parameter,
            new ArrayList<>()
          );

        valuesList.add(parsedValue);
        parsedValues.put(parameter.parameter, valuesList);
      } catch (final Exception e) {
        throw this.exceptionErrorValueUnparseable(parameter, argumentValue, e);
      }
    }

    this.checkParametersObeyCardinality(byName, parsedValues);
  }

  private void checkParametersObeyCardinality(
    final Map<String, ValidatedNamed<?>> byName,
    final Map<QParameterNamedType<?>, List<Object>> parsedValues)
    throws QException
  {
    final var errors =
      new ArrayList<SStructuredErrorType<String>>();

    for (final var parameter : byName.values()) {
      final var p =
        parameter.parameter;
      final var values =
        parsedValues.getOrDefault(p, List.of());

      if (p instanceof final QParameterNamed0N<?> p0n) {
        if (values.isEmpty()) {
          values.addAll(p0n.defaultValue());
        }
        continue;
      }

      if (p instanceof final QParameterNamed1N<?> p1n) {
        if (values.isEmpty()) {
          p1n.defaultValue().ifPresent(values::add);
        }

        if (values.size() < 1) {
          errors.add(this.errorCardinalityViolation(parameter, values));
        }
        continue;
      }

      if (p instanceof final QParameterNamed1<?> p1) {
        if (values.isEmpty()) {
          p1.defaultValue().ifPresent(values::add);
        }
        if (values.size() != 1) {
          errors.add(this.errorCardinalityViolation(parameter, values));
        }
        continue;
      }

      if (p instanceof final QParameterNamed01<?> p01) {
        if (values.isEmpty()) {
          p01.defaultValue().ifPresent(values::add);
        }

        if (values.size() > 1) {
          errors.add(this.errorCardinalityViolation(parameter, values));
        }
      }
    }

    throwExceptionIfNecessary(errors);
  }

  private void checkParameterAddObeysCardinality(
    final ValidatedNamed<?> validated,
    final Map<QParameterNamedType<?>, List<Object>> parsedValues)
    throws QException
  {
    final var parameter = validated.parameter();
    if (parameter instanceof QParameterNamed0N<?>) {
      return;
    }
    if (parameter instanceof QParameterNamed01<?>) {
      return;
    }
    if (parameter instanceof QParameterNamed1<?>) {
      final var existing = parsedValues.get(parameter);
      if (!existing.isEmpty()) {
        throw exceptionError(this.errorTooManyValuesProvided(validated));
      }
    }
    if (parameter instanceof QParameterNamed1N<?>) {
      return;
    }
  }

  private QException exceptionErrorValueUnparseable(
    final ValidatedNamed<?> parameter,
    final String argumentValue,
    final Exception e)
  {
    return exceptionError(
      this.errorValueUnparseableNamed(parameter, argumentValue, e)
    );
  }

  private SStructuredErrorType<String> errorValueUnparseableNamed(
    final ValidatedNamed<?> parameter,
    final String argumentValue,
    final Exception e)
  {
    return new SStructuredError<>(
      "parameter-unparseable-value",
      this.errorParameterUnparseable(),
      Map.ofEntries(
        Map.entry(this.command(), parameter.command.metadata().name()),
        Map.entry(this.parameter(), parameter.parameter.name()),
        Map.entry(this.provided(), argumentValue),
        Map.entry(this.type(), parameter.parameter.type().getCanonicalName()),
        Map.entry(this.syntax(), parameter.valueConverter().syntax())
      ),
      Optional.of(this.errorSuggestProvideParseable()),
      Optional.of(e)
    );
  }

  private String errorSuggestProvideParseable()
  {
    return this.localize("quarrel.errorSuggestProvideParseable");
  }

  private String errorParameterUnparseable()
  {
    return this.localize("quarrel.errorParameterUnparseable");
  }

  private String provided()
  {
    return this.localize("quarrel.provided");
  }

  private SStructuredErrorType<String> errorValueUnparseablePositional(
    final ValidatedPositional<?> parameter,
    final String argumentValue,
    final Exception e)
  {
    return new SStructuredError<>(
      "parameter-unparseable-value",
      this.errorParameterUnparseable(),
      Map.ofEntries(
        Map.entry(this.command(), parameter.command.metadata().name()),
        Map.entry(this.parameter(), parameter.parameter.name()),
        Map.entry(this.provided(), argumentValue),
        Map.entry(this.type(), parameter.parameter.type().getCanonicalName()),
        Map.entry(this.syntax(), parameter.valueConverter().syntax())
      ),
      Optional.of(this.errorSuggestProvideParseable()),
      Optional.of(e)
    );
  }

  private String syntax()
  {
    return this.localize("quarrel.syntax");
  }

  private QException exceptionErrorValueMissingForParameter(
    final ValidatedNamed<?> parameter)
  {
    return exceptionError(this.errorValueMissingForParameter(parameter));
  }

  private SStructuredErrorType<String> errorValueMissingForParameter(
    final ValidatedNamed<?> parameter)
  {
    return new SStructuredError<>(
      "parameter-missing-value",
      this.errorParameterMissingValue(),
      Map.ofEntries(
        Map.entry(
          this.command(),
          parameter.command.metadata().name()),
        Map.entry(
          this.parameter(),
          parameter.parameter.name()),
        Map.entry(
          this.type(),
          parameter.parameter.type().getCanonicalName()),
        Map.entry(
          this.syntax(),
          parameter.valueConverter.syntax())
      ),
      Optional.of(this.errorSuggestProvideValue()),
      Optional.empty()
    );
  }

  private String errorSuggestProvideValue()
  {
    return this.localize("quarrel.errorSuggestProvideValue");
  }

  private String errorParameterMissingValue()
  {
    return this.localize("quarrel.errorParameterMissingValue");
  }

  private String example()
  {
    return this.localize("quarrel.example");
  }

  private SStructuredErrorType<String> errorNoValueConverterForParameter(
    final QCommandType command,
    final String name,
    final Class<?> type)
  {
    return new SStructuredError<>(
      "parameter-no-value-converter",
      this.errorNoValueConverter(),
      Map.ofEntries(
        Map.entry(this.command(), command.metadata().name()),
        Map.entry(this.parameter(), name),
        Map.entry(this.type(), type.getCanonicalName())
      ),
      Optional.of(this.errorSuggestRegisterValueConverter()),
      Optional.empty()
    );
  }

  private String errorSuggestRegisterValueConverter()
  {
    return this.localize("quarrel.errorSuggestRegisterConverter");
  }

  private String errorNoValueConverter()
  {
    return this.localize("quarrel.errorParameterNoValueConverter");
  }

  private SStructuredError<String> errorDuplicateParameterName(
    final QCommandType command,
    final String name)
  {
    return new SStructuredError<>(
      "parameter-duplicate",
      this.errorMultipleParametersSameName(),
      Map.ofEntries(
        Map.entry(this.command(), command.metadata().name()),
        Map.entry(this.parameter(), name)
      ),
      Optional.of(this.errorSuggestUniqueNamesParameters()),
      Optional.empty()
    );
  }

  private String errorSuggestUniqueNamesParameters()
  {
    return this.localize("quarrel.errorSuggestUniqueNames");
  }

  private String errorMultipleParametersSameName()
  {
    return this.localize("quarrel.errorParameterMultipleSameNames");
  }

  private SStructuredErrorType<String> errorPositionalArgumentsWrongCount(
    final QCommandType command,
    final int countExpected,
    final int countReceived)
  {
    return new SStructuredError<>(
      "parameter-positional-count",
      this.errorWrongNumberOfPositionalArguments(),
      Map.ofEntries(
        Map.entry(this.command(), command.metadata().name()),
        Map.entry(this.expectedCount(), toUnsignedString(countExpected)),
        Map.entry(this.providedCount(), toUnsignedString(countReceived))
      ),
      Optional.of(this.errorSuggestProvideRightNumber()),
      Optional.empty()
    );
  }

  private String errorWrongNumberOfPositionalArguments()
  {
    return this.localize("quarrel.errorWrongNumberOfPositionalArguments");
  }

  private String providedCount()
  {
    return this.localize("quarrel.provided_count");
  }

  private String expectedCount()
  {
    return this.localize("quarrel.expected_count");
  }

  private SStructuredErrorType<String> errorTooManyValuesProvided(
    final ValidatedNamed<?> parameter)
  {
    return new SStructuredError<>(
      "parameter-cardinality",
      this.errorExpectsOneValue(),
      Map.ofEntries(
        Map.entry(this.command(), parameter.command.metadata().name()),
        Map.entry(this.parameter(), parameter.parameter.name()),
        Map.entry(this.type(), parameter.parameter.type().getCanonicalName())
      ),
      Optional.of(this.errorSuggestProvideExactlyOne()),
      Optional.empty()
    );
  }

  private String errorExpectsOneValue()
  {
    return this.localize("quarrel.errorExpectsOneValue");
  }

  private String errorSuggestProvideExactlyOne()
  {
    return this.localize("quarrel.errorSuggestProvideExactlyOne");
  }

  private String type()
  {
    return this.localize("quarrel.type");
  }

  private Map<String, ValidatedNamed<?>> validateNamedParameters(
    final QCommandType command)
    throws QException
  {
    final var parametersByName =
      new HashMap<String, ValidatedNamed<?>>();
    final var errors =
      new ArrayList<SStructuredErrorType<String>>();

    for (final var parameter : command.onListNamedParameters()) {
      this.validateParameterNameUnique(
        command,
        parametersByName,
        errors,
        parameter,
        parameter.name()
      );

      for (final var name : parameter.nameAlternatives()) {
        this.validateParameterNameUnique(
          command,
          parametersByName,
          errors,
          parameter,
          name
        );
      }
    }

    throwExceptionIfNecessary(errors);
    return parametersByName;
  }

  private void validateParameterNameUnique(
    final QCommandType command,
    final Map<String, ValidatedNamed<?>> parametersByName,
    final Collection<SStructuredErrorType<String>> errors,
    final QParameterNamedType<?> parameter,
    final String name)
  {
    final var converters =
      this.configuration.converters();

    if (parametersByName.containsKey(name)) {
      errors.add(this.errorDuplicateParameterName(command, name));
      return;
    }

    final var converter =
      converters.converterFor(parameter.type());

    if (converter.isEmpty()) {
      errors.add(
        this.errorNoValueConverterForParameter(command, name, parameter.type())
      );
      return;
    }

    final var validated =
      new ValidatedNamed<Object>(
        (QParameterNamedType<Object>) parameter,
        command,
        (QValueConverterType<Object>) converter.get()
      );

    parametersByName.put(name, validated);
  }

  private ValidatedPositionalsType validatePositionalParameters(
    final QCommandType command,
    final QParametersPositionalType positionals)
    throws QException
  {
    final var errors =
      new ArrayList<SStructuredErrorType<String>>();

    if (positionals instanceof final QParametersPositionalTyped typed) {
      return this.validatePositionalParametersTyped(command, errors, typed);
    }

    if (positionals instanceof QParametersPositionalAny) {
      return ValidatedPositionalsAny.ANY;
    }

    if (positionals instanceof QParametersPositionalNone) {
      return ValidatedPositionalsNone.NONE;
    }

    throw new IllegalStateException("Unreachable code.");
  }

  private ValidatedPositionalsTyped validatePositionalParametersTyped(
    final QCommandType command,
    final ArrayList<SStructuredErrorType<String>> errors,
    final QParametersPositionalTyped typed)
    throws QException
  {
    final var converters =
      this.configuration.converters();

    final var results =
      new ArrayList<ValidatedPositional<?>>(typed.parameters().size());

    for (final var p : typed.parameters()) {
      final var converter =
        converters.converterFor(p.type());

      if (converter.isEmpty()) {
        errors.add(
          this.errorNoValueConverterForParameter(command, p.name(), p.type())
        );
        continue;
      }

      results.add(
        new ValidatedPositional<>(
          (QParameterPositional<Object>) p,
          command,
          (QValueConverterType<Object>) converter.get()
        )
      );
    }

    throwExceptionIfNecessary(errors);
    return new ValidatedPositionalsTyped(results);
  }

  private SStructuredErrorType<String> errorCardinalityViolation(
    final ValidatedNamed<?> parameter,
    final Collection<Object> values)
  {
    return new SStructuredError<>(
      "parameter-cardinality",
      this.errorWrongNumberOfValues(),
      Map.ofEntries(
        Map.entry(
          this.command(),
          parameter.command.metadata().name()),
        Map.entry(
          this.parameter(),
          parameter.parameter.name()),
        Map.entry(
          this.minimumValues(),
          toUnsignedString(parameter.parameter.cardinalityMinimum())),
        Map.entry(
          this.maximumValues(),
          toUnsignedString(parameter.parameter.cardinalityMaximum())),
        Map.entry(
          this.providedCount(),
          toUnsignedString(values.size()))
      ),
      Optional.of(this.errorSuggestProvideRightNumber()),
      Optional.empty()
    );
  }

  private String errorWrongNumberOfValues()
  {
    return this.localize("quarrel.errorWrongNumberOfValues");
  }

  private String errorSuggestProvideRightNumber()
  {
    return this.localize("quarrel.errorSuggestProvideRightNumber");
  }

  private String maximumValues()
  {
    return this.localize("quarrel.maximum_values");
  }

  private String minimumValues()
  {
    return this.localize("quarrel.minimum_values");
  }

  private String parameter()
  {
    return this.localize("quarrel.parameter");
  }

  private String localize(
    final String id)
  {
    return this.localization.localize(new QLocalize(id));
  }

  private String command()
  {
    return this.localize("quarrel.command");
  }

  @Override
  public QCommandContextType execute(
    final SortedMap<String, QCommandOrGroupType> tree,
    final PrintWriter output,
    final QCommandType command,
    final List<String> arguments)
    throws QException
  {
    Objects.requireNonNull(tree, "tree");
    Objects.requireNonNull(output, "output");
    Objects.requireNonNull(command, "command");
    Objects.requireNonNull(arguments, "arguments");

    final var byName =
      this.validateNamedParameters(command);
    final var startPositionals =
      command.onListPositionalParameters();
    final var positionals =
      this.validatePositionalParameters(command, startPositionals);

    final var parsedNamedValues =
      new HashMap<QParameterNamedType<?>, List<Object>>();

    for (final var parameter : byName.values()) {
      final var values = new ArrayList<>();
      parsedNamedValues.put(parameter.parameter, values);
    }

    final var mutableArguments = new ArrayList<>(arguments);
    this.parseParametersNamed(byName, parsedNamedValues, mutableArguments);

    final var rawPositionalValues =
      List.copyOf(mutableArguments);
    final var parsedPositionalValues =
      this.parseParametersPositional(command, positionals, mutableArguments);

    return new QCommandContext(
      tree,
      this.configuration.converters(),
      output,
      command,
      this.localization,
      parsedNamedValues,
      startPositionals,
      parsedPositionalValues,
      rawPositionalValues
    );
  }

  private enum ValidatedPositionalsNone
    implements ValidatedPositionalsType
  {
    NONE
  }

  private enum ValidatedPositionalsAny
    implements ValidatedPositionalsType
  {
    ANY
  }

  private sealed interface ValidatedPositionalsType
  {

  }

  private record ValidatedNamed<T>(
    QParameterNamedType<T> parameter,
    QCommandType command,
    QValueConverterType<T> valueConverter)
  {

  }

  private record ValidatedPositionalsTyped(
    List<ValidatedPositional<?>> values)
    implements ValidatedPositionalsType
  {

  }

  private record ValidatedPositional<T>(
    QParameterPositional<T> parameter,
    QCommandType command,
    QValueConverterType<T> valueConverter)
  {

  }

}
