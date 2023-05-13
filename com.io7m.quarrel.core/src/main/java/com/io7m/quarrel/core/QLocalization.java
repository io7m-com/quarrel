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

import com.io7m.quarrel.core.internal.QEmptyResources;
import com.io7m.quarrel.core.internal.QStrings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Convenient localization methods.
 */

public final class QLocalization implements QLocalizationType
{
  private final ResourceBundle internalResources;
  private final ResourceBundle applicationResources;

  /**
   * Convenient localization methods.
   *
   * @param inInternalResources    The internal resources
   * @param inApplicationResources The application-specific resources
   */

  private QLocalization(
    final ResourceBundle inInternalResources,
    final ResourceBundle inApplicationResources)
  {
    this.internalResources =
      Objects.requireNonNull(inInternalResources, "internalResources");
    this.applicationResources =
      Objects.requireNonNull(inApplicationResources, "applicationResources");
  }

  /**
   * Create a localizer.
   *
   * @param inInternalResources    The internal resources
   * @param inApplicationResources The application-specific resources
   *
   * @return A localizer
   */

  public static QLocalizationType create(
    final ResourceBundle inInternalResources,
    final ResourceBundle inApplicationResources)
  {
    return new QLocalization(inInternalResources, inApplicationResources);
  }

  /**
   * Create a localizer.
   *
   * @param locale                 The locale for the internal resources
   * @param inApplicationResources The application-specific resources
   *
   * @return A localizer
   */

  public static QLocalizationType create(
    final Locale locale,
    final ResourceBundle inApplicationResources)
  {
    try {
      return new QLocalization(
        new QStrings(locale).resources(),
        inApplicationResources
      );
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Create a localizer without application-specific resources.
   *
   * @param locale The locale for the internal resources
   *
   * @return A localizer
   */

  public static QLocalizationType create(
    final Locale locale)
  {
    try {
      return new QLocalization(
        new QStrings(locale).resources(),
        new QEmptyResources()
      );
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public String localize(
    final QStringType string)
  {
    Objects.requireNonNull(string, "string");

    if (string instanceof final QStringType.QConstant c) {
      return c.text();
    }

    if (string instanceof final QStringType.QLocalize local) {
      try {
        return this.applicationResources.getString(local.id());
      } catch (final MissingResourceException e) {
        return this.internalResources.getString(local.id());
      }
    }

    throw new IllegalStateException("Unreachable code.");
  }

  @Override
  public String format(
    final QStringType string,
    final Object... arguments)
  {
    Objects.requireNonNull(string, "string");
    Objects.requireNonNull(arguments, "arguments");

    return MessageFormat.format(this.localize(string), arguments);
  }
}
