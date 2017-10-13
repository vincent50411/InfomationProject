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

package org.apache.kylin.job.constant;

/**
 */
public final class ExecutableConstants {

    private ExecutableConstants() {
    }

    public static final String YARN_APP_ID = "yarn_application_id";
    public static final String YARN_APP_URL = "yarn_application_tracking_url";
    public static final String MR_JOB_ID = "mr_job_id";
    public static final String HDFS_BYTES_WRITTEN = "hdfs_bytes_written";
    public static final String SOURCE_RECORDS_COUNT = "source_records_count";
    public static final String SOURCE_RECORDS_SIZE = "source_records_size";

    public static final int DEFAULT_SCHEDULER_INTERVAL_SECONDS = 60;

    public static final String STEP_NAME_BUILD_DICTIONARY = "Build Dimension Dictionary";
    public static final String STEP_NAME_CREATE_FLAT_HIVE_TABLE = "Create Intermediate Flat Hive Table";
    public static final String STEP_NAME_MATERIALIZE_HIVE_VIEW_IN_LOOKUP = "Materialize Hive View in Lookup Tables";
    public static final String STEP_NAME_FACT_DISTINCT_COLUMNS = "Extract Fact Table Distinct Columns";
    public static final String STEP_NAME_BUILD_BASE_CUBOID = "Build Base Cuboid";
    public static final String STEP_NAME_BUILD_IN_MEM_CUBE = "Build Cube In-Mem";
    public static final String STEP_NAME_BUILD_SPARK_CUBE = "Build Cube with Spark";
    public static final String STEP_NAME_BUILD_N_D_CUBOID = "Build N-Dimension Cuboid";
    public static final String STEP_NAME_GET_CUBOID_KEY_DISTRIBUTION = "Calculate HTable Region Splits";
    public static final String STEP_NAME_CREATE_HBASE_TABLE = "Create HTable";
    public static final String STEP_NAME_CONVERT_CUBOID_TO_HFILE = "Convert Cuboid Data to HFile";
    public static final String STEP_NAME_BULK_LOAD_HFILE = "Load HFile to HBase Table";
    public static final String STEP_NAME_MERGE_DICTIONARY = "Merge Cuboid Dictionary";
    public static final String STEP_NAME_MERGE_STATISTICS = "Merge Cuboid Statistics";
    public static final String STEP_NAME_SAVE_STATISTICS = "Save Cuboid Statistics";
    public static final String STEP_NAME_MERGE_CUBOID = "Merge Cuboid Data";
    public static final String STEP_NAME_UPDATE_CUBE_INFO = "Update Cube Info";
    public static final String STEP_NAME_HIVE_CLEANUP = "Hive Cleanup";
    public static final String STEP_NAME_KAFKA_CLEANUP = "Kafka Intermediate File Cleanup";
    public static final String STEP_NAME_GARBAGE_COLLECTION = "Garbage Collection";
    public static final String STEP_NAME_GARBAGE_COLLECTION_HBASE = "Garbage Collection on HBase";
    public static final String STEP_NAME_GARBAGE_COLLECTION_HDFS = "Garbage Collection on HDFS";
    public static final String STEP_NAME_REDISTRIBUTE_FLAT_HIVE_TABLE = "Redistribute Flat Hive Table";
    public static final String NOTIFY_EMAIL_TEMPLATE = "<div><b>Build Result of Job ${job_name}</b><pre><ul>" + "<li>Build Result: <b>${result}</b></li>" + "<li>Job Engine: ${job_engine}</li>" + "<li>Env: ${env_name}</li>" + "<li>Project: ${project_name}</li>" + "<li>Cube Name: ${cube_name}</li>" + "<li>Source Records Count: ${source_records_count}</li>" + "<li>Start Time: ${start_time}</li>" + "<li>Duration: ${duration}</li>" + "<li>MR Waiting: ${mr_waiting}</li>" + "<li>Last Update Time: ${last_update_time}</li>" + "<li>Submitter: ${submitter}</li>" + "<li>Error Log: ${error_log}</li>" + "</ul></pre><div/>";
}
