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

import java.util.regex.Pattern;

/**
 * A value converter.
 */

public final class QVCPattern
  extends QVCAbstract<Pattern>
{
  private static final QVCPattern INSTANCE = new QVCPattern();

  private QVCPattern()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<Pattern> get()
  {
    return INSTANCE;
  }

  @Override
  protected Pattern parse(
    final String text)
    throws Exception
  {
    return Pattern.compile(text, Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE);
  }

  @Override
  public String convertToString(
    final Pattern value)
  {
    return value.toString();
  }

  @Override
  public Pattern exampleValue()
  {
    return Pattern.compile("([a-z][a-z0-9_-]{0,63})(\\.[a-z][a-z0-9_-]{0,62}){0,15}");
  }

  @Override
  public String syntax()
  {
    return "java.util.Pattern";
  }

  @Override
  public Class<Pattern> convertedClass()
  {
    return Pattern.class;
  }
}
