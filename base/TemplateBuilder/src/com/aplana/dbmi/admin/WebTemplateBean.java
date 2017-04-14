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
package com.aplana.dbmi.admin;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.web.Option;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * Created by IntelliJ IDEA. User: ipolukhin Date: Feb 5, 2008 Time: 7:09:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebTemplateBean extends Template {
	private static final long serialVersionUID = 2L;

	public static final String BLOCK_ACTION = "BLOCK_ACTION";

    public static final String SAVE_ACTION = "SAVE_ACTION";

    public static final String CLOSE_ACTION = "CLOSE_ACTION";

    public static final String EDIT_TEMPLATE_ACCESS_ACTION = "EDIT_TEMPLATE_ACCESS_ACTION";
    
    public static final String SAVE_AND_DETAIL_ACTION = "SAVE_AND_DETAIL_ACTION";

    public static final String EDIT_TEMPLATE_RW_ATTR_ACTION = "EDIT_TEMPLATE_RW_ATTR_ACTION";

	public static final String EDIT_TEMPLATE_ATTR_ACTION = "EDIT_TEMPLATE_ATTR_ACTION";

	public static final String EDIT_TEMPLATE_WORFLOW_MOVE_REQ_ATTR_ACTION = "EDIT_TEMPLATE_WORFLOW_MOVE_REQ_ATTR_ACTION";

    private Collection templates;

    private List[] blockColumns = new ArrayList[] { new ArrayList(), new ArrayList(), new ArrayList() };

    //private List blocks2 = new ArrayList();

    private String templateAction;
    
    private String blockId;

    private String redirectURL;

    private Boolean fromSubmit;

    private boolean showAccessSettingsWarning;
    
    private boolean changed;

    private List blockItems;

    private String message;    
    
    private List workflows;
    
	private AsyncDataServiceBean dataService;
    
	private String editAccessRoles;
	private boolean editAccessExists=false;

	public AsyncDataServiceBean getDataService() {
		return dataService;
	}
	
	public void setDataService(AsyncDataServiceBean dataService) {
		this.dataService = dataService;
	}

    public Collection getTemplates() {
        return templates;
    }

    public void setTemplates(Collection templates) {
        this.templates = templates;
    }

    public Boolean getShowTemplate() {
        return Boolean.valueOf(this.getId() != null && this.getId().getId() != null);
    }

    public List getBlockColumn(int position) {
        return blockColumns[getColumn(position)];
    }

    public void setBlockColumn(int position, List blocks) {
        this.blockColumns[getColumn(position)] = blocks;
    }
    
    public int getColumn(int position) {
    	switch (position) {
    	case TagConstant.POS_LEFT:
    		return 0;
    	case TagConstant.POS_RIGHT:
    		return 1;
    	case TagConstant.POS_DOWN:
    		return 2;
    	}
    	throw new IllegalArgumentException("Wrong position: " + position);
    }
    
    public int getPosition(int column) {
    	switch (column) {
    	case 0:
    		return TagConstant.POS_LEFT;
    	case 1:
    		return TagConstant.POS_RIGHT;
    	case 2:
    		return TagConstant.POS_DOWN;
    	}
    	throw new IllegalArgumentException("Undefined column: " + column);
    }
    
    public List getBlocksLeft() {
    	return getBlockColumn(TagConstant.POS_LEFT);
    }
    
    public List getBlocksRight() {
    	return getBlockColumn(TagConstant.POS_RIGHT);
    }
    
    public List getBlocksDown() {
    	return getBlockColumn(TagConstant.POS_DOWN);
    }
    
    /**
     * disanbirdin: always provide setter if u want to use BeanUtils.copyProperties
     * 
     * sets List into blockColumns[0].
     */
    public void setBlocksLeft(List blockLeft) {
    	setBlockColumn(TagConstant.POS_LEFT, blockLeft);
    }
    
    /**
     * disanbirdin: always provide setter if u want to use BeanUtils.copyProperties
     * 
     * sets List into blockColumns[1].
     */
    public void setBlocksRight(List blockRight) {
    	setBlockColumn(TagConstant.POS_RIGHT, blockRight);
    }
    
    /**
     * disanbirdin: always provide setter if u want to use BeanUtils.copyProperties
     * 
     * sets List into blockColumns[2].
     */
    public void setBlocksDown(List blockDown) {
    	setBlockColumn(TagConstant.POS_DOWN, blockDown);
    }
    
    
    
    public void clearBlocks() {
    	blockColumns = new List[] { new ArrayList(), new ArrayList(), new ArrayList() };
    }

    public void removeBlock(int column, String code) {
        List blocks = getBlockColumn(getPosition(column));
        TemplateBlock block = getBlock(code, blocks);
        blocks.remove(block);
    }

    public void upBlock(int column, String code) {
        List blocks = getBlockColumn(getPosition(column));
        int index = getBlockIndex(code, blocks);
        if (index > 0) {
            TemplateBlock tmpBlock = (TemplateBlock) blocks.get(index - 1);
            blocks.remove(tmpBlock);
            blocks.add(index, tmpBlock);
        }
    }

    public void downBlock(int column, String code) {
        List blocks = getBlockColumn(getPosition(column));
        int index = getBlockIndex(code, blocks);
        if (index < blocks.size() - 1) {
            TemplateBlock tmpBlock = (TemplateBlock) blocks.get(index + 1);
            blocks.remove(tmpBlock);
            blocks.add(index, tmpBlock);
        }
    }

    public void leftBlock(String code, int position) {
    	List blocks = getBlockColumn(position);
        TemplateBlock block = getBlock(code, blocks);
        blocks.remove(block);
        int target = TagConstant.POS_LEFT;
        if (position == TagConstant.POS_LEFT)
        	target = TagConstant.POS_DOWN;
//TODO �������� �������� Ace						
//        block.setColumn(getColumn(target));
        getBlockColumn(target).add(block);
    }

    public void rightBlock(String code, int position) {
    	List blocks = getBlockColumn(position);
        TemplateBlock block = getBlock(code, blocks);
        blocks.remove(block);
        int target = TagConstant.POS_RIGHT;
        if (position == TagConstant.POS_RIGHT)
        	target = TagConstant.POS_DOWN;
//TODO �������� �������� Ace						
//        block.setColumn(getColumn(target));
        getBlockColumn(target).add(block);
    }

    public static TemplateBlock getBlock(String code, List blocks) {
        if (blocks == null) {
            return null;
        }
        for (Iterator it = blocks.iterator(); it.hasNext();) {
            TemplateBlock block = (TemplateBlock) it.next();
            if (code.equals(block.getId().getId())) {
                return block;
            }
        }
        return null;
    }

    public static int getBlockIndex(String code, List blocks) {
        if (blocks == null) {
            return -1;
        }
        int index = 0;
        for (Iterator it = blocks.iterator(); it.hasNext(); index++) {
            TemplateBlock block = (TemplateBlock) it.next();
            if (code.equals(block.getId().getId())) {
                return index;
            }
        }
        return -1;
    }

    /*private List getBlocks(int column) {
        if (column == 0) {
            return blocks1;
        } else {
            return blocks2;
        }
    }*/

    public String getTemplateAction() {
        return templateAction;
    }

    public void setTemplateAction(String action) {
        this.templateAction = action;
    }

    public List getBlockItems() {
        return blockItems;
    }

    public Object getRealId() {
        return super.getId() == null || super.getId().getId() == null ? null : super.getId().getId();
    }

    public void setRealId(Object id) {
        ObjectId objectId = new ObjectId(Template.class, id);
        super.setId(objectId);
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }


	public void setBlockItems(Collection blockItems) {
		final List/*<Option>*/ tmpBlockItems = new ArrayList();
		if (blockItems != null) {
			for (Iterator it = blockItems.iterator(); it.hasNext();) {
				final AttributeBlock block = (AttributeBlock) it.next();
				final Option option = new Option();
				option.setLabelEn(block.getNameEn());
				option.setLabelRu(block.getNameRu());
				option.setValue(block.getId().getId().toString());
				tmpBlockItems.add(option);
			} // for
		} // blockItems
		// ���������� �� ��������...
		final Object[] arropts = tmpBlockItems.toArray();
		Arrays.sort(arropts, new Comparator(){
			public int compare(Object argA, Object argB) {
				final Option a = (Option) argA;
				final Option b = (Option) argB;
				final boolean aIsNull = (a==null) || (a.getLabelRu() == null);
				final boolean bIsNull = (b==null) || (b.getLabelRu() == null);
				if (aIsNull) return (bIsNull) ? 0 : -1;
				if (bIsNull) return 1;
				// here a!=null & b!=null
				return String.CASE_INSENSITIVE_ORDER.compare(a.getLabelRu(), b.getLabelRu());
			}});

		this.blockItems = new ArrayList( arropts.length );
		for (int i = 0; i < arropts.length; i++) {
			this.blockItems.add(arropts[i]);
		}
	}

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public Boolean getFromSubmit() {
        return fromSubmit;
    }

    public void setFromSubmit(Boolean fromSubmit) {
        this.fromSubmit = fromSubmit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	public List getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List workflows) {
		this.workflows = workflows;
	}
	
	public void setWorkflowId(long workflowId) {
		if (workflowId != getWorkflowId()) {
			showAccessSettingsWarning = true;
		}
		setWorkflow(new ObjectId(Workflow.class, new Long(workflowId)));
	}
	
	public long getWorkflowId() {
		return getWorkflow() == null ? -1 : ((Long)getWorkflow().getId()).longValue();
	}

	public boolean isShowAccessSettingsWarning() {
		return showAccessSettingsWarning;
	}

	public void setShowAccessSettingsWarning(boolean value) {
		this.showAccessSettingsWarning = value;
	}

	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}

	/**
	 * ��������� ������� � �������� ������������ ���� �� �������������� �������� � ��������.
	 * ����� ���������� ���������� ���������� � ������ ������ � ����� ��� ����, 
	 * ����� ��� ���������/������������ ���� � ������ ������ ���� ��� ����� ������ ���������/��������� ��� ������������ 
	 * @return true - ����� ����, false - ���� ���
	 */
	public boolean isEditAccessExists() throws DataException, ServiceException{
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
			CheckRolesForUser checkAction = new CheckRolesForUser();
			checkAction.setPersonLogin(dataService.getUserName());
			checkAction.setRoles(editAccessRoles);
			editAccessExists = (Boolean)dataService.doAction(checkAction);
		} else
			// ���� ��������������� ���� �� ������, �� ����� �� �������������� � �������� ������������ ����
			editAccessExists = true;
		return editAccessExists;
	}
}
