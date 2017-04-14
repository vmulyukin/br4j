/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.aplana.scanner.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * Split button that provides a drop down menu when the right side arrow is clicked.
 * <p/>
 * Based on Edward Scholl's (edscholl@atwistedweb.com)
 * <a href="http://www.atwistedweb.com/java/SplitButton.html"><code>SplitButton</code></a>.
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class SplitButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 4959870238429816362L;
	
	private JButton mainButton, dropDownButton;
	private JPopupMenu dropDownMenu;

	/**
   * Default Constructor that creates a blank button with a down facing arrow.
   */
	public SplitButton() {
		this(" ");
	}
	
	/**
   * Creates a button with the specified text  and a down facing arrow.
   * 
   * @param text String
   */
	public SplitButton(String text) {
		this(new JButton(text), SwingConstants.SOUTH);
	}

	/**
   * Creates a button with the specified text 
   * and a arrow in the specified direction.
   * 
   * @param text String
   * @param orientation int
   */
	public SplitButton(String text, int orientation) {
		this(new JButton(text), orientation);
	}

	/**
   * Passes in the button to use in the left hand side, with the specified 
   * orientation for the arrow on the right hand side.
   * 
   * @param mainButton JButton
   * @param orientation int
   */
	public SplitButton(JButton mainButton, int orientation) {
		super();
		this.dropDownMenu = new JPopupMenu();
		this.mainButton = mainButton;
		
		this.dropDownButton  = new BasicArrowButton(orientation);
		dropDownButton.addActionListener(this);
		
		this.setBorderPainted(false);
		this.dropDownButton.setBorderPainted(false);
		this.mainButton.setBorderPainted(false);
		
		this.setPreferredSize(new Dimension(75, 34));
		this.setMaximumSize(new Dimension(75, 34));
		this.setMinimumSize(new Dimension(200, 34));
		
		this.setLayout(new BorderLayout());
		this.setMargin(new Insets(-3, -3,-3,-3));
		
		this.add(mainButton, BorderLayout.CENTER);
		this.add(dropDownButton, BorderLayout.EAST);
	}

	/**
   * Adds a menu item to the popup menu to show when the arrow is clicked.
   * 
   * @param  menuItem  the menu item to add
   * @return the <code>JMenuItem</code> added
   */
	public JMenuItem addMenuItem(JMenuItem menuItem) {
		return this.dropDownMenu.add(menuItem);
	}

	/**
   * Returns the main (left hand side) button.
   * 
   * @return JButton
   */
	public JButton getMainButton() {
		return mainButton;
	}
	
	/**
	 * Gets the drop down button (with the arrow).
	 * 
	 * @return JButton
	 */
	public JButton getDropDownButton() {
		return dropDownButton;
	}
	
	/**
	 * Gets the drop down menu.
	 * 
	 * @return JPopupMenu
	 */
	public JPopupMenu getMenu() {
		return dropDownMenu;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (this.dropDownMenu == null) {
			return;
		}
		dropDownMenu.show(this, 0, getHeight());
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.AbstractButton#addActionListener(java.awt.event.ActionListener)
	 */
	@Override
	public void addActionListener(ActionListener l){
		if (l != this) {
			this.mainButton.addActionListener(l);
			super.addActionListener(l);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.AbstractButton#removeActionListener(java.awt.event.ActionListener)
	 */
	@Override
	public void removeActionListener(ActionListener l) {
		if (l != this) {
			this.mainButton.removeActionListener(l);
			super.removeActionListener(l);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.AbstractButton#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		mainButton.setEnabled(b);
		dropDownButton.setEnabled(b);
	}

	/* (non-Javadoc)
	 * @see javax.swing.AbstractButton#setIcon(javax.swing.Icon)
	 */
	@Override
	public void setIcon(Icon defaultIcon) {
		mainButton.setIcon(defaultIcon);
	}

	/* (non-Javadoc)
	 * @see javax.swing.AbstractButton#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		mainButton.setText(text);
	}
}
