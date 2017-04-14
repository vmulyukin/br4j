/*
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
function encoderus(EntryTXT) {
  var text = "";
  var Ucode;
  var ExitValue;
  var s;

  for (var i=0; i<EntryTXT.length; i++)
  {
    s= EntryTXT.charAt(i);
    Ucode = s.charCodeAt(0);
    var Acode = Ucode;
    if (Ucode > 1039 && Ucode < 1104)
    {
      Acode -= 848;
      ExitValue = "%" + Acode.toString(16);
    }
    else
    if (Ucode == 1025)
    {
      Acode = 168;
      ExitValue = "%" + Acode.toString(16);
    }
    else
    if (Ucode == 1105)
    {
      Acode = 184;
      ExitValue = "%" + Acode.toString(16);
    }
    else
    if (Ucode == 32)
    {
      Acode = 32;
      ExitValue = "%" + Acode.toString(16);
    }
    else if(Ucode == 10)
    {
      Acode=10;
      ExitValue = "%0A";
    } else if(Ucode == 63)
    {
      Acode=63;
      ExitValue = "%" + Acode.toString(16);
    }else
     ExitValue=s;

  text = text + ExitValue;
  }
  return text;
}

function maillink(link, to, subj, body)
{

/*
  link.href="mailto:" + encodeURIComponent(to) + "?subject=" + encodeURIComponent(subj) + "&body=" + encodeURIComponent(body);
  link.href="mailto:" + encoderus(to) + "?subject=" + encoderus(subj) + "&body=" + encoderus(body);
  link.href="mailto:" + encoderus(to) + "?subject=" + encoderus(subj) + "&body=" + encoderus(body);
*/
  link.href="mailto:" + encoderus(to) + "?body=" + encoderus(body);
  
}
