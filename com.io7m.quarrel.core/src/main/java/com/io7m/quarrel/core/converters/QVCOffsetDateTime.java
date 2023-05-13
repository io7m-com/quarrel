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

import com.io7m.quarrel.core.QValueConverterType;

import java.time.OffsetDateTime;

/**
 * A value converter.
 */

public final class QVCOffsetDateTime
  extends QVCAbstract<OffsetDateTime>
{
  private static final QVCOffsetDateTime INSTANCE =
    new QVCOffsetDateTime();
  private static final OffsetDateTime EXAMPLE =
    OffsetDateTime.now();

  private QVCOffsetDateTime()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<OffsetDateTime> get()
  {
    return INSTANCE;
  }

  @Override
  protected OffsetDateTime parse(
    final String text)
  {
    return OffsetDateTime.parse(text);
  }

  @Override
  public String convertToString(
    final OffsetDateTime value)
  {
    return value.toString();
  }

  @Override
  public OffsetDateTime exampleValue()
  {
    return EXAMPLE;
  }

  @Override
  public String syntax()
  {
    return "yyyy-mm-ddThh:mm:ss+zz:zz (ISO 8601)";
  }

  @Override
  public Class<OffsetDateTime> convertedClass()
  {
    return OffsetDateTime.class;
  }
}
