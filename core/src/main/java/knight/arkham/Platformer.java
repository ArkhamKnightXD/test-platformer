package knight.arkham;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;

/** Super Mario Brothers-like very basic platformer, using a tile map built using <a href="http://www.mapeditor.org/">Tiled</a> and a
 * tileset and sprites by <a href="http://www.vickiwenderlich.com/">Vicky Wenderlich</a></p>
 *
 * Shows simple platformer collision detection as well as on-the-fly map modifications through destructible blocks!
 * @author mzechner */
public class Platformer extends InputAdapter implements ApplicationListener {

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private Koala koala;
    private final Array<Rectangle> tiles = new Array<>();
    private static final float GRAVITY = -2.5f;
    private boolean isDebugMode;
    private ShapeRenderer debugRenderer;
    private Pool<Rectangle> rectPool;

    @Override
    public void create() {

        // create the Koala we want to move around the world
        koala = new Koala();

        // load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
        map = new TmxMapLoader().load("level1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        rectPool = new Pool<>() {
            @Override
            protected Rectangle newObject() {
                return new Rectangle();
            }
        };

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();

        debugRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        // clear the screen
        ScreenUtils.clear(0.7f, 0.7f, 1.0f, 1);

        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        // update the koala (process input, collision detection, position update)
        updateKoala(deltaTime);

        // let the camera follow the koala, x-axis only
        camera.position.x = koala.position.x;
        camera.update();

        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        mapRenderer.setView(camera);
        mapRenderer.render();

        // render the koala
        koala.draw(mapRenderer.getBatch());

        // render debug rectangles
        if (isDebugMode)
            renderDebug();
    }

    private void updateKoala(float deltaTime) {
        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        koala.update(deltaTime);

        if (Gdx.input.isKeyJustPressed(Keys.B))
            isDebugMode = !isDebugMode;

        // apply gravity if we are falling
        koala.velocity.add(0, GRAVITY);

        // multiply by delta time, so we know how far we go
        // in this frame
        koala.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the koala is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle koalaRect = rectPool.obtain();
        koalaRect.set(koala.position.x, koala.position.y, koala.width, koala.height);
        int startX, startY, endX, endY;

        if (koala.velocity.x > 0)
            startX = endX = (int)(koala.position.x + koala.width + koala.velocity.x);

        else
            startX = endX = (int)(koala.position.x + koala.velocity.x);

        startY = (int)(koala.position.y);
        endY = (int)(koala.position.y + koala.height);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.x += koala.velocity.x;

        for (Rectangle tile : tiles) {

            if (koalaRect.overlaps(tile)) {
                koala.velocity.x = 0;
                break;
            }
        }
        koalaRect.x = koala.position.x;

        // if the koala is moving upwards, check the tiles to the top of its
        // top bounding box edge, otherwise check the ones to the bottom
        if (koala.velocity.y > 0)
            startY = endY = (int)(koala.position.y + koala.height + koala.velocity.y);
        else
            startY = endY = (int)(koala.position.y + koala.velocity.y);

        startX = (int)(koala.position.x);
        endX = (int)(koala.position.x + koala.width);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.y += koala.velocity.y;

        for (Rectangle tile : tiles) {

            if (koalaRect.overlaps(tile)) {
                // we actually reset the koala y-position here,
                // so it is just below/above the tile we collided with
                // this removes bouncing :)
                if (koala.velocity.y > 0) {
                    koala.position.y = tile.y - koala.height;
                    // we hit a block jumping upwards, let's destroy it!
                    TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
                    layer.setCell((int)tile.x, (int)tile.y, null);
                } else {
                    koala.position.y = tile.y + tile.height;
                    // if we hit the ground, mark us as grounded, so we can jump
                    koala.isGrounded = true;
                }
                koala.velocity.y = 0;
                break;
            }
        }
        rectPool.free(koalaRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        koala.position.add(koala.velocity);
        koala.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis, so we don't
        // walk infinitely once a key was pressed
        koala.velocity.x *= koala.damping;
    }

    private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {

        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");

        rectPool.freeAll(tiles);
        tiles.clear();

        for (int y = startY; y <= endY; y++) {

            for (int x = startX; x <= endX; x++) {

                Cell cell = layer.getCell(x, y);

                if (cell != null) {

                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }
            }
        }
    }

    private void renderDebug() {

        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeType.Line);

        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(koala.position.x, koala.position.y, koala.width, koala.height);

        debugRenderer.setColor(Color.YELLOW);
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
//   This method check all the tiles on each row for every column

        for (int y = 0; y <= layer.getHeight(); y++) {

            for (int x = 0; x <= layer.getWidth(); x++) {

                Cell cell = layer.getCell(x, y);

                if (cell != null) {
                    if (camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0))
                        debugRenderer.rect(x, y, 1, 1);
                }
            }
        }

        debugRenderer.end();
    }

    @Override
    public void dispose () {koala.dispose();}

    @Override
    public void resume () {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}
}
