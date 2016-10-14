package *your.package.name*;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;


import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends UnityPlayerActivity
{
    public float mJoystickValues[]={0,0};
    public String mButtonPressed ="None";
    public float mOrientationValues[]={0,0,0};
    public String mActualView ="";
    public boolean mSensorActivated=false;
    public int mConnectedWearable=-1;

    private String mWearableID="";
    private GoogleApiClient mApiClient=null;

    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks=null;
    GoogleApiClient.OnConnectionFailedListener mFailedListener =null;
    MessageApi.MessageListener mMessageListener=null;


    //Listeners Unity
    private enum ListenerType {Joystick, ButtonPress, ButtonRelease, Orientation, Connection, Disconnection}

    protected LinkedHashMap<String, String> mJoystickListeners=null;
    protected LinkedHashMap<String, String> mButtonPressListeners=null;
    protected LinkedHashMap<String, String> mButtonReleaseListeners=null;
    protected LinkedHashMap<String, String> mOrientationListeners=null;
    protected LinkedHashMap<String, String> mConnectionListeners=null;
    protected LinkedHashMap<String, String> mDisconnectionListeners=null;

    public static Context mContextActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContextActivity=this;

        initializeManagerListeners();
        initGoogleApiClient();
    }

	public void createShortToast(final String msg){ createToast(msg, Toast.LENGTH_SHORT); }
    public void createLongToast(final String msg) { createToast(msg, Toast.LENGTH_LONG);  }

    public void createShortToastWear(final String msg){ sendMessageChecking(PublicConstants.TOAST_SHORT, msg); }
    public void createLongToastWear(final String msg) { sendMessageChecking(PublicConstants.TOAST_LONG, msg);  }


    private void createToast(final String msg, final int length)
    {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, length).show();
            }
        });
    }

    public void setWearInterface(String newinterface)
    {
        if(mActualView!=newinterface)
        {
            mJoystickValues[0]=mJoystickValues[1]=0;
            mActualView=newinterface;
            sendMessageChecking(PublicConstants.CHANGE_LAYOUT, newinterface);
        }
    }

    public String getWearInterface() { return mActualView; }
    public String getButtonPressed() { return mButtonPressed; }
	
    public float[] getOrientationValues(){return mOrientationValues;}
    public float[] getJoystickValues(){return mJoystickValues;}

    public void OpenWearableApp()
    {
        sendMessageChecking(PublicConstants.START_ACTIVITY, "");
    }

    public void CloseWearableApp()
    {
        sendMessageChecking(PublicConstants.STOP_ACTIVITY, "");
        mConnectedWearable=-1;
    }

    public void ActiveOrientationSensors()
    {
        if(!mSensorActivated)
        {
            mSensorActivated = true;
            sendMessageChecking(PublicConstants.ACTIVE_SENSORS, "");
        }
    }

    public void DeActiveOrientationSensors()
    {
        if(mSensorActivated)
        {
            mOrientationValues[0]=mOrientationValues[1]=mOrientationValues[2]=0;


            mSensorActivated = false;
            sendMessageChecking(PublicConstants.DEACTIVE_SENSORS, "");
        }
    }

    private void initGoogleApiClient()
    {
        initConnectionCallbacks();
        initConnectionFailedListener();

        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mFailedListener)
                .build();

        mApiClient.connect();
    }

    private void initMessageListener()
    {
        if(mMessageListener==null)
        {
            mMessageListener = new MessageApi.MessageListener()
            {
                @Override
                public void onMessageReceived(MessageEvent messageEvent)
                {
                    String path = messageEvent.getPath();

                    byte[] Bdata = messageEvent.getData();
                    String data = new String(Bdata);

                    Log.d("MessageReceived", " P: " + path + " D: " + data);

                    switch (path)
                    {
                        case PublicConstants.BUTTON_PRESS:
                            mButtonPressed = data;
                            AddTextInterface("Button Pressed: " + data);
                            notifyListener(ListenerType.ButtonPress, data);
                            break;

                        case PublicConstants.BUTTON_RELEASE:
                            AddTextInterface("Button Releasad: " + data);
                            if (mButtonPressed.equalsIgnoreCase(data))
                                mButtonPressed = "None";
                            notifyListener(ListenerType.ButtonRelease, data);

                            break;

                        case PublicConstants.ORIENTATION_VALUES:
                            String[] _pieces = data.split("#");

                            if (_pieces.length > 2)
                                for (int i = 0; i < 3; i++)
                                    mOrientationValues[i] = Float.parseFloat(_pieces[i]);

                            notifyListener(ListenerType.Orientation, data);
                            break;

                        case PublicConstants.JOYSTICK_VALUES:
                            String[] _piecs = data.split("#");
                            if (_piecs.length > 1)
                                for (int i = 0; i < 2; i++)
                                    mJoystickValues[i] = Float.parseFloat(_piecs[i]);

                            notifyListener(ListenerType.Joystick, data);
                            break;

                        case PublicConstants.DISCONNECTION:
                            mConnectedWearable = -1;
                            AddTextInterface("SmartWatch disconnected!! :(", Toast.LENGTH_LONG);

                            for (int i = 0; i < 2; i++)
                                mJoystickValues[i] = 0;

                            for (int i = 0; i < 3; i++)
                                mOrientationValues[i] = 0;

                            notifyListener(ListenerType.Disconnection, data);
                            break;

                        case PublicConstants.CONNECTION_WEAR:
                            if (mConnectedWearable != 1)
                            {
                                mWearableID = messageEvent.getSourceNodeId();
                                mConnectedWearable = 1;

                                sendMessageChecking(PublicConstants.CONNECTION_WELL, "jeje");
                                AddTextInterface("SmartWatch connected!! :)", Toast.LENGTH_LONG);

                                ReConnection();


                                notifyListener(ListenerType.Connection, data);
                            }
                            break;

                        default:
                            Log.d("onMessageReceived", "Default :0");
                            break;
                    }
                }
            };
        }
    }

    private void initConnectionCallbacks()
    {
        mConnectionCallbacks=new GoogleApiClient.ConnectionCallbacks()
        {
            @Override
            public void onConnected(@Nullable Bundle bundle)
            {
                AddTextInterface("onConnected...");
                initMessageListener();
                Wearable.MessageApi.addListener(mApiClient, mMessageListener);
                sendMessageChecking(PublicConstants.START_ACTIVITY, "");
                AddTextInterface("Sended START_ACTIVITY command");
            }

            @Override
            public void onConnectionSuspended(int i) {AddTextInterface("Connection suspended");}
        };
    }

    private void initConnectionFailedListener()
    {
        if(mFailedListener==null)
        {
            mFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    AddTextInterface("Connection failed: " + connectionResult.toString(), Toast.LENGTH_LONG);
                }
            };
        }
    }

    private void getMWearable()
    {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
        List<Node> list=nodes.getNodes();

        if(list.size()>0)
            mWearableID=list.get(0).getId();
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
            if (mWearableID == "") {
                AddTextInterface("SendMessage: No smartwatch Connected!! :0");
            } else
            {
                Log.d("MessageSend", "P: " + Path + " " + new String(Buff.array()));
                PendingResultSend = Wearable.MessageApi.sendMessage(mApiClient, mWearableID, Path, Buff.array());
            }
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
            if (mWearableID == "")
                getMWearable();

            if (mConnectedWearable != 1 && !Path.equals(PublicConstants.START_ACTIVITY) && !Path.equals(PublicConstants.STOP_ACTIVITY))
            {
                //if not connected
                Log.e("SendMessage", "Not smartwatch connected");
                OpenWearableApp();
            }else
            {
                super.run();

                if (PendingResultSend != null)
                {
                    MessageApi.SendMessageResult result = PendingResultSend.await();
                    if (!result.getStatus().isSuccess())
                        Log.e("sendMessage", "ERROR: failed to send Message: " + result.getStatus());
                } else
                    AddTextInterface("No smartwatch connected", Toast.LENGTH_LONG);
            }
        }
    }

    private void sendMessageUnchecked(String path, String text )
    {
        if (mConnectedWearable!=0 || path.equals(PublicConstants.START_ACTIVITY) || path.equals(PublicConstants.STOP_ACTIVITY))
        {
            AddTextInterface("mConnected: "+mConnectedWearable+" sendMessage P:"+path+" T:"+text);
            new SendMessage_Thread(path, text).run();
        }
    }

    private void sendMessageChecking(String path, String text ) {
        if (mConnectedWearable!=0 || path.equals(PublicConstants.START_ACTIVITY) || path.equals(PublicConstants.STOP_ACTIVITY))
        {
            AddTextInterface("mConnected: "+mConnectedWearable+" sendMessageChecking P:"+path+" T:"+text);
            new SendMessageThreadAwait(path, text).start();
        }
    }

    private void DisconnectWear()
    {
        if(mApiClient.isConnected())
        {
            if(mMessageListener!=null)
            {
                Wearable.MessageApi.removeListener(mApiClient, mMessageListener);
            }
            mApiClient.disconnect();
        }
    }

    private void AddTextInterface(String txt)
    {
        AddTextInterface(txt, -1);
    }

    private void AddTextInterface(final String txt, final int length)
    {
        Log.d("AddTextInterface", txt);
        if(length!=-1)
        {
            MainActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run() { Toast.makeText(MainActivity.this, txt, length).show(); }
            });
        }
    }

    private void ReConnection()
    {
        if (mSensorActivated)
            sendMessageUnchecked(PublicConstants.ACTIVE_SENSORS, "Orientation");

        if (mActualView != null)
            sendMessageUnchecked(PublicConstants.CHANGE_LAYOUT, mActualView);
    }

    @Override
    protected void onDestroy()
    {
        if(mConnectedWearable==1)
            sendMessageUnchecked(PublicConstants.STOP_ACTIVITY, "");

        DisconnectWear();
        ClearAllUnityListeners();
        super.onDestroy();
    }


    /*Listeners para Unity*/
    private void initializeManagerListeners()
    {
        mJoystickListeners =new LinkedHashMap<String, String>();
        mButtonPressListeners =new LinkedHashMap<String, String>();
        mButtonReleaseListeners =new LinkedHashMap<String, String>();
        mOrientationListeners =new LinkedHashMap<String, String>();
        mConnectionListeners=new LinkedHashMap<String, String>();
        mDisconnectionListeners =new LinkedHashMap<String, String>();
    }

    public void addJoystickListener(String object, String function){ addListener(mJoystickListeners, object, function);}
    public void addButtonPressListener(String object, String function){ addListener(mButtonPressListeners, object, function);}
    public void addButtonReleaseListener(String object, String function){ addListener(mButtonReleaseListeners, object, function);}
    public void addOrientationListener(String object, String function){ addListener(mOrientationListeners, object, function);}
    public void addConnectionListener(String object, String function){ addListener(mConnectionListeners, object, function);}
    public void addDisconnectionListener(String object, String function){ addListener(mDisconnectionListeners, object, function);}

    public void removeJoystickListener(String object){ removeListener(mJoystickListeners, object);}
    public void removeButtonPressListener(String object){ removeListener(mButtonPressListeners, object);}
    public void removeButtonReleaseListener(String object){ removeListener(mButtonReleaseListeners, object);}
    public void removeOrientationListener(String object){ removeListener(mOrientationListeners, object);}
    public void removeConnectionListener(String object){ removeListener(mConnectionListeners, object);}
    public void removeDisconnectionListener(String object){ removeListener(mDisconnectionListeners, object);}

    public void removeListeners(String object)
    {
        mJoystickListeners.remove(object);
        mButtonPressListeners.remove(object);
        mButtonReleaseListeners.remove(object);
        mOrientationListeners.remove(object);
        mConnectionListeners.remove(object);
        mDisconnectionListeners.remove(object);
    }

    public void ClearAllUnityListeners()
    {
        mJoystickListeners.clear();
        mButtonPressListeners.clear(); mButtonReleaseListeners.clear();
        mOrientationListeners.clear();
        mConnectionListeners.clear(); mDisconnectionListeners.clear();
    }

    private void addListener(LinkedHashMap<String, String> map, String object, String function){ map.put(object, function);  }
    private void removeListener(LinkedHashMap<String, String> map, String object){ map.remove(object);  }

    private LinkedHashMap<String, String> getMapByType(ListenerType tipo)
    {
        if(tipo==ListenerType.ButtonPress)          return mButtonPressListeners;
        else if(tipo==ListenerType.ButtonRelease)   return mButtonReleaseListeners;
        else if(tipo==ListenerType.Orientation)     return mOrientationListeners;
        else if(tipo==ListenerType.Joystick)        return mJoystickListeners;
        else if(tipo==ListenerType.Connection)      return mConnectionListeners;
        else                                        return mDisconnectionListeners;
    }

    private void notifyListener(ListenerType tipo) {  notifyListener(tipo, "");  }
    private void notifyListener(ListenerType tipo, String data)
    {
        LinkedHashMap<String, String> _map= getMapByType(tipo);

        Iterator<String> _objects=_map.keySet().iterator();
        while(_objects.hasNext())
        {
            String _object=_objects.next();
            String _method=_map.get(_object);

            NotifyObject(_object, _method, data);

        }
    }

    private void NotifyObject(String idObject, String method, String message)
    {
        UnityPlayer.UnitySendMessage(idObject, method, message);
    }
}
