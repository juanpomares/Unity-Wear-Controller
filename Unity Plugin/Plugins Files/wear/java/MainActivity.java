package *your.package.name*;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements ButtonListener, JoystickListener
{
    private static final int MAX_MILLIS_BETWEEN_UPDATES =125;

    private GoogleApiClient mApiClient=null;

    private Node mTelephone =null;

    private boolean mMustNotifyDestroy =true, mSensorsActivated =false;

    private long mLastPositionJoystickSend_Time=0;
    private float mJoystickNX=0, mJoystickNY=0, mJoystickSendNX=0, mJoystickSendNY=0;
    private Timer mTimer=null;

    private String mActualView="None";

    private SensorManager mSensorManager;
    private SensorEventListener mOrientationListener;

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks=null;
    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener=null;
    private MessageApi.MessageListener mMessageListener=null;

    /*private DismissOverlayView mDismissOverlay=null;
    private GestureDetector mDetector;*/

    private void initializeVariables()
    {
        Log.d("Variables", "initializing Variables...");
        mApiClient=null;

        mTelephone =null;

        mMustNotifyDestroy =true; mSensorsActivated =false;

        mLastPositionJoystickSend_Time=0;
        mJoystickNX=mJoystickNY=mJoystickSendNX=mJoystickSendNY=0;
        mTimer=null;

        mActualView="None";
        mSensorManager=null;
        mOrientationListener=null;

        mConnectionCallbacks=null;
        mConnectionFailedListener=null;
        mMessageListener=null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d("onCreateWear", "Creating...");
        initializeVariables();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mOrientationListener=new SensorEventListener()
        {
            float [] mGravityValues;
            float [] mGeoMagneticValues;
            long mLastOrientationSent=0;

            float []R=new float[9];
            float []orientation=new float[3];

            @Override
            public void onSensorChanged(SensorEvent event)
            {
                switch(event.sensor.getType())
                {
                    case Sensor.TYPE_ACCELEROMETER:
                        mGravityValues =event.values;
                        break;

                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mGeoMagneticValues =event.values;
                        break;
                }

                if(System.currentTimeMillis()-mLastOrientationSent< MAX_MILLIS_BETWEEN_UPDATES)
                    return;

                if(mGeoMagneticValues !=null && mGravityValues !=null)
                {
                    if(SensorManager.getRotationMatrix(R, null, mGravityValues, mGeoMagneticValues))
                    {
                        SensorManager.getOrientation(R, orientation);
                        sendMessageChecking(PublicConstants.ORIENTATION_VALUES, orientation[0] + "#" + orientation[1] + "#" + orientation[2]);
                        mLastOrientationSent=System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };

        if(AppSharedPreferences.getAppOpen(this))
        {
            Log.d("Error", "App opened yet!!");
            return;
        }else
            AppSharedPreferences.setAppOpen(this, true);

		/*	
        // Obtain the DismissOverlayView element
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText("Long Press to dismiss");
        mDismissOverlay.showIntroIfNecessary();

        // Configure a gesture detector
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                mDismissOverlay.show();
            }
        });*/

        initGoogleApiClient();
    }

    /*// Capture long presses
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mActualView == "None")
            return mDetector.onTouchEvent(ev);
        else
            return super.onTouchEvent(ev);
    }*/

    @Override
    protected void onStop()
    {
        super.onStop();

        stopTimerTask();
        DeactivateSensors();
        Log.d("onStop", "...");

        if(mApiClient.isConnected())
        {
            if(mMustNotifyDestroy)
            {
                mMustNotifyDestroy=false;
                sendMessageWithCloseApp(PublicConstants.DISCONNECTION, "Bye :)");
                Log.d("onStop", "send disconnected wear");
            }


            if (mMessageListener!=null)
            {
                Wearable.MessageApi.removeListener(mApiClient, mMessageListener);
                mMessageListener = null;
            }
            mApiClient.disconnect();
        }
        mTelephone=null;
        AppSharedPreferences.setAppOpen(this, false);
        this.finish();
    }

    private void initGoogleApiClient()
    {
        createConnectionCallbacks();
        createConnectionFailed();

        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();

        mApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "...");
    }

    private void createConnectionCallbacks()
    {
        mConnectionCallbacks=new GoogleApiClient.ConnectionCallbacks()
        {
            @Override
            public void onConnected(Bundle bundle)
            {
                //Get the node of the phone
                PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mApiClient);
                nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>()
                {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result)
                    {
                        List<Node> nodes=result.getNodes();

                        if(nodes.size()>0)
                            mTelephone = nodes.get(0);

                        if (mTelephone == null)
                        {
                            mMustNotifyDestroy=false;
                            MainActivity.this.finish();
                        }
                        else
                        {
                            mMustNotifyDestroy=true;
                            sendMessageUnchecked(PublicConstants.CONNECTION_WEAR, "");
                        }
                    }
                });

                initMessageListener();
                Wearable.MessageApi.addListener(mApiClient, mMessageListener);
            }

            @Override
            public void onConnectionSuspended(int i) {Log.d("onConnectionSuspended", "error: "+i);}

        };
    }

    private void createConnectionFailed()
    {
        mConnectionFailedListener=new GoogleApiClient.OnConnectionFailedListener()
        {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
            {
                Log.d("onConnectionFailed", "Error: "+connectionResult.getErrorCode());
            }
        };
    }

    public void DeactivateSensors()
    {
        if(mSensorsActivated)
        {
            mSensorsActivated = false;
            mSensorManager.unregisterListener(mOrientationListener);
        }
    }

    public void ActiveSensors()
    {
        if(!mSensorsActivated)
        {
            List<Sensor> sensorsAcelerometer = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
            List<Sensor> sensorsMagnetic = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensorsAcelerometer.size() > 0 && sensorsMagnetic.size()>0)
            {
                mSensorManager.registerListener(mOrientationListener, sensorsAcelerometer.get(0), SensorManager.SENSOR_DELAY_GAME);
                mSensorManager.registerListener(mOrientationListener, sensorsMagnetic.get(0), SensorManager.SENSOR_DELAY_GAME);
                mSensorsActivated = true;
            }
        }
    }

    private void initMessageListener()
    {
        mMessageListener = new MessageApi.MessageListener()
        {
            @Override
            public void onMessageReceived(MessageEvent messageEvent)
            {
                String path = messageEvent.getPath();
                byte[] data = messageEvent.getData();
                String sData = new String(data);
                Log.d("onMessageReceived", "P: " + path + " D: " + sData);

                switch (path)
                {
                    case PublicConstants.CHANGE_LAYOUT:

                        if (sData != mActualView) {

                            if (mActualView == "Joystick")//Actual view is Joystick virtual
                            {
                                stopTimerTask();
                            }

                            switch (sData) {
                                case "HorizontalPad":
                                    setContentView(new ButtonView(MainActivity.this, MainActivity.this, ViewType.HorizontalButtonView, false));
                                    break;
                                case "HorizontalPadWithButton":
                                    setContentView(new ButtonView(MainActivity.this, MainActivity.this, ViewType.HorizontalButtonView, true));
                                    break;

                                case "VerticalPad":
                                    setContentView(new ButtonView(MainActivity.this, MainActivity.this, ViewType.VerticalButtonView, false));
                                    break;
                                case "VerticalPadWithButton":
                                    setContentView(new ButtonView(MainActivity.this, MainActivity.this, ViewType.VerticalButtonView, true));
                                    break;

                                case "Pad":
                                    setContentView(new ButtonView(MainActivity.this, MainActivity.this, ViewType.PadButtonView, false));
                                    break;
                                case "PadWithButton":
                                    setContentView(new ButtonView(MainActivity.this, MainActivity.this, ViewType.PadButtonView, true));
                                    break;

                                case "Joystick":
                                    setContentView(new JoystickView(MainActivity.this, MainActivity.this));
                                    mTimer = new Timer();
                                    mTimer.scheduleAtFixedRate(new SendJoystickNewPosition_Task(), MAX_MILLIS_BETWEEN_UPDATES * 4, MAX_MILLIS_BETWEEN_UPDATES * 4);

                                    break;

                                default:
                                    TextView tview = (TextView) findViewById(R.id.text);
                                    if (tview == null)
                                    {
                                        setContentView(R.layout.activity_main);
                                        tview = (TextView) findViewById(R.id.text);
                                        RelativeLayout layout=(RelativeLayout)findViewById(R.id.MiLayout);
                                        layout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                sendMessageChecking(PublicConstants.BUTTON_PRESS, "ViewText");
                                            }
                                        });

                                    }

                                    tview.setText(sData.length() > 1 ? sData : "Loading Unity game...");
                                    break;
                            }
                            mActualView = sData;

                        }
                        break;

					case PublicConstants.TOAST_LONG:
                       createToast(sData, Toast.LENGTH_LONG);
                        break;

                    case PublicConstants.TOAST_SHORT:
                        createToast(sData, Toast.LENGTH_SHORT);
                        break;
					
                    case PublicConstants.ACTIVE_SENSORS:
                        ActiveSensors();
                        break;

                    case PublicConstants.DEACTIVE_SENSORS:
                        DeactivateSensors();
                        break;

                    case PublicConstants.STOP_ACTIVITY:
                        mMustNotifyDestroy = false;
                        MainActivity.this.finish();
                        break;

                    case PublicConstants.START_ACTIVITY:
                        if(!mMustNotifyDestroy)
                        {
                            mMustNotifyDestroy=true;
                            sendMessageUnchecked(PublicConstants.CONNECTION_WEAR, "");
                        }
                        break;

                    case PublicConstants.CONNECTION_WELL:
                        if (!mMustNotifyDestroy)//Not connected
                        {
                            mMustNotifyDestroy = true;
                            TextView tview = ((TextView) findViewById(R.id.text));
                            if (tview == null) {
                                setContentView(R.layout.activity_main);
                                tview = (TextView) findViewById(R.id.text);
                            }
                            tview.setText("Connection established!!");
                        }
                        break;

                    default:
                        Log.d("onMessageReceived", "Default Message");
                        break;
                }
            }

        };
    }
	
    private void createToast(final String msg, final int length)
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, length).show();
            }
        });
    }

    public void sendMessageChecking(String path, String buffer)
    {
        if(mTelephone !=null)
        {
            (new SendMessage_Thread(path, buffer)).start();
        }else
            Log.e("sendChecking", "TelephoneNull, P: "+path+" D: "+buffer);
    }
    public void sendMessageUnchecked(String path, String buffer)
    {
        if(mTelephone !=null)
        {
            (new SendMessage_Thread(path, buffer)).run();
        }else
            Log.e("sendUnchecked", "TelephoneNull, P: "+path+" D: "+buffer);
    }


    public void sendMessageUnchecked(String path, ByteBuffer buffer)
    {
        if(mTelephone !=null)
        {
            (new SendMessage_Thread(path, buffer)).run();
        }
    }
    public void sendMessageChecking(String path, ByteBuffer buffer)
    {
        if(mTelephone !=null)
        {
            (new SendMessage_Thread(path, buffer)).start();
        }
    }


    public void sendMessageWithCloseApp(String path, String buffer)
    {
        if(mTelephone !=null)
        {
            sendMessageUnchecked(path, buffer);
            Wearable.MessageApi.removeListener(mApiClient, mMessageListener);
            mApiClient.disconnect();
		}
    }

    private class SendMessage_Thread extends Thread
    {
        protected String Path;
        protected ByteBuffer Buff;
        protected PendingResult<MessageApi.SendMessageResult> PendingResultSend;

        SendMessage_Thread(String pth, ByteBuffer bff)
        {
            Path=pth;
            Buff=bff;
        }

        SendMessage_Thread(String pth, String buffer)
        {
            Path=pth;
            Buff=ByteBuffer.allocate(buffer.length());
            Buff.put(buffer.getBytes());
        }

        public void run()
        {
            Log.d("MessageSend", "P: "+Path+" "+new String(Buff.array()));
            PendingResultSend=Wearable.MessageApi.sendMessage(mApiClient, mTelephone.getId(), Path, Buff.array());
        }
    }

    private class SendMessageThreadAwait extends SendMessage_Thread
    {
        SendMessageThreadAwait(String pth, ByteBuffer bff){  super(pth, bff); }
        SendMessageThreadAwait(String pth, String buffer)
        {
            super(pth, buffer);
        }


        public void run()
        {
            super.run();

            MessageApi.SendMessageResult result =PendingResultSend.await();
            if (!result.getStatus().isSuccess())
                Log.e("sendMessage", "ERROR: failed to send Message: " + result.getStatus());
        }
    }

    /*public void ViewTextClicked(View v)
    {
        sendMessageChecking(PublicConstants.BUTTON_PRESS, "ViewText");
    }*/

    @Override
    public void onButtonPress(ButtonName pressed)
    {
        sendMessageChecking(PublicConstants.BUTTON_PRESS, pressed.name());
    }

    @Override
    public void onButtonHold(ButtonName released)
    {
        sendMessageChecking(PublicConstants.BUTTON_RELEASE, released.name());
    }

    @Override
    public void onPositionChange(float newX, float newY)
    {
        if(newX!=mJoystickNX && newY!=mJoystickNY)
        {
            mJoystickNX=newX;
            mJoystickNY=newY;
        }else
            return;

        long actualTime=System.currentTimeMillis();

        if(Math.abs(newX)+Math.abs(newY)>0.1)
            if(actualTime- mLastPositionJoystickSend_Time < MAX_MILLIS_BETWEEN_UPDATES *3) {   /*Log.d("onPositionChange", "No info sended :)");*/ return;}

        sendJoystickPosition(actualTime);
    }

    private void sendJoystickPosition(long time)
    {
        mLastPositionJoystickSend_Time =time;
        sendMessageUnchecked(PublicConstants.JOYSTICK_VALUES, mJoystickNX+"#"+mJoystickNY);
        mJoystickSendNX=mJoystickNX; mJoystickSendNY=mJoystickNY;
    }

    private void stopTimerTask()
    {
        if(mTimer!=null)
        {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    class SendJoystickNewPosition_Task extends TimerTask
    {
        public void run()
        {
            if (mTelephone==null)
            {
                this.cancel();
                Log.d("SendJoystick", "cancelling task");
            }else
            {
                if (mJoystickSendNX != mJoystickNX && mJoystickSendNY != mJoystickNY)
                {
                    long actualTime = System.currentTimeMillis();

                    if (actualTime - mLastPositionJoystickSend_Time < MAX_MILLIS_BETWEEN_UPDATES * 4) {return; }

                    sendJoystickPosition(actualTime);
                }
            }
        }
    }
}
