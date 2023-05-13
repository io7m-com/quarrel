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


package com.io7m.quarrel.ext.xstructural;

import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandTreeResolver;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionErrorDoesNotExist;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionOKCommand;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionOKGroup;
import com.io7m.quarrel.core.QCommandTreeResolver.QResolutionRoot;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamed01;
import com.io7m.quarrel.core.QParameterNamed0N;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamed1N;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParameterType;
import com.io7m.quarrel.core.QParametersPositionalAny;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QStringType.QConstant;
import com.io7m.quarrel.core.QValueConverterType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.io7m.quarrel.core.QCommandStatus.FAILURE;
import static com.io7m.quarrel.core.QCommandStatus.SUCCESS;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A command that produces an xstructural documentation template.
 */

public final class QCommandXS implements QCommandType
{
  private final boolean hidden;
  private final QCommandMetadata metadata;

  /**
   * A command that produces an xstructural documentation template.
   *
   * @param inName   The command name
   * @param inHidden {@code true} if the command should be hidden
   */

  public QCommandXS(
    final String inName,
    final boolean inHidden)
  {
    this.hidden = inHidden;
    this.metadata =
      new QCommandMetadata(
        inName,
        new QConstant("Produce an xstructural documentation template."),
        Optional.empty()
      );
  }

  @Override
  public boolean isHidden()
  {
    return this.hidden;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return this.metadata;
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
    final var command =
      context.parametersPositionalRaw();
    final var result =
      QCommandTreeResolver.resolve(context.commandTree(), command);

    if (result instanceof QResolutionRoot) {
      return SUCCESS;
    }

    if (result instanceof final QResolutionOKCommand cmd) {
      showCommand(context, cmd.command());
      return SUCCESS;
    }

    if (result instanceof final QResolutionOKGroup group) {
      return SUCCESS;
    }

    if (result instanceof QResolutionErrorDoesNotExist) {
      return FAILURE;
    }

    return SUCCESS;
  }

  private static final String NS =
    "urn:com.io7m.structural:8:0";

  private static void showCommand(
    final QCommandContextType context,
    final QCommandType command)
    throws ParserConfigurationException, TransformerException
  {
    final var documents =
      DocumentBuilderFactory.newDefaultNSInstance();
    final var document =
      documents.newDocumentBuilder().newDocument();

    final Element root =
      (Element) document.appendChild(
        document.createElementNS(NS, "Section")
      );

    root.setAttribute("title", command.metadata().name());
    root.setAttribute("id", idFor(command).toString());

    sectionName(context, command, document, root);
    sectionDescription(context, command, document, root);
    sectionExamples(document, root);

    write(document, context.output());
  }

  private static void sectionExamples(
    final Document document,
    final Element root)
  {
    final Element examples =
      (Element) root.appendChild(document.createElementNS(NS, "Subsection"));
    examples.setAttribute("title", "Examples");

    final var formal =
      (Element) examples.appendChild(document.createElementNS(
        NS,
        "FormalItem"));

    formal.setAttribute("title", "Example");
    formal.setAttribute("type", "example");

    final var verbatim =
      (Element) formal.appendChild(document.createElementNS(
        NS, "Verbatim"));
    verbatim.setTextContent("...");
  }

  private static void sectionDescription(
    final QCommandContextType context,
    final QCommandType command,
    final Document document,
    final Element root)
  {
    final Element description =
      (Element) root.appendChild(document.createElementNS(NS, "Subsection"));
    description.setAttribute("title", "Description");

    final var para =
      (Element) description.appendChild(document.createElementNS(
        NS,
        "Paragraph"));

    final var meta =
      command.metadata();
    final var term =
      (Element) para.appendChild(document.createElementNS(NS, "Term"));
    term.setAttribute("type", "command");
    term.setTextContent(meta.name());

    para.appendChild(document.createTextNode("The "));
    para.appendChild(term);
    para.appendChild(document.createTextNode(" command... "));

    final var named = command.onListNamedParameters();
    if (!named.isEmpty()) {
      final var formal =
        (Element) description.appendChild(document.createElementNS(
          NS,
          "FormalItem"));
      formal.setAttribute("title", "Parameters");

      final var table =
        (Element) formal.appendChild(document.createElementNS(NS, "Table"));

      table.setAttribute("type", "parameterTable");

      final var columns =
        (Element) table.appendChild(document.createElementNS(NS, "Columns"));

      final var c0 =
        (Element) columns.appendChild(document.createElementNS(NS, "Column"));
      c0.setTextContent("Parameter");

      final var c1 =
        (Element) columns.appendChild(document.createElementNS(NS, "Column"));
      c1.setTextContent("Type");

      final var c2 =
        (Element) columns.appendChild(document.createElementNS(NS, "Column"));
      c2.setTextContent("Cardinality");

      final var c3 =
        (Element) columns.appendChild(document.createElementNS(NS, "Column"));
      c3.setTextContent("Default");

      final var c4 =
        (Element) columns.appendChild(document.createElementNS(NS, "Column"));
      c4.setTextContent("Description");

      final var sorted = new ArrayList<>(named);
      sorted.sort(Comparator.comparing(QParameterType::name));

      for (final var param : sorted) {
        final var row =
          (Element) table.appendChild(document.createElementNS(NS, "Row"));

        generateCellForName(document, param, row);
        generateCellForType(document, param, row);
        generateCellForCardinality(document, param, row);
        generateCellForDefault(context, document, param, row);
        generateCellForDescription(context, document, param, row);
      }
    }
  }

  private static void generateCellForDescription(
    final QCommandContextType context,
    final Document document,
    final QParameterNamedType<?> param,
    final Element row)
  {
    final var cell =
      (Element) row.appendChild(document.createElementNS(NS, "Cell"));
    cell.setTextContent(context.localize(param.description()));
  }

  private static void generateCellForDefault(
    final QCommandContextType context,
    final Document document,
    final QParameterNamedType<?> param,
    final Element row)
  {
    final var cell =
      (Element) row.appendChild(document.createElementNS(NS, "Cell"));
    final var term =
      (Element) cell.appendChild(document.createElementNS(NS, "Term"));

    final QValueConverterType<Object> c =
      (QValueConverterType<Object>)
        context.valueConverters()
          .converterFor(param.type())
          .orElseThrow();

    term.setAttribute("type", "constant");
    if (param instanceof final QParameterNamed1<?> n) {
      term.setTextContent(
        n.defaultValue()
          .map(c::convertToString)
          .orElse("")
      );
    } else if (param instanceof final QParameterNamed01<?> n) {
      term.setTextContent(
        n.defaultValue()
          .map(c::convertToString)
          .orElse("")
      );
    } else if (param instanceof final QParameterNamed0N<?> n) {
      term.setTextContent(
        new StringBuilder(128)
          .append("[")
          .append(
            n.defaultValue()
              .stream()
              .map(c::convertToString)
              .collect(Collectors.joining(", ")))
          .append("]")
          .toString()
      );
    } else if (param instanceof final QParameterNamed1N<?> n) {
      term.setTextContent(
        new StringBuilder(128)
          .append("[")
          .append(
            n.defaultValue()
              .stream()
              .map(c::convertToString)
              .collect(Collectors.joining(", ")))
          .append("]")
          .toString()
      );
    }
  }

  private static void generateCellForCardinality(
    final Document document,
    final QParameterNamedType<?> param,
    final Element row)
  {
    final var cell =
      (Element) row.appendChild(document.createElementNS(NS, "Cell"));
    final var term =
      (Element) cell.appendChild(document.createElementNS(NS, "Term"));

    term.setAttribute("type", "expression");
    if (param instanceof QParameterNamed1<?>) {
      term.setTextContent("[1, 1]");
    } else if (param instanceof QParameterNamed01<?>) {
      term.setTextContent("[0, 1]");
    } else if (param instanceof QParameterNamed0N<?>) {
      term.setTextContent("[0, N]");
    } else if (param instanceof QParameterNamed1N<?>) {
      term.setTextContent("[1, N]");
    }
  }

  private static void generateCellForType(
    final Document document,
    final QParameterNamedType<?> param,
    final Element row)
  {
    final var cell =
      (Element) row.appendChild(document.createElementNS(NS, "Cell"));
    final var term =
      (Element) cell.appendChild(document.createElementNS(NS, "Term"));
    term.setTextContent(param.type().getCanonicalName());
    term.setAttribute("type", "type");
  }

  private static void generateCellForName(
    final Document document,
    final QParameterNamedType<?> param,
    final Element row)
  {
    final var cell =
      (Element) row.appendChild(document.createElementNS(NS, "Cell"));
    final var term =
      (Element) cell.appendChild(document.createElementNS(NS, "Term"));
    term.setAttribute("type", "parameter");
    term.setTextContent(param.name());
  }

  private static void sectionName(
    final QCommandContextType context,
    final QCommandType command,
    final Document document,
    final Element root)
  {
    final Element name =
      (Element) root.appendChild(document.createElementNS(NS, "Subsection"));
    name.setAttribute("title", "Name");

    final var para =
      (Element) name.appendChild(document.createElementNS(NS, "Paragraph"));

    final var term =
      (Element) para.appendChild(document.createElementNS(NS, "Term"));
    term.setAttribute("type", "command");

    final var meta = command.metadata();
    term.setTextContent(meta.name());
    para.appendChild(document.createTextNode(" - "));
    para.appendChild(document.createTextNode(context.localize(meta.shortDescription())));
  }

  private static UUID idFor(
    final QCommandType command)
  {
    final var meta = command.metadata();
    try (var data = new ByteArrayOutputStream()) {
      data.write(meta.name().getBytes(UTF_8));
      data.write(meta.shortDescription().toString().getBytes(UTF_8));
      return UUID.nameUUIDFromBytes(data.toByteArray());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void write(
    final Document document,
    final PrintWriter output)
    throws TransformerException
  {
    final var tr = TransformerFactory.newInstance().newTransformer();
    tr.setOutputProperty(OutputKeys.INDENT, "yes");
    tr.setOutputProperty(OutputKeys.METHOD, "xml");
    tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    tr.transform(
      new DOMSource(document),
      new StreamResult(output)
    );
  }
}
