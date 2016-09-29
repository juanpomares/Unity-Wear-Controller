using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class RotationTable : MonoBehaviour 
{
	public Button backbutton;

	private float mCurrentX, mCurrentY, mCurrentZ;
	private const float RADIANS_DEGREE= 180.0f/ Mathf.PI;
	private Quaternion mNewRotation;

	private const float mMaxDegree = 55;

	#if UNITY_ANDROID && !UNITY_EDITOR
	AndroidJavaClass unityAndroidClass;
	AndroidJavaObject currentActivity;
	#endif

	// Use this for initialization
	void Start () 
	{
		mCurrentX = transform.localEulerAngles.x;
		mCurrentY = transform.localEulerAngles.y;
		mCurrentZ = transform.localEulerAngles.z;

		#if UNITY_ANDROID && !UNITY_EDITOR
			unityAndroidClass= new AndroidJavaClass ("juanpomares.tfm.matermoviles.unitywearcontroller.MainActivity");
			if(unityAndroidClass!=null)
				currentActivity= unityAndroidClass.GetStatic<AndroidJavaObject> ("mContextActivity");

			if(currentActivity!=null)
			{
				currentActivity.Call("ActiveOrientationSensors");
				currentActivity.Call("addButtonPressListener", transform.name, "onButtonPressed");
				currentActivity.Call("setWearInterface", "Rotate the watch to move the ball.\n Tap the screen to exit.");
			}
		#endif
	}

	void OnDestroy()
	{
		#if UNITY_ANDROID && !UNITY_EDITOR
			if(currentActivity!=null)
			{
				currentActivity.Call("DeActiveOrientationSensors");
				currentActivity.Call("removeButtonPressListener", transform.name);
			}
		#endif
	}

	public void getNewRotation()
	{
		#if UNITY_ANDROID && !UNITY_EDITOR
			float[] _parts = currentActivity.Call<float[]> ("getOrientationValues");

			mCurrentX = _parts [1]*RADIANS_DEGREE;
			mCurrentZ = -_parts [2]*RADIANS_DEGREE;


			if (mCurrentX > mMaxDegree)
				mCurrentX = mMaxDegree;

			if (mCurrentZ> mMaxDegree)
				mCurrentZ= mMaxDegree;

			if (mCurrentX < -mMaxDegree)
				mCurrentX = -mMaxDegree;

			if (mCurrentZ < -mMaxDegree)
				mCurrentZ = -mMaxDegree;


			mNewRotation = Quaternion.Euler (mCurrentX, mCurrentY , mCurrentZ);
		#endif
	}

	// Update is called once per frame
	void Update () 
	{
		#if UNITY_EDITOR
			if (Input.GetKey ("w"))
				mCurrentX += 1;
			else
				if(Input.GetKey("s"))
					mCurrentX -= 1;

			if (Input.GetKey ("a"))
				mCurrentZ += 1;
			else
				if(Input.GetKey("d"))
					mCurrentZ -= 1;

			if (mCurrentX > mMaxDegree)
				mCurrentX = mMaxDegree;

			if (mCurrentZ> mMaxDegree)
				mCurrentZ= mMaxDegree;

			while (mCurrentX < -mMaxDegree)
				mCurrentX = -mMaxDegree;

			while (mCurrentZ< -mMaxDegree)
				mCurrentZ = -mMaxDegree;


			mNewRotation = Quaternion.Euler (mCurrentX, mCurrentY , mCurrentZ);
		#endif

		#if UNITY_ANDROID && !UNITY_EDITOR
			getNewRotation ();
		#endif

		transform.rotation = Quaternion.Lerp (transform.rotation, mNewRotation, Time.deltaTime*10);
	}

	public void onButtonPressed(string buttonpressed)
	{
		if (backbutton)
			backbutton.onClick.Invoke ();
	}
}