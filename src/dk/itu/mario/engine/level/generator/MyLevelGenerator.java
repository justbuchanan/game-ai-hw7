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

	void mutate(Random rand, double multiplier) {
		double HOLE_CHANCE = 0.001 * multiplier;
		double COIN_COUNT_CHANCE = 0.004 * multiplier + HOLE_CHANCE;
		double COIN_HEIGHT_CHANCE = 0.003 * multiplier + COIN_COUNT_CHANCE;
		double ENEMY_CHANCE = 0.001 * multiplier + COIN_HEIGHT_CHANCE;
		double POWER_UP_CHANCE = 0.002 * multiplier + ENEMY_CHANCE;
		double PIPE_CHANCE = 0.001 * multiplier + POWER_UP_CHANCE;
		double CANNON_CHANCE = 0.001 * multiplier + PIPE_CHANCE;

		double roll = rand.nextDouble();

		if (roll < HOLE_CHANCE) {
			hole = !hole;
			if (hole) {
				enemy = -1;
				powerUpHeight = 0;
				pipeHeight = 0;
				cannonHeight = 0;
			}
		} else if (roll < COIN_COUNT_CHANCE) {
			coinCount += rand.nextInt(5) - 2; // subtract 2, add 2, or something in-between
			if (coinCount < 0) coinCount = 0;
			if (coinHeight == 0) coinHeight = 1;
		} else if (roll < COIN_HEIGHT_CHANCE) {
			coinHeight += rand.nextInt(5) - 2; // subtract 2, add 2, or something in-between
			if (coinHeight <= 0) {
				coinHeight = 0;
				coinCount = 0;
			}
		} else if (roll < ENEMY_CHANCE) {
			enemy = rand.nextInt(4); // enemy enum ranges from [0-3]
		} else if (roll < POWER_UP_CHANCE) {
			powerUpHeight += rand.nextInt(5) - 2; // subtract 2, add 2, or something in-between
			if (powerUpHeight <= 1) powerUpHeight = 0;
		} else if (roll < PIPE_CHANCE) {
			pipeHeight += rand.nextInt(5) - 2; // subtract 2, add 2, or something in-between
			if (pipeHeight < 0) pipeHeight = 0;
		} else if (roll < CANNON_CHANCE) {
			cannonHeight += rand.nextInt(5) - 2; // subtract 2, add 2, or something in-between
			if (cannonHeight < 0) cannonHeight = 0;
		}
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

			int clearY = aboveGroundLevel;

			ColumnRepresentation col = columns.get(i);
			ColumnRepresentation prevCol = i > 0 ? columns.get(i-1) : null;

			Boolean occupied = false;

			if (prevCol != null && prevCol.pipeHeight > 0) {
				// right side of pipe

				// build top of pipe
				int topIndex = aboveGroundLevel-prevCol.pipeHeight+1;
				level.setBlock(x,topIndex,Level.TUBE_TOP_RIGHT);

				// build side of pipe
				for (int j = aboveGroundLevel; j > topIndex; j--) {
					level.setBlock(x,j,Level.TUBE_SIDE_RIGHT);
				}

				clearY = topIndex - 1;
				occupied = true;

				// Remove pipe from this block, it doesn't make sense
				col.pipeHeight = 0;
			} else if (col.pipeHeight > 0) {
				// left side of pipe

				// build top of pipe
				int topIndex = aboveGroundLevel-col.pipeHeight+1;
				level.setBlock(x,topIndex,Level.TUBE_TOP_LEFT);

				// build side of pipe
				for (int j = aboveGroundLevel; j > topIndex; j--) {
					level.setBlock(x,j,Level.TUBE_SIDE_LEFT);
				}

				clearY = topIndex - 1;
				occupied = true;
			} else if (col.hole) {
				level.setBlock(x,aboveGroundLevel+1,Level.EMPTY);
				level.setBlock(x,aboveGroundLevel+2,Level.EMPTY);
				occupied = true;
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

				clearY = topIndex - 1;
				occupied = true;
			}

			if (col.enemy != -1 && !occupied) {
				level.setSpriteTemplate(x, aboveGroundLevel, new SpriteTemplate(col.enemy,false));
			}


			if (col.coinCount > 0) {
				int first = clearY - col.coinHeight + 1;
				for (int j = first; j > first - col.coinCount; j--) {
					level.setBlock(x,j,Level.COIN);
				}
			}
		}

		return level;
	}

	// makes a deep copy
	public LevelRepresentation clone() {
		LevelRepresentation clone = new LevelRepresentation(levelWidth, levelHeight, initialClearing);
		clone.columns.clear();
		for (ColumnRepresentation col : columns) {
			clone.columns.add(col.clone());
		}

		return clone;
	}

	public void mutate(Random rand, double multiplier) {
		for (ColumnRepresentation col : columns) {
			col.mutate(rand, multiplier);
		}
	}
}





class MyPair implements Comparable<MyPair> {
	double score;
	LevelRepresentation rep;

	public MyPair(LevelRepresentation r, double s) {
		rep = r;
		score = s;
	}

	// Returns a negative integer, zero, or a positive integer as this object is
	// less than, equal to, or greater than the specified object.
	public int compareTo(MyPair o) {
		return Double.valueOf(score).compareTo(o.score);
	}
}



public class MyLevelGenerator{

	public static int POPULATION_SIZE = 15;
	public static int MAX_GENERATIONS = 300;

	public Level generateLevel(PlayerProfile playerProfile) {
		Random rand = new Random();

		ArrayList<MyPair> population = new ArrayList<MyPair>();

		// create initial population
		LevelRepresentation parent = createDefaultLevelRepresentation();
		double score = playerProfile.evaluateLevel(parent.generateLevel());
		for (int i = 0; i < POPULATION_SIZE; i++) {
			population.add(new MyPair(parent, score));
		}

		for (int g = 0; g < MAX_GENERATIONS; g++) {
			System.out.println("Generation " + g);

			ArrayList<MyPair> newGeneration = new ArrayList<MyPair>();

			for (MyPair pair : population) {
				LevelRepresentation child = pair.rep.clone();
				child.mutate(rand, 2);

				score = playerProfile.evaluateLevel(child.generateLevel());
				System.out.println("  Child Score: "+score);

				newGeneration.add(new MyPair(child, score));
			}

			population.addAll(newGeneration);

			Collections.sort(population, Collections.reverseOrder());

			// thin the herd
			while (population.size() > POPULATION_SIZE) population.remove(population.size() - 1);

			double bestScore = population.get(0).score;
			System.out.println("Best score so far: " + bestScore);

			if (bestScore > 0.8) {
				System.out.println("Score exceeds 0.8, done");
				break;
			}
		}

		//// YOUR CODE GOES ABOVE HERE ////
		Level level = (Level)population.get(0).rep.generateLevel();

		// System.out.println("------------------------------------------");
		// for (int x = 0; x < level.getWidth(); x++) {
		// 	for (int y = 0; y < level.getHeight(); y++) {
		// 		byte b = level.getBlock(x,y);
		// 		if (b == Level.TUBE_TOP_LEFT) {
		// 			System.out.print('L');
		// 		} else if (b == Level.TUBE_TOP_RIGHT) {
		// 			System.out.print('R');
		// 		} else {
		// 			System.out.print(b);
		// 		}
		// 	}
		// 	System.out.println();
		// }
		// System.out.println("------------------------------------------");


		return level;
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
