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
	public static final int chunkSize = 32;

	private int numPolys;
	private int vertexLength;
	private FloatBuffer vertices;
	private ShortBuffer indices;

	public MeshGenerator(Terrain terrain) {
		this.terrain = terrain;
		width = terrain.configuration.width;
		depth = terrain.configuration.depth;
		height = terrain.configuration.height;

		vertexLength = VertexAttribute.Position().numComponents
				+ VertexAttribute.Normal().numComponents
				+ VertexAttribute.ColorUnpacked().numComponents;
		vertices = new FloatBuffer(chunkSize * chunkSize * vertexLength * 64);
		indices = new ShortBuffer(chunkSize * chunkSize * 64);
	}

	public List<Mesh> generateMeshes() {
		System.out.println("Generating vertex data");
		long startTime = System.nanoTime();
		
		final ArrayList<Mesh> result = new ArrayList<Mesh>();

		for (int x = 0; x < width; x += chunkSize)
			for (int z = 0; z < depth; z += chunkSize)
				result.add(generateMeshForChunk(x, z));
		
		System.out.println(String.format("Generated %d triangles", numPolys));
		System.out.println(String.format("Generation complete in %.1f seconds", (System.nanoTime() - startTime) / 1000000000.0));
		return result;
	}
	
	public List<Mesh> generateMeshesForWater() {
		final ArrayList<Mesh> result = new ArrayList<Mesh>();
		final int borderSize = 3;
		for (int x = -chunkSize * borderSize; x < width + chunkSize * borderSize; x += chunkSize)
			for (int z = -chunkSize * borderSize; z < depth + chunkSize * borderSize; z += chunkSize)
				result.add(generateMeshForWaterChunk(x, z));
		return result;
	}

	private Mesh generateMeshForWaterChunk(final int startX, final int startZ) {
		for (int x = startX; x < startX + chunkSize; x++)
			for (int z = startZ; z < startZ + chunkSize; z++)
				if (terrain.get(x, 0, z) == 0)
					addYQuad(x, -1, z, 1, 3);

		return createMeshFromCurrentData();
	}

	private Mesh generateMeshForChunk(final int startX, final int startZ) {
		for (int x = startX; x < startX + chunkSize; x++) {
			for (int z = startZ; z < startZ + chunkSize; z++) {
				for (int y = 0; y < height; y++) {
					int blockType = terrain.get(x, y, z);
					if (blockType > 0 && terrain.get(x, y + 1, z) == 0)
						addYQuad(x, y, z, 1, blockType);
					if (y > 0 && blockType > 0 && terrain.get(x, y - 1, z) == 0)
						addYQuad(x, y, z, -1, blockType);
					if (blockType > 0 && terrain.get(x + 1, y, z) == 0)
						addXQuad(x, y, z, 1, blockType);
					if (blockType > 0 && terrain.get(x - 1, y, z) == 0)
						addXQuad(x, y, z, -1, blockType);
					if (blockType > 0 && terrain.get(x, y, z + 1) == 0)
						addZQuad(x, y, z, 1, blockType);
					if (blockType > 0 && terrain.get(x, y, z - 1) == 0)
						addZQuad(x, y, z, -1, blockType);
				}
			}
		}

		return createMeshFromCurrentData();
	}

	private Mesh createMeshFromCurrentData() {
		numPolys +=  indices.length / 3;
		final Mesh mesh = new Mesh(true, vertices.length, indices.length,
				VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
		mesh.setVertices(vertices.buffer, 0, vertices.length);
		mesh.setIndices(indices.buffer, 0, indices.length);
		vertices.reset();
		indices.reset();
		return mesh;
	}

	private void addXQuad(int x, int y, int z, float direction, int blockType) {
		float offset = direction > 0 ? 1 : 0;
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + offset, y, z + 1);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, -1, 1, blockType));

		vertices.add(x + offset, y + 1, z + 1);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, 1, 1, blockType));

		vertices.add(x + offset, y + 1, z);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, 1, -1, blockType));

		vertices.add(x + offset, y, z);
		vertices.add(direction, 0f, 0f);
		vertices.add(getColorForXFace(x + (int)direction, y, z, -1, -1, blockType));

		if (direction > 0) {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		} else {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		}
	}
	
	private void addYQuad(int x, int y, int z, float direction, int blockType) {
		float offset = direction > 0 ? 1 : 0;
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x, y + offset, z + 1);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, -1, 1, blockType));

		vertices.add(x + 1, y + offset, z + 1);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, 1, 1, blockType));

		vertices.add(x + 1, y + offset, z);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, 1, -1, blockType));

		vertices.add(x, y + offset, z);
		vertices.add(0f, direction, 0f);
		vertices.add(getColorForYFace(x, y, z, -1, -1, blockType));

		if (direction > 0) {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		} else {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		}
	}

	private void addZQuad(int x, int y, int z, float direction, int blockType) {
		float offset = direction > 0 ? 1 : 0;
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x + 1, y, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, 1, -1, blockType));

		vertices.add(x + 1, y + 1, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, 1, 1, blockType));

		vertices.add(x, y + 1, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, -1, 1, blockType));

		vertices.add(x, y, z + offset);
		vertices.add(0f, 0f, direction);
		vertices.add(getColorForZFace(x, y, z + (int)direction, -1, -1, blockType));

		if (direction < 0) {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		} else {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		}
	}

	private final Vector3 water = new Vector3(.6f, 1f, 1);
	private final Vector3 sand = new Vector3(1f, .8f, .6f);
	private final Vector3 grass = new Vector3(.5f, .6f, .2f);
	private final Vector3 snow = new Vector3(.9f, .9f, 1f);

	private Vector3 colorScratchVector = new Vector3();
	private FloatBuffer colorScratchArray = new FloatBuffer(4);
	private float[] debugColor = new float[] {2, 2, 0, 1};
	
	private float[] getColorForXFace(int x, int y, int z, int yBias, int zBias, int blockType) {
		if (blockType == 255)
			return debugColor;
		
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
	
	private float[] getColorForYFace(int x, int y, int z, int xBias, int zBias, int blockType) {
		if (blockType == 255)
			return debugColor;
		
		colorScratchVector.set(y < 0 ? water : (y < 3 ? sand : (y < height - 26 ? grass : (SimplexNoise.noise(x * 32 / width, z * 32 / depth) > 0 ? grass : snow))));
		colorScratchVector.scl(
			terrain.getLight(x, y, z) * .25f
			+ terrain.getLight(x + xBias, y, z + zBias) * .25f
			+ terrain.getLight(x + xBias, y, z) * .25f
			+ terrain.getLight(x, y, z + zBias) * .25f);
		colorScratchArray.reset();
		colorScratchArray.add(colorScratchVector.x, colorScratchVector.y, colorScratchVector.z, 1);
		return colorScratchArray.buffer;
	}
	
	private float[] getColorForZFace(int x, int y, int z, int xBias, int yBias, int blockType) {
		if (blockType == 255)
			return debugColor;
		
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
