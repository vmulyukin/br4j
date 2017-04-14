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
package com.aplana.arm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

public class GRControls extends JPanel implements ItemListener, ActionListener,
		MouseListener {
	private static final long serialVersionUID = -7091233044846376235L;
	private GRDrawingPanel target;
	private GraphicsResolution parent;
	public JPanel custom_panel;

	private Map<String, JButton> buttons;
	private Map<String, JPopupMenu> popupmenus;

	public JButton active_button;

	ImageIcon icon2;
	ImageIcon icon2_;
	ImageIcon icon2__;
	ImageIcon icon2_mouse;
	ImageIcon icon2_mouse_;
	ImageIcon icon2_mouse__;
	ImageIcon icon2_active;
	ImageIcon icon2_active_;
	ImageIcon icon2_active__;

	public GRControls(GRDrawingPanel target, GraphicsResolution applet) {
		try {
			this.target = target;
			this.parent = applet;
			buttons = new HashMap<String, JButton>();
			popupmenus = new HashMap<String, JPopupMenu>();

			// setBackground(Color.lightGray);
			setBackground(Color.white);
			target.setForeground(Color.blue);
			// panels
			JPanel colors_panel = new JPanel();
			colors_panel.setBackground(Color.white);
			addColorsButtons(colors_panel);

			JPanel brush_panel = new JPanel();
			brush_panel.setBackground(Color.white);
			addBrushsizeButtons(brush_panel);

			custom_panel = new JPanel();
			custom_panel.setBackground(Color.white);
			addCustomButtons(custom_panel);
			custom_panel.setVisible(true);

			JPanel actions_panel = new JPanel();
			actions_panel.setBackground(Color.white);
			addActionsButtons(actions_panel);

			// box
			Box box = Box.createVerticalBox();
			box.add(colors_panel);
			box.add(Box.createVerticalStrut(20));
			box.add(brush_panel);
			box.add(Box.createVerticalStrut(20));
			box.add(custom_panel);
			box.add(Box.createVerticalStrut(20));

			box.add(actions_panel);

			add(box);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void actionSaveSend(ActionEvent e) {
		int opt = JOptionPane.showConfirmDialog(this.getRootPane(),
				"��������� ����������� ���������?", "����-��������",
				JOptionPane.YES_NO_OPTION);
		if (opt != JOptionPane.YES_OPTION)
			return;

		Iterator it = buttons.values().iterator();
		while (it.hasNext()) {
			JButton b = (JButton) it.next();
			b.setEnabled(false);
		}

		target.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		target.removeMouseListener(target);
		target.removeMouseMotionListener(target);

		try {
			this.parent.save();
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane
					.showMessageDialog(
							this.getRootPane(),
							"�������� ������ ��� ����������. ���������� ������ ����� � ���� Tools->Show Java Debug Console.");
		}

	}

	private void actionSave(ActionEvent e) {
		getButton("undo").setEnabled(false);
		getButton("redo").setEnabled(false);
		System.out.println("saving...");
		try {
			// this.parent.save();
			this.parent.post();
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane
					.showMessageDialog(
							this.getRootPane(),
							"�������� ������ ��� ����������. ���������� ������ ����� � ���� Tools->Show Java Debug Console.");
		}
	}

	private void setCustomOptions(String key) {
		getButton("sign").setSelected(key.equals("sign"));
		getButton("exmark").setSelected(
				key.equals("exmark") || key.equals("qumark")
						|| key.equals("checkmark"));
		getButton("custom").setSelected(key.equals("custom"));

		target.setDrawSign(key.equals("sign"));
		target.setExMarkSign(key.equals("exmark"));
		target.setCheckMarkSign(key.equals("checkmark"));
		target.setQuMarkSign(key.equals("qumark"));
		target.setCustomSign(key.equals("custom"));

		target.setCursor(parent.custom);
	}

	private void actionSign(ActionEvent e) {
		if (parent.getSignData() != null) {
			setCustomOptions("sign");
		} else {
			JOptionPane
					.showMessageDialog(
							this.getRootPane(),
							"������� �� ������. ��� ����� ������� � ������������ ���������� �� ���, �������� '�������'.");
		}
	}

	private void actionExMark(ActionEvent e) {
		setCustomOptions("exmark");
	}

	private void actionCheckMark(ActionEvent e) {
		setCustomOptions("checkmark");
	}

	private void actionQuMark(ActionEvent e) {
		setCustomOptions("qumark");
	}

	private void actionCustom(ActionEvent e) {
		if (parent.getSelectedCustomData() != null) {
			setCustomOptions("custom");
		}
	}

	private void actionClear(ActionEvent e) {
		String message;
		if (parent.isGraphicsMessage()) {
			message = "������� ��� �������, ��������� � ���������?";
		} else {
			message = "������� ��� �������, ��������� �� ���������?";
		}
		int opt = JOptionPane.showConfirmDialog(this.getRootPane(), message,
				"����-��������", JOptionPane.YES_NO_OPTION);
		if (opt == JOptionPane.YES_OPTION) {
			getButton("redo").setEnabled(false);
			getButton("undo").setEnabled(false);
			parent.clear();
		}
	}

	private void actionUndo(ActionEvent e) {
		parent.undo();
		getButton("redo").setEnabled(true);
		if (parent.isDataEmpty()) {
			((JButton) e.getSource()).setEnabled(false);
		}

	}

	private void actionRedo(ActionEvent e) {
		parent.redo();
		getButton("undo").setEnabled(true);
		if (parent.isUndoEmpty()) {
			((JButton) e.getSource()).setEnabled(false);
		}
	}

	private void actionPopupMenu(ActionEvent e) {
		JMenuItem item = (JMenuItem) e.getSource();
		JPopupMenu menu = (JPopupMenu) item.getParent();
		JButton b = (JButton) menu.getInvoker();
		// System.out.println("action : "+b.getActionCommand());
		parent.imgData.setSelectedColorOrSize(b.getActionCommand());

		if (b.getActionCommand().equals("1px")
				| b.getActionCommand().equals("2px")
				| b.getActionCommand().equals("4px")) {
			getPMenu("1px").getComponents()[0].setEnabled(true);
			getPMenu("2px").getComponents()[0].setEnabled(true);
			getPMenu("4px").getComponents()[0].setEnabled(true);
		} else {
			getPMenu("blue").getComponents()[0].setEnabled(true);
			getPMenu("black").getComponents()[0].setEnabled(true);
			getPMenu("red").getComponents()[0].setEnabled(true);
			getPMenu("green").getComponents()[0].setEnabled(true);
		}
		getMenu(b).getComponents()[0].setEnabled(false);
	}

	private void actionExMark_select(ActionEvent e) {
		JButton button_mark = getButton("exmark");
		button_mark.setActionCommand("exmark");
		button_mark.setIcon(icon2);
		button_mark.setSelectedIcon(icon2_active);
		button_mark.setRolloverSelectedIcon(icon2_active);
		button_mark.setRolloverIcon(icon2_mouse);
		button_mark.setToolTipText("��������������� ����");
		actionExMark(null);
	}

	private void actionQuMark_select(ActionEvent e) {
		JButton button_mark = getButton("exmark");
		button_mark.setActionCommand("qumark");
		button_mark.setIcon(icon2_);
		button_mark.setSelectedIcon(icon2_active_);
		button_mark.setRolloverSelectedIcon(icon2_active_);
		button_mark.setRolloverIcon(icon2_mouse_);
		button_mark.setToolTipText("�������������� ����");
		actionQuMark(null);
	}

	private void actionCheckMark_select(ActionEvent e) {
		JButton button_mark = getButton("exmark");
		button_mark.setActionCommand("checkmark");
		button_mark.setIcon(icon2__);
		button_mark.setSelectedIcon(icon2_active__);
		button_mark.setRolloverSelectedIcon(icon2_active__);
		button_mark.setRolloverIcon(icon2_mouse__);
		button_mark.setToolTipText("�������");
		actionCheckMark(null);
	}

	private void setSelectedCustom(String tname) {
		Iterator undoIterator = parent.getCustomData().iterator();
		while (undoIterator.hasNext()) {
			UndoData undo = (UndoData) undoIterator.next();
			if (undo.name.equals(tname)) {
				// parent.setEnviroment("BRARM_SelectedCustomData", tname);
				// TODO
				parent.setSelectedCustomData(undo);
				getButton("custom").setToolTipText(tname);
				actionCustom(null);
			}
		}

	}

	private void actionCustomMenu(ActionEvent e) {
		JMenuItem item = (JMenuItem) e.getSource();
		setSelectedCustom(item.getText());
	}

	private void actionCustomChooseShape(ActionEvent e) {
		List<String> names = new ArrayList<String>();
		Iterator undoIterator = parent.getCustomData().iterator();
		while (undoIterator.hasNext()) {
			UndoData undo = (UndoData) undoIterator.next();
			names.add(undo.name);
		}
		Object[] arr = names.toArray();
		String s = (String) JOptionPane.showInputDialog(getButton("custom")
				.getRootPane(), "�������� ����� ��� ���������:",
				"����-��������", JOptionPane.QUESTION_MESSAGE, null, arr,
				arr[0]);
		if ((s != null) && (s.length() > 0)) {
			setSelectedCustom(s);
		}
	}

	private void setColorOptions(String key) {
		getButton("black").setSelected(key.equals("black"));
		getButton("blue").setSelected(key.equals("blue"));
		getButton("red").setSelected(key.equals("red"));
		getButton("green").setSelected(key.equals("green"));
	}

	private void setSizeOptions(String key) {
		getButton("1px").setSelected(key.equals("1px"));
		getButton("2px").setSelected(key.equals("2px"));
		getButton("4px").setSelected(key.equals("4px"));
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String action = e.getActionCommand();
			if (action.equals("save_send")) {
				actionSaveSend(e);
			} else if (action.equals("save")) {
				actionSave(e);
			} else if (action.equals("sign")) {
				actionSign(e);
			} else if (action.equals("exmark")) {
				actionExMark(e);
			} else if (action.equals("exmark_select")) {
				actionExMark_select(e);
			} else if (action.equals("qumark")) {
				actionQuMark(e);
			} else if (action.equals("qumark_select")) {
				actionQuMark_select(e);
			} else if (action.equals("checkmark")) {
				actionCheckMark(e);
			} else if (action.equals("checkmark_select")) {
				actionCheckMark_select(e);
			} else if (action.equals("custom")) {
				actionCustom(e);
			} else if (action.equals("black")) {
				target.setForeground(Color.black);
				setColorOptions(action);
			} else if (action.equals("red")) {
				target.setForeground(Color.red);
				setColorOptions(action);
			} else if (action.equals("blue")) {
				target.setForeground(Color.blue);
				setColorOptions(action);
			} else if (action.equals("green")) {
				target.setForeground(Color.green);
				setColorOptions(action);
			} else if (action.equals("1px")) {
				parent.setBrush_size(1);
				setSizeOptions(action);
				active_button = getButton(action);
			} else if (action.equals("2px")) {
				parent.setBrush_size(2);
				setSizeOptions(action);
				active_button = getButton(action);
			} else if (action.equals("4px")) {
				parent.setBrush_size(4);
				setSizeOptions(action);
				active_button = getButton(action);
			} else if (action.equals("clear")) {
				actionClear(e);
			} else if (action.equals("undo")) {
				actionUndo(e);
			} else if (action.equals("redo")) {
				actionRedo(e);
			} else if (action.equals("popupmenu")) {
				actionPopupMenu(e);
			} else if (action.equals("custom_rightmenu")) {
				actionCustomMenu(e);
			} else if (action.equals("custom_allshapes")) {
				actionCustomChooseShape(e);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void activatePanel() {
		getButton("save").setEnabled(true);
		custom_panel.setVisible(parent.showCustom);

		if (parent.isGraphicsMessage()) {
			getButton("save").setText("���������");
			getButton("save").setActionCommand("save_send");
			getButton("save").setToolTipText("��������� ����������� ���������");
		}

		getButton("clear").setEnabled(true);
		getButton("sign").setEnabled(true);
		getButton("exmark").setEnabled(true);
		getButton("custom").setEnabled(true);

		if (!parent.showCustom) {
			getButton("1px").setSelected(true);
			getButton("black").setSelected(true);
			target.setForeground(Color.black);
			parent.setBrush_size(1);

			active_button = getButton("1px");

		} else {
			getButton("1px").setEnabled(true);
			getButton("2px").setEnabled(true);
			getButton("4px").setEnabled(true);
			getButton("black").setEnabled(true);
			getButton("blue").setEnabled(true);
			getButton("red").setEnabled(true);
			getButton("green").setEnabled(true);

			addPopupMenu("1px");
			addPopupMenu("2px");
			addPopupMenu("4px");
			addPopupMenu("black");
			addPopupMenu("blue");
			addPopupMenu("red");
			addPopupMenu("green");

			JButton button1 = getButton(parent.selectedColorButton);
			button1.setSelected(true);
			getMenu(button1).getComponents()[0].setEnabled(false);

			button1 = getButton(parent.selectedSizeButton);
			button1.setSelected(true);
			getMenu(button1).getComponents()[0].setEnabled(false);
			active_button = button1;

			// System.out.println(parent.selectedColorButton);
			// System.out.println(parent.selectedSizeButton);
			if (parent.selectedColorButton.equals("blue")) {
				target.setForeground(Color.blue);
			} else if (parent.selectedColorButton.equals("black")) {
				target.setForeground(Color.black);
			} else if (parent.selectedColorButton.equals("red")) {
				target.setForeground(Color.red);
			} else if (parent.selectedColorButton.equals("green")) {
				target.setForeground(Color.green);
			}

			if (parent.selectedSizeButton.equals("1px")) {
				parent.setBrush_size(1);
			} else if (parent.selectedSizeButton.equals("2px")) {
				parent.setBrush_size(2);
			} else if (parent.selectedSizeButton.equals("4px")) {
				parent.setBrush_size(4);
			}
		}

	}

	private void addActionsButtons(JPanel panel) {
		try {
			panel.setLayout(new GridLayout(5, 1));
			JLabel title = new JLabel();
			title.setText("��������");
			title.setFont(new Font("Arial", Font.PLAIN, 14));
			title.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(title);

			ImageIcon saveIcon = parent.getIcon("save.gif");
			ImageIcon undoIcon = parent.getIcon("otmena.gif");
			ImageIcon redoIcon = parent.getIcon("vozvrat.gif");
			ImageIcon clearIcon = parent.getIcon("delete.gif");

			if (parent.isGraphicsMessage()) {
				createActionButton(panel, saveIcon, "���������", "save_send",
						"��������� ����������� ���������");
			} else {
				createActionButton(panel, saveIcon, "���������", "save",
						"��������� ����������� ���������");
			}
			createActionButton(panel, undoIcon, "������", "undo",
					"�������� ��������� ��������");
			createActionButton(panel, redoIcon, "�������", "redo",
					"��������� ���������� ��������");
			createActionButton(panel, clearIcon, "�������", "clear",
					"������� ��������� ������ � �������� � ��������������� �����������");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addBrushsizeButtons(JPanel panel) {
		try {
			panel.setLayout(new BorderLayout());
			JLabel title = new JLabel();
			title.setText("������� ����");
			title.setFont(new Font("Arial", Font.PLAIN, 14));
			title.setHorizontalAlignment(SwingConstants.CENTER);
			title.setBorder(new MatteBorder(10, 0, 10, 0, Color.white));
			panel.add(title, BorderLayout.NORTH);

			JPanel panel2 = new JPanel();
			panel2.setLayout(new GridLayout(1, 3));
			panel2.setBackground(Color.WHITE);

			ImageIcon icon1 = parent.getIcon("icon_1px.gif");
			ImageIcon icon2 = parent.getIcon("icon_2px.gif");
			ImageIcon icon4 = parent.getIcon("icon_4px.gif");
			ImageIcon icon1_mouse = parent.getIcon("icon_1px_mouse.gif");
			ImageIcon icon2_mouse = parent.getIcon("icon_2px_mouse.gif");
			ImageIcon icon4_mouse = parent.getIcon("icon_4px_mouse.gif");
			ImageIcon icon1_active = parent.getIcon("icon_1px_active.gif");
			ImageIcon icon2_active = parent.getIcon("icon_2px_active.gif");
			ImageIcon icon4_active = parent.getIcon("icon_4px_active.gif");

			createSizeButton(panel2, "1px", icon1, icon1_mouse, icon1_active,
					"������ ����� 1 �������");
			createSizeButton(panel2, "2px", icon2, icon2_mouse, icon2_active,
					"������ ����� 2 �������");
			createSizeButton(panel2, "4px", icon4, icon4_mouse, icon4_active,
					"������ ����� 4 �������");

			panel.add(panel2, BorderLayout.SOUTH);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addCustomButtons(JPanel panel) {
		try {
			panel.setLayout(new BorderLayout());
			JLabel title = new JLabel();
			title.setText("������");
			title.setFont(new Font("Arial", Font.PLAIN, 14));
			title.setHorizontalAlignment(SwingConstants.CENTER);
			title.setBorder(new MatteBorder(10, 0, 10, 0, Color.white));
			panel.add(title, BorderLayout.NORTH);

			JPanel panel2 = new JPanel();
			panel2.setLayout(new GridLayout(1, 3));
			panel2.setBackground(Color.WHITE);

			ImageIcon icon1 = parent.getIcon("icon_sign.gif");
			icon2 = parent.getIcon("icon_exmark.gif");
			icon2_ = parent.getIcon("icon_qumark.gif");
			icon2__ = parent.getIcon("icon_check.gif");
			ImageIcon icon4 = parent.getIcon("icon_custom.gif");
			ImageIcon icon1_mouse = parent.getIcon("icon_sign_mouse.gif");
			icon2_mouse = parent.getIcon("icon_exmark_mouse.gif");
			icon2_mouse_ = parent.getIcon("icon_qumark_mouse.gif");
			icon2_mouse__ = parent.getIcon("icon_check_mouse.gif");
			ImageIcon icon4_mouse = parent.getIcon("icon_custom_mouse.gif");
			ImageIcon icon1_active = parent.getIcon("icon_sign_active.gif");
			icon2_active = parent.getIcon("icon_exmark_active.gif");
			icon2_active_ = parent.getIcon("icon_qumark_active.gif");
			icon2_active__ = parent.getIcon("icon_check_active.gif");
			ImageIcon icon4_active = parent.getIcon("icon_custom_active.gif");

			createSizeButton(panel2, "sign", icon1, icon1_mouse, icon1_active,
					"������ �������");

			JButton button_custom = createSizeButton(panel2, "custom", icon4,
					icon4_mouse, icon4_active, "����������� ����� �� ������");
			button_custom.addMouseListener(this);

			JButton button_mark = createSizeButton(panel2, "exmark", icon2,
					icon2_mouse, icon2_active, "��������������� ����");
			JPopupMenu menu = new JPopupMenu();
			popupmenus.put("exmark", menu);
			JMenuItem item = new JMenuItem("��������������� ����");
			item.setActionCommand("exmark_select");
			menu.add(item);
			item.addActionListener(this);
			item = new JMenuItem("�������������� ����");
			item.setActionCommand("qumark_select");
			menu.add(item);
			item.addActionListener(this);
			item = new JMenuItem("�������");
			item.setActionCommand("checkmark_select");
			menu.add(item);
			item.addActionListener(this);
			button_mark.addMouseListener(this);

			panel.add(panel2, BorderLayout.SOUTH);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void makeCustomMenu() {
		JPopupMenu menu_custom = getPMenu("custom");
		if (menu_custom != null)
			return;

		menu_custom = new JPopupMenu();
		popupmenus.put("custom", menu_custom);

		List<String> names = new ArrayList<String>();
		Iterator undoIterator = parent.getCustomData().iterator();
		int i = 0;
		while (undoIterator.hasNext() && i < 10) {
			i = i + 1;
			UndoData undo = (UndoData) undoIterator.next();

			JMenuItem item = new JMenuItem(undo.name);
			item.setActionCommand("custom_rightmenu");
			menu_custom.add(item);
			item.addActionListener(this);
			names.add(undo.name);
		}
		if (i >= 10 && undoIterator.hasNext()) {
			menu_custom.add(new JSeparator());

			JMenuItem item = new JMenuItem("��� ������...");
			item.setActionCommand("custom_allshapes");
			menu_custom.add(item);
			item.addActionListener(this);
		}

		if (names.isEmpty()) {
			JOptionPane
					.showMessageDialog(
							parent.getRootPane(),
							"� ���������� ��� �� ������ ������ ��� ������� ���������. �������� ����� ����� � ������������ ���������� �� ���, �������� '������� ���������'.");
			popupmenus.remove("custom");
			return;
		}
		getButton("custom").addMouseListener(this);
	}

	private void addColorsButtons(JPanel panel) {
		try {
			panel.setBorder(new MatteBorder(0, 12, 5, 12, Color.white));
			panel.setLayout(new BorderLayout());

			JLabel title = new JLabel();
			title.setText("���� ����");
			title.setFont(new Font("Arial", Font.PLAIN, 14));
			title.setHorizontalAlignment(SwingConstants.CENTER);
			title.setBorder(new MatteBorder(10, 0, 10, 0, Color.white));
			panel.add(title, BorderLayout.NORTH);

			ImageIcon icon_black = parent.getIcon("black.gif");
			ImageIcon icon_blue = parent.getIcon("blue.gif");
			ImageIcon icon_green = parent.getIcon("green.gif");
			ImageIcon icon_red = parent.getIcon("red.gif");
			ImageIcon icon_black_mouse = parent.getIcon("black_mouse.gif");
			ImageIcon icon_blue_mouse = parent.getIcon("blue_mouse.gif");
			ImageIcon icon_green_mouse = parent.getIcon("green_mouse.gif");
			ImageIcon icon_red_mouse = parent.getIcon("red_mouse.gif");
			ImageIcon icon_black_active = parent.getIcon("black_active.gif");
			ImageIcon icon_blue_active = parent.getIcon("blue_active.gif");
			ImageIcon icon_green_active = parent.getIcon("green_active.gif");
			ImageIcon icon_red_active = parent.getIcon("red_active.gif");

			JPanel panel2 = new JPanel();
			panel2.setLayout(new GridLayout(2, 2));

			createColorButton(panel2, icon_black, icon_black_mouse,
					icon_black_active, "black", "������� ������ ����");
			createColorButton(panel2, icon_blue, icon_blue_mouse,
					icon_blue_active, "blue", "������� ����� ����");
			createColorButton(panel2, icon_red, icon_red_mouse,
					icon_red_active, "red", "������� ������� ����");
			createColorButton(panel2, icon_green, icon_green_mouse,
					icon_green_active, "green", "������� ������� ����");

			panel.add(panel2, BorderLayout.SOUTH);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void addPopupMenu(String key) {
		JPopupMenu menu = new JPopupMenu();
		popupmenus.put(key, menu);
		JMenuItem item = new JMenuItem("���������� �� ���������");
		item.setActionCommand("popupmenu");
		menu.add(item);
		item.addActionListener(this);
		getButton(key).addMouseListener(this);

	}

	protected void finalize() throws Throwable {
		// do finalization here
		super.finalize(); // not necessary if extending Object.
	}

	private JButton createActionButton(JPanel panel, ImageIcon icon,
			String txt, String action, String tooltip) {
		JButton b = new JButton();
		try {
			b.setActionCommand(action);
			b.setText(txt);
			b.setIconTextGap(8);
			b.setFont(new Font("Arial", Font.BOLD, 12));
			b.addActionListener(this);
			b.setIcon(icon);
			b.setHorizontalAlignment(JButton.LEFT);
			b.setMargin(new Insets(6, 15, 6, 15));
			b.setToolTipText(tooltip);
			b.setEnabled(false);
			panel.add(b);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		buttons.put(action, b);
		return b;
	}

	public JButton getButton(String key) {
		return buttons.get(key);
	}

	private JPopupMenu getPMenu(String key) {
		return popupmenus.get(key);
	}

	private JButton createColorButton(JPanel panel, ImageIcon icon,
			ImageIcon icon_mouse, ImageIcon icon_selected, String action,
			String tooltip) {
		JButton b = new JButton();
		try {
			b.setActionCommand(action);
			b.setBorder(new MatteBorder(3, 3, 3, 3, Color.white));
			b.addActionListener(this);
			b.setIcon(icon);
			b.setRolloverIcon(icon_mouse);
			b.setSelectedIcon(icon_selected);
			b.setRolloverSelectedIcon(icon_selected);
			b.setFocusPainted(false);

			b.setEnabled(false);
			b.setToolTipText(tooltip);
			panel.add(b);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		buttons.put(action, b);
		return b;
	}

	public JPopupMenu getMenu(JButton button) {
		// System.out.println(button.getActionCommand());
		Iterator it = buttons.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (buttons.get(key).equals(button)) {
				return getPMenu(key);
			}
		}
		return null;
	}

	private JButton createSizeButton(JPanel panel, String action,
			ImageIcon icon, ImageIcon icon_mouse, ImageIcon icon_selected,
			String tooltip) {
		JButton b = new JButton();
		try {
			b.setActionCommand(action);
			b.setBorder(new MatteBorder(3, 3, 3, 3, Color.white));
			b.addActionListener(this);
			b.setBackground(Color.white);
			b.setFocusPainted(false);

			b.setIcon(icon);
			b.setRolloverIcon(icon_mouse);
			b.setSelectedIcon(icon_selected);
			b.setRolloverSelectedIcon(icon_selected);
			b.setContentAreaFilled(false);

			b.setEnabled(false);
			b.setToolTipText(tooltip);
			panel.add(b);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		buttons.put(action, b);
		return b;
	}

	public void itemStateChanged(ItemEvent arg0) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			JButton b = (JButton) evt.getComponent();
			if (b.equals(getButton("custom"))) {
				makeCustomMenu();
			}
			JPopupMenu menu = getMenu(b);
			menu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	public void mouseReleased(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			JButton b = (JButton) evt.getComponent();
			if (b.equals(getButton("custom"))) {
				makeCustomMenu();
			}
			JPopupMenu menu = getMenu(b);
			menu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

}
