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


package com.io7m.quarrel.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The function to resolve commands or groups from a command tree.
 */

public final class QCommandTreeResolver
{
  private QCommandTreeResolver()
  {

  }

  /**
   * The type of resolutions.
   */

  public sealed interface QResolutionType
  {

  }

  /**
   * The targeted object does not exist.
   *
   * @param path    The entered path
   * @param badName The missing name
   */

  public record QResolutionErrorDoesNotExist(
    List<String> path,
    String badName)
    implements QResolutionType
  {
    /**
     * The targeted object does not exist.
     */

    public QResolutionErrorDoesNotExist
    {
      Objects.requireNonNull(path, "path");
      Objects.requireNonNull(badName, "badName");
    }
  }

  /**
   * The targeted object is the root.
   */

  public enum QResolutionRoot implements QResolutionType
  {
    /**
     * The targeted object is the root.
     */

    ROOT
  }

  /**
   * The targeted object exists and is a command.
   *
   * @param command   The command
   * @param path      The path that lead to the command
   * @param remaining The remaining arguments
   */

  public record QResolutionOKCommand(
    QCommandType command,
    List<String> path,
    List<String> remaining)
    implements QResolutionType
  {
    /**
     * The targeted object exists and is a command.
     */

    public QResolutionOKCommand
    {
      Objects.requireNonNull(command, "command");
      Objects.requireNonNull(path, "path");
      Objects.requireNonNull(remaining, "remaining");
    }
  }

  /**
   * The targeted object exists and is a group.
   *
   * @param target The group
   * @param path   The path that lead to the command
   */

  public record QResolutionOKGroup(
    QCommandGroupType target,
    List<String> path)
    implements QResolutionType
  {
    /**
     * The targeted object exists and is a group.
     */

    public QResolutionOKGroup
    {
      Objects.requireNonNull(target, "command");
      Objects.requireNonNull(path, "path");
    }
  }

  /**
   * Resolve an object.
   *
   * @param tree The command tree
   * @param args The arguments
   *
   * @return The result
   */

  public static QResolutionType resolve(
    final Map<String, QCommandOrGroupType> tree,
    final List<String> args)
  {
    Objects.requireNonNull(tree, "tree");
    Objects.requireNonNull(args, "args");

    if (args.isEmpty()) {
      return QResolutionRoot.ROOT;
    }

    var treeNow = tree;
    final var pathSoFar =
      new ArrayList<String>(args.size());
    final var pathRemain =
      new ArrayList<>(args);

    final var iter = pathRemain.iterator();
    while (iter.hasNext()) {
      final var name = iter.next();
      iter.remove();
      pathSoFar.add(name);

      final var item = treeNow.get(name);
      if (item == null) {
        return new QResolutionErrorDoesNotExist(
          List.copyOf(pathSoFar),
          name
        );
      }

      if (item instanceof final QCommandType cmd) {
        return new QResolutionOKCommand(
          cmd,
          List.copyOf(pathSoFar),
          List.copyOf(pathRemain)
        );
      }

      if (item instanceof QCommandGroupType group) {
        treeNow = group.commandTree();
        if (!iter.hasNext()) {
          return new QResolutionOKGroup(group, List.copyOf(pathSoFar));
        }
      }
    }

    throw new IllegalStateException("Unreachable code.");
  }
}
