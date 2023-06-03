package knight.arkham.objects;

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


    public Koala(float width, float height) {
        this.width = width;
        this.height = height;

        state = PlayerState.Walking;
        stateTime = 0;

        maxVelocity = 10f;
        jumpVelocity = 40f;
        damping = 0.87f;

        position = new Vector2();
        velocity = new Vector2();

        facesRight = true;
        grounded = false;
    }
}
