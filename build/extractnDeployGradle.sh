#!/bin/bash
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


function testError {
  if [ "$result" -ne 0 ] 
  then
      echo $errorMsg; 
      echo $result;
      echo "dirname here";
      echo $dirname;
      exit 1
  fi
}

echo $1
if [ -z $1 ]
then
    echo "No any file specified"; exit 1
fi

cd $2/Scripts 

#unzip
dirname=`echo $1 | sed -e 's/.tar.gz//g'`
mkdir -p $dirname
tar xvzf $1 -C $dirname
result=$?
errorMsg="Error while unpacking"
testError


#copy
cd $dirname/app
JBDIR=$2/server/default/
mkdir -p $JBDIR
cp -vrf * $JBDIR
result=$?
errorMsg="Error while copy"
testError

#remove
cd ../..
rm -frv $dirname
rm -frv $1