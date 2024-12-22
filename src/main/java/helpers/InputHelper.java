package helpers;

import java.util.Map;
import java.util.Scanner;

public class InputHelper {
    private static final Scanner scanner = new Scanner(System.in);

    public static String getUserInput() {
        return scanner.nextLine();
    }

    public static String matchRepositoryIndexToName(Map<Integer, String> repoMap){
        System.out.println("Enter the number corresponding to the repository:");

        int repoNumber;
        try {
            repoNumber = Integer.parseInt(InputHelper.getUserInput());
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a valid number.");
            return null;
        }

        if (!repoMap.containsKey(repoNumber)) {
            System.err.println("Invalid repository number.");
            return null;
        }

        return repoMap.get(repoNumber); // Return the repository name
    }
}
