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

    // DANIELLE: toString() and equals() override
    /**
     * Represents the GameResult object as a String.
     * @return the object's String representation.
     */
    @Override
    public String toString() {
        return bracketIndex + ", " + region + ", " + team1 + ", " +
                score1 + ", " + team2 + ", " + score2 + ", " + winner;
    }

    /**
     * Compares a given object to this.
     * @param obj the object to compare with.
     * @return whether the objects are the same.
     */
    @Override
    public boolean equals(Object obj) {
        // Check if objects are the same in memory
        if(this == obj) {
            return true;
        }

        // Check if obj is null or is a GameResult
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }

        // Cast obj to a GameResult and compare fields
        GameResult gameResult = (GameResult) obj;

        return this.bracketIndex == gameResult.bracketIndex
                && this.region.equals(gameResult.region)
                && this.team1.equals(gameResult.team1)
                && this.score1 == gameResult.score1
                && this.team2.equals(gameResult.team2)
                && this.score2 == gameResult.score2
                && this.winner.equals(gameResult.winner);
    }

}
