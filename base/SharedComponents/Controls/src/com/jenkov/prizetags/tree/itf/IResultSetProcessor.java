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
    Copyright 2004 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



package com.jenkov.prizetags.tree.itf;

import java.sql.ResultSet;

/**
 * Implement this interface when you want to read a node from a result set, or
 * assist the built-in node reader with its job.
 *
 * <br/><br/>
 * NOTE: Still experimental
 *
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public interface IResultSetProcessor {

    /**
     * This method is called by the ResultSetTreeReader whenever a node
     * has been read from a record in the result set..
     * @param result The result set to read the node or other data from.
     * @param node The node that was read by the node reader. Null if node
     *             node was read automatically.
     */
    public void process(ResultSet result, ITreeNode node);
}
