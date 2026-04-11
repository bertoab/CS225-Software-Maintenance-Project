//LIOR: commented out unused imports
import java.io.*;
//import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.concurrent.ExecutionException;


/**
 * Created by Artem on 5/2/2017.
 */

/*
 * LIOR: made the methods of this class static, changing the method calls in other classes to call
 * these methods rather than methods on an instance of this class. 
 * Moved simulate() to the Bracket class.
 * Added method getEmptyBracket()
 */

public class TournamentInfo{//renamed from teamInfo by matt 5/4
    //LIOR: made teams private & static
    private static HashMap<String, Team> teams;
    //LIOR: added an emptyBracket attribute
    private static ArrayList<String> emptyBracket;

    //LIOR: no need for a constructor like this since this class doesn't need to be instantiated anymore
    // public TournamentInfo() throws IOException{
    //     teams = new HashMap<>();
    //     loadFromFile();
    // }

    /**
     * This private method will load all the team information from the teamInfo.txt file via a BufferedReader and load each team into
     * the teams HashMap using their name as the key and the actual Team object as the data.
     * @authors Artem, Rodrigo
     */
    public static void loadTeamsFromFile() throws IOException{
        teams = new HashMap<String, Team>();

        String name;
        String nickname;
        String info;
        int ranking;
        double offensivePPG;
        double defensivePPG;

        try{
            //LIOR: replaced getClass() with TournamentInfo.class to make it work as a static method
            InputStream u = TournamentInfo.class.getResourceAsStream("teamInfo.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(u));

            while((name = br.readLine()) != null){
            	nickname = br.readLine();
                info = br.readLine();
                ranking = Integer.parseInt(br.readLine());
                offensivePPG = Double.parseDouble(br.readLine());
                defensivePPG = Double.parseDouble(br.readLine());
                
                Team newTeam = new Team(name, nickname, info, ranking, offensivePPG, defensivePPG); //creates team with info

                br.readLine();   //gets rid of empty line between team infos

                teams.put(newTeam.getName(), newTeam);   //map team name with respective team object
            }

            br.close();

        }
        catch(IOException ioe) {
            throw ioe;
        }
    }

    /**
     * This method will take a parameter of a team name and return the Team object corresponding to it.
     * If it is unsuccessful, meaning the team does not exist, it will throw an exception.
     * @authors Artem
     * @param teamName -- the name of the team to be found
     * @return the Team object for that team
     * @throws Exception in case it's not in there
     */
    public static Team getTeam(String teamName){
        return teams.get(teamName);
    }

    //LIOR: moved this method to Bracket

    /**
     * This will be the method that actually does the work of determining the outcome of the games.
     * It will use the seed/ranking from each team on the bracket and put it into an algorithm to somewhat randomly generate a winner
     * @authors Artem, Dan, Matt
     * @param startingBracket -- the bracket to be simulated upon. The master bracket
     */
    public void simulate(Bracket startingBracket){
        for (int i = 62; i >= 0; i--) {
        /* The equation for score that I settled on is this:
         * (Random int 75-135) * (1 - 0.02 * seed ranking)
         * This way, the multiplier would be between 0.68 and 0.98. Multiply that by 75-135, and you get a reasonable score with room for chance to prevail for lower teams. */

            int index1 = 2*i+1;
            int index2 = 2*i+2;

            Team team1 = teams.get(startingBracket.getBracket().get(index1));
            Team team2 = teams.get(startingBracket.getBracket().get(index2));

            int score1 = 0;
            int score2 = 0;
            while(score1==score2) {
                score1 = (int) (((Math.random() * 136) + 75) * (1 - (team1.getRanking() * 0.02)));
                score2 = (int) (((Math.random() * 136) + 75) * (1 - (team2.getRanking() * 0.02)));
            }

            startingBracket.setTeamScore(index1, score1);
            startingBracket.setTeamScore(index2, score2);

            if(score1>score2)
                startingBracket.moveTeamUp(index1);
            else
                startingBracket.moveTeamUp(index2);
        }

    }



    //LIOR: changed return type to void, changed method to just load the arrayList into a stored 
    /**
     * reads Strings from initialMatches.txt into an ArrayList in order to construct the starting bracket
     * @authors Matt, Artem
     * @return ArrayList of Strings
     */
    public static void loadEmptyBracket() throws IOException {
        String name;
        emptyBracket = new ArrayList<String>();

        try{
            //LIOR: replaced getClass() with TournamentInfo.class to make it work as a static method
            InputStream u = TournamentInfo.class.getResourceAsStream("initialMatches.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(u));

            while((name = br.readLine()) != null){
                emptyBracket.add(name);
            }
            
            br.close();
        }
        catch(IOException ioe){
            throw ioe;
        }
    }

    //LIOR: added this method which returns a copy of an empty bracket
    public static Bracket getEmptyBracket() {
        return new Bracket(emptyBracket);
    }

    public static void autoFill(Bracket bracket) {
        for (int i = 62; i >= 0; i--) {
            if (!bracket.getBracket().get(i).isEmpty()) continue;

            int index1 = 2 * i + 1;
            int index2 = 2 * i + 2;

            String team1 = bracket.getBracket().get(index1);
            String team2 = bracket.getBracket().get(index2);

            if (team1.isEmpty() || team2.isEmpty()) continue;

            Team t1 = teams.get(team1);
            Team t2 = teams.get(team2);
            
            if (team1 == null || team2 == null) continue;

            int score1 = 0, score2 = 0;
            while (score1 == score2) {
                score1 = (int) (((Math.random() * 136) + 75) * (1 - (t1.getRanking() * 0.02)));
                score2 = (int) (((Math.random() * 136) + 75) * (1 - (t2.getRanking() * 0.02)));
            }
            if (score1 > score2) 
                bracket.moveTeamUp(index1);
            else
                bracket.moveTeamUp(index2);
        }
    }
}
