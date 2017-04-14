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
package com.aplana.scanner.twain;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata.Type;
import uk.co.mmscomputing.device.twain.TwainConstants;
import uk.co.mmscomputing.device.twain.TwainIOException;
import uk.co.mmscomputing.device.twain.TwainIOMetadata;
import uk.co.mmscomputing.device.twain.TwainSource;
import uk.co.mmscomputing.device.twain.jtwain;

import com.aplana.scanner.ScannerApplet;
import com.aplana.scanner.ScannerController;
import com.aplana.scanner.ui.ScannerPanel;
import com.aplana.scanner.util.ResourceLoader;

/**
 * Panel with TWAIN specific controls.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class TwainPanel extends JComponent implements ScannerListener {
	private static final long serialVersionUID = 3364432742034660156L;
	private static final Log logger = LogFactory.getLog(TwainPanel.class);
	
	private JButton scanButton;
	private JButton selectButton;
	private JCheckBox guiCheckBox;
	
	/**
	 * Constructs an instance of the class.
	 *
	 * @param  controller  the {@link ScannerController}
	 * @throws TwainIOException if TWAIN driver cannot be loaded
	 */
	public TwainPanel(ScannerController controller) throws TwainIOException {
		setLayout(new GridLayout(0, 1));
		scanButton = new JButton(ScannerApplet.getMessage("label.button.scan"),
						new ImageIcon(ResourceLoader.getResource(ScannerPanel.SCAN_IMAGE_PATH)));
		scanButton.addActionListener((ActionListener)EventHandler.create(
						ActionListener.class, controller, "scan"));
		add(scanButton);
		
		guiCheckBox = new JCheckBox(ScannerApplet.getMessage("label.checkbox.gui"));
		guiCheckBox.setSelected(true);
		add(guiCheckBox);
		
		selectButton = new JButton(ScannerApplet.getMessage("label.button.select"),
						new ImageIcon(ResourceLoader.getResource(ScannerPanel.SELECT_IMAGE_PATH)));
		selectButton.addActionListener((ActionListener)EventHandler.create(
						ActionListener.class, controller, "select"));
		add(selectButton);
		
		controller.addListener(this);
		
		if (jtwain.getSource().isBusy()) {
			scanButton.setEnabled(false);
			guiCheckBox.setEnabled(false);
			selectButton.setEnabled(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.co.mmscomputing.device.scanner.ScannerListener#update(uk.co.mmscomputing.device.scanner.ScannerIOMetadata.Type, uk.co.mmscomputing.device.scanner.ScannerIOMetadata)
	 */
	public void update(Type type, ScannerIOMetadata metadata) {
		if (metadata instanceof TwainIOMetadata) {
			TwainIOMetadata twainMetadata = (TwainIOMetadata)metadata;
			TwainSource source = twainMetadata.getSource();
			
			if (type.equals(ScannerIOMetadata.STATECHANGE)) {
				if (twainMetadata.isState(TwainConstants.STATE_SRCMNGOPEN)) {
					if (source.isBusy()) {
						scanButton.setEnabled(false);
						guiCheckBox.setEnabled(false);
						selectButton.setEnabled(false);
					} else {
						scanButton.setEnabled(true);
						guiCheckBox.setEnabled(true);
						selectButton.setEnabled(true);
					}
				}
			} else if (type.equals(ScannerIOMetadata.NEGOTIATE)) {
				// if it is possible to hide source's GUI then use checkbox value
				// else set to true whatever the user selected
				if (source.isUIControllable())
					source.setShowUI(guiCheckBox.isSelected());
				else {
					if (!guiCheckBox.isSelected())
						logger.warn("Failed to hide twain source's GUI");
					guiCheckBox.setSelected(true);
				}
			}
		}
	}
}
