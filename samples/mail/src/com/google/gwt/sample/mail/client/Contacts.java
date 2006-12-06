/*
 * Copyright 2006 Google Inc.
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
package com.google.gwt.sample.mail.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A component that displays a list of contacts.
 */
public class Contacts extends Composite {

  /**
   * Simple data structure representing a contact.
   */
  private class Contact {
    public String email;
    public String name;
    public String photo = "default_photo.jpg";

    public Contact(String name, String email) {
      this.name = name;
      this.email = email;
    }
  }

  /**
   * A simple popup that displays a contact's information.
   */
  private class ContactPopup extends PopupPanel {

    public ContactPopup(Contact contact) {
      // The popup's constructor's argument is a boolean specifying that it
      // auto-close itself when the user clicks outside of it.
      super(true);

      VerticalPanel inner = new VerticalPanel();
      Label nameLabel = new Label(contact.name);
      Label emailLabel = new Label(contact.email);
      inner.add(nameLabel);
      inner.add(emailLabel);

      HorizontalPanel panel = new HorizontalPanel();
      panel.setSpacing(4);
      panel.add(new Image(contact.photo));
      panel.add(inner);

      add(panel);
      setStyleName("mail-ContactPopup");
      nameLabel.setStyleName("mail-ContactPopupName");
      emailLabel.setStyleName("mail-ContactPopupEmail");
    }
  }

  private Contact[] contacts = new Contact[]{
    new Contact("Benoit Mandelbrot", "benoit@example.com"),
    new Contact("Albert Einstein", "albert@example.com"),
    new Contact("Rene Descartes", "rene@example.com"),
    new Contact("Bob Saget", "bob@example.com"),
    new Contact("Ludwig von Beethoven", "ludwig@example.com"),
    new Contact("Richard Feynman", "richard@example.com"),
    new Contact("Alan Turing", "alan@example.com"),
    new Contact("John von Neumann", "john@example.com")};

  private VerticalPanel panel = new VerticalPanel();

  public Contacts() {
    // Add all the contacts to the list.
    for (int i = 0; i < contacts.length; ++i)
      addContact(contacts[i]);

    initWidget(panel);
    setStyleName("mail-Contacts");
  }

  private void addContact(final Contact contact) {
    final HTML link = new HTML("<a href='javascript:;'>" + contact.name + "</a>");
    panel.add(link);

    // Add a click listener that displays a ContactPopup when it is clicked.
    link.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        ContactPopup popup = new ContactPopup(contact);
        int left = link.getAbsoluteLeft() + 32;
        int top = link.getAbsoluteTop() + 8;
        popup.setPopupPosition(left, top);
        popup.show();
      }
    });
  }
}
