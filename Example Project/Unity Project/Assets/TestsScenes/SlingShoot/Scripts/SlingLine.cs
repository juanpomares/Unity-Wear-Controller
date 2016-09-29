using UnityEngine;
using System.Collections;

public class SlingLine : MonoBehaviour 
{

	public Transform mLeftObject;
	public Transform mRightObject;

	private Vector3 antLeftPosition;
	private Vector3 antRightPosition;
	private Vector3 mDesp;

	private float tam=0.3f; 


	// Use this for initialization
	void Start () 
	{
		if (mLeftObject == null || mRightObject == null)
			GetComponent<SlingLine> ().enabled = false;
		else 
		{

			transform.position = new Vector3 (0, 0, 0);
			transform.rotation = new Quaternion();
			transform.localScale = new Vector3 (1, 1, 1);

			if (transform.parent != null)
				transform.localScale= new Vector3 (1f/transform.parent.localScale.x, 1f/transform.parent.localScale.y, 1f/transform.parent.localScale.z);
			

			mDesp = new Vector3 (0, 0, tam);

			Mesh mesh = GetComponent<MeshFilter> ().mesh;
			Vector3[] vertices = mesh.vertices;

			antRightPosition = mRightObject.position;
			antLeftPosition = mLeftObject.position;

			vertices [1] = antRightPosition + mDesp;
			vertices [2] = antRightPosition - mDesp;

			vertices [3] = antLeftPosition + mDesp;
			vertices [0] = antLeftPosition - mDesp;

			mesh.vertices = vertices;
			mesh.RecalculateBounds ();
			mesh.RecalculateNormals ();
		}
	}

	// Update is called once per frame
	void Update () 
	{
		bool LeftPositionChange = antLeftPosition != mLeftObject.position;
		bool RightPositionChange = antRightPosition != mRightObject.position;
		if (LeftPositionChange || RightPositionChange) 
		{
			Mesh mesh = GetComponent<MeshFilter>().mesh;
			Vector3[] vertices = mesh.vertices;

			if (RightPositionChange) 
			{
				antRightPosition = mRightObject.position;
				vertices [1] = antRightPosition + mDesp;
				vertices [2] = antRightPosition - mDesp;
			}

			if (LeftPositionChange) 
			{				
				antLeftPosition = mLeftObject.position;
				vertices [3] = antLeftPosition + mDesp;
				vertices [0] = antLeftPosition - mDesp;
			}
			mesh.vertices = vertices;
			mesh.RecalculateBounds();
			mesh.RecalculateNormals ();
		}
	}
}
