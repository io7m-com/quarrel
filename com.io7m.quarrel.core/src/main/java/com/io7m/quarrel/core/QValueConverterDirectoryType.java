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

import java.util.Collection;
import java.util.Optional;

/**
 * A directory of value converters.
 */

public interface QValueConverterDirectoryType
{
  /**
   * @param type The type
   * @param <T>  The type
   *
   * @return A converter for the given class, if one exists
   */

  <T> Optional<QValueConverterType<T>> converterFor(Class<T> type);

  /**
   * @return The set of converters in the directory
   */

  Collection<QValueConverterType<?>> converters();

  /**
   * Extend this value converter directory with the given converter, returning a
   * new directory.
   *
   * @param clazz     The class
   * @param <T>       The type
   * @param converter The converter
   *
   * @return A new directory with the given converter
   */

  <T> QValueConverterDirectoryType with(
    Class<T> clazz,
    QValueConverterType<T> converter);
}
