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
package com.aplana.scanner;

import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.scanner.util.MessageResolver;

/**
 * Applet to scan documents and upload them to the server.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class ScannerApplet extends JApplet {
	private static final long serialVersionUID = -6272311014501734787L;
	
	private static final Log logger = LogFactory.getLog(ScannerApplet.class);
	
	private static final String APPLET_LOADED_PARAM = "appletLoaded";
	private static final String FILENAME_PARAM = "filename";
	private static final String TARGET_URL_PARAM = "targetUrl";
	private static final String NAMESPACE_PARAM = "namespace";
	private static final String TARGET_ATTR_PARAM = "targetAttr";
	
	// JOptionPane resources
	public static final String OPTIONPANE_YES_BUTTON_TEXT_KEY = "OptionPane.yesButtonText";
	public static final String OPTIONPANE_NO_BUTTON_TEXT_KEY = "OptionPane.noButtonText";
	public static final String OPTIONPANE_OK_BUTTON_TEXT_KEY = "OptionPane.okButtonText";
	public static final String OPTIONPANE_CANCEL_BUTTON_TEXT_KEY = "OptionPane.cancelButtonText";
	
	// JFileChooser resources
	public static final String FILECHOOSER_ACCEPT_ALL_FILES_FILTER_TEXT_KEY =
		"FileChooser.acceptAllFileFilterText";
	public static final String FILECHOOSER_CANCEL_BUTTON_TEXT_KEY = "FileChooser.cancelButtonText";
	public static final String FILECHOOSER_CANCEL_BUTTON_TOOLTIP_TEXT_KEY =
		"FileChooser.cancelButtonToolTipText";
	public static final String FILECHOOSER_DETAILS_VIEW_BUTTON_TOOLTIP_TEXT_KEY =
		"FileChooser.detailsViewButtonToolTipText";
	public static final String FILECHOOSER_DIRECTORY_OPEN_BUTTON_TEXT_KEY =
		"FileChooser.directoryOpenButtonText";
	public static final String FILECHOOSER_DIRECTORY_OPEN_BUTTON_TOOLTIP_TEXT_KEY =
		"FileChooser.directoryOpenButtonToolTipText";
	public static final String FILECHOOSER_DIRECTORY_OPEN_BUTTON_MNEMONIC_KEY =
		"FileChooser.directoryOpenButtonMnemonic";
	public static final String FILECHOOSER_FILE_NAME_LABEL_TEXT_KEY = "FileChooser.fileNameLabelText";
	public static final String FILECHOOSER_FILE_NAME_LABEL_MNEMONIC_KEY =
		"FileChooser.fileNameLabelMnemonic";
	public static final String FILECHOOSER_FILES_OF_TYPE_LABEL_TEXT_KEY =
		"FileChooser.filesOfTypeLabelText";
	public static final String FILECHOOSER_FILES_OF_TYPE_LABEL_MNEMONIC_KEY =
		"FileChooser.filesOfTypeLabelMnemonic";
	public static final String FILECHOOSER_LIST_VIEW_BUTTON_TOOLTIP_TEXT_KEY =
		"FileChooser.listViewButtonToolTipText";
	public static final String FILECHOOSER_NEW_FOLDER_TOOLTIP_TEXT_KEY =
		"FileChooser.newFolderToolTipText";
	public static final String FILECHOOSER_SAVE_BUTTON_TEXT_KEY = "FileChooser.saveButtonText";
	public static final String FILECHOOSER_SAVE_BUTTON_TOOLTIP_TEXT_KEY =
		"FileChooser.saveButtonToolTipText";
	public static final String FILECHOOSER_SAVE_BUTTON_MNEMONIC_KEY =
		"FileChooser.saveButtonMnemonic";
	public static final String FILECHOOSER_SAVE_DIALOG_TITLE_TEXT_KEY =
		"FileChooser.saveDialogTitleText";
	public static final String FILECHOOSER_SAVE_IN_LABEL_TEXT_KEY = "FileChooser.saveInLabelText";
	public static final String FILECHOOSER_SAVE_IN_LABEL_MNEMONIC_KEY =
		"FileChooser.saveInLabelMnemonic";
	public static final String FILECHOOSER_UP_ONE_FOLDER_TOOLTIP_TEXT_KEY =
		"FileChooser.upFolderToolTipText";
	public static final String FILECHOOSER_VIEW_MENU_LABEL_TEXT_KEY = "FileChooser.viewMenuLabelText";
	public static final String FILECHOOSER_REFRESH_ACTION_LABEL_TEXT_KEY =
		"FileChooser.refreshActionLabelText";
	public static final String FILECHOOSER_NEW_FOLDER_ACTION_LABEL_TEXT_KEY =
		"FileChooser.newFolderActionLabelText";
	public static final String FILECHOOSER_LIST_VIEW_ACTION_LABEL_TEXT_KEY =
		"FileChooser.listViewActionLabelText";
	public static final String FILECHOOSER_DETAILS_VIEW_ACTION_LABEL_TEXT_KEY =
		"FileChooser.detailsViewActionLabelText";

	private String appletLoaded;
	private String filename;
	private String targetUrl;
	private String namespace;
	private String targetAttr;
	private ScannerController controller;
	public static final MessageResolver messageResolver =
		new MessageResolver(ScannerApplet.class.getPackage().getName() + ".resources");

	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	@Override
	public void init() {
		appletLoaded = getParameter(APPLET_LOADED_PARAM);
		filename = getParameter(FILENAME_PARAM);
		targetUrl = getParameter(TARGET_URL_PARAM);
		namespace = getParameter(NAMESPACE_PARAM);
		targetAttr = getParameter(TARGET_ATTR_PARAM);
		
		ImageIO.scanForPlugins();
		localizeSwingComponents();
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createGUI();
				}
			});
		} catch (Exception e) {
			logger.error("Failed to create GUI", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.applet.Applet#start()
	 */
	@Override
	public void start() {
		// call onAppletLoaded event handler
		if (appletLoaded != null) {
			Object[] args = { this };
			jsCall(appletLoaded, args);
		}
		logger.debug("Applet started");
	}

	/* (non-Javadoc)
	 * @see java.applet.Applet#stop()
	 */
	@Override
	public void stop() {
		if (controller != null)
			controller.stop();
	}
	
	/* (non-Javadoc)
	 * @see java.applet.Applet#destroy()
	 */
	@Override
	public void destroy() {
		if (controller != null)
			controller.deleteTmpDir();
	}

	/**
   * Convenience method to get a resource string for the default locale.
   *
   * @param  code    the code identifying the message to obtain
   * @return the resolved message
   */
	public static String getMessage(String code) {
		return messageResolver.getMessage(code);
	}
	
	/**
   * Convenience method to get a resource string.
   *
   * @param  code    the code identifying the message to obtain
   * @param  locale  the locale
   * @return the resolved message
   */
	public static String getMessage(String code, Locale locale) {
		return messageResolver.getMessage(code, locale);
	}
	
	/**
	 * Gets a cookie.
	 *
	 * @param  name  the cookie name
	 * @return the cookie value
	 */
	public String getCookie(String name) {
		String cookie = (String)jsEval("document.cookie");
		if (cookie != null) {
			String search = name + "=";
			int offset = cookie.indexOf(search);
			if (offset != -1) {
				offset += search.length();
				int end = cookie.indexOf(';', offset);
				if (end == -1) end = cookie.length();
				return cookie.substring(offset, end);
			}
		}
		logger.warn("Cookie '" + name + "' not found");
		return "";
	}

	/**
	 * Gets the filename to use when uploading documents.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Gets the URL to upload documents.
	 */
	public String getTargetUrl() {
		return targetUrl;
	}
	
	/**
	 * Gets the portlet namespace.
	 */
	public String getNamespace() {
		return namespace;
	}
	
	public String getTargetAttr(){
		return targetAttr;
	}

	private void createGUI() {
		controller = new ScannerController(this);
		setContentPane(controller.getScannerPanel());
	}
	
	/**
	 * Calls a JavaScript function.
	 * 
	 * @param  jsFunction  the name of the JavaScript function to be invoked
	 * @param  args        an array of Java object to be passed as arguments to the function
	 * @return the function result
	 * 
	 * @see netscape.javascript.JSObject#call(String, Object[])
	 */
	public Object jsCall(String jsFunction, Object... args) {
		try {
			return JSObject.getWindow(this).call(jsFunction, args);
		} catch (JSException e) {
			logger.error("Failed to call the JavaScript function " + jsFunction +
							" with arguments " + args, e);
		}
		return null;
	}
	
	/**
	 * Evaluates a JavaScript expression.
	 * 
	 * @param  jsExpression  the JavaScript expression to be evaluated
	 * @return the result of the JavaScript evaluation
	 * 
	 * @see netscape.javascript.JSObject#eval(String)
	 */
	public Object jsEval(String jsExpression) {
		try {
			return JSObject.getWindow(this).eval(jsExpression);
		} catch (JSException e) {
			logger.error("Failed to evaluate the JavaScript expression " + jsExpression, e);
		}
		return null;
	}
	
	private void localizeSwingComponents() {
		// JOptionPane localization
		UIManager.put(OPTIONPANE_YES_BUTTON_TEXT_KEY, getMessage(OPTIONPANE_YES_BUTTON_TEXT_KEY));
		UIManager.put(OPTIONPANE_NO_BUTTON_TEXT_KEY, getMessage(OPTIONPANE_NO_BUTTON_TEXT_KEY));
		UIManager.put(OPTIONPANE_OK_BUTTON_TEXT_KEY, getMessage(OPTIONPANE_OK_BUTTON_TEXT_KEY));
		UIManager.put(OPTIONPANE_CANCEL_BUTTON_TEXT_KEY, getMessage(OPTIONPANE_CANCEL_BUTTON_TEXT_KEY));
		
		// JFileChooser localization
		UIManager.put(FILECHOOSER_ACCEPT_ALL_FILES_FILTER_TEXT_KEY,
						getMessage(FILECHOOSER_ACCEPT_ALL_FILES_FILTER_TEXT_KEY));
		UIManager.put(FILECHOOSER_CANCEL_BUTTON_TEXT_KEY,
						getMessage(FILECHOOSER_CANCEL_BUTTON_TEXT_KEY));
		UIManager.put(FILECHOOSER_CANCEL_BUTTON_TOOLTIP_TEXT_KEY,
						getMessage(FILECHOOSER_CANCEL_BUTTON_TOOLTIP_TEXT_KEY));
		UIManager.put(FILECHOOSER_DETAILS_VIEW_BUTTON_TOOLTIP_TEXT_KEY,
						getMessage(FILECHOOSER_DETAILS_VIEW_BUTTON_TOOLTIP_TEXT_KEY));
		UIManager.put(FILECHOOSER_DIRECTORY_OPEN_BUTTON_TEXT_KEY,
						getMessage(FILECHOOSER_DIRECTORY_OPEN_BUTTON_TEXT_KEY));
		UIManager.put(FILECHOOSER_DIRECTORY_OPEN_BUTTON_TOOLTIP_TEXT_KEY,
						getMessage(FILECHOOSER_DIRECTORY_OPEN_BUTTON_TOOLTIP_TEXT_KEY));
		UIManager.put(FILECHOOSER_DIRECTORY_OPEN_BUTTON_MNEMONIC_KEY,
						(int)getMessage(FILECHOOSER_DIRECTORY_OPEN_BUTTON_MNEMONIC_KEY).charAt(0));
		UIManager.put(FILECHOOSER_FILE_NAME_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_FILE_NAME_LABEL_TEXT_KEY));
		UIManager.put(FILECHOOSER_FILE_NAME_LABEL_MNEMONIC_KEY,
						(int)getMessage(FILECHOOSER_FILE_NAME_LABEL_MNEMONIC_KEY).charAt(0));
		UIManager.put(FILECHOOSER_FILES_OF_TYPE_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_FILES_OF_TYPE_LABEL_TEXT_KEY));
		UIManager.put(FILECHOOSER_FILES_OF_TYPE_LABEL_MNEMONIC_KEY,
						(int)getMessage(FILECHOOSER_FILES_OF_TYPE_LABEL_MNEMONIC_KEY).charAt(0));
		UIManager.put(FILECHOOSER_LIST_VIEW_BUTTON_TOOLTIP_TEXT_KEY,
						getMessage(FILECHOOSER_LIST_VIEW_BUTTON_TOOLTIP_TEXT_KEY));
		UIManager.put(FILECHOOSER_NEW_FOLDER_TOOLTIP_TEXT_KEY,
						getMessage(FILECHOOSER_NEW_FOLDER_TOOLTIP_TEXT_KEY));
		UIManager.put(FILECHOOSER_SAVE_BUTTON_TEXT_KEY,
						getMessage(FILECHOOSER_SAVE_BUTTON_TEXT_KEY));
		UIManager.put(FILECHOOSER_SAVE_BUTTON_TOOLTIP_TEXT_KEY,
						getMessage(FILECHOOSER_SAVE_BUTTON_TOOLTIP_TEXT_KEY));
		UIManager.put(FILECHOOSER_SAVE_BUTTON_MNEMONIC_KEY,
						(int)getMessage(FILECHOOSER_SAVE_BUTTON_MNEMONIC_KEY).charAt(0));
		UIManager.put(FILECHOOSER_SAVE_DIALOG_TITLE_TEXT_KEY,
						getMessage(FILECHOOSER_SAVE_DIALOG_TITLE_TEXT_KEY));
		UIManager.put(FILECHOOSER_SAVE_IN_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_SAVE_IN_LABEL_TEXT_KEY));
		UIManager.put(FILECHOOSER_SAVE_IN_LABEL_MNEMONIC_KEY,
						(int)getMessage(FILECHOOSER_SAVE_IN_LABEL_MNEMONIC_KEY).charAt(0));
		UIManager.put(FILECHOOSER_UP_ONE_FOLDER_TOOLTIP_TEXT_KEY,
						getMessage(FILECHOOSER_UP_ONE_FOLDER_TOOLTIP_TEXT_KEY));
		UIManager.put(FILECHOOSER_VIEW_MENU_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_VIEW_MENU_LABEL_TEXT_KEY));
		UIManager.put(FILECHOOSER_REFRESH_ACTION_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_REFRESH_ACTION_LABEL_TEXT_KEY));
		UIManager.put(FILECHOOSER_NEW_FOLDER_ACTION_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_NEW_FOLDER_ACTION_LABEL_TEXT_KEY));
		UIManager.put(FILECHOOSER_LIST_VIEW_ACTION_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_LIST_VIEW_ACTION_LABEL_TEXT_KEY));
		UIManager.put(FILECHOOSER_DETAILS_VIEW_ACTION_LABEL_TEXT_KEY,
						getMessage(FILECHOOSER_DETAILS_VIEW_ACTION_LABEL_TEXT_KEY));
	}
}
