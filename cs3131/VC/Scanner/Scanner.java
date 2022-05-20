/*
 *
 *	Scanner.java                        
 *
 */

package VC.Scanner;

import VC.ErrorReporter;

public final class Scanner { 

  private SourceFile sourceFile;
  private boolean debug;

  private ErrorReporter errorReporter;
  private StringBuffer currentSpelling;
  private char currentChar;
  private SourcePosition sourcePos;
  
  public int startOfStringColumnNumber, endOfStringColumnNumber;
  public int lineNumber;
  

// =========================================================

  public Scanner(SourceFile source, ErrorReporter reporter) {
    sourceFile = source;
    errorReporter = reporter;
    currentChar = sourceFile.getNextChar();
    debug = false;

    // you may initialise your counters for line and column numbers here
    startOfStringColumnNumber = 1;
    endOfStringColumnNumber = 1;
    lineNumber = 1;

  }

  public void enableDebugging() {
    debug = true;
  }

  // accept gets the next character from the source program.

  private void accept() {

    currentChar = sourceFile.getNextChar();

  // you may save the lexeme of the current token incrementally here
  // you may also increment your line and column counters here
  }

  // inspectChar returns the n-th character after currentChar
  // in the input stream. 
  //
  // If there are fewer than nthChar characters between currentChar 
  // and the end of file marker, SourceFile.eof is returned.
  // 
  // Both currentChar and the current position in the input stream
  // are *not* changed. Therefore, a subsequent call to accept()
  // will always return the next char after currentChar.

  private char inspectChar(int nthChar) {
    return sourceFile.inspectChar(nthChar);
  }

   private int nextToken() {
   // Tokens: separators, operators, literals, identifiers and keyworods
       
      switch (currentChar) {
          // separators 
         case '(':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.LPAREN;
	      case ')':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.RPAREN;
	      case '{':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.LCURLY;
	      case '}':
	         accept();   
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.RCURLY;
	      case '[':   
	       	accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.LBRACKET;
	      case ']':   
	       	accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.RBRACKET;
	      case ';':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.SEMICOLON;
	      // Operators
	      case '*':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.MULT;
	      case '/':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.DIV;
	      case '-':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.MINUS;
	      case '+':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.PLUS;
	      case ',':
	         accept();
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.COMMA;
	      case '<':
	         accept();
      	   if (currentChar == '=') {
      	      currentSpelling.append(currentChar);
               accept();
               endOfStringColumnNumber++;
	            sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.LTEQ;
      	   } else {
      	      sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.LT;
            }
         case '>':
            accept();
      	   if (currentChar == '=') {
      	      currentSpelling.append(currentChar);
               accept();
               endOfStringColumnNumber++;
	            sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.GTEQ;
      	   } else {
      	      sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.GT;
            }
         case '!': 
            accept();
               if (currentChar == '=') {
      	      currentSpelling.append(currentChar);
               accept();
               endOfStringColumnNumber++;
	            sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.NOTEQ;
      	   } else {
      	      sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.NOT;
            }
         case '"':
            // We need to find the end of the string. 
            // System.out.println(startOfStringColumnNumber);
            int savedLineNumber = lineNumber;
            int savedCharNumber = startOfStringColumnNumber;
            accept();
            while (currentChar != '"'){
               if (currentChar == '\\' && inspectChar(1) == '\\'){
                  // accept both and move forward by two.
                  currentSpelling.append('\\');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;
               } else if (currentChar == '\\' && inspectChar(1) == '"'){
                  // accept both and move forward by two.
                  currentSpelling.append('"');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;
               } else if (currentChar == '\\' && inspectChar(1) == '\''){
                  currentSpelling.append('\'');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;               
               } else if (currentChar == '\\' && inspectChar(1) == 'f'){
                  currentSpelling.append('\f');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;  
               } else if (currentChar == '\\' && inspectChar(1) == 'b'){
                  currentSpelling.append('\b');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;                 
               } else if (currentChar == '\\' && inspectChar(1) == 'r'){
                  currentSpelling.append('\r');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;                
               } else if (currentChar == '\\' && inspectChar(1) == 'n'){
                  currentSpelling.append('\n');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;                
               } else if (currentChar == '\\' && inspectChar(1) == 't'){
                  currentSpelling.append('\t');
                  accept();
                  endOfStringColumnNumber++;
                  accept();
                  endOfStringColumnNumber++;  
               } else if (currentChar == '\\'){
                  currentSpelling.append('\\');
                  accept();
                  endOfStringColumnNumber++;
                  errorReporter.reportError("%: illegal escape character", "\\" + currentChar, new SourcePosition(savedLineNumber, savedCharNumber, endOfStringColumnNumber));
                  currentSpelling.append(currentChar);
                  accept();
                  endOfStringColumnNumber++;  
               } else if (currentChar == '\n'){
                  String tempString = currentSpelling.toString().substring(1);
                  errorReporter.reportError("%: unterminated string", tempString, new SourcePosition(savedLineNumber, savedCharNumber, savedCharNumber));
                  break;
               } else {
                  currentSpelling.append(currentChar);
                  accept();
                  endOfStringColumnNumber++;
               }
            }
            
            
            if (currentChar == '"'){
               // At this point currentChar equals = "
               currentSpelling.append(currentChar);
               accept();
               endOfStringColumnNumber++;
               sourcePos.charFinish = endOfStringColumnNumber;
               // Some Extra work has to be done, need to remove the start
               // and end ". Ideally in this case the string should start and end with "
               currentSpelling.deleteCharAt(0);
               currentSpelling.deleteCharAt(currentSpelling.length()-1);
            } else if (currentChar == '\n'){
               // This is the case of unterminated String
               currentSpelling.append(currentChar);
               accept();
               sourcePos.charFinish = endOfStringColumnNumber;
               // Some Extra work has to be done, need to remove the start
               // and end ". Ideally in this case the string should start and end with "
               currentSpelling.deleteCharAt(0);
               currentSpelling.deleteCharAt(currentSpelling.length()-1);
               
               // Need to set to 0 because at the end of getToken function
               // we ++ to the counters anyway. 
               lineNumber++;
               endOfStringColumnNumber = 0;
               startOfStringColumnNumber = 0;
            }
            
            // At this point we want check if there are anymore \'s in the string
            
            /*
            String currentWord = currentSpelling.toString();
            // ArrayList<Integer> positions = new ArrayList<Integer>();
            
            for (int i = 0; i < currentWord.length(); i++){

               if (currentWord.charAt(i) == '\\' ){
                  if (i+1 < currentWord.length()){
                     errorReporter.reportError("% : illegal escape character", "\\" + currentWord.charAt(i+1), new SourcePosition(savedLineNumber, startOfStringColumnNumber, startOfStringColumnNumber+i+1));
                  } else {
                     errorReporter.reportError("% : illegal escape character", "\\" + currentWord.charAt(i), new SourcePosition(savedLineNumber, startOfStringColumnNumber, startOfStringColumnNumber+i));
                  }
               

               }
            }
            */
            
            
            return Token.STRINGLITERAL;
         case '0':
         case '1':
         case '2':
         case '3':
         case '4':
         case '5':
         case '6':
         case '7':
         case '8':
         case '9':
         case '.':
           
            int i = 1;
            StringBuffer tempString = new StringBuffer("");
            tempString.append(currentChar);
            while (inspectChar(i) != '\n' && inspectChar(i) != ' ' && inspectChar(i) != '\t'){
               tempString.append(inspectChar(i));
               i++;
            }
            // System.out.println(tempString.toString());
            String str = tempString.toString();
            // System.out.println(str.matches("[0-9]+.[0-9]+[eE][+-][0-9]+"));
            boolean result1 = str.matches("[0-9]+.[0-9]+[eE][+-][0-9]+");
            boolean result2 = str.matches("[0-9]+[eE][0-9]+");
            boolean result3 = str.matches(".[0-9]+[eE][0-9]+");
            boolean result4 = str.matches("[0-9]+.[eE][0-9]+");
            boolean result5 = str.matches("[0-9]+.[0-9]+[eE][0-9]+");
            boolean result6 = str.matches("[0-9]+[eE][+-][0-9]+");
            boolean result7 = str.matches(".[0-9]+[eE][+-][0-9]+");
            boolean result8 = str.matches("[0-9].+[eE][+-][0-9]+");
            
            if (result1 || result2 || result3 || result4 || result5 || result6 || result7 || result8){
               // Must be a float:
         
               //  attempting to recognise a float
               accept();
               while (currentChar == '0' || currentChar == '1' || currentChar == '2' || currentChar == '3' || currentChar == '4' || currentChar == '5'
                        || currentChar == '6' || currentChar == '7' || currentChar == '8' || currentChar == '9' || currentChar == '+' || currentChar == '-'
                        || currentChar == 'e' || currentChar == 'E' || currentChar == '.'){
                  currentSpelling.append(currentChar);   
                  accept();
                  endOfStringColumnNumber++;         
               }
            
               sourcePos.charFinish = endOfStringColumnNumber;
            
               if (currentSpelling.indexOf(".") != -1 || currentSpelling.indexOf("e") != -1 || currentSpelling.indexOf("E") != -1) {    // insert E meme here
	               return Token.FLOATLITERAL;
	            } else {
	               return Token.INTLITERAL;
	            }
            
            } else {
               // Must be an int.
               accept();
               while (currentChar == '0' || currentChar == '1' || currentChar == '2' || currentChar == '3' || currentChar == '4' || currentChar == '5'
                        || currentChar == '6' || currentChar == '7' || currentChar == '8' || currentChar == '9' || currentChar == '.'){
                  currentSpelling.append(currentChar);   
                  accept();
                  endOfStringColumnNumber++;         
               }
               
               sourcePos.charFinish = endOfStringColumnNumber;
               if (currentSpelling.indexOf(".") != -1) {    // insert E meme here
	               return Token.FLOATLITERAL;
	            } else {
	               return Token.INTLITERAL;
	            }
            }
            

            
         case '|':	
       	   accept();
      	   if (currentChar == '|') {
      	      currentSpelling.append(currentChar);
               accept();
               endOfStringColumnNumber++;
	            sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.OROR;
      	   } else {
      	      sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.ERROR;
            }
         case '&':
            accept();
      	   if (currentChar == '&') {
      	      currentSpelling.append(currentChar);
               accept();
               endOfStringColumnNumber++;
	            sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.ANDAND;
      	   } else {
      	      sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.ERROR;
            }
         case '=':
            accept();
      	   if (currentChar == '=') {
      	      currentSpelling.append(currentChar);
               accept();
               endOfStringColumnNumber++;
	            sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.EQEQ;
      	   } else {
      	      sourcePos.charFinish = endOfStringColumnNumber;
	            return Token.EQ;
            }
            // ....
         case SourceFile.eof:	
	         currentSpelling.append(Token.spell(Token.EOF));
	         sourcePos.charFinish = endOfStringColumnNumber;
	         return Token.EOF;
         default:
            char nextChar = inspectChar(1);
            while (nextChar != ' ' && nextChar != ';' && nextChar != ',' && nextChar != '\n' && nextChar != '\t' && nextChar != SourceFile.eof 
                     && nextChar != '(' && nextChar != ')' && nextChar != '{' && nextChar != '}' && nextChar != '[' && inspectChar(1) != ']'
                     && nextChar != '=' && nextChar != '+' && nextChar != '-' && nextChar != '*' && nextChar != '/'
                     && nextChar != '<' && nextChar != '>'){
               accept();
               nextChar = inspectChar(1);
               endOfStringColumnNumber++;
               currentSpelling.append(currentChar);
            }
            sourcePos.charFinish = endOfStringColumnNumber;
            startOfStringColumnNumber = endOfStringColumnNumber;
	         break;
      }


      // Key Words
      if (currentSpelling.toString().equals("boolean")){
         accept();
         return Token.BOOLEAN;
      } else if (currentSpelling.toString().equals("break")){
         accept();
         return Token.BREAK;
      } else if (currentSpelling.toString().equals("continue")){
         accept();
         return Token.CONTINUE;
      } else if (currentSpelling.toString().equals("else")){
         accept();
         return Token.ELSE;
      } else if (currentSpelling.toString().equals("float")){
         accept();
         return Token.FLOAT;
      } else if (currentSpelling.toString().equals("for")){
         accept();
         return Token.FOR;
      } else if (currentSpelling.toString().equals("if")){
         accept();
         return Token.IF;
      } else if (currentSpelling.toString().equals("int")){
         accept();
         return Token.INT;
      } else if (currentSpelling.toString().equals("return")){
         accept();
         return Token.RETURN;
      } else if (currentSpelling.toString().equals("void")){
         accept();
         return Token.VOID;
      } else if (currentSpelling.toString().equals("while")){
         accept();
         return Token.WHILE;
      }
   
      // Check if it is a bool-literal
      if(IsStringABooleanLiteral(currentSpelling.toString())){
         accept();
         return Token.BOOLEANLITERAL;
      }
   
      // Check if it is an Int-literal
      if (IsStringAnIntLiteral(currentSpelling.toString())){
         accept();
         return Token.INTLITERAL;
      }
      
      if (StringContainsOnlyChars(currentSpelling.toString())){
         accept();
         return Token.ID;
      }
      
   
      accept(); 
      return Token.ERROR;
   }

  void skipSpaceAndComments() {
      //Skip Space
      if (currentChar == ' '){
         endOfStringColumnNumber++;
         startOfStringColumnNumber = endOfStringColumnNumber;
         accept();
      }
  }
  
  void skipNewLines(){
      if (currentChar == '\n'){
         accept();
         lineNumber++;
         endOfStringColumnNumber = 1;
         startOfStringColumnNumber = 1;
      }
  }
  
  void skipLineComments(){
      if (currentChar == '/' && inspectChar(1) == '/'){
         while(currentChar != '\n'){
            accept();
         }
      } 
  }
  
  void skipBlockComments(){
      
      if (currentChar == '/' && inspectChar(1) == '*'){
         int savedLineNumber = lineNumber;
         int savedCharNumber = endOfStringColumnNumber;
      
         // first we move the current Char forward by two.
         accept();
         accept();
         endOfStringColumnNumber++;
         endOfStringColumnNumber++;
         startOfStringColumnNumber = endOfStringColumnNumber;
         
         // Now we seek for the */
         while (!(currentChar == '*' && inspectChar(1) == '/')){
            if(currentChar == '\n'){
               accept();
               lineNumber++;
               endOfStringColumnNumber = 1;
               startOfStringColumnNumber = 1;
            } else {
               accept();
               endOfStringColumnNumber++;
               startOfStringColumnNumber = endOfStringColumnNumber;
            }
            
            // If we hit a EOF then we must break
            int ascii = (int) currentChar;
            if (ascii == 0){
               //lineNumber++;
               startOfStringColumnNumber = 1;
               endOfStringColumnNumber = 1;
               errorReporter.reportError(": unterminated comment", "*wtf is this?", new SourcePosition(savedLineNumber, savedCharNumber, savedCharNumber));
               return;
            }
         }
         
         // At this point we should be at the end of the block comment. 
         // We will need to move forward by two again.
         accept();
         accept();
         endOfStringColumnNumber++;
         endOfStringColumnNumber++;
         startOfStringColumnNumber = endOfStringColumnNumber;
      }
  }
  
  // This is a horizontal tab
   public void skipHorizontalTabs(){
      if (currentChar == '\t'){
         accept();
         while (endOfStringColumnNumber % 8 != 0){
            endOfStringColumnNumber++;
         }
         endOfStringColumnNumber++;
         // endOfStringColumnNumber += 8;
         startOfStringColumnNumber = endOfStringColumnNumber;
      }
   }
   
   public void skipVerticalTabs(){
      if ((int) currentChar == 11){
         accept();
         endOfStringColumnNumber += 8;
         startOfStringColumnNumber = endOfStringColumnNumber;
      }
   }

   public Token getToken() {
      Token tok;
      int kind;

      // skip white space and comments
      while (currentChar == ' ' || (currentChar == '/' && inspectChar(1) == '/') || currentChar == '\n' || (currentChar == '/' && inspectChar(1) == '*') || currentChar == '\t'){

         // System.out.println("Start : " + startOfStringColumnNumber);
         skipSpaceAndComments();
         skipNewLines();
         skipLineComments();
         skipBlockComments();
         // System.out.println("Finish : " + startOfStringColumnNumber);
         skipHorizontalTabs();
         
      }
      // System.out.println(lineNumber);
      // System.out.println(startOfStringColumnNumber);
      // From this point onwards char should be the first letter of the word

      // System.out.println((int) currentChar);
      currentSpelling = new StringBuffer("");
      if (currentChar != '\0'){
         currentSpelling.append(currentChar);
      }
      sourcePos = new SourcePosition();
      sourcePos.charStart = startOfStringColumnNumber;
      sourcePos.lineStart = lineNumber;
      sourcePos.lineFinish = lineNumber;
      // You must record the position of the current token somehow
   
      kind = nextToken();

      tok = new Token(kind, currentSpelling.toString(), sourcePos);


      startOfStringColumnNumber = endOfStringColumnNumber + 1;
      endOfStringColumnNumber = startOfStringColumnNumber;
      
      
      
      // * do not remove these three lines
      if (debug)
         System.out.println(tok);
      return tok;
   }
   
   
   public boolean IsStringAnIntLiteral(String s){
      for (int i = 0; i < s.length(); i++){
         if (!Character.isDigit(s.charAt(i))){
            return false;
         }
      }
      return true;
   }
   
   public boolean IsStringABooleanLiteral(String s){
      if (s.equals("true") || s.equals("false")){
         return true;
      }
      return false;
   }
   
   public boolean StringContainsOnlyChars(String s){
      for (int i = 0; i < s.length(); i++){
         if (!Character.isLetter(s.charAt(i))){
            return false;
         }
      }
      return true;
   }
}
