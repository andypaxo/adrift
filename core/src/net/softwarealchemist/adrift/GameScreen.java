package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.network.AdriftClient;
import net.softwarealchemist.network.AdriftServer;
import net.softwarealchemist.network.Broadcaster;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.math.Vector3;

public class GameScreen implements Screen {

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
	private Hud hud;
	private Broadcaster broadcaster;
	private AdriftClient client;
	private AdriftServer server;
	private boolean terrainGenerationComplete;

	public GameScreen() {
		createEnvironment();
		terrain = new Terrain();
		stage = new Stage(terrain, this);
		createPlayer();
		stage.setPlayer(player);
		
		inputHandler = new InputHandler(player);
		
		modelBatch = new ModelBatch();
		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.near = .1f;
		cam.far = 1;
		cam.update();
		
		hud = new Hud();
		
		time = 0;

		if (GameState.server == null) {
			terrain.configureRandom();
			server = new AdriftServer();
			server.setConfiguration(terrain.getConfiguration());
			server.setStage(stage);
			server.start();
			broadcaster = new Broadcaster();
			broadcaster.start();
			startTerrainGeneration();
		}
		else
		{
			client = new AdriftClient(GameState.server, stage);
			client.start();
		}
		System.out.println("Game screen initialized");
	}
	
	public void startTerrainGeneration() {
		new Thread(() -> createTerrain()).start();
	}

	private void createTerrain() {
		terrain.generate();
		terrainGenerationComplete = true;
	}
	
	private void createMeshes() {
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
		player.position.set(120, 0, 0);
		player.size.set(.8f, .99f, .8f);
		player.name = System.getProperty("user.name");
		player.id = stage.getNextId();
		
		final ModelBuilder modelBuilder = new ModelBuilder();
		playerIndicatorModel = modelBuilder.createBox(player.size.x, player.size.y, player.size.z, new Material(ColorAttribute.createDiffuse(1, .2f, .2f, 1)), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		playerIndicatorModelInstance = new ModelInstance(playerIndicatorModel);
	}

//	private long lastFpsCountTime;
//	private int fps;
	@Override
	public void render(float delta) {
		if (!terrainGenerationComplete)
			return;
		
		if (terrainModel == null)
			createMeshes();
		
		time += delta;

		inputHandler.handleInput();
		if (terrainGenerationComplete)
			stage.step(delta);

		if (GameState.InteractionMode == GameState.MODE_FLY || GameState.InteractionMode == GameState.MODE_WALK)
			moveCameraToMatchPlayer();
		else if (GameState.InteractionMode == GameState.MODE_SPECTATE)
			rotateCameraAroundOrigin();
		cam.far = Math.min(terrain.depth * .75f, time * terrain.depth * .1f);
		cam.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		final ArrayList<Label2d> labels = new ArrayList<Label2d>();
		
		if (terrainGenerationComplete) {
			modelBatch.begin(cam);
			modelBatch.render(terrainModelInstance, environment);
			for (Entity entity : stage.entities.values()) {
				//only draw local player if (GameState.InteractionMode == GameState.MODE_SPECTATE) { }
				synchronized (stage) {
//					playerIndicatorModelInstance.transform.setToWorld(entity.position, entity.rotation, Vector3.Y);
					playerIndicatorModelInstance.transform.setToTranslation(entity.position);
					playerIndicatorModelInstance.transform.rotate(Vector3.Y, entity.rotation.y);
					modelBatch.render(playerIndicatorModelInstance, environment);
					if ((entity != player || GameState.InteractionMode == GameState.MODE_SPECTATE) && cam.frustum.pointInFrustum(entity.position)) {
						final Vector3 labelPos = cam.project(new Vector3(entity.position));
						labels.add(new Label2d(labelPos, entity.name));
					}
				}
			}
			modelBatch.end();
		}
//		fps++;
//		final long time = System.nanoTime();
//		if (time - lastFpsCountTime > 1000000000){
//			hud.log("FPS : " + fps);
//			lastFpsCountTime = time;
//			fps = 0;
//		}

		hud.render(labels);
	}

	private void rotateCameraAroundOrigin() {
		final float rotation = time * .2f;
		cam.position.set(
				(float) (terrain.depth * .5f + (Math.sin(rotation) * terrain.width) * .6f),
				terrain.configuration.height * 1.5f,
				(float) (terrain.depth * .5f + (Math.cos(rotation) * terrain.depth) * .6f));
		cam.lookAt(terrain.width * .5f, 0, terrain.depth * .5f);
		cam.up.set(0, 1, 0);
	}
	
	private void moveCameraToMatchPlayer() {
		cam.position.set(player.position);
		cam.position.y += player.size.y * .25f;
		cam.direction.set(0, 0, 1);
		cam.direction.rotate(player.rotation.x, 1, 0, 0);
		cam.direction.rotate(player.rotation.y, 0, 1, 0);
	}
	
	@Override
	public void resize(int width, int height) {
		cam.viewportHeight = height;
		cam.viewportWidth = width;
		hud.resize(width, height);
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		terrainModel.dispose();
		playerIndicatorModel.dispose();
		broadcaster.dispose();
		if (server != null)
			server.dispose();
		if (client != null)
			client.dispose();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}
}
