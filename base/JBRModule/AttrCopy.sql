--
--   Licensed to the Apache Software Foundation (ASF) under one or more
--   contributor license agreements.  See the NOTICE file distributed with
--   this work for additional information regarding copyright ownership.
--   The ASF licenses this file to you under the Apache License, Version 2.0
--   (the "License"); you may not use this file except in compliance with
--   the License.  You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--   Unless required by applicable law or agreed to in writing, software
--   distributed under the License is distributed on an "AS IS" BASIS,
--   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--   See the License for the specific language governing permissions and
--   limitations under the License.
--

-- ��������� ���� "��� ����������" ��� ���� �������� ����������, ��� ��� �� �����������
INSERT INTO attribute_value (card_id, attribute_code, value_id)
SELECT c.card_id, 'JBR_IMPL_KIND', 1111
FROM card c
LEFT JOIN attribute_value v ON c.card_id=v.card_id AND v.attribute_code='JBR_IMPL_KIND'
WHERE c.template_id=224 AND v.value_id IS null

-- ��������� ���� "��� ��������" ��� ���� �������� ����������, ��� ��� �� �����������
INSERT INTO attribute_value (card_id, attribute_code, value_id)
SELECT c.card_id, 'JBR_IMPL_TYPECONT', 1435
FROM card c
LEFT JOIN attribute_value v ON c.card_id=v.card_id AND v.attribute_code='JBR_IMPL_TYPECONT'
JOIN attribute_value f ON c.card_id=f.card_id AND f.attribute_code='JBR_IMPL_ONCONT' AND f.value_id=1432
WHERE c.template_id=224 AND v.value_id IS null

-- ����������� ���� "��� ����������" �� ��� �������� ������������ ��� ���������� ������ ��� ������������
INSERT INTO attribute_value (card_id, attribute_code, value_id)
SELECT c.number_value, 'JBR_C_IMPL_KIND', v.value_id
FROM attribute_value v
JOIN attribute_value c ON v.card_id=c.card_id AND c.attribute_code='JBR_IMPL_ACQUAINT'
WHERE v.attribute_code='JBR_IMPL_KIND';

-- ����������� ���� "��� ���������" �� ��� �������� ���������� ��� ���������� ������ ��� ������������
INSERT INTO attribute_value (card_id, attribute_code, value_id)
SELECT c.number_value, 'JBR_C_INFD_TYPEDOC', v.value_id
FROM attribute_value v
JOIN attribute_value c ON v.card_id=c.card_id AND c.attribute_code='JBR_SIGN_SIGNING'
WHERE v.attribute_code='JBR_INFD_TYPEDOC';
