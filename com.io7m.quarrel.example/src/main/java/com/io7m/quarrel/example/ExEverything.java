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

import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed0N;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamed1N;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParameterPositional;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QParametersPositionalTyped;
import com.io7m.quarrel.core.QStringType.QConstant;

import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;

/**
 * An example command.
 */

public final class ExEverything implements QCommandType
{
  private static final QParameterNamed01<String> PARAMETER_0 =
    new QParameterNamed01<>(
      "--0file",
      List.of("-x", "-y", "-z"),
      new QConstant("A file."),
      Optional.empty(),
      String.class
    );

  private static final QParameterNamed01<Integer> PARAMETER_1 =
    new QParameterNamed01<>(
      "--1number",
      List.of(),
      new QConstant("A number."),
      Optional.of(23),
      Integer.class
    );

  private static final QParameterNamed1<Integer> PARAMETER_2 =
    new QParameterNamed1<>(
      "--2number-opt",
      List.of(),
      new QConstant("A number."),
      Optional.empty(),
      Integer.class
    );

  private static final QParameterNamed1<OffsetDateTime> PARAMETER_3 =
    new QParameterNamed1<>(
      "--3date",
      List.of(),
      new QConstant("A date."),
      Optional.of(OffsetDateTime.now()),
      OffsetDateTime.class
    );

  private static final QParameterNamed0N<InetAddress> PARAMETER_4 =
    new QParameterNamed0N<>(
      "--4net",
      List.of(),
      new QConstant("A network address."),
      List.of(),
      InetAddress.class
    );

  private static final QParameterNamed0N<UUID> PARAMETER_5 =
    new QParameterNamed0N<>(
      "--5uuid",
      List.of(),
      new QConstant("A UUID."),
      List.of(UUID.randomUUID()),
      UUID.class
    );

  private static final QParameterNamed1N<Path> PARAMETER_6 =
    new QParameterNamed1N<>(
      "--6path",
      List.of(),
      new QConstant("A path."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1N<URI> PARAMETER_7 =
    new QParameterNamed1N<>(
      "--7uri",
      List.of(),
      new QConstant("A URI."),
      Optional.of(URI.create("urn:x")),
      URI.class
    );

  private static final QParameterPositional<Integer> P_PARAMETER_0 =
    new QParameterPositional<>(
      "x",
      new QConstant("An x."),
      Integer.class
    );

  private static final QParameterPositional<Integer> P_PARAMETER_1 =
    new QParameterPositional<>(
      "y",
      new QConstant("A y."),
      Integer.class
    );

  private static final QParameterPositional<Integer> P_PARAMETER_2 =
    new QParameterPositional<>(
      "z",
      new QConstant("A z."),
      Integer.class
    );

  private static final String TEXT = """
Farmer Bertram was in bed when the stranger entered, having had a fall
from his horse while hunting.

The horseman said his business was of such pressing importance that he
must see the farmer at once.

Bertram recognized the name, and directed his old servant to admit the
stranger to his chamber at once.
    """;

  /**
   * An example command.
   */

  public ExEverything()
  {

  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return List.of(
      PARAMETER_0,
      PARAMETER_1,
      PARAMETER_2,
      PARAMETER_3,
      PARAMETER_4,
      PARAMETER_5,
      PARAMETER_6,
      PARAMETER_7
    );
  }

  @Override
  public QParametersPositionalType onListPositionalParameters()
  {
    return new QParametersPositionalTyped(
      List.of(P_PARAMETER_0, P_PARAMETER_1, P_PARAMETER_2)
    );
  }

  @Override
  public QCommandMetadata metadata()
  {
    return new QCommandMetadata(
      "cmd-everything",
      new QConstant("A command with everything."),
      Optional.of(new QConstant(TEXT))
    );
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType context)
  {
    final var w = context.output();
    w.println(context.parameterValue(PARAMETER_0));
    w.println(context.parameterValue(PARAMETER_1));
    w.println(context.parameterValue(PARAMETER_2));
    w.println(context.parameterValue(PARAMETER_3));
    w.println(context.parameterValues(PARAMETER_4));
    w.println(context.parameterValues(PARAMETER_5));
    w.println(context.parameterValues(PARAMETER_6));
    w.println(context.parameterValues(PARAMETER_7));
    w.println(context.parameterValue(P_PARAMETER_0));
    w.println(context.parameterValue(P_PARAMETER_1));
    w.println(context.parameterValue(P_PARAMETER_2));
    w.flush();
    return SUCCESS;
  }
}
