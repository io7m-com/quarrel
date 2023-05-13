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

public final class QVCDouble
  extends QVCAbstract<Double>
{
  private static final QVCDouble INSTANCE = new QVCDouble();

  private QVCDouble()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<Double> get()
  {
    return INSTANCE;
  }

  @Override
  protected Double parse(final String text)
    throws Exception
  {
    return Double.valueOf(Double.parseDouble(text));
  }

  @Override
  public String convertToString(
    final Double value)
  {
    return value.toString();
  }

  @Override
  public Double exampleValue()
  {
    return Double.valueOf(23.0);
  }

  @Override
  public String syntax()
  {
    return "<floating-point value>";
  }

  @Override
  public Class<Double> convertedClass()
  {
    return Double.class;
  }
}
