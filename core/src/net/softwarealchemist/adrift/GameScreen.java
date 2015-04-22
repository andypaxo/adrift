package net.softwarealchemist.adrift;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.adrift.entities.Entity;
import net.softwarealchemist.adrift.entities.Monster;
import net.softwarealchemist.adrift.entities.Particle;
import net.softwarealchemist.adrift.entities.PlayerCharacter;
import net.softwarealchemist.adrift.entities.Relic;
import net.softwarealchemist.adrift.entities.RelicSlot;
import net.softwarealchemist.adrift.model.Terrain;
import net.softwarealchemist.adrift.model.Zone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
	
	private Model relicModel;
	private ModelInstance relicModelInstance;
	
	private Model particleModel;
	private ModelInstance particleModelInstance;
	
	private Model emptyRelicSlotModel;
	private ModelInstance emptyRelicSlotInstance;
	
	private Model activeRelicSlotModel;
	private ModelInstance activeRelicSlotInstance;
	
	private Model monsterModel;
	private ModelInstance monsterModelInstance;
	
	private ModelBatch modelBatch;
	private PerspectiveCamera cam;
	private float time = 0;
	private Terrain terrain;
	private Environment environment;
	private PlayerCharacter player;
	private InputHandler inputHandler;
	private Hud hud;
	private boolean terrainGenerationComplete;
	private List<Mesh> waterMeshes;
	private ShaderProgram waterShader;
	private float[] fogColor;

	private ClientWorld stage;
	private Zone zone;

	public GameScreen() {
		createEnvironment();
		terrain = new Terrain();
		stage = new ClientWorld(terrain, this);
		zone = stage.zone;
		
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
		
		stage.startAndConnectToServer();
		
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
		player = new PlayerCharacter(System.getProperty("user.name"));
		player.position.set(120, 0, 0);
		player.rotation.set(0, 45, 0);
		player.size.set(.8f, .99f, .8f);
		player.id = zone.getNextId();
		
		final ModelBuilder modelBuilder = new ModelBuilder();
		
		playerIndicatorModel = modelBuilder.createBox(player.size.x, player.size.y, player.size.z, new Material(ColorAttribute.createDiffuse(1, .2f, .2f, 1)), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		playerIndicatorModelInstance = new ModelInstance(playerIndicatorModel);
		
//		relicModel = modelBuilder.createBox(.75f, 1.25f, .75f, new Material(ColorAttribute.createDiffuse(1, 1, .2f, 1)), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		relicModel = modelBuilder.createSphere(
				.75f, .75f, .75f, 10, 10, GL20.GL_TRIANGLES,
				new Material(
						ColorAttribute.createSpecular(.2f, .8f, .4f, 1),
						ColorAttribute.createDiffuse(1, 1, .2f, 1)),
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		relicModelInstance = new ModelInstance(relicModel);
		
		particleModel = modelBuilder.createSphere(
				.1f, .1f, .1f, 5, 5, GL20.GL_TRIANGLES,
				new Material(
						ColorAttribute.createSpecular(.2f, .8f, .4f, 1),
						ColorAttribute.createDiffuse(1, 1, .2f, 1)),
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		particleModelInstance = new ModelInstance(particleModel);
		
		emptyRelicSlotModel = modelBuilder.createCone(
				.9f, .1f, .9f, 4,
				new Material(ColorAttribute.createDiffuse(Color.WHITE)),
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		emptyRelicSlotInstance = new ModelInstance(emptyRelicSlotModel);
		
		activeRelicSlotModel = modelBuilder.createCone(
				.9f, .1f, .9f, 4,
				new Material(ColorAttribute.createDiffuse(.2f, 1f, .2f, 1)),
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		activeRelicSlotInstance = new ModelInstance(activeRelicSlotModel);
		
		monsterModel = modelBuilder.createBox(.75f, .75f, .75f,
				new Material(ColorAttribute.createDiffuse(Color.RED)),
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		monsterModelInstance = new ModelInstance(monsterModel);
	}

	private long lastFpsCountTime;
	private int fps;
	@Override
	public void render(float delta) {
		try {
			step(delta);
		} catch (Exception e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
	}
	
	private void step(float delta) {
		if (!terrainGenerationComplete)
			return;
		
		if (terrainModel == null)
			createMeshes();
		
		time += delta;

		inputHandler.handleInput();
		if (terrainGenerationComplete) {
			zone.step(delta);
			stage.doEvents(delta);
		}

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
			// TODO Would be more elegant with multi-iterator
			for (Entity entity : zone.entities.values())
				drawEntity(labels, entity);
			for (Entity entity : zone.localEntities)
				drawEntity(labels, entity);
			
			// TEMP: Draw what player is looking at
			drawCursor();
			
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

	private void drawCursor() {
//		Vector3 lookingAtVoxel = RayCaster
//				.cast(player.position, player.getFacing(), 512, terrain)
//				.add(.5f);
		Entity lookingAtEntity = zone.findEntityInFrontOf(player);
		if (lookingAtEntity != null) {
			Entity entity = new Relic(null);
			entity.position.set(lookingAtEntity.position);
			entity.position.add(0, .5f, 0);
			drawEntity(new ArrayList<Label2d>(), entity);
			Hud.setInfo("Looking at", String.format("%s %d", lookingAtEntity.getClass().getSimpleName(), lookingAtEntity.id));
		} else {
			Hud.setInfo("Looking at", "");
		}
	}

	private void drawEntity(final ArrayList<Label2d> labels, Entity entity) {
		if (entity.isInactive())
			return;
		
		synchronized (stage) {
			//only draw local player if (GameState.InteractionMode == GameState.MODE_SPECTATE) { }
			ModelInstance modelToRender = playerIndicatorModelInstance;
			if (entity instanceof Relic)
				modelToRender = relicModelInstance;
			else if (entity instanceof Particle)
				modelToRender = particleModelInstance;
			else if (entity instanceof RelicSlot)
				modelToRender = ((RelicSlot)entity).isActivated ? activeRelicSlotInstance : emptyRelicSlotInstance;
			else if (entity instanceof Monster)
				modelToRender = monsterModelInstance;
			
			modelToRender.transform.setToTranslation(entity.position);
			modelToRender.transform.rotate(Vector3.Y, entity.rotation.y);
			
			modelBatch.render(modelToRender, environment);
			if (entity instanceof PlayerCharacter && (entity != player || GameState.InteractionMode == GameState.MODE_SPECTATE) && cam.frustum.pointInFrustum(entity.position)) {
				final Vector3 labelPos = cam.project(new Vector3(entity.position));
				labels.add(new Label2d(labelPos, entity.getName()));
			}
		}
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
		cam.direction.set(Vector3.Z);
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
		relicModel.dispose();
		particleModel.dispose();
		emptyRelicSlotModel.dispose();
		activeRelicSlotModel.dispose();
		monsterModel.dispose();
		for (Mesh waterMesh : waterMeshes)
			waterMesh.dispose();
		waterShader.dispose();
		stage.dispose();
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
