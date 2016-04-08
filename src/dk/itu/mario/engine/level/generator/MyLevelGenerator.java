package dk.itu.mario.engine.level.generator;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.level.Level;
import dk.itu.mario.engine.level.MyLevel;

import dk.itu.mario.engine.PlayerProfile;

import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;

public class MyLevelGenerator{

	public Level generateLevel(PlayerProfile playerProfile) {
		MyLevel level=new MyLevel(205,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND);	
		//// YOUR CODE GOES BELOW HERE ////

		MyLevel neighbor = level.clone();

		//Powerup block
		neighbor.setBlock(15,10,Level.BLOCK_POWERUP);

		//Create enemy
		neighbor.setSpriteTemplate(20, 12, new SpriteTemplate(Enemy.ENEMY_GREEN_KOOPA,false));
		
		//Create Gap
		neighbor.setBlock(30,13,Level.EMPTY);
		neighbor.setBlock(30,14,Level.EMPTY);
		neighbor.setBlock(31,13,Level.EMPTY);
		neighbor.setBlock(31,14,Level.EMPTY);

		//Create cannons
		neighbor.setBlock(40,11,Level.CANNON_TOP);
		neighbor.setBlock(40,12,Level.CANNON_MID);

		neighbor.setBlock(41,10,Level.CANNON_TOP);
		neighbor.setBlock(41,11,Level.CANNON_MID);
		neighbor.setBlock(41,12,Level.CANNON_BASE);

		//Create pipes
		neighbor.setBlock(50,12,Level.TUBE_SIDE_LEFT);
		neighbor.setBlock(51,12,Level.TUBE_SIDE_RIGHT);
		neighbor.setBlock(50,11,Level.TUBE_TOP_LEFT);
		neighbor.setBlock(51,11,Level.TUBE_TOP_RIGHT);

		neighbor.setBlock(53,12,Level.TUBE_SIDE_LEFT);
		neighbor.setBlock(54,12,Level.TUBE_SIDE_RIGHT);
		neighbor.setBlock(53,11,Level.TUBE_SIDE_LEFT);
		neighbor.setBlock(54,11,Level.TUBE_SIDE_RIGHT);
		neighbor.setBlock(53,10,Level.TUBE_TOP_LEFT);
		neighbor.setBlock(54,10,Level.TUBE_TOP_RIGHT);

		//Create coins
		neighbor.setBlock(55,9,Level.COIN);
		neighbor.setBlock(56,9,Level.COIN);
		neighbor.setBlock(57,9,Level.COIN);

		//Evaluate the neighbor
		System.out.println("Neighbor Score: "+playerProfile.evaluateLevel(neighbor));

		//Set level to (best scoring) neighbor
		level = neighbor;


		//// YOUR CODE GOES ABOVE HERE ////
		return (Level)level;
	}


}
