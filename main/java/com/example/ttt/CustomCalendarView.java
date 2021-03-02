package com.example.ttt;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.DialogInterface.*;

public class CustomCalendarView extends LinearLayout {
    public static final int MAX_CALENDAR_DAYS = 42;

    ImageButton mNextBut;
    ImageButton mPrevBut;
    TextView mCurrData;
    GridView mGridView;
    Calendar mCalendar = Calendar.getInstance(Locale.ENGLISH);
    Context mContext;

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    SimpleDateFormat eventFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    MyGridAdapter myGridAdapter;
    AlertDialog alertDialog;

    List<Date> dates = new ArrayList<>();
    List<Events> eventsList = new ArrayList<>();

    int alarmYear;
    int alarmMonth;
    int alarmDay;
    int alarmHour;
    int alarmMinute;

    DBOpenHelper dbOpenHelper;


    public CustomCalendarView(Context context) {
        super(context);
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        InitialiseLayout();
        SetUpCalendar();

        mPrevBut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.add(Calendar.MONTH, -1);
                SetUpCalendar();
            }
        });
        mNextBut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.add(Calendar.MONTH, 1);
                SetUpCalendar();
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);

                View addView = LayoutInflater.from((parent.getContext())).inflate(R.layout.add_newevent_layout, null);
                EditText EventName = addView.findViewById(R.id.eventName);
                TextView EventTime = addView.findViewById(R.id.eventTime);
                ImageButton SetTime = addView.findViewById(R.id.setEventTime);

                CheckBox alarm = addView.findViewById(R.id.alarm);
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(dates.get(position));
                alarmYear = dateCalendar.get(Calendar.YEAR);
                alarmMonth = dateCalendar.get(Calendar.MONTH);
                alarmDay = dateCalendar.get(Calendar.DAY_OF_MONTH);
//

                Button AddEvent = addView.findViewById(R.id.addEvent);
                EventTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        int minutes = calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(addView.getContext(), R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat hformate = new SimpleDateFormat("K:mm a", Locale.ENGLISH);
                                String event_time = hformate.format(c.getTime());
                                EventTime.setText(event_time);

                                alarmHour = c.get(Calendar.HOUR_OF_DAY);
                                alarmMinute = c.get(Calendar.MINUTE);
                            }
                        }, hours, minutes, false);
                        timePickerDialog.show();
                    }
                });
//
                final String date = eventFormat.format(dates.get(position));
                final String month = monthFormat.format(dates.get(position));
                final String year = yearFormat.format(dates.get(position));


                AddEvent.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (alarm.isChecked()) {
                            SaveEvent(EventName.getText().toString(), EventTime.getText().toString(), date, month, year, "on");
                            SetUpCalendar();
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute);
                            setAlarm(calendar, EventName.getText().toString(), EventTime.getText().toString()
                                    , getRequestCode(date, EventName.getText().toString(), EventTime.getText().toString()));
                            alertDialog.dismiss();
                        } else {

                            SaveEvent(EventName.getText().toString(), EventTime.getText().toString(), date, month, year, "off");
                            SetUpCalendar();
                            alertDialog.dismiss();
                        }
                    }
                });
                builder.setView(addView);
                alertDialog = builder.create();
                alertDialog.show();
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String datee = eventFormat.format(dates.get(position));

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View showView = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_layout, null);

                RecyclerView recyclerView = showView.findViewById(R.id.EventRV);

                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(showView.getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                EventRecyclerAdapter eventRecyclerAdapter = new EventRecyclerAdapter(showView.getContext(), CollectEventsByDate(datee));

                recyclerView.setAdapter(eventRecyclerAdapter);
                eventRecyclerAdapter.notifyDataSetChanged();

                eventRecyclerAdapter.setOnCardClickListener(new EventRecyclerAdapter.OnDeleteClickListener() {
                    @Override
                    public void onDeleteClick() {
                        alertDialog.cancel();
                    }
                });

                builder.setView(showView);

                alertDialog = builder.create();

                if (CollectEventsByDate(datee).size() != 0) {
                    alertDialog.show();
                }


                alertDialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        SetUpCalendar();
                    }
                });
                return true;
            }
        });
    }


    private int getRequestCode(String date, String event, String time) {
        int code = 0;
        dbOpenHelper = new DBOpenHelper(mContext);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadIDEvents(date, event, time, db);

        while (cursor.moveToNext()) {
            code = cursor.getInt(cursor.getColumnIndex(DBStructure.ID));

        }
        cursor.close();
        dbOpenHelper.close();
        return code;

    }


    private void setAlarm(Calendar calendar, String event, String time, int requestCode) {
        Intent intent = new Intent(mContext.getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("event", event);
        intent.putExtra("time", time);
        intent.putExtra("id", requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) mContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }

    private ArrayList<Events> CollectEventsByDate(String date) {
        ArrayList<Events> arrayList = new ArrayList<>();
        dbOpenHelper = new DBOpenHelper(mContext);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadEvents(date, db);

        while (cursor.moveToNext()) {
            String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            String time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            String day1 = cursor.getString(cursor.getColumnIndex(DBStructure.DAY));
            String month1 = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            String year1 = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
            Events events = new Events(event, time, day1, month1, year1);
            arrayList.add(events);
            Log.d("dd", "ddddd");
        }
        cursor.close();
        dbOpenHelper.close();

        return arrayList;

    }

    public void SaveEvent(String event, String time, String day, String month, String year, String notify) {
        dbOpenHelper = new DBOpenHelper(mContext);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.SaveEvent(event, time, day, month, year, notify, db);
        dbOpenHelper.close();
        Toast.makeText(mContext, "Event Saved", Toast.LENGTH_SHORT).show();
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private void InitialiseLayout() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calendar_layout, this);
        mNextBut = view.findViewById(R.id.nextBtn);
        mPrevBut = view.findViewById(R.id.previousBtn);
        mCurrData = view.findViewById(R.id.curData);
        mGridView = view.findViewById(R.id.gridView);

    }

    private void SetUpCalendar() {
        String curData = dateFormat.format(mCalendar.getTime());
        mCurrData.setText(curData);

        dates.clear();
        Calendar monthCalendar = (Calendar) mCalendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int FirstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 2;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayOfMonth);
        CollectEventsMonth(monthFormat.format(mCalendar.getTime()), yearFormat.format(mCalendar.getTime()));


        while (dates.size() < MAX_CALENDAR_DAYS) {
            dates.add((monthCalendar.getTime()));
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        myGridAdapter = new MyGridAdapter(mContext, dates, mCalendar, eventsList);
        mGridView.setAdapter(myGridAdapter);
    }

    private void CollectEventsMonth(String month, String year) {
        eventsList.clear();
        dbOpenHelper = new DBOpenHelper(mContext);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadEventsMonth(month, year, db);
        while (cursor.moveToNext()) {
            String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
            String time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
            String day = cursor.getString(cursor.getColumnIndex(DBStructure.DAY));
            String month1 = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
            String year1 = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
            Events events = new Events(event, time, day, month1, year1);
            eventsList.add(events);
        }
        cursor.close();
        dbOpenHelper.close();
    }


}
