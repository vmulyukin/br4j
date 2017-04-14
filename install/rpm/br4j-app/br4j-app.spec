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

# имя приложения
Name:         br4j-app
# версия -- номер версии до номера сборки и патча 
Version:      {version_here}
# релиз - номер сборки и патча
Release:      {release_here}
# группа пакета
Group:        Applications/Productivity
# лицензия 
License:      LGPL
# иия файла с артефактом 
Source0:      %{version}.%{release}.tar.gz
# целевая архитектурв 
BuildArch:    noarch
# путь до каталога сборки пакета
BuildRoot:    %{_tmppath}/%{name}-%{version}-%{release}-build

#
# ОПРЕДЕЛЕНИЕ ЗАВИСИМОСТЕЙ
#

Requires:     br4j-jboss-portal > 0.6
Requires:     br4j-ghostscript
Requires:     br4j-loconverter
Requires:     br4j-groovy
Requires:     gdb
# учитываем минорные версии
Requires:     postgresql94 >= 9.4

# краткое описание продукта
Summary:      br4j - document management system  

%description
# полное описание продукта
JBoss Portal provides an open source platform for hosting and serving a portal's Web interface, publishing and managing its content, and customizing its experience.


#
# ОПРЕДЕЛЕНИЕ ПЕРЕМЕННЫХ
#
# путь до общего каталога дистрибутива
%define _br4j_dir /opt/br4j

# путь до каталога оснастки (/opt/br4j/iuh)
%define _br4j_iuh_dir %{_br4j_dir}/iuh

# путь до каталога пользовательского файла ответов
%define _br4j_answers_dir /opt/br4j-answers

# путь до каталога jboss-portal (/opt/br4j/jboss-portal)
%define _br4j_portal_dir %{_br4j_dir}/jboss-portal

# путь до каталога конфигурации (/opt/br4j/jboss/portal/server/default)
%define _br4j_jboss_conf_dir %{_br4j_portal_dir}/server/default

# путь до каталога данных
%define _br4j_data_dir %{_br4j_jboss_conf_dir}/data

# путь до каталога с конфигурационными файлами
%define _br4j_conf_dir %{_br4j_jboss_conf_dir}/conf/dbmi

# путь до каталога журналов
%define _br4j_log_dir %{_br4j_jboss_conf_dir}/log


%prep
#
# СЕКЦИЯ ПОДГОТОВКИ
#

# распаковка архива в каталог install
%setup -q -T -b 0 -c -n  %{name} 


%build
#
# СЕКЦИЯ СБОРКИ
#

%install
#
# СЕКЦИЯ УСТАНОВКИ
#
# На самом деле никакой установки не происходит. 
# В данной секции создается необходимая структура каталогов которая,
# в конечном итоге запакуется в пакет и при установке пакета будет развернута
# на клиентской машине

# очистка каталога установки
rm -rf %{buildroot}

# создание JBOSS_HOME (/opt/br4j/jboss-portal/server/default)
mkdir -p %{buildroot}/%{_br4j_jboss_conf_dir}

# установка содержимого артефакта поставки 
cp -r app/* %{buildroot}/%{_br4j_jboss_conf_dir}/

# установко каталога iuh
cp -r iuh %{buildroot}/%{_br4j_iuh_dir}

# установка пакетов 
cp -r packages_update %{buildroot}/%{_br4j_iuh_dir}/

# создание каталога для скриптов развертывания бд
mkdir -p %{buildroot}/%{_br4j_iuh_dir}/packages_update/01.FirstInstall/data/

# копирование скриптов развертывания бд
cp -r db/* %{buildroot}/%{_br4j_iuh_dir}/packages_update/01.FirstInstall/data/


%pre
#
# Подготовка к установке
#

# Скрипты из данной секции выполняются перед установкой/обновлением/удалением пакета

# если не первая установка то останавливаем jboss
if [ ! $1 -eq "1" ]; then 
echo "Checking jboss running"
/sbin/service jboss stop > /dev/null 2>&1 || \
    echo "jboss already stopped"
fi
# в обратном случае создаем пользователя и группу
getent passwd br4j_admin > /dev/null || \
    useradd -M -N -g br4j -r -d %{_br4j_dir} -s /bin/bash br4j_admin >/dev/null 2>&1


# Если существует файл oldconfig (т.е. необходимо провести миграцию), 
# то меняем группу и домашнюю директорию пользователя
if [ -e %{_br4j_answers_dir}/oldconfig ]; then
    usermod -g br4j -d /opt/br4j br4j_admin 
fi


%post
# Послеустановочные действия
# 
# Эти операции выполняются после распаковки содержимого RPM-пакета

# Если первая установка, то создаем необходимую структуру каталогов для данных 

if [ $1 -eq 1 ]
then
    mkdir -p %{_br4j_data_dir}/filestore/{root,null,cache}
    mkdir -p %{_br4j_data_dir}/gost/{in,processed,discarded,out}
    mkdir -p %{_br4j_data_dir}/dmsi/{in,processed,discarded}
    mkdir -p %{_br4j_data_dir}/medo/{in,processed,discarded,out,tickets,processedTickets}
    mkdir -p %{_br4j_data_dir}/materialsync/{inbox,bad}
    mkdir -p %{_br4j_data_dir}/owriter/data
    mkdir -p %{_br4j_data_dir}/loconverter/{system,temp}
    mkdir -p %{_br4j_data_dir}/replication/{in,out}
    mkdir -p %{_br4j_data_dir}/soz/{out,out_ok,out_fail,in}
    mkdir -p %{_br4j_jboss_conf_dir}/log/gsLogs
    chown -R jboss:br4j %{_br4j_data_dir}
    chown -R jboss:br4j %{_br4j_jboss_conf_dir}/log
fi


# функция запуска оснастки
run_iuh ()
{ 

# сообщаем в консоль что делаем

echo "Starting IUH-${1}"

# Переходим в каталог оснастки 
# Для того чтобы не указывать каталог с наборами

cd %{_br4j_iuh_dir}

# копируем нужный нам файл паспорта набора в файл паспорта оснастки 

cp %{_br4j_iuh_dir}/packages_update/update-set_${1}.id \
    %{_br4j_iuh_dir}/packages_update/update-set.id


if [ -e %{_br4j_answers_dir}/answer.properties ]

# при наличии пользовательского файла ответов указываем путь к нему 
# и запускаем оснастку

then
    ./starter.sh -Diuh.user.answers.file=%{_br4j_answers_dir}/answer.properties
else  
    ./starter.sh  
fi

if [ $(cat %{_br4j_iuh_dir}/update_state) != 'success' ]; then
    echo "There are errors during update."
    echo "Check %{_br4j_iuh_dir}/iuh.log"
    echo "You may run IUH manually with:"
    echo "%{_br4j_iuh_dir}/starter.sh -Diuh.update.set.path=%{_br4j_iuh_dir}/packages_update \\ "
    echo "-Diuh.user.answers.file=<your answers file>"
    exit 0
fi
}

if [ "$1" -eq 1 ];then

# если используется режим установки
# запускаем оснастку в режиме установки
# но не запускаем в случае миграции

    run_iuh install

fi 


# режим обновления запускается всегда

run_iuh update


%preun

# останавливаем jboss
if [ $1 -eq 0 ]; then
    
    /sbin/service jboss stop || echo "already stopped"

fi

%postun

# пользователь и группа не удаляются 
# чтобы не осталось файлов без владельца
# поскольку это может привести 
# к проблемам с безопасностью


%clean
rm -rf %{buildroot}

%files

%defattr(2755,jboss,br4j) 
%attr(2775,jboss,br4j) %{_br4j_jboss_conf_dir}
%attr(775,jboss,br4j) %{_br4j_iuh_dir}/
