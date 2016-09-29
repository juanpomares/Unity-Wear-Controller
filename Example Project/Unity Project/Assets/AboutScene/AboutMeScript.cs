using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class AboutMeScript : MonoBehaviour 
{
	#if UNITY_ANDROID && !UNITY_EDITOR
	AndroidJavaClass unityAndroidClass;
	AndroidJavaObject currentActivity;
	#endif

	public Button backbutton;

	// Use this for initialization
	void Start () 
	{
		#if UNITY_ANDROID && !UNITY_EDITOR
		unityAndroidClass= new AndroidJavaClass ("juanpomares.tfm.matermoviles.unitywearcontroller.MainActivity");
		if(unityAndroidClass!=null)
		currentActivity= unityAndroidClass.GetStatic<AndroidJavaObject> ("mContextActivity");

		if(currentActivity!=null)
		{
			currentActivity.Call("addButtonPressListener", transform.name, "onButtonPressed");
			currentActivity.Call("setWearInterface", "Tap the screen to go back.");
		}
		#endif
	}

	void OnDestroy()
	{
		#if UNITY_ANDROID && !UNITY_EDITOR
		if(currentActivity!=null)
		{
			currentActivity.Call("removeButtonPressListener", transform.name);
		}
		#endif
	}

	public void onButtonPressed(string buttonpressed)
	{
		if (backbutton)
			backbutton.onClick.Invoke ();
	}
}
