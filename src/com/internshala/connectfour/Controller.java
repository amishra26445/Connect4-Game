package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable{

	private static final int COLOUMN=7;
	private static final int ROW=6;
	private static final int CIRCLE_DIAMETER=80;
	private static final String diskColor1="#24303E";
	private static final String diskColor2="#4CAA88";

	private static String PLAYER_ONE="Player One";
	private static String PLAYER_TWO="Player Two";

	private boolean isPlayerOneTurn=true;

	private Disc[][] insertedDiscArray=new Disc[ROW][COLOUMN];

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDisc;

	@FXML
	public Label playerNameLabel;

	@FXML
	public TextField PlayerOneTextFeild;

	@FXML
	public TextField PlayerTwo;

	@FXML
	public Button SetNamesButton;



	private boolean isAllowedToInsert=true;

	public void createPlayground(){
		Shape rectangleWithHoles=createGameStucturalGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList=createClickableColoumn();

		for (Rectangle rectangle:
		     rectangleList) {
			rootGridPane.add(rectangle,0,1);
		}




	}
	private Shape createGameStucturalGrid(){
		Shape rectangleWithHoles= new Rectangle((COLOUMN + 1) * CIRCLE_DIAMETER,(ROW +1) * CIRCLE_DIAMETER);
		for(int row=0;row<ROW;row++){
			for(int col=0;col<COLOUMN;col++){
				Circle circle=new Circle();

				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);
				circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
				circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);

				rectangleWithHoles=Shape.subtract(rectangleWithHoles,circle);

			}
		}


		rectangleWithHoles.setFill(Color.WHITE);

		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColoumn(){

		List<Rectangle> rectangleList=new ArrayList<>();


		for(int col=0;col<COLOUMN;col++){
			Rectangle rectangle=new Rectangle(CIRCLE_DIAMETER,(ROW +1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int coloumn=col;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToInsert) {
					isAllowedToInsert=false;
					insertDisc(new Disc(isPlayerOneTurn), coloumn);
				}});


			rectangleList.add(rectangle);

		}

		return rectangleList;
	}
	private void insertDisc(Disc disc,int coloumn){
		
		int row=ROW-1;
		while(row>=0){
			if(getDiscIfPresent(row,coloumn)==null)
				break;
			row--;
		}
		if(row<0)
			return;

		insertedDiscArray[row][coloumn]=disc;
		insertedDisc.getChildren().add(disc);
		disc.setTranslateX(coloumn * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
		TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
		int currentRow=row;
		translateTransition.setOnFinished(event -> {
			isAllowedToInsert=true;
			if(gameEnded(currentRow,coloumn)){
				gameOver();
				return;
			}

			isPlayerOneTurn=!isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn?PLAYER_ONE:PLAYER_TWO);
		});



		translateTransition.play();




	}
	private boolean gameEnded(int row,int coloumn)  {

		List<Point2D> verticalPoints= IntStream.rangeClosed(row - 3,row + 3)
										.mapToObj(r-> new Point2D(r,coloumn))
										.collect(Collectors.toList());

		List<Point2D> horizontalPoints= IntStream.rangeClosed(coloumn - 3,coloumn + 3)
				.mapToObj(col-> new Point2D(row,col))
				.collect(Collectors.toList());

		Point2D starPoint1=new Point2D(row -3,coloumn +3);
		List<Point2D> diagonal1Points=IntStream.rangeClosed(0,6)
				.mapToObj(i-> starPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D starPoint2=new Point2D(row -3,coloumn -3);
		List<Point2D> diagonal2Points=IntStream.rangeClosed(0,6)
				.mapToObj(i-> starPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded=checkCombinations(verticalPoints)|| checkCombinations(horizontalPoints)
				|| checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);




		return isEnded;
	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain=0;

		for (Point2D point:points) {

			int rowIndexOfArray= (int) point.getX();
			int coloumnIndexOfArray= (int) point.getY();

			Disc disc=getDiscIfPresent(rowIndexOfArray,coloumnIndexOfArray);

			if(disc!=null && disc.isPlayerOneMove==isPlayerOneTurn) {
				chain++;
				if (chain == 4) {
					return true;
				}
			}else{
					chain=0;
				}


		}
		return false;
	}
	private Disc getDiscIfPresent(int row,int coloumn){
		if(row>=ROW || row<0 || coloumn>=COLOUMN || coloumn<0)
			return null;

		return insertedDiscArray[row][coloumn];
	}



	private void gameOver(){
		String winner=isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
		System.out.println("Winner is : " + winner);

		Alert alert=new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is : "+ winner);
		alert.setContentText("Want to play again ?");

		ButtonType yesBtn=new ButtonType("Yes");
		ButtonType noBtn=new ButtonType("No, Exit");

		alert.getButtonTypes().setAll(yesBtn,noBtn);

		Platform.runLater(() ->{
			Optional<ButtonType> btnClicked=alert.showAndWait();
			if(btnClicked.isPresent() && btnClicked.get()==yesBtn){
				resetGame();
			}
			else{
				Platform.exit();
				System.exit(0);
			}
		});



	}

	public void resetGame() {
		insertedDisc.getChildren().clear();

		for(int row=0;row<insertedDiscArray.length;row++){
			for (int col=0;col<insertedDiscArray[row].length;col++){
				insertedDiscArray[row][col]=null;
			}
		}
		isPlayerOneTurn=true;
		playerNameLabel.setText(PLAYER_ONE);

		createPlayground();
	}

	private static class Disc extends Circle{
		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove){

				this.isPlayerOneMove=isPlayerOneMove;
				setRadius(CIRCLE_DIAMETER/2);
				setFill(isPlayerOneMove?Color.valueOf(diskColor1):Color.valueOf(diskColor2));
				setCenterX(CIRCLE_DIAMETER/2);
				setCenterY(CIRCLE_DIAMETER/2);

		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

			SetNamesButton.setOnAction(event -> {

				String input1 = PlayerOneTextFeild.getText();

				String input2=PlayerTwo.getText();

				PLAYER_ONE = input1 + "`s";
				PLAYER_TWO = input2 + "`s";

				if (input1.isEmpty())
					PLAYER_ONE = "Player One`s";

				if (input2.isEmpty())
					PLAYER_TWO = "Player Two`s";

				// isPlayerOneTurn = !isPlayerOneTurn;
				playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);

			});



	}
}
