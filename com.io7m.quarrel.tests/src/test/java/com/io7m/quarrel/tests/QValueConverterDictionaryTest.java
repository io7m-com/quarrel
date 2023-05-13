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
import com.io7m.quarrel.core.QValueConverterDirectoryType;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class QValueConverterDictionaryTest
{
  @Provide
  private static Arbitrary<UUID> uuids()
  {
    return Arbitraries.create(UUID::randomUUID);
  }

  @Provide
  private static Arbitrary<Duration> durations()
  {
    return Arbitraries.longs()
      .map(Duration::ofSeconds);
  }

  @Provide
  private static Arbitrary<OffsetDateTime> offsetDateTimes()
  {
    return Arbitraries.longs()
      .between(100L, 1_000_000L)
      .map(x -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(x), ZoneId.systemDefault()))
      .map(d -> d.withNano(0));
  }

  @Provide
  private static Arbitrary<InetAddress> inetAddresses()
  {
    return Arbitraries.integers()
      .map(x -> {
        try {
          return Inet4Address.getByName(
            "localhost"
          );
        } catch (final UnknownHostException e) {
          throw new UncheckedIOException(e);
        }
      });
  }

  @Provide
  private static Arbitrary<URI> uris()
  {
    return uuids().map(u -> URI.create("urn:" + u.toString()));
  }

  @Provide
  private static Arbitrary<Path> paths()
  {
    return Arbitraries.strings()
      .alpha()
      .ofMinLength(1)
      .ofMaxLength(8)
      .array(String[].class)
      .ofMinSize(1)
      .ofMaxSize(8)
      .map(xs -> Paths.get(xs[0], xs));
  }

  @Provide
  private static Arbitrary<Class<?>> classes()
  {
    return Arbitraries.of(Void.class);
  }

  @Test
  public void testLookupIdentity()
  {
    final var core = QValueConverterDirectory.core();
    core
      .converters()
      .forEach(s -> {
        assertEquals(s, core.converterFor(s.convertedClass()).orElseThrow());
      });
  }

  @Test
  public void testSyntax()
  {
    QValueConverterDirectory.core()
      .converters()
      .forEach(s -> {
        assertFalse(s.syntax().isBlank());
      });
  }

  @Property
  public void testEmpty(
    final @ForAll("classes") Class<?> clazz)
  {
    assertEquals(
      Optional.empty(),
      QValueConverterDirectory.empty()
        .converterFor(clazz)
    );
  }

  @Property
  public void testBoolean(
    final @ForAll Boolean x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Boolean.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );

    assertThrows(QException.class, () -> {
      c.convertFromString("unparseable");
    });
  }

  @Property
  public void testPath(
    final @ForAll("paths") Path x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Path.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testInteger(
    final @ForAll Integer x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Integer.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );

    assertThrows(QException.class, () -> {
      c.convertFromString("unparseable");
    });
  }

  @Property
  public void testLong(
    final @ForAll Long x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Long.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testFloat(
    final @ForAll Float x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Float.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testDouble(
    final @ForAll Double x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Double.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testString(
    final @ForAll String x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(String.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testURI(
    final @ForAll("uris") URI x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(URI.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testUUID(
    final @ForAll("uuids") UUID x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(UUID.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testBigInteger(
    final @ForAll BigInteger x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(BigInteger.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testBigDecimal(
    final @ForAll BigDecimal x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(BigDecimal.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testDuration(
    final @ForAll("durations") Duration x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(Duration.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testOffsetDateTime(
    final @ForAll("offsetDateTimes") OffsetDateTime x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(OffsetDateTime.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }

  @Property
  public void testInetAddress(
    final @ForAll("inetAddresses") InetAddress x)
    throws QException
  {
    final var c =
      QValueConverterDirectory.core()
        .converterFor(InetAddress.class)
        .orElseThrow();

    assertEquals(x, c.convertFromString(c.convertToString(x)));
    assertEquals(
      c.exampleValue(),
      c.convertFromString(c.convertToString(c.exampleValue()))
    );
  }
}
