package net.softwarealchemist.adrift;

import java.util.Arrays;
import java.util.Random;

import net.softwarealchemist.adrift.dto.TerrainConfig;

public class Terrain {
	public int width = 320, depth = 320;

	private int[] voxelData;
	private float[] lightData;
	public TerrainConfig configuration = new TerrainConfig();

	public void generate() {
		System.out.println("Generating voxel data");
		long startTime = System.nanoTime();

		voxelData = new int[width * depth * configuration.height];
		lightData = new float[width * depth * configuration.height];

		generateVoxelData();
		long caveStartTime = System.nanoTime();
		removeUnreachableCaves();
		System.out.println(String.format("Cave removal took %.1f seconds", (System.nanoTime() - caveStartTime) / 1000000000.0));
		addTrees();
		calculateLights();

		System.out.println(String.format("Generation complete in %.1f seconds", (System.nanoTime() - startTime) / 1000000000.0));
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
						((SimplexNoise.noise((x * configuration.noiseScale / width) + configuration.seed, z * configuration.noiseScale / depth) * .5 + .5) +
						(SimplexNoise.noise((x * configuration.noiseScale * 2 / width) + configuration.seed + 10000, z * configuration.noiseScale * 2 / depth) * .2))
						* (1 - distFromCentre) - (1.0 / configuration.height);
				for (int y = 0; y < configuration.height; y++) {
					caveValue = SimplexNoise.noise((
							x * configuration.caveScale / width) + configuration.seed + 30000,
							z * configuration.caveScale / depth,
							y * configuration.caveScale / configuration.caveStretch / configuration.height) * .5 + .5;
					set(x, y, z, (y / (double) configuration.height) < heightAtPoint && caveValue < caveThreshold ? 1 : 0);
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
		for (int y = configuration.height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				for (int z = 0; z < depth; z++)
				{
					if (get(x, y, z) > 0)
						continue;
					
					groupX = groupY = groupZ = -1;
					
					if (x > 0 && get(x - 1, y, z) == 0)
						groupX = getInt(groupMap, x - 1, y, z);
					
					if (y < configuration.height - 1 && get(x, y + 1, z) == 0)
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
				for (int y = 0; y < configuration.height; y++)
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
		for (int y = configuration.height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				for (int z = 0; z < depth; z++) {
					if (y == configuration.height - 1)
						setLight(x, y, z, 1);
					else {
						float lightLevel = 0;
						for (int dx = -1; dx <= 1; dx++)
							for (int dz = -1; dz <= 1; dz++)
								lightLevel += get(x + dx, y + 1, z + dz) == 0 ? getLight(x + dx, y + 1, z + dz) : 0.1f;
						lightLevel -= (get(x, y + 1, z) == 0 ? getLight(x, y + 1, z) : 0.1f);
						lightLevel /= 8f;
						setLight(x, y, z, lightLevel);
					}
				}
			}
		}
	}

	public int get(int x, int y, int z) {
		if (y >= configuration.height || y < 0 || x >= width || x < 0 || z >= depth || z < 0)
			return 0;

		return voxelData[y * width * depth + z * width + x];
	}

	private void set(int x, int y, int z, int val) {
		if (y >= configuration.height || y < 0 || x >= width || x < 0 || z >= depth || z < 0)
			return;
		
		voxelData[y * width * depth + z * width + x] = val;
	}

	public float getLight(int x, int y, int z) {

		if (y >= configuration.height || x >= width || x < 0 || z >= depth || z < 0)
			return 1;
		
		if (y < 0)
			return getLight(x, 0, z);
		
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

	private Random rng;
	private void addTrees() {
		rng = new Random((int) configuration.seed);
		for (int i = 0; i < width / 2; i++)
			addTree((int) (rng.nextFloat() * width), (int) (rng.nextFloat() * depth));
	}

	private void addTree(int x, int z) {		
		int y;
		for (y = configuration.height - 1; y >= 0; y--)
			if (get(x, y, z) > 0)
				break;
		
		if (y < 2)
			return;

		int trunkHeight = (int)(rng.nextInt(3) + 3);
		int leavesHeight = (int)(rng.nextInt(3) + 4);
		
		for (int cY = 0; cY <= trunkHeight; cY++)
			set(x, y + cY, z, 1);
		
		set(x + 1, y + trunkHeight, z, 1);
		set(x - 1, y + trunkHeight, z, 1);
		set(x, y + trunkHeight, z - 1, 1);
		set(x, y + trunkHeight, z + 1, 1);

		for (int cX = -1; cX <= 1; cX++)
			for (int cZ = -1; cZ <= 1; cZ++)
				for (int cY = trunkHeight + 1; cY < trunkHeight + leavesHeight; cY++)
					set(x + cX, y + cY, z + cZ, 1);
		
//		set(x + 1, y + trunkHeight + leavesHeight, z, 1);
//		set(x - 1, y + trunkHeight + leavesHeight, z, 1);
//		set(x, y + trunkHeight + leavesHeight, z + 1, 1);
//		set(x, y + trunkHeight + leavesHeight, z - 1, 1);
		set(x, y + trunkHeight + leavesHeight, z, 1);

	}

	public void configureRandom() {
		configuration.seed = Math.random() * 1000000.0;
		configuration.noiseScale = Math.random() * 7 + 3;
		configuration.caveScale = Math.random() * 7 + 3;
		configuration.caveStretch = Math.random() + .5;
		configuration.height = (int) (Math.random() * 64.0 + 32);
	}

	public void configure(TerrainConfig config) {
		configuration = config;
	}

	public TerrainConfig getConfiguration() {
		return configuration;
	}
}
