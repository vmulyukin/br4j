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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.aplana.scanner.ScannerApplet;
import com.aplana.scanner.model.Page;

/**
 * <code>ListCellRender</code> for the page list.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class PageListCellRenderer extends JPanel implements ListCellRenderer {
	private static final long serialVersionUID = 953443123602377538L;
	
	private static final int BORDER_SIZE = 5;
	
	private JLabel pageLabel;
	private JLabel imageLabel;

	/**
	 * Default constructor.
	 */
	public PageListCellRenderer() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, 0, BORDER_SIZE, 0));
		
		imageLabel = new JLabel();
		imageLabel.setOpaque(true);
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		imageLabel.setVerticalAlignment(JLabel.CENTER);
		imageLabel.setBackground(Color.WHITE);
		imageLabel.setBorder(new CompoundBorder(
						new LineBorder(Color.BLACK, 1), new EmptyBorder(1, 1, 1, 1)));
		addComponent(imageLabel, this);
		
		pageLabel = new JLabel(" ");
		addComponent(pageLabel, this);
		
		setOpaque(true);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
					int index, boolean isSelected, boolean cellHasFocus) {
		Page page = (Page)value;
		pageLabel.setText(getPageTitle(page, index));
		imageLabel.setIcon(page.getThumbnail(this));
		
		if (isSelected)
			adjustColors(list.getSelectionBackground(), list.getSelectionForeground(), this, pageLabel);
		else
			adjustColors(list.getBackground(), list.getForeground(), this, pageLabel);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics(pageLabel.getFont());
		int imageLabelWidth = imageLabel.getIcon().getIconWidth() +
			imageLabel.getInsets().left + imageLabel.getInsets().right;
		int imageLabelHeight = imageLabel.getIcon().getIconHeight() +
			imageLabel.getInsets().top + imageLabel.getInsets().bottom;
		int width = Math.max(fm.stringWidth(pageLabel.getText()), imageLabelWidth);
		int height = fm.getHeight() + imageLabelHeight + getInsets().top + getInsets().bottom;
		return new Dimension(width, height);
	}

	private void addComponent(JComponent component, Container container) {
		component.setAlignmentX(Component.CENTER_ALIGNMENT);
		container.add(component);
	}
	
	private String getPageTitle(Page page, int index) {
		StringBuilder sb = new StringBuilder()
			.append(ScannerApplet.getMessage("page.title"))
			.append(' ')
			.append(index + 1);
		return sb.toString();
	}
	
	private void adjustColors(Color bg, Color fg, Component... components) {
		for (Component component : components) {
			component.setForeground(fg);
			component.setBackground(bg);
		}
	}
}
