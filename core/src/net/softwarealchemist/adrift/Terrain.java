package net.softwarealchemist.adrift;

import java.util.Arrays;
import java.util.Random;

import net.softwarealchemist.adrift.dto.TerrainConfig;
import net.softwarealchemist.adrift.util.IntMapReader;

public class Terrain {
	private int[] voxelData;
	private float[] lightData;
	public TerrainConfig configuration = new TerrainConfig();
	private Random rng;
	IntMapReader regions;

	public void generate() {
		System.out.println("Generating voxel data");
		long startTime = System.nanoTime();

		voxelData = new int[configuration.width * configuration.depth * configuration.height];
		lightData = new float[voxelData.length];

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
		
		for (int x = 0; x < configuration.width; x++)
			for (int z = 0; z < configuration.depth; z++) {
				distFromCentre = Math
						.sqrt(((x - configuration.width * .5) * (x - configuration.width * .5) + (z - configuration.depth * .5) * (z - configuration.depth * .5)))
						* 2.0 / configuration.width;
				heightAtPoint =
						((SimplexNoise.noise((x * configuration.noiseScale / configuration.width) + configuration.seed, z * configuration.noiseScale / configuration.depth) * .5 + .5) +
						(SimplexNoise.noise((x * configuration.noiseScale * 2 / configuration.width) + configuration.seed + 10000, z * configuration.noiseScale * 2 / configuration.depth) * .2))
						* (1 - distFromCentre) - (1.0 / configuration.height);
				for (int y = 0; y < configuration.height; y++) {
					caveValue = SimplexNoise.noise((
							x * configuration.caveScale / configuration.width) + configuration.seed + 30000,
							z * configuration.caveScale / configuration.depth,
							y * configuration.caveScale / configuration.caveStretch / configuration.height) * .5 + .5;
					set(x, y, z, (y / (double) configuration.height) < heightAtPoint && caveValue < caveThreshold ? 1 : 0);
				}
			}
	}
	
	private void removeUnreachableCaves() {
		int[] regionData = new int[voxelData.length];
		regions = new IntMapReader(configuration.width, configuration.depth, configuration.height, 0, regionData);
		Arrays.fill(regionData, -1);
		int maxGroup = 0;
		int groupX, groupY, groupZ, minGroup;
		boolean[][] equivalencyMatrix = new boolean[4096][4096];

		// Label all unoccupied cells with groups
		for (int y = configuration.height - 1; y >= 0; y--) {
			for (int x = 0; x < configuration.width; x++) {
				for (int z = 0; z < configuration.depth; z++)
				{
					if (get(x, y, z) > 0)
						continue;
					
					groupX = groupY = groupZ = -1;
					
					if (x > 0 && get(x - 1, y, z) == 0)
						groupX = regions.getInt(x - 1, y, z);
					
					if (y < configuration.height - 1 && get(x, y + 1, z) == 0)
						groupY = regions.getInt(x, y + 1, z);
					
					if (z > 0 && get(x, y, z - 1) == 0)
						groupZ = regions.getInt(x, y, z - 1);

					minGroup = minPositive(groupX, groupY, groupZ);
					
					if (minGroup < 0)
					{
						// New group
						regions.setInt(x, y, z, maxGroup++);
					} else {
						// Existing group. Make sure adjacent groups are joined.
						regions.setInt(x, y, z, minGroup);
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
		
		for (int x = 0; x < configuration.width; x++)
			for (int z = 0; z < configuration.depth; z++)
				for (int y = 0; y < configuration.height; y++)
					if (get(x, y, z) == 0 && !(equivalencyMatrix[0][regions.getInt(x, y, z)]))
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
			for (int x = 0; x < configuration.width; x++) {
				for (int z = 0; z < configuration.depth; z++) {
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
		if (y >= configuration.height || y < 0 || x >= configuration.width || x < 0 || z >= configuration.depth || z < 0)
			return 0;

		return voxelData[y * configuration.width * configuration.depth + z * configuration.width + x];
	}

	private void set(int x, int y, int z, int val) {
		if (y >= configuration.height || y < 0 || x >= configuration.width || x < 0 || z >= configuration.depth || z < 0)
			return;
		
		voxelData[y * configuration.width * configuration.depth + z * configuration.width + x] = val;
	}

	public float getLight(int x, int y, int z) {

		if (y >= configuration.height || x >= configuration.width || x < 0 || z >= configuration.depth || z < 0)
			return 1;
		
		if (y < 0)
			return getLight(x, 0, z);
		
		return lightData[y * configuration.width * configuration.depth + z * configuration.width + x];
	}
	
	private void setLight(int x, int y, int z, float val) {
		lightData[y * configuration.width * configuration.depth + z * configuration.width + x] = val;
	}

	private void addTrees() {
		rng = new Random((int) configuration.seed);
		for (int i = 0; i < configuration.width / 2; i++)
			addTree((int) (rng.nextFloat() * configuration.width), (int) (rng.nextFloat() * configuration.depth));
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
		configuration.height = (int) (Math.random() * 16.0 + 64);
		int sizeClass = (int) (Math.random() * 30.0);
		int size = 160 + 32 * sizeClass;
		configuration.width = size;
		configuration.depth = size;
	}

	public void configure(TerrainConfig config) {
		configuration = config;
	}

	public TerrainConfig getConfiguration() {
		return configuration;
	}
}
