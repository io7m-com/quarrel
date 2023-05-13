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

/**
 * The type of named parameters.
 *
 * @param <T> The type of parameter
 */

public sealed interface QParameterNamedType<T>
  extends QParameterType<T>
  permits QParameterNamed01,
  QParameterNamed0N,
  QParameterNamed1,
  QParameterNamed1N
{
  /**
   * @return The alternative names, if any, by which this parameter can be
   * referred
   */

  List<String> nameAlternatives();

  /**
   * @return The minimum cardinality of the parameter (typically 0 or 1)
   */

  int cardinalityMinimum();

  /**
   * @return The maximum cardinality of the parameter (typically 1 or MAX_VALUE)
   */

  int cardinalityMaximum();
}
