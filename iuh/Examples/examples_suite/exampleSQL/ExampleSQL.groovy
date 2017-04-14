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
import ru.datateh.jbr.iuh.AbstractExecute

public class ExampleSQL extends AbstractExecute {
	
	public void install() {
        //выполнить sql-скрипт
		execSql("""insert into card (card_id, template_id, is_active, status_id)
						select 25, 224, 1, 103
						where not exists (select 1 from card where card_id = 25); \n
					 	insert into card (card_id, template_id, is_active, status_id)
						select 24, 864, 1, 206
						where not exists (select 1 from card where card_id = 24);""")
        //выполнить sql-скрипт из файла
        execSqlFromFile('qwerty.sql')
	}
	
	public static void main(String[] args) {
		new ExampleSQL().start()
	}
}
