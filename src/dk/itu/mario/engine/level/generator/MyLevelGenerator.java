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
	int coinHeight = 0;
	int enemy = -1;
	int powerUpHeight = 0;
	int pipeHeight = 0; // 0 indicates no pipe
	int cannonHeight = 0;

	public ColumnRepresentation clone() {
		ColumnRepresentation clone = new ColumnRepresentation();
		clone.hole = hole;
		clone.coinCount = coinCount;
		clone.coinHeight = coinHeight;
		clone.enemy = enemy;
		clone.powerUpHeight = powerUpHeight;
		clone.pipeHeight = pipeHeight;
		clone.cannonHeight = cannonHeight;

		return clone;
	}
}

class LevelRepresentation {
	int initialClearing; // number of blocks to skip before making modifications
	ArrayList<ColumnRepresentation> columns;
	int levelHeight, levelWidth;

	public LevelRepresentation(int width, int height, int blankColumns) {
		columns = new ArrayList<ColumnRepresentation>();
		for (int x = blankColumns; x < width; x++)
			columns.add(new ColumnRepresentation());
		levelHeight = height;
		levelWidth = width;
		initialClearing = blankColumns;
	}

	ColumnRepresentation columnAt(int x) {
		return columns.get(x - initialClearing);
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
				level.setBlock(x,aboveGroundLevel+1,Level.EMPTY);
				level.setBlock(x,aboveGroundLevel+2,Level.EMPTY);
			} else if (col.powerUpHeight > 0) {
				level.setBlock(x,aboveGroundLevel-col.powerUpHeight+1,Level.BLOCK_POWERUP);
			} else if (col.cannonHeight > 0) {
				int topIndex = aboveGroundLevel-col.cannonHeight+1;

				level.setBlock(x, topIndex, Level.CANNON_TOP);

				if (topIndex != aboveGroundLevel) {
					level.setBlock(x, aboveGroundLevel, Level.CANNON_BASE);

					if (aboveGroundLevel - topIndex > 1) {
						for (int j = aboveGroundLevel-1; j > topIndex; j--) {
							level.setBlock(x, j, Level.CANNON_MID);
						}
					}
				}
			}

			if (col.enemy != -1) {
				level.setSpriteTemplate(x, aboveGroundLevel, new SpriteTemplate(col.enemy,false));
			}


			if (col.coinCount > 0) {
				int first = aboveGroundLevel-col.coinHeight+1;
				for (int j = first; j > first - col.coinCount; j--) {
					level.setBlock(x,j,Level.COIN);
				}
			}
		}

		return level;
	}

	// makes a deep copy
	public LevelRepresentation clone() {
		LevelRepresentation clone = new LevelRepresentation(levelHeight, levelWidth, initialClearing);
		clone.columns.clear();
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
		// MyLevel parent=new MyLevel(205,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND);	
		//// YOUR CODE GOES BELOW HERE ////

		LevelRepresentation parent = createDefaultLevelRepresentation();
		MyLevel parentLevel = parent.generateLevel();

		if(parent != null) return parentLevel; // TODO: remove

		LevelRepresentation bestChild = null;
		double bestChildScore = 0;

		for (int i = 0; i < NUM_CHILDREN; i++) {
			LevelRepresentation child = parent.clone();
			MyLevel childLevel = (MyLevel)createDefaultLevel();
			//Evaluate the neighbor
			double score = playerProfile.evaluateLevel(childLevel);
			System.out.println("Child Score: "+score);

			if (score > bestChildScore) {
				bestChild = child;
				bestChildScore = score;
			}
		}

		//// YOUR CODE GOES ABOVE HERE ////
		return (Level)bestChild.generateLevel();
	}


	public LevelRepresentation createDefaultLevelRepresentation() {
		LevelRepresentation rep = new LevelRepresentation(205, 15, 15);

		rep.columnAt(15).powerUpHeight = 3;

		rep.columnAt(20).enemy = Enemy.ENEMY_GREEN_KOOPA;

		rep.columnAt(30).hole = true;
		rep.columnAt(31).hole = true;

		rep.columnAt(40).cannonHeight = 2;
		rep.columnAt(41).cannonHeight = 3;

		rep.columnAt(50).pipeHeight = 2;
		rep.columnAt(53).pipeHeight = 3;

		rep.columnAt(55).coinCount = 1; rep.columnAt(55).coinHeight = 4;
		rep.columnAt(56).coinCount = 1; rep.columnAt(56).coinHeight = 4;
		rep.columnAt(57).coinCount = 1; rep.columnAt(57).coinHeight = 4;

		return rep;
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
		level.setBlock(40,11,Level.CANNON_TOP);
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
