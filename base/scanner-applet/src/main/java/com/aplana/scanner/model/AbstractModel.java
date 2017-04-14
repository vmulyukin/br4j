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
package com.aplana.scanner.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract Swing model object that supports notifications on updates of its properties.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public abstract class AbstractModel {
	protected final PropertyChangeSupport propertyChangeSupport;

	/**
	 * Default constructor.
	 */
	public AbstractModel() {
		propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	/**
	 * Adds a <code>PropertyChangeListener</code> to the listener list. The same listener object may
	 * be added more than once, and will be called as many times as it is added. If
	 * <code>listener</code> is <code>null</code>, no exception is thrown and no action is taken.
	 *
	 * @param listener  the <code>PropertyChangeListener</code> to be added
	 * 
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	/**
	 * Adds a <code>PropertyChangeListener</code> for a specific property. The listener will be
	 * invoked only when a call on {@link #firePropertyChange} names that specific property. The same
	 * listener object may be added more than once, and will be called the number of times it was
	 * added for that property. If <code>propertyName</code> or <code>listener</code> is
	 * <code>null</code>, no exception is thrown and no action is taken.
	 *
	 * @param property  the name of the property to listen on
	 * @param listener  the <code>PropertyChangeListener</code> to be added
	 * 
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Removes an <code>PropertyChangeListener</code> listener from the listener list. If a listener
	 * was added more than once to the same event source, it will be notified one less time after
	 * being removed. If <code>listener</code> is <code>null</code>, or was never added, no exception
	 * is thrown and no action is taken.
	 *
	 * @param listener  the <code>PropertyChangeListener</code> to be removed
	 * 
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	/**
	 * Removes an <code>PropertyChangeListener</code> listener for a specific property. If a listener
	 * was added more than once to the same event source for the specified property, it will be
	 * notified one less time after being removed. If <code>propertyName</code> is null, no exception
	 * is thrown and no action is taken. If <code>listener</code> is <code>null</code>, or was never
	 * added for the specified property, no exception is thrown and no action is taken.
	 *
	 * @param property  the name of the property that was listened on
	 * @param listener  the <code>PropertyChangeListener</code> to be removed
	 * 
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * Reports a bound property update to any registered listeners. No event is fired if old and new
	 * values are equal and non-<code>null</code>.
	 *
	 * @param propertyName  the programmatic name of the property that was changed
	 * @param oldValue      the old value of the property
	 * @param newValue      the new value of the property
	 * 
	 * @see java.beans.PropertyChangeSupport#firePropertyChange(String, Object, Object)
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
}
