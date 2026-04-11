import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import com.google.gson.*;

public class RealResultsPane extends BorderPane {

    private static final String[] ROUND_NAMES = {"Round of 64", "Round of 32",
                                                "Sweet 16", "Elite 8", "Final Four",
                                                "Championship"
    };

    //We want to maintain the order of our results
    private final Map<String, List<GameResult>> resultsByRound = new LinkedHashMap<>();
    
    private StackPane activeButton = null;

    public RealResultsPane() {
        buildResultsData();
        buildUI();
    }

    private void buildUI() {
        Label title = new Label("2017 MARCH MADNESS - REAL LIFE RESULTS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#eeab20"));
        title.setPadding(new Insets(12));

        HBox header = new HBox(title);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #2e292a; -fx-border-color: #fe6229; -fx-border-width 0 0 2 0;");
        setTop(header);

        VBox roundSelector = new VBox(6);
        roundSelector.setPadding(new Insets(16, 8, 16, 8));
        roundSelector.setStyle("-fx-background-color: #2e292a");
        roundSelector.setAlignment(Pos.TOP_CENTER);
        roundSelector.setMinWidth(150);

        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background-color: #231f20; -fx-background: #231f20;");
        setCenter(contentScroll);

        for(String roundName : ROUND_NAMES) {
            StackPane btn = makeRoundButton(roundName);
            roundSelector.getChildren().add(btn);

            btn.setOnMouseClicked(e -> {

                //Deselect the previous round
                if(activeButton != null) {
                    activeButton.setStyle("-fx-background-color: transparent; -fx-border-color: #3a3435; -fx-border-width: 0 0 1 0;");
                    activeButton.setEffect(null);
                }

                btn.setStyle("-fx-background-color: #fe6229;");
                activeButton = btn;

                contentScroll.setContent(buildRoundView(roundName));
            });

        }

        setLeft(roundSelector);

        StackPane first = (StackPane) roundSelector.getChildren().get(0);
        first.setStyle("-fx-background-color: #fe6229;");
        activeButton = first;
        contentScroll.setContent(buildRoundView(ROUND_NAMES[0]));
    }

    private StackPane makeRoundButton(String label) {

        Rectangle bg = new Rectangle(130, 44, Color.TRANSPARENT);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#a9a073"));
        lbl.setTextAlignment(TextAlignment.CENTER);
        lbl.setWrapText(true);
        lbl.setMaxWidth(120);

        StackPane btn = new StackPane(bg, lbl);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: #3a3435; -fx-border-width: 0 0 1 0;");
        btn.setMaxWidth(Double.MAX_VALUE);

        return btn;
    }

    private VBox buildRoundView(String roundName) {
        List<GameResult> games = resultsByRound.getOrDefault(roundName, Collections.emptyList());

        VBox container = new VBox(16);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #231f20;");

        //We want the results grouped by region
        Map<String, List<GameResult>> byRegion = new LinkedHashMap<>();
        for(GameResult g : games) {
            byRegion.computeIfAbsent(g.getRegion(), k -> new ArrayList<>()).add(g);
        }

        for(Map.Entry<String, List<GameResult>> entry : byRegion.entrySet()) {
            Label regionLabel = new Label(entry.getKey().toUpperCase());
            regionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            regionLabel.setTextFill(Color.web("#eeab20"));
            regionLabel.setPadding(new Insets(4, 0, 4, 4));

            FlowPane cardsRow = new FlowPane();
            cardsRow.setStyle("-fx-background-color: #231f20;");
            cardsRow.setHgap(12);
            cardsRow.setVgap(12);
            cardsRow.setPadding(new Insets(4));

            for(GameResult g : entry.getValue()) {
                cardsRow.getChildren().add(buildGameCard(g));
            }
            
            container.getChildren().addAll(regionLabel, cardsRow);
        }

        return container;

    }

    private VBox buildGameCard(GameResult g) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle(
            "-fx-background-color: #2e292a;" +
            "-fx-border-color: #3a3435;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" 
        );
        card.setMinWidth(220);
        card.setMaxWidth(260);

        HBox row1 = teamRow(g.getTeam1(), g.getScore1(), g.getWinner().equals(g.getTeam1()));
        HBox row2 = teamRow(g.getTeam2(), g.getScore2(), g.getWinner().equals(g.getTeam2()));

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #231f20;");

        card.getChildren().addAll(row1, divider, row2);

        return card;
    }

    private HBox teamRow(String teamName, int score, boolean isWinner) {
        Label nameLbl = new Label(teamName);
        Label scoreLbl = new Label(String.valueOf(score));

        //If the team is a winner, the font should be bold
        //else the font should be normal
        nameLbl.setFont(Font.font("Arial", isWinner ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        scoreLbl.setFont(Font.font("Arial", isWinner ? FontWeight.BOLD : FontWeight.NORMAL, 13));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(6, nameLbl, spacer, scoreLbl);
        row.setPadding(new Insets(4, 6, 4, 6));
        row.setAlignment(Pos.CENTER_LEFT);

        if(isWinner) {
            row.setStyle("-fx-background-color: #3a3435;");
            nameLbl.setTextFill(Color.web("#fe6229"));
            scoreLbl.setTextFill(Color.web("#fe6229"));
        } else {
            nameLbl.setTextFill(Color.web("#70684e"));
            scoreLbl.setTextFill(Color.web("#70684e"));    
        }

        return row;
    }

    private void buildResultsData() {
        try {
            InputStream is = getClass().getResourceAsStream("2017Results.JSON");
            if(is == null) {
                showParseError("2017Results.JSON not found in resources folder");
                return;
            }

            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray rounds = root.getAsJsonArray("rounds");

            for(JsonElement roundE1 : rounds) {
                JsonObject round = roundE1.getAsJsonObject();
                String roundName = round.get("name").getAsString();
                JsonArray games = round.getAsJsonArray("games");

                List<GameResult> results = new ArrayList<>();
                for(JsonElement gameE1 : games) {
                    JsonObject game = gameE1.getAsJsonObject();
                    int index = game.get("bracketIndex").getAsInt();
                    String region = game.get("region").getAsString();
                    String winner = game.get("winner").getAsString();

                    JsonObject t1 = game.getAsJsonObject("team1");
                    JsonObject t2 = game.getAsJsonObject("team2");
                    String team1Name = t1.get("name").getAsString();
                    int score1 = t1.get("score").getAsInt();
                    String team2Name = t2.get("name").getAsString();
                    int score2 = t2.get("score").getAsInt();

                    results.add(new GameResult(index, region, 
                                               team1Name , score1, 
                                               team2Name, score2, winner));
                }

                resultsByRound.put(roundName, results);
            }
        } catch (JsonParseException e) {
            showParseError("JSON broke: " + e.getMessage());
        }
    }

    //Helper method for displaying an error message within the JavaFX window
    private void showParseError(String msg) {
        Label err = new Label("Could not load real results:\n" + msg);
        err.setTextFill(Color.RED);
        err.setPadding(new Insets(20));
        setCenter(err);
    }
}
