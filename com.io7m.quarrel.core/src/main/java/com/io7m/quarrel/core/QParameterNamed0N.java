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

import java.util.List;
import java.util.Objects;

/**
 * A named parameter with [0, N] cardinality; any number of values may be
 * present.
 *
 * @param name             The name
 * @param nameAlternatives The alternative names
 * @param description      The parameter description string
 * @param defaultValue     The default value
 * @param <T>              The type of values
 * @param type             The type of values
 */

public record QParameterNamed0N<T>(
  String name,
  List<String> nameAlternatives,
  QStringType description,
  List<T> defaultValue,
  Class<T> type)
  implements QParameterNamedType<T>
{
  /**
   * A named parameter with [0, N] cardinality; any number of values may be
   * present.
   *
   * @param name             The name
   * @param nameAlternatives The alternative names
   * @param description      The parameter description string
   * @param defaultValue     The default value
   * @param type             The type of values
   */

  public QParameterNamed0N
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(nameAlternatives, "nameAlternatives");
    Objects.requireNonNull(description, "description");
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(type, "type");

    QNames.checkOK(name);
  }

  @Override
  public int cardinalityMinimum()
  {
    return 0;
  }

  @Override
  public int cardinalityMaximum()
  {
    return Integer.MAX_VALUE;
  }
}
