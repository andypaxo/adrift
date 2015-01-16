package net.softwarealchemist.adrift;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;

public class AdriftMain extends ApplicationAdapter {

	private Renderable renderable;
	private ModelBatch modelBatch;
	private PerspectiveCamera cam;
	private float time = 0;
	private Terrain terrain;

	@Override
	public void create() {
		terrain = new Terrain();
		final Mesh mesh = terrain.generateMesh();
		renderable = new Renderable();
		renderable.mesh = mesh;
		renderable.meshPartOffset = 0;
		renderable.meshPartSize = mesh.getNumIndices();
		renderable.primitiveType = GL20.GL_TRIANGLES;
		renderable.material = new Material(ColorAttribute.createDiffuse(.4f, 1, .2f, 1));
		final PointLight light = new PointLight().set(1, 1, 1, terrain.width * .5f,
				terrain.height * 3, terrain.depth * .3f, terrain.width * terrain.depth);
		renderable.environment = new Environment().add(light);
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.far = terrain.depth * 2;
		cam.update();
	}

	@Override
	public void render() {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		time += Gdx.graphics.getDeltaTime();
		// renderable.worldTransform.rotate(0, 1, 0, time * 10);

		final float rotation = time * .5f;
		cam.position.set(//
				//
				(float) (terrain.depth * .5f + Math.sin(rotation) * terrain.width), //
				terrain.height + terrain.depth / 2, //
				(float) (terrain.depth * .5f + Math.cos(rotation) * terrain.depth));
		cam.lookAt(terrain.width * .5f, 0, terrain.depth * .5f);
		cam.up.set(0, 1, 0);
		cam.update();

		modelBatch.begin(cam);
		modelBatch.render(renderable);
		modelBatch.end();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		renderable.mesh.dispose();
	}

}
