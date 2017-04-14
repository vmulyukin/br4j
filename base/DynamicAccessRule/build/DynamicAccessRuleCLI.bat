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

::cd c:\Project\BossRef\_Build\tool\DynamicAccessRule
::java -cp C:\Users\etarakanov\FSIN_base\DynamicAccessRule\build\jars/postgresql-9.0-801.jdbc3.jar;c:\Project\BossRef\_Build\tool\DynamicAccessRule\DynamicAccessRule.jar org.aplana.br4j.dynamicaccess.DynamicAccessCLI -command rcACL -url jdbc:postgresql://localhost:5432/fsin_boev1 -userName postgres -userPassword XSW@zaq1
java -cp jars/postgresql-9.0-801.jdbc3.jar;DynamicAccessRule.jar org.aplana.br4j.dynamicaccess.DynamicAccessCLI -h