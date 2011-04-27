/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.uibinder.elementparsers;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.uibinder.elementparsers.HtmlMessageInterpreter.PlaceholderInterpreterProvider;
import com.google.gwt.uibinder.rebind.UiBinderWriter;
import com.google.gwt.uibinder.rebind.XMLElement;
import com.google.gwt.uibinder.rebind.messages.MessageWriter;
import com.google.gwt.uibinder.rebind.messages.PlaceholderInterpreter;

/**
 * Parses {@link com.google.gwt.user.client.ui.AttachableHTMLPanel} widgets.
 */
public class AttachableHTMLPanelParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, JClassType type,
      final UiBinderWriter writer) throws UnableToCompleteException {

    assert writer.useLazyWidgetBuilders();

    /*
     * Gathers up elements that indicate nested Attachable objects.
     */
    AttachableInterpreter attachableInterpreter = null;
    attachableInterpreter = new AttachableInterpreter(
        fieldName, writer);

    /*
     * Gathers up elements that indicate nested widgets (but only those that are
     * not inside msg elements).
     */
    WidgetInterpreter widgetInterpreter = new WidgetInterpreter(fieldName,
        writer);

    /*
     * Handles non-widget elements like msg, and dom elements with ui:field
     * attributes. There may be widgets inside a msg, which is why the
     * construction in makeHtmlInterpreter is so complicated.
     */
    HtmlInterpreter htmlInterpreter = makeHtmlInterpreter(fieldName, writer);

    writer.beginAttachedSection(fieldName);

    final InterpreterPipe interpreters;
    interpreters = InterpreterPipe.newPipe(
        attachableInterpreter, widgetInterpreter, htmlInterpreter);
    String html = elem.consumeInnerHtml(interpreters);

    writer.endAttachedSection();

    /*
     * AttachableHTMLPanel has no no-arg ctor, so we have to generate our own, using the
     * element's innerHTML and perhaps its tag attribute. Do this in a way that
     * will not break subclasses if they happen to have the same constructor
     * signature (by passing in type).
     */
    String customTag = elem.consumeStringAttribute("tag", null);

    // TODO(rdcastro): Add support for custom tags in AttachableHTMLPanel.
    if (customTag != null) {
      writer.getLogger().die(
          "AttachableHTMLPanel does not support custom root elements yet.");
    }

    writer.setFieldInitializerAsConstructor(fieldName, type, writer.declareTemplateCall(html));
  }

  /**
   * Creates an HtmlInterpreter with our specialized placeholder interpreter,
   * which will allow widget instances to be declared inside of ui:msg elements.
   */
  private HtmlInterpreter makeHtmlInterpreter(final String fieldName,
      final UiBinderWriter uiWriter) {
    final String ancestorExpression = fieldName;

    PlaceholderInterpreterProvider placeholderInterpreterProvider =
        new PlaceholderInterpreterProvider() {
      public PlaceholderInterpreter get(MessageWriter message) {
        return new WidgetPlaceholderInterpreter(fieldName, uiWriter, message,
            ancestorExpression);
      }
    };

    HtmlInterpreter htmlInterpreter = new HtmlInterpreter(uiWriter,
        ancestorExpression, new HtmlMessageInterpreter(uiWriter,
            placeholderInterpreterProvider));

    return htmlInterpreter;
  }
}
