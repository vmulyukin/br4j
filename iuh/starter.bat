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

@echo off
IF EXIST %TEMP%\harness_param_map (
  ECHO This directory contains harness_param_map file
  DEL %TEMP%\harness_param_map
) ELSE (
  ECHO This directory does not contains harness_param_map file
)

SET mypath=%~dp0
SET folder=%mypath:~0,-1%

@echo on

java -cp %folder%\iuh.jar;%GROOVY_HOME%\embeddable\groovy-all-2.2.2.jar;%GROOVY_HOME%\lib\ant-1.9.2.jar;%GROOVY_HOME%\lib\ant-launcher-1.9.2.jar;%GROOVY_HOME%\lib\commons-cli-1.2.jar ru.datateh.jbr.iuh.Main

@echo off
IF EXIST %TEMP%\harness_param_map (
  ECHO This directory contains harness_param_map file
  DEL %TEMP%\harness_param_map
) ELSE (
  ECHO This directory does not contains harness_param_map file
)
@echo on

pause