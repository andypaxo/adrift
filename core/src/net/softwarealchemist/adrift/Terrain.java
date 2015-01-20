package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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

	public List<Mesh> generateMeshes() {
		System.out.println("Generating...");
		
		seed = 100;//Math.random() * 1000000.0;
		noiseScale = 8;//Math.random() * 5 + 3;
		height = 96;//(int) (Math.random() * 64.0 + 32);

		voxelData = new int[width * depth * height];
		lightData = new float[width * depth * height];
		
		System.out.println(noiseScale);
		System.out.println(height);
		generateVoxelData();
		long startTime = System.nanoTime();
		removeUnreachableCaves();
		System.out.println(String.format("Cave removal took %.1f seconds", (System.nanoTime() - startTime) / 1000000000.0));
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
			for (int z = 0; z < depth; z++)
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
	
	private void removeUnreachableCaves() {
		boolean[] reachableAir = new boolean[voxelData.length];
		floodfill(reachableAir);

		for (int x = 0; x < width; x++)
			for (int z = 0; z < depth; z++)
				for (int y = 0; y < height; y++)
					if (get(x, y, z) == 0 && !getBoolean(reachableAir, x, y, z))
						set(x, y, z, 1);
	}
	
	private void floodfill(boolean[] vals)
	{
	    Stack<IntVector3> stack = new Stack<IntVector3>();
	    stack.push(new IntVector3(0, height - 1, 0));
	    int x, y, z, block;
	    boolean alreadyFilled;
	    while (stack.size() > 0)
	    {
	    	IntVector3 p = stack.pop();
	    	x = p.x;
	    	y = p.y;
	    	z = p.z;
	    	if (y < 0 || y > height - 1 || x < 0 || x > width - 1 || z < 0 || z > depth - 1)
	    		continue;
	    	block = get(x, y, z);
	    	alreadyFilled = getBoolean(vals, x, y, z);
	    	if (block == 0 && !alreadyFilled)
	    	{
	    		setBoolean(vals, x, y, z);
	    		stack.push(new IntVector3(x + 1, y, z));
	    		stack.push(new IntVector3(x - 1, y, z));
	    		stack.push(new IntVector3(x, y + 1, z));
	    		stack.push(new IntVector3(x, y - 1, z));
	    		stack.push(new IntVector3(x, y, z + 1));
	    		stack.push(new IntVector3(x, y, z - 1));
	    	}
	    }
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
	
	private boolean getBoolean(boolean[] vals, int x, int y, int z) {
		return vals[y * width * depth + z * width + x]; 
	}

	private void setBoolean(boolean[] vals, int x, int y, int z) {
		vals[y * width * depth + z * width + x] = true;
	}
	
	private void addYQuad(int x, int y, int z) {
		short indexBase = (short) (vertices.length / vertexLength);

		vertices.add(x - .5f, y + .5f, z + .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z, -1, 1));

		vertices.add(x + .5f, y + .5f, z + .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z, 1, 1));

		vertices.add(x + .5f, y + .5f, z - .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z, 1, -1));

		vertices.add(x - .5f, y + .5f, z - .5f);
		vertices.add(0f, 1f, 0f);
		vertices.add(getColor(x, y, z, -1, -1));

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
