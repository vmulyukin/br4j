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

# Clean up and create directories
scp jbr@172.16.125.71:/mnt/disk/ftp/public/br4j-portal/br4j-jboss-portal.tar.gz .
scp jbr@172.16.125.71:/mnt/disk/ftp/public/br4j-portal/postgresql-9.4-1201.jdbc4.jar .
for dir in BUILD RPMS SOURCES SPECS SRPMS
do
     [[ -d $dir ]] && rm -Rf $dir
       mkdir $dir
done

# Put our files in the right place
mv br4j-jboss-portal.tar.gz SOURCES/.
mv postgresql-9.4-1201.jdbc4.jar SOURCES/.
mv jboss_init_br4j.sh SOURCES/.
mv br4j-jboss-portal.spec SPECS/.
cp -r jboss-web.deployer SOURCES/jboss-web.deployer 
rm jboss-web.deployer -rf

# Create rpm in RPMS/noarch/
rpmbuild --define '_topdir '$(pwd) -bb SPECS/br4j-jboss-portal.spec

