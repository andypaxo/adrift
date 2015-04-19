package net.softwarealchemist.adrift.util;

import net.softwarealchemist.adrift.model.Terrain;

import com.badlogic.gdx.math.Vector3;

public class RayCaster {
	public static Vector3 cast(Vector3 origin, Vector3 direction, float radius, Terrain terrain) {
		// Cube containing origin point.
		int x = (int) Math.floor(origin.x);
		int y = (int) Math.floor(origin.y);
		int z = (int) Math.floor(origin.z);
		// Break out direction vector.
		float dx = direction.x;
		float dy = direction.y;
		float dz = direction.z;
		// Direction to increment x,y,z when stepping.
		int stepX = signum(dx);
		int stepY = signum(dy);
		int stepZ = signum(dz);
		// See description above. The initial values depend on the fractional
		// part of the origin.
		float tMaxX = intbound(origin.x, dx);
		float tMaxY = intbound(origin.y, dy);
		float tMaxZ = intbound(origin.z, dz);
		// The change in t when taking a step (always positive).
		float tDeltaX = (float)stepX/dx;
		float tDeltaY = (float)stepY/dy;
		float tDeltaZ = (float)stepZ/dz;
		// World boundaries
		float wx = terrain.configuration.width;
		float wy = terrain.configuration.height;
		float wz = terrain.configuration.depth;
		// Buffer for reporting faces to the callback.
		Vector3 face = new Vector3();

		// Avoids an infinite loop.
		if (dx == 0 && dy == 0 && dz == 0) {
			System.out.println("Raycast in zero direction!");
			return null;
		}

		// Rescale from units of 1 cube-edge to units of 'direction' so we can
		// compare with 't'.
		radius /= Math.sqrt(dx*dx+dy*dy+dz*dz);

		while (/* ray has not gone past bounds of world */
				(stepX > 0 ? x < wx : x >= 0) &&
				(stepY > 0 ? y < wy : y >= 0) &&
				(stepZ > 0 ? z < wz : z >= 0)) {

			// Return value, unless we are not *yet* within the bounds of the world.
			if (!(x < 0 || y < 0 || z < 0 || x >= wx || y >= wy || z >= wz))
				if (terrain.get(x, y, z) > 0)
					return new Vector3(x, y, z);

			// tMaxX stores the t-value at which we cross a cube boundary along the
			// X axis, and similarly for Y and Z. Therefore, choosing the least tMax
			// chooses the closest cube boundary. Only the first case of the four
			// has been commented in detail.
			if (tMaxX < tMaxY) {
				if (tMaxX < tMaxZ) {
					if (tMaxX > radius) break;
					// Update which cube we are now in.
					x += stepX;
					// Adjust tMaxX to the next X-oriented boundary crossing.
					tMaxX += tDeltaX;
					// Record the normal vector of the cube face we entered.
					face.x = -stepX;
					face.y = 0;
					face.z = 0;
				} else {
					if (tMaxZ > radius) break;
					z += stepZ;
					tMaxZ += tDeltaZ;
					face.x = 0;
					face.y = 0;
					face.z = -stepZ;
				}
			} else {
				if (tMaxY < tMaxZ) {
					if (tMaxY > radius) break;
					y += stepY;
					tMaxY += tDeltaY;
					face.x = 0;
					face.y = -stepY;
					face.z = 0;
				} else {
					// Identical to the second case, repeated for simplicity in
					// the conditionals.
					if (tMaxZ > radius) break;
					z += stepZ;
					tMaxZ += tDeltaZ;
					face.x = 0;
					face.y = 0;
					face.z = -stepZ;
				}
			}
		}

		return null;
	}

	private static float intbound(float s, float ds) {
		// Find the smallest positive t such that s+t*ds is an integer.
		if (ds < 0) {
			return intbound(-s, -ds);
		} else {
			s = mod(s, 1);
			// problem is now s+t*ds = 1
			return (1-s)/ds;
		}
	}

	private static int signum(float x) {
		return x > 0 ? 1 : x < 0 ? -1 : 0;
	}

	private static float mod(float value, float modulus) {
		return (value % modulus + modulus) % modulus;
	}
}
