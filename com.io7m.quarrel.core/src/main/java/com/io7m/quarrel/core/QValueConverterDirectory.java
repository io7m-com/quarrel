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

import com.io7m.quarrel.core.converters.QVCBigDecimal;
import com.io7m.quarrel.core.converters.QVCBigInteger;
import com.io7m.quarrel.core.converters.QVCBoolean;
import com.io7m.quarrel.core.converters.QVCDouble;
import com.io7m.quarrel.core.converters.QVCDuration;
import com.io7m.quarrel.core.converters.QVCFloat;
import com.io7m.quarrel.core.converters.QVCInetAddress;
import com.io7m.quarrel.core.converters.QVCInteger;
import com.io7m.quarrel.core.converters.QVCLong;
import com.io7m.quarrel.core.converters.QVCOffsetDateTime;
import com.io7m.quarrel.core.converters.QVCPath;
import com.io7m.quarrel.core.converters.QVCString;
import com.io7m.quarrel.core.converters.QVCURI;
import com.io7m.quarrel.core.converters.QVCUUID;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The default value converter directory.
 */

public final class QValueConverterDirectory
  implements QValueConverterDirectoryType
{
  private final Map<Class<?>, QValueConverterType<?>> converters;

  private QValueConverterDirectory(
    final Map<Class<?>, QValueConverterType<?>> inConverters)
  {
    this.converters =
      Objects.requireNonNull(inConverters, "inConverters");
  }

  /**
   * @return An empty value converter directory.
   */

  public static QValueConverterDirectoryType empty()
  {
    return new QValueConverterDirectory(Map.of());
  }

  /**
   * @return A value converter directory containing a useful set of core types.
   */

  public static QValueConverterDirectoryType core()
  {
    return new QValueConverterDirectory(Map.ofEntries(
      Map.entry(BigDecimal.class, QVCBigDecimal.get()),
      Map.entry(BigInteger.class, QVCBigInteger.get()),
      Map.entry(Boolean.class, QVCBoolean.get()),
      Map.entry(Double.class, QVCDouble.get()),
      Map.entry(Duration.class, QVCDuration.get()),
      Map.entry(Float.class, QVCFloat.get()),
      Map.entry(InetAddress.class, QVCInetAddress.get()),
      Map.entry(Integer.class, QVCInteger.get()),
      Map.entry(Long.class, QVCLong.get()),
      Map.entry(OffsetDateTime.class, QVCOffsetDateTime.get()),
      Map.entry(Path.class, QVCPath.get()),
      Map.entry(String.class, QVCString.get()),
      Map.entry(URI.class, QVCURI.get()),
      Map.entry(UUID.class, QVCUUID.get())
    ));
  }

  @Override
  public <T> Optional<QValueConverterType<T>> converterFor(
    final Class<T> type)
  {
    final QValueConverterType<?> converter = this.converters.get(type);
    if (converter == null) {
      return Optional.empty();
    }
    return Optional.of((QValueConverterType<T>) converter);
  }

  @Override
  public Collection<QValueConverterType<?>> converters()
  {
    return this.converters.values();
  }

  @Override
  public <T> QValueConverterDirectoryType with(
    final Class<T> clazz,
    final QValueConverterType<T> converter)
  {
    final var existing = new HashMap<>(this.converters);
    existing.put(clazz, converter);
    return new QValueConverterDirectory(Map.copyOf(existing));
  }
}
