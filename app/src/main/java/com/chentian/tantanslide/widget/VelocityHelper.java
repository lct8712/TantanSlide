package com.chentian.tantanslide.widget;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过每个触摸事件的坐标计算速度
 *
 * @author chentian
 */
public class VelocityHelper {

    private static final int MAX_RECORD_COUNT = 10;
    private static final int RECORD_COUNT_FOR_COMPUTE = 5;

    private List<Record> recordList;

    public VelocityHelper() {
        recordList = new ArrayList<>();
    }

    public void reset() {
        recordList.clear();
    }

    public void record(float x, float y) {
        recordList.add(new Record(x, y));
        if (recordList.size() > MAX_RECORD_COUNT * 2) {
            recordList = recordList.subList(recordList.size() - MAX_RECORD_COUNT, recordList.size());
        }
    }

    public float computeVelocity() {
        Pair<Record, Record> records = getRecords();
        if (records == null) {
            return .0f;
        }

        float dx = records.second.x - records.first.x;
        float dy = records.second.y - records.first.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance / (records.second.timestamp - records.first.timestamp);
    }

    public Pair<Float, Float> computeVelocityWithDirection() {
        Pair<Record, Record> records = getRecords();
        if (records == null) {
            return null;
        }

        float dx = records.second.x - records.first.x;
        float dy = records.second.y - records.first.y;
        long timestamp = records.second.timestamp - records.first.timestamp;
        return Pair.create(dx / timestamp, dy / timestamp);
    }

    private @Nullable Pair<Record, Record> getRecords() {
        if (recordList.size() <= 2) {
            return null;
        }

        int firstRecordIndex = recordList.size() - RECORD_COUNT_FOR_COMPUTE;
        firstRecordIndex = Math.max(firstRecordIndex, 0);
        Record firstRecord = recordList.get(firstRecordIndex);
        Record lastRecord = recordList.get(recordList.size() - 1);
        return Pair.create(firstRecord, lastRecord);
    }

    private static class Record {

        private long timestamp;

        private float x;

        private float y;

        private Record(float x, float y) {
            this.timestamp = System.currentTimeMillis();
            this.x = x;
            this.y = y;
        }
    }
}
