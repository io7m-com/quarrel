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

import com.io7m.quarrel.core.internal.QCommandParser;
import com.io7m.quarrel.core.internal.QEmptyResources;
import com.io7m.quarrel.core.internal.QStrings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The default factory of command parsers.
 */

public final class QCommandParsers implements QCommandParserFactoryType
{
  private static final QEmptyResources EMPTY_RESOURCES =
    new QEmptyResources();

  /**
   * The default factory of command parsers.
   */

  public QCommandParsers()
  {

  }

  /**
   * @return A convenient immutable empty resource bundle
   */

  public static ResourceBundle emptyResources()
  {
    return EMPTY_RESOURCES;
  }

  @Override
  public QCommandParserType create(
    final QCommandParserConfiguration configuration)
  {
    final QStrings strings;
    try {
      strings = new QStrings(Locale.getDefault());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
    return new QCommandParser(configuration, strings);
  }
}
