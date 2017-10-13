--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

SELECT "TABLE1"."DIM1_1" "DIM1_1"
       ,"TABLE2"."DIM2_1" "DIM2_1"
       ,SUM("FACT"."M1") "M1"
       ,SUM("FACT"."M2") "M2"
  FROM ("COGNOS"."FACT" "FACT" LEFT OUTER JOIN "COGNOS"."TABLE1"
        "TABLE1" ON "FACT"."FK_1" = "TABLE1"."PK_1")
  LEFT OUTER JOIN "COGNOS"."TABLE2" "TABLE2"
    ON "FACT"."FK_2" = "TABLE2"."PK_2"
 GROUP BY "TABLE2"."DIM2_1"
          ,"TABLE1"."DIM1_1";
