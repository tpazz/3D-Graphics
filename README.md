# 3D-Graphics
A graphical snowman scene coded entirely in OpenGL and Java that has various animations, lighting effects and movement. Manipulation of the shaders give the various objects a different texture that behave differently when exposed to light sources. The mesh of the snowman appears to have a matte finish that is not reflective, in comparison to the 'metal' ball that exhibits a specular highlight when the spotlight is shone over it.

<p align="center">
  <img src="/images/SnowmanScene.gif" width="550" height="400"/>

#### Animation
The snowman has the option to **Rock** its head, **Roll** its body, **Slide** itself from left to right, or a combination of all three: 

<p align="center">
  <img src="/images/RockRollSlide.gif" width="550" height="400"/>

#### Lighting
There are two light sources in the scene, the **sun** which acts as the ambient light casting over the whole scene, and a **lamp post** that casts a moving spotlight on the ground. Both of these sources can be toggled on or off independently.

<p align="center">
  <img src="/images/Lighting.gif" width="550" height="400"/>
  
#### Camera & Movement
You are able to move **Up**, **Down**, **Left** and **Right** using the arrow keys, and zoom in and out with the **A** and **Z** keys respectively. It is also possible to click and drag to change the angle of the camera. 

<p align="center">
  <img src="/images/Movement.gif" width="550" height="400"/>
