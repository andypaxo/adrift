package net.softwarealchemist.adrift;

import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class AdriftMain extends ApplicationAdapter {

	private Model model;
	private ModelInstance modelInstance;
	private ModelBatch modelBatch;
	private PerspectiveCamera cam;
	private float time = 0;
	private Terrain terrain;
	private Environment environment;

	@Override
	public void create() {
		createTerrain();
		createEnvironment();
		
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.far = terrain.depth * 2;
		cam.update();
	}

	private void createEnvironment() {
//		final PointLight light = new PointLight().set(1, 1, 1, terrain.width * .5f,
//				terrain.height * 3, terrain.depth * .3f, terrain.width * terrain.depth);
		environment = new Environment()
			.add(new DirectionalLight().set(1f, 1f, 1f, .7f, -1f, .4f))
			.add(new DirectionalLight().set(.3f, .3f, .3f, -.4f, -1f, -.7f));
		// environment.set(ColorAttribute.createAmbient(1, 1, 1, 1));
	}

	private void createTerrain() {
		terrain = new Terrain();
		final Material groundMaterial = new Material(
				ColorAttribute.createDiffuse(1, 1, 1, 1),
				ColorAttribute.createAmbient(11f, 1.1f, 1.1f, 1));
		final List<Mesh> meshes = terrain.generateMeshes();

		final ModelBuilder modelBuilder = new ModelBuilder();
		int i = 0;
		modelBuilder.begin();
		for (Mesh mesh : meshes) {
			modelBuilder.part(Integer.toString(i++), mesh, GL20.GL_TRIANGLES, groundMaterial);
		}
		model = modelBuilder.end();
		modelInstance = new ModelInstance(model);
	}

	@Override
	public void render() {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		time += Gdx.graphics.getDeltaTime();
		// renderable.worldTransform.rotate(0, 1, 0, time * 10);

		final float rotation = time * .2f;
		cam.position.set(//
				//
				(float) (terrain.depth * .5f + (Math.sin(rotation) * terrain.width) * .6f), //
				terrain.height * 1.2f, //
				(float) (terrain.depth * .5f + (Math.cos(rotation) * terrain.depth) * .6f));
		cam.lookAt(terrain.width * .5f, 0, terrain.depth * .5f);
		cam.up.set(0, 1, 0);
		cam.update();

		modelBatch.begin(cam);
		modelBatch.render(modelInstance, environment);
		modelBatch.end();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		model.dispose();
	}

}
