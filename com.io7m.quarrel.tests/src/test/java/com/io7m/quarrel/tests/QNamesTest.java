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


package com.io7m.quarrel.tests;

import com.io7m.quarrel.core.QNames;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class QNamesTest
{
  @Provide
  public Arbitrary<String> nonWhitespace()
  {
    return Arbitraries.strings()
      .ofMinLength(1)
      .filter(p -> {
        return !p.matches("[\\p{Z}\\p{Cc}\\p{Space}]+");
      })
      .filter(p -> {
        return !p.startsWith("@");
      });
  }

  @Provide
  public Arbitrary<String> nonWhitespaceWithAt()
  {
    return Arbitraries.strings()
      .filter(p -> {
        return !p.matches("[\\p{Z}\\p{Cc}\\p{Space}]+");
      }).map(s -> "@" + s);
  }

  @Provide
  public Arbitrary<String> whitespace()
  {
    return Arbitraries.strings()
      .whitespace();
  }

  @Property
  public void testNamesOK(
    final @ForAll("nonWhitespace") String name)
  {
    assertEquals(name, QNames.checkOK(name));
  }

  @Property
  public void testNamesNotOK0(
    final @ForAll("whitespace") String name)
  {
    assertThrows(IllegalArgumentException.class, () -> {
      QNames.checkOK(name);
    });
  }

  @Property
  public void testNamesNotOK1(
    final @ForAll("nonWhitespaceWithAt") String name)
  {
    assertThrows(IllegalArgumentException.class, () -> {
      QNames.checkOK(name);
    });
  }

  @Test
  public void testNamesNotOK2()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      QNames.checkOK("");
    });
  }

  @Test
  public void testNamesNotOK3()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      QNames.checkOK("\t");
    });
  }
}
