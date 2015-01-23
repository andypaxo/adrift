package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.util.FloatBuffer;
import net.softwarealchemist.adrift.util.ShortBuffer;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Vector3;

public class MeshGenerator {
	Terrain terrain;
	public int width, depth, height;

	private int numPolys;
	private int vertexLength;
	private FloatBuffer vertices;
	private ShortBuffer indices;

	public MeshGenerator(Terrain terrain) {
		this.terrain = terrain;
		width = terrain.width;
		depth = terrain.depth;
		height = terrain.height;
	}

	public List<Mesh> generateMeshes() {
		System.out.println("Generating vertex data");
		long startTime = System.nanoTime();
		
		vertexLength = VertexAttribute.Position().numComponents
				+ VertexAttribute.Normal().numComponents
				+ VertexAttribute.ColorUnpacked().numComponents;

		final ArrayList<Mesh> result = new ArrayList<Mesh>();
		final int chunkSize = 32;
		vertices = new FloatBuffer(chunkSize * chunkSize * vertexLength * 64);
		indices = new ShortBuffer(chunkSize * chunkSize * 64);

		for (int x = 0; x < width; x += chunkSize)
			for (int z = 0; z < depth; z += chunkSize)
				result.add(generateMeshForChunk(chunkSize, x, z));
		
		System.out.println(String.format("Generated %d triangles", numPolys));
		System.out.println(String.format("Generation complete in %.1f seconds", (System.nanoTime() - startTime) / 1000000000.0));
		return result;
	}

	private Mesh generateMeshForChunk(final int chunkSize, final int startX, final int startZ) {
		for (int x = startX; x < startX + chunkSize; x++) {
			for (int z = startZ; z < startZ + chunkSize; z++) {
				for (int y = 0; y < height; y++) {
					if (terrain.get(x, y, z) > 0 && terrain.get(x, y + 1, z) == 0)
						addYQuad(x, y, z, 1);
					if (y > 0 && terrain.get(x, y, z) > 0 && terrain.get(x, y - 1, z) == 0)
						addYQuad(x, y, z, -1);
					if (terrain.get(x, y, z) > 0 && terrain.get(x + 1, y, z) == 0)
						addXQuad(x, y, z, 1);
					if (terrain.get(x, y, z) > 0 && terrain.get(x - 1, y, z) == 0)
						addXQuad(x, y, z, -1);
					if (terrain.get(x, y, z) > 0 && terrain.get(x, y, z + 1) == 0)
						addZQuad(x, y, z, 1);
					if (terrain.get(x, y, z) > 0 && terrain.get(x, y, z - 1) == 0)
						addZQuad(x, y, z, -1);
				}
			}
		}

		numPolys +=  indices.length / 3;
		final Mesh mesh = new Mesh(true, vertices.length, indices.length,
				VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
		mesh.setVertices(vertices.buffer, 0, vertices.length);
		mesh.setIndices(indices.buffer, 0, indices.length);
		vertices.reset();
		indices.reset();
		return mesh;
	}

	private void addXQuad(int x, int y, int z, float direction) {
		float offset = direction > 0 ? 1 : 0;
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + offset, y, z + 1);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, -1, 1));

		vertices.add(x + offset, y + 1, z + 1);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, 1, 1));

		vertices.add(x + offset, y + 1, z);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, 1, -1));

		vertices.add(x + offset, y, z);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, -1, -1));

		if (direction > 0) {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		} else {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		}
	}
	
	private void addYQuad(int x, int y, int z, float direction) {
		float offset = direction > 0 ? 1 : 0;
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x, y + offset, z + 1);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, -1, 1));

		vertices.add(x + 1, y + offset, z + 1);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, 1, 1));

		vertices.add(x + 1, y + offset, z);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, 1, -1));

		vertices.add(x, y + offset, z);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, -1, -1));

		if (direction > 0) {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		} else {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		}
	}

	private void addZQuad(int x, int y, int z, float direction) {
		float offset = direction > 0 ? 1 : 0;
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + 1, y, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, 1, -1));

		vertices.add(x + 1, y + 1, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, 1, 1));

		vertices.add(x, y + 1, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, -1, 1));

		vertices.add(x, y, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, -1, -1));

		if (direction < 0) {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		} else {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		}
	}

	private final Vector3 sand = new Vector3(1f, .8f, .6f);
	private final Vector3 grass = new Vector3(.5f, .6f, .2f);
	private final Vector3 snow = new Vector3(.9f, .9f, 1f);

	private Vector3 colorScratchVector = new Vector3();
	private FloatBuffer colorScratchArray = new FloatBuffer(4);
	
	private float[] getColorForXFace(int x, int y, int z, int yBias, int zBias) {
		colorScratchVector.set(y < 3 ? sand : (y < height - 16 ? grass : (SimplexNoise.noise(x * 32 / width, z * 32 / depth) > 0 ? grass : snow)));
		colorScratchVector.scl(
				terrain.getLight(x, y, z) * .25f
				+ terrain.getLight(x, y + yBias, z + zBias) * .25f
				+ terrain.getLight(x, y + yBias, z) * .25f
				+ terrain.getLight(x, y, z + zBias) * .25f);
		colorScratchArray.reset();
		colorScratchArray.add(colorScratchVector.x, colorScratchVector.y, colorScratchVector.z, 1);
		return colorScratchArray.buffer;
	}
	
	private float[] getColorForYFace(int x, int y, int z, int xBias, int zBias) {
		colorScratchVector.set(y < 3 ? sand : (y < height - 26 ? grass : (SimplexNoise.noise(x * 32 / width, z * 32 / depth) > 0 ? grass : snow)));
		colorScratchVector.scl(
			terrain.getLight(x, y, z) * .25f
			+ terrain.getLight(x + xBias, y, z + zBias) * .25f
			+ terrain.getLight(x + xBias, y, z) * .25f
			+ terrain.getLight(x, y, z + zBias) * .25f);
		colorScratchArray.reset();
		colorScratchArray.add(colorScratchVector.x, colorScratchVector.y, colorScratchVector.z, 1);
		return colorScratchArray.buffer;
	}
	
	private float[] getColorForZFace(int x, int y, int z, int xBias, int yBias) {
		colorScratchVector.set(y < 3 ? sand : (y < height - 16 ? grass : (SimplexNoise.noise(x * 32 / width, z * 32 / depth) > 0 ? grass : snow)));
		colorScratchVector.scl(
			terrain.getLight(x, y, z) * .25f
			+ terrain.getLight(x + xBias, y + yBias, z) * .25f
			+ terrain.getLight(x + xBias, y, z) * .25f
			+ terrain.getLight(x, y + yBias, z) * .25f);
		colorScratchArray.reset();
		colorScratchArray.add(colorScratchVector.x, colorScratchVector.y, colorScratchVector.z, 1);
		return colorScratchArray.buffer;
	}
}
