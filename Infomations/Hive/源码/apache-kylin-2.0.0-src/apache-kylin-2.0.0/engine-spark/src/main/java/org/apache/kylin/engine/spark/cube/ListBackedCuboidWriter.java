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
package org.apache.kylin.engine.spark.cube;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.kylin.engine.spark.SparkCuboidWriter;
import org.apache.kylin.gridtable.GTRecord;

import scala.Tuple2;

/**
 */
public class ListBackedCuboidWriter implements SparkCuboidWriter {

    private final ArrayList<Tuple2<byte[], byte[]>> result;
    private final TupleConverter tupleConverter;

    public ListBackedCuboidWriter(TupleConverter tupleConverter) {
        this.result = new ArrayList();
        this.tupleConverter = tupleConverter;
    }

    @Override
    public void write(long cuboidId, GTRecord record) throws IOException {
        result.add(tupleConverter.convert(cuboidId, record));
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    public Iterable<Tuple2<byte[], byte[]>> getResult() {
        return result;
    }
}
