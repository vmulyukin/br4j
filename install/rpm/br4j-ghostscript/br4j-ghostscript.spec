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

%define         _realname ghostscript
Name:          br4j-ghostscript
Version:       9.14
Release:       test
Summary:       The GPL release of the Ghostscript interpreter
Group:         Applications/Publishing
Vendor:        br4j-team
Distribution:  br4j-team
Packager:      Artur Gurov <agurov@it.ru>
URL:           http://www.ghostscript.com/awki
Source0:       %{_realname}-%{version}.tar.bz2
Source1:       ghostscript-fonts-std-8.11.tar.gz
Source2:       gnu-gs-fonts-other-6.0.tar.gz
License:       GPL
BuildRequires: gcc
BuildRequires: make
Conflicts: ghostscript 
%description
Ghostscript is a package of software that provides:
- An interpreter for the PostScript (TM) language, with the ability to convert PostScript language files to 
many raster formats, view them on displays, and print them on printers that don't have PostScript language 
capability built in;
- An interpreter for Portable Document Format (PDF) files, with the same abilities;
- The ability to convert PostScript language files to PDF (with some limitations) and vice versa; and
- A set of C procedures (the Ghostscript library) that implement the graphics and filtering (data 
compression / decompression / conversion) capabilities that appear as primitive operations in the PostScript 
language and in PDF.

BuildRoot:     %{_tmppath}/%{name}-%{version}-root

%prep
%setup -qcb0
%setup -TDqa1
%setup -TDqa2
cd ..
mv %{name}-%{version}/%{_realname}-%{version}/*  %{name}-%{version}/
rm %{name}-%{version}/%{_realname}-%{version}/ -rf

%build
%configure \
   --disable-compile-inits \
   --enable-dynamic 
make -j1 prefix=%{_prefix}
make -j1 so prefix=%{_prefix}

%pre 
for item in \
%{_bindir}/dvipdf \
%{_bindir}/eps2eps \
%{_bindir}/font2c \
%{_bindir}/gs \
%{_bindir}/gsbj \
%{_bindir}/gsc \
%{_bindir}/gsdj \
%{_bindir}/gsdj500 \
%{_bindir}/gslj \
%{_bindir}/gslp \
%{_bindir}/gsnd \
%{_bindir}/gsx \
%{_bindir}/lprsetup.sh \
%{_bindir}/pdf2dsc \
%{_bindir}/pdf2ps \
%{_bindir}/pf2afm \
%{_bindir}/pfbtopfa \
%{_bindir}/pphs \
%{_bindir}/printafm \
%{_bindir}/ps2ascii \
%{_bindir}/ps2epsi \
%{_bindir}/ps2pdf \
%{_bindir}/ps2pdf12 \
%{_bindir}/ps2pdf13 \
%{_bindir}/ps2pdf14 \
%{_bindir}/ps2pdfwr \
%{_bindir}/ps2ps \
%{_bindir}/ps2ps2 \
%{_bindir}/unix-lpr.sh \
%{_bindir}/wftopfa \
%{_sysconfdir}/profile.d/%{name}.sh \
%{_includedir}/%{_realname}/* \
%{_includedir}/ps \
%{_libdir}/libgs.so \
%{_libdir}/libgs.so.9 \
%{_libdir}/libgs.so.9.14 \
%{_datadir}/%{_realname}/%{version}/* \
%{_datadir}/doc/%{_realname}-%{version} \
%{_mandir}/de/man1/dvipdf.1.gz \
%{_mandir}/de/man1/eps2eps.1.gz \
%{_mandir}/de/man1/font2c.1.gz \
%{_mandir}/de/man1/gsnd.1.gz \
%{_mandir}/de/man1/pdf2dsc.1.gz \
%{_mandir}/de/man1/pdf2ps.1.gz \
%{_mandir}/de/man1/printafm.1.gz \
%{_mandir}/de/man1/ps2ascii.1.gz \
%{_mandir}/de/man1/ps2pdf.1.gz \
%{_mandir}/de/man1/ps2pdf12.1.gz \
%{_mandir}/de/man1/ps2pdf13.1.gz \
%{_mandir}/de/man1/ps2pdf14.1.gz \
%{_mandir}/de/man1/ps2ps.1.gz \
%{_mandir}/de/man1/wftopfa.1.gz \
%{_mandir}/man1/dvipdf.1.gz \
%{_mandir}/man1/eps2eps.1.gz \
%{_mandir}/man1/font2c.1.gz \
%{_mandir}/man1/gs.1.gz \
%{_mandir}/man1/gsbj.1.gz \
%{_mandir}/man1/gsdj.1.gz \
%{_mandir}/man1/gsdj500.1.gz \
%{_mandir}/man1/gslj.1.gz \
%{_mandir}/man1/gslp.1.gz \
%{_mandir}/man1/gsnd.1.gz \
%{_mandir}/man1/pdf2dsc.1.gz \
%{_mandir}/man1/pdf2ps.1.gz \
%{_mandir}/man1/pf2afm.1.gz \
%{_mandir}/man1/pfbtopfa.1.gz \
%{_mandir}/man1/printafm.1.gz \
%{_mandir}/man1/ps2ascii.1.gz \
%{_mandir}/man1/ps2epsi.1.gz \
%{_mandir}/man1/ps2pdf.1.gz \
%{_mandir}/man1/ps2pdf12.1.gz \
%{_mandir}/man1/ps2pdf13.1.gz \
%{_mandir}/man1/ps2pdf14.1.gz \
%{_mandir}/man1/ps2pdfwr.1.gz \
%{_mandir}/man1/ps2ps.1.gz \
%{_mandir}/man1/wftopfa.1.gz
do
[[ -e $item ]] && rm $item -rf
done
exit 0

%install
[ "%{buildroot}" != / ] && rm -rf "%{buildroot}"
make install DESTDIR=%{buildroot}
make soinstall DESTDIR=%{buildroot}
install -v -m644 base/*.h %{buildroot}%{_includedir}/ghostscript
mkdir -p %{buildroot}%{_datadir}/%{_realname}/%{version}/fonts
install -v -m644 fonts/* %{buildroot}%{_datadir}/%{_realname}/%{version}/fonts
ln -vfs ghostscript %{buildroot}%{_includedir}/ps
mkdir -p %{buildroot}%{_datadir}/doc
ln -sfv ../ghostscript/%{version}/doc %{buildroot}%{_datadir}/doc/ghostscript-%{version}
mkdir -p %{buildroot}/etc/profile.d/
echo "GS_FONTPATH=%{_datadir}/%{_realname}-%{version}/fonts/" > %{buildroot}/etc/profile.d/%{name}.sh

%clean
[ "%{buildroot}" != / ] && rm -rf "%{buildroot}"

%post -p /sbin/ldconfig

%postun -p /sbin/ldconfig

%files 
%defattr(-,root,root)
%{_bindir}/dvipdf
%{_bindir}/eps2eps
%{_bindir}/font2c
%{_bindir}/gs
%{_bindir}/gsbj
%{_bindir}/gsc
%{_bindir}/gsdj
%{_bindir}/gsdj500
%{_bindir}/gslj
%{_bindir}/gslp
%{_bindir}/gsnd
%{_bindir}/gsx
%{_bindir}/lprsetup.sh
%{_bindir}/pdf2dsc
%{_bindir}/pdf2ps
%{_bindir}/pf2afm
%{_bindir}/pfbtopfa
%{_bindir}/pphs
%{_bindir}/printafm
%{_bindir}/ps2ascii
%{_bindir}/ps2epsi
%{_bindir}/ps2pdf
%{_bindir}/ps2pdf12
%{_bindir}/ps2pdf13
%{_bindir}/ps2pdf14
%{_bindir}/ps2pdfwr
%{_bindir}/ps2ps
%{_bindir}/ps2ps2
%{_bindir}/unix-lpr.sh
%{_bindir}/wftopfa
%{_sysconfdir}/profile.d/%{name}.sh
%{_includedir}/%{_realname}/* 
%{_includedir}/ps 
%{_libdir}/libgs.so
%{_libdir}/libgs.so.9
%{_libdir}/libgs.so.9.14
%{_datadir}/%{_realname}/%{version}/*
%{_datadir}/doc/%{_realname}-%{version}
%{_mandir}/de/man1/dvipdf.1.gz
%{_mandir}/de/man1/eps2eps.1.gz
%{_mandir}/de/man1/font2c.1.gz
%{_mandir}/de/man1/gsnd.1.gz
%{_mandir}/de/man1/pdf2dsc.1.gz
%{_mandir}/de/man1/pdf2ps.1.gz
%{_mandir}/de/man1/printafm.1.gz
%{_mandir}/de/man1/ps2ascii.1.gz
%{_mandir}/de/man1/ps2pdf.1.gz
%{_mandir}/de/man1/ps2pdf12.1.gz
%{_mandir}/de/man1/ps2pdf13.1.gz
%{_mandir}/de/man1/ps2pdf14.1.gz
%{_mandir}/de/man1/ps2ps.1.gz
%{_mandir}/de/man1/wftopfa.1.gz
%{_mandir}/man1/dvipdf.1.gz
%{_mandir}/man1/eps2eps.1.gz
%{_mandir}/man1/font2c.1.gz
%{_mandir}/man1/gs.1.gz
%{_mandir}/man1/gsbj.1.gz
%{_mandir}/man1/gsdj.1.gz
%{_mandir}/man1/gsdj500.1.gz
%{_mandir}/man1/gslj.1.gz
%{_mandir}/man1/gslp.1.gz
%{_mandir}/man1/gsnd.1.gz
%{_mandir}/man1/pdf2dsc.1.gz
%{_mandir}/man1/pdf2ps.1.gz
%{_mandir}/man1/pf2afm.1.gz
%{_mandir}/man1/pfbtopfa.1.gz
%{_mandir}/man1/printafm.1.gz
%{_mandir}/man1/ps2ascii.1.gz
%{_mandir}/man1/ps2epsi.1.gz
%{_mandir}/man1/ps2pdf.1.gz
%{_mandir}/man1/ps2pdf12.1.gz
%{_mandir}/man1/ps2pdf13.1.gz
%{_mandir}/man1/ps2pdf14.1.gz
%{_mandir}/man1/ps2pdfwr.1.gz
%{_mandir}/man1/ps2ps.1.gz
%{_mandir}/man1/wftopfa.1.gz

%changelog
* Wed Mar 23 2015 Artur Gurov  <agurov@it.ru> 9.14-test
- update to 9.14
