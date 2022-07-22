import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception { //Pass File Names for Read and Write
        System.out.println(args[0]);
        System.out.println(args[1]);

        //Initialize File Writer for Python File
        FileWriter py = new FileWriter("parser.py");

        //Initialize Scanner to read Command Output
        File fr = new File(args[0]);
        Scanner myReader = new Scanner(fr);
        
        int currLineNum = 1;

        //Python Imports
        py.write("import re\n");
        py.write("import json\n\n");

        //Convert JSON to Java Object
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(args[1]));
        Line[] lines = gson.fromJson(reader, Line[].class);
        
        //Create Regular Expression Dictionary
        py.write("rx_dict =  {\n");
        for (int i = 0; i < lines.length; i++) {
            Line currLine = lines[i];
            String lineText = new String();
            //Get the cooresponding line in the command output
            while (currLineNum <= currLine.lineNum) {
                lineText = myReader.nextLine(); //String form of current line
                currLineNum++;
            }

            //Length of the Current Line
            int lineLength = lineText.length();
            
            //Mark Spaces occupied by Variables in HashMap blocked
            HashMap<Integer, Boolean> blocked = new HashMap<Integer, Boolean>();
            List<Selection> lineSelections = currLine.selections;
            for (int j = 0; j < lineSelections.size(); j++) {
                for (int k = lineSelections.get(j).start; k <= lineSelections.get(j).end; k++) {
                    blocked.put(k, true);
                }
            }
            
            //Traverse Line and Create RegExp
            String lineExp = new String("re.compile(r\"^");
            int currSelection = 0;
            for (int j = 0; j < lineLength; j++) {
                if (!blocked.containsKey(j)) {
                    if (lineText.charAt(j) == '^' || lineText.charAt(j) == '$' || lineText.charAt(j) == '.'
                            || lineText.charAt(j) == '|' || lineText.charAt(j) == '+' || lineText.charAt(j) == '*'
                            || lineText.charAt(j) == '?'
                            || lineText.charAt(j) == '[' || lineText.charAt(j) == ']' || lineText.charAt(j) == '(' || lineText.charAt(j) == ')' ) {
                        lineExp += ("\\" + lineText.charAt(j));
                    }
                    else if (lineText.charAt(j) == '\\') {
                        lineExp += ("\\\\");
                    } 
                    else {
                        lineExp += lineText.charAt(j);
                    } 
                } else if (blocked.get(j) == true) {
                    lineExp += "(?P<" + lineSelections.get(currSelection).name + ">.*)";
                    j = lineSelections.get(currSelection).end;
                    currSelection++;
                }
            }
            lineExp += "$\"),";
            py.write("  \"r" + String.valueOf(currLine.lineNum) + "\"" + ": " + lineExp + "\n");
        }

        //Close Regular Expression Dictionary
        py.write("}\n\n");

        //Python Code for Parsing File
        py.write("def parse_file(filepath):\n");
        py.write("  data = []\n");
        //Iterate through rx_dict and store all keys in a new List
        py.write("  with open(filepath, \"r\") as file_object:\n");
        py.write("      line = file_object.readline()\n");
        py.write("      while line:\n");
        py.write("          for key, rx in rx_dict.items():\n");
        py.write("              if rx.search(line):\n");
        py.write("                  for m in rx.finditer(line):\n");
        py.write("                      data.append(m.groupdict())\n");
        py.write("          line = file_object.readline()\n");     
        py.write("  return data\n\n");


        //Python Driver Code
        py.write("if __name__ == \"__main__\":\n");
        py.write("  filepath = \"");
        py.write(args[0]);
        py.write("\"\n");
        
        py.write("  data = parse_file(filepath)\n");
        py.write("  print(\"File Data:\", data)\n");
        py.write("  with open('data.json', 'w') as f:\n");
        py.write("      json.dump(data, f)\n");


        //Close Scanner
        myReader.close();

        //Close File Writer
        py.close();
    }
}
