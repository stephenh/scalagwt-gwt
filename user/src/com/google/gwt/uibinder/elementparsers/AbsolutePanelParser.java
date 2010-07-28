/*
 * Copyright 2010 Google Inc.
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
import com.google.gwt.uibinder.rebind.UiBinderWriter;
import com.google.gwt.uibinder.rebind.XMLElement;

/**
 * Parses {@link com.google.gwt.user.client.ui.AbsolutePanel AbsolutePanel}
 * widgets.
 */
public class AbsolutePanelParser implements ElementParser {

  private static final String CHILD = "child";

  public void parse(XMLElement elem, String fieldName, JClassType type,
      UiBinderWriter writer) throws UnableToCompleteException {

    // Parse children.
    for (XMLElement positionElem : elem.consumeChildElements()) {
      // Ensure position element.
      if (!isPositionElement(elem, positionElem)) {
        writer.die(positionElem, "Only <%s:%s> children are allowed.",
            elem.getPrefix(), CHILD);
      }

      // Parse position.
      String left = positionElem.consumeRequiredIntAttribute("left");
      String top = positionElem.consumeRequiredIntAttribute("top");

      // Add child widget.
      XMLElement widgetElem = positionElem.consumeSingleChildElement();
      String widgetFieldName = writer.parseElementToField(widgetElem);
      writer.addStatement("%1$s.add(%2$s, %3$s, %4$s);", fieldName,
          widgetFieldName, left, top);
    }
  }

  private boolean isPositionElement(XMLElement parent, XMLElement child) {
    if (!parent.getNamespaceUri().equals(child.getNamespaceUri())) {
      return false;
    }
    if (!CHILD.equals(child.getLocalName())) {
      return false;
    }
    return true;
  }
}
