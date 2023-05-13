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

package com.io7m.quarrel.core.converters;

import com.io7m.quarrel.core.QValueConverterType;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A value converter.
 */

public final class QVCInetAddress
  extends QVCAbstract<InetAddress>
{
  private static final QVCInetAddress INSTANCE = new QVCInetAddress();

  private QVCInetAddress()
  {

  }

  /**
   * @return A value converter.
   */

  public static QValueConverterType<InetAddress> get()
  {
    return INSTANCE;
  }

  @Override
  protected InetAddress parse(
    final String text)
    throws UnknownHostException
  {
    return InetAddress.getByName(text);
  }

  @Override
  public String convertToString(
    final InetAddress value)
  {
    return value.getHostName();
  }

  @Override
  public InetAddress exampleValue()
  {
    return InetAddress.getLoopbackAddress();
  }

  @Override
  public String syntax()
  {
    return "Hostname, IPv4 or IPv6 address (RFC 2732)";
  }

  @Override
  public Class<InetAddress> convertedClass()
  {
    return InetAddress.class;
  }
}
