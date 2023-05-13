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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A value converter.
 */

public final class QVCPath
  extends QVCAbstract<Path>
{
  private static final QVCPath INSTANCE = new QVCPath();

  private QVCPath()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<Path> get()
  {
    return INSTANCE;
  }

  @Override
  protected Path parse(final String text)
    throws Exception
  {
    return Paths.get(text);
  }

  @Override
  public String convertToString(
    final Path value)
  {
    return value.toString();
  }

  @Override
  public Path exampleValue()
  {
    return Paths.get("/etc/passwd");
  }

  @Override
  public String syntax()
  {
    return "<platform-specific path syntax>";
  }

  @Override
  public Class<Path> convertedClass()
  {
    return Path.class;
  }
}
