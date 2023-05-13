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

import com.io7m.quarrel.core.QApplicationType;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParametersPositionalNone;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QStringType.QConstant;
import com.io7m.quarrel.core.QStringType.QLocalize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;
import static java.text.MessageFormat.format;

/**
 * The anonymous application usage command executed when no arguments are
 * provided.
 */

public final class QCommandApplicationUsage implements QCommandType
{
  private final QApplicationType target;

  /**
   * The anonymous application usage command executed when no arguments are
   * provided.
   *
   * @param inTarget The target
   */

  public QCommandApplicationUsage(
    final QApplicationType inTarget)
  {
    this.target = Objects.requireNonNull(inTarget, "target");
  }

  @Override
  public QCommandMetadata metadata()
  {
    return new QCommandMetadata(
      "application",
      new QConstant(""),
      Optional.empty()
    );
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
    final var meta =
      this.target.metadata();

    final var w = context.output();
    w.printf(
      format(
        context.localize(new QLocalize("quarrel.usage")),
        meta.applicationName())
    );
    w.println();

    w.println();
    w.print("  ");
    w.print(meta.title());
    w.println();

    {
      final var text =
        context.format(
          new QLocalize("quarrel.usage.help"),
          meta.applicationName());
      final var lines =
        text.lines().toList();
      for (final var line : lines) {
        w.print("  ");
        w.println(line);
      }
      w.println();
    }

    final var items = new ArrayList<>(this.target.commandTree().values());
    items.sort(Comparator.comparing(o -> o.metadata().name()));

    w.print("  ");
    w.println(context.localize(new QLocalize("quarrel.usage.commands")));

    final var longest =
      4 + items.stream()
        .mapToInt(g -> g.metadata().name().length())
        .reduce(0, Integer::max);

    for (final var command : items) {
      if (command.isHidden()) {
        continue;
      }

      final var metadata =
        command.metadata();
      final var name =
        metadata.name();
      final var pad =
        longest - name.length();

      w.print("    ");
      w.print(name);
      w.print(" ".repeat(pad));
      w.print(context.localize(metadata.shortDescription()));
      w.println();
    }
    w.println();

    final var siteOpt = meta.site();
    if (siteOpt.isPresent()) {
      final var site = siteOpt.get();
      w.print("  ");
      w.println(context.localize(new QLocalize("quarrel.usage.documentation")));
      w.print("    ");
      w.println(site);
    }

    w.println();
    w.flush();
    return SUCCESS;
  }
}
