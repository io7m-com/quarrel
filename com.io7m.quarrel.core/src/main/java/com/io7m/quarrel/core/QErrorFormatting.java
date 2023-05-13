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

import com.io7m.seltzer.api.SStructuredErrorType;

import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

import static java.lang.Math.max;

/**
 * Standard functions to format errors.
 */

public final class QErrorFormatting
{
  private QErrorFormatting()
  {

  }

  /**
   * Format the given error, passing the formatted message to the given
   * consumer.
   *
   * @param localization The localizer
   * @param error        The error
   * @param consumer     The consumer
   */

  public static void format(
    final QLocalizationType localization,
    final SStructuredErrorType<?> error,
    final Consumer<String> consumer)
  {
    Objects.requireNonNull(localization, "localization");
    Objects.requireNonNull(error, "e");
    Objects.requireNonNull(consumer, "consumer");

    final var attributeTable = new TreeMap<>(error.attributes());
    attributeTable.put(
      localization.localize(new QStringType.QLocalize("quarrel.error_code")),
      error.errorCode().toString()
    );
    error.remediatingAction().ifPresent(act -> {
      attributeTable.put(
        localization.localize(
          new QStringType.QLocalize("quarrel.suggested_action")),
        act
      );
    });

    int maxLength = 0;
    for (final var entry : attributeTable.entrySet()) {
      maxLength = max(entry.getKey().length(), maxLength);
    }
    maxLength = maxLength + 1;

    final var text = new StringBuilder(attributeTable.size() * 32);
    text.append(error.message());
    text.append(System.lineSeparator());

    for (final var entry : attributeTable.entrySet()) {
      final var name = entry.getKey();
      final var value = entry.getValue();
      text.append("  ");
      text.append(name);
      text.append(" ".repeat(maxLength - name.length()));
      text.append(": ");
      text.append(value);
      text.append(System.lineSeparator());
    }

    consumer.accept(text.toString());
  }
}
