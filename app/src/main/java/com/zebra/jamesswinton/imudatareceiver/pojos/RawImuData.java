package com.zebra.jamesswinton.imudatareceiver.pojos;

import android.content.Intent;

import java.util.ArrayList;

public class RawImuData {

    public static final String EXTRA_ACCELEROMETER_DATA = "imu.raw_data.accelerometer";
    public static final String EXTRA_GYROSCOPE_DATA = "imu.raw_data.gyroscope";
    public static final String EXTRA_MAGNETOMETER_DATA = "imu.raw_data.magnetometer";
    public static final String EXTRA_QUATERNION_VECTOR_DATA = "imu.raw_data.quaternion_vector";
    public static final String EXTRA_TEMP_DATA = "imu.raw_data.temp";

    ArrayList<Double> accelerometer;
    ArrayList<Double> gyroscope;
    ArrayList<Double> magnetometer;
    ArrayList<Double> quaternionVector;
    double temp;

    public RawImuData(Intent intent) {
        this.accelerometer = (ArrayList<Double>) intent.getSerializableExtra(EXTRA_ACCELEROMETER_DATA);
        this.gyroscope = (ArrayList<Double>) intent.getSerializableExtra(EXTRA_GYROSCOPE_DATA);
        this.magnetometer = (ArrayList<Double>) intent.getSerializableExtra(EXTRA_MAGNETOMETER_DATA);
        this.quaternionVector = (ArrayList<Double>) intent.getSerializableExtra(EXTRA_QUATERNION_VECTOR_DATA);
        this.temp = intent.getDoubleExtra(EXTRA_TEMP_DATA, 0);
    }

    public RawImuData(ArrayList<Double> accelerometer, ArrayList<Double> gyroscope, ArrayList<Double> magnetometer, ArrayList<Double> quaternionVector, double temp) {
        this.accelerometer = accelerometer;
        this.gyroscope = gyroscope;
        this.magnetometer = magnetometer;
        this.quaternionVector = quaternionVector;
        this.temp = temp;
    }

    public ArrayList<Double> getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(ArrayList<Double> accelerometer) {
        this.accelerometer = accelerometer;
    }

    public ArrayList<Double> getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(ArrayList<Double> gyroscope) {
        this.gyroscope = gyroscope;
    }

    public ArrayList<Double> getMagnetometer() {
        return magnetometer;
    }

    public void setMagnetometer(ArrayList<Double> magnetometer) {
        this.magnetometer = magnetometer;
    }

    public ArrayList<Double> getQuaternionVector() {
        return quaternionVector;
    }

    public void setQuaternionVector(ArrayList<Double> quaternionVector) {
        this.quaternionVector = quaternionVector;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getTemp() {
        return temp;
    }
}
