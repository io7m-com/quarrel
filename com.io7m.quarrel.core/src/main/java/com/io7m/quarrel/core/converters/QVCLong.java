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

/**
 * A value converter.
 */

public final class QVCLong
  extends QVCAbstract<Long>
{
  private static final QVCLong INSTANCE = new QVCLong();

  private QVCLong()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<Long> get()
  {
    return INSTANCE;
  }

  @Override
  protected Long parse(final String text)
    throws Exception
  {
    return Long.valueOf(Long.parseLong(text));
  }

  @Override
  public String convertToString(
    final Long value)
  {
    return value.toString();
  }

  @Override
  public Long exampleValue()
  {
    return 23L;
  }

  @Override
  public String syntax()
  {
    return "0 | [1-9][0-9]+";
  }

  @Override
  public Class<Long> convertedClass()
  {
    return Long.class;
  }
}
