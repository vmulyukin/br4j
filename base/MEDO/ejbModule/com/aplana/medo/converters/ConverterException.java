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
package com.aplana.medo.converters;

import com.aplana.medo.MedoException;

/**
 * An <code>ConverterException</code> exception is thrown when some error is
 * occurred during initialization of <code>Converter</code> or error during
 * convert operation.
 * 
 * @see Converter
 */
public class ConverterException extends MedoException {

    private static final long serialVersionUID = -3832053650190348487L;

    public ConverterException() {
	super();
    }

    public ConverterException(Throwable cause) {
	super(cause);
    }

    public ConverterException(String message) {
	super(message);
    }

    public ConverterException(String msgId, Object[] params) {
	super(msgId, params);
    }

    public ConverterException(String msgId, Object[] params, Throwable cause) {
	super(msgId, params, cause);
    }

    public ConverterException(String message, Throwable cause) {
	super(message, cause);
    }
}
