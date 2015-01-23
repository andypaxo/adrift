package net.softwarealchemist.adrift;

import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class AdriftMain extends ApplicationAdapter {

	private Model terrainModel;
	private ModelInstance terrainModelInstance;
	private Model playerIndicatorModel;
	private ModelInstance playerIndicatorModelInstance;
	private ModelBatch modelBatch;
	private PerspectiveCamera cam;
	private float time = 0;
	private Terrain terrain;
	private Environment environment;
	private Entity player;
	private InputHandler inputHandler;
	private Stage stage;

	@Override
	public void create() {
		createTerrain();
		createEnvironment();
		createPlayer();
		stage = new Stage(terrain);
		stage.addEntity(player);
		
		inputHandler = new InputHandler(player);
		
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = .1f;
		cam.far = terrain.depth * .75f;
		cam.update();
	}

	private void createTerrain() {
		terrain = new Terrain();
		terrain.generate();
		
		final MeshGenerator terrainGenerator = new MeshGenerator(terrain);
		final Material groundMaterial = new Material(
				ColorAttribute.createDiffuse(1, 1, 1, 1),
				ColorAttribute.createAmbient(11f, 1.1f, 1.1f, 1));
		final List<Mesh> meshes = terrainGenerator.generateMeshes();

		final ModelBuilder modelBuilder = new ModelBuilder();
		int i = 0;
		modelBuilder.begin();
		for (Mesh mesh : meshes) {
			modelBuilder.part(Integer.toString(i++), mesh, GL20.GL_TRIANGLES, groundMaterial);
		}
		terrainModel = modelBuilder.end();
		terrainModelInstance = new ModelInstance(terrainModel);
	}
	private void createEnvironment() {
		environment = new Environment()
			.add(new DirectionalLight().set(1f, 1f, 1f, .7f, -1f, .4f))
			.add(new DirectionalLight().set(.3f, .3f, .3f, -.4f, -1f, -.7f));
		environment.set(new ColorAttribute(ColorAttribute.Fog, .3f, .6f, 1, 2f));
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));
	}

	private void createPlayer() {
		player = new Entity();
		player.position.set(terrain.width / 2, 10, 0);
		player.size.set(.8f, .99f, .8f);
		
		final ModelBuilder modelBuilder = new ModelBuilder();
		playerIndicatorModel = modelBuilder.createArrow(0, 6, 0, 0, 0, 0, .4f, .4f, 12, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(1, .2f, .2f, 1)), Usage.Position | Usage.Normal);
		playerIndicatorModelInstance = new ModelInstance(playerIndicatorModel);
	}


	@Override
	public void render() {
		time += Gdx.graphics.getDeltaTime();

		inputHandler.handleInput();
		stage.step(Gdx.graphics.getDeltaTime());
		
		if (GameState.InteractionMode == GameState.MODE_FLY || GameState.InteractionMode == GameState.MODE_WALK)
			moveCameraToMatchPlayer();
		else if (GameState.InteractionMode == GameState.MODE_SPECTATE)
			rotateCameraAroundOrigin();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(terrainModelInstance, environment);
		if (GameState.InteractionMode == GameState.MODE_SPECTATE) {
			playerIndicatorModelInstance.transform.setToTranslation(player.position);
			modelBatch.render(playerIndicatorModelInstance, environment);
		}
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
		cam.position.set(player.position);
		cam.position.y += player.size.y * .25f;
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
		terrainModel.dispose();
		playerIndicatorModel.dispose();
	}

}
