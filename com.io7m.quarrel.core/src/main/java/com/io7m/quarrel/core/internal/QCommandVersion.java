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


package com.io7m.quarrel.core.internal;

import com.io7m.quarrel.core.QApplicationMetadata;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParametersPositionalNone;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QStringType.QLocalize;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;

/**
 * The version command.
 */

public final class QCommandVersion implements QCommandType
{
  private final QApplicationMetadata metadata;

  /**
   * The version command.
   *
   * @param inMetadata The application metadata
   */

  public QCommandVersion(
    final QApplicationMetadata inMetadata)
  {
    this.metadata = Objects.requireNonNull(inMetadata, "metadata");
  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return List.of();
  }

  @Override
  public QParametersPositionalType onListPositionalParameters()
  {
    return new QParametersPositionalNone();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType context)
    throws Exception
  {
    final var w = context.output();
    w.printf(
      "%s %s %s%n",
      this.metadata.applicationId(),
      this.metadata.version(),
      this.metadata.build()
    );
    w.flush();
    return SUCCESS;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return new QCommandMetadata(
      "version",
      new QLocalize("quarrel.version.description"),
      Optional.of(new QLocalize("quarrel.version.description.long"))
    );
  }
}
