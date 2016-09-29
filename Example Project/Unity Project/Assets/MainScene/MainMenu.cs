using UnityEngine;
using System.Collections;
using UnityEngine.SceneManagement;

public class MainMenu : MonoBehaviour {

	// Use this for initialization
	void Start () {	
		//Not switch off the screen
		Screen.sleepTimeout = SleepTimeout.NeverSleep;
	}

	public void ClickExit()
	{
		Debug.Log ("ClickExit");
		#if UNITY_EDITOR
			UnityEditor.EditorApplication.isPlaying=false;
		#else
			Application.Quit ();
		#endif
	}

	public void ClickPlay()
	{
		SceneManager.LoadScene ("SelectLevelScene");
	}

	public void ClickAbout()
	{
		SceneManager.LoadScene ("About");
	}
}