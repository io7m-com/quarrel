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

import com.io7m.seltzer.api.SStructuredErrorExceptionType;
import com.io7m.seltzer.api.SStructuredErrorType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The type of exceptions raised by the package.
 */

public final class QException extends Exception
  implements SStructuredErrorExceptionType<String>
{
  private final String errorCode;
  private final Map<String, String> attributes;
  private final Optional<String> remediatingAction;
  private final List<SStructuredErrorType<String>> extraErrors;

  /**
   * Construct an exception.
   *
   * @param message             The message
   * @param inErrorCode         The error code
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action, if any
   * @param inExtraErrors       The extra errors, if any
   */

  public QException(
    final String message,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction,
    final List<SStructuredErrorType<String>> inExtraErrors)
  {
    super(Objects.requireNonNull(message, "message"));

    this.errorCode =
      Objects.requireNonNull(inErrorCode, "errorCode");
    this.attributes =
      Objects.requireNonNull(inAttributes, "attributes");
    this.remediatingAction =
      Objects.requireNonNull(inRemediatingAction, "remediatingAction");
    this.extraErrors =
      Objects.requireNonNull(inExtraErrors, "extraErrors");
  }

  /**
   * Construct an exception.
   *
   * @param message             The message
   * @param cause               The cause
   * @param inErrorCode         The error code
   * @param inAttributes        The error attributes
   * @param inRemediatingAction The remediating action, if any
   * @param inExtraErrors       The extra errors, if any
   */

  public QException(
    final String message,
    final Throwable cause,
    final String inErrorCode,
    final Map<String, String> inAttributes,
    final Optional<String> inRemediatingAction,
    final List<SStructuredErrorType<String>> inExtraErrors)
  {
    super(
      Objects.requireNonNull(message, "message"),
      Objects.requireNonNull(cause, "cause")
    );
    this.errorCode =
      Objects.requireNonNull(inErrorCode, "errorCode");
    this.attributes =
      Objects.requireNonNull(inAttributes, "attributes");
    this.remediatingAction =
      Objects.requireNonNull(inRemediatingAction, "remediatingAction");
    this.extraErrors =
      Objects.requireNonNull(inExtraErrors, "extraErrors");
  }

  /**
   * Adapt a generic exception to this exception type.
   *
   * @param exception          An exception
   * @param errorCodeConverter A converter from error codes to strings
   * @param <C>                The type of error codes
   * @param <T>                The type of base exceptions
   *
   * @return An exception
   */

  public static <C, T extends Exception & SStructuredErrorExceptionType<C>>
  QException adapt(
    final T exception,
    final Function<C, String> errorCodeConverter)
  {
    Objects.requireNonNull(exception, "exception");
    Objects.requireNonNull(errorCodeConverter, "errorCodeConverter");

    return new QException(
      exception.getMessage(),
      exception,
      errorCodeConverter.apply(exception.errorCode()),
      exception.attributes(),
      exception.remediatingAction(),
      List.of()
    );
  }

  /**
   * @return The extra errors
   */

  public List<SStructuredErrorType<String>> extraErrors()
  {
    return this.extraErrors;
  }

  @Override
  public String errorCode()
  {
    return this.errorCode;
  }

  @Override
  public Map<String, String> attributes()
  {
    return this.attributes;
  }

  @Override
  public Optional<String> remediatingAction()
  {
    return this.remediatingAction;
  }

  @Override
  public Optional<Throwable> exception()
  {
    return Optional.of(this);
  }
}
