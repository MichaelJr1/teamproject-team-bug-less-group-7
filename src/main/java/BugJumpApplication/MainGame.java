package BugJumpApplication;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


import acm.graphics.*;


public class MainGame extends GraphicsPane {
	private static final int RIGHT_VELOCITY = 10;
	private static final int LEFT_VELOCITY = -10;
	
	/////////////////////////////////////////////////////////////

	/* All player's variables used in this class */
	private Player player;
	private GImage playerImage;
	private int xVel; //left and right velocity of the player object
	private int fireRate;
	private Boolean isPrevOrientationRight; // used for wall detection
	private int playerWidth;
		
	/////////////////////////////////////////////////////////////

	/* Data Structures that holds all object in game scene */
	private HashMap<GImage, Collectable> collectablesMap;
	private HashMap<GImage, Enemy> enemiesMap;
	private HashMap<GImage, Terrain> terrainMap;
	private HashMap<GImage, Bullet> bulletMap;
	
	//Arraylist of all keys pressed at once
	private ArrayList<Integer> keyList;
	
	
	/////////////////////////////////////////////////////////////

	/* Variables that are convenient for the game */
	private int timerCount;
	private boolean isGamePaused;
	private int stars = 0;
	private MainApplication program;
	private Dimension dimension;
	
	
	/////////////////////////////////////////////////////////////
	
	private GLabel starsGlable;
	private GLabel heartGLabel;
	private GImage background;
	private AudioPlayer audio;
	private GRect victoryBorder;
	private GParagraph victory;
	private GButton nextLevelButton;
	
	private GButton mainMenuButton;
	
	
	public MainGame(MainApplication e) {
		program = e;
	}
	
	@Override
	public void showContents() {
		dimension = Toolkit.getDefaultToolkit().getScreenSize();
		keyList = new ArrayList<Integer>();
		collectablesMap = new HashMap<>();
		enemiesMap = new HashMap<>();
		terrainMap = new HashMap<>();
		bulletMap = new HashMap<>();
		isPrevOrientationRight = null;
		fireRate = 0;
		timerCount = 0;
		isGamePaused = false;
		
		//audio = audio.getInstance();
		//audio.playSoundWithOptions("sounds", "r2d2.mp3", true);
		setupTerrain();
		setupCollectables();
		setupPlayer();
		setupGUI();
		setupEnemies();
		program.setupTimer(30);
		player.startTimer();
	}

	@Override
	public void hideContents() {
		program.removeAll();	
		dimension = null;
		collectablesMap = null;
		enemiesMap = null;
		terrainMap = null;
		bulletMap = null;
		keyList = null;
		player = null;
		playerImage = null;
		starsGlable = null;
		heartGLabel = null;
		victoryBorder = null;
		victory = null;
		nextLevelButton = null;
		mainMenuButton = null;
	
		
	}
	
	
	public int getStars() {
		return stars;
		
	}

	public void setHearts(int s) {
		stars = s;
	}
	
	/**
	 * Gets the keycode of the last key pressed by the player. 
	 * Adds key pressed to a list of all keys pressed on the keyboard at once
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		
		if (!keyList.contains(keyCode)) {
			keyList.add(keyCode);
		}
	}
	
	/**
	 * As soon as one key is released from the keyboard, it is removed from the list of all keys held down by the user
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		if (keyList.contains(e.getKeyCode())) {
			keyList.remove(keyList.indexOf(e.getKeyCode()));
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		GObject obj = program.getElementAt(e.getX(), e.getY());
		if (obj == mainMenuButton) {
			program.switchToMenu();
		}
	}
	
	@Override
	public void performAction(ActionEvent e) {
		if (playerImage != null && isGamePaused == false) {
			timerCount++;
			playerImage.setLocation(player.getX(), player.getY());
			updateBullet();
			enemyAwareness();
			doEnemyActions();
	
			// If the d key is held and the a key is not
			if (keyList.contains(68) && !keyList.contains(65)) {
				if (xVel < RIGHT_VELOCITY) {
					xVel += 2;
				}
				
			}
			// If the a key is held and the d key is not
			else if (keyList.contains(65) && !keyList.contains(68)) {
				if (xVel > LEFT_VELOCITY) {
					xVel -= 2;
				}
				// Case for if no key is held or no specific key combination is found
			} else {
				// Slows momentum of player to a stop
				if (xVel != 0) {
					if (xVel > 0) {
						xVel -= 2;
					} else {
						xVel += 2;
					}
				}
			}
			if(player.checkOrientation(xVel)) {
				changePlayerImage();
			}
			
	
			if (keyList.contains(87)) {
				player.turnOnJumping();
			}
	
			if (checkCollision()) {
				if (isPrevOrientationRight == player.isRightOrientation) {
					xVel = 0;
				} else if (isPrevOrientationRight != player.isRightOrientation) {
					player.isOnWall = false;
				}
			}
			
			if (player.getX() <= 100 && xVel < 0) {
				for (Entry<GImage, Terrain> entry : terrainMap.entrySet()) {
					GImage key = entry.getKey();
					Terrain val = entry.getValue();
					key.setLocation(key.getX()-xVel, key.getY());
				}
				for (Entry<GImage, Collectable> entry : collectablesMap.entrySet()) {
					GImage key = entry.getKey();
					Collectable val = entry.getValue();
					key.setLocation(key.getX()-xVel, key.getY());
				}
				for (Entry<GImage, Enemy> entry : enemiesMap.entrySet()) {
					GImage key = entry.getKey();
					Enemy val = entry.getValue();
	
					val.moveXAxis(-xVel);
					if (val.getAwareness()) {
						key.setLocation(val.getX(), val.getY());
					}
					
				}
			}
			else if (player.getX()+playerImage.getWidth() >= 1000 && xVel > 0) {
				for (Entry<GImage, Terrain> entry : terrainMap.entrySet()) {
					GImage key = entry.getKey();
					Terrain val = entry.getValue();
					key.setLocation(key.getX()-xVel, key.getY());
				}
				for (Entry<GImage, Collectable> entry : collectablesMap.entrySet()) {
					GImage key = entry.getKey();
					Collectable val = entry.getValue();
					key.setLocation(key.getX()-xVel, key.getY());
				}
				for (Entry<GImage, Enemy> entry : enemiesMap.entrySet()) {
					GImage key = entry.getKey();
					Enemy val = entry.getValue();
					val.moveXAxis(-xVel);
					if (val.getAwareness()) {
						key.setLocation(val.getX(), val.getY());
					}
				}
			}
			else {
				player.move(xVel, 0);
			}	
	
			
			// adding a bullet on the screen when pressing Space
			if (keyList.contains(32) && player.weapon != null && fireRate <= 0) {
				switch (player.weapon.wType) {
				case HANDHELD: {
					Bullet bullet = player.weapon.attack(new GPoint(player.getX(), player.getY()), player.isRightOrientation);
					GImage image =  new GImage("/Images/rightBullet.png", bullet.getX(), bullet.getY());
					if (!player.isRightOrientation) {image.setImage("/Images/leftBullet.png");}
					bulletMap.put(image, bullet);
					program.add(image);
					fireRate = 35;
					break;
				}
				case MELEE:
					Bullet bullet;
					GImage image; 
					if (player.isRightOrientation) {
						bullet = new Bullet(player.getX()+60, player.getY()-50, 15, 0, true, 15);
						image = new GImage("/Images/rightMeleeWave.png", bullet.getX(), bullet.getY());
					}
					else {
						bullet = new Bullet(player.getX()-150, player.getY()-50, 15, 180, true, 15);
						image = new GImage("/Images/leftMeleeWave.png", bullet.getX(), bullet.getY());
					}
					bulletMap.put(image, bullet);
					program.add(image);
					fireRate = 35;
					break;
				default:
					System.out.println("Unknown Weapon Type");
				}
			}
			if (player.weapon != null) {fireRate--;}
	
			
			if (player.getY() + 50 > dimension.getHeight() || player.isDead()) {
				System.out.println("player is dead");
				program.remove(playerImage);
				playerImage = null;
				program.switchToMenu();
				
			}
		}
	}

	
	private void changePlayerImage() {
		if (player.weapon != null) {
			switch (player.weapon.getWeaponType()) {
			case MELEE: {
				if (player.isRightOrientation) {playerImage.setImage("/Images/rightPlayerSword.png");}
				else {playerImage.setImage("/Images/leftPlayerSword.png");}
				playerWidth = (int)playerImage.getWidth();
				return;
			}
			case HANDHELD:
				if (player.isRightOrientation) {playerImage.setImage("/Images/rightPlayerGun.png");}
				else {playerImage.setImage("/Images/leftPlayerGun.png");}
				playerWidth = (int)playerImage.getWidth();
				return;
			default:
				System.out.println("Invalid weapon type : changePlayerImage()");
				return;
			}
		}
		else {
			if (player.isRightOrientation) {
				playerImage.setImage("/Images/rightPlayer.png");
				playerWidth = (int)playerImage.getWidth();
				return;
			}
			else {
				playerImage.setImage("/Images/leftPlayer.png");
				playerWidth = (int)playerImage.getWidth();
				return;
			}
		}
	}
	
	/**
	 * ONLY Checks player's left, right, top, and bottom collision. work hand-on-hand with objectPlayerCollision()
	 * @return true if player is colliding with a wall. False otherwise
	 */
	private boolean checkCollision() {
		if (objectPlayerCollision(new GObject[] {program.getElementAt(player.getX()+.333*playerWidth, player.getY()-6),
			program.getElementAt(player.getX()+.667*playerWidth, player.getY()-6)})) {
				GObject obj = program.getElementAt(player.getX() + playerWidth/2, player.getY()-6);
				player.turnOffJumping();
				if (obj != null && obj != background) {				
					player.setY((int)obj.getY()+(int)obj.getHeight()+1);
				}
		}
			
		
		// functionality for ground detection
		if(objectPlayerCollision(new GObject[]{program.getElementAt(player.getX()+2, player.getY() + 54), 
		   program.getElementAt(player.getX() + (playerWidth-2), player.getY() + 54)})) {
			

			player.isInAir = false;
			GObject obj = program.getElementAt(player.getX() + 5, player.getY() + 55);
			GObject obj2 = program.getElementAt(player.getX() + (playerWidth-5), player.getY()+55);
			

			
			//obj != null
			if (obj != null && obj != background) {				
				player.setY((int)obj.getY()-51);
			}
			//obj2 != null
			else if (obj2 != null && obj2 != background) {
				player.setY((int)obj2.getY()-51);
			} 
		}
		else {
			player.isInAir = true;
		}
			

	// functionality for wall detection 		
		if (objectPlayerCollision(new GObject[] {program.getElementAt(player.getX()-6, player.getY()),
		    program.getElementAt(player.getX()-6, player.getY()+50)})) {
			
			GObject obj = program.getElementAt(player.getX() - 6, player.getY()+25);
			if (obj != null && obj != background) {		
				player.setX((int)obj.getX()+(int)obj.getWidth());
			}
			isPrevOrientationRight = false;
			player.isOnWall = true;
			return true;
		}
		else if(objectPlayerCollision(new GObject[] {program.getElementAt(player.getX()+playerWidth+6, player.getY()),
				program.getElementAt(player.getX()+playerWidth+6, player.getY() + 50)})) {
			
			GObject obj = program.getElementAt(player.getX()+playerWidth+6, player.getY()+25);
			if (obj != null && obj != background) {				
				player.setX((int)obj.getX()-playerWidth);
			}
			isPrevOrientationRight = true;
			player.isOnWall = true;
			return true;
		}
		else {
			player.isOnWall = false;
			isPrevOrientationRight = null;
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param arr an array of GObjects used to check if they exist. These are our player collision points
	 * @return false if all GObjects are null or player is colliding with a enemy, or collectable. 
	 * True if one or more collision points aren't null and are colliding wih a wall
	 * 
	 */
	private boolean objectPlayerCollision(GObject[] arr) {	
		
		int nullCount = 0;
		for (GObject gImage : arr) {
			if(gImage == background) {nullCount++; continue;}

			if (collectablesMap.containsKey(gImage)) {
				//Checks Map for which collectable is associated to which gImage and switches to perform
				//effects accordingly
				switch(collectablesMap.get(gImage).getCType()) {
					case HEART:
						//Increases player hearts by 1 while hearts < 3 (The max amount of hearts)}
						player.setHearts(player.getHearts()+1);
						heartGLabel.setLabel("Hearts: " + player.getHearts());
						
						break;
					case CHEESE:
						setupWinningScreen();
						break;
					case STAR:
						//Increments total stars by 1;
						starsGlable.setLabel("Stars: " + ++stars);
						
						break;
					case HANDHELD:
						player.weapon = new Weapon(WeaponType.HANDHELD);
						break;
					case MELEE:
						player.weapon = new Weapon(WeaponType.MELEE);
						break;
					default:
						//Should not be called unless collectable has incorrect collectable type
						System.out.println("INVALID COLLECTABLE TYPE");
				}
				collectablesMap.remove(gImage);
				program.remove(gImage);
				return false;
			}
			else if (enemiesMap.containsKey(gImage)) {
				if (!(player.getHitCooldown() > 0)) {				
					player.setHearts(player.getHearts()-1);
					heartGLabel.setLabel("Hearts: " + player.getHearts());
					player.resetHitCooldown();
				}
				return false;
			}
			else if (bulletMap.containsKey(gImage)) {
				return false;
			}
			else if (terrainMap.containsKey(gImage) && terrainMap.get(gImage).getTerrainType() == TerrainType.SPIKE) {
				player.setHearts(0);
			} 
		} 
		if (nullCount == arr.length) {return false;}	
		return true;
}
	
	private boolean checkBulletCollision(GImage key, Bullet val) {
		GObject obj1 = program.getElementAt(key.getX()-2, key.getY());
		GObject obj2 = program.getElementAt(key.getX()-2, key.getY()+key.getHeight());
		GObject obj3 = program.getElementAt(key.getX()+key.getWidth()+2, key.getY());
		GObject obj4 = program.getElementAt(key.getX()+key.getWidth()+2, key.getY()+key.getHeight());
		
		
		if (val.isFriendly() == false && (obj1 == playerImage || obj2 == playerImage || obj3 == playerImage || obj4 == playerImage)) {
			if (!(player.getHitCooldown() > 0)) {				
				player.setHearts(player.getHearts()-1);
				heartGLabel.setLabel("Hearts: " + player.getHearts());
				player.resetHitCooldown();
			}
			return true;
		}

		if (val.isFriendly() && ((enemiesMap.containsKey(obj1)) || enemiesMap.containsKey(obj2)  || enemiesMap.containsKey(obj3)  || enemiesMap.containsKey(obj4))){
			return true;
		}
	
		if(terrainMap.containsKey(obj1) || terrainMap.containsKey(obj2) ||  terrainMap.containsKey(obj3) || terrainMap.containsKey(obj4)) {				
			return true;
		}
		return false;
		
	}
	
	/**
	 * updates bullet location on the GUI
	 */
	private void updateBullet() {
		if (bulletMap.isEmpty()) {return;}
		
		GImage key;
		Bullet val;
		ArrayList<GImage> keysToRemove = new ArrayList<>();
		for (Entry<GImage, Bullet> entry : bulletMap.entrySet()) { 
			key = entry.getKey();
			val = entry.getValue();
			
			key.movePolar(val.getVelocity(), val.getTheta());
			if(checkBulletCollision(key, val)) {keysToRemove.add(key); continue;}
			if (val.hasTimerRunout()) {keysToRemove.add(key);}		
		}
		
		for (GImage gImage : keysToRemove) {
			bulletMap.remove(gImage);
			program.remove(gImage);
		}
		
	}
	
	
	//For now just attacks but could do other stuff?
	private void doEnemyActions() {
		
		for (Entry<GImage, Enemy> entry : enemiesMap.entrySet()) {
			Enemy each = entry.getValue();
			GImage eachImage = entry.getKey();
			if (each.getAwareness() && (each.getEnemyType() == EnemyType.BEATLE || each.getEnemyType() == EnemyType.FLOWER)) {
				if (timerCount - each.getLastShot() >= 150) {
					each.setLastShot(timerCount);
					Bullet[] bullets = each.attack();
					if (bullets != null) {
						for (int i = 0; i < bullets.length; i++) {
							Bullet b = bullets[i];
							GImage bImage = new GImage("/Images/rightBullet.png", b.getX(),b.getY());
							bulletMap.put(bImage,b);
							program.add(bImage);
						}
					} 
				}
			}
			else {
				eachImage.setLocation(each.getX(),each.getY());
				
				if((program.getElementAt(each.getX()-2, each.getY()+52) == background || program.getElementAt(each.getX()-2, each.getY()+52) == null) || terrainMap.containsKey(program.getElementAt(each.getX()-2, each.getY()))) {
					each.setIsRightOrientation(true);
				}
				else if ((program.getElementAt(each.getX()+52, each.getY()+52) == background || program.getElementAt(each.getX()+52, each.getY()+52) == null) || terrainMap.containsKey(program.getElementAt(each.getX()-2, each.getY()))) {
					each.setIsRightOrientation(false);
				}

			}
		}
	}
	
	private void enemyAwareness() {
		int ePointx, ePointy;
		for(Entry<GImage,Enemy> entry : enemiesMap.entrySet()) {

			Enemy all = entry.getValue();
			ePointx = all.getX(); 		
			ePointy = all.getY();
 
			if(all.getEnemyType() != EnemyType.BEATLE && all.getEnemyType() != EnemyType.FLOWER) {continue;}
 
			if (Math.abs(player.getX()-ePointx) <= 400 && Math.abs(player.getY()-ePointy) <= 150) {
				all.switchAwareness(true);
				//System.out.println("sees player");
				break;
			}
			else {
				all.switchAwareness(false);
				//System.out.println("Awareness : False");
			}
		}
	}
	
	private void stopGame() {
		isGamePaused = true;
		player.stopTimer();
		for(Entry<GImage,Enemy> entry : enemiesMap.entrySet()) {
			entry.getValue().stopTimer();
		}
		
		for (Entry<GImage, Bullet> entry : bulletMap.entrySet()) { 
			entry.getValue().stopTimer();
		}

	}
	private void continueGame() {
		isGamePaused = false;
		player.startTimer();
		for(Entry<GImage,Enemy> entry : enemiesMap.entrySet()) {
			entry.getValue().startTimer();
		}
		
		for (Entry<GImage, Bullet> entry : bulletMap.entrySet()) { 
			entry.getValue().startTimer();
		}
	}
	
	public void setupWinningScreen() {
		stopGame();
		
		victoryBorder = new GRect(dimension.getWidth()/2-700/2, dimension.getHeight()/2-400/2, 700, 400);
		victoryBorder.setFillColor(Color.decode("#5f6c5a"));
		victoryBorder.setFilled(true);
		program.add(victoryBorder);
		
		victory = new GParagraph("Victory!" , 0, 0);
		victory.setFont("Ariel-Bold-90");
		victory.setColor(Color.white);
		victory.setLocation(dimension.getWidth()/2-victory.getWidth()/2, victoryBorder.getY()+victory.getHeight()+5);
		program.add(victory);
		
		nextLevelButton = new GButton("Next Level", dimension.getWidth()/2-200-50, victory.getY()+100, 200, 100, Color.decode("#879383"));
		program.add(nextLevelButton);
		
		mainMenuButton = new GButton("Main Menu", dimension.getWidth()/2+50, victory.getY()+100, 200, 100, Color.decode("#879383"));
		program.add(mainMenuButton);		
	}
	
	/**
	 * Sets up the collectables on the main window
	 */
	private void setupGUI() {
		heartGLabel = new GLabel("Hearts: " + player.getHearts() , 50, 50);
		starsGlable = new GLabel("Stars: " + stars, 1400 , 50);
		program.add(heartGLabel);
		program.add(starsGlable);
		
	}
	
	/**
	 * Sets up the collectables on the main window
	 */
	private void setupTerrain() {
		background = new GImage("/Images/forestBackground.jpeg");
		background.setSize(dimension.getWidth(), dimension.getHeight());
		program.add(background);
		
		Terrain terrain = new Terrain(0, 500, 800, 500, TerrainType.GRASS);
		GImage image = new GImage(terrain.getTerrainType().toString(), terrain.getX(), terrain.getY());
		image.setSize((double)terrain.getWidth(), (double)terrain.getHeight());
		program.add(image);
		terrainMap.put(image, terrain);
		
		terrain = new Terrain(900, 700, 800, 200, TerrainType.GRASS);
		image = new GImage(terrain.getTerrainType().toString(), terrain.getX(), terrain.getY());
		image.setSize((double)terrain.getWidth(), (double)terrain.getHeight());
		program.add(image);
		terrainMap.put(image, terrain);
		
		terrain = new Terrain(700, 300, 200, 100, TerrainType.DIRT);
		image = new GImage(terrain.getTerrainType().toString(), terrain.getX(), terrain.getY());
		image.setSize((double)terrain.getWidth(), (double)terrain.getHeight());
		program.add(image);
		terrainMap.put(image, terrain);
	}
	
	// sets up the collectables on the main window
	private void setupCollectables() {
		Collectable collectable = new Collectable(300, 450, CollectableType.HEART);
		GImage image = new GImage(collectable.toString(), collectable.getX(), collectable.getY());
		collectablesMap.put(image, collectable);
		program.add(image);
		
		collectable = new Collectable(400, 450, CollectableType.HANDHELD);
		image = new GImage(collectable.toString(), collectable.getX(), collectable.getY());
		collectablesMap.put(image, collectable);
		program.add(image);
		
		collectable = new Collectable(800, 250, CollectableType.MELEE);
		image = new GImage(collectable.toString(), collectable.getX(), collectable.getY());
		collectablesMap.put(image, collectable);
		program.add(image);
		
		collectable = new Collectable(800, 650, CollectableType.STAR);
		image = new GImage(collectable.toString(), collectable.getX(), collectable.getY());
		collectablesMap.put(image, collectable);
		program.add(image);

		collectable = new Collectable(1500, 600, CollectableType.CHEESE);
		image = new GImage(collectable.toString(), collectable.getX(), collectable.getY());
		collectablesMap.put(image, collectable);
		program.add(image);
	}
	
	private void setupPlayer() {
		player = new Player(200, 300);
		playerImage = new GImage("/Images/rightPlayer.png", 50, 50);
		playerWidth = (int)playerImage.getWidth();
		program.add(playerImage);
		
	}
	
	private void setupEnemies() {
		Enemy tempEnemy = new Enemy (500,450,EnemyType.FLOWER);
		GImage image = new GImage(tempEnemy.getEnemyType().toString(),tempEnemy.getX(),tempEnemy.getY());
		enemiesMap.put(image, tempEnemy);
		program.add(image);
		
		tempEnemy = new Enemy (750,250,EnemyType.WORM);
		image = new GImage(tempEnemy.getEnemyType().toString(),tempEnemy.getX(),tempEnemy.getY());
		program.add(image);
		enemiesMap.put(image, tempEnemy);
			
		tempEnemy = new Enemy (950,650,EnemyType.SPIDER);
		image = new GImage(tempEnemy.getEnemyType().toString(),tempEnemy.getX(),tempEnemy.getY());
		enemiesMap.put(image, tempEnemy);
		program.add(image);
		
		}


}


