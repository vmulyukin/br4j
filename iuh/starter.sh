#!/bin/bash
#
#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to you under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

GROOVYBIN=$(readlink -f $(which groovy))
export GROOVY_HOME=${GROOVY_HOME:-${GROOVYBIN%%/bin*}}

if [[ -z /tmp/harness_param_map ]]
then
rm /tmp/harness_param_map
fi

IUH_HOME=$(dirname $0)
java -cp $IUH_HOME/iuh.jar:${GROOVY_HOME}/embeddable/groovy-all-2.2.2.jar:$GROOVY_HOME/lib/ant-1.9.2.jar:$GROOVY_HOME/lib/ant-launcher-1.9.2.jar:$GROOVY_HOME/lib/commons-cli-1.2.jar ru.datateh.jbr.iuh.Main $*

if [[ $? -eq 1 ]];then
    echo "fail" > $IUH_HOME/update_state
else 
    echo "success" > $IUH_HOME/update_state
fi

if [[ -z /tmp/harness_param_map ]]
then
rm /tmp/harness_param_map
fi
