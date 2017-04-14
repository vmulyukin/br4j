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

Name:          br4j-loconverter
Version:       0.3
Release:       test
Summary:       br4j-loconverter automates conversions between office document formats using LibreOffice in br4j-app 
Vendor:        br4j-team
Distribution:  br4j-team
Packager:      Artur Gurov <agurov@it.ru>
URL:           http://br4j.datateh.ru
Source1:       loconverter
License:       GPL
Requires:      br4j-jboss-portal
Requires:      libreoffice
Requires:      libreoffice-headless

%description
br4j-loconverter automates conversions between office document formats using LibreOffice in app 

BuildRoot:     %{_tmppath}/%{name}-%{version}-build

%prep 

%install
[ "%{buildroot}" != / ] && rm -rf "%{buildroot}"
install -vd  %{buildroot}%{_sysconfdir}/init.d/
install -v -m755 %{SOURCE1} %{buildroot}%{_sysconfdir}/init.d/

%clean
[ "%{buildroot}" != / ] && rm -rf "%{buildroot}"

%post 

%preun 
#stopping loconverter
echo "Stopping loconverter service"
/sbin/service loconverter stop || echo "loconverter already stopped"
%postun 

%files 
%defattr(-,root,root)
%{_sysconfdir}/init.d/loconverter

%changelog
* Wed Mar 23 2015 Artur Gurov  <agurov@it.ru> 0.1-test
- initial version
