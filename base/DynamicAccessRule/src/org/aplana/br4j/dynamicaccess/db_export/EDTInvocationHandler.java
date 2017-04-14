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
package org.aplana.br4j.dynamicaccess.db_export;

import javax.swing.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * EDT invocation handler. Intercepts calls to the interface and invokes methods in EDT
 * according to the {@link RequiresEDTPolicy}
 *
 * @author Eugene Matyushkin aka Skipy
 * http://skipy.ru/technics/gui_sync.html  
 */
public class EDTInvocationHandler implements InvocationHandler {

    /**
     * Method invocation result
     */
    private Object invocationResult = null;

    /**
     * Target object to translate method's call
     */
    private UICallback ui;

    /**
     * Creates invocation handler
     *
     * @param ui target object
     */
    public EDTInvocationHandler(UICallback ui) {
        this.ui = ui;
    }

    /**
     * Invokes method on target object. If {@link RequiresEDT} annotation present,
     * method is invoked in the EDT thread, otherwise - in current thread.
     *
     * @param proxy  proxy object
     * @param method method to invoke
     * @param args   method arguments
     * @return invocation result
     * @throws Throwable if error occures while calling method
     */
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        RequiresEDT mark = method.getAnnotation(RequiresEDT.class);
        if (mark != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                invocationResult = method.invoke(ui, args);
            } else {
                Runnable shell = new Runnable() {
                    public void run() {
                        try {
                            invocationResult = method.invoke(ui, args);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };
                if (RequiresEDTPolicy.ASYNC.equals(mark.value())) {
                    SwingUtilities.invokeLater(shell);
                } else {
                    SwingUtilities.invokeAndWait(shell);
                }
            }
        } else {
            invocationResult = method.invoke(ui, args);
        }
        return invocationResult;
    }
}