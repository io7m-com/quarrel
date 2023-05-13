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


package com.io7m.quarrel.core.converters;

import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QValueConverterType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An enum converter.
 *
 * @param <T> The base enum type
 */

public final class QVCEnum<T extends Enum<T>>
  implements QValueConverterType<T>
{
  private final Class<T> enumClass;

  /**
   * An enum converter.
   *
   * @param inEnumClass The base enum class
   */

  public QVCEnum(
    final Class<T> inEnumClass)
  {
    this.enumClass =
      Objects.requireNonNull(inEnumClass, "enumClass");
  }

  @Override
  public T convertFromString(
    final String text)
    throws QException
  {
    final var constants =
      this.enumClass.getEnumConstants();

    for (final var constant : constants) {
      if (Objects.equals(constant.name(), text)) {
        return constant;
      }
    }

    throw new QException(
      "No enum value exists with the given name.",
      "parameter-value-unparseable",
      Map.of(),
      Optional.empty(),
      List.of()
    );
  }

  @Override
  public String convertToString(
    final T value)
  {
    return value.name();
  }

  @Override
  public T exampleValue()
  {
    return Arrays.stream(this.enumClass.getEnumConstants())
      .findFirst()
      .orElseThrow(() -> new IllegalStateException(
        "Could not produce an example value of type %s".formatted(this.convertedClass()))
      );
  }

  @Override
  public String syntax()
  {
    return Arrays.stream(this.enumClass.getEnumConstants())
      .sorted()
      .map(Enum::name)
      .collect(Collectors.joining("|"));
  }

  @Override
  public Class<T> convertedClass()
  {
    return this.enumClass;
  }
}
