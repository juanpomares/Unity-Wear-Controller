using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class SlingBallScript : MonoBehaviour 
{

	private Vector3 mVelocity;
	private Rigidbody mRigidBody;
	private bool mIsMoving=false;
	private List<GameObject> mTouchingGameObject;

	private int mColor;

	public Material RedMaterial;
	public Material BlueMaterial;
	public Material YellowMaterial;
	public Material GreenMaterial;

	public Material getTheColorMaterial()
	{
		switch (mColor) 
		{
		case 0:
			return BlueMaterial;
		case 1:
			return RedMaterial;
		case 2:
			return YellowMaterial;
		default:
			return GreenMaterial;
		}
	}

	void Awake()
	{
		mColor = Random.Range (0, 4);
		mTouchingGameObject = new List<GameObject> ();
	}

	// Use this for initialization
	void Start () 
	{		
		mRigidBody = GetComponent<Rigidbody> ();

		//Deactive physics
		mRigidBody.isKinematic = true;
		mRigidBody.detectCollisions = false;
	
		mRigidBody.solverIterationCount = 30;

		gameObject.GetComponent<Renderer> ().material.color = getTheColorMaterial().color;
	}

	public void ThrowBall(float velX, float velZ)
	{
		mIsMoving = true;

		mVelocity.x = velX*10f;
		mVelocity.z = velZ*10f;

		//Active Physics colisiones
		mRigidBody.isKinematic = false;
		mRigidBody.detectCollisions = true;

		mRigidBody.velocity=mVelocity;
	}

	public void OnDestroy()
	{
		foreach (GameObject obj in mTouchingGameObject) 
		{
			if(obj)
				Destroy (obj);
		}
	}

	public bool getMoving()
	{
		return mIsMoving;
	}

	private void stopMovement()
	{
		mRigidBody.solverIterationCount = 1;
		mIsMoving = false;
		mRigidBody.velocity = mVelocity=new Vector3(0,0,0);
		mRigidBody.angularVelocity =mVelocity;
		mRigidBody.constraints = RigidbodyConstraints.FreezeAll;
	}

	private void checkDestroy()
	{
		if (getBallContact () > 3)
			Destroy (gameObject);
	}

	public int getBallContact()
	{
		int i = 1;

		foreach (GameObject obj in mTouchingGameObject) 
		{
			if (obj) 
			{
				SlingBallScript scriptcollision = obj.GetComponent<SlingBallScript> ();
				if(scriptcollision)
					i += (scriptcollision.getBallContact ());
			}
		}

		return i;
	}

	public int getColor()
	{
		return mColor;
	}

	void OnCollisionEnter(Collision _collision)
	{
		if(mIsMoving)
		{
			GameObject objectcollision = _collision.collider.gameObject;

			if (objectcollision.tag == "Wall" || objectcollision.tag == "SlingBall") 
			{				
				//If collision withj walls, destroy self gameobject	
				if (objectcollision.tag == "SlingBall") 
				{
					SlingBallScript scriptcollision = objectcollision.GetComponent<SlingBallScript> ();
					if (scriptcollision!=null && !scriptcollision.getMoving ()) 
					{
						stopMovement ();

						if (scriptcollision.getColor () == mColor) 
						{
							mTouchingGameObject.Add (objectcollision);
							
							if (mColor == scriptcollision.getColor ())
								checkDestroy ();
						}						
					}
				} else
					stopMovement ();
			} else 
			{
				if (objectcollision.tag == "WallLateral") 
				{
					mVelocity.x *= -1;
					mRigidBody.velocity = mVelocity;
					Debug.Log ("Pared Lateral");
				}else
					Debug.Log (objectcollision.tag);
			}
		}
	}
}
