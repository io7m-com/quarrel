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
import java.util.regex.Pattern;

import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

/**
 * Valid names.
 */

public final class QNames
{
  private static final Pattern WHITESPACE =
    Pattern.compile("[\\p{Z}\\p{Cc}\\p{Space}]+", UNICODE_CHARACTER_CLASS);

  /**
   * Valid names.
   */

  private QNames()
  {

  }

  /**
   * Check that the given name is valid.
   *
   * @param name The name
   *
   * @return The name
   *
   * @throws IllegalArgumentException If the name is not valid
   */

  public static String checkOK(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    if (name.isEmpty()) {
      throw new IllegalArgumentException(
        "Names cannot be empty");
    }

    if (WHITESPACE.matcher(name).matches()) {
      throw new IllegalArgumentException(
        "Names cannot contain whitespace");
    }

    if (name.startsWith("@")) {
      throw new IllegalArgumentException(
        "Names cannot begin with @");
    }

    return name;
  }
}
