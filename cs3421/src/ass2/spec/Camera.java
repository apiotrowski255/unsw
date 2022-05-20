package ass2.spec;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;

public class Camera implements KeyListener, MouseMotionListener {
	private float[] position = new float[3];
	//Important to note, these angles are in Radians
	private float pitch = 0;
	public float yaw = 0;
	private float roll = 0;
	//
	private Point myMousePoint = null;
	private static final float ROTATION_SCALE = 0.04f;
	private static final float GRAVITY = -1f;
	private static final float JUMP_POWER = 5f;
	private static final float DIS_FROM_PLAYER = 5f;
	private Avatar avatar = null;

	public Camera() {
		position[0] = 0; // x coordinate
		position[1] = 0; // y coordinate
		position[2] = 0; // z coordinate
	}

	public float[] getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}

	public void setCameraView(GL2 gl) {
		GLU glu = new GLU();

		if (avatar != null) {
			glu.gluLookAt(avatar.getX() - DIS_FROM_PLAYER * Math.cos(pitch) * Math.cos(yaw),
					avatar.getY() - DIS_FROM_PLAYER * Math.sin(pitch),
					avatar.getZ() - DIS_FROM_PLAYER * Math.cos(pitch) * Math.sin(yaw), avatar.getX(), avatar.getY(),
					avatar.getZ(), 0, 1, 0);
			avatar.draw(gl);

		} else {
			glu.gluLookAt(position[0], position[1], position[2], Math.cos(pitch) * Math.cos(yaw) + position[0],
					Math.sin(pitch) + position[1], Math.cos(pitch) * Math.sin(yaw) + position[2], 0, 1, 0);
		}
	}

	public void setPosition(float x, float y, float z) {
		position[0] = x;
		position[1] = y;
		position[2] = z;
	}

	@Override
	public void keyPressed(KeyEvent e) {
//		int key = e.getKeyCode();
		int key = e.getKeyCode();
		float speed = 0.1f;
		if (avatar == null) {
			if (key == KeyEvent.VK_W) {
				position[2] += speed;
				System.out.println(position[2]);
			}
			if (key == KeyEvent.VK_S) {
				position[2] -= speed;
			}
			if (key == KeyEvent.VK_A) {
				position[0] += speed;
			}
			if (key == KeyEvent.VK_D) {
				position[0] -= speed;
			}
			if (key == KeyEvent.VK_SHIFT) {
				position[1] -= speed;
			}
			if (key == KeyEvent.VK_SPACE) {
				position[1] += speed;
			}
			if (key == KeyEvent.VK_C) {
				if (avatar == null) {
					System.out.println("3rd person cam enabled");
					avatar = new Avatar(position[0], position[1], position[2]);
				} else {
					System.out.println("3rd person cam disabled");
					avatar = null;
				}
			}
		} else {
			if (key == KeyEvent.VK_W) {
				avatar.setZ(avatar.getZ() + speed);
				System.out.println(avatar.getZ());
			}
			if (key == KeyEvent.VK_S) {
				avatar.setZ(avatar.getZ() - speed);
			}
			if (key == KeyEvent.VK_A) {
				avatar.setX(avatar.getX() + speed);
			}
			if (key == KeyEvent.VK_D) {
				avatar.setX(avatar.getX() - speed);
			}
			if (key == KeyEvent.VK_SHIFT) {
				avatar.setY(avatar.getY() - speed);
			}
			if (key == KeyEvent.VK_SPACE) {
				avatar.setY(avatar.getY() + speed);
			}
			if (key == KeyEvent.VK_C) {
				if (avatar == null) {
					System.out.println("3rd person cam enabled");
					avatar = new Avatar(position[0], position[1], position[2]);
				} else {
					System.out.println("3rd person cam disabled");
					setPosition(avatar.getX(), avatar.getY(), avatar.getZ());
					avatar = null;
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {

		Point p = e.getPoint();

		if (myMousePoint != null) {
			float dx = p.x - myMousePoint.x;
			float dy = p.y - myMousePoint.y;

			// This part makes the camera smoother when clicking and dragging
			// multiple times//
			if (dx > 2) {
				dx = 1f;
			} else if (dx < -2) {
				dx = -1f;
			}
			if (dy > 2) {
				dy = 1f;
			} else if (dy < -2) {
				dy = -1f;
			}
			/////////////////////////////////////////////

			// Note: dragging in the x dir rotates about y
			// dragging in the y dir rotates about x
			yaw += dx * ROTATION_SCALE;
			pitch += dy * ROTATION_SCALE;

		}

		myMousePoint = p;

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	public Avatar getAvatar() {
		return avatar;
	}
	
	

}
