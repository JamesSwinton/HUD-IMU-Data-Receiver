package com.zebra.jamesswinton.imudatareceiver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.jamesswinton.imudatareceiver.databinding.LayoutEmptyViewAdapterBinding;
import com.zebra.jamesswinton.imudatareceiver.databinding.LayoutRawImuDataAdapterBinding;
import com.zebra.jamesswinton.imudatareceiver.pojos.RawImuData;

import java.util.List;

public class ImuDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Data Holder
    private Context mCx;
    private List<RawImuData> mRawImuData;

    // View Types
    private static final int EMPTY_VIEW_TYPE = 0;
    private static final int RAW_DATA_VIEW_TYPE = 1;

    public ImuDataAdapter(Context cx, List<RawImuData> rawImuData) {
        this.mCx = cx;
        this.mRawImuData = rawImuData;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RAW_DATA_VIEW_TYPE) {
            return new RawDataHolder(DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.layout_raw_imu_data_adapter, parent, false));
        } else {
            return new EmptyDataHolder(DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.layout_empty_view_adapter, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RawDataHolder) {
            RawDataHolder rawDataHolder = (RawDataHolder) holder;
            RawImuData rawImuData = mRawImuData.get(position);

            // Set Accel
            if (rawImuData.getAccelerometer() != null && rawImuData.getAccelerometer().size() > 0) {
                Double x = rawImuData.getAccelerometer().get(0);
                Double y = rawImuData.getAccelerometer().get(1);
                Double z = rawImuData.getAccelerometer().get(2);
                rawDataHolder.mDataBinding.accelerometerData.setText(
                        String.format(mCx.getString(R.string.accelerometer_data), x, y, z));
            }

            // Set Gyro
            if (rawImuData.getGyroscope() != null && rawImuData.getGyroscope().size() > 0) {
                Double x = rawImuData.getGyroscope().get(0);
                Double y = rawImuData.getGyroscope().get(1);
                Double z = rawImuData.getGyroscope().get(2);
                rawDataHolder.mDataBinding.gyroData.setText(
                        String.format(mCx.getString(R.string.gyroscope_data), x, y, z));
            }

            // Set Mag
            if (rawImuData.getMagnetometer() != null && rawImuData.getMagnetometer().size() > 0) {
                Double x = rawImuData.getMagnetometer().get(0);
                Double y = rawImuData.getMagnetometer().get(1);
                Double z = rawImuData.getMagnetometer().get(2);
                rawDataHolder.mDataBinding.magData.setText(
                        String.format(mCx.getString(R.string.magnetometer_data), x, y, z));
            }

            // Set Quart
            if (rawImuData.getQuaternionVector() != null && rawImuData.getQuaternionVector().size() > 0) {
                Double x = rawImuData.getQuaternionVector().get(0);
                Double y = rawImuData.getQuaternionVector().get(1);
                Double z = rawImuData.getQuaternionVector().get(2);
                rawDataHolder.mDataBinding.quartData.setText(
                        String.format(mCx.getString(R.string.quaternion_vector_data), x, y, z));
            }

            // Set Temp
            rawDataHolder.mDataBinding.tempData.setText(
                    String.format(mCx.getString(R.string.temp_data), rawImuData.getTemp()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mRawImuData == null || mRawImuData.size() == 0 ? EMPTY_VIEW_TYPE : RAW_DATA_VIEW_TYPE;
    }

    @Override
    public int getItemCount() {
        return mRawImuData == null || mRawImuData.size() == 0 ? 1 : mRawImuData.size();
    }

    public void updateImuData(List<RawImuData> rawImuData) {
        this.mRawImuData = rawImuData;
        notifyDataSetChanged();
    }

    public void addImuData(RawImuData rawImuData) {
        this.mRawImuData.add(rawImuData);
        this.notifyDataSetChanged();
        // this.notifyItemInserted(mRawImuData.size() -1);
    }

    private class RawDataHolder extends RecyclerView.ViewHolder {
        public LayoutRawImuDataAdapterBinding mDataBinding;
        public RawDataHolder(@NonNull LayoutRawImuDataAdapterBinding dataBinding) {
            super(dataBinding.getRoot());
            mDataBinding = dataBinding;
        }
    }

    private class EmptyDataHolder extends RecyclerView.ViewHolder {
        public LayoutEmptyViewAdapterBinding mDataBinding;
        public EmptyDataHolder(@NonNull LayoutEmptyViewAdapterBinding dataBinding) {
            super(dataBinding.getRoot());
            mDataBinding = dataBinding;
        }
    }

}
