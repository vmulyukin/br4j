@REM
@REM   Licensed to the Apache Software Foundation (ASF) under one or more
@REM   contributor license agreements.  See the NOTICE file distributed with
@REM   this work for additional information regarding copyright ownership.
@REM   The ASF licenses this file to you under the Apache License, Version 2.0
@REM   (the "License"); you may not use this file except in compliance with
@REM   the License.  You may obtain a copy of the License at
@REM
@REM       http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM   Unless required by applicable law or agreed to in writing, software
@REM   distributed under the License is distributed on an "AS IS" BASIS,
@REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM   See the License for the specific language governing permissions and
@REM   limitations under the License.
@REM

rem Данный скрипт позволяет сгенерировать клиент для Веб-сервиса GostStatisticService по сбору статистики сообщений ГОСТ

rem первым делом необходимо прописать путь до утилиты wsconsume, которая по умолчанию лежит в папке bin JBoss'а
set JBOSS_WSCONSUME_BIN="C:\jboss-portal-2.6.8.GA\bin\wsconsume.bat"

rem В этом параметре необходимо указать wsdl файл веб-серсива, который в нашем случае генерится во время деплоя сервиса на jboss с помещается в папку tmp\jbossws.
rem Необходимо указывать самый свежий файл, который соответствует нужной версии веб-сервиса.
set CRYPTO_SERVICE_WSDL_URL="C:\jboss-portal-2.6.8.GA\server\default\tmp\jbossws\GostStatisticServiceImplService6267926314189848000.wsdl"

%JBOSS_WSCONSUME_BIN% -k -t 2.1 -s ejbModule -o output -p com.aplana.dbmi.ws.goststatisticserviceproxy %CRYPTO_SERVICE_WSDL_URL% & rmdir /S /Q "output" & pause
