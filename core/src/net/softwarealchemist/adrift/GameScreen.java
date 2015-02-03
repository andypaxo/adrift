package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.PlayerCharacter;
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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
	private List<Mesh> waterMeshes;
	private ShaderProgram waterShader;
	private float[] fogColor;

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
		
		waterMeshes = terrainGenerator.generateMeshesForWater();
		waterShader = new ShaderProgram(Gdx.files.internal("shaders/waterVertex.glsl"), Gdx.files.internal("shaders/waterFragment.glsl"));
		if (!waterShader.isCompiled()) {
			System.out.println(waterShader.getLog());
			Gdx.app.exit();
		}
	}
	
	private void createEnvironment() {
		fogColor = new float[] {.3f, .6f, 1, 1f};
		environment = new Environment()
			.add(new DirectionalLight().set(1f, 1f, 1f, .7f, -1f, .4f))
			.add(new DirectionalLight().set(.3f, .3f, .3f, -.4f, -1f, -.7f));
		environment.set(new ColorAttribute(ColorAttribute.Fog, fogColor[0], fogColor[1], fogColor[2], fogColor[3]));
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1.f));
	}

	private void createPlayer() {
		player = new PlayerCharacter();
		player.position.set(120, 0, 0);
		player.rotation.set(0, 45, 0);
		player.size.set(.8f, .99f, .8f);
		player.name = System.getProperty("user.name");
		player.id = stage.getNextId();
		
		final ModelBuilder modelBuilder = new ModelBuilder();
		playerIndicatorModel = modelBuilder.createBox(player.size.x, player.size.y, player.size.z, new Material(ColorAttribute.createDiffuse(1, .2f, .2f, 1)), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		playerIndicatorModelInstance = new ModelInstance(playerIndicatorModel);
	}

	private long lastFpsCountTime;
	private int fps;
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
		cam.far = Math.min(terrain.configuration.depth * .75f, time * terrain.configuration.depth * .1f);
		cam.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(.3f, .6f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		final ArrayList<Label2d> labels = new ArrayList<Label2d>();
		
		if (terrainGenerationComplete) {
			modelBatch.begin(cam);
			modelBatch.render(terrainModelInstance, environment);
			// TODO Might be able to do this in a single call
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
			
			waterShader.begin();
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			waterShader.setUniformMatrix("u_projectionViewMatrix", cam.combined);
			waterShader.setUniform4fv("u_fogColor", fogColor, 0, 4);
			waterShader.setUniform4fv("u_cameraPosition", new float [] { cam.position.x, cam.position.y, cam.position.z, 1.1881f / (cam.far * cam.far) } , 0, 4);
			waterShader.setUniformf("u_time", time);
			for (Mesh waterMesh : waterMeshes)
				waterMesh.render(waterShader, GL20.GL_TRIANGLES);
			waterShader.end();
		}
		fps++;
		final long time = System.nanoTime();
		if (time - lastFpsCountTime > 1000000000){
			Hud.setInfo("FPS", "" + fps);
			lastFpsCountTime = time;
			fps = 0;
		}

		hud.render(labels);
	}

	private void rotateCameraAroundOrigin() {
		final float rotation = time * .2f;
		cam.position.set(
				(float) (terrain.configuration.depth * .5f + (Math.sin(rotation) * terrain.configuration.width) * .6f),
				terrain.configuration.height * 1.5f,
				(float) (terrain.configuration.depth * .5f + (Math.cos(rotation) * terrain.configuration.depth) * .6f));
		cam.lookAt(terrain.configuration.width * .5f, 0, terrain.configuration.depth * .5f);
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
		for (Mesh waterMesh : waterMeshes)
			waterMesh.dispose();
		waterShader.dispose();
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
