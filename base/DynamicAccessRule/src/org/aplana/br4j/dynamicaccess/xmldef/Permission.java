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
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */

package org.aplana.br4j.dynamicaccess.xmldef;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.OperationType;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * �������� ����(����������)����� �������� ��
 * ������(�����������)������� � ��������� �������� �������� �
 * �������� ����������� �����(����������)
 * 
 * @version $Revision$ $Date$
 */
public class Permission implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _status
     */
    private java.lang.String _status;

    /**
     * Field _rule
     */
    private java.lang.String _rule;

    /**
     * ������ rule_id ������������ �������
     */
    private org.aplana.br4j.dynamicaccess.xmldef.Operations _Operations;

    /**
     * �������� ��������� ��� ����������� ������� � ����
     */
    private org.aplana.br4j.dynamicaccess.xmldef.WfMoves _wfMoves;

    /**
     * �������� ��������� ��� ����������� ������� � ����
     */
    private org.aplana.br4j.dynamicaccess.xmldef.Attributes _attributes;


      //----------------/
     //- Constructors -/
    //----------------/

    public Permission() {
        super();
        _Operations = new Operations();
        _wfMoves = new WfMoves();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Permission()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'attributes'. The field
     * 'attributes' has the following description: ��������
     * ��������� ��� ����������� ������� � ����
     * 
     * @return Attributes
     * @return the value of field 'attributes'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Attributes getAttributes()
    {
        return this._attributes;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Attributes getAttributes() 

    /**
     * Returns the value of field 'Operations'. The field 'Operations' has the
     * following description: ������ rule_id ������������ �������
     * 
     * @return Operations
     * @return the value of field 'Operations'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Operations getOperations()
    {
        return this._Operations;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Operations getOperations() 

    /**
     * Returns the value of field 'rule'.
     * 
     * @return String
     * @return the value of field 'rule'.
     */
    public java.lang.String getRule()
    {
        return this._rule;
    } //-- java.lang.String getRule() 

    /**
     * Returns the value of field 'status'.
     * 
     * @return String
     * @return the value of field 'status'.
     */
    public java.lang.String getStatus()
    {
        return this._status;
    } //-- java.lang.String getStatus() 

    /**
     * Returns the value of field 'wfMoves'. The field 'wfMoves'
     * has the following description: �������� ��������� ���
     * ����������� ������� � ����
     * 
     * @return WfMoves
     * @return the value of field 'wfMoves'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.WfMoves getWfMoves()
    {
        return this._wfMoves;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WfMoves getWfMoves() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'attributes'. The field 'attributes'
     * has the following description: �������� ��������� ���
     * ����������� ������� � ����
     * 
     * @param attributes the value of field 'attributes'.
     */
    public void setAttributes(org.aplana.br4j.dynamicaccess.xmldef.Attributes attributes)
    {
        this._attributes = attributes;
    } //-- void setAttributes(org.aplana.br4j.dynamicaccess.xmldef.Attributes) 


    /**
     * Sets the value of field 'Operations'. The field 'Operations' has the
     * following description: ������ rule_id ������������ �������
     * 
     * @param Operations the value of field 'Operations'.
     */
    public void setOperations(org.aplana.br4j.dynamicaccess.xmldef.Operations Operations)
    {
        this._Operations = Operations;
    } //-- void setOperations(org.aplana.br4j.dynamicaccess.xmldef.Operations) 

    /**
     * Sets the value of field 'rule'.
     * 
     * @param rule the value of field 'rule'.
     */
    public void setRule(java.lang.String rule)
    {
        this._rule = rule;
    } //-- void setRule(java.lang.String) 

    /**
     * Sets the value of field 'status'.
     * 
     * @param status the value of field 'status'.
     */
    public void setStatus(java.lang.String status)
    {
        this._status = status;
    } //-- void setStatus(java.lang.String) 

    /**
     * Sets the value of field 'wfMoves'. The field 'wfMoves' has
     * the following description: �������� ��������� ���
     * ����������� ������� � ����
     * 
     * @param wfMoves the value of field 'wfMoves'.
     */
    public void setWfMoves(org.aplana.br4j.dynamicaccess.xmldef.WfMoves wfMoves)
    {
        this._wfMoves = wfMoves;
    } //-- void setWfMoves(org.aplana.br4j.dynamicaccess.xmldef.WfMoves) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.aplana.br4j.dynamicaccess.xmldef.Permission) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.Permission.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

    @Override
    public String toString() {
    	return "Rule: " + _rule + ", Status: " + _status;
    }
    
    public OperationType resolveOperationType(){
    	if(this.getOperations() == null || this.getOperations().getOperationCount() == 0){
    		return OperationType.EMPTY;
    	}
    	boolean hasRealOperation = false;
    	List<Operation> operations = Arrays.asList(this.getOperations().getOperations());
    	for(Operation operation : operations ){
    		if(!Action.REMOVE.equals(operation.getAction())){
    			hasRealOperation = true;
    			OperationType type = operation.getOperationType();
    			if(OperationType.WRITE.equals(type) || OperationType.CREATE.equals(type)){
    				return type;
    			}
    		}
    	}
    	if(hasRealOperation){
    		return OperationType.READ;
    	} else {
    		return OperationType.EMPTY;
    	}
    }
    
    public void generatePermHashes(Template template){ 
    	for(Rule rule : template.getRules().getRule()){
    		if(rule.getName().equals(this.getRule())){
    			if(this.getOperations() != null){
	    			for(Operation operation : this.getOperations().getOperations()){
	    				operation.generateRuleHash(rule.getRuleString(), template.getTemplate_id(), this.getStatus());
	    			}
    			}
    			if(this.getWfMoves() != null){
	    			for(WfMove wfMove : this.getWfMoves().getWfMove()){
	    				wfMove.generateRuleHash(rule.getRuleString(), template.getTemplate_id(), this.getStatus());
	    			}
    			}
    		}
    	}
    }
    @Override
	public int hashCode() {
		return 		( (_rule != null) ? _rule.hashCode() : 2345234) 
				 ^	( (_status != null) ? _status.hashCode() : 45234323);
	}


	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Permission))
			return false;
		return obj.hashCode() == this.hashCode();
	}
	
	public boolean hasOperation(OperationType type, Action action){
		return getOperation(type, action) != null;
	}
	
	/*
	 * ���������� Operation � ���������� OperationType � Action
	 */
	public Operation getOperation(OperationType type, Action action){
		for(Operation operation : this.getOperations().getOperations()){
			if(type.equals(operation.getOperationType())){
				if((action == null && operation.getAction() == null) 
						|| (action != null && action.equals(operation.getAction()))){
					return operation;
				}
			}
		}
		return null;
	}
	
	/*
	 * ���������� Operation � ��������� OperationType � ����� Action
	 */
	public Operation getOperation(OperationType type){
		for(Operation operation : this.getOperations().getOperations()){
			if(type.equals(operation.getOperationType())){
				return operation;
			}
		}
		return null;
	}
	
	public boolean hasWfMove(String wfm_id, Action action){
		return getWfMove(wfm_id, action) != null;
	}
	
	/*
	 * ���������� WfMove � ���������� wfm_id � Action
	 */
	public WfMove getWfMove(String wfm_id, Action action){
		for(WfMove wfMove : this.getWfMoves().getWfMove()){
			if(wfm_id.equals(wfMove.getWfm_id())){
				if((action == null && wfMove.getAction() == null) 
						|| (action != null && action.equals(wfMove.getAction()))){
					return wfMove;
				}
			}
		}
		return null;
	}
	
	/*
	 * ���������� WfMove � ���������� wfm_id � ����� Action
	 */
	public WfMove getWfMove(String wfm_id){
		for(WfMove wfMove : this.getWfMoves().getWfMove()){
			if(wfm_id.equals(wfMove.getWfm_id())){
				return wfMove;
			}
		}
		return null;
	}
	
	public List<Operation> getOperationChanges(){
		List<Operation> operations = new ArrayList<Operation>();
		for(Operation operation : this.getOperations().getOperations()){
			if(operation.getAction() != null){
				operations.add(operation);
			}
		}
		return operations;
	}
	
	public List<WfMove> getWfmChanges(){
		List<WfMove> wfMoves = new ArrayList<WfMove>();
		for(WfMove wfMove : this.getWfMoves().getWfMove()){
			if(wfMove.getAction() != null){
				wfMoves.add(wfMove);
			}
		}
		return wfMoves;
	}
	
	public void removeAutoOperationsAndWFMoves(){
		Iterator<Operation> iOp = this.getOperations().getOperationList().iterator();
		while(iOp.hasNext()){
			Operation o = iOp.next();
			if(o.isAuto()){
				iOp.remove();
			}
		}
		
		Iterator<WfMove> iWfm = this.getWfMoves().getWfMoveList().iterator();
		while(iWfm.hasNext()){
			WfMove w = iWfm.next();
			if(w.isAuto()){
				iWfm.remove();
			}
		}

	}
    
}
