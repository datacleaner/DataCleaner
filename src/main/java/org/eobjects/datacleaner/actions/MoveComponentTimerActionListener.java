package org.eobjects.datacleaner.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;

public class MoveComponentTimerActionListener implements ActionListener {

	private final JComponent component;
	private final int x;
	private final int y;
	private volatile int numSteps;

	public MoveComponentTimerActionListener(JComponent component, int x, int y, int numSteps) {
		if (numSteps <= 0) {
			throw new IllegalArgumentException("numSteps must be a postive number");
		}
		this.component = component;
		this.x = x;
		this.y = y;
		this.numSteps = numSteps;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Timer timer = (Timer) e.getSource();
		
		Point location = component.getLocation();
		int currentX = location.x;
		int currentY = location.y;
		if (currentX == x && currentY == y) {
			timer.stop();
		} else if (numSteps == 1) {
			component.setLocation(x, y);
		} else {
			int diffX = x-currentX;
			diffX = diffX / numSteps;
			int diffY = y-currentY;
			diffY = diffY / numSteps;
			
			component.setLocation(currentX + diffX, currentY + diffY);
		}
		component.updateUI();
		numSteps--;
	}
}
