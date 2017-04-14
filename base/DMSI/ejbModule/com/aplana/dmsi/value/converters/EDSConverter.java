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
package com.aplana.dmsi.value.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dmsi.types.EDS;
import com.aplana.dmsi.types.EDSIdKindEnumType;
import com.aplana.dmsi.value.controllers.StringValuePart;

public class EDSConverter implements Converter {
	
	protected final Log logger = LogFactory.getLog(EDSConverter.class);

    public Object convert(Object value) {
	if (value instanceof EDS) {
	    return convertEDSToString((EDS) value);
	} else if (value instanceof StringValuePart) {
	    return convertStringToEDS(((StringValuePart) value).getValue());
	} else {
	    throw new IllegalArgumentException("Unsupported type of argument: "
		    + value.getClass());
	}
    }

    private StringValuePart convertEDSToString(EDS eds) {
		try {
		    Root.Sign sign = new Root.Sign();
		    sign.setCertId(eds.getCertificate());
		    sign.setPkcs7(revert(eds.getValue()));
		    sign.setTime(eds.getDate());
		    if(eds.getCertificate_owner()!=null){
		    	sign.setDmsiInfo(eds.getCertificate_owner().replace(" ", "_"));
		    } else {
		    	sign.setDmsiInfo("");
		    }
		    ByteArrayOutputStream os = new ByteArrayOutputStream();
		    Marshaller marshaller = getContext().createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		    marshaller.marshal(sign, os);
		    return new StringValuePart(new String(os.toByteArray(), "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
		    throw new IllegalStateException(
			    "Error during converting EDS to String", ex);
		} catch (JAXBException ex) {
		    throw new IllegalStateException(
			    "Error during converting EDS to String", ex);
		}
    }

	private byte[] revert(byte[] buffer){
		byte[] result = null;
		if (buffer != null){
			result = new byte[buffer.length];
			
			for (int i=0; i<buffer.length; i++){
				result[i]=buffer[buffer.length-1-i];
			}
		}
		
		logger.debug(hexify(buffer));
		logger.debug(hexify(result));
		return result;
		//return buffer;
	}
	
	public String hexify(byte bytes[]) {

		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		StringBuffer buf = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}

		return buf.toString();
	}
	
        
    private List<EDS> convertStringToEDS(String value) {
	try {
	    Root root = (Root) getContext().createUnmarshaller().unmarshal(
		    new ByteArrayInputStream(String.format("<root>%s</root>",
			    value).getBytes("UTF-8")));
	    List<Root.Sign> signs = root.getSigns();
	    List<EDS> edses = new ArrayList<EDS>();
	    for (Root.Sign sign : signs) {
		EDS eds = new EDS();
		eds.setCertificate(sign.getCertId());
		eds.setDate(sign.getTime());
		eds.setValue(revert(sign.getPkcs7()));
		// FIXME: (N.Zhegalin) Probably more flexible mechanism should
		// be introduced
		eds.setKind("�� ���������");
		eds.setIdKind(EDSIdKindEnumType.NOT_DEFINED);
		edses.add(eds);
	    }
	    return edses;
	} catch (UnsupportedEncodingException ex) {
	    throw new IllegalStateException(
		    "Error during converting String to EDS", ex);
	} catch (JAXBException ex) {
	    throw new IllegalStateException(
		    "Error during converting String to EDS", ex);
	}
    }

    private JAXBContext getContext() throws JAXBException {
	return JAXBContext.newInstance(Root.class);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "root")
    @XmlType(name = "", propOrder = { "signs" })
    private static class Root {
	@XmlElement(name = "sign")
	private List<Sign> signs;

	public List<Sign> getSigns() {
	    if (this.signs == null) {
		this.signs = new ArrayList<Sign>();
	    }
	    return this.signs;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "sign")
	@XmlType(name = "", propOrder = { "card" })
	public static class Sign {

	    public Sign() {
	    }

	    @XmlAttribute
	    private String certId;
	    @XmlAttribute (name = "dmsiInfo")
	    private String dmsiInfo;
		@XmlAttribute
	    @XmlSchemaType(name = "dateTime")
	    private XMLGregorianCalendar time;
	    @XmlAttribute
	    @XmlSchemaType(name = "base64Binary")
	    private byte[] signature;
	    @XmlAttribute
	    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
	    @XmlSchemaType(name = "hexBinary")
	    private byte[] pkcs7;
	    @XmlElement(name = "card")
	    protected Card card = new Card();

	    public String getCertId() {
		return this.certId;
	    }

	    public void setCertId(String certId) {
		this.certId = certId;
	    }

	    public XMLGregorianCalendar getTime() {
		return this.time;
	    }

	    public void setTime(XMLGregorianCalendar time) {
		this.time = time;
	    }

	    public byte[] getSignature() {
		return this.signature;
	    }

	    public void setSignature(byte[] signature) {
		this.signature = signature;
	    }

	    public byte[] getPkcs7() {
		return this.pkcs7;
	    }

	    public void setPkcs7(byte[] pkcs7) {
		this.pkcs7 = pkcs7;
	    }
	    public String getDmsiInfo() {
			return dmsiInfo;
		}

		public void setDmsiInfo(String dmsiInfo) {
			this.dmsiInfo = dmsiInfo;
		}


	    @XmlAccessorType(XmlAccessType.FIELD)
	    @XmlType(name = "", propOrder = { "attr" })
	    private static class Card {
		public Card() {
		}

		@XmlAttribute
		protected String id = "0";
		@XmlElement(name = "attr")
		protected Attr attr = new Attr();

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "")
		private static class Attr {

		    public Attr() {
		    }

		    @XmlAttribute
		    protected String id = "MATERIAL";
		    @XmlAttribute
		    protected String type = "M";

		}
	    }

	}

    }

}
