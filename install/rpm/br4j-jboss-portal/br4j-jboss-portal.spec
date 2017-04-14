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

#имя приложения
Name:         br4j-jboss-portal
#версия -- оставляем оригинальную
Version:      2.6.8.GA
#релиз - тут наша версионность (не забываем увеличить при изменении пакета)
Release:      0.6
#OneString description 
Summary:      JBoss Portal open source platform for hosting and serving a portal's Web interface

Group:        Productivity/Networking/Web/Servers
License:      LGPL
Source0:      %{name}.tar.gz
Source1:      postgresql-9.4-1201.jdbc4.jar
Source2:      jboss_init_br4j.sh
BuildArch:    noarch
BuildRoot:    %{_tmppath}/%{name}-%{version}-%{release}-build
Requires:     java-1.6.0-openjdk
%define appname jboss-portal
%description
JBoss Portal provides an open source platform for hosting and serving a portal's Web interface, publishing and managing its content, and customizing its experience.

%prep
%setup -q -T -b 0 -n %{appname} 
cp %{SOURCE1} ./server/default/lib/
cp %{SOURCE2} ./bin/
cp %{_sourcedir}/jboss-web.deployer/conf/web.xml ./server/default/deploy/jboss-web.deployer/conf/
%build

%install
rm -rf %{buildroot}
# create JBOSS_HOME
mkdir -p %{buildroot}/opt/br4j/jboss-portal

# copy app to JBOSS_HOME
cp -r . %{buildroot}/opt/br4j/jboss-portal
mv %{buildroot}/opt/br4j/jboss-portal/server/default/deploy/jboss-portal.sar/portal-identity.sar/portal-identity.war/WEB-INF/jsf/admin/roles/roleMembers.xhtml %{buildroot}/opt/br4j/jboss-portal/server/default/deploy/jboss-portal.sar/portal-identity.sar/portal-identity.war/WEB-INF/jsf/admin/roles/roleMembers.xhtml.save 
mv %{buildroot}/opt/br4j/jboss-portal/server/default/deploy/jboss-portal.sar/portal-server.war/WEB-INF/web.xml %{buildroot}/opt/br4j/jboss-portal/server/default/deploy/jboss-portal.sar/portal-server.war/WEB-INF/web.xml.save
# startup scrips
mkdir -p %{buildroot}/etc/init.d
cp %{buildroot}/opt/br4j/jboss-portal/bin/jboss_init_br4j.sh %{buildroot}/etc/init.d/jboss
chmod 777 %{buildroot}/etc/init.d/jboss

%pre
if [ $1 -gt 1]; then  
echo "Checking jboss running"
/sbin/service jboss stop > /dev/null 2>&1 || echo "jboss already stopped"
else 
# Добавляем пользователя и группу
getent group br4j > /dev/null || \
    groupadd -r br4j >/dev/null 2>&1
getent passwd jboss > /dev/null || \
    useradd -M -N -g br4j -r -d /opt/br4j -s /bin/bash jboss >/dev/null 2>&1
fi
if [ -e /opt/br4j-answers/oldconfig ]; then
    # при наличии старой установки меняем имя init-скрипта
    # и меняем домашний каталог юзера jboss

    mv /etc/init.d/jboss /etc/init.d/jboss.old
    usermod -g br4j -d /opt/br4j/jboss-portal jboss 
fi
%post


%preun
# stop jboss-portal before delete or update
/sbin/service jboss stop >/dev/null 2>&1 || echo "Jboss already stopped"

%postun
# пользователь и группа не удаляются 
# чтобы не осталось файлов без владельца
# поскольку это может привести 
# к проблемам с безопасностью

%clean
rm -rf %{buildroot}

%files
%attr(-,root,root) /etc/init.d/jboss
%attr(775,jboss,br4j) /opt/br4j

%config(noreplace) /opt/br4j/jboss-portal/bin/run.conf
%config(noreplace) /opt/br4j/jboss-portal/server/default/conf/jboss-log4j.xml
