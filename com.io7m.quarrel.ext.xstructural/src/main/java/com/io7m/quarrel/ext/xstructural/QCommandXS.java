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
import com.io7m.quarrel.core.QException;
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

  private static final QParameterNamed1<String> TYPE =
    new QParameterNamed1<>(
      "--type",
      List.of(),
      new QConstant("The type of output."),
      Optional.empty(),
      String.class
    );

  private static final QParameterNamed1<String> PARAMETERS_INCLUDE_NAME =
    new QParameterNamed1<>(
      "--parameters-include",
      List.of(),
      new QConstant("The name of the file to include for parameters."),
      Optional.of("parameters.xml"),
      String.class
    );

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
    return List.of(TYPE, PARAMETERS_INCLUDE_NAME);
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
    final var type =
      context.parameterValue(TYPE);
    final var result =
      QCommandTreeResolver.resolve(context.commandTree(), command);

    switch (result) {
      case final QResolutionRoot r -> {
        return SUCCESS;
      }
      case final QResolutionOKCommand cmd -> {
        showCommand(context, type, cmd.command());
        return SUCCESS;
      }
      case final QResolutionOKGroup group -> {
        return SUCCESS;
      }
      case final QResolutionErrorDoesNotExist r -> {
        return FAILURE;
      }
    }
  }

  private static final String NS =
    "urn:com.io7m.structural:8:0";

  private static final String NS_XI =
    "http://www.w3.org/2001/XInclude";

  private static void showCommand(
    final QCommandContextType context,
    final String type,
    final QCommandType command)
    throws Exception
  {
    final var documents =
      DocumentBuilderFactory.newDefaultNSInstance();
    final var document =
      documents.newDocumentBuilder().newDocument();

    switch (type) {
      case "main" -> {
        final Element root =
          (Element) document.appendChild(
            document.createElementNS(NS, "Section")
          );

        root.setAttribute("xmlns:xi", NS_XI);
        root.setAttribute("title", command.metadata().name());
        root.setAttribute("id", idFor(command).toString());

        sectionName(context, command, document, root);
        sectionDescription(context, command, document, root);
        sectionExamples(document, root);
      }
      case "parameters" -> {
        final Element root =
          (Element) document.appendChild(
            parameterTable(
              context,
              command,
              document,
              command.onListNamedParameters()
            )
          );
      }
      default -> {
        throw new IllegalStateException("Unexpected value: " + type);
      }
    }

    write(document, context.output());
  }

  private static void sectionExamples(
    final Document document,
    final Element root)
  {
    final Element examples =
      (Element) root.appendChild(
        document.createElementNS(NS, "Subsection")
      );
    examples.setAttribute("title", "Examples");

    final var formal =
      (Element) examples.appendChild(
        document.createElementNS(NS, "FormalItem")
      );

    formal.setAttribute("title", "Example");
    formal.setAttribute("type", "example");

    final var verbatim =
      (Element) formal.appendChild(
        document.createElementNS(NS, "Verbatim")
      );
    verbatim.setTextContent("...");
  }

  private static void sectionDescription(
    final QCommandContextType context,
    final QCommandType command,
    final Document document,
    final Element root)
  {
    final Element description =
      (Element) root.appendChild(
        document.createElementNS(NS, "Subsection")
      );
    description.setAttribute("title", "Description");

    final var para =
      (Element) description.appendChild(
        document.createElementNS(NS, "Paragraph")
      );

    final var meta =
      command.metadata();
    final var term =
      (Element) para.appendChild(
        document.createElementNS(NS, "Term")
      );
    term.setAttribute("type", "command");
    term.setTextContent(meta.name());

    para.appendChild(document.createTextNode("The "));
    para.appendChild(term);
    para.appendChild(document.createTextNode(" command... "));

    final var named = command.onListNamedParameters();
    if (!named.isEmpty()) {
      final var formal =
        (Element) description.appendChild(
          document.createElementNS(NS, "FormalItem")
        );

      formal.setAttribute("title", "Parameters");

      final var e =
        (Element) description.appendChild(
          document.createElementNS(NS_XI, "xi:include")
        );

      e.setAttribute("href", context.parameterValue(PARAMETERS_INCLUDE_NAME));
      formal.appendChild(e);
    }
  }

  private static Element parameterTable(
    final QCommandContextType context,
    final QCommandType command,
    final Document document,
    final List<QParameterNamedType<?>> named)
    throws QException
  {
    final var root =
      (Element) document.createElementNS(NS, "Subsection");

    root.setAttribute("title", "Parameters");

    final var sorted = new ArrayList<>(named);
    sorted.sort(Comparator.comparing(QParameterType::name));

    for (final var param : sorted) {
      final var formal =
        (Element) document.createElementNS(NS, "FormalItem");

      formal.setAttribute("title", param.name());
      formal.setAttribute("id", UUID.nameUUIDFromBytes(
        (command.metadata().name() + ":" + param.name()).getBytes(UTF_8)
      ).toString());

      final var table =
        (Element) document.createElementNS(NS, "Table");

      table.setAttribute("type", "parameterTable");

      final var columns =
        (Element) table.appendChild(
          document.createElementNS(NS, "Columns")
        );

      final var c0 =
        (Element) columns.appendChild(
          document.createElementNS(NS, "Column")
        );
      c0.setTextContent("Attribute");

      final var c1 =
        (Element) columns.appendChild(
          document.createElementNS(NS, "Column")
        );
      c1.setTextContent("Value");

      table.appendChild(generateRowForName(document, param));
      table.appendChild(generateRowForType(document, param));
      table.appendChild(generateRowForDefaults(context, document, param));
      table.appendChild(generateRowForCardinality(document, param));
      table.appendChild(generateRowForDescription(context, document, param));

      formal.appendChild(table);
      root.appendChild(formal);
    }

    return root;
  }

  private static Element generateRowForDescription(
    final QCommandContextType context,
    final Document document,
    final QParameterNamedType<?> param)
  {
    final var row =
      (Element) document.createElementNS(NS, "Row");
    final var cellN =
      (Element) document.createElementNS(NS, "Cell");
    final var cellV =
      (Element) document.createElementNS(NS, "Cell");

    final var vNode =
      document.createTextNode(context.localize(param.description()));

    cellN.appendChild(document.createTextNode("Description"));
    cellV.appendChild(vNode);

    row.appendChild(cellN);
    row.appendChild(cellV);
    return row;
  }

  private static Element generateRowForCardinality(
    final Document document,
    final QParameterNamedType<?> param)
  {
    final var row =
      (Element) document.createElementNS(NS, "Row");
    final var cellN =
      (Element) document.createElementNS(NS, "Cell");
    final var cellV =
      (Element) document.createElementNS(NS, "Cell");

    final var vNode =
      generateCellForCardinality(document, param);

    cellN.appendChild(document.createTextNode("Cardinality"));
    cellV.appendChild(vNode);

    row.appendChild(cellN);
    row.appendChild(cellV);
    return row;
  }

  private static Element generateRowForType(
    final Document document,
    final QParameterNamedType<?> param)
  {
    final var row =
      (Element) document.createElementNS(NS, "Row");
    final var cellN =
      (Element) document.createElementNS(NS, "Cell");
    final var cellV =
      (Element) document.createElementNS(NS, "Cell");

    final var vNode =
      document.createElementNS(NS, "Term");
    vNode.setAttribute("type", "class");
    vNode.setTextContent(param.type().getCanonicalName());

    cellN.appendChild(document.createTextNode("Type"));
    cellV.appendChild(vNode);

    row.appendChild(cellN);
    row.appendChild(cellV);
    return row;
  }

  private static Element generateRowForName(
    final Document document,
    final QParameterNamedType<?> param)
  {
    final var row =
      (Element) document.createElementNS(NS, "Row");
    final var cellN =
      (Element) document.createElementNS(NS, "Cell");
    final var cellV =
      (Element) document.createElementNS(NS, "Cell");

    final var vNode =
      document.createElementNS(NS, "Term");
    vNode.setAttribute("type", "parameter");
    vNode.setTextContent(param.name());

    cellN.appendChild(document.createTextNode("Name"));
    cellV.appendChild(vNode);

    row.appendChild(cellN);
    row.appendChild(cellV);
    return row;
  }

  private static Element generateRowForDefaults(
    final QCommandContextType context,
    final Document document,
    final QParameterNamedType<?> param)
    throws QException
  {
    final var row =
      (Element) document.createElementNS(NS, "Row");
    final var cellN =
      (Element) document.createElementNS(NS, "Cell");
    final var cellV =
      (Element) document.createElementNS(NS, "Cell");

    final var vNode =
      generateTermForDefault(context, document, param);

    cellN.appendChild(document.createTextNode("Default Value"));
    cellV.appendChild(vNode);

    row.appendChild(cellN);
    row.appendChild(cellV);
    return row;
  }

  private static Element generateTermForDefault(
    final QCommandContextType context,
    final Document document,
    final QParameterNamedType<?> param)
    throws QException
  {
    final var term =
      (Element) document.createElementNS(NS, "Term");

    final QValueConverterType<Object> c =
      (QValueConverterType<Object>)
        context.valueConverters()
          .converterFor(param.type())
          .orElseThrow();

    term.setAttribute("type", "constant");
    if (param instanceof final QParameterNamed1<?> n) {
      term.setTextContent(formatParameterCellContent1(c, n));
    } else if (param instanceof final QParameterNamed01<?> n) {
      term.setTextContent(formatParameterCellContent01(c, n));
    } else if (param instanceof final QParameterNamed0N<?> n) {
      term.setTextContent(formatParameterCellContent0N(c, n));
    } else if (param instanceof final QParameterNamed1N<?> n) {
      term.setTextContent(formatParameterCellContent1N(c, n));
    }

    return term;
  }

  private static String formatParameterCellContent1N(
    final QValueConverterType<Object> c,
    final QParameterNamed1N<?> n)
    throws QException
  {
    final String text;
    final var opt = n.defaultValue();
    if (opt.isPresent()) {
      text = c.convertToString(opt.get());
    } else {
      text = "";
    }

    return new StringBuilder(128)
      .append("[")
      .append(text)
      .append("]")
      .toString();
  }

  private static String formatParameterCellContent0N(
    final QValueConverterType<Object> c,
    final QParameterNamed0N<?> n)
    throws QException
  {
    final List<String> items = new ArrayList<>();
    for (final var o : n.defaultValue()) {
      items.add(c.convertToString(o));
    }
    return new StringBuilder(128)
      .append("[")
      .append(String.join(", ", items))
      .append("]")
      .toString();
  }

  private static String formatParameterCellContent01(
    final QValueConverterType<Object> c,
    final QParameterNamed01<?> n)
    throws QException
  {
    final var opt = n.defaultValue();
    if (opt.isPresent()) {
      return c.convertToString(opt.get());
    }
    return "";
  }

  private static String formatParameterCellContent1(
    final QValueConverterType<Object> c,
    final QParameterNamed1<?> n)
    throws QException
  {
    final var opt = n.defaultValue();
    if (opt.isPresent()) {
      return c.convertToString(opt.get());
    }
    return "";
  }

  private static Element generateCellForCardinality(
    final Document document,
    final QParameterNamedType<?> param)
  {
    final var term =
      (Element) document.appendChild(document.createElementNS(NS, "Term"));

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

    return term;
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
