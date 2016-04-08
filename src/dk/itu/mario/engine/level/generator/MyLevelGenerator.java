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

*/

class ColumnRepresentation {
	// int heightDifference;
	// int initialElevation;
	Boolean hole; // a hole in the floor
	int coinCount;
	Enemy enemy;
	Boolean powerUpBlock;

	public ColumnRepresentation clone() {
		ColumnRepresentation clone = new ColumnRepresentation();
		clone.hole = hole;
		clone.coinCount = coinCount;
		clone.enemy = enemy;
		clone.powerUpBlock = powerUpBlock;

		return clone;
	}
}

class LevelRepresentation {
	int initialClearing = 15; // number of blocks to skip before making modifications
	ArrayList<ColumnRepresentation> columns;
	int levelHeight;

	public LevelRepresentation(int height) {
		columns = new ArrayList<ColumnRepresentation>();
		levelHeight = height;
	}

	public MyLevel generateLevel(int height) {
		for (int i = 0; i < columns.size(); i++) {
			int x = initialClearing + i;
		}


		// TODO
		return new MyLevel(columns.size() + initialClearing, height);
	}

	// makes a deep copy
	public LevelRepresentation clone() {
		LevelRepresentation clone = new LevelRepresentation(levelHeight);
		clone.initialClearing = initialClearing;
		for (ColumnRepresentation col : columns) {
			clone.columns.add(col.clone());
		}

		return clone;
	}
}



public class MyLevelGenerator{

	public static int NUM_CHILDREN = 1;

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
