"Basic 3D Pong-Like Environment"

Here, I've attempted to build a the groundwork
for a fun 3D pong enviroment.

The environment includes a texturized globe environment
containing two user-controlled paddles and a randomized
trajectory ball. The goal is to hit the ball. Upon hitting,
the ball selects a new direction and to head toward. If the
ball exceeds the paddle's radius of trajectory, the ball
resets at the origin with a new randomized direction.

A special feature is the use of the LWJGL slick package to 
add texture to the my environment. Some simple physics for
tracjecories has also been implemented.

Information and code for the use of textures was obtained
from http://slick.ninjacave.com/javadoc-util/.

Display lists are used to generate all but the coordinate axes
on the field. 

A simple game logic is also used to distinguish
between player turns. 

Lighting in the scene rounds the ball in play and shines 
a small yellow or green light on its top depending on the 
turn of a player. 

Last, I have made the camera follow one of the players 
simply as an exercise in camera location coordination.

-------------- USER CONTROLS -----------------

Press A/D to move Player 2 left/right .

Use arrow keys to move Player 1.
