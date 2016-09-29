using UnityEngine;
using System.Collections;
using UnityEngine.SceneManagement;

public class SelectLevel : MonoBehaviour 
{
	public void ClickBack()
	{
		SceneManager.LoadScene ("MainScene");
	}

	public void ClickBubbleShotButton()
	{
		SceneManager.LoadScene ("Bubleshot");
	}

	public void ClickGyrosLabyrinthButton()
	{
		SceneManager.LoadScene ("GyroscopeLabyrinth");
	}

	public void ClickPadLabyrinthButton()
	{
		SceneManager.LoadScene ("PadLabyrinth");
	}

	public void ClickJoysLabyrinthButton()
	{
		SceneManager.LoadScene ("JoystickLabyrinth");
	}
}