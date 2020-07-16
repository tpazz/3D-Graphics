/* I declare that this code is my own work */
/* Author Theo Koorehpaz
/* tkoorehpaz1@sheffield.ac.uk
/* 04/12/2019 */
/* This class represents the objects that will be used for constructing a scene graph */

import gmaths.*;
import com.jogamp.opengl.*;

public class SceneGraph {

  private Camera camera;
  private Model snowball, button, carrot, hat, bean;
  private Light light;
  private SGNode snowmanRoot;
  private TransformNode snowmanMoveTranslate, snowmanMoveHead, snowmanMoveRotate;
  private float xPosition, zPosition, offsetY, offsetX, translation1, translation2;
  private Mesh mesh;
  private Shader shader;
  private Material material;
  private Mat4 modelMatrix;
  private GL3 gl;

  private static final int SMALL_SCALE = 5;
  private static final int MEDIUM_SCALE = 10;
  private static final int LARGE_SCALE = 30;

  // constructor
  public SceneGraph(GL3 gl, Light light, Camera camera, Shader shader) {
    this.gl = gl;
    this.light = light;
    this.camera = camera;
    this.shader = shader;
  }

  public void dispose(GL3 gl) {
    snowball.dispose(gl);
    button.dispose(gl);
    carrot.dispose(gl);
    hat.dispose(gl);
    bean.dispose(gl);
  }

  // ANIMATIONS ****************************************************************

  // slide snowman left and right
  public void slideBody(double eS, double sS) {
    eS = getSeconds()-sS;
    xPosition = SMALL_SCALE*(float)Math.sin(eS);
    snowmanMoveTranslate.setTransform(Mat4Transform.translate(xPosition,0,zPosition));
    snowmanMoveTranslate.update();
  }

  // roll snowman body
  public void rollBody(double eR, double sR) {
    eR = getSeconds()-sR;
    translation1 = 30*(float)Math.sin(eR);
    snowmanMoveRotate.setTransform(Mat4Transform.rotateAroundZ(translation1));
    snowmanMoveRotate.update();
  }

  // rock snowman head
  public void rockHead(double eR, double sR) {
    eR = getSeconds()-sR;
    translation2 = MEDIUM_SCALE*(float)Math.cos(eR);
    snowmanMoveHead.setTransform(Mat4Transform.rotateAroundX(translation2));
    snowmanMoveHead.update();
  }

  // slide rock and roll snowman
  public void slideRockRoll(double eC, double sC) {
    eC = getSeconds()-sC;
    translation1 = MEDIUM_SCALE*(float)Math.sin(eC*SMALL_SCALE);
    translation2 = MEDIUM_SCALE*(float)Math.cos(eC*SMALL_SCALE);
    snowmanMoveHead.setTransform(Mat4.multiply(Mat4Transform.rotateAroundZ(translation2),
       Mat4.multiply(Mat4Transform.rotateAroundY((float)(eC*MEDIUM_SCALE*20)),
       Mat4Transform.rotateAroundX(translation1)))); // head rotation
    translation1 = LARGE_SCALE*(float)Math.sin(eC*SMALL_SCALE);
    translation2 = LARGE_SCALE*(float)Math.cos(eC*SMALL_SCALE);
    snowmanMoveRotate.setTransform(Mat4.multiply(Mat4Transform.rotateAroundZ(translation1),
      Mat4.multiply(Mat4Transform.rotateAroundY((float)(-eC*MEDIUM_SCALE*10)),
      Mat4Transform.rotateAroundX(translation2)))); // body rotation
    xPosition = 3*(float)Math.cos(eC);
    zPosition = 3*(float)Math.sin(2*eC); // leminscate of Bernoulli path
    snowmanMoveTranslate.setTransform(Mat4Transform.translate(xPosition,0,zPosition));
    snowmanMoveTranslate.update();
  }

  // reset variables
  public void reset() {
    xPosition = 0;
    zPosition = 0;
    snowmanMoveRotate.setTransform(Mat4Transform.rotateAroundZ(0));
    snowmanMoveRotate.update();
    snowmanMoveHead.setTransform(Mat4Transform.rotateAroundX(0));
    snowmanMoveHead.update();
    snowmanMoveTranslate.setTransform(Mat4Transform.translate(0,0,0));
    snowmanMoveTranslate.update();
  }

  private double getSeconds() {
    return System.currentTimeMillis()/1000.0;
  }

  // SCENE GRAPH ***************************************************************

  // returns constructed scene graph of snowman as SGNode
  public SGNode buildSceneGraph() {
    int[] textureId0 = TextureLibrary.loadTexture(gl, "textures/snowMan.jpg");
    int[] textureId1 = TextureLibrary.loadTexture(gl, "textures/woven.jpg");
    int[] textureId2 = TextureLibrary.loadTexture(gl, "textures/dirt.jpg");
    int[] textureId3 = TextureLibrary.loadTexture(gl, "textures/xmasWool.jpg");
    int[] textureId4 = TextureLibrary.loadTexture(gl, "textures/carrot.jpg");

    // HAT
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_obj.txt", "fs_1tex_obj.txt");
    material = new Material(new Vec3(1,1,1), new Vec3(1,1,1), new Vec3(1f,1f,1f), 320.0f);
    hat = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId1);
    bean = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId3);

    // NOSE
    material = new Material(new Vec3(0.3f,0.1f,0), new Vec3(0.3f,0.3f,0.3f), new Vec3(0.5f,0.5f,0.5f), 32.0f);
    carrot = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId4);

    // HEAD and BODY
    shader = new Shader(gl, "vs_obj.txt", "fs_2tex_obj.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0,0,0), 1f);
    snowball = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId2, textureId0);

    // EYES and BUTTONS
    shader = new Shader(gl, "vs_obj.txt", "fs_col_obj.txt");
    material = new Material(new Vec3(0.1f,0.1f,0.1f), new Vec3(0.1f,0.1f,0.1f), new Vec3(1,1,1), 32.0f);
    button = new Model(gl, camera, light, shader, material, modelMatrix, mesh);

    float bodyRadius = 3f;
    float headScale = 2f;
    float buttonScale = 0.4f;
    float eyeScale = 0.3f;
    float hatScale = 5f;
    float beanScale = hatScale/10;
    float circleRatio = (float)Math.sqrt(2f);

    snowmanRoot = new NameNode("root");
    snowmanMoveTranslate = new TransformNode("snowman transform",Mat4Transform.translate(xPosition,0,zPosition));
    snowmanMoveHead = new TransformNode("snowman transform",Mat4Transform.translate(xPosition,0,zPosition));
    snowmanMoveRotate = new TransformNode("snowman transform",Mat4Transform.translate(xPosition,0,zPosition));

    NameNode body = new NameNode("body");
      Mat4 m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.scale(bodyRadius,bodyRadius,bodyRadius));
      m = Mat4.multiply(m, Mat4Transform.translate(0,0.5f,0));
        TransformNode bodyTransform = new TransformNode("body transform",m);
          ModelNode bodyShape = new ModelNode("Sphere(body)", snowball);

    NameNode head = new NameNode("head");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,bodyRadius+0.8f,0));
      m = Mat4.multiply(m, Mat4Transform.scale(headScale,headScale,headScale));
      TransformNode headTransform = new TransformNode("head transform", m);
        ModelNode headShape = new ModelNode("Sphere(head)", snowball);

    NameNode buttonL = new NameNode("buttonL");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,1,circleRatio));
      m = Mat4.multiply(m, Mat4Transform.scale(buttonScale,buttonScale,buttonScale));
      TransformNode buttonLTransform = new TransformNode("buttonL transform", m);
        ModelNode buttonLShape = new ModelNode("Sphere(lowButton)", button);

    NameNode buttonM = new NameNode("buttonM");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,bodyRadius/2,bodyRadius/2));
      m = Mat4.multiply(m, Mat4Transform.scale(buttonScale,buttonScale,buttonScale));
      TransformNode buttonMTransform = new TransformNode("buttonM transform", m);
        ModelNode buttonMShape = new ModelNode("Sphere(midButton)", button);

    NameNode buttonT = new NameNode("buttonT");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,2,circleRatio));
      m = Mat4.multiply(m, Mat4Transform.scale(buttonScale,buttonScale,buttonScale));
      TransformNode buttonTTransform = new TransformNode("buttonT transform", m);
        ModelNode buttonTShape = new ModelNode("Sphere(topButton)", button);

    NameNode leftEye = new NameNode("leftEye");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(-eyeScale,circleRatio*3,0.8f));
      m = Mat4.multiply(m, Mat4Transform.scale(eyeScale,eyeScale,eyeScale));
      TransformNode leftEyeTransform = new TransformNode("leftEye transform", m);
        ModelNode leftEyeShape = new ModelNode("Sphere(leftEye)", button);

    NameNode rightEye = new NameNode("rightEye");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0.3f,circleRatio*3,0.8f));
      m = Mat4.multiply(m, Mat4Transform.scale(eyeScale,eyeScale,eyeScale));
      TransformNode rightEyeTransform = new TransformNode("rightEye transform", m);
        ModelNode rightEyeShape = new ModelNode("Sphere(rightEye)", button);

    NameNode nose = new NameNode("nose");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,headScale*2,0.8f));
      m = Mat4.multiply(m, Mat4Transform.scale(0.2f,0.2f,1.5f));
      TransformNode noseTransform = new TransformNode("buttonT transform", m);
        ModelNode noseShape = new ModelNode("Sphere(nose)", carrot);

    NameNode mouth = new NameNode("mouth");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,3.5f,0.82f));
      m = Mat4.multiply(m, Mat4Transform.scale(1,0.3f,0.3f));
      TransformNode mouthTransform = new TransformNode("mouth transform", m);
        ModelNode mouthShape = new ModelNode("Sphere(mouth)", button);

    NameNode hatBase = new NameNode("hatBase");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,hatScale-0.2f,0));
      m = Mat4.multiply(m, Mat4Transform.scale(hatScale,hatScale/10,hatScale));
      TransformNode hatBaseTransform = new TransformNode("hatBase transform", m);
        ModelNode hatBaseShape = new ModelNode("Sphere(hatBase)", hat);

    NameNode hatMid = new NameNode("hatMid");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,hatScale-0.2f,0));
      m = Mat4.multiply(m, Mat4Transform.scale(1,1.8f,1));
      TransformNode hatMidTransform = new TransformNode("hatMid transform", m);
        ModelNode hatMidShape = new ModelNode("Sphere(hatMid)", hat);

    NameNode hatBean = new NameNode("hatBean");
      m = new Mat4(1);
      m = Mat4.multiply(m, Mat4Transform.translate(0,hatScale+0.9f,0));
      m = Mat4.multiply(m, Mat4Transform.scale(beanScale,beanScale,beanScale));
      TransformNode hatBeanTransform = new TransformNode("hatBean transform", m);
        ModelNode hatBeanShape = new ModelNode("Sphere(hatBean)", bean);

    snowmanRoot.addChild(snowmanMoveTranslate);
      snowmanMoveTranslate.addChild(snowmanMoveRotate);
        snowmanMoveRotate.addChild(body);
          body.addChild(bodyTransform);
            bodyTransform.addChild(bodyShape);
          body.addChild(snowmanMoveHead);
            snowmanMoveHead.addChild(head);
              head.addChild(headTransform);
              headTransform.addChild(headShape);
                head.addChild(leftEye);
                  leftEye.addChild(leftEyeTransform);
                  leftEyeTransform.addChild(leftEyeShape);
                head.addChild(rightEye);
                  rightEye.addChild(rightEyeTransform);
                  rightEyeTransform.addChild(rightEyeShape);
                head.addChild(nose);
                  nose.addChild(noseTransform);
                  noseTransform.addChild(noseShape);
                head.addChild(mouth);
                  mouth.addChild(mouthTransform);
                  mouthTransform.addChild(mouthShape);
                head.addChild(hatBase);
                  hatBase.addChild(hatBaseTransform);
                  hatBaseTransform.addChild(hatBaseShape);
                head.addChild(hatMid);
                  hatMid.addChild(hatMidTransform);
                  hatMidTransform.addChild(hatMidShape);
                head.addChild(hatBean);
                  hatBean.addChild(hatBeanTransform);
                  hatBeanTransform.addChild(hatBeanShape);
            body.addChild(buttonL);
              buttonL.addChild(buttonLTransform);
              buttonLTransform.addChild(buttonLShape);
            body.addChild(buttonM);
              buttonL.addChild(buttonMTransform);
              buttonMTransform.addChild(buttonMShape);
            body.addChild(buttonT);
              buttonT.addChild(buttonTTransform);
              buttonTTransform.addChild(buttonTShape);

    snowmanRoot.update();
    return snowmanRoot;
  }
}
