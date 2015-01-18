package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Vector3;

public class Terrain {
	private FloatBuffer vertices;
	private ShortBuffer indices;
	private int vertexLength;

	public int width = 320, depth = 320, height = 64;
	public double seed;

	private int[] voxelData = new int[width * depth * height];
	private double noiseScale = 4;

	public List<Mesh> generateMeshes() {
		System.out.println("Generating...");
		seed = Math.random() * 1000000.0;
		generateVoxelData();

		vertexLength = VertexAttribute.Position().numComponents
				+ VertexAttribute.Normal().numComponents
				+ VertexAttribute.ColorUnpacked().numComponents;

		final ArrayList<Mesh> result = new ArrayList<Mesh>();
		final int chunkSize = 32;
		vertices = new FloatBuffer(chunkSize * chunkSize * vertexLength * 64);
		indices = new ShortBuffer(chunkSize * chunkSize * 48);

		for (int x = 0; x < width; x += chunkSize)
			for (int z = 0; z < depth; z += chunkSize)
				result.add(generateMeshForChunk(chunkSize, x, z));
		
		System.out.println("Generation complete");
		return result;
	}

	private Mesh generateMeshForChunk(final int chunkSize, final int startX, final int startZ) {
		for (int x = startX; x < startX + chunkSize; x++) {
			for (int z = startZ; z < startZ + chunkSize; z++) {
				for (int y = 0; y < height; y++) {
					if (get(x, y, z) > 0 && get(x, y + 1, z) == 0)
						addYQuad(x, y, z);
					if (get(x, y, z) > 0 && get(x + 1, y, z) == 0)
						addXQuad(x, y, z, 1);
					if (get(x, y, z) > 0 && get(x - 1, y, z) == 0)
						addXQuad(x, y, z, -1);
					if (get(x, y, z) > 0 && get(x, y, z + 1) == 0)
						addZQuad(x, y, z, 1);
					if (get(x, y, z) > 0 && get(x, y, z - 1) == 0)
						addZQuad(x, y, z, -1);
				}
			}
		}

		System.out.println(String.format("Generated %d triangles", indices.length / 3));
		final Mesh mesh = new Mesh(true, vertices.length, indices.length,
				VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
		mesh.setVertices(vertices.buffer, 0, vertices.length);
		mesh.setIndices(indices.buffer, 0, indices.length);
		vertices.reset();
		indices.reset();
		return mesh;
	}

	private void generateVoxelData() {
		double heightAtPoint, distFromCentre;
		double caveValue, caveThreshold = .8f;
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					distFromCentre = Math
							.sqrt(((x - width * .5) * (x - width * .5) + (z - depth * .5)
									* (z - depth * .5)))
							* 2.0 / width;

					heightAtPoint =
						((SimplexNoise.noise((x * noiseScale / width) + seed, z * noiseScale / depth) * .5 + .5) +
						(SimplexNoise.noise((x * noiseScale * 2 / width) + seed + 10000, z * noiseScale * 2 / depth) * .2))
							* (1 - distFromCentre) - (1.0 / height);
//					caveValue = (SimplexNoise.noise((x * noiseScale / width) + seed + 30000, z * noiseScale / depth, y * noiseScale / height) * .5 + .5)
//							* (1 - ((double) y / height));
					caveValue = SimplexNoise.noise((
							x * noiseScale * 1.5 / width) + seed + 30000,
							z * noiseScale * 1.5 / depth,
							y * noiseScale * 1.5 / height) * .5 + .5;
					set(x, y, z, (y / (double) height) < heightAtPoint && caveValue < caveThreshold ? 1 : 0);
				}
			}
		}
	}

	private int get(int x, int y, int z) {
		if (y >= height || y < 0 || x >= width || x < 0 || z >= depth || z < 0)
			return 0;

		return voxelData[y * width * depth + z * width + x];
	}

	private void set(int x, int y, int z, int val) {
		voxelData[y * width * depth + z * width + x] = val;
	}

	private void addYQuad(int x, int y, int z) {
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x - .5f, y + .5f, z + .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z));

		vertices.add(x + .5f, y + .5f, z + .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z));

		vertices.add(x + .5f, y + .5f, z - .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z));

		vertices.add(x - .5f, y + .5f, z - .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z));

		indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
		indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
	}

	private void addXQuad(int x, int y, int z, float direction) {
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + .5f * direction, y - .5f, z + .5f);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColor(x, y, z));

		vertices.add(x + .5f * direction, y + .5f, z + .5f);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColor(x, y, z));

		vertices.add(x + .5f * direction, y + .5f, z - .5f);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColor(x, y, z));

		vertices.add(x + .5f * direction, y - .5f, z - .5f);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColor(x, y, z));

		if (direction > 0) {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		} else {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		}
	}

	private void addZQuad(int x, int y, int z, float direction) {
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + .5f, y - .5f, z + .5f * direction);
		vertices.add(0f, 0f, direction);
		vertices.add(getColor(x, y, z));

		vertices.add(x + .5f, y + .5f, z + .5f * direction);
		vertices.add(0f, 0f, direction);
		vertices.add(getColor(x, y, z));

		vertices.add(x - .5f, y + .5f, z + .5f * direction);
		vertices.add(0f, 0f, direction);
		vertices.add(getColor(x, y, z));

		vertices.add(x - .5f, y - .5f, z + .5f * direction);
		vertices.add(0f, 0f, direction);
		vertices.add(getColor(x, y, z));

		if (direction < 0) {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		} else {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		}
	}

	private final Vector3 sand = new Vector3(1f, .8f, 0f);
	private final Vector3 grass = new Vector3(0f, .8f, .2f);
	private float[] getColor(int x, int y, int z) {
		Vector3 color = new Vector3(y < 3 ? sand : grass);
		color.scl((float) y / height);
		return new float[] {color.x, color.y, color.z, 1};
	}
}
