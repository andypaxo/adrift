package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

public class Terrain {
	private ArrayList<Float> vertices;
	private ArrayList<Short> indices;
	private int vertexLength;

	public int width = 50, depth = 50, height = 5;

	private int[] voxelData = new int[width * depth * height];

	public Mesh generateMesh() {
		System.out.println("Generating...");
		generateVoxelData();

		vertices = new ArrayList<Float>();
		indices = new ArrayList<Short>();

		vertexLength = VertexAttribute.Position().numComponents
				+ VertexAttribute.Normal().numComponents;

		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					if (get(x, y, z) > 0 && get(x, y + 1, z) == 0)
						addYQuad(x, y, z);
				}
			}
		}

		final Mesh result = new Mesh(true, vertices.size(), indices.size(),
				VertexAttribute.Position(), VertexAttribute.Normal());
		result.setVertices(convertFloats(vertices));
		result.setIndices(convertShorts(indices));
		System.out.println("Generation complete");
		return result;
	}

	private void generateVoxelData() {
		double heightAtPoint;
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					heightAtPoint = Math
							.cos(((x - width * .5) * (x - width * .5) + (z - depth * .5)
									* (z - depth * .5))
									/ (width + depth));
					set(x, y, z, (y / (double) height) < heightAtPoint ? 1 : 0);
				}
			}
		}
	}

	private int get(int x, int y, int z) {
		if (y >= height)
			return 0;

		return voxelData[y * width * depth + z * width + x];
	}

	private void set(int x, int y, int z, int val) {
		voxelData[y * width * depth + z * width + x] = val;
	}

	private void addYQuad(float x, float y, float z) {
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
