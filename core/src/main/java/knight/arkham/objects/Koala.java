package knight.arkham.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Koala {
    public float width;
    public float height;
    public float maxVelocity;
    public float jumpVelocity;
    public float damping;
    public final Vector2 position;
    public final Vector2 velocity;
    public PlayerState state;
    public float stateTime;
    public boolean facesRight;
    public boolean grounded;
    private final Animation<TextureRegion> stand;
    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> jump;
    private final Texture sprite;

    public Koala() {

        sprite = new Texture("koalio.png");

        // load the koala frames, split them, and assign them to Animations
        TextureRegion[] regions = TextureRegion.split(sprite, 18, 26)[0];

        stand = new Animation<>(0, regions[0]);
        jump = new Animation<>(0, regions[1]);
        walk = new Animation<>(0.15f, regions[2], regions[3], regions[4]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // figure out the width and height of the koala for collision
        // detection and rendering by converting a koala frames pixel
        // size into world units (1 unit == 16 pixels)
        width = 1 / 16f * regions[0].getRegionWidth();
        height = 1 / 16f * regions[0].getRegionHeight();

        state = PlayerState.Walking;
        stateTime = 0;

        maxVelocity = 10f;
        jumpVelocity = 40f;
        damping = 0.87f;

        position = new Vector2(20, 20);
        velocity = new Vector2();

        facesRight = true;
        grounded = false;
    }

    public void update(float deltaTime) {

        stateTime += deltaTime;

        // check input and apply to velocity & state
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && grounded) {
            velocity.y += jumpVelocity;
            state = PlayerState.Jumping;
            grounded = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x = -maxVelocity;

            if (grounded)
                state = PlayerState.Walking;

            facesRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x = maxVelocity;

            if (grounded)
                state = PlayerState.Walking;

            facesRight = true;
        }

        // clamp the velocity to the maximum, x-axis only
        velocity.x = MathUtils.clamp(velocity.x, -maxVelocity, maxVelocity);

        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(velocity.x) < 1) {
            velocity.x = 0;

            if (grounded)
                state = PlayerState.Standing;
        }
    }

    public void draw(Batch batch) {
        // based on the koala state, get the animation frame
        TextureRegion frame = null;

        switch (state) {
            case Standing:
                frame = stand.getKeyFrame(stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(stateTime);
                break;
        }

        // draw the koala, depending on the current velocity
        // on the x-axis, draw the koala facing either right
        // or left
        batch.begin();

        if (facesRight)
            batch.draw(frame, position.x, position.y, width, height);

        else
            batch.draw(frame, position.x + width, position.y, -width, height);

        batch.end();
    }

    public Texture getSprite() {return sprite;}
}
