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
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;

/**
 * The context of execution for a command. Typically used to give commands
 * access to parsed, typed parameters.
 */

public interface QCommandContextType
  extends QLocalizationType
{
  /**
   * @return Access to the command tree
   */

  SortedMap<String, QCommandOrGroupType> commandTree();

  /**
   * @return The output writer
   */

  PrintWriter output();

  /**
   * The value converters used to produce this context.
   *
   * @return The value converters
   */

  QValueConverterDirectoryType valueConverters();

  /**
   * @return The command to which this context belongs
   */

  QCommandType command();

  /**
   * @return The raw positional parameters, if any
   */

  List<String> parametersPositionalRaw();

  /**
   * @param parameter The parameter
   * @param <T>       The parameter type
   *
   * @return The value for the parameter
   */

  <T> T parameterValue(QParameterPositional<T> parameter);

  /**
   * @param parameter The parameter
   * @param <T>       The parameter type
   *
   * @return The value for the parameter
   */

  <T> T parameterValue(QParameterNamed1<T> parameter);

  /**
   * @param parameter The parameter
   * @param <T>       The parameter type
   *
   * @return The value for the parameter, if one was provided
   */

  <T> Optional<T> parameterValue(QParameterNamed01<T> parameter);

  /**
   * @param parameter The parameter
   * @param <T>       The parameter type
   *
   * @return The values for the parameter
   */

  <T> List<T> parameterValues(QParameterNamed1N<T> parameter);

  /**
   * @param parameter The parameter
   * @param <T>       The parameter type
   *
   * @return The values for the parameter
   */

  <T> List<T> parameterValues(QParameterNamed0N<T> parameter);

  /**
   * Execute the command.
   *
   * @return The result of execution
   *
   * @throws Exception On errors
   */

  QCommandStatus execute()
    throws Exception;
}
