package com.example.ttt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.MyViewHolder> {
    Context context;
    ArrayList<Events> arrayList;
    DBOpenHelper dbOpenHelper;

    public EventRecyclerAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_row_layout, parent, false);

        return new MyViewHolder(view);

    }

    // Создаем сам интерфейс и указываем метод и передаваемые им аргументы
    interface OnDeleteClickListener {
        void onDeleteClick();
    }

    // создаем поле объекта-колбэка
    private static OnDeleteClickListener mListener;

    // метод-сеттер для привязки колбэка к получателю событий
    public void setOnCardClickListener(OnDeleteClickListener listener) {
        mListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Events events = arrayList.get(position);
        holder.event.setText(events.getEVENT());
        holder.dateTxt.setText(events.getDAY());
        holder.time.setText(events.getTIME());
        holder.delete.setOnClickListener(v -> {
            deleteCalendarEvent(events.getEVENT(), events.getDAY(), events.getTIME());
            arrayList.remove(position);
            notifyDataSetChanged();

            if (arrayList.size() == 0) {
                mListener.onDeleteClick();
            }

        });

        if (isAlarmed(events.getDAY(), events.getEVENT(), events.getTIME())) {
            holder.setAlarm.setImageResource(R.drawable.ic_action_notif_on);
        } else {
            holder.setAlarm.setImageResource(R.drawable.ic_action_notif_off);
        }
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(convertStringToDate(events.getDAY()));
        int alarmYear = dateCalendar.get(Calendar.YEAR);
        int alarmMonth = dateCalendar.get(Calendar.MONTH);
        int alarmDay = dateCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(convertStringToTime(events.getTIME()));
        int alarmHour = timeCalendar.get(Calendar.HOUR_OF_DAY);
        int alarmMinute = timeCalendar.get(Calendar.MINUTE);


        holder.setAlarm.setOnClickListener(v -> {
            if (isAlarmed(events.getDAY(), events.getEVENT(), events.getTIME())) {
                holder.setAlarm.setImageResource(R.drawable.ic_action_notif_off);
                cancelAlarm(getRequestCode(events.getDAY(), events.getEVENT(), events.getTIME()));
                updateEvent(events.getDAY(), events.getEVENT(), events.getTIME(), "off");
            } else {
                holder.setAlarm.setImageResource(R.drawable.ic_action_notif_on);
                Calendar alarmCalendar = Calendar.getInstance();
                alarmCalendar.set(alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute);
                setAlarm(alarmCalendar, events.getEVENT(), events.getTIME()
                        , getRequestCode(events.getDAY(), events.getEVENT(), events.getTIME()));
                updateEvent(events.getDAY(), events.getEVENT(), events.getTIME(), "on");

            }
            notifyDataSetChanged();

        });
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dateTxt;
        TextView event;
        TextView time;
        Button delete;
        ImageButton setAlarm;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            dateTxt = itemView.findViewById(R.id.eventDate);
            event = itemView.findViewById(R.id.eventName);
            time = itemView.findViewById(R.id.eventTime);
            delete = itemView.findViewById(R.id.delete);
            setAlarm = itemView.findViewById(R.id.alarm);

        }
    }

    private Date convertStringToDate(String eventDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private Date convertStringToTime(String eventDate) {
        SimpleDateFormat format = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void deleteCalendarEvent(String event, String data, String time) {
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.deleteEvent(event, data, time, db);
        dbOpenHelper.close();

    }


    private boolean isAlarmed(String date, String event, String time) {

        boolean alarmed = false;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadIDEvents(date, event, time, db);

        while (cursor.moveToNext()) {
            String notify = cursor.getString(cursor.getColumnIndex(DBStructure.NOTIFY));
            if (notify.equals("on")) {
                alarmed = true;
            } else {
                alarmed = false;
            }
        }
        cursor.close();
        dbOpenHelper.close();
        return alarmed;
    }


    private void setAlarm(Calendar calendar, String event, String time, int requestCode) {
        Intent intent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("event", event);
        intent.putExtra("time", time);
        intent.putExtra("id", requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }

    private void cancelAlarm(int requestCode) {
        Intent intent = new Intent(context.getApplicationContext(), AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    private int getRequestCode(String date, String event, String time) {
        int code = 0;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadIDEvents(date, event, time, db);

        while (cursor.moveToNext()) {
            code = cursor.getInt(cursor.getColumnIndex(DBStructure.ID));

        }
        cursor.close();
        dbOpenHelper.close();
        return code;

    }

    private void updateEvent(String date, String event, String time, String notify) {
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.updateEvent(date, event, time, notify, db);
        dbOpenHelper.close();
    }
}
