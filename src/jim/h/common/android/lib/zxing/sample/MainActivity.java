package jim.h.common.android.lib.zxing.sample;

import java.util.Calendar;
import java.util.TimeZone;

import jim.h.common.android.lib.zxing.config.ZXingLibConfig;
import jim.h.common.android.lib.zxing.integrator.IntentIntegrator;
import jim.h.common.android.lib.zxing.integrator.IntentResult;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import jim.h.common.android.lib.zxing.sample.R;

public class MainActivity extends Activity {
    private Handler        handler = new Handler();
    private TextView       txtScanResult;
    private ZXingLibConfig zxingLibConfig;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	
        txtScanResult = (TextView) findViewById(R.id.scan_result);
        zxingLibConfig = new ZXingLibConfig();
        zxingLibConfig.useFrontLight = true;
        View btnScan = findViewById(R.id.scan_button);
        // 扫描按钮
        btnScan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.initiateScan(MainActivity.this, zxingLibConfig);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	final Calendar cal = Calendar.getInstance();  
        final Uri EVENTS_URI = Uri.parse(getCalendarUriBase(this) + "events");	
        
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE: // 扫描结果
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
                        resultCode, data);
                if (scanResult == null) {
                    return;
                }
                final String result = scanResult.getContents();
                if (result != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        	//example:IIU Basketball:::IUvsKentucky^Assembly Hall^2/14/2013/4:30:5:30^Gonna be a good game!!:::IUvsOhio^Assembly Hall^2/16/2013/4:30:6:30^IU is gonna win!!
                            String[] temp = result.split(":::");
                            int count = temp.length - 1;
                            //title,location,datetime,description
                            //2/14/2013/4:30:2:30  (military time required)
                            for (int i=1;i<=count;i++){
                            	String foG = temp[i];
                            	String[] data = foG.split("::");
                            	//parse date value into strings, to be converted into one long value
                            	//splits into 4 
                            	String [] datesplit1 = data[2].split("/");
                            	
                            	int tempYear = Integer.parseInt(datesplit1[2]);
                            	int tempMonth = Integer.parseInt(datesplit1[0])-1;
                            	int tempDay = Integer.parseInt(datesplit1[1]);
                            	String [] datesplit2 = datesplit1[3].split(":");
                            	
                            	int tempHourStart = Integer.parseInt(datesplit2[0]);
                            	int tempMinStart = Integer.parseInt(datesplit2[1]);
                            	int tempHourStop = Integer.parseInt(datesplit2[2]);
                            	int tempMinStop = Integer.parseInt(datesplit2[3]);
                            	
                            	Calendar tempStart = Calendar.getInstance();
                            	Calendar tempStop = Calendar.getInstance();
                            	tempStart.set(tempYear, tempMonth, tempDay, tempHourStart, tempMinStart);
                                tempStop.set(tempYear, tempMonth, tempDay, tempHourStop, tempMinStop);
                        
                            	addEventToCalendar(EVENTS_URI, cr, temp, data,
										tempStart, tempStop);
                                
                            	
                                /*
                            	ContentValues values = new ContentValues();
                                values.put("calendar_id", 1);
                                values.put("title", "dfsa");
                                values.put("allDay", 0);
                                values.put("dtstart", cal.getTimeInMillis()+ 60*1000*60*4); // event starts at 11 minutes from now
                                values.put("dtend", cal.getTimeInMillis()+ 60*1000*60*4); // ends 60 minutes from now
                                values.put("description", "gfsdfdgs");
                                values.put("visibility", 0);
                                values.put("hasAlarm", 1);
                                Uri event = cr.insert(EVENTS_URI, values);
                               */ 
                            }
                            Toast.makeText(getApplicationContext(), count+" events added", Toast.LENGTH_LONG).show();
                        }

                        final ContentResolver cr = getContentResolver();
						private void addEventToCalendar(final Uri EVENTS_URI,
								final ContentResolver cr, String[] temp,
								String[] data, Calendar tempStart,
								Calendar tempStop) {
							ContentValues values = new ContentValues();
							values.put("calendar_id", 1);
							values.put("title", data[0]);
							values.put("allDay", 0);
							values.put("dtstart", tempStart.getTimeInMillis()); // event starts at 11 minutes from now
							values.put("dtend", tempStop.getTimeInMillis()); // ends 60 minutes from now
							values.put("description", temp[0]+" - Location: "+ data[1] + " Description" + data[3]);
							//values.put("visibility", 0);
							values.put("hasAlarm", 1);
							values.put("eventTimezone", TimeZone.getDefault().getID());
							Uri event = cr.insert(EVENTS_URI, values);
						}
                    });
                }
                break;
            default:
        }
    }
    public String getCalendarUriBase(Activity act) {

        String calendarUriBase = null;
        Uri calendars = Uri.parse("content://calendar/calendars");
        Cursor managedCursor = null;
        try {
            managedCursor = act.managedQuery(calendars, null, null, null, null);
        } catch (Exception e) {
        }
        if (managedCursor != null) {
            calendarUriBase = "content://calendar/";
        } else {
            calendars = Uri.parse("content://com.android.calendar/calendars");
            try {
                managedCursor = act.managedQuery(calendars, null, null, null, null);
            } catch (Exception e) {
            }
            if (managedCursor != null) {
                calendarUriBase = "content://com.android.calendar/";
            }
        }
        return calendarUriBase;
    }
}
