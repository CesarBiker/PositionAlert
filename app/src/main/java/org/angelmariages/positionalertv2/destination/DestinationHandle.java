package org.angelmariages.positionalertv2.destination;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

import org.angelmariages.positionalertv2.R;
import org.angelmariages.positionalertv2.Utils;

import java.util.Arrays;
import java.util.List;

public class DestinationHandle extends IntentService {

    private DestinationDBHelper dbHelper;

    public DestinationHandle() {
        super(Utils.DESTINATION_SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        dbHelper = new DestinationDBHelper(this);

        if(geofencingEvent.hasError()) {
            Utils.sendLog("GeofencingIntent has error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();

        if(geoFenceTransition == GeofencingRequest.INITIAL_TRIGGER_ENTER) {

            List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

            for(Geofence geofence : geofenceList) {
                Utils.sendLog(geofence.getRequestId() + " : " + geoFenceTransition);
                parseGeofenceRequest(getGeofenceArguments(geofence.getRequestId()));
            }
        }
    }

    private void parseGeofenceRequest(String[] geofenceArguments) {
        if(geofenceArguments == null) {
            Utils.sendLog("Null geofence arguments!");
            return;
        }
        createGeofenceNotification(Integer.parseInt(geofenceArguments[0]), geofenceArguments[1]);
        if(Boolean.parseBoolean(geofenceArguments[2])) {
            dbHelper.deleteDestination(Integer.parseInt(geofenceArguments[0]));

            dbHelper.close();
        }

        String ringtoneSaved = getSharedPreferences(Utils.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(Utils.RINGTONE_PREFERENCE, null);
        if(ringtoneSaved == null) {
            ringtoneSaved = RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI;
        }



            /*try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, Uri.parse(ringtoneSaved));
                mediaPlayer.prepare();
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
                mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mediaPlayer) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                });
            } catch(IOException e) {
                e.printStackTrace();
            }*/
    }

    private void createGeofenceNotification(int geofenceID, String geofenceName) {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Reached destination!")
                .setContentText(geofenceName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(geofenceID, notification);
    }

    public static String[] getGeofenceArguments(String geofenceName) {
        String[] arguments = null;
        if(geofenceName != null && !geofenceName.isEmpty()) {
            String[] splited = geofenceName.split("\\|@\\|");
            System.out.println("Biker: " + splited.length);
            System.out.println("Biker: " + Arrays.toString(splited));
            if(splited.length == 3) {
                arguments = new String[3];
                arguments[0] = splited[0];
                arguments[1] = splited[1];
                arguments[2] = splited[2];
            }
        }
        return arguments;
    }
}
