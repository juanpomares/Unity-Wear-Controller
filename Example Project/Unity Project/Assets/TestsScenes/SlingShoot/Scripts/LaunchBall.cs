using UnityEngine;
using System.Collections;

public class LaunchBall : MonoBehaviour 
{
	public Transform BallPieces;
	public GameObject OriginalprefabBall;

	public SlingLineHelp helpline;

	private GameObject mPrefabBall;



	private Vector3 mInitialPosition;
	private Vector3 mLaunchedBallPosition;
	private Vector3 mNewPosition;
	private float despX = 0;
	private float newDespX=0;
	private float despZ = 0;

	private const float MaxDespX = 2.5f;
	private const float MaxDespZ = 3f;


	private float mov=0.05f;

	private float Zmultiplier=1.5f;

	private int launching=0;
	private const int stepsLaunching=10;
	private int recharging=0;
	private const int stepsrecharging=15;

	private string buttonpressed="";

	#if UNITY_ANDROID && !UNITY_EDITOR
	AndroidJavaClass unityAndroidClass;
	AndroidJavaObject currentActivity;
	#endif


	// Use this for initialization
	void Start () 
	{

		mInitialPosition = BallPieces.position;
		mNewPosition = mInitialPosition;
		CreateBall ();

		#if UNITY_ANDROID && !UNITY_EDITOR
		unityAndroidClass= new AndroidJavaClass ("juanpomares.tfm.matermoviles.unitywearcontroller.MainActivity");
		if(unityAndroidClass!=null)
		{
			currentActivity= unityAndroidClass.GetStatic<AndroidJavaObject> ("mContextActivity");

			if(currentActivity!=null)
			{
				currentActivity.Call("addButtonPressListener", transform.name, "onButtonPressed");
				currentActivity.Call("addButtonReleaseListener", transform.name, "onButtonReleased");
				currentActivity.Call("setWearInterface", "VerticalPadWithButton");
			}
		}
		#endif
	}

	void OnDestroy()
	{
		#if UNITY_ANDROID && !UNITY_EDITOR
		if(currentActivity!=null)
		{
		currentActivity.Call("removeListeners", transform.name);
		}
		#endif
	}

	public void onButtonPressed(string newButton)
	{
		if (newButton == "Center")
			ThrowBall ();

		buttonpressed = newButton;
	}

	public void onButtonReleased(string releasedButton)
	{
		if (releasedButton == buttonpressed)
			buttonpressed = "";
	}


	// Update is called once per frame
	void Update () 
	{
		#if UNITY_EDITOR
		if(Input.GetKeyDown("a"))
			onButtonPressed("Left");
		else
			if(Input.GetKeyUp("a"))
				onButtonReleased("Left");
		
		if(Input.GetKeyDown("d"))
			onButtonPressed("Right");
		else
			if(Input.GetKeyUp("d"))
				onButtonReleased("Right");

		if(Input.GetKeyDown("space"))
			onButtonPressed("Center");
		else
			if(Input.GetKeyUp("space"))
				onButtonReleased("Center");
		#endif

		if (launching != 0) 
		{
			launching--;
			mNewPosition = Vector3.Lerp (mLaunchedBallPosition, mInitialPosition, (stepsLaunching - launching)*1f/ stepsLaunching);
			mNewPosition.y = mInitialPosition.y;
			BallPieces.position = mNewPosition;
			if (launching == 0)
				CreateBall ();
		} else 
		{
			if (recharging != 0) {
				recharging--;
				changeDesp (newDespX * (stepsrecharging - recharging) / (stepsrecharging), -MaxDespZ * (stepsrecharging - recharging) / (stepsrecharging));
			}else 
			{
				if(buttonpressed=="Left")
					changeDesp (despX+mov, despZ);
				else if(buttonpressed=="Right")
					changeDesp (despX-mov, despZ);
			}

			BallPieces.position = Vector3.Lerp (BallPieces.position, mNewPosition, Time.deltaTime * 10);
		}
	}

	private void ThrowBall()
	{
		//if (despX != 0 || despZ != 0) 
		if(launching==0 && recharging==0)
		{
			launching = stepsLaunching;
			newDespX = despX;
			mPrefabBall.GetComponent<SlingBallScript> ().ThrowBall (-despX, -despZ*Zmultiplier);
			mPrefabBall.transform.SetParent (null);

			//CreateBall ();
			mLaunchedBallPosition = BallPieces.position;
			//despX = 0; despZ=0;
		}
	}

	private void CreateBall()
	{
		mPrefabBall = Instantiate (OriginalprefabBall, BallPieces.position, Quaternion.identity) as GameObject;
		mPrefabBall.transform.SetParent (BallPieces);
		recharging = stepsrecharging;

		if (helpline) 
		{			
			Material helplineMat = mPrefabBall.GetComponent<SlingBallScript> ().getTheColorMaterial ();
			helpline.GetComponent<Renderer> ().material =helplineMat ;
		}

	}

	private void changeDesp(float newX, float newZ)
	{
		/*if (newX == 0 && newZ == 0) {
			ThrowBall ();
		} else 
		{*/
		if(launching==0)
		{
			/*if (recharging == 0) 
			{*/
				despX = newX;
				if (despX > MaxDespX)
					despX = MaxDespX;
				else if (despX < -MaxDespX)
					despX = -MaxDespX;
			//}

			despZ = newZ;
			if (despZ > MaxDespZ)
				despZ = MaxDespZ;

			//launching = 0;
			//if (returning == 0)
				mNewPosition = mInitialPosition + new Vector3 (despX, 0, despZ * Zmultiplier);
		//}		
		}
	}
}
