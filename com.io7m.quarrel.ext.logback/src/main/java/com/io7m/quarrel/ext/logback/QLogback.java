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

package com.io7m.quarrel.ext.logback;

import ch.qos.logback.classic.Logger;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QStringType;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>A convenient extension to configure logging on the command-line.</p>
 *
 * <p>Add named parameters to a command using {@link #parameters()}, and then
 * call {@link #configure(QCommandContextType)} when executing the command.</p>
 */

public final class QLogback
{
  private static final QParameterNamed1<QLogLevel> VERBOSITY =
    new QParameterNamed1<>(
      "--verbose",
      List.of(),
      new QStringType.QConstant("Set the logging level of the application."),
      Optional.of(QLogLevel.info),
      QLogLevel.class
    );

  private QLogback()
  {

  }

  /**
   * @return The named parameters involving logging
   */

  public static List<QParameterNamedType<?>> parameters()
  {
    return List.of(VERBOSITY);
  }

  /**
   * Configure the root Logback logger based on the given verbosity parameters.
   *
   * @param context The command context
   */

  public static void configure(
    final QCommandContextType context)
  {
    Objects.requireNonNull(context, "context");

    final Logger root =
      (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(context.parameterValue(VERBOSITY).toLevel());
  }
}
