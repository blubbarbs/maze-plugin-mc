package com.gmail.bluballsman.themaze.mazegen;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Maze {
	protected Tile[][] tiles;
	protected int length;
	protected int width;
	protected long randomSeed;
	
	public Maze(int length, int width, long randomSeed) {
		length = length % 2 == 1 ? length : length + 1;
		width = width % 2 == 1 ? width : width + 1;
		tiles = new Tile[length][width];
		this.length = length;
		this.width = width;
		this.randomSeed = randomSeed;
		
		for(int y = 0; y < width; y++) {
			for(int x = 0; x < length; x++) {
				tiles[x][y] = new Tile();
			}
		}
	}

	public Maze(int length, int width) {
		this(length, width, new Random().nextLong());
	}
	
	public Tile getTile(int x, int y) {
		return tiles[x][y];
	}
	
	public Tile getTile(Point p) {
		return getTile(p.x, p.y);
	}
	
	public int getLength() {
		return length;
	}

	public int getWidth() {
		return width;
	}
	
	public long getSeed() {
		return randomSeed;
	}
	
	public Point getCenterPoint() {
		return new Point(((length + 1)/2) - 1, ((width + 1)/2) - 1);
	}
	
	public boolean isPointValid(int x, int y) {
		return x >= 0 && y >= 0 && x < length && y < width;
	}
	
	public boolean isPointValid(Point p) {
		return isPointValid(p.x, p.y);
	}
	
	public ArrayList<Point> getValidNeighbors(int x, int y, int reach, Predicate<Point> matches) {
		ArrayList<Point> validNeighbors = new ArrayList<Point>();
		
		for(int i = 0; i < 4; i++) {
			double radians = (Math.PI * i) / 2;
			int xAddend = (int) (reach * Math.sin(radians));
			int yAddend = (int) (reach * Math.cos(radians));
			Point neighbor = new Point(x + xAddend, y + yAddend);
			if(isPointValid(neighbor) && (matches == null || matches.test(neighbor))) {
				validNeighbors.add(neighbor);
			}
		}
		
		return validNeighbors;
	}
	
	public ArrayList<Point> getValidNeighbors(Point p, int reach, Predicate<Point> matches) {
		return getValidNeighbors(p.x, p.y, reach, matches);
	}
	
	public int getNumberOfWallsAvailableForKnockdown() {
		int wallRows = Math.floorDiv(width - 2, 2);
		int numberOfWalls = (wallRows * (length - 2)) - wallRows;

		return numberOfWalls/2;
	}
	
	public ArrayList<Point> getMatchingPoints(Predicate<Point> condition) {
		ArrayList<Point> matching = new ArrayList<Point>();
		
		for(int x = 1; x < length - 1; x++) {
			for(int y = 1; y < width - 1; y++) {
				Point p = new Point(x, y);
				if(condition.test(p)) {
					matching.add(p);
				}
			}
		}
		
		return matching;
	}
	
	public Stack<Point> solveBreadthFirst(Point startingPoint, Point endingPoint) {
		if(!isPointValid(startingPoint) || !isPointValid(endingPoint) || 
				!getTile(startingPoint).isGround() || !getTile(endingPoint).isGround()) {
			return null;
		}
		
		HashSet<Point> visitedPoints= new HashSet<Point>();
		Stack<Point> path = new Stack<Point>();
		Point currentPoint = startingPoint;
		visitedPoints.add(startingPoint);
		path.push(startingPoint);
		
		while(!path.isEmpty()) {
			currentPoint = path.peek();
			
			if(currentPoint.equals(endingPoint)) {
				return path;
			}
			
			Predicate<Point> neighborCondition = p -> !visitedPoints.contains(p) && getTile(p).isGround();
			ArrayList<Point> availableNeighbors = getValidNeighbors(currentPoint, 1, neighborCondition);
			
			if(!availableNeighbors.isEmpty()) {
				Point firstFound = availableNeighbors.get(0);
				path.push(firstFound);
				visitedPoints.add(firstFound);
			} else {
				path.pop();
			}
		}
		
		return null;
	}
	
	public void setTile(int x, int y, boolean isGround, boolean updateSurrounding) {
		getTile(x, y).setGround(isGround);
		if(updateSurrounding && getTile(x, y).isGround() != isGround) {
			getValidNeighbors(x, y, 1, null).forEach(p -> updateTile(p));
		}
	}
	
	public void setTile(Point p, boolean isGround, boolean updateSurrounding) {
		setTile(p.x, p.y, isGround, updateSurrounding);
	}
	
	public void setTile(int x, int y, boolean isGround) {
		setTile(x, y, isGround, false);
	}
	
	public void setTile(Point p, boolean isGround) {
		setTile(p, isGround, false);
	}
	
	public void updateTile(int x, int y) {
		if(!isPointValid(x, y)) {
			return;
		}
		
		int surroundingTileBits = 0;
		
		for(int i = 0; i < 4; i++) {
			double radians = (Math.PI * i) / 2;
			int xAddend = (int) Math.sin(radians);
			int yAddend = (int) Math.cos(radians);
			Point neighbor = new Point(x + xAddend, y + yAddend);
			boolean countsAsGround = (!isPointValid(neighbor) || getTile(neighbor).isGround());
			surroundingTileBits += countsAsGround ? (int) (Math.pow(2, 3 - i)) : 0;
		}
		
		getTile(x, y).setSurroundingTiles(surroundingTileBits);
	}
	
	public void updateTile(Point p) {
		updateTile(p.x, p.y);
	}
	
	public void updateAllTiles() {
		for(int y = 0; y < width; y++) {
			for(int x = 0; x < length; x++) {
				updateTile(x, y);
			}
		}
	}
	
	public void fill() {
		Point startingPoint = new Point(1, 1);
		Point currentPoint = startingPoint;
		Stack<Point> currentPath = new Stack<Point>();
		Random random = new Random(randomSeed);
		currentPath.add(startingPoint);
		setTile(startingPoint, true);

		while(!currentPath.isEmpty()) {
			ArrayList<Point> validNeighbors = getValidNeighbors(currentPoint, 2, (p -> !getTile(p).isGround()));
			
			if(validNeighbors.isEmpty()) {
				currentPoint = currentPath.pop();
				continue;
			}
			
			Point nextPoint = validNeighbors.get(random.nextInt(validNeighbors.size()));
			int xAddend = (nextPoint.x - currentPoint.x)/2;
			int yAddend = (nextPoint.y - currentPoint.y)/2;
			Point wallInBetween = new Point(currentPoint.x + xAddend, currentPoint.y + yAddend);
			setTile(wallInBetween, true);
			setTile(nextPoint, true);
			currentPoint = currentPath.push(nextPoint);
		}
	}

	public void rotate(int ccwRotations) {
		ccwRotations = ccwRotations < 0 ? (4 + ccwRotations) % 4 : ccwRotations % 4;

		if(ccwRotations == 0) {
			return;
		}

		Tile[][] rotatedTiles;
		rotatedTiles = ccwRotations % 2 == 0 ? new Tile[length][width] : new Tile[width][length];

		for(int y = 0; y < width; y++) {
			for(int x = 0; x < length; x++) {
				getTile(x, y).rotate(ccwRotations);
				if(ccwRotations == 1) {
					rotatedTiles[width - y - 1][x] = tiles[x][y];
					int oldLength = length;
					length = width;
					width = oldLength;
				} else if(ccwRotations == 2) {
					rotatedTiles[length - x - 1][width - y - 1] = tiles[x][y];
				} else {
					rotatedTiles[y][length - x - 1] = tiles[x][y];
					int oldLength = length;
					length = width;
					width = oldLength;
				}
			}
		}
		tiles = rotatedTiles;
	}

	public void knockDownRandomWalls(float percentageOfWalls) {
		ArrayList<Point> availableWalls = new ArrayList<Point>();
		
		for(int y = 1; y < width - 1; y++) {
			for(int x = 1 + y % 2; x < length - 1; x+=2) {
				Point p = new Point(x, y);
				if(getTile(p).isGround()) {
					continue;
				}
				availableWalls.add(new Point(x, y));
			}
		}
		
		int wallsToTearDown = Math.round(percentageOfWalls * (float) availableWalls.size());
		Random random = new Random(randomSeed);
		
		for(int i = 0; i < wallsToTearDown; i++) {
			Point randomPoint = availableWalls.get(random.nextInt(availableWalls.size()));
			setTile(randomPoint, true);
			availableWalls.remove(randomPoint);
		}
	}
		
	public void fixStandaloneWalls() {
		Random random = new Random(randomSeed);
		
		for(int y = 2; y < width - 1; y+=2) {
			for(int x = 2; x < length - 1; x+=2) {
				if(getTile(x, y).isGround()) {
					continue;
				}
				ArrayList<Point> adjacentGroundTiles = getValidNeighbors(x, y, 1, p -> getTile(p).isGround());
				if(adjacentGroundTiles.size() == 4) {
					Point randomAdjacent = adjacentGroundTiles.get(random.nextInt(4));
					setTile(randomAdjacent, false);
				}
				
			}
		}
	}
	
	public void applyToRange(int startingX, int startingY, int endingX, int endingY, Consumer<Point> func) {
		startingX = Math.min(startingX, endingX);
		endingX = Math.max(startingX, endingX);
		startingY = Math.min(startingY, endingY);
		endingY = Math.max(startingY, endingY);
		
		if(startingX < 0 || startingY < 0 || endingX >= length || endingY >= width) {
			return;
		}
		
		for(int y = startingY; y <= endingY; y++) {
			for(int x = startingX; x <= endingX; x++) {
				func.accept(new Point(x, y));
			}
		}
	}
	
	public void applyToRange(Point start, Point end, Consumer<Point> func) {
		applyToRange(start.x, start.y, end.x, end.y, func);
	}
	
	@Override
	public Maze clone() {
		Maze clone = new Maze(length, width, randomSeed);
		for(int y = 0; y < width; y++) {
			for(int x = 0; x < length; x++) {
				Tile original = getTile(x, y);
				clone.getTile(x, y).setTileData(original.getTileData());
			}
		}
		return clone;
	}
}
