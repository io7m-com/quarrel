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

import com.io7m.quarrel.core.QCommandParserConfiguration;
import com.io7m.quarrel.core.QCommandParsers;
import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QValueConverterDirectory;
import com.io7m.quarrel.core.QValueConverterDirectoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class QCommandParserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(QCommandParserTest.class);

  private QValueConverterDirectoryType converters;
  private ByteArrayOutputStream output;
  private PrintWriter writer;
  private QCommandParsers parsers;
  private QCommandParserConfiguration configuration;

  @BeforeEach
  public void setup()
  {
    this.converters =
      QValueConverterDirectory.core();
    this.output =
      new ByteArrayOutputStream();
    this.writer =
      new PrintWriter(this.output, false, StandardCharsets.UTF_8);
    this.parsers =
      new QCommandParsers();
    this.configuration =
      new QCommandParserConfiguration(
        this.converters,
        QCommandParsers.emptyResources()
      );
  }

  @Test
  public void testEmpty()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandNoParameters(),
          List.of()
        );
  }

  @Test
  public void testParameters1()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameter1(),
            List.of()
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-cardinality", ex.errorCode());
  }

  @Test
  public void testParameters1AltCollision()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameter1Alts(),
            List.of("--file", "a", "-x", "b")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-cardinality", ex.errorCode());
  }

  @Test
  public void testParameters1MissingValue()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameter1(),
            List.of("--file")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-missing-value", ex.errorCode());
  }

  @Test
  public void testParameters1TooMany()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameter1(),
            List.of("--file", "x", "--file", "y")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-cardinality", ex.errorCode());
  }

  @Test
  public void testParameters1P()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter1(),
          List.of("--file", "x")
        );

    assertEquals(
      "x",
      c.parameterValue(QCommandParameter1.PARAMETER)
    );
  }

  @Test
  public void testParameters1D()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter1D(),
          List.of()
        );

    assertEquals(
      "File!",
      c.parameterValue(QCommandParameter1D.PARAMETER)
    );
  }

  @Test
  public void testParameters1DP()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter1D(),
          List.of("--file", "x")
        );

    assertEquals(
      "x",
      c.parameterValue(QCommandParameter1D.PARAMETER)
    );
  }

  @Test
  public void testParameters1N()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter1N(),
          List.of("--file", "y")
        );

    assertEquals(
      List.of("y"),
      c.parameterValues(QCommandParameter1N.PARAMETER)
    );
  }

  @Test
  public void testParameters1NTooFew()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameter1N(),
            List.of()
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-cardinality", ex.errorCode());
  }

  @Test
  public void testParameters1NDTooFew()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter1ND(),
          List.of()
        );

    assertEquals(
      List.of("x"),
      c.parameterValues(QCommandParameter1ND.PARAMETER)
    );
  }

  @Test
  public void testParameters1NP()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter1N(),
          List.of("--file", "a")
        );

    assertEquals(
      List.of("a"),
      c.parameterValues(QCommandParameter1N.PARAMETER)
    );
  }

  @Test
  public void testParameters0N()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter0N(),
          List.of()
        );

    assertEquals(
      List.of(),
      c.parameterValues(QCommandParameter0N.PARAMETER)
    );
  }

  @Test
  public void testParameters0NP()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter0N(),
          List.of("--file", "a", "--file", "b")
        );

    assertEquals(
      List.of("a", "b"),
      c.parameterValues(QCommandParameter0N.PARAMETER)
    );
  }

  @Test
  public void testParameters0ND()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter0ND(),
          List.of()
        );

    assertEquals(
      List.of("x", "y"),
      c.parameterValues(QCommandParameter0ND.PARAMETER)
    );
  }

  @Test
  public void testParameters0NDP()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter0ND(),
          List.of("--file", "a", "--file", "b")
        );

    assertEquals(
      List.of("a", "b"),
      c.parameterValues(QCommandParameter0ND.PARAMETER)
    );
  }

  @Test
  public void testParameters01TooMany()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameter01(),
            List.of("--file", "x", "--file", "y")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-cardinality", ex.errorCode());
  }

  @Test
  public void testParameters01D()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter01D(),
          List.of()
        );

    assertEquals(
      Optional.of("x"),
      c.parameterValue(QCommandParameter01D.PARAMETER)
    );
  }

  @Test
  public void testParameters01()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter01(),
          List.of()
        );

    assertEquals(
      Optional.empty(),
      c.parameterValue(QCommandParameter01.PARAMETER)
    );
  }

  @Test
  public void testParametersInt1()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameterInt1(),
            List.of("--int", "z")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-unparseable-value", ex.errorCode());
  }

  @Test
  public void testParameters0NAlts()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter0NAlts(),
          List.of("--file", "a", "-x", "b", "-y", "c", "-z", "d")
        );

    assertEquals(
      List.of("a", "b", "c", "d"),
      c.parameterValues(QCommandParameter0NAlts.PARAMETER)
    );
  }

  @Test
  public void testParametersCollision0()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParametersCollision0(),
            List.of()
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-duplicate", ex.errorCode());
  }

  @Test
  public void testParametersCollision1()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParametersCollision1(),
            List.of()
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-duplicate", ex.errorCode());
  }

  @Test
  public void testParametersMissingType()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParametersNoType(),
            List.of()
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-no-value-converter", ex.errorCode());
  }

  @Test
  public void testParametersUndefined()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameter1(),
          List.of("--file", "x")
        );

    assertThrows(IllegalArgumentException.class, () -> {
      c.parameterValue(QCommandParameterInt1.PARAMETER);
    });
  }

  @Test
  public void testParametersPositional0()
    throws QException
  {
    final var c =
      this.parsers.create(this.configuration)
        .execute(
          Collections.emptySortedMap(),
          this.writer,
          new QCommandParameterPositionals0(),
          List.of("23", "24", "25")
        );
  }

  @Test
  public void testParametersPositionalTooMany0()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameter1(),
            List.of("--file", "x", "y")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-positional-count", ex.errorCode());
  }

  @Test
  public void testParametersPositionalTooFew0()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameterPositionals0(),
            List.of("x", "y")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-positional-count", ex.errorCode());
  }

  @Test
  public void testParametersPositionalMissingType()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParametersPositionalNoType(),
            List.of()
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-no-value-converter", ex.errorCode());
  }

  @Test
  public void testParametersPositionalUnparseable0()
    throws QException
  {
    final var ex =
      assertThrows(QException.class, () -> {
        this.parsers.create(this.configuration)
          .execute(
            Collections.emptySortedMap(),
            this.writer,
            new QCommandParameterPositionals0(),
            List.of("x", "y", "z")
          );
      });

    LOG.error("", ex);
    assertEquals("parameter-unparseable-value", ex.errorCode());
  }
}
