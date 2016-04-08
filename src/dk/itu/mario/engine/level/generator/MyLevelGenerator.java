package dk.itu.mario.engine.level.generator;

import java.util.*;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.level.Level;
import dk.itu.mario.engine.level.MyLevel;

import dk.itu.mario.engine.PlayerProfile;

import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;



/* Conditions

* enemies should start out at ground level
* left and right parts of pipes in correct positions and single "top"
* no floating blocks out of reach

* enemy types:
	* RED_TURTLE
	* GREEN_TURTLE
	* GOOMPA
	* ARMORED_TURTLE
	* JUMP_FLOWER
	* CANNON_BALL
	* CHOMP_FLOWER

* things:
	* enemy
	* coins
	* pipe
	* power up
	* floating blocks
	* grounded block(s)
	* cannon
	* HILL_TOP?


* params:
	* cannon height
	* ground height, starting elevation
	* width of column rep - holes and pipes will be two

Constraints:

hole <-> enemy
hole <-> floating block
cannon <-> enemy

max height of reachable coins
not too many holes in a row - does the evaluate method check for this?

do pipes com in pairs?

*/



class ColumnRepresentation {
	// int heightDifference;
	// int initialElevation;
	Boolean hole = false; // a hole in the floor
	int coinCount = 0;
	Enemy enemy = null;
	Boolean powerUpBlock = false;
	int pipeHeight = 0; // 0 indicates no pipe

	public ColumnRepresentation clone() {
		ColumnRepresentation clone = new ColumnRepresentation();
		clone.hole = hole;
		clone.coinCount = coinCount;
		clone.enemy = enemy;
		clone.powerUpBlock = powerUpBlock;
		clone.pipeHeight = pipeHeight;

		return clone;
	}
}

class LevelRepresentation {
	int initialClearing = 15; // number of blocks to skip before making modifications
	ArrayList<ColumnRepresentation> columns;
	int levelHeight, levelWidth;

	public LevelRepresentation(int height, int width) {
		columns = new ArrayList<ColumnRepresentation>();
		levelHeight = height;
		levelWidth = width;
	}

	// generate level may ignore some details provided by columns in order to
	// make the level playable
	public MyLevel generateLevel() {
		// this index is the first clear block above the ground
		int aboveGroundLevel = levelHeight - 3;

		MyLevel level = new MyLevel(levelWidth,levelHeight,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND);

		for (int i = 0; i < columns.size(); i++) {
			int x = initialClearing + i;

			ColumnRepresentation col = columns.get(i);
			ColumnRepresentation prevCol = i > 0 ? columns.get(i-1) : null;

			if (prevCol != null && prevCol.pipeHeight > 0) {
				// right side of pipe

				// build top of pipe
				int topIndex = aboveGroundLevel-prevCol.pipeHeight+1;
				level.setBlock(x,topIndex,Level.TUBE_TOP_RIGHT);

				// build side of pipe
				for (int j = aboveGroundLevel; j > topIndex; j--) {
					level.setBlock(x,j,Level.TUBE_SIDE_RIGHT);
				}
			} else if (col.pipeHeight > 0) {
				// left side of pipe

				// build top of pipe
				int topIndex = aboveGroundLevel-col.pipeHeight+1;
				level.setBlock(x,topIndex,Level.TUBE_TOP_LEFT);

				// build side of pipe
				for (int j = aboveGroundLevel; j > topIndex; j--) {
					level.setBlock(x,j,Level.TUBE_SIDE_LEFT);
				}
			} else if (col.hole) {
				level.setBlock(x,levelHeight+1,Level.EMPTY);
				level.setBlock(x,levelHeight+2,Level.EMPTY);
			}

			// TODO
		}

		return level;
	}

	// makes a deep copy
	public LevelRepresentation clone() {
		LevelRepresentation clone = new LevelRepresentation(levelHeight, levelWidth);
		clone.initialClearing = initialClearing;
		for (ColumnRepresentation col : columns) {
			clone.columns.add(col.clone());
		}

		return clone;
	}

	public void mutate() {
		// TODO: mutate
	}
}



public class MyLevelGenerator{

	public static int NUM_CHILDREN = 1;
	public static int NUM_PARENTS = 2;

	public Level generateLevel(PlayerProfile playerProfile) {
		MyLevel parent=new MyLevel(205,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND);	
		//// YOUR CODE GOES BELOW HERE ////



		MyLevel bestChild = null;
		double bestChildScore = 0;

		for (int i = 0; i < NUM_CHILDREN; i++) {
			MyLevel child = parent.clone();
			child = (MyLevel)createDefaultLevel();
			//Evaluate the neighbor
			double score = playerProfile.evaluateLevel(child);
			System.out.println("Child Score: "+score);

			if (score > bestChildScore) {
				bestChild = child;
				bestChildScore = score;
			}
		}

		//// YOUR CODE GOES ABOVE HERE ////
		return (Level)bestChild;
	}


	public Level createDefaultLevel() {
		MyLevel level=new MyLevel(205,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND);	

		//Powerup block
		level.setBlock(15,10,Level.BLOCK_POWERUP);

		//Create enemy
		level.setSpriteTemplate(20, 12, new SpriteTemplate(Enemy.ENEMY_GREEN_KOOPA,false));
		
		//Create Gap
		level.setBlock(30,13,Level.EMPTY);
		level.setBlock(30,14,Level.EMPTY);
		level.setBlock(31,13,Level.EMPTY);
		level.setBlock(31,14,Level.EMPTY);

		//Create cannons
		level.setBlock(40,12,Level.CANNON_TOP);
		level.setBlock(40,12,Level.CANNON_MID);

		level.setBlock(41,10,Level.CANNON_TOP);
		level.setBlock(41,11,Level.CANNON_MID);
		level.setBlock(41,12,Level.CANNON_BASE);

		//Create pipes
		level.setBlock(50,12,Level.TUBE_SIDE_LEFT);
		level.setBlock(51,12,Level.TUBE_SIDE_RIGHT);
		level.setBlock(50,11,Level.TUBE_TOP_LEFT);
		level.setBlock(51,11,Level.TUBE_TOP_RIGHT);

		level.setBlock(53,12,Level.TUBE_SIDE_LEFT);
		level.setBlock(54,12,Level.TUBE_SIDE_RIGHT);
		level.setBlock(53,11,Level.TUBE_SIDE_LEFT);
		level.setBlock(54,11,Level.TUBE_SIDE_RIGHT);
		level.setBlock(53,10,Level.TUBE_TOP_LEFT);
		level.setBlock(54,10,Level.TUBE_TOP_RIGHT);

		//Create coins
		level.setBlock(55,9,Level.COIN);
		level.setBlock(56,9,Level.COIN);
		level.setBlock(57,9,Level.COIN);

		return (Level)level;
	}


}
