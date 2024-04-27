package knight.arkham;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Koala {

    private enum PlayerState {STANDING, WALKING, JUMPING}
    private PlayerState currentState = PlayerState.WALKING;
    public float width;
    public float height;
    public final Vector2 position = new Vector2(20, 20);
    public final Vector2 velocity = new Vector2();
    private float stateTimer;
    private boolean isMovingRight = true;
    public boolean isGrounded;
    private final TextureRegion standingRegion;
    private final TextureRegion jumpingRegion;
    private final Animation<TextureRegion> walkingAnimation;
    private TextureRegion actualRegion;
    private final Texture sprite = new Texture("koalio.png");

    public Koala() {

        // load the koala frames, split them, and assign them to Animations
        TextureRegion[] regions = TextureRegion.split(sprite, 18, 26)[0];

        actualRegion = regions[0];

        standingRegion = regions[0];
        jumpingRegion = regions[1];

        walkingAnimation = new Animation<>(0.15f, regions[2], regions[3], regions[4]);

        // figure out the width and height of the koala for collision
        // detection and rendering by converting a koala frames pixel
        // size into world units (1 unit == 16 pixels)
        width = 1 / 16f * regions[0].getRegionWidth();
        height = 1 / 16f * regions[0].getRegionHeight();
    }

    public void update(float deltaTime) {

        stateTimer += deltaTime;

        // check input and apply to velocity & state
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && isGrounded) {

            float jumpVelocity = 40f;
            velocity.y += jumpVelocity;
            currentState = PlayerState.JUMPING;
            isGrounded = false;
        }

        float maxVelocity = 10f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x = -maxVelocity;

            if (isGrounded)
                currentState = PlayerState.WALKING;

            isMovingRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x = maxVelocity;

            if (isGrounded)
                currentState = PlayerState.WALKING;

            isMovingRight = true;
        }

        // clamp the velocity to the maximum, x-axis only
        velocity.x = MathUtils.clamp(velocity.x, -maxVelocity, maxVelocity);

        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(velocity.x) < 1) {
            velocity.x = 0;

            if (isGrounded)
                currentState = PlayerState.STANDING;
        }
    }

    private TextureRegion getActualRegion() {

        switch (currentState) {

            case WALKING:
                return walkingAnimation.getKeyFrame(stateTimer, true);

            case JUMPING:
                return jumpingRegion;

            default:
                return standingRegion;
        }
    }

    public void draw(Batch batch) {

        actualRegion = getActualRegion();
        // draw the koala, depending on the current velocity
        // on the x-axis, draw the koala facing either right or left
        batch.begin();

        if (isMovingRight)
            batch.draw(actualRegion, position.x, position.y, width, height);
        else
            batch.draw(actualRegion, position.x + width, position.y, -width, height);

        batch.end();
    }

    public void dispose() { sprite.dispose();}
}
