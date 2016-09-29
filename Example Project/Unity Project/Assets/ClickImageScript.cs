using UnityEngine;
using System.Collections;

public class ClickImageScript : MonoBehaviour {

	public void ClickJuanpomares()
	{
		OpenURL ("https://github.com/juanpomares");
	}

	public void ClickMasterLogo()
	{
		OpenURL ("http://www.eps.ua.es/es/master-moviles/");
	}

	public void ClickUALogo()
	{
		OpenURL ("http://www.ua.es/");
	}

	private void OpenURL(string str)
	{
		Application.OpenURL (str);
	}

}
