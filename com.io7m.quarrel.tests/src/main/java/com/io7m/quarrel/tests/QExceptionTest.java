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

import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QValueConverterDirectory;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class QExceptionTest
{
  @Property
  public void testBoolean(
    final @ForAll Boolean x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Boolean.class)
        .orElseThrow();

    final var ex0 =
    assertThrows(QException.class, () -> {
      c.convertFromString("unparseable");
    });

    final var ex1 = QException.adapt(ex0, Function.identity());
    assertEquals(ex0.getMessage(), ex1.getMessage());
    assertEquals(ex0.errorCode(), ex1.errorCode());
    assertEquals(ex0.extraErrors(), ex1.extraErrors());
    assertEquals(ex0.remediatingAction(), ex1.remediatingAction());
    assertEquals(ex0.attributes(), ex1.attributes());
    assertEquals(ex0.exception().isPresent(), ex1.exception().isPresent());
  }
}
