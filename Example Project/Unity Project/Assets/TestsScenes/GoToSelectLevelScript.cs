using UnityEngine;
using System.Collections;
using UnityEngine.SceneManagement;

public class GoToSelectLevelScript : MonoBehaviour 
{
	public void ClickBack()
	{
		SceneManager.LoadScene ("SelectLevelScene");
	}
}
