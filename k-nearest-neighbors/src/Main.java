/**
 * @author Michał Krejner s27799
 */

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        new Processing(obtainFName(), obtainK());
    }
    private static String obtainFName() {
        Scanner sc = new Scanner(System.in);
        String fileName;
        do {
            System.out.print("Wprowadz nazwe pliku: ");
            fileName = sc.nextLine();
        } while (!new File(fileName).exists());

        return fileName;
    }

    private static int obtainK() {
        String strK;
        double k = -1;
        Scanner sc = new Scanner(System.in);

        do {
            System.out.print("Podaj parametr K: ");
            strK = sc.nextLine();
            if( strK != null && !strK.isEmpty()){
                try{
                    k = Double.parseDouble( strK );
                }catch (NumberFormatException n){
                    k = -1;
                }
            }

            if( (int) k > 0 )
                return (int) k;
        }while ( true );
    }
}
