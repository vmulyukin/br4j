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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MultiDbProgressDialog extends DialogBase{
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	JTable table; 
	MultiDbProgressModel model;
	public static ExecutorService threadPool =  Executors.newFixedThreadPool(20);
	
	
	
	private static final int COLUMN_DB_NAME = 0;
	private static final int COLUMN_PROGRESS = 1;
	private static final int COLUMN_STATUS = 2;
	private static final int COLUMN_ACTION = 3;

	public MultiDbProgressDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		this.btOk.setVisible(false);
		model = new MultiDbProgressModel();
		
		table = new JTable(model);
		table.addMouseListener(new MultiBbProgressDialog_jtableStatus_MouseAdapter(this));
        table.getColumn("��� ��������").setCellRenderer(new ProgressCellRender());
        table.getColumn("���������").setCellRenderer(new ConditionCellRender());
        table.getColumn("��������").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumn("��������").setCellRenderer(new ButtonRenderer());
        
		//table.setFillsViewportHeight(true);
		initColumnSizes(table);

		JScrollPane scrollPane = new JScrollPane(table);
		 
		BorderLayout tableLayout = new BorderLayout();
		panelInputControls.setLayout(tableLayout);
		panelInputControls.add(scrollPane);
	}
	
	

	public void executeAll() {
		for(RowData rowData : this.model.rows){
			addWorkerPropertyChangeListener(rowData.getAction().getWorker());
			threadPool.submit(rowData.getAction().getWorker());
		}
	}
	
    public void btCancel_actionPerformed(ActionEvent e)
    {
	  int result = JOptionPane.showConfirmDialog(this,
      		"�������� ���������� �������� ��� ���� ��?", "��������������!",
              JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
	  
	  if (result == JOptionPane.YES_OPTION) { 
		boolean allDone = true;
		for(RowData rowData : model.rows){
			if(rowData.getAction().getWorker() != null){  //����� Worker �������� ������, �� ���������
				rowData.getAction().getWorker().cancel(true);
				allDone = false;
			}
		}
		if(!allDone){
			JOptionPane.showMessageDialog(this, "�������� �������� ��������������", "Access Rule", JOptionPane.INFORMATION_MESSAGE);
		}
	  }
    }
    
    @Override
    public boolean onOk() {
    	setVisible(false);
    	return true;
    }
	
	private void addWorkerPropertyChangeListener(final SwingWorker<Exception, Void> worker){
		worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("progress")) {
                    Status status= new Status(Condition.GOOD, "�����������...");
                    MultiDbProgressDialog.this.model.setStatus(worker, status);
                    MultiDbProgressDialog.this.model.updateProgress(worker, (Integer) evt.getNewValue());
                }
                if ("state".equals(evt.getPropertyName())) {
                    if(worker.isCancelled()){
                    	MultiDbProgressDialog.this.model.setFinished(worker, new ButtonConfig(false, "��������"));
                    	Status status= new Status(Condition.GOOD, "��������");
                    	MultiDbProgressDialog.this.model.setStatus(worker, status);
                    	MultiDbProgressDialog.this.checkAllExecuted();
                    } else if(worker.isDone()){
						Exception exception = null;
						try {
							exception = worker.get();
							logger.error(MultiDbProgressDialog.this.model.mapWorker.get(worker), exception);
						} catch (Exception e1) {
							exception = e1;
						}
						if (exception != null) {
							Status status = new Status(Condition.ERROR,
									ExceptionUtils.getStackTrace(exception));
							MultiDbProgressDialog.this.model.setStatus(worker,
									status);
							MultiDbProgressDialog.this.model.setFinished(
									worker, new ButtonConfig(false, "������"));
						} else {
							Status status = new Status(Condition.GOOD,
									"��� ������");
							MultiDbProgressDialog.this.model.setStatus(worker,
									status);
							MultiDbProgressDialog.this.model.setFinished(
									worker,
									new ButtonConfig(false, "���������"));
						}
						MultiDbProgressDialog.this.checkAllExecuted();
                    }
                }
            }
        });
	}
	
    protected void checkAllExecuted() {
    	boolean allDone = true;
    	for(RowData rowData : model.rows){
    		if(rowData.getAction().getWorker() != null && !rowData.getAction().getWorker().isDone()){  //����� Worker �������� ������, �� ���������
    			allDone = false;
    		}
    	}
    	if(allDone){
    		this.btCancal.setVisible(false);
    		this.btOk.setVisible(true);
    	}
		
	}



	private void initColumnSizes(JTable table) {
    	table.getColumnModel().getColumn(0).setPreferredWidth(250);
    	table.getColumnModel().getColumn(1).setPreferredWidth(250);
    	table.getColumnModel().getColumn(2).setPreferredWidth(100);
    	table.getColumnModel().getColumn(3).setPreferredWidth(100);
    }
    
    public void setAutoSize()
    {
    	this.setMinimumSize(new Dimension(700, 500));

        double x = getOwner().getLocation().getX() + getOwner().getWidth() / 2 - getWidth() / 2;
        double y = getOwner().getLocation().getY() + getOwner().getHeight() / 2 - getHeight() / 2;
        setLocation((new Double(x)).intValue(), (new Double(y)).intValue());

    }
    
    
    
	private void jtableStatus_mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            if (col == 2) {
                if (row < 0 || col < 0) {
                    return;
                }
                Status status = (Status) table.getValueAt(row, col);
                if (status.getCond().equals(Condition.ERROR)) {
                    JOptionPane.showMessageDialog(this.getParent(), status.getContent(), "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        }
		
	}
	
    class MultiBbProgressDialog_jtableStatus_MouseAdapter extends MouseAdapter {

        private MultiDbProgressDialog adaptee;

        MultiBbProgressDialog_jtableStatus_MouseAdapter(MultiDbProgressDialog adaptee) {
            this.adaptee = adaptee;
        }
        
		@Override
		public void mouseClicked(MouseEvent e) {
			adaptee.jtableStatus_mouseClicked(e);
		}
    }
    
    public class ProgressCellRender extends JProgressBar implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            int progress = 0;
            if (value instanceof Float) {
                progress = Math.round(((Float) value) * 100f);
            } else if (value instanceof Integer) {
                progress = (Integer) value;
            }
            setValue(progress);
            return this;
        }
    }

    public class ConditionCellRender extends JLabel implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final Status status = (Status) value;
            if (status.getCond().equals(Condition.GOOD)) {
                setText(status.getContent());
            } else {
                setText(status.getCond().toString());
            }
            return this;
        }
    }
    
    class ButtonEditor extends DefaultCellEditor {

        protected JButton button;
        private String label;
        private boolean isPushed;
        private SwingWorker worker;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	ButtonEditor.this.stopCellEditing();
                    worker.cancel(true);
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            ButtonConfig buttonConfig = (ButtonConfig) value;
            this.worker = buttonConfig.getWorker();
            label = (value == null) ? "" : buttonConfig.getName();
            button.setText(label);
            button.setEnabled(buttonConfig.isEnable());
            if(buttonConfig.isEnable())
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                // 
                // 
                JOptionPane.showMessageDialog(button, label + ": Ouch!");
                // System.out.println(label + ": Ouch!");
            }
            isPushed = false;
            return new String(label);
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

    }
    
    class ButtonRenderer extends JButton implements TableCellRenderer {

      public ButtonRenderer() {
        setOpaque(true);
      }

      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
          setForeground(table.getSelectionForeground());
          setBackground(table.getSelectionBackground());
        } else {
          setForeground(table.getForeground());
          setBackground(UIManager.getColor("Button.background"));
        }
        ButtonConfig buttonConfig = (ButtonConfig) value;
        setText((value == null) ? "" : buttonConfig.getName());
        setEnabled(buttonConfig.isEnable());
        return this;
      }
    }
    
    private class ButtonConfig {
        private boolean enable;
        private String name;
        private SwingWorker worker;

        public ButtonConfig(boolean enable, String name) {
            this.enable = enable;
            this.name = name;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private void setWorker(SwingWorker worker) {
            this.worker = worker;
        }

        public SwingWorker getWorker() {
            return worker;
        }
        
    }
    
    private class Status {
    	Condition cond;
    	String content;
    	
		public Status(Condition cond, String content) {
			this.cond = cond;
			this.content = content;
		}

		public Condition getCond() {
			return cond;
		}

		public void setCond(Condition cond) {
			this.cond = cond;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
		
    }
    
    public class RowData {

        private String name;
        private float progress;
        private Status status = new Status(Condition.GOOD, "��������...");;
        private ButtonConfig action = new ButtonConfig(true, "Cancel");
        
        
		public RowData(String name) {
			super();
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public float getProgress() {
			return progress;
		}
		public void setProgress(float progress) {
			this.progress = progress;
		}
		public Status getStatus() {
			return status;
		}
		public void setStatus(Status status) {
			this.status = status;
		}
		public ButtonConfig getAction() {
			return action;
		}
		public void setAction(ButtonConfig action) {
			this.action = action;
		}
        
    }
    
    private enum Condition{
    	GOOD, ERROR;
    }
    
    public class MultiDbProgressModel extends AbstractTableModel {

        private List<RowData> rows;
        private Map<String, RowData> mapLookup;
        private Map<SwingWorker, RowData> mapWorker;

        public MultiDbProgressModel() {
            rows = new ArrayList<RowData>(25);
            mapLookup = new HashMap<String, RowData>(25);
            mapWorker = new HashMap<SwingWorker, RowData>();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == COLUMN_ACTION) {
                return true;
            };
            return false;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            String name = "??";
            switch (column) {
                case COLUMN_DB_NAME:
                    name = "�������� ��";
                    break;
                case COLUMN_PROGRESS:
                    name = "��� ��������";
                    break;
                case COLUMN_STATUS:
                    name = "���������";
                    break;
                case COLUMN_ACTION:
                    name = "��������";
                    break;
            }
            return name;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RowData rowData = rows.get(rowIndex);
            Object value = null;
            switch (columnIndex) {
                case COLUMN_DB_NAME:
                    value = rowData.getName();
                    break;
                case COLUMN_PROGRESS:
                    value = rowData.getProgress();
                    break;
                case COLUMN_STATUS:
                    value = rowData.getStatus();
                    break;
                case COLUMN_ACTION:
                    value = rowData.getAction();
                    break;
            }
            return value;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            RowData rowData = rows.get(rowIndex);
            switch (columnIndex) {
                case 1:
                    if (aValue instanceof Float) {
                        rowData.setProgress((Float) aValue);
                    }
                    break;
                case 2:
                    if (aValue instanceof Status) {
                        rowData.setStatus((Status) aValue);
                    }
                case 3:
                    if (aValue instanceof ButtonConfig) {
                        rowData.setAction((ButtonConfig) aValue);
                    }
            }
        }

        public void addDb(String name, SwingWorker worker) {
            RowData rowData = new RowData(name);
            rowData.getAction().setWorker(worker);
            mapWorker.put(worker, rowData);
            mapLookup.put(name, rowData);
            rows.add(rowData);
        }

        protected void updateProgress(SwingWorker worker, int progress) {
        	RowData rowData = mapWorker.get(worker);
            if (rowData != null) {
                int row = rows.indexOf(rowData);
                float p = (float) progress / 100f;
                setValueAt(p, row, 1);
                fireTableCellUpdated(row, 1);
            }
        }

        protected void setStatus(SwingWorker worker, Status condition) {
        	RowData rowData = mapWorker.get(worker);
            if (rowData != null) {
                int row = rows.indexOf(rowData);
                setValueAt(condition, row, 2);
                fireTableCellUpdated(row, 2);
            }
        }

        private void setFinished(SwingWorker worker, ButtonConfig buttonConfig) {
        	RowData rowData = mapWorker.get(worker);
            if (rowData != null) {
                int row = rows.indexOf(rowData);
                setValueAt(buttonConfig, row, 3);
                fireTableCellUpdated(row, 3);
            }
        }
    }

    public class StubWorker extends SwingWorker<Exception, Void> {
        @Override
        protected Exception doInBackground() throws Exception {
        	try{
	            int i = 0;
	            Random r = new Random();
	            while (i <= 100) {
	                Thread.sleep(500);
	                setProgress(i);
	                i += (1 + r.nextInt(10));
	                if (i < 35 && i > 31) {
	                   throw new Exception("������ ��� ������������: " + i);
	                }
	            }
	            setProgress(100);
	            return null;
        	}catch (Exception ex) {
        		 return ex;
        	}
        }
    }
   
}
