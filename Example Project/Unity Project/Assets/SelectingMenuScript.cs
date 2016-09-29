using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class SelectingMenuScript : MonoBehaviour 
{
	public Button button1, button2, button3, button4, button5, button6;
	public GameObject pointer;
	public Button BackButton;

	private Button[] mButtons;
	private int mButtonsCount=0;
	private int mButtonSelected = -1;


	private Vector3 mPrevPosition, mNewPosition;
	private bool mInterpolating=false;
	private float mInitTime;


	#if UNITY_ANDROID && !UNITY_EDITOR
	AndroidJavaClass unityAndroidClass;
	AndroidJavaObject currentActivity;

	#endif


	// Use this for initialization
	void Start () 
	{
		mButtons=new Button[6];

		if (button1)
		{
			mButtons [mButtonsCount] = button1;
			mButtonsCount++;
		}

		if (button2)
		{
			mButtons [mButtonsCount] = button2;
			mButtonsCount++;
		}

		if (button3)
		{
			mButtons [mButtonsCount] = button3;
			mButtonsCount++;
		}

		if (button4)
		{
			mButtons [mButtonsCount] = button4;
			mButtonsCount++;
		}

		if (button5)
		{
			mButtons [mButtonsCount] = button5;
			mButtonsCount++;
		}

		if (button6)
		{
			mButtons [mButtonsCount] = button6;
			mButtonsCount++;
		}

		if(pointer)
			mPrevPosition = mNewPosition = pointer.transform.position;


		if (mButtonsCount>0) 
		{
			#if UNITY_ANDROID && !UNITY_EDITOR

				unityAndroidClass= new AndroidJavaClass ("juanpomares.tfm.matermoviles.unitywearcontroller.MainActivity");
				if(unityAndroidClass!=null)
					currentActivity= unityAndroidClass.GetStatic<AndroidJavaObject> ("mContextActivity");

				if(currentActivity!=null)
				{
					currentActivity.Call("addButtonPressListener", transform.name, "onButtonPressed");
					currentActivity.Call("setWearInterface", "HorizontalPadWithButton");
				}
			#endif
			setButtonSelected (0);
		}
	}


	void OnDestroy()
	{
		if (mButtonsCount>0) 
		{
			#if UNITY_ANDROID && !UNITY_EDITOR
			if(currentActivity!=null)
			{
				currentActivity.Call("removeButtonPressListener", transform.name);
			}
			#endif
		}

	}

	
	// Update is called once per frame
	void Update () 
	{
		if (mInterpolating) 
		{
			float elapsedTime = Time.time - mInitTime;
			float percent = elapsedTime / 0.25f;
			if (percent > 1) 
			{
				percent = 1; 
				mInterpolating = false;
			}
			pointer.transform.position = Vector3.Lerp (mPrevPosition, mNewPosition, percent);
		}


		#if UNITY_EDITOR
			if (Input.GetKeyDown ("up"))
				onButtonPressed ("Up");
			else if (Input.GetKeyDown ("down"))
				onButtonPressed ("Down");
			else if (Input.GetKeyDown ("space"))
				onButtonPressed ("Center");
		#endif

		if (Input.GetKey(KeyCode.Escape))
		{
			if(BackButton)
				BackButton.onClick.Invoke();
		}

	}

	public void onButtonPressed(string button)
	{
		switch (button)
		{
		case "Up":
			setButtonSelected (-1);
			break;
		case "Down":
			setButtonSelected (+1);
			break;
		case "Center":
			if(mButtonSelected!=-1)
				mButtons[mButtonSelected].onClick.Invoke ();
			break;
		}
	}

	private void setButtonFocus(int num, bool interp)
	{
		Vector3 newpos = mButtons [num].transform.position;

		if (!interp) 
		{
			mPrevPosition=mNewPosition=pointer.transform.position = newpos;
		}else
		{
			mInterpolating = true;
			mInitTime = Time.time;
			mPrevPosition = mNewPosition = pointer.transform.position;
			mNewPosition= newpos;
		}
			
	}

	private void setButtonSelected(int dif)
	{
		if (mButtonsCount > 0) 
		{
			if (mButtonSelected != -1) 
			{
				mButtonSelected += dif;
				if (mButtonSelected < 0)
					mButtonSelected = 0;
				else if (mButtonSelected >= mButtonsCount)
					mButtonSelected = mButtonsCount - 1;

				setButtonFocus (mButtonSelected, true);

			} else 
			{
				mButtonSelected = 0;
				setButtonFocus (mButtonSelected, false);
			}
		}
	}
}
