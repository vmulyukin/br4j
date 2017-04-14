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

[[ -d rpmbuild ]] && rm -rvf rpmbuild
       mkdir rpmbuild

pushd .
cd rpmbuild
for dir in BUILD RPMS SOURCES SPECS SRPMS
do
     [[ -d $dir ]] && rm -Rf $dir
       mkdir $dir
done
# Put our files in the right place
popd 
mv rpmsrc/*.spec rpmbuild/SPECS/.
mv rpmsrc/*.* rpmbuild/SOURCES/.
# Create rpm in RPMS/noarch/
pushd .
cd rpmbuild
rpmbuild --define '_topdir '$(pwd) -bb SPECS/*.spec
popd
