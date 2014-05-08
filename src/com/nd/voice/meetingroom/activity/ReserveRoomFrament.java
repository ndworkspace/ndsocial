package com.nd.voice.meetingroom.activity;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import cn.nd.social.R;
import cn.nd.social.util.DateStringUtils;

interface ReserveRoomFramentListener{
	void reserveRoomOnNextBtnClick(String title,Calendar date);
}

public class ReserveRoomFrament extends Fragment{
	
	EditText et_title;
	TextView tv_date;
	TextView tv_time;
	ImageButton btn_back;
	Button btn_next;
	
	String mTimeString;
	String mDateString;
	String mTitle;
	Calendar mDate;
	
	ResereActivity mActivity;
	View mRootView;
	
	private final static int DATE_DIALOG = 0;    
	private final static int TIME_DIALOG = 1;
	
	private ReserveRoomFramentListener listener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (ResereActivity)getActivity();
        mRootView = inflater.inflate(R.layout.reserve_room,null); 
		setupView();
		setupListener();
		initData();
		updateDateUI();
		return mRootView;
	}
	
	
	private void setupView() {
		// TODO Auto-generated method stub
		et_title = (EditText) mRootView.findViewById(R.id.et_title);
		tv_date = (TextView) mRootView.findViewById(R.id.tv_date);
		tv_time = (TextView) mRootView.findViewById(R.id.tv_time);
		btn_back = (ImageButton) mRootView.findViewById(R.id.btn_back);
		btn_next = (Button) mRootView.findViewById(R.id.btn_next);
	}
	
	private void setupListener() {
		// TODO Auto-generated method stub
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mActivity.finish();
			}
		});
		
		btn_next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(et_title.getText().length() == 0){
					new AlertDialog.Builder(mActivity) 
					.setMessage("请输入会议标题")
					.setPositiveButton("确定", null)
					.show();
					return;
				}
				mTitle = et_title.getText().toString();
				if(ReserveRoomFrament.this.listener != null){
					ReserveRoomFrament.this.listener.reserveRoomOnNextBtnClick(mTitle,mDate);
				}
			}
		});
		
		tv_date.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ReserveRoomFrament.this.onCreateDialog(DATE_DIALOG).show();
			}
		});
		
		tv_time.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ReserveRoomFrament.this.onCreateDialog(TIME_DIALOG).show();
			}
		});
	}
	
	/**
     * 创建日期及时间选择对话框
     */
    Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DATE_DIALOG:
            dialog = new DatePickerDialog(
            		mActivity,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker dp, int year,int month, int dayOfMonth) {
                    	mDate.set(Calendar.MONTH, month);
                    	mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        ReserveRoomFrament.this.updateDateUI();
                    }
                }, 
                mDate.get(Calendar.YEAR), // 传入年份
                mDate.get(Calendar.MONTH), // 传入月份
                mDate.get(Calendar.DAY_OF_MONTH) // 传入天数
            );
            break;
        case TIME_DIALOG:
            dialog=new TimePickerDialog(
            		mActivity, 
                new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    	mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    	mDate.set(Calendar.MINUTE, minute);
                        ReserveRoomFrament.this.updateDateUI();
                    }
                },
                mDate.get(Calendar.HOUR_OF_DAY),
                mDate.get(Calendar.MINUTE),
                false
            );
            break;
        }
        return dialog;
    }



	private void initData() {
		// TODO Auto-generated method stub
		mDate = Calendar.getInstance();
		mDate.add(Calendar.DATE, 1);
		mDate.set(Calendar.HOUR_OF_DAY, 15);
		mDate.set(Calendar.MINUTE, 0);
	}

	private void updateDateUI(){
		String dateString = DateStringUtils.dateToString(mDate.getTime(), "MM/dd");
		String week = DateStringUtils.getWeekByDate(mDate.getTime());
		mDateString = dateString + " " + week;
		mTimeString = DateStringUtils.dateToString(mDate.getTime(), "HH:mm");
		tv_time.setText(mTimeString);
		tv_date.setText(mDateString);
	}


	public ReserveRoomFramentListener getListener() {
		return listener;
	}


	public void setListener(ReserveRoomFramentListener listener) {
		this.listener = listener;
	}


}
