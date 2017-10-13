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

package org.apache.kylin.storage.hbase.steps;

import java.util.ArrayList;
import java.util.List;

import org.apache.kylin.common.util.StringUtil;
import org.apache.kylin.cube.CubeInstance;
import org.apache.kylin.cube.CubeSegment;
import org.apache.kylin.engine.mr.CubingJob;
import org.apache.kylin.engine.mr.JobBuilderSupport;
import org.apache.kylin.engine.mr.common.AbstractHadoopJob;
import org.apache.kylin.engine.mr.common.BatchConstants;
import org.apache.kylin.engine.mr.common.HadoopShellExecutable;
import org.apache.kylin.engine.mr.common.MapReduceExecutable;
import org.apache.kylin.job.constant.ExecutableConstants;
import org.apache.kylin.job.execution.DefaultChainedExecutable;
import org.apache.kylin.storage.hbase.HBaseConnection;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class HBaseMRSteps extends JobBuilderSupport {

    public HBaseMRSteps(CubeSegment seg) {
        super(seg, null);
    }

    public void addSaveCuboidToHTableSteps(DefaultChainedExecutable jobFlow, String cuboidRootPath) {
        String jobId = jobFlow.getId();

        // calculate key distribution
        jobFlow.addTask(createRangeRowkeyDistributionStep(cuboidRootPath, jobId));
        // create htable step
        jobFlow.addTask(createCreateHTableStep(jobId));
        // generate hfiles step
        jobFlow.addTask(createConvertCuboidToHfileStep(jobId));
        // bulk load step
        jobFlow.addTask(createBulkLoadStep(jobId));
    }

    public MapReduceExecutable createRangeRowkeyDistributionStep(String cuboidRootPath, String jobId) {
        String inputPath = cuboidRootPath + (cuboidRootPath.endsWith("/") ? "" : "/") + "*";

        MapReduceExecutable rowkeyDistributionStep = new MapReduceExecutable();
        rowkeyDistributionStep.setName(ExecutableConstants.STEP_NAME_GET_CUBOID_KEY_DISTRIBUTION);
        StringBuilder cmd = new StringBuilder();

        appendMapReduceParameters(cmd);
        appendExecCmdParameters(cmd, BatchConstants.ARG_INPUT, inputPath);
        appendExecCmdParameters(cmd, BatchConstants.ARG_OUTPUT, getRowkeyDistributionOutputPath(jobId));
        appendExecCmdParameters(cmd, BatchConstants.ARG_CUBE_NAME, seg.getRealization().getName());
        appendExecCmdParameters(cmd, BatchConstants.ARG_JOB_NAME, "Kylin_Region_Splits_Calculator_" + seg.getRealization().getName() + "_Step");

        rowkeyDistributionStep.setMapReduceParams(cmd.toString());
        rowkeyDistributionStep.setMapReduceJobClass(RangeKeyDistributionJob.class);
        return rowkeyDistributionStep;
    }

    public HadoopShellExecutable createCreateHTableStep(String jobId) {
        return createCreateHTableStep(jobId, false);
    }

    public HadoopShellExecutable createCreateHTableStepWithStats(String jobId) {
        return createCreateHTableStep(jobId, true);
    }

    private HadoopShellExecutable createCreateHTableStep(String jobId, boolean withStats) {
        HadoopShellExecutable createHtableStep = new HadoopShellExecutable();
        createHtableStep.setName(ExecutableConstants.STEP_NAME_CREATE_HBASE_TABLE);
        StringBuilder cmd = new StringBuilder();
        appendExecCmdParameters(cmd, BatchConstants.ARG_CUBE_NAME, seg.getRealization().getName());
        appendExecCmdParameters(cmd, BatchConstants.ARG_SEGMENT_ID, seg.getUuid());
        appendExecCmdParameters(cmd, BatchConstants.ARG_PARTITION, getRowkeyDistributionOutputPath(jobId) + "/part-r-00000");
        appendExecCmdParameters(cmd, BatchConstants.ARG_STATS_ENABLED, String.valueOf(withStats));

        createHtableStep.setJobParams(cmd.toString());
        createHtableStep.setJobClass(CreateHTableJob.class);

        return createHtableStep;
    }

    public MapReduceExecutable createMergeCuboidDataStep(CubeSegment seg, List<CubeSegment> mergingSegments, String jobID, Class<? extends AbstractHadoopJob> clazz) {

        final List<String> mergingCuboidPaths = Lists.newArrayList();
        for (CubeSegment merging : mergingSegments) {
            mergingCuboidPaths.add(getCuboidRootPath(merging) + "*");
        }
        String formattedPath = StringUtil.join(mergingCuboidPaths, ",");
        String outputPath = getCuboidRootPath(jobID);

        MapReduceExecutable mergeCuboidDataStep = new MapReduceExecutable();
        mergeCuboidDataStep.setName(ExecutableConstants.STEP_NAME_MERGE_CUBOID);
        StringBuilder cmd = new StringBuilder();

        appendMapReduceParameters(cmd);
        appendExecCmdParameters(cmd, BatchConstants.ARG_CUBE_NAME, seg.getCubeInstance().getName());
        appendExecCmdParameters(cmd, BatchConstants.ARG_SEGMENT_ID, seg.getUuid());
        appendExecCmdParameters(cmd, BatchConstants.ARG_INPUT, formattedPath);
        appendExecCmdParameters(cmd, BatchConstants.ARG_OUTPUT, outputPath);
        appendExecCmdParameters(cmd, BatchConstants.ARG_JOB_NAME, "Kylin_Merge_Cuboid_" + seg.getCubeInstance().getName() + "_Step");

        mergeCuboidDataStep.setMapReduceParams(cmd.toString());
        mergeCuboidDataStep.setMapReduceJobClass(clazz);
        return mergeCuboidDataStep;
    }

    public MapReduceExecutable createConvertCuboidToHfileStep(String jobId) {
        String cuboidRootPath = getCuboidRootPath(jobId);
        String inputPath = cuboidRootPath + (cuboidRootPath.endsWith("/") ? "" : "/") + "*";

        MapReduceExecutable createHFilesStep = new MapReduceExecutable();
        createHFilesStep.setName(ExecutableConstants.STEP_NAME_CONVERT_CUBOID_TO_HFILE);
        StringBuilder cmd = new StringBuilder();

        appendMapReduceParameters(cmd);
        appendExecCmdParameters(cmd, BatchConstants.ARG_CUBE_NAME, seg.getRealization().getName());
        appendExecCmdParameters(cmd, BatchConstants.ARG_PARTITION, getRowkeyDistributionOutputPath(jobId) + "/part-r-00000_hfile");
        appendExecCmdParameters(cmd, BatchConstants.ARG_INPUT, inputPath);
        appendExecCmdParameters(cmd, BatchConstants.ARG_OUTPUT, getHFilePath(jobId));
        appendExecCmdParameters(cmd, BatchConstants.ARG_HTABLE_NAME, seg.getStorageLocationIdentifier());
        appendExecCmdParameters(cmd, BatchConstants.ARG_JOB_NAME, "Kylin_HFile_Generator_" + seg.getRealization().getName() + "_Step");

        createHFilesStep.setMapReduceParams(cmd.toString());
        createHFilesStep.setMapReduceJobClass(CubeHFileJob.class);
        createHFilesStep.setCounterSaveAs(",," + CubingJob.CUBE_SIZE_BYTES);

        return createHFilesStep;
    }

    public HadoopShellExecutable createBulkLoadStep(String jobId) {
        HadoopShellExecutable bulkLoadStep = new HadoopShellExecutable();
        bulkLoadStep.setName(ExecutableConstants.STEP_NAME_BULK_LOAD_HFILE);

        StringBuilder cmd = new StringBuilder();
        appendExecCmdParameters(cmd, BatchConstants.ARG_INPUT, getHFilePath(jobId));
        appendExecCmdParameters(cmd, BatchConstants.ARG_HTABLE_NAME, seg.getStorageLocationIdentifier());
        appendExecCmdParameters(cmd, BatchConstants.ARG_CUBE_NAME, seg.getRealization().getName());

        bulkLoadStep.setJobParams(cmd.toString());
        bulkLoadStep.setJobClass(BulkLoadJob.class);

        return bulkLoadStep;
    }

    public MergeGCStep createMergeGCStep() {
        MergeGCStep result = new MergeGCStep();
        result.setName(ExecutableConstants.STEP_NAME_GARBAGE_COLLECTION_HBASE);
        result.setOldHTables(getMergingHTables());
        return result;
    }

    public List<String> getMergingHTables() {
        final List<CubeSegment> mergingSegments = ((CubeInstance) seg.getRealization()).getMergingSegments((CubeSegment) seg);
        Preconditions.checkState(mergingSegments.size() > 1, "there should be more than 2 segments to merge, target segment " + seg);
        final List<String> mergingHTables = Lists.newArrayList();
        for (CubeSegment merging : mergingSegments) {
            mergingHTables.add(merging.getStorageLocationIdentifier());
        }
        return mergingHTables;
    }

    public List<String> getMergingHDFSPaths() {
        final List<CubeSegment> mergingSegments = ((CubeInstance) seg.getRealization()).getMergingSegments((CubeSegment) seg);
        Preconditions.checkState(mergingSegments.size() > 1, "there should be more than 2 segments to merge, target segment " + seg);
        final List<String> mergingHDFSPaths = Lists.newArrayList();
        for (CubeSegment merging : mergingSegments) {
            mergingHDFSPaths.add(getJobWorkingDir(merging.getLastBuildJobID()));
        }
        return mergingHDFSPaths;
    }

    public String getHFilePath(String jobId) {
        return HBaseConnection.makeQualifiedPathInHBaseCluster(getJobWorkingDir(jobId) + "/" + seg.getRealization().getName() + "/hfile/");
    }

    public String getRowkeyDistributionOutputPath(String jobId) {
        return HBaseConnection.makeQualifiedPathInHBaseCluster(getJobWorkingDir(jobId) + "/" + seg.getRealization().getName() + "/rowkey_stats");
    }

    public void addMergingGarbageCollectionSteps(DefaultChainedExecutable jobFlow) {
        String jobId = jobFlow.getId();

        jobFlow.addTask(createMergeGCStep());

        List<String> toDeletePaths = new ArrayList<>();
        toDeletePaths.addAll(getMergingHDFSPaths());

        HDFSPathGarbageCollectionStep step = new HDFSPathGarbageCollectionStep();
        step.setName(ExecutableConstants.STEP_NAME_GARBAGE_COLLECTION_HDFS);
        step.setDeletePaths(toDeletePaths);
        step.setJobId(jobId);

        jobFlow.addTask(step);
    }

    public void addCubingGarbageCollectionSteps(DefaultChainedExecutable jobFlow) {
        String jobId = jobFlow.getId();

        List<String> toDeletePaths = new ArrayList<>();
        toDeletePaths.add(getFactDistinctColumnsPath(jobId));

        HDFSPathGarbageCollectionStep step = new HDFSPathGarbageCollectionStep();
        step.setName(ExecutableConstants.STEP_NAME_GARBAGE_COLLECTION_HBASE);
        step.setDeletePaths(toDeletePaths);
        step.setJobId(jobId);

        jobFlow.addTask(step);
    }

}
