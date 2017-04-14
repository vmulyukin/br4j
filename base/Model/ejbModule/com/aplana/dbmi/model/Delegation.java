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
/**
 * 
 */
package com.aplana.dbmi.model;

import java.util.Calendar;
import java.util.Date;

import com.aplana.dbmi.service.DataException;

/**
 * @author IKuznetsov
 *
 */
public class Delegation extends DataObject {

    // ���� "� �" "��"
    Date startAt;
    Date endAt;
    //���� ��������
    Date createdAt;

	// ��� �����������
    ObjectId fromPersonId;

    // ���� ������������ 
    ObjectId toPersonId;
    
    //��������� �������������
    ObjectId creatorId;
    
    // ���� ����������� �������������� �������������:
    private Editable editable = Editable.NOT_SET;
    
    // ���� ���������� �������������
    private boolean active = true;
    
    //���� ��������� �������������
    private boolean hidden;
    
    public enum Editable {
    	NOT_SET, // ����������� �������������� �� ������
    	YES, // ���� ����������� ��������������
    	NO // ����������� �������������� ���
    }
    
    public void setEditable(Editable editable) {
    	this.editable = editable;
    }
    
    public Editable getEditable() {
    	return editable;
    }
    
    public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
    
	/**
	 * ��������� ���������� ������������� � ����������� �� �������
	 * + ������������� ��������� �������������
	 */
	private void updateActive(){
		Date now = new Date();
		active = (now != null) && ( this.startAt == null || this.startAt.before(now) ) &&	( this.endAt == null || this.endAt.after(now) );
		hidden = !((now != null) && ( this.endAt == null || this.endAt.after(now)));
	}
    
    public boolean isActive() {
		return active;
	}

	// @Override
    public void setId(long id) {
        super.setId(new ObjectId(Delegation.class, id));
    }
    
    /**
     * @return the startAt
     */
    public Date getStartAt() {
        return startAt;
    }

    /**
     * @param startAt the startAt to set
     */
    public void setStartAt(Date startAt) {
        this.startAt = startAt;
        updateActive();
    }

    /**
     * @return the endAt
     */
    public Date getEndAt() {
        return endAt;
    }

    /**
     * @param endAt the endAt to set
     */
    public void setEndAt(Date endAt) {
        this.endAt = endAt;
        updateActive();
    }

    /**
     * @return the fromPersonId
     */
    public ObjectId getFromPersonId() {
        return fromPersonId;
    }

    /**
     * @param fromPersonId the fromPersonId to set
     */
    public void setFromPersonId(ObjectId fromPersonId) {
        this.fromPersonId = fromPersonId;
    }
    
    public void setFromPersonId(long id) {
        setFromPersonId( (id <= 0) ? null : new ObjectId( Person.class, id));
    }
    
    public void setCreatorPersonId(long id) {
    	setCreatorId( (id <= 0) ? null : new ObjectId( Person.class, id));
    }

    /**
     * @return the toPersonId
     */
    public ObjectId getToPersonId() {
        return toPersonId;
    }

    /**
     * @param toPersonId the toPersonId to set
     */
    public void setToPersonId(ObjectId toPersonId) {
        this.toPersonId = toPersonId;
    }
    
    public void setToPersonId(long id) {
        setToPersonId( (id <= 0) ? null : new ObjectId( Person.class, id));
    }
    
    public void setCreatorId(ObjectId creatorId) {
    	this.creatorId = creatorId;
    }
    
    public ObjectId getCreatorId() {
    	return creatorId;
    }
    
    public void setPeriod(Date dateFrom, Date dateTo) throws DataException {
        if ( dateFrom != null && dateTo != null && dateFrom.after(dateTo))
            throw new DataException("delegate.period.invalid2", new Object[] {
                    dateFrom, dateTo
            } );

        this.startAt = dateFrom;
        if(dateTo != null) {
	    	Calendar calendar = Calendar.getInstance();
	    	calendar.setTime(dateTo);
	    	calendar.set(Calendar.HOUR_OF_DAY, 23);
	    	calendar.set(Calendar.MINUTE, 59);
	    	calendar.set(Calendar.SECOND, 59);
	    	calendar.set(Calendar.MILLISECOND, 999);
	    	dateTo = calendar.getTime();
    	}
        this.endAt = dateTo;
        
        updateActive();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endAt == null) ? 0 : endAt.hashCode());
        result = prime * result
                + ((fromPersonId == null) ? 0 : fromPersonId.hashCode());
        result = prime * result + ((startAt == null) ? 0 : startAt.hashCode());
        result = prime * result
                + ((toPersonId == null) ? 0 : toPersonId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Delegation))
            return false;
        Delegation other = (Delegation) obj;
        if (endAt == null) {
            if (other.endAt != null)
                return false;
        } else if (!endAt.equals(other.endAt))
            return false;
        if (fromPersonId == null) {
            if (other.fromPersonId != null)
                return false;
        } else if (!fromPersonId.equals(other.fromPersonId))
            return false;
        if (startAt == null) {
            if (other.startAt != null)
                return false;
        } else if (!startAt.equals(other.startAt))
            return false;
        if (toPersonId == null) {
            if (other.toPersonId != null)
                return false;
        } else if (!toPersonId.equals(other.toPersonId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Delegation [endAt=" + endAt + ", fromPersonId=" + fromPersonId
                + ", startAt=" + startAt + ", toPersonId=" + toPersonId + ", active ="+active+"]";
    }

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}
