//Joey Barton
//GameResult.java
//This class contains all the data for a single, real life game

public class GameResult {
    
    private final int bracketIndex;
    private final String region;
    private final String team1;
    private final int score1;
    private final String team2;
    private final int score2;
    private final String winner;

    public GameResult(int bracketIndex, String region, String team1, int score1, String team2, int score2, String winner) {
        this.bracketIndex = bracketIndex;
        this.region = region;
        this.team1 = team1;
        this.score1 = score1;
        this.team2 = team2;
        this.score2 = score2;
        this.winner = winner;
    }

    //GETTERS
    public int getBracketIndex() { return this.bracketIndex;}
    public String getRegion() { return this.region;}
    public String getTeam1() { return this.team1;}
    public int getScore1() { return this.score1;}
    public String getTeam2() { return this.team2;}
    public int getScore2() { return this.score2;}
    public String getWinner() { return this.winner;}

    @Override
    public String toString() {
        return "GameResult { \n" + 
               "bracketIndex = " + bracketIndex +
               ", region = " + region +
               ", " + team1 + 
               " scored = " + score1 + 
               ", " + team2 +
               " scored = " + score2 +
               "\nWinner = " + winner +
               " }" ;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        return false;
    }
}
