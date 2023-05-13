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

public final class QVCBoolean
  extends QVCAbstract<Boolean>
{
  private static final QVCBoolean INSTANCE = new QVCBoolean();

  private QVCBoolean()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<Boolean> get()
  {
    return INSTANCE;
  }

  @Override
  protected Boolean parse(
    final String text)
    throws Exception
  {
    return switch (text) {
      case "true" -> Boolean.TRUE;
      case "false" -> Boolean.FALSE;
      default -> throw new IllegalArgumentException("Expected 'true' or 'false'");
    };
  }

  @Override
  public String convertToString(
    final Boolean value)
  {
    return value.toString();
  }

  @Override
  public Boolean exampleValue()
  {
    return Boolean.TRUE;
  }

  @Override
  public String syntax()
  {
    return "true | false";
  }

  @Override
  public Class<Boolean> convertedClass()
  {
    return Boolean.class;
  }
}
