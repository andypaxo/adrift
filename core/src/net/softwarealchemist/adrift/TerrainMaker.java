package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

public class TerrainMaker {
	private ArrayList<Float> vertices;
	private ArrayList<Short> indices;
	private int vertexLength;

	public Mesh generate() {
		vertices = new ArrayList<Float>();
		indices = new ArrayList<Short>();

		vertexLength = VertexAttribute.Position().numComponents
				+ VertexAttribute.Normal().numComponents;

		for (int x = -10; x < 10; x++)
			for (int z = -10; z < 10; z++)
				addQuad(x, (float) Math.random(), z);

		final Mesh result = new Mesh(true, vertices.size(), indices.size(),
				VertexAttribute.Position(), VertexAttribute.Normal());
		result.setVertices(convertFloats(vertices));
		result.setIndices(convertShorts(indices));

		return result;
	}

	private void addQuad(float x, float y, float z) {
		// Only adds vertical quads for now
		short indexBase = (short) (vertices.size() / vertexLength);

		vertices.add(x - .5f);
		vertices.add(y + .5f);
		vertices.add(z + .5f);
		vertices.add(0f);
		vertices.add(1f);
		vertices.add(0f);

		vertices.add(x + .5f);
		vertices.add(y + .5f);
		vertices.add(z + .5f);
		vertices.add(0f);
		vertices.add(1f);
		vertices.add(0f);

		vertices.add(x + .5f);
		vertices.add(y + .5f);
		vertices.add(z - .5f);
		vertices.add(0f);
		vertices.add(1f);
		vertices.add(0f);

		vertices.add(x - .5f);
		vertices.add(y + .5f);
		vertices.add(z - .5f);
		vertices.add(0f);
		vertices.add(1f);
		vertices.add(0f);

		indices.add(indexBase);
		indices.add(new Short((short) (indexBase + 1)));
		indices.add(new Short((short) (indexBase + 2)));
		indices.add(indexBase);
		indices.add(new Short((short) (indexBase + 2)));
		indices.add(new Short((short) (indexBase + 3)));
	}

	private static float[] convertFloats(List<Float> floats) {
		float[] ret = new float[floats.size()];
		Iterator<Float> iterator = floats.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().floatValue();
		}
		return ret;
	}

	private static short[] convertShorts(List<Short> shorts) {
		short[] ret = new short[shorts.size()];
		Iterator<Short> iterator = shorts.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().shortValue();
		}
		return ret;
	}
}
