package jp.ac.titech.itpro.sdl.accelgraph;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {

    private final static String TAG = "MainActivity";

    private TextView rateView, accuracyView;
    private GraphView xView, yView, zView;

    private SensorManager sensorMgr;

    private Sensor accelerometer;
    private Sensor tempmeter;
    private Sensor rotationmeter;
    private Sensor lightmeter;
    private Sensor gravitymeter;
    private Sensor nearmeter;
    private Sensor magnetmeter;
    private Sensor pressuremeter;
    private Sensor vectormeter;


    private final static long GRAPH_REFRESH_WAIT_MS = 20;

    private GraphRefreshThread th = null;
    private Handler handler;

    private float vx, vy, vz;
    private float rate;
    private int accuracy;
    private long prevts;

    private final static float alpha = 0.75F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        rateView = (TextView) findViewById(R.id.rate_view);
        accuracyView = (TextView) findViewById(R.id.accuracy_view);
        xView = (GraphView) findViewById(R.id.x_view);
        yView = (GraphView) findViewById(R.id.y_view);
        zView = (GraphView) findViewById(R.id.z_view);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        /* init sensors */
        accelerometer = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        tempmeter     = sensorMgr.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        rotationmeter = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        lightmeter    = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
        gravitymeter  = sensorMgr.getDefaultSensor(Sensor.TYPE_GRAVITY);
        nearmeter     = sensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        pressuremeter = sensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        magnetmeter   = sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        vectormeter   = sensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (accelerometer == null ) {
            Toast.makeText(this, getString(R.string.toast_no_accel_error),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        handler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        //List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_LIGHT);

        sensorMgr.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, nearmeter,    SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, lightmeter,   SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, magnetmeter, SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, gravitymeter, SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, rotationmeter, SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, tempmeter, SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, pressuremeter, SensorManager.SENSOR_DELAY_FASTEST);
        sensorMgr.registerListener(this, vectormeter, SensorManager.SENSOR_DELAY_FASTEST);
        th = new GraphRefreshThread();
        th.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        th = null;
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()){

            case Sensor.TYPE_PROXIMITY:
                //v = alpha * vx + (1-alpha)* event.values[0];
                break;

            case Sensor.TYPE_LIGHT:
                vx = alpha * vy + (1-alpha)* event.values[0];
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                vy = alpha * vx + (1-alpha)* event.values[0];
                break;

            case Sensor.TYPE_GRAVITY:
                //v = alpha * vz + (1-alpha)* event.values[0];
                break;

            case Sensor.TYPE_ORIENTATION:
                //v = alpha * vz + (1-alpha)* event.values[0];
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                vz = alpha * vz + (1-alpha)* event.values[0];
                break;

            case Sensor.TYPE_PRESSURE:
                //v = alpha * vz + (1-alpha)* event.values[0];
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                //v = alpha * vz + (1-alpha)* event.values[0];
                break;
        }

        rate = ((float) (event.timestamp - prevts)) / (1000 * 1000);
        prevts = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged: ");
        this.accuracy = accuracy;
    }

    private class GraphRefreshThread extends Thread {
        public void run() {
            try {
                while (th != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            rateView.setText(String.format(Locale.getDefault(), "%f", rate));
                            accuracyView.setText(String.format(Locale.getDefault(), "%d", accuracy));
                            xView.addData(vx, true);
                            yView.addData(vy, true);
                            zView.addData(vz, true);
                        }
                    });
                    Thread.sleep(GRAPH_REFRESH_WAIT_MS);
                }
            }
            catch (InterruptedException e) {
                Log.e(TAG, e.toString());
                th = null;
            }
        }
    }
}
