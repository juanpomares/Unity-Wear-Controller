using UnityEngine;
using System.Collections;

public class CoinScript : MonoBehaviour 
{	
	// Update is called once per frame
	void Update () 
	{
		transform.Rotate (new Vector3 (0, 0, Time.deltaTime*180));
	}

	//Collision with ball
	void OnCollisionEnter(Collision collision) 
	{
		Destroy (gameObject);
	}
}
