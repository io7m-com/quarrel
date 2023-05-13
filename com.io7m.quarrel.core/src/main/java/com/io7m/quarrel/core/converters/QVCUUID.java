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

import java.util.UUID;

/**
 * A value converter.
 */

public final class QVCUUID
  extends QVCAbstract<UUID>
{
  private static final QVCUUID INSTANCE =
    new QVCUUID();
  private static final UUID RANDOM_UUID =
    UUID.randomUUID();

  private QVCUUID()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<UUID> get()
  {
    return INSTANCE;
  }

  @Override
  protected UUID parse(final String text)
    throws Exception
  {
    return UUID.fromString(text);
  }

  @Override
  public String convertToString(
    final UUID value)
  {
    return value.toString();
  }

  @Override
  public UUID exampleValue()
  {
    return RANDOM_UUID;
  }

  @Override
  public String syntax()
  {
    return "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
  }

  @Override
  public Class<UUID> convertedClass()
  {
    return UUID.class;
  }
}
