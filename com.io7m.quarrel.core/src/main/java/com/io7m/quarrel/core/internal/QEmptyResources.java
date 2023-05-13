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

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * An empty resource bundle.
 */

public final class QEmptyResources extends ResourceBundle
{
  private final Properties properties;

  /**
   * An empty resource bundle.
   */

  public QEmptyResources()
  {
    this.properties = new Properties();
  }

  @Override
  protected Object handleGetObject(
    final String key)
  {
    return this.properties.get(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Enumeration<String> getKeys()
  {
    return (Enumeration<String>) (Object) this.properties.keys();
  }
}
