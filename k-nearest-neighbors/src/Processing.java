/**
 * @author Michał Krejner s27799
 */

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Processing {
    private final String trainingFileName;
    private int k;
    private int trainingFileColumns = 0;
    private int trainingFileRows = 0;
    private String[][] trainArr;
    private String[][] testArr;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public Processing(String trainingFileName, int k){
        this.trainingFileName = trainingFileName;
        this.k = k;
        train();
        openLoop();
        System.out.println("Asta la vista");
    }
    public void openLoop(){
        while(true){
            System.out.println("Wybierz jedna z opcji");
            System.out.println("A -> Klasyfikacja zbioru");
            System.out.println("B -> Klasyfikacja obserwacji");
            System.out.println("K -> Zmień parametr K");
            System.out.println("Q -> Wyjdź z programu");

            Scanner sc = new Scanner(System.in);
            String option;
            do{
                System.out.print(": ");
                option = sc.nextLine().toLowerCase().trim();
                System.out.println();
            }
            while (!option.equals("a") && !option.equals("b") && !option.equals("k") && !option.equals("q"));

            switch (option) {
                case "a" -> optionA();
                case "b" -> optionB();
                case "k" -> changeK();
                case "q" -> System.out.println("Quitting...");
            }
            if(option.equalsIgnoreCase("q"))
                break;
        }
    }
    private void optionA(){
        int correctClasifications = 0;
        BufferedReader bReader = obtainFReader();

        long start = System.currentTimeMillis();

        String line = null;
        try{
            line = bReader.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
        if(trainingFileColumns != line.split(",").length){
            System.out.println(ANSI_RED + "Plik testowy niezgodny z treningowym\n" + ANSI_RESET);
            return;
        }else {
            testArr = new String[1000][trainingFileColumns];
            testArr[0] = line.split(",");
        }

        int counter = 1;
        try {
            while ((line = bReader.readLine()) != null){
                testArr[counter] = line.split(","); // zczytywanie pliku test do tablicy
                counter++;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        int testFileRows = counter;

        for(int i = 0; i < testFileRows; i++){
            double[] vectorArr = readAndProcessInput(testArr[i], -1); // wektor double
            double[] distancesArr = calculateDistances(vectorArr); // obliczenie dystansow dla danego k
            HashMap<String, Integer> myMap = classifyAndVote(distancesArr); // klasyfikacja wektora
            correctClasifications += printClass(i, myMap); // wynik
        }

        double accuracy = (double) correctClasifications/ testFileRows;
        accuracy *= 100;
        System.out.println(ANSI_GREEN + "Accuracy: " + roundTo2DecimalPlace(accuracy) + "%" + ANSI_RESET);

        long end = System.currentTimeMillis();
        long elapsed = end - start;
        if(elapsed <= 1){
            System.out.println(ANSI_CYAN + "Time elapsed: less then " + elapsed + " [ms]\n" + ANSI_RESET);
        }else
            System.out.println(ANSI_CYAN + "Time elapsed: " + elapsed + " [ms]\n" + ANSI_RESET);
    }
    private void optionB(){
        System.out.println("Wprowadź argumenty: ( arg1 [,] arg2 [,] ... argX )");
        Scanner sc = new Scanner(System.in);
        String[] args = sc.nextLine().split(",");

        long start = System.currentTimeMillis();

        double[] vectorArr = readAndProcessInput(args, 0); // input=string[] vector ---> output=double[] vector
        double[] distancesArr = calculateDistances(vectorArr); // obliczenie dystansow dla danego k
        HashMap<String, Integer> myMap = classifyAndVote(distancesArr); // klasyfikacja wektora

        System.out.println("Recognized class of object -> " + ANSI_BLUE + findMaxValue(myMap) + ANSI_RESET);
//        System.out.println(myMap + "\n"); // wynik / klasyfikacja dla wektora z konsoli

        long end = System.currentTimeMillis();
        long elapsed = end - start;
        if(elapsed <= 1){
            System.out.println(ANSI_CYAN + "Time elapsed: less then " + elapsed + " [ms]\n" + ANSI_RESET);
        }else
            System.out.println(ANSI_CYAN + "Time elapsed: " + elapsed + " [ms]\n" + ANSI_RESET);
    }
    private double[] readAndProcessInput(String[] line, int tail){ // tail=0 (wektor z konsoli) tail=-1 (wektor z pliku)
        double[] vector = new double[line.length + tail];
        for (int i = 0; i < line.length + tail; i++){ // konwersja string ---> double
            vector[i] = Double.parseDouble(line[i]);
        }
        return vector;
    }
    private double[] calculateDistances(double[] vector){
        double[] distances = new double[trainingFileRows];

        double val = 0;
        for(int i = 0; i < distances.length; i++){ // obliczanie dystansu dla kazdego rekordu
            for(int k = 0; k < trainingFileColumns - 1; k++){
                val += Math.pow( ( vector[k] - Double.parseDouble(trainArr[i][k]) ), 2 ) ;
            }
            distances[i] = val;
            val = 0;
        }
        return distances;
    }
    private HashMap<String, Integer> classifyAndVote(double[] distancesArr){
        HashMap<String, Integer> myMap = new HashMap<>();
        int[] smallestValueIndexes = getIndexWithNewSmallestValue(distancesArr); // pozyskanie najblizszych sasiadow
        for(int i = 0; i < k; i++){
            String key = trainArr[smallestValueIndexes[i]][trainingFileColumns-1]; // pozysanie klucza dla indexu z najmniejsza odlegloscia
            if(myMap.containsKey( key )){ // aktualizacja wartosci dla klcuza
                int tmp = myMap.get( key );
                tmp++;
                myMap.put(key, tmp);
            }
            else{ // dodanie nowego klucza
                myMap.put(key, 1);
            }
        }
//        System.out.println(myMap);
        return myMap;
    }
    private int[] getIndexWithNewSmallestValue(double[] distances){ // znalezienie indexow z najmniejszymi wartosciami
        int[] indexy = new int[distances.length];
        for (int i = 0; i < indexy.length; i++) { // sztuczne wypelnienie
            indexy[i] = i;
        }

        for (int i = 0; i < distances.length - 1; i++) { // posortowanie indexow w oparciu o najmniejsze wartosci
            for (int j = i + 1; j < distances.length; j++) {
                if (distances[indexy[i]] > distances[indexy[j]]) {
                    int temp = indexy[i];
                    indexy[i] = indexy[j];
                    indexy[j] = temp;
                }
            }
        }
        return Arrays.copyOfRange(indexy, 0, k);
    }
    private String findMaxValue(HashMap<String, Integer> map){
        String maxKey = null;
        int maxValue = Integer.MIN_VALUE;

        for (Map.Entry<String, Integer> entry : map.entrySet()) { // iteracja po mapie
            if (entry.getValue() > maxValue) { //
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        return maxKey;
    }
    private int printClass(int row, HashMap<String, Integer> myMap){
        System.out.print("< ");
        for(int i = 0; i < trainingFileColumns - 1; i++){
            System.out.print(testArr[row][i] + "; ");
        }
        String key = findMaxValue(myMap);
        System.out.print("> nalezy do: " + ANSI_BLUE + key + ANSI_RESET + "\n");

        if (key.equals(testArr[row][trainingFileColumns-1])){
            return 1;
        }
        return 0;
    }
    private void train(){
        try{
            File file = new File(trainingFileName);
            FileReader fReader = new FileReader(file);
            BufferedReader bReader = new BufferedReader(fReader);

            String line = bReader.readLine();
            trainingFileColumns = line.split(",").length; // zczytanie wymiaru wektora/ów
            trainArr = new String[1000][trainingFileColumns]; // default record size = 1000

            fReader.close();
            bReader.close();
            fReader = new FileReader(file);
            bReader = new BufferedReader(fReader);

            while( (line = bReader.readLine()) != null ){ // zczytywanie pliku
                for(int i = 0; i < trainingFileColumns; i++){
                    trainArr[trainingFileRows] = line.split(",");
                }
                trainingFileRows++;
            }
            if(k > trainingFileRows){
                k = trainingFileRows;
                System.out.println(ANSI_RED + "Podany parametr K zbyt duzy w stosunku do zgromadzonych danych treningowych.");
                System.out.println("Ustawiony zostanie maksymalny mozliwy ( K = " + trainingFileRows + " )" + ANSI_RESET);
            }

            System.out.println("TrainingFileColumns: " + trainingFileColumns);
            System.out.println("TrainingFileRows: " + trainingFileRows + "\n");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    private void changeK(){
        String strK;
        double k = -1;
        Scanner sc = new Scanner(System.in);

        do {
            System.out.print("Podaj parametr K: ");
            strK = sc.nextLine();
            if (strK != null && !strK.isEmpty()) {
                try {
                    k = Double.parseDouble(strK);
                } catch (NumberFormatException n) {
                    k = -1;
                }
            }

        } while ((int) k <= 0);
        this.k = (int) k;

        if(k > trainingFileRows){
            this.k = trainingFileRows;
            System.out.println(ANSI_RED + "Podany parametr K zbyt duzy w stosunku do zgromadzonych danych treningowych.");
            System.out.println("Ustawiony zostanie maksymalny mozliwy ( K = " + trainingFileRows + " )\n" + ANSI_RESET);
        }
        System.out.println();
    }
    private static BufferedReader obtainFReader() {
        Scanner sc = new Scanner(System.in);
        String fileName;
        do {
            System.out.print("Wprowadz nazwe pliku: ");
            fileName = sc.nextLine();
        } while (!new File(fileName).exists());

        BufferedReader bReader = null;
        try{
            File file = new File(fileName);
            FileReader fReader = new FileReader(file);
            bReader = new BufferedReader(fReader);
        }catch (IOException e){
            e.printStackTrace();
        }
        return bReader;
    }
    public static double roundTo2DecimalPlace(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
