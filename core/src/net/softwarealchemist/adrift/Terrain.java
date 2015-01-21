package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.softwarealchemist.adrift.util.FloatBuffer;
import net.softwarealchemist.adrift.util.ShortBuffer;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Vector3;

public class Terrain {
	private FloatBuffer vertices;
	private ShortBuffer indices;
	private int vertexLength;

	public int width = 320, depth = 320, height;
	public double seed;

	private int[] voxelData;
	private float[] lightData;
	private double noiseScale;
	private int numPolys;
	private double caveScale;
	private double caveStretch;

	public List<Mesh> generateMeshes() {
		System.out.println("Generating...");
		long startTime = System.nanoTime();
		
		seed = Math.random() * 1000000.0;
		noiseScale = Math.random() * 5 + 3;
		caveScale = Math.random() * 5 + 3;
		caveStretch = Math.random() + .5;
		height = (int) (Math.random() * 64.0 + 32);

		voxelData = new int[width * depth * height];
		lightData = new float[width * depth * height];
		
		System.out.println(noiseScale);
		System.out.println(height);
		generateVoxelData();
		long caveStartTime = System.nanoTime();
		removeUnreachableCaves();
		System.out.println(String.format("Cave removal took %.1f seconds", (System.nanoTime() - caveStartTime) / 1000000000.0));
		addTrees();
		calculateLights();

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

	private void addTrees() {
		for (int i = 0; i < 10; i++)
			addTree((int) (Math.random() * width), (int) (Math.random() * depth));
	}

	private void addTree(int x, int z) {		
		int y;
		for (y = height - 1; y >= 0; y--)
			if (get(x, y, z) > 0)
				break;
		
		if (y < 2)
			return;
		
		for (int cY = 0; cY < 4; cY++)
			set(x, y + cY, z, 1);

		for (int cX = -1; cX <= 1; cX++)
			for (int cZ = -1; cZ <= 1; cZ++)
				for (int cY = 4; cY < 7; cY++)
					set(x + cX, y + cY, z + cZ, 1);
	}

	private Mesh generateMeshForChunk(final int chunkSize, final int startX, final int startZ) {
		for (int x = startX; x < startX + chunkSize; x++) {
			for (int z = startZ; z < startZ + chunkSize; z++) {
				for (int y = 0; y < height; y++) {
					if (get(x, y, z) > 0 && get(x, y + 1, z) == 0)
						addYQuad(x, y, z, 1);
					if (get(x, y, z) > 0 && get(x, y - 1, z) == 0)
						addYQuad(x, y, z, -1);
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

		numPolys +=  indices.length / 3;
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
		
		for (int x = 0; x < width; x++)
			for (int z = 0; z < depth; z++) {
				distFromCentre = Math
						.sqrt(((x - width * .5) * (x - width * .5) + (z - depth * .5) * (z - depth * .5)))
						* 2.0 / width;
				heightAtPoint =
						((SimplexNoise.noise((x * noiseScale / width) + seed, z * noiseScale / depth) * .5 + .5) +
						(SimplexNoise.noise((x * noiseScale * 2 / width) + seed + 10000, z * noiseScale * 2 / depth) * .2))
						* (1 - distFromCentre) - (1.0 / height);
				for (int y = 0; y < height; y++) {
					caveValue = SimplexNoise.noise((
							x * caveScale / width) + seed + 30000,
							z * caveScale / depth,
							y * caveScale / caveStretch / height) * .5 + .5;
					set(x, y, z, (y / (double) height) < heightAtPoint && caveValue < caveThreshold ? 1 : 0);
				}
			}
	}
	
	private void removeUnreachableCaves() {
		int[] groupMap = new int[voxelData.length];
		Arrays.fill(groupMap, -1);
		int maxGroup = 0;
		int groupX, groupY, groupZ, minGroup;
		boolean[][] equivalencyMatrix = new boolean[4096][4096];

		// Label all unoccupied cells with groups
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				for (int z = 0; z < depth; z++)
				{
					if (get(x, y, z) > 0)
						continue;
					
					groupX = groupY = groupZ = -1;
					
					if (x > 0 && get(x - 1, y, z) == 0)
						groupX = getInt(groupMap, x - 1, y, z);
					
					if (y < height - 1 && get(x, y + 1, z) == 0)
						groupY = getInt(groupMap, x, y + 1, z);
					
					if (z > 0 && get(x, y, z - 1) == 0)
						groupZ = getInt(groupMap, x, y, z - 1);

					minGroup = minPositive(groupX, groupY, groupZ);
					
					if (minGroup < 0)
					{
						// New group
						setInt(groupMap, x, y, z, maxGroup++);
					} else {
						// Existing group. Make sure adjacent groups are joined.
						setInt(groupMap, x, y, z, minGroup);
						if (groupX >= 0 && groupX != minGroup)
							equivalencyMatrix[minGroup][groupX] = true;
						if (groupY >= 0 && groupY != minGroup)
							equivalencyMatrix[minGroup][groupY] = true;
						if (groupZ >= 0 && groupZ != minGroup)
							equivalencyMatrix[minGroup][groupZ] = true;
					}
				}
			}
		}
		
		System.out.println(String.format("Finished with %d groups", maxGroup));
		
		// Build full equivalency (group 0 only, as that's all we need)
		equivalencyMatrix[0][0] = true;
		for (int groupN = 1; groupN <= maxGroup; groupN++)
			equivalencyMatrix[0][groupN] |= equivalencyMatrix[groupN][0];
		
		for (int x = 0; x < width; x++)
			for (int z = 0; z < depth; z++)
				for (int y = 0; y < height; y++)
					if (get(x, y, z) == 0 && !(equivalencyMatrix[0][getInt(groupMap, x, y, z)]))
						set(x, y, z, 1);
	}
	
	private int minPositive(int a, int b, int c) {
		// All +ve
		if (a >= 0 && b >= 0 && c >= 0)
			return a > b ? (b > c ? c : b) : (a > c ? c : a);
		// 2 +ve
		if (a >= 0 && b >= 0)
			return a > b ? b : a;
		if (b >= 0 && c >= 0)
			return b > c ? c : b;
		if (a >= 0 && c >= 0)
			return a > c ? c : a;
		// 1 +ve
		if (a >= 0) return a;
		if (b >= 0) return b;
		if (c >= 0) return c;
		// Nope
		return -1;
	}
	
	private void calculateLights() {
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				for (int z = 0; z < depth; z++) {
					if (y == height - 1)
						setLight(x, y, z, 1);
					else {
						float lightLevel = 0;
						for (int dx = -1; dx <= 1; dx++)
							for (int dz = -1; dz <= 1; dz++)
								lightLevel += (get(x + dx, y + 1, z + dz) == 0 ? getLight(x, y + 1, z) : 0.1f) / 9f;
						setLight(x, y, z, lightLevel);
					}
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
		if (y >= height || y < 0 || x >= width || x < 0 || z >= depth || z < 0)
			return;
		
		voxelData[y * width * depth + z * width + x] = val;
	}

	private float getLight(int x, int y, int z) {
		if (y >= height || y < 0 || x >= width || x < 0 || z >= depth || z < 0)
			return 0;

		return lightData[y * width * depth + z * width + x];
	}
	
	private void setLight(int x, int y, int z, float val) {
		lightData[y * width * depth + z * width + x] = val;
	}

	private int getInt(int[] vals, int x, int y, int z) {
		return vals[y * width * depth + z * width + x];
	}

	private void setInt(int[] vals, int x, int y, int z, int val) {
		vals[y * width * depth + z * width + x] = val;
	}
	
	private void addYQuad(int x, int y, int z, float direction) {
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x - .5f, y + .5f * direction, z + .5f);
		vertices.add(0f, direction, 0f);
		vertices.add(getColor(x, y, z, -1, 1));

		vertices.add(x + .5f, y + .5f * direction, z + .5f);
		vertices.add(0f, direction, 0f);
		vertices.add(getColor(x, y, z, 1, 1));

		vertices.add(x + .5f, y + .5f * direction, z - .5f);
		vertices.add(0f, direction, 0f);
		vertices.add(getColor(x, y, z, 1, -1));

		vertices.add(x - .5f, y + .5f * direction, z - .5f);
		vertices.add(0f, direction, 0f);
		vertices.add(getColor(x, y, z, -1, -1));

		if (direction > 0) {
			indices.add(indexBase, (short) (indexBase + 1), (short) (indexBase + 2));
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 3));
		} else {
			indices.add(indexBase, (short) (indexBase + 2), (short) (indexBase + 1));
			indices.add(indexBase, (short) (indexBase + 3), (short) (indexBase + 2));
		}
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

	private final Vector3 sand = new Vector3(1f, .8f, .6f);
	private final Vector3 grass = new Vector3(.5f, .6f, .2f);
	private final Vector3 snow = new Vector3(.9f, .9f, 1f);
	
	private float[] getColor(int x, int y, int z) {
		colorScratchVector.set(y < 3 ? sand : (y < height - 20 ? grass : snow));
		colorScratchVector.scl(getLight(x, y, z));
		colorScratchArray.reset();
		colorScratchArray.add(colorScratchVector.x, colorScratchVector.y, colorScratchVector.z, 1);
		return colorScratchArray.buffer;	
	}
	
	private Vector3 colorScratchVector = new Vector3();
	private FloatBuffer colorScratchArray = new FloatBuffer(4);
	private float[] getColor(int x, int y, int z, int xBias, int zBias) {
		colorScratchVector.set(y < 3 ? sand : (y < height - 26 ? grass : (SimplexNoise.noise(x * noiseScale / width, z * noiseScale / depth + seed) > 0 ? grass : snow)));
		colorScratchVector.scl(getLight(x, y, z) * .6f + getLight(x + xBias, y, z + zBias) * .4f);
		colorScratchArray.reset();
		colorScratchArray.add(colorScratchVector.x, colorScratchVector.y, colorScratchVector.z, 1);
		return colorScratchArray.buffer;
	}
}
