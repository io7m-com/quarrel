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

/**
 * <p>The type of value converters.</p>
 * <p>For all implementations,
 * {@code forall x, x = convertFromString(convertToString(x))}.</p>
 *
 * @param <T> The type of converted values
 */

public interface QValueConverterType<T>
{
  /**
   * Convert the given string to a value of type {@code T}.
   *
   * @param text The input string
   *
   * @return A value of {@code T}
   *
   * @throws QException On errors
   */

  T convertFromString(String text)
    throws QException;

  /**
   * Convert a value of {@code T} to a string.
   *
   * @param value The value
   *
   * @return A string
   */

  String convertToString(T value);

  /**
   * @return An example value used for documentation
   */

  T exampleValue();

  /**
   * @return A description of the syntax for valid values of {@code T}
   */

  String syntax();

  /**
   * @return The type that will be converted
   */

  Class<T> convertedClass();
}
