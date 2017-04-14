/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
log4j {

appender.stdout = "org.apache.log4j.ConsoleAppender"
appender."stdout.layout"="org.apache.log4j.PatternLayout"
appender."stdout.Threshold"="ERROR"

appender.scrlog="org.apache.log4j.RollingFileAppender"
appender."scrlog.layout"="org.apache.log4j.PatternLayout"
appender."scrlog.Threshold"="DEBUG"
appender."scrlog.layout.ConversionPattern"="%d %-5r %-5p [%c] %m%n"
appender."scrlog.file"="iuh.log"
appender."scrlog.append"="true"
appender."scrlog.MaxFileSize"="10MB"
appender."scrlog.MaxBackupIndex"="10"

rootLogger="all,stdout,scrlog"
}