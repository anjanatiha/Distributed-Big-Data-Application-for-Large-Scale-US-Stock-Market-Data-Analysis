import DataFieldType.*;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static DataFieldType.TickerSymbols.getTickers;
import static Misc.FileClass.unZip;
import static Misc.FileProperties.extractYear;
import static Misc.Print.print;
import static Misc.Time.printElapsedTime;
import static Misc.Time.printTime;


public class TAQConverterMainFN2 {
    private TAQConverterSparkFN2 TAQConverterSparkFNObject;
    private String inputFileName;
    private String outputFileName;
    private ITAQSpec ITAQSpecObject;
    private IFieldType[] fieldTypes;
    private List<String> tickerSymbols;
    private String startTime="";
    private String endTime="";
    private String TAQFileType="";
    private String inputFileType="";
    private boolean tickerFilter=false;
    private boolean timeFilter=false;
    private int fileYear;
    private int start=0;
    public static JavaSparkContext sc;

    public String getOutputFileName(String inputFileName){
        String outputFileName;
        if (inputFileName.substring(inputFileName.length()-3,inputFileName.length()).equals("zip")) {
            outputFileName = inputFileName.substring(0,inputFileName.length()-4) + "_extracted";
        } else {
            outputFileName = inputFileName + "_converted";
        }
        return outputFileName;
    }
    public String getInputFileType(String inputFileName) {
        if (inputFileName.substring(inputFileName.length() - 3, inputFileName.length()).equals("zip")){
        return "zip";
    }else
            return "txt";
    }
    TAQConverterMainFN2(String[] args) {


        this.TAQFileType = args[0];
        this.fileYear = Integer.parseInt(args[1]);
        this.inputFileName = args[2];
        if (!args[3].equals("n")) {
            this.timeFilter = true;
            this.startTime = args[4];
            this.endTime = args[5];
        }
        if (!args[5].equals("n")){
            this.tickerSymbols = getTickers();
            this.tickerFilter=true;
        }
        this.start = 1;
        this.inputFileType = getInputFileType(inputFileName);
        this.outputFileName = getOutputFileName(inputFileName);
        this.sc = sc;

        switch (fileYear){
            case 2010:
                ITAQSpecObject = new TAQ102010Spec();
                break;
            case 2012:
                ITAQSpecObject = new TAQ072012Spec();
                break;
            case 2013:
                ITAQSpecObject = new TAQ082013Spec();
                break;
            case 2015:
                ITAQSpecObject = new TAQ062015Spec();
                break;
            case 2016:
                ITAQSpecObject = new TAQ062016Spec();
                break;
        }
        switch (TAQFileType) {
            case "trade":
                fieldTypes = ITAQSpecObject.getTradeFields();
                break;
            case "nbbo":
                fieldTypes = ITAQSpecObject.getNBBOFields();
                break;
            case "quote":
                fieldTypes = ITAQSpecObject.getQuoteFields();
                break;
        }
        if(inputFileType.equals("zip")) {
            unZip(inputFileName, outputFileName);
            print("Completed Unzipping: "+ inputFileName + outputFileName);
            inputFileName = outputFileName;
            outputFileName = getOutputFileName(inputFileName);
        }
        if (!timeFilter) {
            if(!tickerFilter)
                TAQConverterSparkFNObject = new TAQConverterSparkFN2(sc, inputFileName, outputFileName, fieldTypes, start);
            else
                TAQConverterSparkFNObject = new TAQConverterSparkFN2(sc, inputFileName, outputFileName, fieldTypes, tickerSymbols, start);
        }
        else
        {
            if (!tickerFilter)
                TAQConverterSparkFNObject = new TAQConverterSparkFN2(sc, inputFileName, outputFileName, fieldTypes, startTime, endTime, start);
            else
                TAQConverterSparkFNObject = new TAQConverterSparkFN2(sc, inputFileName, outputFileName, fieldTypes,startTime,endTime, tickerSymbols, start);
        }

    }

    public static void convertDirectory(String directory){
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                String year = extractYear(directory+listOfFiles[i].getName());

            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
    }
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        printTime();
        TAQConverterMainFN2 TAQAnalysisObject = new TAQConverterMainFN2(args);
        printTime();
        long endTime = System.currentTimeMillis();
        printElapsedTime(startTime, endTime);
    }

}