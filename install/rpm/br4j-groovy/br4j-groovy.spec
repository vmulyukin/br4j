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

%define     groovy_root_dir /usr/br4j
%define     real_name groovy	
Name:           br4j-groovy
Version:        2.2.2
Release:        1
License:        See: http://groovy.codehaus.org/license.html
BuildRoot:      %{_tmppath}/%{real_name}-%{version}-build
Group:          Development/Languages/Groovy
Summary:        Contains the base system for executing groovy scripts.
SOurce:         http://dl.bintray.com/groovy/maven/groovy-binary-%{version}.zip
BuildArch:      noarch
BuildRequires:  unzip
Packager:       Artur Gurov <agurov@it.ru>
Conflicts:	groovy
Requires: java-1.6.0-openjdk-devel
%description
Groovy is an object-oriented programming language for the Java Platform as an 
alternative to the Java programming language. It can be viewed as a scripting 
language for the Java Platform, as it has features similar to those of Python, 
Ruby, Perl, and Smalltalk. In some contexts, the name JSR 241 is used as an 
alternate identifier for the Groovy language.

%prep
%setup -n %{real_name}-%{version}
rm bin/*.bat
rm -rf $RPM_BUILD_ROOT

%build

%install
install -d $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/lib
install -p lib/* $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/lib

install -d $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/conf
install -p conf/* $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/conf

install -d $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/embeddable
install -p embeddable/* $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/embeddable

install -d $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/bin
install -p bin/* $RPM_BUILD_ROOT/%{groovy_root_dir}/%{real_name}/bin
mkdir -p $RPM_BUILD_ROOT/usr/bin
for file in grape groovy groovy.icns groovy.ico groovyConsole groovyc groovydoc groovysh java2groovy startGroovy ; do
  ln -s %{groovy_root_dir}/%{real_name}/bin/$file $RPM_BUILD_ROOT/usr/bin
done

install -d $RPM_BUILD_ROOT/etc/profile.d
echo "export GROOVY_HOME=%{groovy_root_dir}/%{real_name}" >$RPM_BUILD_ROOT/etc/profile.d/groovy.sh
echo "setenv GROOVY_HOME %{groovy_root_dir}/%{real_name}" >$RPM_BUILD_ROOT/etc/profile.d/groovy.csh

%clean
rm -rf "$RPM_BUILD_ROOT"

%post
/sbin/ldconfig
export GROOVY_HOME="%{groovy_root_dir}/%{real_name}"

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)
/etc/profile.d/groovy.csh
/etc/profile.d/groovy.sh
/usr/bin/grape
/usr/bin/groovy
/usr/bin/groovy.icns
/usr/bin/groovy.ico
/usr/bin/groovyConsole
/usr/bin/groovyc
/usr/bin/groovydoc
/usr/bin/groovysh
/usr/bin/java2groovy
/usr/bin/startGroovy
/usr/br4j/%{real_name}/*


%changelog
* Wed Mar  11 2015 Artur Gurov <agurov@it.ru>
- First version 2.2.2
