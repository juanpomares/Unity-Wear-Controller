using UnityEngine;
using System.Collections;

public class BolaScript : MonoBehaviour {

	private Vector3 mInitialPosition;
	private Rigidbody mRigidBody;

	// Use this for initialization
	void Start () 
	{
		mInitialPosition = transform.position;	
		mRigidBody = GetComponent<Rigidbody> ();
	}
	
	// Update is called once per frame
	void Update () 
	{
		if (transform.position.y < -5) 
		{
			transform.position = mInitialPosition;
			mRigidBody.velocity = new Vector3 (0, 0, 0);
		}
	}
}
