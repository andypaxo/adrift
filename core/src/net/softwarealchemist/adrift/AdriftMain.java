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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class AdriftMain extends ApplicationAdapter {

	private Renderable renderable;
	private ModelBatch modelBatch;
	private PerspectiveCamera cam;

	@Override
	public void create() {
		final Mesh mesh = new TerrainMaker().generate();
		renderable = new Renderable();
		renderable.mesh = mesh;
		renderable.meshPartOffset = 0;
		renderable.meshPartSize = mesh.getNumIndices();
		renderable.primitiveType = GL20.GL_TRIANGLES;
		renderable.material = new Material(ColorAttribute.createDiffuse(.4f, 1, .2f, 1));
		final DirectionalLight light = new DirectionalLight().set(1, 1, 1, .2f, -.8f, .2f);
		renderable.environment = new Environment().add(light);
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, 2, -3);
		cam.lookAt(0, 0, 0);
		cam.update();
	}

	@Override
	public void render() {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

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
