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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * Dialog to show progress of a task.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class ProgressDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -3713525032126151048L;
	
	private JProgressBar progressBar;
	private JLabel statusLabel;
	private JButton cancelButton;
	private boolean canceled;
	private boolean done;
	
	/**
	 * Constructs a progress dialog.
	 *
	 * @param owner   the <code>Frame</code> from which the dialog is displayed
	 * @param title   the string to display in the dialog's title bar
	 * @param status  the progress status string
	 * @param min     the progress minimum value
	 * @param max     the progress maximum value
	 */
	public ProgressDialog(Frame owner, String title, String status, int min, int max) {
		super(owner, title, true);
		
		JPanel progressPane = new JPanel();
		progressPane.setLayout(new BoxLayout(progressPane, BoxLayout.PAGE_AXIS));
		statusLabel = new JLabel(status);
		progressBar = new JProgressBar(min, max);
		progressBar.setPreferredSize(
						new Dimension(300, UIManager.getDimension("ProgressBar.horizontalSize").height));
		progressPane.add(statusLabel);
		progressPane.add(progressBar);
		progressPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		cancelButton = new JButton(UIManager.getString("OptionPane.cancelButtonText"));
		cancelButton.addActionListener(this);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.setBorder(new EmptyBorder(0, 10, 10, 10));
		
		Container content = getContentPane();
		content.add(progressPane, BorderLayout.CENTER);
		content.add(buttonPane, BorderLayout.PAGE_END);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(false);
  }
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (done)
			dispose();
		else
			canceled = true;
	}

	/**
	 * Indicates whether the task is canceled.
	 *
	 * @return <code>true</code> if the task is canceled, or <code>false</code> otherwise
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Gets the progress bar's current value. The value is always between the minimum and maximum
	 * values, inclusive.
	 * 
	 * @see #setProgress(int)
	 * @see javax.swing.JProgressBar#getValue()
	 */
	public int getProgress() {
		return progressBar.getValue();
	}
	
	/**
	 * Sets the progress bar's current value. If the value is greater or equal to the progress
	 * maximum value the dialog is closed.
	 *
	 * @param progress  the new progress value
	 * 
	 * @see #getProgress()
	 * @see javax.swing.JProgressBar#setValue(int)
	 */
	public void setProgress(int progress) {
		progressBar.setValue(progress < getMaximum() ? progress : getMaximum());
	}
	
	/**
	 * Gets the progress minimum value.
	 * 
	 * @see #setMinimum(int)
	 * @see javax.swing.JProgressBar#getMinimum()
	 */
	public int getMinimum() {
		return progressBar.getMinimum();
	}
	
	/**
	 * Sets the progress minimum value.
	 *
	 * @param min  the new minimum
	 * 
	 * @see #getMinimum()
	 * @see javax.swing.JProgressBar#setMinimum(int)
	 */
	public void setMinimum(int min) {
		progressBar.setMinimum(min);
	}
	
	/**
	 * Gets the progress maximum value.
	 * 
	 * @see #setMaximum(int)
	 * @see javax.swing.JProgressBar#getMaximum()
	 */
	public int getMaximum() {
		return progressBar.getMaximum();
	}
	
	/**
	 * Sets the progress maximum value.
	 *
	 * @param max  the new maximum
	 * 
	 * @see #getMaximum()
	 * @see javax.swing.JProgressBar#setMaximum(int)
	 */
	public void setMaximum(int max) {
		progressBar.setMaximum(max);
	}
	
	/**
	 * Indicates whether the progress dialog is in determinate or indeterminate mode.
	 *
	 * @see #setIndeterminate(boolean)
	 * @see javax.swing.JProgressBar#isIndeterminate()
	 */
	public boolean isIndeterminate() {
		return progressBar.isIndeterminate();
	}
	
	/**
	 * Determines whether the progress dialog is in determinate or indeterminate mode.
	 * An indeterminate progress dialog continuously displays animation indicating that an operation
	 * of unknown length is occurring. By default, this property is <code>false</code>.
	 *
	 * @param indeterminate  <code>true</code> if the progress dialog should change to indeterminate
	 *                       mode; <code>false</code> if it should revert to normal
	 *                       
	 * @see #isIndeterminate()
	 * @see javax.swing.JProgressBar#setIndeterminate(boolean)
	 */
	public void setIndeterminate(boolean indeterminate) {
		progressBar.setIndeterminate(indeterminate);
	}
	
	/**
	 * Sets the progress dialog's status string.
	 *
	 * @param text  the new status string
	 */
	public void setStatus(String text) {
		statusLabel.setText(text);
	}
	
	/**
	 * Signals that the progress has been completed. It sets the progress value to maximum.
	 * The dialog button closes the dialog instead of canceling the operation.
	 */
	public void setDone() {
		progressBar.setValue(getMaximum());
		cancelButton.setText(UIManager.getString("OptionPane.okButtonText"));
		done = true;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
}
