package com.gmail.bluballsman.themaze.mazegen;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

public class SymmetricMaze extends Maze {

	public SymmetricMaze(int halfTemplateLength, int halfTemplateWidth, long randomSeed) {
		super(halfTemplateLength, (halfTemplateWidth * 2) - 3, randomSeed);
	}

	public SymmetricMaze(int halfTemplateLength, int halfTemplateWidth) {
		this(halfTemplateLength, halfTemplateWidth, new Random().nextLong());
	}

	public Point getMirroredPoint(int x, int y) {
		return new Point(length - 1 - x, width - 1 - y);
	}

	public Point getMirroredPoint(Point p) {
		return getMirroredPoint(p.x, p.y);
	}
	
	public ArrayList<Point> getUnmirroredMatchingPoints(Predicate<Point> condition){
		ArrayList<Point> matching = new ArrayList<Point>();
		Point center = getCenterPoint();

		for(int x = 1; x < length - 1; x++) {
			for(int y = 1; y < center.y; y++) {
				Point p = new Point(x, y);
				
				if(condition.test(p)) {
					matching.add(p);
				}
			}
		}

		for(int x = 1; x < center.x + 1; x++) {
			Point p = new Point(x, center.y);
			
			if(condition.test(p)) {
				matching.add(p);
			}		
		}

		return matching;
	}
	
	@Override
	public ArrayList<Point> getMatchingPoints(Predicate<Point> condition){
		ArrayList<Point> matching = new ArrayList<Point>();
		Point center = getCenterPoint();

		for(int x = 1; x < length - 1; x++) {
			for(int y = 1; y < center.y; y++) {
				Point p = new Point(x, y);
				
				if(condition.test(p)) {
					matching.add(p);
					matching.add(getMirroredPoint(p));
				}
			}
		}

		for(int x = 1; x < center.x + 1; x++) {
			Point p = new Point(x, center.y);
			
			if(condition.test(p)) {
				matching.add(p);
				matching.add(getMirroredPoint(p));
			}		
		}

		return matching;
	}
	
	@Override
	public void fill() {
		Point center = getCenterPoint();
		int halfTemplateWidth = (width + 3) / 2;
		Maze halfTemplate = new Maze(length, halfTemplateWidth, randomSeed);
		halfTemplate.fill();
		for(int y = 0; y < center.y; y++) {
			for(int x = 0; x < halfTemplate.length; x++) {
				Tile t = halfTemplate.getTile(x, y);
				setTileMirrored(x, y, t.isGround());
			}
		}

		for(int x = 0; x < center.x + 1; x++) {
			Tile t = halfTemplate.getTile(x, center.y);
			Tile mirrored = halfTemplate.getTile(length - 1 - x, center.y);
			setTileMirrored(x, center.y, (t.isGround() && mirrored.isGround()));
		}
	}

	@Override
	public void knockDownRandomWalls(float percentageOfWalls) {
		ArrayList<Point> availableWalls = new ArrayList<Point>();
		Point center = getCenterPoint();

		for(int y = 1; y < center.y; y++) {
			for(int x = 1 + y % 2; x < length - 1; x+=2) {
				Point p = new Point(x, y);
				if(!getTile(p).isGround()) {
					availableWalls.add(p);
				}
			}
		}

		for(int x = 1 + center.y % 2; x < center.x + 1; x+=2) {
			Point p = new Point(x, center.y);
			if(!getTile(p).isGround()) {
				availableWalls.add(p);
			}
		}

		int wallsToTearDown = Math.round(percentageOfWalls * (float) availableWalls.size());
		Random random = new Random(randomSeed);

		for(int i = 0; i < wallsToTearDown; i++) {
			Point randomPoint = availableWalls.get(random.nextInt(availableWalls.size()));
			
			setTileMirrored(randomPoint, true);
			availableWalls.remove(randomPoint);
		}
	}
	
	@Override
	public void fixStandaloneWalls() {
		Point centerPoint = getCenterPoint();
		Random random = new Random(randomSeed);
		
		for(int y = 2; y < centerPoint.y; y+=2) {
			for(int x = 2; x < length - 1; x+=2) {
				if(getTile(x, y).isGround()) {
					continue;
				}
				ArrayList<Point> adjacentGroundTiles = getValidNeighbors(x, y, 1, p -> getTile(p).isGround());
				if(adjacentGroundTiles.size() == 4) {
					Point randomAdjacent = adjacentGroundTiles.get(random.nextInt(4));
					setTileMirrored(randomAdjacent, false);
				}
				
			}
		}
	}
	
	public void validateCenterAccessibility() {
		Point center = getCenterPoint();

		for(int x = 1; x < center.x + 1; x+=2) {
			Point test = new Point(x, center.y);
			
			if(!getTile(test).isGround()) {
				continue;
			}
			
			boolean solved = solveBreadthFirst(test, new Point(length - 2, 1)) != null;
			if(!solved) {
				setTileMirrored(test.x - 1, center.y, true);
			}
		}
	}

	public void openUpCenter(int centerSize) {
		Point center = getCenterPoint();

		for(int yOffset = -centerSize; yOffset <= centerSize; yOffset++) {
			for(int xOffset = -centerSize; xOffset <= centerSize; xOffset++) {
				Point p = new Point(center.x + xOffset, center.y + yOffset);
				setTile(p, true);
			}
		}
	}

	public void setTileMirrored(int x, int y, boolean isGround, boolean update) {
		Point mirror = getMirroredPoint(x, y);
		
		super.setTile(x, y, isGround, update);
		super.setTile(mirror.x, mirror.y, isGround, update);
	}

	public void setTileMirrored(Point p, boolean isGround, boolean update) {
		setTile(p, isGround, update);
	}

	public void setTileMirrored(int x, int y, boolean isGround) {
		setTileMirrored(x, y, isGround, false);
	}

	public void setTileMirrored(Point p, boolean isGround) {
		setTileMirrored(p.x, p.y, isGround, false);
	}

}
