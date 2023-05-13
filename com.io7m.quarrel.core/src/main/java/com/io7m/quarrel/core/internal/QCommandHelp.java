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

import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandHelpFormatting;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandOrGroupType;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandTreeResolver;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionErrorDoesNotExist;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionOKCommand;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionOKGroup;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionRoot;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QLocalizationType;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParametersPositionalAny;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QStringType.QLocalize;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;

/**
 * The help command.
 */

public final class QCommandHelp implements QCommandType
{
  private final String applicationName;
  private final Map<String, QCommandOrGroupType> commandTree;

  /**
   * The help command.
   *
   * @param inApplicationName The application name
   * @param inCommandTree     The tree of commands
   */

  public QCommandHelp(
    final String inApplicationName,
    final Map<String, QCommandOrGroupType> inCommandTree)
  {
    this.applicationName =
      Objects.requireNonNull(inApplicationName, "inApplicationName");
    this.commandTree =
      Objects.requireNonNull(inCommandTree, "commandTree");
  }

  private static QException errorNonexistentCommand(
    final QLocalizationType context,
    final Iterable<String> items)
  {
    final var errorCommandNonexistent =
      context.localize(new QLocalize("quarrel.errorCommandNonexistent"));
    final var command =
      context.localize(new QLocalize("quarrel.command"));
    final var suggestMessage =
      context.localize(new QLocalize("quarrel.errorSuggestRightPath"));

    return new QException(
      errorCommandNonexistent,
      "command-nonexistent",
      Map.ofEntries(
        Map.entry(command, String.join(" ", items))
      ),
      Optional.of(suggestMessage),
      List.of()
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
    return new QParametersPositionalAny();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType context)
    throws Exception
  {
    final var raw =
      context.parametersPositionalRaw();
    final var resolved =
      QCommandTreeResolver.resolve(this.commandTree, raw);

    if (resolved instanceof QResolutionRoot) {
      QCommandHelpFormatting.formatCommand(
        context.valueConverters(),
        context,
        this.applicationName,
        context.output(),
        this
      );
      return SUCCESS;
    }

    if (resolved instanceof QResolutionErrorDoesNotExist) {
      throw errorNonexistentCommand(context, raw);
    }

    if (resolved instanceof final QResolutionOKCommand cmd) {
      QCommandHelpFormatting.formatCommand(
        context.valueConverters(),
        context,
        this.applicationName,
        context.output(),
        cmd.command()
      );
      return SUCCESS;
    }

    if (resolved instanceof final QResolutionOKGroup group) {
      QCommandHelpFormatting.formatGroup(
        context.valueConverters(),
        context,
        this.applicationName,
        context.output(),
        group.target(),
        group.path()
      );
      return SUCCESS;
    }

    throw new IllegalStateException("Unreachable code.");
  }


  @Override
  public QCommandMetadata metadata()
  {
    return new QCommandMetadata(
      "help",
      new QLocalize("quarrel.help.description"),
      Optional.of(new QLocalize("quarrel.help.description.long"))
    );
  }
}
