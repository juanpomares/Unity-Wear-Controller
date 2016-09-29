using UnityEngine;
using System.Collections;

public class CreateBalls : MonoBehaviour 
{
	public int Balls;
	public int rows = 1;
	public GameObject OriginalBall;
	public Vector3 InitialPosition;
	public Vector3 FinalPosition;



	private GameObject[] newBalls;


	// Use this for initialization
	void Start () 
	{
		if (rows < 1)
			rows = 1;
		newBalls = new GameObject[Balls*rows];

		for (int j = 0; j < rows; j++)			
			for(int i=0; i<Balls; i++)
				newBalls[i+Balls*j]=Instantiate (OriginalBall, Vector3.Lerp(InitialPosition, FinalPosition, i/(Balls-1.0f))+new Vector3(0, 0, -2f*j), Quaternion.identity) as GameObject;

		Balls *= rows;
	}

	
	// Update is called once per frame
	void Update () 
	{
		for(int i=0; i<Balls; i++)
			newBalls[i].GetComponent<SlingBallScript> ().ThrowBall (0, 2);
		Destroy (this);
	}
}
