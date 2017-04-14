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

Name:          br4j-tsearch-dictionary
Version:       0.1
Release:       test
Summary:       provides customized tsearch dictionary for postgresql
Vendor:        br4j-team
Distribution:  br4j-team
Packager:      Artur Gurov <agurov@it.ru>
URL:           http://br4j.datateh.ru
License:       GPL
Source:        dicts.tar.gz
Requires:      postgresql94-server
Requires:      postgresql94-contrib
Requires:      postgresql94

%description
provides customized tsearch dictionary for postgresql

BuildRoot: %{_tmppath}/%{name}-%{version}-build

%prep 
%setup -q -T -b 0 -c -n %{name}

%install
[ "%{buildroot}" != / ] && rm -rf "%{buildroot}"
install -vd  %{buildroot}/usr/pgsql-9.4/share/tsearch_data/
install -v -m755 dicts/russian.affix %{buildroot}/usr/pgsql-9.4/share/tsearch_data/
install -v -m755 dicts/russian.dict %{buildroot}/usr/pgsql-9.4/share/tsearch_data/
install -v -m755 dicts/russian.stop %{buildroot}/usr/pgsql-9.4/share/tsearch_data/

%clean
[ "%{buildroot}" != / ] && rm -rf "%{buildroot}"

%files 
%defattr(-,root,root)
/usr/pgsql-9.4/share/tsearch_data/russian.stop
/usr/pgsql-9.4/share/tsearch_data/russian.dict
/usr/pgsql-9.4/share/tsearch_data/russian.affix
%changelog
* Wed Mar 23 2015 Artur Gurov  <agurov@it.ru> 0.1-test
- initial version
