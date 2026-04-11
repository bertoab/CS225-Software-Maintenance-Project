import java.io.IOException;

/**
 * Created by Matthew on 5/4/2017.
 */
public class BackendTest {
    public static void main(String[]args){
        try {
            //LIOR: changed this to work with the reworked TournamentInfo
            TournamentInfo.loadEmptyBracket();
            System.out.println(TournamentInfo.getTeam("Villanova").getRanking());
            Bracket starting = new Bracket(TournamentInfo.getEmptyBracket());
            //System.out.println(starting.getBracket().get(125));
            //starting.moveTeamUp(125);
            starting.simulate();


            System.out.println(starting.getBracket().get(0));
            System.out.println(starting.getBracket().get(3));
            System.out.println(starting.getBracket().get(4));
            System.out.println(starting.getBracket().get(5));
            System.out.println(starting.getBracket().get(6));
        }
        catch (IOException e){
            System.out.println("File not found");
        }
    }
}
