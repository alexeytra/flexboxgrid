package classes;

import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reader {
    private String pathFile;
    private String prevNumber;
    private boolean firstEntrance = true;
    private List<ListOne> listSection;
    private List<ListOne> listSubsection;
    private List<ListOne> listSubparag;
    private List<ListOne> listPosition;
    private List<ListOne> listItems;
    static final String TYPE = "java.lang.String";
    static final String NPA = "Постановление Совмина СССР от 22.08.1956 N 1173,  ";
    static final String RAZD = "razd";
    static final String RAZDTWO = "разд.";
    static final String PODR = "podr";
    static final String PODRTWO = "подразд.";
    static final String PP = "pp";
    static final String PPTWO = "пп.";
    static final String POST = "post";
    static final String POSTR = "поз.";
    static final String P = "p";
    static final String PR = "п.";



    public Reader(String pathFile) {
        this.pathFile = pathFile;
    }

    public void ReadFile() throws IOException {
        String[] str = new String[2];
        List<StringOfList> stringOfLists = new ArrayList<>();
        try{
            FileInputStream fstream = new FileInputStream(pathFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream,  "Cp1251"));
            String strLine;

            while ((strLine = br.readLine()) != null){


                int i = 0;
                String currentStr;
                for (String retval : strLine.split("\\s", 2)) {
                    str[i] = retval;
                    i++;
                    br.mark(9999);
                }

                if (!(testRoman(str[0]) || testNumeral(str[0]) || testLetter(str[0]) || testNumItem(str[0])) && !str[0].equals("")){str[1] = str[0] + " " + str[1]; str[0] = "-";}

                if((testRoman(str[0]) || testNumeral(str[0]) || testNumItem(str[0]) || testLetter(str[0])) && !br.readLine().equals("")){
                    br.reset();
                    while (!(strLine = br.readLine()).equals("")){
                        str[1] += " " + strLine;
                    }
                }

                if (!str[0].equals("") && !str[1].equals("")) {
                    //System.out.println(str[0] + " " + str[1]);
                    stringOfLists.add(new StringOfList(str[0], str[1]));
                    Arrays.fill(str, "");
                }


            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        stringOfLists.forEach((temp) -> System.out.println(temp.getNum() + " " + temp.getContent()));

        intoListOne(stringOfLists);


        WriteExcel.WriteToExcel(listSection, 1);
        WriteExcel.WriteToExcel(listSubsection, 2);
        WriteExcel.WriteToExcel(listItems, 3);
        WriteExcel.WriteToExcel(listSubparag, 4);
        WriteExcel.WriteToExcel(listPosition, 5);




        System.out.println();
    }


    void intoListOne(List<StringOfList> l){
        listSection = new ArrayList<>();
        listSubsection = new ArrayList<>();
        listSubparag = new ArrayList<>();
        listPosition = new ArrayList<>();
        listItems = new ArrayList<>();

        final String[] code = new String[1];
        RomanNumeral r = new RomanNumeral();
        final String[] upRazd = {""};
        final String[] upRazdRom = {""};
        final String[] upSubrazd = {""};
        final String[] upPP = {""};
        final String[] upPPR = {""};
        final String[] upP = {""};
        final String[] upPR = {""};
        String[] upSubrazdR = {""};
        final boolean[] existSubsection = {false};
        final boolean[] existSubparagraph = {false};
        final boolean[] existItem = {false};
        final int[] j = {0};
        final String[] cipher = {""};



        l.forEach( (temp) -> {
            if (testRoman(temp.num)){
                j[0] = 0;

                existSubsection[0] = false;
                existSubparagraph[0] = false;
                existItem[0] = false;

                String n = temp.num.replaceAll("\\.", "");
                String num = String.valueOf(r.convertRomanToInt(n));
                if (num.length() == 1) code[0] = RAZD + "0" + num; else code[0] = RAZD + num;

                listSection.add(new ListOne(
                        temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + RAZDTWO + n
                ));
                upRazd[0] = code[0];
                upRazdRom[0] = RAZDTWO + n;
                code[0] = "";


            }else if (testNumeral(temp.num)){ // Подраздел
                j[0] = 0;
                existSubsection[0] = true; //Существует ли подраздел
                existItem[0] = false;
                String num = temp.num.replaceAll("\\.", "");
                if (num.length() == 1) code[0] = upRazd[0] + PODR + "0" + num; else code[0] = upRazd[0] + PODR + num;

                listSubsection.add(new ListOne(
                        temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upRazdRom[0] + " " + PODRTWO + num
                ));

                upSubrazd[0] = code[0];
                upSubrazdR[0] = upRazdRom[0] + " " + PODRTWO + num;
                code[0] = "";

            }else if (testLetter(temp.num)) { //Подпункт
                existSubparagraph[0] = true; //Существует ли Подпункт

                j[0] = 0;
                String n = temp.num.replaceAll("\\)", "");
                String num = ConverterToEngLetter.getEngLetter(n);

                if (existSubsection[0]) {
                    if (existItem[0]){
                        code[0] = upP[0] + PP + num.toUpperCase();
                        listSubparag.add(new ListOne(
                                temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upPR[0] + " " + PPTWO + n
                        ));
                        upPP[0] = code[0];
                        upPPR[0] = upPR[0] + " " + PPTWO + n;
                        code[0] = "";
                    }else {
                        code[0] = upSubrazd[0] + PP + num.toUpperCase();
                        listSubparag.add(new ListOne(
                                temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upSubrazdR[0] + " " + PPTWO + n
                        ));
                        upPP[0] = code[0];
                        upPPR[0] = upSubrazdR[0] + " " + PPTWO + n;
                        code[0] = "";
                    }

                }else {
                    code[0] = upRazd[0] + PP + num.toUpperCase();
                    listSubparag.add(new ListOne(
                            temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upRazdRom[0] + " " + PPTWO + n
                    ));
                    upPP[0] = code[0];
                    upPPR[0] = upRazdRom[0] + " " + PPTWO + n;
                    code[0] = "";
                }
            }else if(testNumItem(temp.num)){ //Существует ли пунк
                String n = temp.num.replaceAll("\\)", "");
                existItem[0] = true;
                code[0] = upSubrazd[0] + P + n;
                listItems.add(new ListOne(
                        temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upSubrazdR[0] + " " + PR + n
                ));

                upP[0] = code[0];
                upPR[0] = upSubrazdR[0] + " " + PR + n;
                code[0] = "";
            }else { // Позиция

               if(!temp.num.contains("-")){
                   code[0] = upSubrazd[0];
                    listSection.add(new ListOne(
                            temp.num + " " + temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upSubrazdR[0]));
               }else {
                   if (!existSubparagraph[0] && !existSubsection[0]) {
                       j[0]++;
                       code[0] = upRazd[0] + POST + j[0];
                       listPosition.add(new ListOne(
                               temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upRazdRom[0] + " " + POSTR + j[0]
                       ));
                   } else if (existSubsection[0]) {
                       if (existItem[0]) {
                           j[0]++;
                           code[0] = upP[0] + POST + j[0];
                           listPosition.add(new ListOne(
                                   temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upPR[0] + " " + POSTR + j[0]));

                       } else if (existSubparagraph[0]) {
                           j[0]++;
                           code[0] = upPP[0] + POST + j[0];
                           listPosition.add(new ListOne(
                                   temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upPPR[0] + " " + POSTR + j[0]));
                       } else {
                           j[0]++;
                           code[0] = upSubrazd[0] + POST + j[0];
                           listPosition.add(new ListOne(
                                   temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upSubrazdR[0] + " " + POSTR + j[0]
                           ));
                       }
                   } else {
                       j[0]++;
                       code[0] = upPP[0] + POST + j[0];
                       listPosition.add(new ListOne(
                               temp.content, code[0], cipher[0], TYPE, "", "", "", "", NPA + upPPR[0] + " " + POSTR + j[0]
                       ));
                   }
               }

            }
        });
    }

    private static boolean testRoman(String str) {
        Pattern p = Pattern.compile("[IVXLCDM]+");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static boolean testLetter(String str) {
        Pattern p = Pattern.compile("[абвгдеж]\\)");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static boolean testNumeral(String str){
        Pattern p = Pattern.compile("[0-9]+\\.");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static boolean testNumItem(String str){
        Pattern p = Pattern.compile("[0-9]+\\)");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static boolean testCipher(String str){
        Pattern p = Pattern.compile("(-?\\d+){6}");
        Matcher m = p.matcher(str);
        return m.find();
    }
}