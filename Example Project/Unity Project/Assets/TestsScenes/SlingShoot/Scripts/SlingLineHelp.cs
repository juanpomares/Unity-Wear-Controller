using UnityEngine;
using System.Collections;

public class SlingLineHelp : MonoBehaviour 
{
	public Transform mBallObject;
	public Transform mCenterObject;

	private Vector3 antBallPiecesPosition;
	private Vector3 antCenterObjectPosition;
	private Vector3 newCenterPosition;



	private float OriginalY;


	private Vector3 mDesp;

	private float tam=0.75f; 

	public void setMaterial(Material mat)
	{


		gameObject.GetComponent<Renderer> ().material = mat;
	}

	// Use this for initialization
	void Start () 
	{
		if (mBallObject == null || mCenterObject == null)
			GetComponent<SlingLineHelp> ().enabled = false;
		else 
		{
			OriginalY = transform.position.y;
			transform.position = new Vector3 (0, 0, 0);
			transform.rotation = new Quaternion();
			transform.localScale = new Vector3 (1, 1, 1);

			if (transform.parent != null)
				transform.localScale= new Vector3 (1f/transform.parent.localScale.x, 1f/transform.parent.localScale.y, 1f/transform.parent.localScale.z);


			mDesp = new Vector3 (tam, 0, 0);

			antCenterObjectPosition = mCenterObject.position;
			antBallPiecesPosition = mBallObject.position;
			createVertexArray ();
		}
	}

	private void createVertexArray()
	{
		Mesh mesh = GetComponent<MeshFilter> ().mesh;
		Vector3[] vertices = mesh.vertices;

		newCenterPosition = antCenterObjectPosition + 50 * (antCenterObjectPosition - antBallPiecesPosition);

		vertices [1] = newCenterPosition - mDesp;
		vertices [2] = newCenterPosition + mDesp;

		vertices [3] = antBallPiecesPosition - mDesp;
		vertices [0] = antBallPiecesPosition + mDesp;

		for (int i = 0; i < 4; i++)
			vertices [i].y = OriginalY;

		mesh.vertices = vertices;
		mesh.RecalculateBounds ();
		mesh.RecalculateNormals ();
	}

	// Update is called once per frame
	void Update () 
	{

		Vector3 newBallPosition = mBallObject.position;
		Vector3 newCenterPosition = mCenterObject.position;
		if ((newBallPosition != antBallPiecesPosition || newCenterPosition != antCenterObjectPosition) && newBallPosition!=newCenterPosition) 
		{
			antCenterObjectPosition = newCenterPosition;
			antBallPiecesPosition = newBallPosition;
			createVertexArray ();
		}
	}
}