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
	private Entity player;
	private InputHandler inputHandler;

	@Override
	public void create() {
		createTerrain();
		createEnvironment();
		
		player = new Entity();
		player.position.set(terrain.width / 2, 10, 0);
		inputHandler = new InputHandler(player);
		
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.far = terrain.depth * 2;
		cam.update();
	}

	private void createEnvironment() {
		environment = new Environment()
			.add(new DirectionalLight().set(1f, 1f, 1f, .7f, -1f, .4f))
			.add(new DirectionalLight().set(.3f, .3f, .3f, -.4f, -1f, -.7f));
		environment.set(new ColorAttribute(ColorAttribute.Fog, 0.1f, 0.1f, 9f, 2f));
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));
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
		time += Gdx.graphics.getDeltaTime();

		inputHandler.handleInput();
		//rotateCameraAroundOrigin();
		moveCameraToMatchPlayer();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(modelInstance, environment);
		modelBatch.end();
	}

	private void rotateCameraAroundOrigin() {
		final float rotation = time * .2f;
		cam.position.set(
				(float) (terrain.depth * .5f + (Math.sin(rotation) * terrain.width) * .6f),
				terrain.height * 1.5f,
				(float) (terrain.depth * .5f + (Math.cos(rotation) * terrain.depth) * .6f));
		cam.lookAt(terrain.width * .5f, 0, terrain.depth * .5f);
		cam.up.set(0, 1, 0);
		cam.update();
	}
	
	private void moveCameraToMatchPlayer() {
		System.out.println(player.position);
		cam.position.set(player.position);
		cam.direction.set(0, 0, 1);
		cam.direction.rotate(player.rotation.x, 1, 0, 0);
		cam.direction.rotate(player.rotation.y, 0, 1, 0);
		cam.update();
	}
	
	@Override
	public void resize(int width, int height) {
		cam.viewportHeight = height;
		cam.viewportWidth = width;
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		model.dispose();
	}

}
