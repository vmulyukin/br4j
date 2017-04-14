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
package org.aplana.br4j.dynamicaccess;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.borland.jbcl.layout.XYLayout;
import java.awt.Component;

public class DialogBase extends JDialog
{
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel panelInputControls = new JPanel();
    JPanel jPanel2 = new JPanel();
    JButton btOk = new JButton();
    JButton btCancal = new JButton();
    XYLayout xYLayout1 = new XYLayout();

    public DialogBase(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
            addEscapeListener(this);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void setAutoSize()
    {
        int maxX = 0;
        int maxY = 0;
        for (int i = 0; i < panelInputControls.getComponents().length; i++) {
            Component component = panelInputControls.getComponent(i);
            if (maxX < component.getX() + component.getWidth()) {
                maxX = component.getX() + component.getWidth();
            }
            if (maxY < component.getY() + component.getHeight()) {
                maxY = component.getY() + component.getHeight();
            }
        }
        this.setSize(maxX + 10, maxY + panelInputControls.getHeight() + 5);

        double x = ((getOwner().getLocation().getX() + getOwner().getWidth()) / 2) - (getWidth() / 2);
        double y = ((getOwner().getLocation().getY() + getOwner().getHeight()) / 2) - (getHeight() / 2);
        setLocation((new Double(x)).intValue(), (new Double(y)).intValue());

    }

    private void jbInit() throws Exception
    {
        panel1.setLayout(borderLayout1);
        btOk.setPreferredSize(new Dimension(87, 25));
        btOk.setText("OK");
        btOk.addActionListener(new DialogBase_btOk_actionAdapter(this));
        btCancal.setText("��������");
        btCancal.addActionListener(new DialogBase_btCancel_actionAdapter(this));
        panelInputControls.setLayout(xYLayout1);
        xYLayout1.setWidth(100);
        xYLayout1.setHeight(100);
        panel1.setMinimumSize(new Dimension(0, 0));
        panel1.setPreferredSize(new Dimension(200, 100));
        getContentPane().add(panel1);
        panel1.add(panelInputControls, java.awt.BorderLayout.CENTER);
        panel1.add(jPanel2, java.awt.BorderLayout.SOUTH);
        jPanel2.add(btOk);
        jPanel2.add(btCancal);
    }

    public void btOk_actionPerformed(ActionEvent e)
    {
        if (onOk()) {
            hide();
        }
    }

    public boolean onOk()
    {
        return false;
    }

    public void btCancel_actionPerformed(ActionEvent e)
    {
        hide();
    }
    
    public static void addEscapeListener(final JDialog dialog) {
        ActionListener escListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        };

		dialog.getRootPane().registerKeyboardAction(escListener,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

    }

}


class DialogBase_btCancel_actionAdapter implements ActionListener
{
    private DialogBase adaptee;
    DialogBase_btCancel_actionAdapter(DialogBase adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.btCancel_actionPerformed(e);
    }
}


class DialogBase_btOk_actionAdapter implements ActionListener
{
    private DialogBase adaptee;
    DialogBase_btOk_actionAdapter(DialogBase adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.btOk_actionPerformed(e);
    }
}
