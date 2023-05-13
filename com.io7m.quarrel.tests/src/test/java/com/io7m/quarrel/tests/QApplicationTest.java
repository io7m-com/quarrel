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

import com.io7m.quarrel.core.QApplication;
import com.io7m.quarrel.core.QApplicationMetadata;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QParameterPositional;
import com.io7m.quarrel.core.QStringType.QConstant;
import com.io7m.quarrel.core.QValueConverterDirectory;
import com.io7m.quarrel.core.QValueConverterDirectoryType;
import com.io7m.quarrel.ext.xstructural.QCommandXS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.quarrel.core.QCommandStatus.FAILURE;
import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class QApplicationTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(QApplicationTest.class);

  private static final QApplicationMetadata METADATA =
    new QApplicationMetadata(
      "example",
      "com.io7m.example",
      "1.0.0",
      "eb916bb8",
      "The Quarrel example application.",
      Optional.of(URI.create("http://www.example.com/"))
    );

  private QValueConverterDirectoryType converters;
  private ByteArrayOutputStream output;
  private PrintWriter writer;
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.converters =
      QValueConverterDirectory.core();
    this.output =
      new ByteArrayOutputStream();
    this.writer =
      new PrintWriter(this.output, false, UTF_8);
    this.directory =
      QTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    System.out.print(this.output.toString(UTF_8));
    System.out.flush();

    QTestDirectories.deleteDirectory(this.directory);
  }

  @Test
  public void testEmpty()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .build();

    final var s0 =
      app.parse(List.of())
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of());

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelp()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .build();

    final var s0 =
      app.parse(List.of("help"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelpHelp()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .build();

    final var s0 =
      app.parse(List.of("help", "help"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help", "help"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelpVersion()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .build();

    final var s0 =
      app.parse(List.of("help", "version"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help", "version"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelpHelpHelp()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .build();

    final var s0 =
      app.parse(List.of("help", "help", "help"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help", "help", "help"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelpPositionals()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .addCommand(new QCommandParameterPositionals0())
        .build();

    final var s0 =
      app.parse(List.of("help", "cmd-positionals0"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help", "cmd-positionals0"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelpNamedMany()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .addCommand(new QCommandParametersNamedMany())
        .build();

    final var s0 =
      app.parse(List.of("help", "cmd-named-many"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help", "cmd-named-many"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelpGroup()
    throws Exception
  {
    final var builder =
      QApplication.builder(METADATA)
        .setOutput(this.writer);

    final var groupMeta =
      new QCommandMetadata(
        "x",
        new QConstant("A group x."),
        Optional.of(new QConstant("A group."))
      );

    builder.createCommandGroup(groupMeta)
      .addCommand(new QCommandParameter1())
      .addCommand(new QCommandParameter1N())
      .addCommand(new QCommandParameter0N());

    final var app =
      builder.build();

    final var s0 =
      app.parse(List.of("help", "x"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help", "x"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testHelpEverything()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .addCommand(new QCommandParametersEverything())
        .build();

    final var s0 =
      app.parse(List.of("help", "cmd-everything"))
        .execute();

    assertEquals(SUCCESS, s0);
    assertNotEquals("", this.output.toString(UTF_8));

    final var s1 =
      app.run(LOG, List.of("help", "cmd-everything"));

    assertEquals(SUCCESS, s1);
  }

  @Test
  public void testParseEverything()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .addCommand(new QCommandParametersEverything())
        .build();

    final var cmd =
      app.parse(
        List.of(
          "cmd-everything",
          "--0file",
          "other.txt",
          "--1number",
          "5000",
          "--3date",
          "2000-01-01T00:03:00+00:00",
          "--6path",
          "file.txt",
          "--2number-opt",
          "344",
          "--5uuid",
          "f545455c-058e-4af2-96fc-9e5986b6cc99",
          "1000",
          "2000",
          "3000"
        )
      );

    assertEquals(
      Optional.of("other.txt"),
      cmd.parameterValue(QCommandParametersEverything.PARAMETER_0)
    );
    assertEquals(
      Optional.of(5000),
      cmd.parameterValue(QCommandParametersEverything.PARAMETER_1)
    );
    assertEquals(
      344,
      cmd.parameterValue(QCommandParametersEverything.PARAMETER_2)
    );
    assertEquals(
      OffsetDateTime.parse("2000-01-01T00:03:00+00:00"),
      cmd.parameterValue(QCommandParametersEverything.PARAMETER_3)
    );
    assertEquals(
      List.of(),
      cmd.parameterValues(QCommandParametersEverything.PARAMETER_4)
    );
    assertEquals(
      List.of(UUID.fromString("f545455c-058e-4af2-96fc-9e5986b6cc99")),
      cmd.parameterValues(QCommandParametersEverything.PARAMETER_5)
    );
    assertEquals(
      List.of(Paths.get("file.txt")),
      cmd.parameterValues(QCommandParametersEverything.PARAMETER_6)
    );
    assertEquals(
      List.of(URI.create("urn:x")),
      cmd.parameterValues(QCommandParametersEverything.PARAMETER_7)
    );
    assertEquals(
      1000,
      cmd.parameterValue(QCommandParametersEverything.P_PARAMETER_0)
    );
    assertEquals(
      2000,
      cmd.parameterValue(QCommandParametersEverything.P_PARAMETER_1)
    );
    assertEquals(
      3000,
      cmd.parameterValue(QCommandParametersEverything.P_PARAMETER_2)
    );
  }

  @Test
  public void testParseNoPositionals()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .addCommand(new QCommandNoPositionals())
        .build();

    final var cmd =
      app.parse(
        List.of(
          "cmd-no-positionals"
        )
      );

    assertThrows(IllegalArgumentException.class, () -> {
      cmd.parameterValue(new QParameterPositional<Object>(
        "x",
        new QConstant("x"),
        Object.class
      ));
    });
  }

  @Test
  public void testAtSyntaxMissingFile()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .build();

    final var ex =
      assertThrows(QException.class, () -> {
        app.parse(List.of("@nonexistent"));
      });

    assertEquals("io", ex.errorCode());
    assertEquals(FAILURE, app.run(LOG, List.of("@nonexistent")));
  }

  @Test
  public void testAtSyntaxOK()
    throws Exception
  {
    final var file =
      this.directory.resolve("args.txt")
        .toAbsolutePath();

    try (var out = Files.newBufferedWriter(file)) {
      out.write("# this is a comment");
      out.newLine();
      out.write("   ");
      out.newLine();
      out.write("a");
      out.newLine();
      out.write("b");
      out.newLine();
      out.write("c");
      out.newLine();
      out.newLine();
      out.newLine();
      out.write("d");
      out.newLine();
      out.write("e");
      out.newLine();
      out.flush();
    }

    final var builder =
      QApplication.builder(METADATA)
        .setOutput(this.writer);

    builder.createCommandGroup(group("a"))
      .createCommandGroup(group("b"))
      .createCommandGroup(group("c"))
      .addCommand(new QCommandEmpty("d"));

    final var app =
      builder.build();

    final var command =
      app.parse(List.of("@" + file));

    assertEquals("d", command.command().metadata().name());
    assertEquals(List.of("e"), command.parametersPositionalRaw());
  }

  @Test
  public void testNameResolution0()
    throws QException
  {
    final var builder =
      QApplication.builder(METADATA);
    builder.setOutput(this.writer);
    builder.addCommand(new QCommandEmpty("x0"));
    builder.addCommand(new QCommandEmpty("x1"));
    builder.addCommand(new QCommandEmpty("x2"));

    final var y = builder.createCommandGroup(group("y"));
    y.addCommand(new QCommandEmpty("y0"));
    y.addCommand(new QCommandEmpty("y1"));
    y.addCommand(new QCommandEmpty("y2"));

    final var app = builder.build();

    {
      final var c = app.parse(List.of());
      assertEquals("application", c.command().metadata().name());
    }

    {
      final var c = app.parse(List.of("x0"));
      assertEquals("x0", c.command().metadata().name());
    }

    {
      final var c = app.parse(List.of("x1"));
      assertEquals("x1", c.command().metadata().name());
    }

    {
      final var c = app.parse(List.of("x2"));
      assertEquals("x2", c.command().metadata().name());
    }

    {
      final var c = app.parse(List.of("y"));
      assertEquals("help", c.command().metadata().name());
    }

    {
      final var c = app.parse(List.of("y", "y0"));
      assertEquals("y0", c.command().metadata().name());
    }

    {
      final var c = app.parse(List.of("y", "y1"));
      assertEquals("y1", c.command().metadata().name());
    }

    {
      final var c = app.parse(List.of("y", "y2"));
      assertEquals("y2", c.command().metadata().name());
    }

    {
      final var ex = assertThrows(QException.class, () -> {
        app.parse(List.of("y", "z"));
      });
      assertEquals("command-nonexistent", ex.errorCode());
    }
  }

  @Test
  public void testXstructuralEverything()
    throws Exception
  {
    final var app =
      QApplication.builder(METADATA)
        .setOutput(this.writer)
        .addCommand(new QCommandXS("_xs", true))
        .addCommand(new QCommandParametersEverything())
        .build();

    final var cmd =
      app.run(
        LOG,
        List.of(
          "_xs",
          "cmd-everything"
        )
      );

    assertNotEquals("", this.output.toString(UTF_8));
  }

  private static QCommandMetadata group(
    final String name)
  {
    return new QCommandMetadata(
      name,
      new QConstant(name),
      Optional.empty()
    );
  }
}
