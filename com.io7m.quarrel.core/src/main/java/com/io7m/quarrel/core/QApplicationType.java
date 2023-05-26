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
import com.io7m.seltzer.api.SStructuredErrorExceptionType;
import org.slf4j.Logger;

import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedMap;

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
    } catch (final QException ex) {
      QErrorFormatting.format(this, ex, s -> logger.error("{}", s));
      for (final var error : ex.extraErrors()) {
        QErrorFormatting.format(this, error, s -> logger.error("{}", s));
      }
      logger.debug(
        "{}: ",
        this.localize(new QLocalize("quarrel.exception")),
        ex);
      return QCommandStatus.FAILURE;
    } catch (final Exception e) {
      if (e instanceof final SStructuredErrorExceptionType<?> se) {
        QErrorFormatting.format(this, se, s -> logger.error("{}", s));
      } else {
        logger.error("{}", e.getMessage());
      }
      logger.debug(
        "{}: ",
        this.localize(new QLocalize("quarrel.exception")),
        e);
      return QCommandStatus.FAILURE;
    }
  }
}
