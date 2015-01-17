package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;

public class Terrain {
	private FloatBuffer vertices;
	private ShortBuffer indices;
	private int vertexLength;

	public int width = 256, depth = 256, height = 32;

	private int[] voxelData = new int[width * depth * height];

	public List<Mesh> generateMeshes() {
		System.out.println("Generating...");
		generateVoxelData();

		vertexLength = VertexAttribute.Position().numComponents
				+ VertexAttribute.Normal().numComponents;

		final ArrayList<Mesh> result = new ArrayList<Mesh>();
		final int chunkSize = 32;
		vertices = new FloatBuffer(chunkSize * chunkSize * 64);
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

		System.out.println(String.format("Generated %d vertices and %d indices", vertices.length, indices.length));
		final Mesh mesh = new Mesh(true, vertices.length, indices.length,
				VertexAttribute.Position(), VertexAttribute.Normal());
		mesh.setVertices(vertices.buffer, 0, vertices.length);
		mesh.setIndices(indices.buffer, 0, indices.length);
		vertices.reset();
		indices.reset();
		return mesh;
	}

	private void generateVoxelData() {
		double heightAtPoint, distFromCentre;
		final double noiseScale = 3;
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				for (int y = 0; y < height; y++) {
					distFromCentre = Math
							.sqrt(((x - width * .5) * (x - width * .5) + (z - depth * .5)
									* (z - depth * .5)))
							/ width;

					heightAtPoint = (SimplexNoise.noise(x * noiseScale / width, z * noiseScale / depth) * .5 + .5)
							* (1 - distFromCentre);
					set(x, y, z, (y / (double) height) < heightAtPoint || y == 0 ? 1 : 0);
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

	private void addYQuad(float x, float y, float z) {
		short indexBase = (short) (vertices.length / vertexLength);

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

	private void addXQuad(float x, float y, float z, float direction) {
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + .5f * direction);
		vertices.add(y - .5f);
		vertices.add(z + .5f);
		vertices.add(direction);
		vertices.add(0f);
		vertices.add(0f);

		vertices.add(x + .5f * direction);
		vertices.add(y + .5f);
		vertices.add(z + .5f);
		vertices.add(direction);
		vertices.add(0f);
		vertices.add(0f);

		vertices.add(x + .5f * direction);
		vertices.add(y + .5f);
		vertices.add(z - .5f);
		vertices.add(direction);
		vertices.add(0f);
		vertices.add(0f);

		vertices.add(x + .5f * direction);
		vertices.add(y - .5f);
		vertices.add(z - .5f);
		vertices.add(direction);
		vertices.add(0f);
		vertices.add(0f);

		if (direction > 0) {
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 2)));
			indices.add(new Short((short) (indexBase + 1)));
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 3)));
			indices.add(new Short((short) (indexBase + 2)));
		} else {
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 1)));
			indices.add(new Short((short) (indexBase + 2)));
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 2)));
			indices.add(new Short((short) (indexBase + 3)));
		}
	}

	private void addZQuad(float x, float y, float z, float direction) {
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + .5f);
		vertices.add(y - .5f);
		vertices.add(z + .5f * direction);
		vertices.add(0f);
		vertices.add(0f);
		vertices.add(direction);

		vertices.add(x + .5f);
		vertices.add(y + .5f);
		vertices.add(z + .5f * direction);
		vertices.add(0f);
		vertices.add(0f);
		vertices.add(direction);

		vertices.add(x - .5f);
		vertices.add(y + .5f);
		vertices.add(z + .5f * direction);
		vertices.add(0f);
		vertices.add(0f);
		vertices.add(direction);

		vertices.add(x - .5f);
		vertices.add(y - .5f);
		vertices.add(z + .5f * direction);
		vertices.add(0f);
		vertices.add(0f);
		vertices.add(direction);

		if (direction < 0) {
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 2)));
			indices.add(new Short((short) (indexBase + 1)));
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 3)));
			indices.add(new Short((short) (indexBase + 2)));
		} else {
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 1)));
			indices.add(new Short((short) (indexBase + 2)));
			indices.add(indexBase);
			indices.add(new Short((short) (indexBase + 2)));
			indices.add(new Short((short) (indexBase + 3)));
		}
	}
}
