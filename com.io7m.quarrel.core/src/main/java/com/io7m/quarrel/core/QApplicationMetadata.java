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

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * The application metadata.
 *
 * @param applicationName The short name of the application used in command-line
 *                        usage messages (such as "idstore")
 * @param applicationId   The application ID (such as "com.io7m.idstore")
 * @param version         The version (such as "1.0.0")
 * @param build           The build (such as
 *                        "25695d1e138bc93f21759a990d676e02951b0ae4")
 * @param title           The application title
 * @param site            The site (such as
 *                        "https://www.io7m.com/software/idstore")
 */

public record QApplicationMetadata(
  String applicationName,
  String applicationId,
  String version,
  String build,
  String title,
  Optional<URI> site)
{
  /**
   * The application metadata.
   *
   * @param applicationName The short name of the application used in
   *                        command-line usage messages (such as "idstore")
   * @param applicationId   The application ID (such as "com.io7m.idstore")
   * @param version         The version (such as "1.0.0")
   * @param build           The build (such as
   *                        "25695d1e138bc93f21759a990d676e02951b0ae4")
   * @param title           The application title
   * @param site            The site (such as
   *                        "https://www.io7m.com/software/idstore")
   */

  public QApplicationMetadata
  {
    Objects.requireNonNull(applicationName, "applicationName");
    Objects.requireNonNull(applicationId, "applicationId");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(build, "build");
    Objects.requireNonNull(title, "title");
    Objects.requireNonNull(site, "site");
  }
}
