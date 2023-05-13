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
import com.io7m.seltzer.api.SStructuredErrorType;
import org.slf4j.Logger;

import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.lang.Math.max;

/**
 * An application.
 */

public interface QApplicationType
  extends QLocalizationType
{
  /**
   * @return The application metadata
   */

  QApplicationMetadata metadata();

  /**
   * @return The tree of commands and command groups
   */

  SortedMap<String, QCommandOrGroupType> commandTree();

  /**
   * @return The internal resources used to localize strings
   */

  ResourceBundle internalResources();

  /**
   * @return The application-specific resources used to localize strings
   */

  ResourceBundle applicationResources();

  /**
   * @return The value converters used to parse commands
   */

  QValueConverterDirectoryType valueConverters();

  /**
   * Parse the given command line arguments, returning an appropriate command
   * context for execution.
   *
   * @param arguments The arguments
   *
   * @return A command context
   *
   * @throws QException On errors
   */

  QCommandContextType parse(List<String> arguments)
    throws QException;

  /**
   * A convenience method that runs this application for the given arguments.
   * Arguments are parsed, and a suitable command is executed. Parse and other
   * errors are logged to the given error logger.
   *
   * @param logger    The receiver of log messages
   * @param arguments The arguments
   *
   * @return The executed command status
   */

  default QCommandStatus run(
    final Logger logger,
    final List<String> arguments)
  {
    try {
      return this.parse(arguments).execute();
    } catch (final QException e) {
      this.formatStructuredError(logger, e);
      for (final var error : e.extraErrors()) {
        this.formatStructuredError(logger, error);
      }
      logger.debug(
        "{}: ",
        this.localize(new QLocalize("quarrel.exception")),
        e);
      return QCommandStatus.FAILURE;
    } catch (final Exception e) {
      logger.error("{}", e.getMessage());
      logger.debug(
        "{}: ",
        this.localize(new QLocalize("quarrel.exception")),
        e);
      return QCommandStatus.FAILURE;
    }
  }

  private void formatStructuredError(
    final Logger logger,
    final SStructuredErrorType<String> e)
  {
    final var attributeTable = new TreeMap<>(e.attributes());
    attributeTable.put(
      this.localize(new QLocalize("quarrel.error_code")),
      e.errorCode()
    );
    e.remediatingAction().ifPresent(act -> {
      attributeTable.put(
        this.localize(new QLocalize("quarrel.suggested_action")),
        act
      );
    });

    int maxLength = 0;
    for (final var entry : attributeTable.entrySet()) {
      maxLength = max(entry.getKey().length(), maxLength);
    }
    maxLength = maxLength + 1;

    final var text = new StringBuilder(attributeTable.size() * 32);
    text.append(e.message());
    text.append(System.lineSeparator());

    for (final var entry : attributeTable.entrySet()) {
      final var name = entry.getKey();
      final var value = entry.getValue();
      text.append("  ");
      text.append(name);
      text.append(" ".repeat(maxLength - name.length()));
      text.append(value);
      text.append(System.lineSeparator());
    }
    logger.error("{}", text);
  }
}
