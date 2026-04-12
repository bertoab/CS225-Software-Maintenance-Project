import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

// import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.layout.Region;

/**
 * Created by Richard and Ricardo on 5/3/17.
 */
public class BracketPane extends BorderPane {

        /**
         * Reference to the graphical representation of the nodes within the bracket.
         */
        private static ArrayList<BracketNode> nodes;
        /**
         * Used to initiate the paint of the bracket nodes
         */
        //LIOR: commented out this attribute and the one reference to it because it's not doing anything
        // private static boolean isTop = true;
        /**
         * Maps the text "buttons" to it's respective grid-pane
         */
        private HashMap<StackPane, Pane> panes;
        /**
         * Reference to the current bracket.
         */
        private Bracket currentBracket;
        /**
         * Reference to active subtree within current bracket.
         */
        private int displayedSubtree;
        /**
         * Keeps track of whether or not bracket has been finalized.
         */
        private boolean finalized;
        /**
         * Important logical simplification for allowing for code that is easier
         * to maintain.
         */
        private HashMap<BracketNode, Integer> bracketMap = new HashMap<>();
        /**
         * Reverse of the above;
         */
        private HashMap<Integer, BracketNode> nodeMap = new HashMap<>();

        /**
         * Clears the entries of a team future wins
         *
         * @param treeNum
         */
        private void clearAbove(int treeNum) {
                int nextTreeNum = (treeNum - 1) / 2;
                if (!nodeMap.get(nextTreeNum).getName().isEmpty()) {
                        nodeMap.get(nextTreeNum).setName("");
                        clearAbove(nextTreeNum);
                }
        }
        
        
        public void clear(){
            clearSubtree(displayedSubtree);
        }

        /**
         * Handles clicked events for BracketNode objects
         */
        private EventHandler<MouseEvent> clicked = mouseEvent -> {
                //conditional added by matt 5/7 to differentiate between left and right mouse click
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        BracketNode n = (BracketNode) mouseEvent.getSource();
                        int treeNum = bracketMap.get(n);
                        int nextTreeNum = (treeNum - 1) / 2;
                        if (!nodeMap.get(nextTreeNum).getName().equals(n.getName())) {
                                currentBracket.removeAbove((nextTreeNum));
                                clearAbove(treeNum);
                                nodeMap.get((bracketMap.get(n) - 1) / 2).setName(n.getName());
                                currentBracket.moveTeamUp(treeNum);
                        }
                }
                //added by matt 5/7, shows the teams info if you right click
                else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                        String text = "";
                        BracketNode n = (BracketNode) mouseEvent.getSource();
                        int treeNum = bracketMap.get(n);
                        String teamName = currentBracket.getBracket().get(treeNum);
                        try {
                                //LIOR: changed to work with reworked TournamentInfo
                                Team t = TournamentInfo.getTeam(teamName);
                                //by Tyler - added the last two pieces of info to the pop up window
                                text += "Team: " + teamName + " | Ranking: " + t.getRanking() + "\nMascot: " + t.getNickname() + "\nInfo: " + t.getInfo() + "\nAverage Offensive PPG: " + t.getOffensePPG() + "\nAverage Defensive PPG: "+ t.getDefensePPG();
                        } catch (Exception e) {//if for some reason TournamentInfo isnt working, it will display info not found
                                //LIOR: changed IOException to Exception
                                text += "Info for " + teamName + "not found";
                        }
                        //create a popup with the team info
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.CLOSE);
                        alert.setTitle("March Madness Bracket Simulator");
                        alert.setHeaderText(null);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.showAndWait();
                }
        };
        /**
         * Handles mouseEntered events for BracketNode objects
         */
        private EventHandler<MouseEvent> enter = mouseEvent -> {
                BracketNode tmp = (BracketNode) mouseEvent.getSource();

                //New - Joey
                tmp.setStyle("-fx-background-color: #3a3435; -fx-border-color: #fe6229;");
        };

        /**
         * Handles mouseExited events for BracketNode objects
         */
        private EventHandler<MouseEvent> exit = mouseEvent -> {
                BracketNode tmp = (BracketNode) mouseEvent.getSource();
                tmp.setStyle(null);
                tmp.setEffect(null);

        };

        public GridPane getFullPane() {
                return fullPane;
        }

        private GridPane center;
        private GridPane fullPane;

        // DANIELLE: default constructor
        /**
         * Default class constructor.
         */
        public BracketPane() {}

        /**
         * TODO: Reduce. reuse, recycle!
         * Initializes the properties needed to construct a bracket.
         */
        public BracketPane(Bracket currentBracket) {
                displayedSubtree=0;
                this.currentBracket = currentBracket;

                bracketMap = new HashMap<>();
                nodeMap = new HashMap<>();
                panes = new HashMap<>();
                nodes = new ArrayList<>();
                ArrayList<Root> roots = new ArrayList<>();

                center = new GridPane();

                //New - Joey
                String darkBg = "-fx-background-color: #231f20;";
                center.setStyle(darkBg);

                ArrayList<StackPane> buttons = new ArrayList<>();
                buttons.add(customButton("EAST"));
                buttons.add(customButton("WEST"));
                buttons.add(customButton("MIDWEST"));
                buttons.add(customButton("SOUTH"));
                buttons.add(customButton("FULL"));

                //LIOR: commented this out because it's not doing anything
                // ArrayList<GridPane> gridPanes = new ArrayList<>();

                for (int m = 0; m < buttons.size() - 1; m++) {
                        roots.add(new Root(3 + m));
                        panes.put(buttons.get(m), roots.get(m));
                }
                Pane finalPane = createFinalFour();
                //buttons.add(customButton("FINAL"));
                //panes.put(buttons.get(5), finalPane);
                fullPane = new GridPane();
                fullPane.setStyle(darkBg);

                GridPane gp1 = new GridPane();
                gp1.add(roots.get(0), 0, 0);
                gp1.add(roots.get(1), 0, 1);
                gp1.setStyle(darkBg);

                GridPane gp2 = new GridPane();
                gp2.add(roots.get(2), 0, 0);
                gp2.add(roots.get(3), 0, 1);
                gp2.setStyle(darkBg);

                gp2.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

                fullPane.add(gp1, 0, 0);
                fullPane.add(finalPane, 1, 0, 1, 2);
                fullPane.add(gp2, 2, 0);

                fullPane.setAlignment(Pos.CENTER);
                panes.put(buttons.get((buttons.size() - 1)), fullPane);
                finalPane.toBack();

                // Initializes the button grid
                GridPane buttonGrid = new GridPane();
                for (int i = 0; i < buttons.size(); i++)
                        buttonGrid.add(buttons.get(i), 0, i);
                buttonGrid.setAlignment(Pos.CENTER);

                // set default center to the button grid
                this.setCenter(buttonGrid);

                for (StackPane t : buttons) {
                        t.setOnMouseEntered(mouseEvent -> {
                                t.setStyle("-fx-background-color: #3a3435; -fx-border-color: #fe6229;");
                        });
                        t.setOnMouseExited(mouseEvent -> {
                                t.setStyle("-fx-background-color: #2e292a; -fx-border-color: #3a3435; -fx-border-width: 0 0 1 0;");
                        });
                        t.setOnMouseClicked(mouseEvent -> {
                                setCenter(null);
                                /**
                                 * @update Grant & Tyler 
                                 * 			panes are added as ScrollPanes to retain center alignment when moving through full-view and region-view
                                 */
                                center.add(new ScrollPane(panes.get(t)), 0, 0);
                                center.setAlignment(Pos.CENTER);
                                setCenter(center);
                                //Grant 5/7 this is for clearing the tree it kind of works 
                                displayedSubtree=buttons.indexOf(t)==7?0:buttons.indexOf(t)+3;
                        });
                }

        }

        /**
         * Helpful method to retrieve our magical numbers
         *
         * @param root the root node (3,4,5,6)
         * @param pos  the position in the tree (8 (16) , 4 (8) , 2 (4) , 1 (2))
         * @return The list representing the valid values.
         */
        public ArrayList<Integer> helper(int root, int pos) {
                ArrayList<Integer> positions = new ArrayList<>();
                int base = 0;
                int tmp = (root * 2) + 1;
                if (pos == 8) base = 3;
                else if (pos == 4) base = 2;
                else if (pos == 2) base = 1;
                for (int i = 0; i < base; i++) tmp = (tmp * 2) + 1;
                for (int j = 0; j < pos * 2; j++) positions.add(tmp + j);
                return positions; //                while ((tmp = ((location * 2) + 1)) <= 127) ;
        }

        /**
         * Sets the current bracket to,
         *
         * @param target The bracket to replace currentBracket
         */
        public void setBracket(Bracket target) {
                currentBracket = target;
        }

        public int getDisplayedSubtree() {
                return displayedSubtree;
        }

        

        /**
         * Clears the sub tree from,
         *
         * @param position The position to clear after
         */
        public void clearSubtree(int position) {
                currentBracket.resetSubtree(position);
        }

        /**
         * Resets the bracket-display
         */
        public void resetBracket() {
                currentBracket.resetSubtree(0);
        }

        /**
         * Requests a message from current bracket to tell if the bracket
         * has been completed.
         *
         * @return True if completed, false otherwise.
         */
        public boolean isComplete() {
                return currentBracket.isComplete();
        }

        /**
         * @return true if the current-bracket is complete and the value
         * of finalized is also true.
         */
        public boolean isFinalized() {
                return currentBracket.isComplete() && finalized;
        }

        /**
         * @param isFinalized The value to set finalized to.
         */
        public void setFinalized(boolean isFinalized) {
                finalized = isFinalized && currentBracket.isComplete();
        }

        /**
         * Returns a custom "Button" with specified
         *
         * @param name The name of the button
         * @return pane The stack-pane "button"
         */
        private StackPane customButton(String name) {
                StackPane pane = new StackPane();
                Rectangle r = new Rectangle(100, 50, Color.TRANSPARENT);
                Text t = new Text(name);
                t.setTextAlignment(TextAlignment.CENTER);

                //New - Joey
                t.setStyle("-fx-fill: #a9a073; -fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 12px;");
                
                pane.getChildren().addAll(r, t);
                //New - Joey
                pane.setStyle("-fx-background-color: #2e292a; -fx-border-color: #3a3435; -fx-border-width: 0 0 1 0;");
                return pane;
        }

        public Pane createFinalFour() {
                Pane finalPane = new Pane();
                BracketNode nodeFinal0 = new BracketNode("", 162, 300, 70, 0);
                BracketNode nodeFinal1 = new BracketNode("", 75, 400, 70, 0);
                BracketNode nodeFinal2 = new BracketNode("", 250, 400, 70, 0);
                nodeFinal0.setName(currentBracket.getBracket().get(0), currentBracket.getCorrect(0));
                nodeFinal1.setName(currentBracket.getBracket().get(1), currentBracket.getCorrect(1));
                nodeFinal2.setName(currentBracket.getBracket().get(2), currentBracket.getCorrect(2));
                finalPane.getChildren().add(nodeFinal0);
                finalPane.getChildren().add(nodeFinal1);
                finalPane.getChildren().add(nodeFinal2);
                bracketMap.put(nodeFinal1, 1);
                bracketMap.put(nodeFinal2, 2);
                bracketMap.put(nodeFinal0, 0);
                nodeMap.put(1, nodeFinal1);
                nodeMap.put(2, nodeFinal2);
                nodeMap.put(0, nodeFinal0);

                nodeFinal0.setOnMouseClicked(clicked);
                nodeFinal0.setOnMouseDragEntered(enter);
                nodeFinal0.setOnMouseDragExited(exit);

                nodeFinal1.setOnMouseClicked(clicked);
                nodeFinal1.setOnMouseDragEntered(enter);
                nodeFinal1.setOnMouseDragExited(exit);

                nodeFinal2.setOnMouseClicked(clicked);
                nodeFinal2.setOnMouseDragEntered(enter);
                nodeFinal2.setOnMouseDragExited(exit);
                nodeFinal0.setStyle("-fx-border-color: #fe6229; -fx-border-width: 2;");
                nodeFinal1.setStyle("-fx-border-color: #70684e;");
                nodeFinal2.setStyle("-fx-border-color: #70684e;");
                finalPane.setMinWidth(400.0);

                //New - Joey
                finalPane.setStyle("-fx-background-color: #231f20;");

                return finalPane;
        }

        /**
         * Creates the graphical representation of a subtree.
         * Note, this is a vague model. TODO: MAKE MODULAR
         */
        private class Root extends Pane {

                private int location;

                public Root(int location) {
                        this.location = location;

                        //New - Joey
                        this.setStyle("-fx-background-color: #231f20;");

//                         createVertices(420, 200, 100, 20, 0, 0);
//                         createVertices(320, 119, 100, 200, 1, 0);
//                         createVertices(220, 60, 100, 100, 2, 200);
//                         createVertices(120, 35, 100, 50, 4, 100);
//                         createVertices(20, 25, 100, 25, 8, 50);
                        //Matthew Tummino
                        //Adjusted values to better fit school names and box score
                        createVertices(700, 200, 170, 20, 0, 0);
                        createVertices(530, 119, 170, 200, 1, 0);
                        createVertices(360, 60, 170, 100, 2, 200);
                        createVertices(190, 35, 170, 50, 4, 100);
                        createVertices(20, 25, 170, 25, 8, 50);
                        for (BracketNode n : nodes) {
                                n.setOnMouseClicked(clicked);
                                n.setOnMouseEntered(enter);
                                n.setOnMouseExited(exit);
                        }
                }

                /**
                 * The secret sauce... well not really,
                 * Creates 3 lines in appropriate location unless it is the last line.
                 * Adds these lines and "BracketNodes" to the Pane of this inner class
                 */
                private void createVertices(int iX, int iY, int iXO, int iYO, int num, int increment) {
                        int y = iY;
                        if (num == 0 && increment == 0) {
                                BracketNode last = new BracketNode("", iX, y - 20, iXO, 20);
                                nodes.add(last);
                                getChildren().addAll(new Line(iX, iY, iX + iXO, iY), last);
                                last.setName(currentBracket.getBracket().get(location), currentBracket.getCorrect(location));
                                bracketMap.put(last, location);
                                nodeMap.put(location, last);
                        } else {
                                ArrayList<BracketNode> aNodeList = new ArrayList<>();
                                for (int i = 0; i < num; i++) {
                                        Point2D tl = new Point2D(iX, y);
                                        Point2D tr = new Point2D(iX + iXO, y);
                                        Point2D bl = new Point2D(iX, y + iYO);
                                        Point2D br = new Point2D(iX + iXO, y + iYO);
                                        BracketNode nTop = new BracketNode("", iX, y - 20, iXO, 20);
                                        aNodeList.add(nTop);
                                        nodes.add(nTop);
                                        BracketNode nBottom = new BracketNode("", iX, y + (iYO - 20), iXO, 20);
                                        aNodeList.add(nBottom);
                                        nodes.add(nBottom);
                                        Line top = new Line(tl.getX(), tl.getY(), tr.getX(), tr.getY());
                                        Line bottom = new Line(bl.getX(), bl.getY(), br.getX(), br.getY());
                                        Line right = new Line(tr.getX(), tr.getY(), br.getX(), br.getY());

                                        //New - Joey
                                        String lineStyle = "-fx-stroke: #a9a073; -fx-stroke-width: 1;";
                                        top.setStyle(lineStyle);
                                        bottom.setStyle(lineStyle);
                                        right.setStyle(lineStyle);
                                        
                                        getChildren().addAll(top, bottom, right, nTop, nBottom);
                                        //LIOR: commented out because this attribute doesn't do anything
                                        // isTop = !isTop;
                                        y += increment;
                                }
                                ArrayList<Integer> tmpHelp = helper(location, num);
                                for (int j = 0; j < aNodeList.size(); j++) {
                                        //System.out.println(currentBracket.getBracket().get(tmpHelp.get(j)));
                                        if(tmpHelp.get(j) < 63 && tmpHelp.get(j) >= 0)
                                                aNodeList.get(j).setName(currentBracket.getBracket().get(tmpHelp.get(j)),currentBracket.getCorrect(tmpHelp.get(j)));
                                        else
                                                aNodeList.get(j).setName(currentBracket.getBracket().get(tmpHelp.get(j)));
                                        bracketMap.put(aNodeList.get(j), tmpHelp.get(j));
                                        nodeMap.put(tmpHelp.get(j), aNodeList.get(j));
                                        //System.out.println(bracketMap.get(aNodeList.get(j)));
                                }
                        }

                }
        }

        /**
         * The BracketNode model for the Graphical display of the "Bracket"
         */
        private class BracketNode extends Pane {
                private String teamName;
                private Rectangle rect;
                private Label name;

                /**
                 * Creates a BracketNode with,
                 *
                 * @param teamName The name if any
                 * @param x        The starting x location
                 * @param y        The starting y location
                 * @param rX       The width of the rectangle to fill pane
                 * @param rY       The height of the rectangle
                 */
                public BracketNode(String teamName, int x, int y, int rX, int rY) {
                        this.setLayoutX(x);
                        this.setLayoutY(y);
                        this.setMaxSize(rX, rY);
                        this.teamName = teamName;
                        rect = new Rectangle(rX, rY);
                        rect.setFill(Color.TRANSPARENT);
                        name = new Label(teamName);

                        //New - Joey
                        name.setStyle("-fx-text-fill: #d4e8d9; -fx-font-family: Arial; -fx-font-size: 12px;");
                        // setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                        name.setTranslateX(5);
                        getChildren().addAll(name, rect);
                }

                /**
                 * @return teamName The teams name.
                 */
                public String getName() {
                        return teamName;
                }

                //adjusted by Matthew Tummino to account for displaying results
                /**
                 * @param teamName The name to assign to the node.
                 * @param correct whether or not game prediction was correct
                 */
                public void setName(String teamName, boolean correct) {
                        this.teamName = teamName;
                        name.setText(teamName);
                        
                        if(correct)
                                name.setStyle("-fx-text-fill: #38d72a;");
                }
                

                /**
                 * @param teamName The name to assign to the node.
                 */
                public void setName(String teamName)
                {
                        setName(teamName,false);
                }
        }
}
