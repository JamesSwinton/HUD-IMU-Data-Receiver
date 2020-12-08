package com.zebra.jamesswinton.imudatareceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.zebra.jamesswinton.imudatareceiver.databinding.ActivityMainBinding;
import com.zebra.jamesswinton.imudatareceiver.pojos.RawImuData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Binding
    private ActivityMainBinding mDataBinding;

    // Intent Actions
    private static final String ACTION_REGISTER_IMU_CALLBACK = "com.zebra.hudinterface.REGISTER_IMU_CALLBACK";
    private static final String ACTION_UNREGISTER_IMU_CALLBACK = "com.zebra.hudinterface.UNREGISTER_IMU_CALLBACK";

    // Intent Extras
    private static final String EXTRA_IMU_EVENTS = "imu.events";
    private static final String EXTRA_SEND_RAW_IMU_DATA = "imu.send_raw_data";
    private static final String EXTRA_RAW_IMU_DATA_FREQUENCY = "imu.raw_data_frequency";

    // Intent Values
    private static final String[] EVENTS = { "UP", "DOWN", "LEFT", "RIGHT" };
    private static final boolean SEND_RAW_DATA = true;
    private static final int RAW_DATA_FREQUENCY = 100;

    // Receiver
    private static final IntentFilter mImuIntentFilter = new IntentFilter();
    private static final String ACTION_RAW_IMU_DATA = "com.zebra.hudinterface.RAW_IMU_DATA";
    private static final String ACTION_MOVEMENT_DETECTED_LEFT = "com.zebra.hudinterface.HUD_MOVEMENT_DETECTED_LEFT";
    private static final String ACTION_MOVEMENT_DETECTED_RIGHT = "com.zebra.hudinterface.HUD_MOVEMENT_DETECTED_RIGHT";
    private static final String ACTION_MOVEMENT_DETECTED_UP = "com.zebra.hudinterface.HUD_MOVEMENT_DETECTED_UP";
    private static final String ACTION_MOVEMENT_DETECTED_DOWN = "com.zebra.hudinterface.HUD_MOVEMENT_DETECTED_DOWN";

    // Media Players
    private MediaPlayer mLeftPlayer;
    private MediaPlayer mRightPlayer;
    private MediaPlayer mUpPlayer;
    private MediaPlayer mDownPlayer;

    // Adapter
    private ImuDataAdapter mImuDataAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Init Intent Filter
        mImuIntentFilter.addAction(ACTION_RAW_IMU_DATA);
        mImuIntentFilter.addAction(ACTION_MOVEMENT_DETECTED_LEFT);
        mImuIntentFilter.addAction(ACTION_MOVEMENT_DETECTED_RIGHT);
        mImuIntentFilter.addAction(ACTION_MOVEMENT_DETECTED_UP);
        mImuIntentFilter.addAction(ACTION_MOVEMENT_DETECTED_DOWN);

        // Init Adapter
        mImuDataAdapter = new ImuDataAdapter(this, new ArrayList<>());
        mDataBinding.imuDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.imuDataRecyclerView.setAdapter(mImuDataAdapter);

        // Init Sound Player
        mLeftPlayer = MediaPlayer.create(this, R.raw.left);
        mRightPlayer = MediaPlayer.create(this, R.raw.right);
        mUpPlayer = MediaPlayer.create(this, R.raw.up);
        mDownPlayer = MediaPlayer.create(this, R.raw.down);

        // Init Charts
        initCharts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(ImuDataReceiver, mImuIntentFilter);
        startImuDataCapture(EVENTS, SEND_RAW_DATA, RAW_DATA_FREQUENCY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopImuDataCapture();
    }

    private void startImuDataCapture(String[] events, boolean sendRawData, int rawDataFrequency) {
        Intent registerForImuData = new Intent();
        registerForImuData.setAction(ACTION_REGISTER_IMU_CALLBACK);
        registerForImuData.putExtra(EXTRA_IMU_EVENTS, events);
        registerForImuData.putExtra(EXTRA_SEND_RAW_IMU_DATA, sendRawData);
        registerForImuData.putExtra(EXTRA_RAW_IMU_DATA_FREQUENCY, rawDataFrequency);
        sendBroadcast(registerForImuData);
    }

    private void stopImuDataCapture() {
        Intent unregisterFromImuData = new Intent();
        unregisterFromImuData.setAction(ACTION_UNREGISTER_IMU_CALLBACK);
        sendBroadcast(unregisterFromImuData);
    }

    private final BroadcastReceiver ImuDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case ACTION_RAW_IMU_DATA:
                    RawImuData rawImuData = new RawImuData(intent);
                    mImuDataAdapter.addImuData(rawImuData);
                    mDataBinding.imuDataRecyclerView.scrollToPosition(mImuDataAdapter.getItemCount() -1);

                    updateAccelerometerChartData(rawImuData);
                    updateGyroscopeChartData(rawImuData);
                    updateMagnetometerData(rawImuData);
                    updateQuartChartData(rawImuData);
                    updateTempChartData(rawImuData);
                    break;
                case ACTION_MOVEMENT_DETECTED_LEFT:
                    Toast.makeText(MainActivity.this, "LEFT", Toast.LENGTH_SHORT).show();
                    mLeftPlayer.start();
                    break;
                case ACTION_MOVEMENT_DETECTED_RIGHT:
                    Toast.makeText(MainActivity.this, "RIGHT", Toast.LENGTH_SHORT).show();
                    mRightPlayer.start();
                    break;
                case ACTION_MOVEMENT_DETECTED_UP:
                    Toast.makeText(MainActivity.this, "UP", Toast.LENGTH_SHORT).show();
                    mUpPlayer.start();
                    break;
                case ACTION_MOVEMENT_DETECTED_DOWN:
                    Toast.makeText(MainActivity.this, "DOWN", Toast.LENGTH_SHORT).show();
                    mDownPlayer.start();
                    break;
            }
        }
    };

    private void updateAccelerometerChartData(RawImuData rawImuData) {
        // Get Data From Chart
        LineData data = mDataBinding.accelerometerChart.getData();

        // If we've never populated the chart, lets create some sets
        if (data != null) {

            // Create X Y Z Sets
            if (data.getDataSets().size() == 0) {
                data.addDataSet(createDataSetForX());
                data.addDataSet(createDataSetForY());
                data.addDataSet(createDataSetForZ());
            }

            // Get Data Sets
            ILineDataSet dataSetX = data.getDataSetByIndex(0);
            data.addEntry(new Entry(dataSetX.getEntryCount(), rawImuData.getAccelerometer().get(0).floatValue()), 0);
            ILineDataSet dataSetY = data.getDataSetByIndex(1);
            data.addEntry(new Entry(dataSetY.getEntryCount(), rawImuData.getAccelerometer().get(1).floatValue()), 1);
            ILineDataSet dataSetZ = data.getDataSetByIndex(2);
            data.addEntry(new Entry(dataSetZ.getEntryCount(), rawImuData.getAccelerometer().get(2).floatValue()), 2);

            // Add Data Set to Line Data
            data.notifyDataChanged();

            // Update Chart
            mDataBinding.accelerometerChart.notifyDataSetChanged();
            mDataBinding.accelerometerChart.setVisibleXRangeMaximum(150);
            mDataBinding.accelerometerChart.moveViewToX(data.getEntryCount());
        }
    }

    private void updateGyroscopeChartData(RawImuData rawImuData) {
        // Get Data From Chart
        LineData data = mDataBinding.gyroscopeChart.getData();

        // If we've never populated the chart, lets create some sets
        if (data != null) {

            // Create X Y Z Sets
            if (data.getDataSets().size() == 0) {
                data.addDataSet(createDataSetForX());
                data.addDataSet(createDataSetForY());
                data.addDataSet(createDataSetForZ());
            }

            // Get Data Sets
            ILineDataSet dataSetX = data.getDataSetByIndex(0);
            data.addEntry(new Entry(dataSetX.getEntryCount(), rawImuData.getGyroscope().get(0).floatValue()), 0);
            ILineDataSet dataSetY = data.getDataSetByIndex(1);
            data.addEntry(new Entry(dataSetY.getEntryCount(), rawImuData.getGyroscope().get(1).floatValue()), 1);
            ILineDataSet dataSetZ = data.getDataSetByIndex(2);
            data.addEntry(new Entry(dataSetZ.getEntryCount(), rawImuData.getGyroscope().get(2).floatValue()), 2);

            // Add Data Set to Line Data
            data.notifyDataChanged();

            // Update Chart
            mDataBinding.gyroscopeChart.notifyDataSetChanged();
            mDataBinding.gyroscopeChart.setVisibleXRangeMaximum(150);
            mDataBinding.gyroscopeChart.moveViewToX(data.getEntryCount());
        }
    }

    private void updateMagnetometerData(RawImuData rawImuData) {
        // Get Data From Chart
        LineData data = mDataBinding.magnetometerChart.getData();

        // If we've never populated the chart, lets create some sets
        if (data != null) {

            // Create X Y Z Sets
            if (data.getDataSets().size() == 0) {
                data.addDataSet(createDataSetForX());
                data.addDataSet(createDataSetForY());
                data.addDataSet(createDataSetForZ());
            }

            // Get Data Sets
            ILineDataSet dataSetX = data.getDataSetByIndex(0);
            data.addEntry(new Entry(dataSetX.getEntryCount(), rawImuData.getMagnetometer().get(0).floatValue()), 0);
            ILineDataSet dataSetY = data.getDataSetByIndex(1);
            data.addEntry(new Entry(dataSetY.getEntryCount(), rawImuData.getMagnetometer().get(1).floatValue()), 1);
            ILineDataSet dataSetZ = data.getDataSetByIndex(2);
            data.addEntry(new Entry(dataSetZ.getEntryCount(), rawImuData.getMagnetometer().get(2).floatValue()), 2);

            // Add Data Set to Line Data
            data.notifyDataChanged();

            // Update Chart
            mDataBinding.magnetometerChart.notifyDataSetChanged();
            mDataBinding.magnetometerChart.setVisibleXRangeMaximum(150);
            mDataBinding.magnetometerChart.moveViewToX(data.getEntryCount());
        }
    }

    private void updateQuartChartData(RawImuData rawImuData) {
        // Get Data From Chart
        LineData data = mDataBinding.quartChart.getData();

        // If we've never populated the chart, lets create some sets
        if (data != null) {

            // Create X Y Z Sets
            if (data.getDataSets().size() == 0) {
                data.addDataSet(createDataSetForX());
                data.addDataSet(createDataSetForY());
                data.addDataSet(createDataSetForZ());
            }

            // Get Data Sets
            ILineDataSet dataSetX = data.getDataSetByIndex(0);
            data.addEntry(new Entry(dataSetX.getEntryCount(), rawImuData.getQuaternionVector().get(0).floatValue()), 0);
            ILineDataSet dataSetY = data.getDataSetByIndex(1);
            data.addEntry(new Entry(dataSetY.getEntryCount(), rawImuData.getQuaternionVector().get(1).floatValue()), 1);
            ILineDataSet dataSetZ = data.getDataSetByIndex(2);
            data.addEntry(new Entry(dataSetZ.getEntryCount(), rawImuData.getQuaternionVector().get(2).floatValue()), 2);

            // Add Data Set to Line Data
            data.notifyDataChanged();

            // Update Chart
            mDataBinding.quartChart.notifyDataSetChanged();
            mDataBinding.quartChart.setVisibleXRangeMaximum(150);
            mDataBinding.quartChart.moveViewToX(data.getEntryCount());
        }
    }

    private void updateTempChartData(RawImuData rawImuData) {
        // Get Data From Chart
        LineData data = mDataBinding.tempChart.getData();

        // If we've never populated the chart, lets create some sets
        if (data != null) {

            // Create X Y Z Sets
            if (data.getDataSets().size() == 0) {
                data.addDataSet(createDataSetForX());
            }

            // Get Data Sets
            ILineDataSet dataSetX = data.getDataSetByIndex(0);
            data.addEntry(new Entry(dataSetX.getEntryCount(), (float) rawImuData.getTemp()), 0);

            // Add Data Set to Line Data
            data.notifyDataChanged();

            // Update Chart
            mDataBinding.tempChart.notifyDataSetChanged();
            mDataBinding.tempChart.setVisibleXRangeMaximum(150);
            mDataBinding.tempChart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createDataSetForX() {
        LineDataSet set = new LineDataSet(null, "X");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private LineDataSet createDataSetForY() {
        LineDataSet set = new LineDataSet(null, "Y");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.BLUE);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private LineDataSet createDataSetForZ() {
        LineDataSet set = new LineDataSet(null, "Z");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.YELLOW);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private void initCharts() {
        // Init Chart Data
        LineData accelerometerLineData = new LineData();
        accelerometerLineData.setValueTextColor(Color.WHITE);

        // Init Chart
        mDataBinding.accelerometerChart.setTouchEnabled(true);
        mDataBinding.accelerometerChart.setDragEnabled(true);
        mDataBinding.accelerometerChart.setScaleEnabled(true);
        mDataBinding.accelerometerChart.setDrawGridBackground(true);
        mDataBinding.accelerometerChart.setPinchZoom(true);
        mDataBinding.accelerometerChart.setBackgroundColor(Color.WHITE);
        mDataBinding.accelerometerChart.setData(accelerometerLineData);
        mDataBinding.accelerometerChart.getAxisLeft().setDrawGridLines(false);
        mDataBinding.accelerometerChart.getXAxis().setDrawGridLines(false);
        mDataBinding.accelerometerChart.setDrawBorders(false);

        // Init Chart Legend & Description
        Description description = mDataBinding.accelerometerChart.getDescription();
        description.setEnabled(true);
        description.setText("Accelerometer Data | Frequency: " + RAW_DATA_FREQUENCY + "ms");
        Legend accelerometerChartLegend = mDataBinding.accelerometerChart.getLegend();
        accelerometerChartLegend.setForm(Legend.LegendForm.LINE);
        accelerometerChartLegend.setTextColor(Color.BLACK);

        // Init Chart Axises
        XAxis xl = mDataBinding.accelerometerChart.getXAxis();
        xl.setDrawLabels(true);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mDataBinding.accelerometerChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(4f);
        leftAxis.setAxisMinimum(-4f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mDataBinding.accelerometerChart.getAxisRight();
        rightAxis.setEnabled(false);

        /*
          Gyro Chart
         */

        // Init Chart Data
        LineData gyroscopeLineData = new LineData();
        gyroscopeLineData.setValueTextColor(Color.WHITE);

        // Init Chart
        mDataBinding.gyroscopeChart.setTouchEnabled(true);
        mDataBinding.gyroscopeChart.setDragEnabled(true);
        mDataBinding.gyroscopeChart.setScaleEnabled(true);
        mDataBinding.gyroscopeChart.setDrawGridBackground(true);
        mDataBinding.gyroscopeChart.setPinchZoom(true);
        mDataBinding.gyroscopeChart.setBackgroundColor(Color.WHITE);
        mDataBinding.gyroscopeChart.setData(gyroscopeLineData);
        mDataBinding.gyroscopeChart.getAxisLeft().setDrawGridLines(false);
        mDataBinding.gyroscopeChart.getXAxis().setDrawGridLines(false);
        mDataBinding.gyroscopeChart.setDrawBorders(false);

        // Init Chart Legend & Description
        Description gyroDescription = mDataBinding.gyroscopeChart.getDescription();
        gyroDescription.setEnabled(true);
        gyroDescription.setText("Gyro Data | Frequency: " + RAW_DATA_FREQUENCY + "ms");
        Legend gyroscopeChartLegend = mDataBinding.gyroscopeChart.getLegend();
        gyroscopeChartLegend.setForm(Legend.LegendForm.LINE);
        gyroscopeChartLegend.setTextColor(Color.BLACK);

        // Init Chart Axises
        XAxis gyroxl = mDataBinding.gyroscopeChart.getXAxis();
        gyroxl.setDrawLabels(true);
        gyroxl.setTextColor(Color.BLACK);
        gyroxl.setDrawGridLines(true);
        gyroxl.setAvoidFirstLastClipping(true);
        gyroxl.setEnabled(true);

        YAxis gyroLeftAxis = mDataBinding.gyroscopeChart.getAxisLeft();
        gyroLeftAxis.setEnabled(true);
        gyroLeftAxis.setTextColor(Color.BLACK);
        gyroLeftAxis.setDrawGridLines(false);
        gyroLeftAxis.setAxisMaximum(360f);
        gyroLeftAxis.setAxisMinimum(-360f);
        gyroLeftAxis.setDrawGridLines(true);

        YAxis gyroRightAxis = mDataBinding.gyroscopeChart.getAxisRight();
        gyroRightAxis.setEnabled(false);

        /*
          Magnetometer Chart
         */

        // Init Chart Data
        LineData magnetomChartData = new LineData();
        magnetomChartData.setValueTextColor(Color.WHITE);

        // Init Chart
        mDataBinding.magnetometerChart.setTouchEnabled(true);
        mDataBinding.magnetometerChart.setDragEnabled(true);
        mDataBinding.magnetometerChart.setScaleEnabled(true);
        mDataBinding.magnetometerChart.setDrawGridBackground(true);
        mDataBinding.magnetometerChart.setPinchZoom(true);
        mDataBinding.magnetometerChart.setBackgroundColor(Color.WHITE);
        mDataBinding.magnetometerChart.setData(magnetomChartData);
        mDataBinding.magnetometerChart.getAxisLeft().setDrawGridLines(false);
        mDataBinding.magnetometerChart.getXAxis().setDrawGridLines(false);
        mDataBinding.magnetometerChart.setDrawBorders(false);

        // Init Chart Legend & Description
        Description magDescription = mDataBinding.magnetometerChart.getDescription();
        magDescription.setEnabled(true);
        magDescription.setText("Magnetometer Data | Frequency: " + RAW_DATA_FREQUENCY + "ms");
        Legend magChartLegend = mDataBinding.magnetometerChart.getLegend();
        magChartLegend.setForm(Legend.LegendForm.LINE);
        magChartLegend.setTextColor(Color.BLACK);

        // Init Chart Axises
        XAxis magxl = mDataBinding.magnetometerChart.getXAxis();
        magxl.setDrawLabels(true);
        magxl.setTextColor(Color.BLACK);
        magxl.setDrawGridLines(true);
        magxl.setAvoidFirstLastClipping(true);
        magxl.setEnabled(true);

        YAxis magLeftAxis = mDataBinding.magnetometerChart.getAxisLeft();
        magLeftAxis.setEnabled(true);
        magLeftAxis.setTextColor(Color.BLACK);
        magLeftAxis.setDrawGridLines(false);
        magLeftAxis.setAxisMaximum(2000f);
        magLeftAxis.setAxisMinimum(-2000f);
        magLeftAxis.setDrawGridLines(true);

        YAxis magRightAxis = mDataBinding.magnetometerChart.getAxisRight();
        magRightAxis.setEnabled(false);

        /*
          Quart Chart
         */

        // Init Chart Data
        LineData quartChartData = new LineData();
        quartChartData.setValueTextColor(Color.WHITE);

        // Init Chart
        mDataBinding.quartChart.setTouchEnabled(true);
        mDataBinding.quartChart.setDragEnabled(true);
        mDataBinding.quartChart.setScaleEnabled(true);
        mDataBinding.quartChart.setDrawGridBackground(true);
        mDataBinding.quartChart.setPinchZoom(true);
        mDataBinding.quartChart.setBackgroundColor(Color.WHITE);
        mDataBinding.quartChart.setData(quartChartData);
        mDataBinding.quartChart.getAxisLeft().setDrawGridLines(false);
        mDataBinding.quartChart.getXAxis().setDrawGridLines(false);
        mDataBinding.quartChart.setDrawBorders(false);

        // Init Chart Legend & Description
        Description quartDescription = mDataBinding.quartChart.getDescription();
        quartDescription.setEnabled(true);
        quartDescription.setText("Quaternion Vector Data | Frequency: " + RAW_DATA_FREQUENCY + "ms");
        Legend quartChartLegend = mDataBinding.quartChart.getLegend();
        quartChartLegend.setForm(Legend.LegendForm.LINE);
        quartChartLegend.setTextColor(Color.BLACK);

        // Init Chart Axises
        XAxis quartxl = mDataBinding.quartChart.getXAxis();
        quartxl.setDrawLabels(true);
        quartxl.setTextColor(Color.BLACK);
        quartxl.setDrawGridLines(true);
        quartxl.setAvoidFirstLastClipping(true);
        quartxl.setEnabled(true);

        YAxis quartLeftAxis = mDataBinding.quartChart.getAxisLeft();
        quartLeftAxis.setEnabled(true);
        quartLeftAxis.setTextColor(Color.BLACK);
        quartLeftAxis.setDrawGridLines(false);
        quartLeftAxis.setAxisMaximum(2f);
        quartLeftAxis.setAxisMinimum(-2f);
        quartLeftAxis.setDrawGridLines(true);

        YAxis quartRightAxis = mDataBinding.quartChart.getAxisRight();
        quartRightAxis.setEnabled(false);

        /*
          Temp Chart
         */

        // Init Chart Data
        LineData tempChartData = new LineData();
        tempChartData.setValueTextColor(Color.WHITE);

        // Init Chart
        mDataBinding.tempChart.setTouchEnabled(true);
        mDataBinding.tempChart.setDragEnabled(true);
        mDataBinding.tempChart.setScaleEnabled(true);
        mDataBinding.tempChart.setDrawGridBackground(true);
        mDataBinding.tempChart.setPinchZoom(true);
        mDataBinding.tempChart.setBackgroundColor(Color.WHITE);
        mDataBinding.tempChart.setData(tempChartData);
        mDataBinding.tempChart.getAxisLeft().setDrawGridLines(false);
        mDataBinding.tempChart.getXAxis().setDrawGridLines(false);
        mDataBinding.tempChart.setDrawBorders(false);

        // Init Chart Legend & Description
        Description tempDesc = mDataBinding.tempChart.getDescription();
        tempDesc.setEnabled(true);
        tempDesc.setText("Temp Data | Frequency: " + RAW_DATA_FREQUENCY + "ms");
        Legend tempLegend = mDataBinding.tempChart.getLegend();
        tempLegend.setForm(Legend.LegendForm.LINE);
        tempLegend.setTextColor(Color.BLACK);

        // Init Chart Axises
        XAxis tempxl = mDataBinding.tempChart.getXAxis();
        tempxl.setDrawLabels(true);
        tempxl.setTextColor(Color.BLACK);
        tempxl.setDrawGridLines(true);
        tempxl.setAvoidFirstLastClipping(true);
        tempxl.setEnabled(true);

        YAxis tempLeftAxis = mDataBinding.tempChart.getAxisLeft();
        tempLeftAxis.setEnabled(true);
        tempLeftAxis.setTextColor(Color.BLACK);
        tempLeftAxis.setDrawGridLines(false);
        tempLeftAxis.setAxisMaximum(45f);
        tempLeftAxis.setAxisMinimum(25f);
        tempLeftAxis.setDrawGridLines(true);

        YAxis tempRightAxis = mDataBinding.tempChart.getAxisRight();
        tempRightAxis.setEnabled(false);
    }

}