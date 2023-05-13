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

package com.io7m.quarrel.example;

import com.io7m.quarrel.core.QApplication;
import com.io7m.quarrel.core.QApplicationMetadata;
import com.io7m.quarrel.core.QApplicationType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QStringType.QConstant;
import com.io7m.quarrel.ext.xstructural.QCommandXS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The main program.
 */

public final class Main implements Runnable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Main.class);

  private final List<String> args;
  private final QApplicationType application;
  private int exitCode;

  /**
   * The main entry point.
   *
   * @param inArgs Command-line arguments
   */

  public Main(
    final String[] inArgs)
  {
    try {
      this.args =
        Objects.requireNonNull(List.of(inArgs), "Command line arguments");

      final var metadata =
        new QApplicationMetadata(
          "quarrel",
          "com.io7m.quarrel.example",
          "1.2.0",
          "eacd59a2",
          "The Quarrel example application.",
          Optional.of(URI.create("https://www.io7m.com/software/quarrel/"))
        );

      final var resources =
        new ExStrings(Locale.getDefault()).resources();

      final var builder = QApplication.builder(metadata);
      builder.setApplicationResources(resources);
      builder.addCommand(new ExCmd0());
      builder.addCommand(new ExEverything());
      builder.addCommand(new ExMeta());
      builder.addCommand(new QCommandXS("xstructural", true));

      {
        final var group =
          builder.createCommandGroup(new QCommandMetadata(
            "animal",
            new QConstant("Hear an animal speak."),
            Optional.of(new QConstant("A long description."))
          ));

        group.addCommand(new ExCat());
        group.addCommand(new ExDog());
        group.addCommand(new ExCow());
      }

      this.application = builder.build();
      this.exitCode = 0;
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * The main entry point.
   *
   * @param args Command line arguments
   */

  public static void main(
    final String[] args)
  {
    System.exit(mainExitless(args));
  }

  /**
   * The main (exitless) entry point.
   *
   * @param args Command line arguments
   *
   * @return The exit code
   */

  public static int mainExitless(
    final String[] args)
  {
    final var cm = new Main(args);
    cm.run();
    return cm.exitCode();
  }

  /**
   * @return The exit code
   */

  public int exitCode()
  {
    return this.exitCode;
  }

  @Override
  public void run()
  {
    this.exitCode = this.application.run(LOG, this.args).exitCode();
  }

  @Override
  public String toString()
  {
    return String.format(
      "[Main 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
