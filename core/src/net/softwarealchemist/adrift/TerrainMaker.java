package net.softwarealchemist.adrift;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

public class TerrainMaker {
	public Mesh generate() {
		final Mesh result = new Mesh(true, 4, 6, VertexAttribute.Position(),
				VertexAttribute.Normal());
		result.setVertices(new float[] {//
		//
				0, 0, 1, 0, 1, 0, // 0
				1, 0, 1, 0, 1, 0, // 1
				1, 0, 0, 0, 1, 0, // 2
				0, 0, 0, 0, 1, 0, // 3
		});

		result.setIndices(new short[] {//
		//
				0, 1, 2,//
				0, 2, 3, });

		System.out.println("floats per vertex : " + result.getVertexSize() / 4);

		return result;
	}
}
