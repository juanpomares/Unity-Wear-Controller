using UnityEngine;
using System.Collections;

public class BallMovementController : MonoBehaviour {

	public bool isJoystick;
	public Transform aux;

	private Rigidbody mRigidBody;
	private float mForceX = 0f, mForceZ = 0; 

	private float mMaxSpeed=12;


	#if UNITY_EDITOR
	private float step=0.05f;
	#endif

	#if UNITY_ANDROID && !UNITY_EDITOR
	AndroidJavaClass unityAndroidClass;
	AndroidJavaObject currentActivity;
	#endif

	// Use this for initialization
	void Start () 
	{
		mRigidBody = GetComponent<Rigidbody> ();
		#if UNITY_ANDROID && !UNITY_EDITOR
		unityAndroidClass= new AndroidJavaClass ("juanpomares.tfm.matermoviles.unitywearcontroller.MainActivity");
		if(unityAndroidClass!=null)
		currentActivity= unityAndroidClass.GetStatic<AndroidJavaObject> ("mContextActivity");

		if(currentActivity!=null)
		{
			if(isJoystick)
				currentActivity.Call("setWearInterface", "Joystick");
			else
				currentActivity.Call("setWearInterface", "Pad");
		}
		#endif
	}


	void FixedUpdate()
	{
		if (mRigidBody.velocity.magnitude > mMaxSpeed)
			mRigidBody.velocity = mRigidBody.velocity.normalized *mMaxSpeed;
			
	}

	// Update is called once per frame
	void Update () 
	{
		#if UNITY_EDITOR
			if(isJoystick)
			{
				if (Input.GetKey ("space")) { mForceX=0; mForceZ=0;}
				else
				{
					if (Input.GetKey ("w"))
						mForceZ+= step;
					else
						if(Input.GetKey("s"))
							mForceZ -= step;

					if(mForceZ>1) mForceZ=1;
					else if(mForceZ<-1) mForceZ=-1;

					if (Input.GetKey ("a"))
						mForceX-= step;
					else
						if(Input.GetKey("d"))
							mForceX += step;

					if(mForceX>1) mForceX=1;
					else if(mForceX<-1) mForceX=-1;
				}
			}else
			{
				mForceZ=0; mForceX=0;

				if (Input.GetKey ("w"))
					mForceZ= 1;
				else if(Input.GetKey("s"))
					mForceZ= -1;
				else if (Input.GetKey ("a"))
					mForceX= -1;
				else if(Input.GetKey("d"))
					mForceX = 1;
			}

		#endif

		#if UNITY_ANDROID && !UNITY_EDITOR
			if(currentActivity!=null)
			{
				if(isJoystick)
				{
					float[] _jvalues=currentActivity.Get<float[]>("mJoystickValues");
					mForceZ= _jvalues[1]*1.09f;
					mForceX= _jvalues[0]*1.09f;
				}
				else
				{
					mForceZ=0; mForceX=0;
					string buttonPressed=currentActivity.Get<string>("mButtonPressed");

					if (buttonPressed=="Up")
						mForceZ= 1;
					else if (buttonPressed=="Down")
						mForceZ= -1;
					else if (buttonPressed=="Left")
						mForceX= -1;
					else if (buttonPressed=="Right")
						mForceX = 1;
				}
			}
		#endif

		if (aux) 
		{
			aux.position = transform.position+new Vector3 (mForceX, 0, mForceZ);
		}
		mRigidBody.AddForce(mMaxSpeed*new Vector3(mForceX, 0, mForceZ));
	}
}
