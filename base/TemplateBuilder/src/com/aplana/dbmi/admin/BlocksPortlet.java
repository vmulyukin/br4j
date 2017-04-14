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

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.web.Option;
import com.aplana.dbmi.model.web.TreeControl;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.aplana.dbmi.search.SearchUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.jenkov.prizetags.tree.impl.Tree;
import com.jenkov.prizetags.tree.impl.TreeNode;
import com.jenkov.prizetags.tree.itf.ITree;
import com.jenkov.prizetags.tree.itf.ITreeNode;

public class BlocksPortlet extends SimpleFormController {
    protected final Log logger = LogFactory.getLog(getClass());

    private long idNumber;
    private long referenceIdNumber = 0;
    
    private ResourceBundle messages;
    private String editAccessRoles;
    protected Object formBackingObject(PortletRequest request) throws Exception {
        ContextProvider.getContext().setLocale(request.getLocale());

        WebBlockBean blockBean = null;
        try {
            AsyncDataServiceBean dataService = PortletUtil.createService(request);

            if (isSessionForm() && request.getPortletSession().getAttribute(getFormSessionAttributeName()) != null) {
                blockBean = (WebBlockBean) request.getPortletSession().getAttribute(getFormSessionAttributeName());
            } else {
                blockBean = (WebBlockBean) super.formBackingObject(request);
                blockBean.setAvailableAttributes((List) dataService.listChildren(AttributeBlock.ID_REST, Attribute.class));
            }

            loadBlocks(blockBean, dataService);
            SearchUtils.loadTemplates(dataService, blockBean.getWebSearchBean());
            SearchUtils.loadMainAttributes(dataService, blockBean.getWebSearchBean());
            blockBean.setDataService(dataService);
            blockBean.setEditAccessRoles(editAccessRoles);
        } catch (Exception e) {
            logger.error("Error forming backing object: ", e);
            if (blockBean != null)
            	blockBean.setMessage(e.getMessage());
        }
        return blockBean;

    }

    private void loadBlocks(WebBlockBean blockBean, DataServiceBean dataService)
            throws DataException, ServiceException {
        LinkedList blocks = new LinkedList(dataService.listAll(AttributeBlock.class));
        // Ad-hoc solution: sort by russian name
        Collections.sort(blocks, new Comparator() {
            public int compare(Object o0, Object o1) {
                AttributeBlock t0 = (AttributeBlock) o0;
                AttributeBlock t1 = (AttributeBlock) o1;
                
                return t0.getNameRu().compareTo(t1.getNameRu());
            }
        });
        blockBean.setBlocks(blocks);
    }

    /*
    protected ModelAndView onSubmitRender(RenderRequest request, RenderResponse response, Object command, BindException errors) throws Exception {
        ModelAndView mv = super.onSubmitRender(request, response, command, errors);
        Map model = mv.getModel();
        WebBlockBean blockBean = (WebBlockBean) command;
        model.put("searchBean", blockBean.getWebSearchBean());
        return mv;
    }
    */

    public void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {
        ContextProvider.getContext().setLocale(request.getLocale());
        messages = ResourceBundle.getBundle("templates", request.getLocale());
        WebBlockBean blockBean = (WebBlockBean) command;

        try {
            AsyncDataServiceBean dataService = blockBean.getDataService();

            if (WebSearchBean.ATTRIBUTE_SEARCH_ACTION.equals(blockBean.getWebSearchBean().getAction())) {
                SearchUtils.loadAttributes(dataService, blockBean.getWebSearchBean());
                blockBean.getWebSearchBean().setAction("");
                blockBean.setAction("");
            } else if (request.getParameter("block_new_id") != null) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                createNewBlock(blockBean);
            } else if (request.getParameter("block_edit_id") != null) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                String blockId = request.getParameter("block_edit_id");
                editBlock(blockId, blockBean, dataService);
            } else if (request.getParameter("block_delete_id") != null) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                deleteBlock(request, dataService);
            } else if (request.getParameter("attr_up_id") != null) {
                blockBean.upAttribute(request.getParameter("attr_up_id"));
            } else if (request.getParameter("attr_down_id") != null) {
                blockBean.downAttribute(request.getParameter("attr_down_id"));
            } else if (request.getParameter("attr_right_id") != null) {
                blockBean.moveAttributeFromCurrentToAvailable(request.getParameter("attr_right_id"));
            } else if (request.getParameter("attr_edit_id") != null) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                editAttribute(request, blockBean, dataService);
            } else if (request.getParameter("attr_delete_id") != null) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                deleteAttribute(request, blockBean, dataService);
            } else if (request.getParameter(WebBlockBean.PARAM_PREFIX + "up") != null) {
                moveNodeUp(request, blockBean);
            } else if (request.getParameter(WebBlockBean.PARAM_PREFIX + "down") != null) {
                moveNodeDown(request, blockBean);
            } else if (request.getParameter(WebBlockBean.PARAM_PREFIX + "left") != null) {
                moveNodeLeft(request, blockBean);
            } else if (request.getParameter(WebBlockBean.PARAM_PREFIX + "right") != null) {
                moveNodeRight(request, blockBean);
            } else if (request.getParameter(WebBlockBean.PARAM_PREFIX + "delete") != null) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                deleteNode(request, blockBean);
            } else if (request.getParameter(WebBlockBean.PARAM_PREFIX + "edit") != null) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                editReferenceValue(request, blockBean);
            } else if (blockBean.getAction() == null || WebBlockBean.SAVE_ACTION.equals(blockBean.getAction())) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                if (blockBean.getReferenceValue().getRealId() != null) {
                    saveReferenceValue(blockBean);
                } else if (blockBean.getAttribute().getRealId() != null) {
                    saveAttribute(blockBean, dataService);
                } else if (blockBean.getRealId() != null) {
                    saveBlock(blockBean, dataService);
                }
            } else if (WebBlockBean.ADD_ATTRIBUTE_ACTION.equals(blockBean.getAction())) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                addAttribute(blockBean, dataService);
            } else if (WebBlockBean.NEW_LIST_ACTION.equals(blockBean.getAction())) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                createNewList(blockBean);
            } else if (WebBlockBean.LOAD_LIST_ACTION.equals(blockBean.getAction())) {
                loadList(blockBean, dataService);
            } else if (WebBlockBean.ADD_REFERENCE_VALUE_ACTION.equals(blockBean.getAction())) {
    			if (!blockBean.isEditAccessExists()){
    				throw new DataException("admin.edit.access.error");
    			}
                createNewReferenceValue(blockBean);
            } else if (WebBlockBean.CLOSE_ACTION.equals(blockBean.getAction())) {
                if (blockBean.getReferenceValue().getRealId() != null) {
                    BeanUtils.copyProperties(new WebReferenceValue(), blockBean.getReferenceValue());
                    blockBean.getReferenceValue().setRealId(null);
                } else if (blockBean.getAttribute().getRealId() != null) {
                    closeAttribute(blockBean, dataService);
                } else if (!MyBeanUtils.isEmpty(blockBean.getRealId())) {
                    closeBlock(blockBean, dataService);
                }
            } else if (WebBlockBean.ATTRIBUTE_TYPE_ACTION.equals(blockBean.getAction())) {
                changeAttributeType(blockBean, dataService);
            }
        } catch (Exception e) {
            logger.error("Error processing action: ", e);
            blockBean.setMessage(e.getMessage());
        }

        if (isSessionForm()) {
            String formAttrName = getFormSessionAttributeName(request);
            request.getPortletSession().setAttribute(formAttrName, command);
        }

    }

    private void editReferenceValue(ActionRequest request, WebBlockBean blockBean) {
        ITreeNode node = blockBean.getTree().findNode(request.getParameter(WebBlockBean.PARAM_PREFIX + "edit"));
        ReferenceValue referenceValue = WebUtils.getReferenceFromNode(node, true);
        BeanUtils.copyProperties(referenceValue, blockBean.getReferenceValue());
        blockBean.getReferenceValue().setRealId(referenceValue.getId().getId());
        blockBean.getReferenceValue().setParentRealId(referenceValue.getParent().getId());
        Collection availableNodes = new ArrayList();
        WebUtils.getOptionsFromTree(availableNodes, blockBean.getTree().getRoot());
        blockBean.setAvailableNodes(availableNodes);
    }

    private void deleteBlock(ActionRequest request, AsyncDataServiceBean dataService) throws DataException, ServiceException {
        AttributeBlock block = (AttributeBlock) dataService.getById(new ObjectId(AttributeBlock.class, request.getParameter("block_delete_id")));
        block.setActive(false);
        dataService.saveObject(block, ExecuteOption.SYNC);
    }

    private void deleteAttribute(ActionRequest request, WebBlockBean blockBean, AsyncDataServiceBean dataService) throws DataException, ServiceException {
        String attr_id = request.getParameter("attr_delete_id");
        Attribute attribute = (Attribute) dataService.getById(new ObjectId(Attribute.class, attr_id));
//        Attribute attribute = blockBean.getAttribute(attr_id);
        dataService.doAction(new LockObject(attribute));
        attribute.setActive(false);
        dataService.saveObject(attribute, ExecuteOption.SYNC);
        dataService.doAction(new UnlockObject(attribute));
        blockBean.getAttribute(attr_id).setActive(false);
    }

    private void editAttribute(ActionRequest request, WebBlockBean blockBean, AsyncDataServiceBean dataService) throws DataException, ServiceException, RemoteException {
        ObjectId attributeId = new ObjectId(Attribute.class, request.getParameter("attr_edit_id"));
        loadAttribute(attributeId, blockBean, dataService);
    }

    private void loadAttribute(ObjectId attributeId, WebBlockBean blockBean, DataServiceBean dataService) throws DataException, ServiceException, RemoteException {
        Attribute attribute = (Attribute) dataService.getById(attributeId);
        dataService.doAction(new LockObject(attribute));
        BeanUtils.copyProperties(attribute, blockBean.getAttribute(), new String[] { "locker", "lockTime" });
        blockBean.getAttribute().setSearchShow(attribute.getColumnWidth() != 0);

        if (Attribute.TYPE_TREE.equals(attribute.getType()) || Attribute.TYPE_LIST.equals(attribute.getType())) {
            TreeControl control = (TreeControl) TemplateControlUtils.initializeControl(attribute, dataService);
            control.getTree().expandAll();
            blockBean.setTree(control.getTree());
        } else if (Attribute.TYPE_CARD_LINK.equals(attribute.getType())) {
            CardLinkAttribute clAttribute = (CardLinkAttribute) attribute;
            Search filter = clAttribute.getFilter();
            WebSearchBean webSearchBean = blockBean.getWebSearchBean();
            
        	/**
        	 * disanbirdin: 
        	 * important if edit two card link attributes in the same block.
        	 * in the future must be provided a "reset"- method on the WebSearchBean
        	 * and called from SearchUtils  
        	 */
        	webSearchBean.reset();

            
            SearchUtils.initializeFromSearch(filter, webSearchBean);
        }

        blockBean.getAttribute().setId(attribute.getId().getId().toString());
    }

    private void deleteNode(ActionRequest request, WebBlockBean blockBean) {
        ITreeNode node = blockBean.getTree().findNode(request.getParameter(WebBlockBean.PARAM_PREFIX + "delete"));
        ((ReferenceValue) node.getObject()).setActive(false);
//        node.getParent().removeChild(node);
    }

    private void createNewReferenceValue(WebBlockBean blockBean) {
        WebReferenceValue referenceValue = new WebReferenceValue();
        referenceValue.setActive(true);
        referenceValue.setRealId(generateReferenceId());
        blockBean.setReferenceValue(referenceValue);
        referenceValue.setParentRealId(blockBean.getAttribute().getRealId());
        Collection availableNodes = new ArrayList();
        WebUtils.getOptionsFromTree(availableNodes, blockBean.getTree().getRoot());
        blockBean.setAvailableNodes(availableNodes);
    }

    private void saveReferenceValue(WebBlockBean blockBean) {
        WebReferenceValue referenceValue = blockBean.getReferenceValue();
        ITree tree = blockBean.getTree();
        ITreeNode newParent = tree.findNode(referenceValue.getParentRealId().toString());
        ITreeNode node = tree.findNode(referenceValue.getRealId().toString());
        if (node != null) {
            node.setNameEn(referenceValue.getValueEn());
            node.setNameRu(referenceValue.getValueRu());
            ITreeNode oldParent = node.getParent();
            if (!newParent.equals(oldParent)) {
                moveNode(node, oldParent, newParent, tree);
            }
        } else {
            newParent.addChild(WebUtils.getNodeFromReference(referenceValue));
        }
        BeanUtils.copyProperties(new WebReferenceValue(), referenceValue);
    }

    private void moveNode(ITreeNode node, ITreeNode oldParent, ITreeNode newParent, ITree tree) {
        oldParent.removeChild(node);
        if (!oldParent.hasChildren()) {
            tree.collapse(oldParent.getId());
        }
        newParent.addChild(node);
        tree.expand(newParent.getId());
    }

    private void moveNodeRight(ActionRequest request, WebBlockBean blockBean) {
        ITreeNode node = blockBean.getTree().findNode(request.getParameter(WebBlockBean.PARAM_PREFIX + "right"));
        ITreeNode oldParent = node.getParent();
        int index = node.getParent().getChildren().indexOf(node);
        ITreeNode newParent = (ITreeNode) node.getParent().getChildren().get(index - 1);
        moveNode(node, oldParent, newParent, blockBean.getTree());
    }

    private void moveNodeLeft(ActionRequest request, WebBlockBean blockBean) {
        ITreeNode node = blockBean.getTree().findNode(request.getParameter(WebBlockBean.PARAM_PREFIX + "left"));
        ITreeNode oldParent = node.getParent();
        ITreeNode newParent = oldParent.getParent();
        moveNode(node, oldParent, newParent, blockBean.getTree());
    }

    private void moveNodeDown(ActionRequest request, WebBlockBean blockBean) {
        ITreeNode node = blockBean.getTree().findNode(request.getParameter(WebBlockBean.PARAM_PREFIX + "down"));
        List children = node.getParent().getChildren();
        ListUtils.downObject(children, node);
    }

    private void moveNodeUp(ActionRequest request, WebBlockBean blockBean) {
        ITreeNode node = blockBean.getTree().findNode(request.getParameter(WebBlockBean.PARAM_PREFIX + "up"));
        List children = node.getParent().getChildren();
        ListUtils.upObject(children, node);
    }

    private void loadList(WebBlockBean blockBean, DataServiceBean dataService) throws DataException, ServiceException, RemoteException {
        ReferenceAttribute attribute = (ReferenceAttribute) dataService.getById(new ObjectId(Attribute.class, blockBean.getFromAttributeCode()));
        TreeControl control = (TreeControl) TemplateControlUtils.initializeControl(attribute, dataService);
        control.getTree().expandAll();
        blockBean.setTree(control.getTree());
    }

    private void createNewList(WebBlockBean blockBean) {
        blockBean.setTree(new Tree());
        ITreeNode rootNode = new TreeNode();
        rootNode.setNameEn(blockBean.getAttribute().getNameEn());
        rootNode.setNameRu(blockBean.getAttribute().getNameRu());
        rootNode.setId(blockBean.getAttribute().getId().getId().toString());
        rootNode.setObject(blockBean.getAttribute());
        blockBean.getTree().setRoot(rootNode);
        blockBean.getTree().expand(rootNode.getId());
    }

    private void changeAttributeType(WebBlockBean blockBean, DataServiceBean dataService) throws DataException, ServiceException {
        Collection attributes = null;
        if (Attribute.TYPE_TREE.equals(blockBean.getAttribute().getType())) {
            attributes = dataService.listAll(TreeAttribute.class);
        } else if (Attribute.TYPE_LIST.equals(blockBean.getAttribute().getType())) {
            attributes = dataService.listAll(ListAttribute.class);
        }
        List availableAttributes = new ArrayList();
        if (attributes != null) {
            for (Iterator it = attributes.iterator(); it.hasNext();) {
                Attribute attribute = (Attribute) it.next();
                Option option = new Option();
                option.setLabelEn(attribute.getNameEn());
                option.setLabelRu(attribute.getNameRu());
                option.setValue(attribute.getId().getId().toString());
                availableAttributes.add(option);
            }
        }
        blockBean.setAvailableListAttributes(availableAttributes);
        if (Attribute.TYPE_CARD_LINK.equals(blockBean.getAttribute().getType())) {
            Search filter = new Search();
            WebSearchBean webSearchBean = blockBean.getWebSearchBean();
            SearchUtils.initializeFromSearch(filter, webSearchBean);
        }

    }

    private void closeAttribute(WebBlockBean blockBean, DataServiceBean dataService) throws DataException, ServiceException {
        boolean newAttribute = isNewAttribute(blockBean);
        if (!newAttribute) {
        	Attribute attribute = (Attribute) dataService.getById(new ObjectId(Attribute.class, blockBean.getAttribute().getRealId()));
            dataService.doAction(new UnlockObject(attribute));
        }
        BeanUtils.copyProperties(new WebAttribute(), blockBean.getAttribute());
        blockBean.getAttribute().setRealId(null);
        blockBean.setTree(null);
        blockBean.getReferenceValue().setRealId(null);
        blockBean.setAvailableAttributes((List) dataService.listChildren(AttributeBlock.ID_REST, Attribute.class));
    }

    private void closeBlock(WebBlockBean blockBean, DataServiceBean dataService) throws DataException, ServiceException {
        boolean newBlock = isNewBlock(blockBean);
        if (!newBlock) {
            dataService.doAction(new UnlockObject(new ObjectId(AttributeBlock.class, blockBean.getRealId())));
        }                    
        BeanUtils.copyProperties(new AttributeBlock(), blockBean);
        blockBean.setRealId(null);
    }

    private void saveBlock(WebBlockBean blockBean, AsyncDataServiceBean dataService) throws DataException, ServiceException {
        AttributeBlock block = new AttributeBlock();
        BeanUtils.copyProperties(blockBean, block);
        boolean newBlock = isNewBlock(blockBean);
        if (!newBlock) {
            block.setId(blockBean.getRealId().toString());
        } else {
            block.setId((String)null);
            //block.setAttributes(new ArrayList());
        }
        if (block.getAttributes() == null)
        	block.setAttributes(new ArrayList());
        block.setActive(true);
        ObjectId id = dataService.saveObject(block, ExecuteOption.SYNC);
        block = (AttributeBlock) dataService.getById(id);
        if (newBlock) {
//                        block.setId(id.getId().toString());
            blockBean.getBlocks().add(block);
            blockBean.setRealId(id.getId());
        }
        loadBlocks(blockBean, dataService);
        blockBean.setAvailableAttributes((List) dataService.listChildren(AttributeBlock.ID_REST, Attribute.class));
        blockBean.setMessage(messages.getString("templatesBlockSaveSuccess"));
    }

    private void saveAttribute(WebBlockBean blockBean, AsyncDataServiceBean dataService) throws DataException, ServiceException, RemoteException, ParseException {
        Attribute attribute = null;
        final Object type = blockBean.getAttribute().getType();

        if (Attribute.TYPE_DATE.equals(type)) {
            attribute = new DateAttribute();
        } else if (Attribute.TYPE_INTEGER.equals(type)) {
            attribute = new IntegerAttribute();
        } else if (Attribute.TYPE_LONG.equals(type)) {
            attribute = new LongAttribute();
        } else if (Attribute.TYPE_LIST.equals(type)) {
            attribute = WebUtils.getListAttributeFormTree(blockBean.getAttribute(), blockBean.getTree().getRoot());
        } else if (Attribute.TYPE_TREE.equals(type)) {
            attribute = WebUtils.getTreeAttributeFormTree(blockBean.getAttribute(), blockBean.getTree().getRoot());
        } else if (Attribute.TYPE_PERSON.equals(type)) {
            attribute = new PersonAttribute();
        } else if (Attribute.TYPE_STRING.equals(type)) {
            attribute = new StringAttribute();
        } else if (Attribute.TYPE_TEXT.equals(type)) {
            attribute = new TextAttribute();
        } else if (Attribute.TYPE_CARD_LINK.equals(type)) {
            final CardLinkAttribute cardAttribute = new CardLinkAttribute();
            attribute = cardAttribute;
            final WebSearchBean webSearchBean = blockBean.getWebSearchBean();
            /*
            webSearchBean.setNumber(Boolean.FALSE);
            webSearchBean.setProperty(Boolean.TRUE);
            webSearchBean.setFullText(Boolean.FALSE);
            */
            cardAttribute.setFilter(SearchUtils.getSearch(dataService, webSearchBean));
        } else
        	throw new DataException( "store.attribute.notexists", 
        				new Object[] { 
        						" type " + ((type != null) ? type.toString() : "null") 
        				});

        BeanUtils.copyProperties(blockBean.getAttribute(), attribute);
        if (!blockBean.getAttribute().isSearchShow()) {
            attribute.setColumnWidth(0);
        }
        
        if (!isNewAttribute(blockBean)) {
            attribute.setId(blockBean.getAttribute().getRealId().toString());
        } else {
            attribute.setId((String)null);
            attribute.setActive(true);
            blockBean.getAttributes().add(attribute);
        }
     // BDMI00000045 fix 21.05.08 �.�.
        if (attribute.getBlockId().getId().toString().indexOf(WebUtils.ID_PREFIX) >= 0)
            attribute.setBlockId(null);
        // BDMI00000045 fix
        final ObjectId id = dataService.saveObject(attribute, ExecuteOption.SYNC);

        loadAttribute(id, blockBean, dataService);

        attribute.setId(id.getId().toString());

        blockBean.setMessage(messages.getString("templatesAttributeSaveSuccess"));
    }

    private void createNewBlock(WebBlockBean blockBean) {
        BeanUtils.copyProperties(new TemplateBlock(), blockBean);
        blockBean.setId(generateId());
        blockBean.setAttribute(new WebAttribute());
    }

    private void editBlock(String blockId, WebBlockBean blockBean, DataServiceBean dataService) throws DataException, ServiceException {
        AttributeBlock block = (AttributeBlock) dataService.getById(new ObjectId(AttributeBlock.class, blockId));

        dataService.doAction(new LockObject(block));
        BeanUtils.copyProperties(block, blockBean, new String[] { "locker", "lockTime" });
        blockBean.setId(block.getId().getId().toString());
        blockBean.setAttribute(new WebAttribute());
    }

    private boolean isNewBlock(WebBlockBean blockBean) {
        return blockBean.getRealId().toString().indexOf(WebUtils.ID_PREFIX) >= 0;
    }

    private boolean isNewAttribute(WebBlockBean blockBean) {
        return blockBean.getAttribute().getRealId().toString().indexOf(WebUtils.ID_PREFIX) >= 0;
    }

    private void addAttribute(WebBlockBean blockBean, DataServiceBean dataService) 
    	// throws DataException, ServiceException 
    {

        if (blockBean.getRealId() != null) {
            blockBean.getAttribute().setBlockId(blockBean.getId());
        }
        
        if ("-1".equals(blockBean.getAttribute().getNewId())) {
            blockBean.getAttribute().setRealId(generateId());
            blockBean.getAttribute().setActive(true);
        } else {
            blockBean.moveAttributeFromAvailableToCurrent(blockBean.getAttribute().getNewId());
//            blockBean.getAttribute().setRealId(blockBean.getAttribute().getNewId());
        }
/*        
        List availableAttributes = null;
        if (Attribute.TYPE_TREE.equals(blockBean.getAttribute().getType())) {
            availableAttributes = (List) dataService.listAll(TreeAttribute.class);
        } else if (Attribute.TYPE_LIST.equals(blockBean.getAttribute().getType())) {
            availableAttributes = (List) dataService.listAll(ListAttribute.class);
        }
        blockBean.setAvailableAttributes(availableAttributes);
*/        
    }

    /*
     * private Collection getAvailableAttributes(DataServiceBean dataService)
     * throws ServiceException, DataException { return
     * dataService.listChildren(AttributeBlock.ID_REST, Attribute.class);
     *  }
     */

    public long getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(long idNumber) {
        this.idNumber = idNumber;
    }

    /*
     * public Block getBlock(String code, WebBlockBean blockBean) { List blocks =
     * blockBean.getBlocks(); if (blocks == null) { return null; } for (Iterator
     * it = blocks.iterator(); it.hasNext();) { Block block = (Block) it.next();
     * if (code.equals(block.getCode())) { return block; } } return null; }
     */

    private String generateId() {
        ++idNumber;
        return WebUtils.ID_PREFIX + idNumber;
    }

    private Long generateReferenceId() {
        return new Long(--referenceIdNumber);
    }

	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}
}