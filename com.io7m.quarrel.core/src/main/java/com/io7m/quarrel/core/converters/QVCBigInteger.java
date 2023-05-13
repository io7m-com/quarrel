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

import java.math.BigInteger;

/**
 * A value converter.
 */

public final class QVCBigInteger
  extends QVCAbstract<BigInteger>
{
  private static final QVCBigInteger INSTANCE = new QVCBigInteger();

  private QVCBigInteger()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<BigInteger> get()
  {
    return INSTANCE;
  }

  @Override
  protected BigInteger parse(
    final String text)
    throws Exception
  {
    return new BigInteger(text);
  }

  @Override
  public String convertToString(
    final BigInteger value)
  {
    return value.toString();
  }

  @Override
  public BigInteger exampleValue()
  {
    return BigInteger.TEN;
  }

  @Override
  public String syntax()
  {
    return "0 | [1-9][0-9]+";
  }

  @Override
  public Class<BigInteger> convertedClass()
  {
    return BigInteger.class;
  }
}
