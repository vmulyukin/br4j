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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class SecondaryDatabasesDialog extends DialogBase {

	JTable table;

	private static int COLUMN_ENABLE = 0;
	private static int COLUMN_NAME = 1;
	private static int COLUMN_URL = 2;
	private static int COLUMN_USER = 3;
	private static int COLUMN_PASSWORD = 4;

	Map<String, Map<String, Object>> secondaryDbSettings;

	public SecondaryDatabasesDialog(Frame owner, String title, boolean modal,
			Map<String, Map<String, Object>> secondaryDbSettings) {
		super(owner, title, modal);
		this.secondaryDbSettings = secondaryDbSettings;
		SecondaryDbTableModel model = new SecondaryDbTableModel(
				secondaryDbSettings);
		table = new JTable(model);
		model.addTableModelListener(new HeaderCheckBoxHandler(table));
		TableCellRenderer r = new HeaderRenderer(table.getTableHeader(), 0);
		table.getColumnModel().getColumn(0).setHeaderRenderer(r);
		table.setFillsViewportHeight(true);
		initColumnSizes(table);
		model.fireTableDataChanged();
		JScrollPane scrollPane = new JScrollPane(table);
		BorderLayout tableLayout = new BorderLayout();
		panelInputControls.setLayout(tableLayout);
		panelInputControls.add(scrollPane);

	}

	public void setAutoSize() {
		this.setMinimumSize(new Dimension(700, 200));

		double x = getOwner().getLocation().getX() + getOwner().getWidth() / 2
				- getWidth() / 2;
		double y = getOwner().getLocation().getY() + getOwner().getHeight() / 2
				- getHeight() / 2;
		setLocation((new Double(x)).intValue(), (new Double(y)).intValue());

	}

	private void initColumnSizes(JTable table) {
		table.getColumnModel().getColumn(0).setPreferredWidth(10);
		table.getColumnModel().getColumn(1).setPreferredWidth(80);
		table.getColumnModel().getColumn(2).setPreferredWidth(300);
	}

	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
	}

	@Override
	public boolean onOk() {
		int rowCount = table.getModel().getRowCount();
		int columnCount = table.getModel().getColumnCount();
		Map<String, Map<String, Object>> secondaryDbSettings = new HashMap<String, Map<String, Object>>();
		for (int i = 0; i < rowCount; i++) {
			if (table.getValueAt(i, COLUMN_NAME) == null
					|| table.getValueAt(i, COLUMN_NAME).toString().isEmpty()) {
				showErrorMessage("��� �� � ������ " + (i + 1)
						+ " �� �������� ������� \"��������\"",
						"������ � �������");
				return false;
			}
			if ((Boolean) table.getValueAt(i, COLUMN_ENABLE)) {
				try {
					JdbcTemplate jdbcTemplate = new JdbcTemplate(
							ConnectionFactory.getDataSource((String) table
									.getValueAt(i, COLUMN_URL), (String) table
									.getValueAt(i, COLUMN_USER), (String) table
									.getValueAt(i, COLUMN_PASSWORD)));
					jdbcTemplate.execute("select 1");
				} catch (Exception e) {
					showErrorMessage(
							"��������� ��� �� \""
									+ table.getValueAt(i, COLUMN_NAME)
											.toString()
									+ "\" �� �����, ��� �� ����������",
							"������");
					return false;
				}
			}
			Map<String, Object> dbSetting = new HashMap<String, Object>();
			dbSetting.put(EditConfigMainForm.KEY_ENABLE,
					table.getValueAt(i, COLUMN_ENABLE));
			dbSetting.put(EditConfigMainForm.KEY_URL,
					table.getValueAt(i, COLUMN_URL));
			dbSetting.put(EditConfigMainForm.KEY_USER,
					table.getValueAt(i, COLUMN_USER));
			dbSetting.put(EditConfigMainForm.KEY_PASSWORD,
					table.getValueAt(i, COLUMN_PASSWORD));

			secondaryDbSettings.put(
					table.getValueAt(i, COLUMN_NAME).toString(), dbSetting);
		}
		this.secondaryDbSettings.clear();
		this.secondaryDbSettings.putAll(secondaryDbSettings);
		return true;
	}

	private void showErrorMessage(String message, String title) {
		JOptionPane.showConfirmDialog(this, message, title,
				JOptionPane.CLOSED_OPTION, JOptionPane.WARNING_MESSAGE);
	}

	class SecondaryDbTableModel extends AbstractTableModel {

		private String[] columnNames = { "", "�������� ��", "URL",
				"������������", "������" };
		private Object[][] data;

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public SecondaryDbTableModel(
				Map<String, Map<String, Object>> secondaryDbSettings) {
			Iterator<String> itr = secondaryDbSettings.keySet().iterator();
			data = new Object[secondaryDbSettings.size()][];
			int i = 0;
			while (itr.hasNext()) {
				String title = itr.next();
				Map<String, Object> dbSetting = secondaryDbSettings.get(title);
				List<Object> arrayList = new ArrayList<Object>();
				arrayList.add(dbSetting.get(EditConfigMainForm.KEY_ENABLE));
				arrayList.add(title);
				arrayList.add(dbSetting.get(EditConfigMainForm.KEY_URL));
				arrayList.add(dbSetting.get(EditConfigMainForm.KEY_USER));
				arrayList.add(dbSetting.get(EditConfigMainForm.KEY_PASSWORD));
				data[i] = arrayList.toArray();
				i++;
			}
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

	}

	enum Status {
		SELECTED, DESELECTED, INDETERMINATE
	}

	class CheckBoxIcon implements Icon {
		private final JCheckBox check;

		public CheckBoxIcon(JCheckBox check) {
			this.check = check;
		}

		@Override
		public int getIconWidth() {
			return check.getPreferredSize().width;
		}

		@Override
		public int getIconHeight() {
			return check.getPreferredSize().height;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			SwingUtilities.paintComponent(g, check, (Container) c, x, y,
					getIconWidth(), getIconHeight());
		}
	}

	class HeaderCheckBoxHandler implements TableModelListener {
		private final JTable table;

		public HeaderCheckBoxHandler(JTable table) {
			this.table = table;
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			if (e.getType() == TableModelEvent.UPDATE) {
				int mci = 0;
				int vci = table.convertColumnIndexToView(mci);
				TableColumn column = table.getColumnModel().getColumn(vci);
				int selected = 0, deselected = 0;
				TableModel m = table.getModel();
				for (int i = 0; i < m.getRowCount(); i++) {
					if (Boolean.TRUE.equals(m.getValueAt(i, mci))) {
						selected++;
					} else {
						deselected++;
					}
				}
				if (selected == 0) {
					column.setHeaderValue(Status.DESELECTED);
				} else if (deselected == 0) {
					column.setHeaderValue(Status.SELECTED);
				} else {
					column.setHeaderValue(Status.INDETERMINATE);;
				}
				table.getTableHeader().repaint();
			}
		}
	}

	class HeaderRenderer extends JCheckBox implements TableCellRenderer {
		public HeaderRenderer(JTableHeader header, final int targetColumnIndex) {
			super((String) null);
			setOpaque(false);
			setFont(header.getFont());
			header.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					JTableHeader header = (JTableHeader) e.getSource();
					JTable table = header.getTable();
					TableColumnModel columnModel = table.getColumnModel();
					int vci = columnModel.getColumnIndexAtX(e.getX());
					int mci = table.convertColumnIndexToModel(vci);
					if (mci == targetColumnIndex) {
						TableColumn column = columnModel.getColumn(vci);
						Object v = column.getHeaderValue();
						boolean b = Status.DESELECTED.equals(v) ? true : false;
						TableModel m = table.getModel();
						for (int i = 0; i < m.getRowCount(); i++)
							m.setValueAt(b, i, mci);
						column.setHeaderValue(b ? Status.SELECTED
								: Status.DESELECTED);
					}
				}
			});
		}

		@Override
		public Component getTableCellRendererComponent(JTable tbl, Object val,
				boolean isS, boolean hasF, int row, int col) {
			if (val instanceof Status) {
				switch ((Status) val) {
				case SELECTED:
					setSelected(true);
					setEnabled(true);
					break;
				case DESELECTED:
					setSelected(false);
					setEnabled(true);
					break;
				case INDETERMINATE:
					setSelected(true);
					setEnabled(false);
					break;
				}
			} else {
				setSelected(true);
				setEnabled(false);
			}
			TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
			JLabel l = (JLabel) r.getTableCellRendererComponent(tbl, null, isS,
					hasF, row, col);

			l.setIcon(new CheckBoxIcon(this));
			l.setText(null);

			l.setHorizontalAlignment(SwingConstants.CENTER);
			return l;
		}
	}
}
