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
package com.aplana.medo.cards;

import com.aplana.medo.MedoException;

/**
 * An <code>CardException</code> exception is thrown when some error is
 * occurred during card fetch or creation according to values given by MEDO
 * document.
 */
public class CardException extends MedoException {

    private static final long serialVersionUID = -7106922487040873921L;

    public CardException() {
	super();
    }

    public CardException(Throwable cause) {
	super(cause);
    }

    public CardException(String message) {
	super(message);
    }

    public CardException(String msgId, Object[] params) {
	super(msgId, params);
    }

    public CardException(String msgId, Object[] params, Throwable cause) {
	super(msgId, params, cause);
    }

    public CardException(String message, Throwable cause) {
	super(message, cause);
    }
}
