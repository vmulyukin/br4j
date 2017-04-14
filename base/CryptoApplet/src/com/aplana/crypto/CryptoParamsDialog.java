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
package com.aplana.crypto;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

public class CryptoParamsDialog extends JDialog implements ActionListener {
	 JLabel label1 = new JLabel("���������");	 
	 JLabel label2 = new JLabel("������");		
	 JTextField pathField = new JTextField(10);
	 JPasswordField passField = new JPasswordField(10);
	 JFrame mainFrame = null;
	 String _password = "";
	 String _path = "";
	 boolean cancelled = true;
	 final String m_defaultPath;
	 private boolean hidePasswordField;
	 private boolean hideContainerField;
	 
	public CryptoParamsDialog(JFrame parent, String title, String defaultPath, boolean hidePasswordField, boolean hideContainerField) {
	    super(parent, title, true);	     
	    this.setModal(true);
	    mainFrame = parent;
	    this.m_defaultPath = defaultPath;
	    this.hidePasswordField = hidePasswordField;
	    this.hideContainerField = hideContainerField;
	    
	    JPanel dlgPane = new JPanel();
	    JPanel dlgPane2 = new JPanel();
	    JPanel mainPane = new JPanel();
	    
	    dlgPane.setLayout((new GridLayout(2, 2)));	    
	    
	    if (!hideContainerField){
	    	dlgPane.add(label1);
	    	dlgPane.add(pathField);
	    }
	    
	    if (!hidePasswordField){
		    dlgPane.add(label2);
		    dlgPane.add(passField);
	    }
	    
	    pathField.setText(defaultPath);
	    pathField.setActionCommand("path");
	    
	    pathField.addMouseListener(new MouseListener() {
	        public void mouseClicked(MouseEvent e) {
	        	pathField.setBackground(new Color(255,255,255));
	        	pathField.setText(m_defaultPath);
	         }	    	
	    	public void mouseEntered(MouseEvent e){ }
	    	public void mouseExited(MouseEvent e){ }
	    	public void mouseReleased(MouseEvent e){ }
	    	public void mousePressed(MouseEvent e){ }
	    }
	    );
	    	    	    
	    JButton button = new JButton("OK");
	    button.setActionCommand("OK");
	    button.addActionListener(this);
	    dlgPane2.add(button);
	    
	    mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        mainPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	    
	    getContentPane().add(mainPane);
	    mainPane.add(dlgPane);  
	    mainPane.add(dlgPane2);  	 
	   	   
	    setTitle(title);
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    setContentPane(mainPane);
	    setSize(400, 200);
	    setLocation(200, 200);
	    pack();   
	    setVisible(true);	    
	  }
	  public void actionPerformed(ActionEvent e) {
		  final String command = e.getActionCommand();

		  if(command.equals("OK")){
			  if(pathField.getText().equals("") && !hideContainerField){
				  pathField.setBackground(new Color(255,0,0));
			  }else{		
				  this.cancelled = false;
				  setVisible(false);
				  dispose();
			  }
		  }
	  }
		  
	  public String getPath(){
		  return pathField.getText();
	  }
	  public char[] getPassword(){
		  return passField.getPassword();
	  }
	  public boolean isCancelled(){
		  return cancelled;
	  }
}
