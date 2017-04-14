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
/*
 ������� ����� ���������� �� ���������������� � ����������� ���� �������
 */

package org.aplana.br4j.dynamicaccess;

import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.aplana.br4j.dynamicaccess.config.ConfigurationFacade;

import java.awt.Dimension;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2011</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DynamicAccess
{
    boolean packFrame = false;
    private static String file;

    /**
     * Construct and show the application.
     */
    public DynamicAccess(String file)
    {
        EditConfigMainForm frame = new EditConfigMainForm(file);
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack();
        }
        else {
            frame.validate();
        }

        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
        
         try {
        	ConfigurationFacade conf = ConfigurationFacade.getInstance();
        	conf.loadAppVersion();
        	String appVersion = conf.getAppVersion();
        	if(appVersion != null && !appVersion.equals(""))
        		frame.updateAppVersion(appVersion);
        } catch(Exception e) {
        	e.printStackTrace();
        	JOptionPane.showMessageDialog(frame, "�� ������� ���������� ������� ������ �������", "��������!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args)
    {
        //� ����� args[0] ������ ��������� �������� - ������ �� �������� ������������ �������
        //c:\Working\Rule_Editor\DynamicAccessRule\AccessConfig.xml
        //file = args[0]; //������ �������� �������� � ����� � �����

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try {
                    UIManager.setLookAndFeel(UIManager.
                            getSystemLookAndFeelClassName());
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }

                new DynamicAccess(file);
            }
        });
    }
}
