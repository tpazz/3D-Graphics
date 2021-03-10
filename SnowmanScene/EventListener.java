/* I declare that this code is my own work */
/* Author Theo Koorehpaz */
/* tkoorehpaz1@sheffield.ac.uk */
/* 04/12/2019 */
/* This class represents interactions with the scene and objects not in the scene graph */

import gmaths.*;

import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

public class EventListener implements GLEventListener {

  private static final boolean DISPLAY_SHADERS = false;

  public EventListener(Camera camera) {
    this.camera = camera;
    this.camera.setPosition(new Vec3(0f,8.0f,19f));
    this.camera.setTarget(new Vec3(0f,4.0f,0f));
  }

  // METHODS DEFINED BY GLEventListener ****************************************

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW);    // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
    gl.glCullFace(GL.GL_BACK);   // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
    startSlide = getSeconds();
    startRoll = getSeconds();
    startRock = getSeconds();
  }

  /* Called to indicate the drawing surface has been moved and/or resized  */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    float aspect = (float)width/(float)height;
    camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory, if necessary */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    light.dispose(gl);
    floor.dispose(gl);
    wall.dispose(gl);
    spotBox.dispose(gl);
    spotPole.dispose(gl);
    spotBase.dispose(gl);
    present.dispose(gl);
  }

  // INTERATION ****************************************************************

  // each animation has a save state ***
  public void rock() {
   if (rock) {
     rock = false;
     elapsedRock = getSeconds()-startRock;
     savedRock = elapsedRock;
   } else {
     rock = true;
     startRock = getSeconds()-savedRock;
   }
  }

  public void roll() {
   if (roll) {
     roll = false;
     elapsedRoll = getSeconds()-startRoll;
     savedRoll = elapsedRoll;
   } else {
     roll = true;
     startRoll = getSeconds()-savedRoll;
   }
  }

  public void slide() {
   if (slide) {
     slide = false;
     elapsedSlide = getSeconds()-startSlide;
     savedSlide = elapsedSlide;
   } else {
     slide = true;
     startSlide = getSeconds()-savedSlide;
   }
  }

  public void combo() {
   if (srr) {
     srr = false;
     elapsedCombo = getSeconds()-startCombo;
     savedCombo = elapsedCombo;
   } else {
     srr = true;
     rock = false;
     slide = false;
     roll = false;
     startCombo = getSeconds()-savedCombo;
   }
  }

  public void reset() {
   sgModel.reset();
   srr = false;
   rock = false;
   slide = false;
   roll = false;
   xPosition = 0;
   zPosition = 0;
   savedCombo = 0;
   savedSlide = 0;
   savedRoll = 0;
   savedRock = 30;
  }

  public void spotlight() {
    spotlight = !spotlight;
    if (spotlight) {
      light.setDirection(0,0,0);
   }
    else {
      light.setDirection(-5.5f,7,3);
   }
  }

  public void sun() {
    sun = !sun;
    if (sun) {
      light.setSunlight(0,0,0);
   }
    else {
      light.setSunlight(-0.2f, -1.0f, -0.3f);
   }
  }

  // THE SCENE *****************************************************************

  private Camera camera;
  private Mat4 perspective;
  private Model floor, wall, spotPole, spotBase, present, spotBox;
  private Light light;
  private SGNode snowman;
  private boolean sun = false, spotlight = false;
  private boolean rock, roll, slide, srr = false;
  private float xPosition, zPosition, offsetY, offsetX, translation1, translation2;
  private Mesh mesh;
  private Shader shader;
  private Material material;
  private Mat4 modelMatrix;
  private SceneGraph sgModel;
  private double startTime, startRoll, startRock, startSlide, startCombo, t, elapsedTime;
  private double savedSlide, savedRoll, savedCombo;
  private double savedRock = 30; // start rocking from upright position
  private double elapsedSlide, elapsedRock, elapsedRoll, elapsedCombo;

  private void initialise(GL3 gl) {
    int[] textureId0 = TextureLibrary.loadTexture(gl, "textures/snowGroundNice.jpg");
    int[] textureId1 = TextureLibrary.loadTexture(gl, "textures/background.jpg");
    int[] textureId2 = TextureLibrary.loadTexture(gl, "textures/snow.jpg");
    int[] textureId3 = TextureLibrary.loadTexture(gl, "textures/present.jpg");
    int[] textureId4 = TextureLibrary.loadTexture(gl, "textures/metal.jpg");

    // NON-SCENE GRAPH OBJECTS *************************************************

    light = new Light(gl);
    light.setCamera(camera);

    // PLANE MODELS
    mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    shader = new Shader(gl, "vs_plane.txt", "fs_floor.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0,0,0), 256.0f);
    modelMatrix = Mat4Transform.scale(16,1f,16);
    floor = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId0);
    shader = new Shader(gl, "vs_plane.txt", "fs_wall.txt");
    wall = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId1, textureId2);

    // SPOTLIGHT POLE and BASE
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_obj.txt", "fs_1tex_obj.txt");
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-6f,(0.5f*7),6),Mat4Transform.scale(0.5f,7,0.5f));
    material = new Material(new Vec3(0.5f, 0.5f, 0.31f), new Vec3(1,1,1), new Vec3(1f,1f,1f), 32.0f);
    spotPole = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId4);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-6f,0,6),Mat4Transform.scale(2,1,2f));
    spotBase = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId4);

    // SHINY OBJECT
    shader = new Shader(gl, "vs_obj.txt", "fs_2tex_obj.txt");
    material = new Material(new Vec3(1.0f,0.5f,0.31f), new Vec3(1.0f,0.5f,0.31f), new Vec3(1,1,1), 256.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(3.5f,1.5f,5.5f),
      Mat4.multiply(Mat4Transform.scale(2.5f,2.5f,2.5f), Mat4Transform.rotateAroundX(180)));
    present = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId3, textureId4);

    // SPOTLIGHT BOX
    mesh = new Mesh(gl, Cube.vertices.clone(), Cube.indices.clone());
    shader = new Shader(gl, "vs_obj.txt", "fs_1tex_obj.txt");
    material = new Material(new Vec3(0.5f, 0.5f, 0.31f), new Vec3(1,1,1), new Vec3(1f,1f,1f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-6f,7,6), Mat4Transform.scale(2,0.5f,0.5f));
    spotBox = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId4);

    // SCENE GRAPH
    sgModel = new SceneGraph(gl, light, camera, shader);
    snowman = sgModel.buildSceneGraph();
  }

  // NON-SCENE GRAPH METHODS ***************************************************

  // returns rotating spotlight model matrix
  private Mat4 getModelMatrix() {
    double elapsedTime = getSeconds()-startTime;
    translation1 = 50*((float)Math.sin(elapsedTime))+ 45.0f;
    modelMatrix = Mat4Transform.translate(-6,7,6);
    modelMatrix = Mat4Transform.rotateAroundZM(modelMatrix, -30f);
    return modelMatrix;
  }

  // returns background plane matrix
  private Mat4 getWallM() {
    Mat4 modelMatrix = new Mat4(1);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(16,1f,12), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundX(90), modelMatrix);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(0,12*0.5f,-16*0.5f), modelMatrix);
    return modelMatrix;
  }

  // returns spotlight box matrix
  private Mat4 getSpotBoxM() {
    modelMatrix = getModelMatrix();
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundYM(modelMatrix, translation1),
      Mat4Transform.scale(2,0.5f,0.5f));
    return modelMatrix;
  }

  // returns light position matrix
  private Mat4 getLightPosM() {
    modelMatrix = getModelMatrix();
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundYM(modelMatrix, translation1),
      Mat4.multiply(Mat4Transform.scale(0.3f,0.3f,0.3f), Mat4Transform.translate(3.5f,0,0)));
    return modelMatrix;
  }

  // returns light position vector
  private Vec3 getLightPosV() {
    modelMatrix = getModelMatrix();
    modelMatrix = Mat4.multiply(Mat4Transform.rotateAroundYM(modelMatrix, translation1),
      Mat4.multiply(Mat4Transform.scale(2,2,2), Mat4Transform.translate(3,0.25f,0.25f)));
    return Mat4.getMatrixPos(modelMatrix);
  }

  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }

  // RENDER
  private void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    wall.setModelMatrix(getWallM());
    light.setModelMatrix(getLightPosM());
    light.setPosition(getLightPosV());
    spotBox.setModelMatrix(getSpotBoxM());
    wall.setBackground(gl, startTime);
    spotBox.render(gl);
    present.render(gl);
    spotBase.render(gl);
    spotPole.render(gl);
    light.render(gl);
    floor.render(gl);
    wall.render(gl);
    if (rock) sgModel.rockHead(elapsedRock, startRock); // perform scene graph transformations
    if (roll) sgModel.rollBody(elapsedRoll, startRoll);
    if (srr) sgModel.slideRockRoll(elapsedCombo, startCombo);
    if (slide) sgModel.slideBody(elapsedSlide, startSlide);
    snowman.draw(gl);
  }
}
