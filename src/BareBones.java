// Java Program to illustrate Reading from FileReader
// using BufferedReader Class

// Importing input output classes
import java.io.*;
import java.util.HashMap;

// Main class
public class BareBones {
    // Comment using #
    // Arithmetics should be done with space between values
    // e.x. X = 3 + 2 / Y
    // ; is optional, code per line obligatory(python type of code)
    // tabulaton is optional
    // print method should called somewhat like in C++ but with sout instead of cout
    // e.x. sout << X << " " << "Hi!";

    HashMap<String,Double> vals = new HashMap<String,Double>();


    double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }


    String[] splitCode(String line){
        String regex = "\\#.*";
        return line.replace(";","").replaceAll(regex, "").strip().split(" ", 0);
    }

    String[][] read() throws Exception{
        //      """""""""""""""""""""""""""""""""""""""""""""""
        //        Read text file(should be stored as data.txt)
        //      """""""""""""""""""""""""""""""""""""""""""""""
        File file = new File(
                BareBones.class.getProtectionDomain().getCodeSource().getLocation().getPath(),"data.txt"
            );

		BufferedReader br = new BufferedReader(new FileReader(file));

        String[] line;
        String[][] lines = new String[100][];
        String temp = "  ";

        int i = 0;

        while(temp!=null){
            temp = br.readLine();
            if(temp != null){
                if(temp.substring(0, 4).equals("sout")){
                    line = temp.replace(";","").strip().split("<<", 0);
                }else{
                    line = splitCode(temp);
                }
                if(line[0]!=""){
                    lines[i] = line;
                    i++;
                }
            }
        }

        // Close buffer reader for optimisation
        br.close();

        return lines;
    }

    int arithmetics(String[] line){
        String variable = line[0];
        String operation = "";
        for(int i=2;i<line.length;i++){
            if(this.vals.containsKey(line[i])){
                operation = operation + this.vals.get(line[i]);
            }else{
                operation = operation + line[i];
            }
        }
        vals.put(variable, eval(operation));
        return 0;
    }

    void params(String call, String variable){
        //      """""""""""""""""""""""""""""""""""""""""""""""
        //        Implemented Interaction for variables
        //      """""""""""""""""""""""""""""""""""""""""""""""

        if(call.equals("clear")){
            vals.put(variable,0.0);
            // System.out.println(String.format("Initialising %s...", variable));
        }
        else if(call.equals("incr")){
            vals.put(variable,vals.get(variable)+1);
            // System.out.println(String.format("Incrementing %s by one...", variable));
        }else if(call.equals("decr")){
            // X--;
            vals.put(variable,vals.get(variable)-1);
            // System.out.println(String.format("Decrementing %s by one...", variable));
        }
    }

    void print(String[] line){
        String output = "";
        for(int i=1;i<line.length;i++){
            if(line[i].charAt(0) == '"'){
                output = output + line[i].replace("\"","");
            }
            else{
                output = output + this.vals.get(line[i]);
            }
        }
        System.out.println(output);
    }

    int go(String[] st, String[][] data,int i) throws Exception{
		while (st != null){
            String call = st[0];
            //exit recursion if end of the while loop statement
            if(call.equals("end")){
                return i;
            }
            if(call.equals("sout")){
                print(st);
            }
            String variable = st[1];
            if(variable.equals("=")){
                arithmetics(st);
                i++;
                st = data[i];
                continue;
            }
            // call recursion if while statement
            if(call.equals("while")){
                int j = i+1;
                while(vals.get(variable)!=0){
                    i = go(data[j],data,j);
                }
            }
            // Switch between variables
            params(call,variable);
            i++;
            st = data[i];
        }
        //print resulted variables
        return i;
    }

	public static void main(String[] args) throws Exception
	{
        BareBones barebone = new BareBones();

		String[][] data = barebone.read();

        String[] st = data[0];

        barebone.go(st,data,0);
	}
}
