/*
 * Copyright 2013 Timm Hirsens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.br0tbox.gitfx.ui.history;

import de.br0tbox.gitfx.ui.history.JavaFxCommitList.JavaFxLane;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revplot.AbstractPlotRenderer;
import org.eclipse.jgit.revplot.PlotCommit;

public class JavaFxPlotRenderer extends AbstractPlotRenderer<JavaFxLane, Color> {

	private Group currentShape;

	public Group draw(PlotCommit<JavaFxLane> commit, double height) {
		currentShape = new Group();
		paintCommit(commit, (int) height);
		currentShape.layout();
		return currentShape;
	}

	@Override
	protected int drawLabel(int x, int y, Ref ref) {
		String refName = ref.getName();
		if (refName.contains(Constants.R_HEADS)) {
			refName = refName.substring(Constants.R_HEADS.length(), refName.length());
		}
		if (refName.contains(Constants.R_REMOTES)) {
			refName = refName.substring(Constants.R_REMOTES.length(), refName.length());
		}
		if (refName.contains(Constants.R_TAGS)) {
			refName = refName.substring(Constants.R_TAGS.length(), refName.length());
		}
		final Text text = new Text(refName);
		text.setX(x);
		text.setY(y * 1.5);
		text.setFill(Color.RED);
		final double fontSize = text.getFont().getSize();
		final int width = (int) Math.floor(fontSize * refName.trim().length() / 2 + 2);
		//final Rectangle rectangle = RectangleBuilder.create().x(x).y(y /
		//2).width(width).height(fontSize + 3).fill(Color.GRAY).build();
		//currentShape.getChildren().add(rectangle);
		currentShape.getChildren().add(text);
		return (int) Math.floor(10 + width);
	}

	@Override
	protected Color laneColor(JavaFxLane myLane) {
		if (myLane != null) {
			return myLane.color;
		}
		return Color.GRAY;
	}

	@Override
	protected void drawLine(Color color, int x1, int y1, int x2, int y2, int width) {
		final Line path = new Line();
		path.setStartX(x1);
		path.setStartY(y1);
		path.setEndX(x2);
		path.setEndY(y2);
		path.setStrokeWidth(width);
		path.setStroke(color);
		// XXX: Without this circle, all the lines will be off.
		final Circle placeHolder = new Circle();
		currentShape.getChildren().add(placeHolder);
		currentShape.getChildren().add(path);
	}

	@Override
	protected void drawCommitDot(int x, int y, int w, int h) {
		final Circle circle = new Circle();
		circle.setCenterX(Math.floor(x + w / 2) + 1);
		circle.setCenterY(Math.floor(y + h / 2));
		circle.setRadius(Math.floor(w / 2));
		circle.setFill(Color.DARKGRAY);
		final Circle innerCircle = new Circle();
		innerCircle.setCenterX(Math.floor(x + w / 2 + 1));
		innerCircle.setCenterY(Math.floor(y + h / 2));
		innerCircle.setRadius(Math.floor(w / 2 - 2));
		innerCircle.setFill(Color.WHITE);
		currentShape.getChildren().add(circle);
		currentShape.getChildren().add(innerCircle);
	}

	@Override
	protected void drawBoundaryDot(int x, int y, int w, int h) {
		final Circle circle = new Circle();
		circle.setCenterX(x + w / 2);
		circle.setCenterY(y + h / 2);
		circle.setRadius(h / 2);
		circle.setFill(Color.GRAY);
		final Circle innerCircle = new Circle();
		innerCircle.setCenterX(Math.floor(x + w / 2 + 1));
		innerCircle.setCenterY(Math.floor(y + h / 2));
		innerCircle.setRadius(Math.floor(w / 2 - 2));
		innerCircle.setFill(Color.LIGHTGRAY);
		currentShape.getChildren().add(circle);
		currentShape.getChildren().add(innerCircle);
	}

	@Override
	protected void drawText(String msg, int x, int y) {
		final Text text = new Text(msg);
		text.setX(x);
		text.setY(y * 1.5);
		currentShape.getChildren().add(text);
	}

	public Group getCurrentShape() {
		return currentShape;
	}
}
