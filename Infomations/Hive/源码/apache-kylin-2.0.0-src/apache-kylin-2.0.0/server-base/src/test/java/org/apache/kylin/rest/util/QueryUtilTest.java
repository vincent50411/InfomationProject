/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kylin.rest.util;

import org.apache.kylin.common.util.LocalFileMetadataTestCase;
import org.apache.kylin.rest.request.SQLRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QueryUtilTest extends LocalFileMetadataTestCase {
    
    @Before
    public void setUp() throws Exception {
        this.createTestMetadata();
    }

    @After
    public void after() throws Exception {
        this.cleanupTestMetadata();
    }
    
    @Test
    public void testMassageSql() {
        {
            SQLRequest sqlRequest = new SQLRequest();
            sqlRequest.setSql("select ( date '2001-09-28' + interval floor(1.2) day) from test_kylin_fact");
            String s = QueryUtil.massageSql(sqlRequest);
            Assert.assertEquals("select ( date '2001-09-28' + interval '1' day) from test_kylin_fact", s);
        }
        {
            SQLRequest sqlRequest = new SQLRequest();
            sqlRequest.setSql("select ( date '2001-09-28' + interval floor(2) month) from test_kylin_fact group by ( date '2001-09-28' + interval floor(2) month)");
            String s = QueryUtil.massageSql(sqlRequest);
            Assert.assertEquals("select ( date '2001-09-28' + interval '2' month) from test_kylin_fact group by ( date '2001-09-28' + interval '2' month)", s);
        }
    }

    @Test
    public void testKeywordDefaultDirtyHack() {
        {
            SQLRequest sqlRequest = new SQLRequest();
            sqlRequest.setSql("select * from DEFAULT.TEST_KYLIN_FACT");
            String s = QueryUtil.massageSql(sqlRequest);
            Assert.assertEquals("select * from \"DEFAULT\".TEST_KYLIN_FACT", s);
        }
    }
}
