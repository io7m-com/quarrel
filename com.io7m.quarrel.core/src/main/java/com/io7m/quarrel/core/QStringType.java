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

import java.util.Objects;

/**
 * A reference to a string.
 */

public sealed interface QStringType
{
  /**
   * A string constant that can be directly displayed.
   *
   * @param text The text
   */

  record QConstant(String text)
    implements QStringType
  {
    /**
     * A string constant that can be directly displayed.
     *
     * @param text The text
     */

    public QConstant
    {
      Objects.requireNonNull(text, "text");
    }
  }

  /**
   * A string constant that must be localized from a resource.
   *
   * @param id The id
   */

  record QLocalize(String id)
    implements QStringType
  {
    /**
     * A string constant that must be localized from a resource.
     *
     * @param id The id
     */

    public QLocalize
    {
      Objects.requireNonNull(id, "id");
    }
  }
}
