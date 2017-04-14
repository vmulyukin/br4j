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
package com.aplana.distrmanager.letter.types;

import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

public class Letter {
	
	public static final String RESULT_FILE_NAME = "DistributionLetter.xml";
	
	private static final String CLAZZ_LETTER = ".Letter";
	// Setters
	private static final String SET_ID_LETTER = "setId";
	private static final String SET_DATE_LETTER = "setDate";
	private static final String SET_TYPE_LETTER = "setType";
	private static final String SET_ADDRESSEE_LETTER = "setAddressee";
	private static final String SET_SENDER_LETTER = "setSender";
	private static final String SET_ATTACHMENTS_LETTER = "setAttachments";
	private static final String SET_DESCRIPTION_LETTER = "setDescription";
	
	private static final String SET_GUID = "setGuid";
	private static final String SET_NAME = "setName";
	private static final String SET_VALUE = "setValue";
	
	// Getters
	private static final String GET_ID_LETTER = "getId";
	private static final String GET_DATE_LETTER = "getDate";
	private static final String GET_TYPE_LETTER = "getType";
	private static final String GET_ADDRESSEE_LETTER = "getAddressee";
	private static final String GET_SENDER_LETTER = "getSender";
	private static final String GET_ATTACHMENTS_LETTER = "getAttachments";
	private static final String GET_DESCRIPTION_LETTER = "getDescription";
	
	private static final String GET_ATTACHMENT_ATTACHMENTS = "getAttachment";
	
	private static final String GET_GUID = "getGuid";
	private static final String GET_NAME = "getName";
	private static final String GET_VALUE = "getValue";
	
	private static Class<?> clazzLetter = null;
	private static Object objLetter = null;
	private String packageLetter = null;

	private Letter(String packageLetter) {
		this.packageLetter = packageLetter;
	}
	
	private Letter(Object obj) throws Exception {
    	clazzLetter = Class.forName(Package.getPackage().concat(CLAZZ_LETTER));
    	objLetter = obj;
    }
	
	public static Letter newInstance() throws Exception {
		String packageLetter = Package.getPackage();
		clazzLetter = Class.forName(packageLetter.concat(CLAZZ_LETTER));
		objLetter = clazzLetter.newInstance();
		return new Letter(packageLetter);
	}
	
	public void marshal(Writer writer) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(packageLetter);
		//Create marshaller
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		//Marshal object into writer.
		marshaller.marshal(objLetter, writer);
	}
	
	public Letter unmarshal(InputStream is) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(packageLetter);
		//Create unmarshaller
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object jaxbLetter = unmarshaller.unmarshal(is);
		return new Letter(jaxbLetter);
	}

	/**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
	 * @throws Exception  
     *     
     */
    public String getId() throws Exception {
    	Method getIdLetter = clazzLetter.getMethod(GET_ID_LETTER, new Class[] {});
    	Object id =  getIdLetter.invoke(objLetter, new Object[] {});
        return (null == id)?null:(String)id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     * @throws Exception 
     *     
     */
    public void setId(String value) throws Exception {
    	Method setIdLetter = clazzLetter.getMethod(SET_ID_LETTER, new Class[] {String.class});
    	setIdLetter.invoke(objLetter, new Object[] {value});
    }

    /**
     * Gets the value of the sender property.
     * 
     * @return
     *     possible object is
     *     {@link Letter.Sender }
     * @throws Exception 
     *     
     */
    public Letter.Sender getSender() throws Exception {
    	Method getSenderLetter = clazzLetter.getMethod(GET_SENDER_LETTER, new Class[] {});
    	Object sender =  getSenderLetter.invoke(objLetter, new Object[] {});
        return (null == sender)?null:new Letter.Sender(sender);
    }

    /**
     * Sets the value of the sender property.
     * 
     * @param value
     *     allowed object is
     *     {@link Letter.Sender }
     * @throws Exception 
     *     
     */
    @SuppressWarnings("static-access")
	public void setSender(Letter.Sender value) throws Exception {
    	Method setSenderLetter = clazzLetter.getMethod(SET_SENDER_LETTER, new Class[] {value.clazzSender});
    	setSenderLetter.invoke(objLetter, new Object[] {value.objSender});
    }

    /**
     * Gets the value of the addressee property.
     * 
     * @return
     *     possible object is
     *     {@link Letter.Addressee }
     * @throws Exception 
     *     
     */
    public Letter.Addressee getAddressee() throws Exception {
    	Method getAddresseeLetter = clazzLetter.getMethod(GET_ADDRESSEE_LETTER, new Class[] {});
    	Object addressee =  getAddresseeLetter.invoke(objLetter, new Object[] {});
        return (null == addressee)?null:new Letter.Addressee(addressee);
    }

    /**
     * Sets the value of the addressee property.
     * 
     * @param value
     *     allowed object is
     *     {@link Letter.Addressee }
     * @throws Exception  
     *     
     */
    @SuppressWarnings("static-access")
	public void setAddressee(Letter.Addressee value) throws Exception {
    	Method setAddresseeLetter = clazzLetter.getMethod(SET_ADDRESSEE_LETTER, new Class[] {value.clazzAddressee});
    	setAddresseeLetter.invoke(objLetter, new Object[] {value.objAddressee});
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     * @throws Exception 
     *     
     */
    public XMLGregorianCalendar getDate() throws Exception {
    	Method getDateLetter = clazzLetter.getMethod(GET_DATE_LETTER, new Class[] {});
    	Object date =  getDateLetter.invoke(objLetter, new Object[] {});
        return (null == date)?null:(XMLGregorianCalendar)date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     * @throws Exception 
     *     
     */
    public void setDate(XMLGregorianCalendar value) throws Exception {
    	Method setDateLetter = clazzLetter.getMethod(SET_DATE_LETTER, new Class[] {XMLGregorianCalendar.class});
    	setDateLetter.invoke(objLetter, new Object[] {value});
    }

    /**
     * Gets the value of the attachments property.
     * 
     * @return
     *     possible object is
     *     {@link Letter.Attachments }
     * @throws Exception 
     *     
     */
    public Letter.Attachments getAttachments() throws Exception {
    	Method getAttachmentsLetter = clazzLetter.getMethod(GET_ATTACHMENTS_LETTER, new Class[] {});
    	Object attachments =  getAttachmentsLetter.invoke(objLetter, new Object[] {});
        return (null == attachments)?null:new Letter.Attachments(attachments);
    }

    /**
     * Sets the value of the attachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Letter.Attachments }
     * @throws Exception 
     *     
     */
    @SuppressWarnings("static-access")
	public void setAttachments(Letter.Attachments value) throws Exception {
    	Method setAttachmentsLetter = clazzLetter.getMethod(SET_ATTACHMENTS_LETTER, new Class[] {value.clazzAttachments});
    	setAttachmentsLetter.invoke(objLetter, new Object[] {value.objAttachments});
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     * @throws Exception 
     *     
     */
    public String getDescription() throws Exception {
    	Method getDescriptionLetter = clazzLetter.getMethod(GET_DESCRIPTION_LETTER, new Class[] {});
    	Object description =  getDescriptionLetter.invoke(objLetter, new Object[] {});
        return (null == description)?null:(String)description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     * @throws Exception 
     *     
     */
    public void setDescription(String value) throws Exception {
    	Method setDescriptionLetter = clazzLetter.getMethod(SET_DESCRIPTION_LETTER, new Class[] {String.class});
    	setDescriptionLetter.invoke(objLetter, new Object[] {value});
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link LetterType }
     * @throws Exception 
     *     
     */
    public LetterType getType() throws Exception {
    	Method getTypeLetter = clazzLetter.getMethod(GET_TYPE_LETTER, new Class[] {});
    	Object type =  getTypeLetter.invoke(objLetter, new Object[] {});
    	Method nameLetterType =  type.getClass().getMethod("name", new Class[]{});
    	String name = (String)nameLetterType.invoke(type, new Object[]{});
        return (null == type)?null:LetterType.valueOf(name);
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link LetterType }
     * @throws Exception 
     *     
     */
    public void setType(LetterType value) throws Exception {
    	Method setTypeLetter = clazzLetter.getMethod(SET_TYPE_LETTER, new Class[] {value.clazzLetterType()});
    	setTypeLetter.invoke(objLetter, new Object[] {value.getValue()});
    }
    
    /**
     * <p>Java class for anonymous complex type.
     */
    public static class Addressee {
    	
    	private static final String CLAZZ_ADDRESSEE = ".Letter$Addressee";
        
        private static Class<?> clazzAddressee = null;
        private static Object objAddressee = null;
        
        private Addressee() {
        }
        
        private Addressee(Object obj) throws Exception {
        	clazzAddressee = Class.forName(Package.getPackage().concat(CLAZZ_ADDRESSEE));
        	objAddressee = obj;
        }
        
        public static Addressee newInstance() throws Exception {
        	clazzAddressee = Class.forName(Package.getPackage().concat(CLAZZ_ADDRESSEE));
        	objAddressee = clazzAddressee.newInstance();
        	return new Addressee();
        }
        
        public Addressee init(String guid, String name, String value) throws Exception {
        	this.setGuid(guid);
        	this.setName(name);
        	this.setValue(value);
        	return this;
        }

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public String getValue() throws Exception {
        	Method getValueAddressee = clazzAddressee.getMethod(GET_VALUE, new Class[]{});
        	Object value = getValueAddressee.invoke(objAddressee, new Object[]{});
        	return (null == value)?null:(String)value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public void setValue(String value) throws Exception {
        	Method setValueAddressee = clazzAddressee.getMethod(SET_VALUE, new Class[]{String.class});
			setValueAddressee.invoke(objAddressee, new Object[]{value});
        }

        /**
         * Gets the value of the guid property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public String getGuid() throws Exception {
        	Method getGuidAddressee = clazzAddressee.getMethod(GET_GUID, new Class[]{});
			Object guid = getGuidAddressee.invoke(objAddressee, new Object[]{});
			return (null == guid)?null:(String)guid;
        }

        /**
         * Sets the value of the guid property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public void setGuid(String value) throws Exception {
        	Method setGuidAddressee = clazzAddressee.getMethod(SET_GUID, new Class[]{String.class});
			setGuidAddressee.invoke(objAddressee, new Object[]{value});
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public String getName() throws Exception {
        	Method getNameAddressee = clazzAddressee.getMethod(GET_NAME, new Class[]{});
			Object name = getNameAddressee.invoke(objAddressee, new Object[]{});
			return (null == name)?null:(String)name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public void setName(String value) throws Exception {
        	Method setNameAddressee = clazzAddressee.getMethod(SET_NAME, new Class[]{String.class});
			setNameAddressee.invoke(objAddressee, new Object[]{value});
        }
    }
    
    public static class Attachments {
    	
    	private static final String CLAZZ_ATTACHMENTS = ".Letter$Attachments";
    	
    	private static Class<?> clazzAttachments = null;
    	private static Object objAttachments = null;
    	
    	private Attachments() {
    	}
    	
    	private Attachments(Object obj) throws Exception {
    		clazzAttachments  = Class.forName(Package.getPackage().concat(CLAZZ_ATTACHMENTS));
    		objAttachments = obj;
    	}
    	
    	public static Attachments newInstance() throws Exception {
    		clazzAttachments  = Class.forName(Package.getPackage().concat(CLAZZ_ATTACHMENTS));
    		objAttachments = clazzAttachments.newInstance();
			return new Attachments();
    	}

    	private final List<Letter.Attachments.Attachment> attachment = new ArrayList<Letter.Attachments.Attachment>();

        /**
         * Gets the value of the attachment property.
         * 
         * Objects of the following type(s) are allowed in the list
         * {@link Letter.Attachments.Attachment }
         * @throws Exception 
         * 
         * 
         */
        @SuppressWarnings("unchecked")
		public List<Letter.Attachments.Attachment> getAttachments() throws Exception {
            Method getAttachmentAttachments = clazzAttachments.getMethod(GET_ATTACHMENT_ATTACHMENTS, new Class[]{});
            List<Object> listAttachment = (List<Object>)getAttachmentAttachments.invoke(objAttachments, new Object[]{});
            for (Object obj:listAttachment) {
            	this.attachment.add(new Attachment(obj));
            }
            return Collections.unmodifiableList(this.attachment);
        }
        
        @SuppressWarnings({"unchecked", "static-access" })
		public void addAll(List<Letter.Attachments.Attachment> attachment) throws Exception {
        	if(null == attachment) 
        		return;
        	Method getAttachmentAttachments = clazzAttachments.getMethod(GET_ATTACHMENT_ATTACHMENTS, new Class[]{});
        	List<Object> listAttachment = (List<Object>)getAttachmentAttachments.invoke(objAttachments, new Object[]{});
        	for(Letter.Attachments.Attachment element:attachment) {
        		listAttachment.add(element.objAttachment);
        	}
        	this.attachment.addAll(attachment);
        }
        
        @SuppressWarnings({ "unchecked", "static-access" })
		public boolean add(Letter.Attachments.Attachment element) throws Exception {
        	if(null == element) 
        		return false;
        	Method getAttachmentAttachments = clazzAttachments.getMethod(GET_ATTACHMENT_ATTACHMENTS, new Class[]{});
        	List<Object> listAttachment = (List<Object>)getAttachmentAttachments.invoke(objAttachments, new Object[]{});
        	return listAttachment.add(element.objAttachment) && this.attachment.add(element);
        }
        
        @SuppressWarnings("unchecked")
		public void clear() throws Exception {
        	Method getAttachmentAttachments = clazzAttachments.getMethod(GET_ATTACHMENT_ATTACHMENTS, new Class[]{});
        	List<Object> listAttachment = (List<Object>)getAttachmentAttachments.invoke(objAttachments, new Object[]{});
        	listAttachment.clear();
    		this.attachment.clear();
        }
        
        /**
         * <p>Java class for anonymous complex type.
         */
        public static class Attachment {
        	
        	private static final String CLAZZ_ATTACHMENT = ".Letter$Attachments$Attachment";
        	
        	private static Class<?> clazzAttachment = null;
        	private static Object objAttachment = null;
        	
        	private Attachment() {
        	}

        	private Attachment(Object obj) throws Exception {
        		clazzAttachment  = Class.forName(Package.getPackage().concat(CLAZZ_ATTACHMENT));
        		objAttachment = obj;
        	}
        	
        	public static Attachment newInstance() throws Exception {
        		clazzAttachment  = Class.forName(Package.getPackage().concat(CLAZZ_ATTACHMENT));
        		objAttachment = clazzAttachment.newInstance();
        		return new Attachment();
        	}
        	
        	public void init(String name, String value) throws Exception {
            	this.setName(name);
            	this.setValue(value);
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             * @throws Exception 
             *     
             */
            public String getValue() throws Exception {
            	Method getValueAttachment = clazzAttachment.getMethod(GET_VALUE, new Class[]{});
            	Object value = getValueAttachment.invoke(objAttachment, new Object[]{});
            	return (null == value)?null:(String)value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             * @throws Exception 
             *     
             */
            public void setValue(String value) throws Exception {
            	Method setValueAttachment = clazzAttachment.getMethod(SET_VALUE, new Class[]{String.class});
            	setValueAttachment.invoke(objAttachment, new Object[]{value});
            }

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             * @throws Exception 
             *     
             */
            public String getName() throws Exception {
            	Method getNameAttachment = clazzAttachment.getMethod(GET_NAME, new Class[]{});
    			Object name = getNameAttachment.invoke(objAttachment, new Object[]{});
    			return (null == name)?null:(String)name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             * @throws Exception 
             *     
             */
            public void setName(String value) throws Exception {
            	Method setNameAttachment = clazzAttachment.getMethod(SET_NAME, new Class[]{String.class});
            	setNameAttachment.invoke(objAttachment, new Object[]{value});
            }

        }
    }
    
    /**
     * <p>Java class for anonymous complex type.
     */
    public static class Sender {
    	
    	private static final String CLAZZ_SENDER = ".Letter$Sender";
    	
    	private static Class<?> clazzSender = null;
    	private static Object objSender =null;
    	
    	private Sender() {	
    	}
    	
    	private Sender(Object obj) throws Exception {
    		clazzSender = Class.forName(Package.getPackage().concat(CLAZZ_SENDER));
    		objSender = obj;
    	}
    	
    	public static Sender newInstance() throws Exception {
    		clazzSender = Class.forName(Package.getPackage().concat(CLAZZ_SENDER));
    		objSender = clazzSender.newInstance();
    		return new Sender();
    	}
    	
    	public Sender init(String guid, String name, String value) throws Exception {
        	this.setGuid(guid);
        	this.setName(name);
        	this.setValue(value);
        	return this;
        }

    	/**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public String getValue() throws Exception {
        	Method getValueSender = clazzSender.getMethod(GET_VALUE, new Class[]{});
        	Object value = getValueSender.invoke(objSender, new Object[]{});
        	return (null == value)?null:(String)value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public void setValue(String value) throws Exception {
        	Method setValueSender = clazzSender.getMethod(SET_VALUE, new Class[]{String.class});
			setValueSender.invoke(objSender, new Object[]{value});
        }

        /**
         * Gets the value of the guid property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public String getGuid() throws Exception {
        	Method getGuidSender = clazzSender.getMethod(GET_GUID, new Class[]{});
			Object guid = getGuidSender.invoke(objSender, new Object[]{});
			return (null == guid)?null:(String)guid;
        }

        /**
         * Sets the value of the guid property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public void setGuid(String value) throws Exception {
        	Method setGuidSender = clazzSender.getMethod(SET_GUID, new Class[]{String.class});
			setGuidSender.invoke(objSender, new Object[]{value});
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public String getName() throws Exception {
        	Method getNameSender = clazzSender.getMethod(GET_NAME, new Class[]{});
			Object name = getNameSender.invoke(objSender, new Object[]{});
			return (null == name)?null:(String)name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         * @throws Exception 
         *     
         */
        public void setName(String value) throws Exception {
        	Method setNameSender = clazzSender.getMethod(SET_NAME, new Class[]{String.class});
			setNameSender.invoke(objSender, new Object[]{value});
        }
    }
}