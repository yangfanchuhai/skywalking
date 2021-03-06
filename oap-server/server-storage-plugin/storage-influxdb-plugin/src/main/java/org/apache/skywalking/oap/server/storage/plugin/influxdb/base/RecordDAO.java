/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.storage.plugin.influxdb.base;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.skywalking.apm.commons.datacarrier.common.AtomicRangeInteger;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.manual.segment.SegmentRecord;
import org.apache.skywalking.oap.server.core.analysis.record.Record;
import org.apache.skywalking.oap.server.core.storage.IRecordDAO;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxClient;

public class RecordDAO implements IRecordDAO {
    public static final String TAG_SERVICE_ID = "_service_id";
    private static final int PADDING_SIZE = 1_000_000;
    private static final AtomicRangeInteger SUFFIX = new AtomicRangeInteger(0, PADDING_SIZE);

    private InfluxClient client;
    private StorageBuilder<Record> storageBuilder;

    public RecordDAO(InfluxClient client, StorageBuilder<Record> storageBuilder) {
        this.client = client;
        this.storageBuilder = storageBuilder;
    }

    @Override
    public InsertRequest prepareBatchInsert(Model model, Record record) throws IOException {
        final long timestamp = TimeBucket.getTimestamp(
            record.getTimeBucket(), model.getDownsampling()) * PADDING_SIZE + SUFFIX.getAndIncrement();

        return new InfluxInsertRequest(model, record, storageBuilder)
            .time(timestamp, TimeUnit.NANOSECONDS)
            .addFieldAsTag(SegmentRecord.SERVICE_ID, TAG_SERVICE_ID);
    }
}
