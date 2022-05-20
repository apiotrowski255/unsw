/***
 * *
 * * Recogniser.java            
 * *
 ***/

/* At this stage, this parser accepts a subset of VC defined	by
 * the following grammar. 
 *
 * You need to modify the supplied parsing methods (if necessary) and 
 * add the missing ones to obtain a parser for the VC language.
 *
 * [25 Feb 2021]

program       -> func-decl

// declaration

func-decl     -> void identifier "(" ")" compound-stmt

identifier    -> ID

// statements 
compound-stmt -> "{" stmt* "}" 
stmt          -> continue-stmt
    	      |  expr-stmt
continue-stmt -> continue ";"
expr-stmt     -> expr? ";"

// expressions 
expr                -> assignment-expr
assignment-expr     -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
*/

package VC.Recogniser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;

public class Recogniser {

  private Scanner scanner;
  private ErrorReporter errorReporter;
  private Token currentToken, tempToken;

  public Recogniser (Scanner lexer, ErrorReporter reporter) {
    scanner = lexer;
    errorReporter = reporter;

    currentToken = scanner.getToken();
  }

// match checks to see f the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

  void match(int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      currentToken = scanner.getToken();
    } else {
      // System.out.println("DEBUG currentToken is: " + Token.spell(currentToken.kind));
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }

 // accepts the current token and fetches the next
  void accept() {
    currentToken = scanner.getToken();
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.reportError(messageTemplate, tokenQuoted, pos);
    throw(new SyntaxError());
  }


// ========================== PROGRAMS ========================

  public void parseProgram() {

    try {

      while (currentToken.kind != Token.EOF){
        if (funcParse()){
          startParseFuncDecl();
        } else {
          start_var_decl();
        }
      }
      
      if (currentToken.kind != Token.EOF) {
        syntacticError("\"%\" wrong result type for a function", currentToken.spelling);
      }
    }
    catch (SyntaxError s) {  }
  }

// Determine whether function decl or var decl will have to be called
// returns true if function, false if variable
  boolean funcParse() throws SyntaxError {
    type();
    parseIdent();
    if (currentToken.kind == Token.LPAREN){
      return true;
    } else {
      return false;
    }
  }

  void startParseFuncDecl() throws SyntaxError {
    para_list();
    parseCompoundStmt();
  }
  
  void start_var_decl() throws SyntaxError {
    start_init_decl_list();
    match(Token.SEMICOLON);
  }
  
  void start_init_decl_list() throws SyntaxError {
    if (currentToken.kind == Token.LBRACKET){
      match(Token.LBRACKET);
      if (currentToken.kind == Token.INTLITERAL){
        match(Token.INTLITERAL);
      }
      match(Token.RBRACKET);
    }
    
    if (currentToken.kind == Token.EQ){
      match(Token.EQ);
      init();
    }
    
    if (currentToken.kind == Token.COMMA){
      do {
        match(Token.COMMA);
        init_decl();
      } while (currentToken.kind == Token.COMMA);
    }
  }

// ========================== DECLARATIONS ========================

  void parseFuncDecl() throws SyntaxError {

    type();
    parseIdent();
    para_list();
    parseCompoundStmt();
  }
  
  void var_decl() throws SyntaxError {
    type();
    init_decl_list();
    match(Token.SEMICOLON);
  }
  
  void init_decl_list() throws SyntaxError {
    init_decl();
    if (currentToken.kind == Token.COMMA){
      do {
        match(Token.COMMA);
        init_decl();
      } while (currentToken.kind == Token.COMMA);
    }
  }
  
  void init_decl() throws SyntaxError {
    decl();
    if (currentToken.kind == Token.EQ){
      match(Token.EQ);
      init();
    }
  }
  
  void decl() throws SyntaxError {
    match(Token.ID);
    if (currentToken.kind == Token.LBRACKET){
      match(Token.LBRACKET);
      if (currentToken.kind == Token.INTLITERAL){
        match(Token.INTLITERAL);
      }
      match(Token.RBRACKET);
    }
  }
  
  
  // initialiser
  void init() throws SyntaxError {
    if (currentToken.kind == Token.LCURLY){
      match(Token.LCURLY);
      parseExpr();
      if (currentToken.kind == Token.COMMA){
        do {
          match(Token.COMMA);
          parseExpr();
        } while (currentToken.kind == Token.COMMA);
      }
      match(Token.RCURLY);
    } else {
      parseExpr();
    }
  }

// ======================= STATEMENTS ==============================


  void parseCompoundStmt() throws SyntaxError {
    
    match(Token.LCURLY);

    while (currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT || currentToken.kind == Token.BOOLEAN) {
      var_decl();
    }
    parseStmtList();
    match(Token.RCURLY);
  }

 // Here, a new nontermial has been introduced to define { stmt } *
  void parseStmtList() throws SyntaxError {

    while (currentToken.kind != Token.RCURLY) 
      parseStmt();
  }

  void parseStmt() throws SyntaxError {

    switch (currentToken.kind) {
      case Token.LCURLY:
         // This is compound statement
         parseCompoundStmt();
         break;
      case Token.IF:
         parseIfStmt();
         break;
      case Token.FOR:
         parseForStmt();
         break;
      case Token.WHILE:
         parseWhileStmt();
         break;
      case Token.BREAK:
         parseBreakStmt();
         break;
      case Token.CONTINUE:
         parseContinueStmt();
         break;
      case Token.RETURN:
         parseReturnStmt();
         break;
      default:
         parseExprStmt();
         break;

    }
  }
   
   void parseWhileStmt() throws SyntaxError {
      match(Token.WHILE);
      match(Token.LPAREN);
      parseExpr();
      match(Token.RPAREN);
      parseStmt();
   }
   
   void parseForStmt() throws SyntaxError {
      match(Token.FOR);
      match(Token.LPAREN);
      if (currentToken.kind != Token.SEMICOLON){
        parseExpr();
      }
      match(Token.SEMICOLON);
      if (currentToken.kind != Token.SEMICOLON){
        parseExpr();
      }
      match(Token.SEMICOLON);
      if (currentToken.kind != Token.SEMICOLON){
        parseExpr();
      }
      match(Token.RPAREN);
      parseStmt();
   }
   
   void parseIfStmt() throws SyntaxError {
      match(Token.IF);
      match(Token.LPAREN);
      parseExpr();
      match(Token.RPAREN);
      parseStmt();
      if (currentToken.kind == Token.ELSE){
        match(Token.ELSE);
        parseStmt();
      }
   }

   void parseReturnStmt() throws SyntaxError {
      match(Token.RETURN);
      // optional expression
      if (currentToken.kind != Token.SEMICOLON){
         parseExpr();
      }
      match(Token.SEMICOLON);
   }
   
   void parseBreakStmt() throws SyntaxError {
      match(Token.BREAK);
      match(Token.SEMICOLON);
   }

  void parseContinueStmt() throws SyntaxError {

    match(Token.CONTINUE);
    match(Token.SEMICOLON);

  }

  void parseExprStmt() throws SyntaxError {

    if (currentToken.kind != Token.SEMICOLON){
      parseExpr();
    } 
    match(Token.SEMICOLON);
    
    /*
    if (currentToken.kind == Token.ID
        || currentToken.kind == Token.INTLITERAL
        || currentToken.kind == Token.MINUS
        || currentToken.kind == Token.LPAREN) {
        parseExpr();
        match(Token.SEMICOLON);
    } else {
      match(Token.SEMICOLON);
    }
    */
  }


// ======================= IDENTIFIERS ======================

 // Call parseIdent rather than match(Token.ID). 
 // In Assignment 3, an Identifier node will be constructed in here.


  void parseIdent() throws SyntaxError {

    if (currentToken.kind == Token.ID) {
      currentToken = scanner.getToken();
      
    } else {
      // System.out.println("PARSING ID");
      // System.out.println("DEBUG: currentToken is: " + Token.spell(currentToken.kind));
      syntacticError("identifier expected here", "");
    }
  }

// ======================= OPERATORS ======================

 // Call acceptOperator rather than accept(). 
 // In Assignment 3, an Operator Node will be constructed in here.

  void acceptOperator() throws SyntaxError {

    currentToken = scanner.getToken();
  }


// ======================= EXPRESSIONS ======================

  void parseExpr() throws SyntaxError {
    parseAssignExpr();
  }


  void parseAssignExpr() throws SyntaxError {

    cond_or_expr();
    while (currentToken.kind == Token.EQ){
      match(Token.EQ);
      cond_or_expr();
    }
  }
  
  
  // cond-or-expr        -> cond-and-expr 
  //                  |  cond-or-expr "||" cond-and-expr
  void cond_or_expr() throws SyntaxError {
    cond_and_expr();
    while (currentToken.kind == Token.OROR){
      match(Token.OROR);
      cond_and_expr();
    }
  }
  
  // cond-and-expr       -> equality-expr 
  //                  |  cond-and-expr "&&" equality-expr
  void cond_and_expr() throws SyntaxError {
    equality_expr();
    while (currentToken.kind == Token.ANDAND){
      match(Token.ANDAND);
      equality_expr();
    }
  }
  
  // equality-expr       -> rel-expr
  //                  |  equality-expr "==" rel-expr
  //                  |  equality-expr "!=" rel-expr
  void equality_expr() throws SyntaxError {
    rel_expr();
    while (currentToken.kind == Token.EQEQ || currentToken.kind == Token.NOTEQ){
      match(currentToken.kind);
      rel_expr();
    }
  }
  
  // rel-expr            -> additive-expr
  //                  |  rel-expr "<" additive-expr
  //                  |  rel-expr "<=" additive-expr
  //                  |  rel-expr ">" additive-expr
  //                  |  rel-expr ">=" additive-expr
  
  void rel_expr() throws SyntaxError {
    parseAdditiveExpr();
    while (currentToken.kind == Token.LT || currentToken.kind == Token.LTEQ || currentToken.kind == Token.GT || currentToken.kind == Token.GTEQ){
      match(currentToken.kind);
      parseAdditiveExpr();
    }
  }

  void parseAdditiveExpr() throws SyntaxError {

    parseMultiplicativeExpr();
    while (currentToken.kind == Token.PLUS || currentToken.kind == Token.MINUS) {
      acceptOperator();
      parseMultiplicativeExpr();
    }
  }

  void parseMultiplicativeExpr() throws SyntaxError {

    parseUnaryExpr();
    while (currentToken.kind == Token.MULT || currentToken.kind == Token.DIV) {
      acceptOperator();
      parseUnaryExpr();
    }
  }

  void parseUnaryExpr() throws SyntaxError {

    switch (currentToken.kind) {
      case Token.PLUS:
         acceptOperator();
         parseUnaryExpr();
         break;
      case Token.MINUS:
        {
          acceptOperator();
          parseUnaryExpr();
        }
        break;
      case Token.NOT:
         acceptOperator();
         parseUnaryExpr();
         break;
      default:
        parsePrimaryExpr();
        break;
       
    }
  }

  void parsePrimaryExpr() throws SyntaxError {

    switch (currentToken.kind) {

      case Token.ID:
        
        parseIdent();
        if (currentToken.kind == Token.LBRACKET){
            match(Token.LBRACKET);
            parseExpr();
            match(Token.RBRACKET);
        } else if (currentToken.kind == Token.LPAREN){
            arg_list();
        }   
        break;
      case Token.LPAREN:
          match(Token.LPAREN);
          parseExpr();
	       match(Token.RPAREN);
          break;
      case Token.INTLITERAL:
        parseIntLiteral();
        break;
      case Token.FLOATLITERAL:
         parseFloatLiteral();
         break;
      case Token.BOOLEANLITERAL:
         parseBooleanLiteral();
         break;
      case Token.STRINGLITERAL:
         parseStringLiteral();
         break;
      default:
        match(Token.SEMICOLON);
        syntacticError("illegal parimary expression", currentToken.spelling);
       
    }
  }

// ========================== PARAMETERS ==========================
   void para_list() throws SyntaxError {
      
      match(Token.LPAREN);
      if (currentToken.kind != Token.RPAREN){
        proper_para_list();
      }
      match(Token.RPAREN);
   }
   
   void proper_para_list() throws SyntaxError {
      para_decl();
      if (currentToken.kind == Token.COMMA){
        do {
          match(Token.COMMA);
          para_decl();
        } while (currentToken.kind == Token.COMMA);
      }
   }
   
   void para_decl() throws SyntaxError {
      type();
      declarator();
   }
   
   void arg_list() throws SyntaxError {
    match(Token.LPAREN);
    if (currentToken.kind != Token.RPAREN){
      proper_arg_list();
    } 
    match(Token.RPAREN);
   }

  void proper_arg_list() throws SyntaxError {
    arg();
    if (currentToken.kind == Token.COMMA){
      do {
        match(Token.COMMA);
        arg();
      } while (currentToken.kind == Token.COMMA);
    }
  }
  
  void arg() throws SyntaxError {
    parseExpr();
  }

// ========================== DECLARATIONS ==========================
   
   void init_declarator() throws SyntaxError {
      declarator();
      if (currentToken.kind == Token.EQ){
         match(Token.EQ);
         initialiser();
      }
   }
   
   void declarator() throws SyntaxError {
      match(Token.ID);
      if (currentToken.kind == Token.LBRACKET){
         match(Token.LBRACKET);
         if (currentToken.kind == Token.INTLITERAL){
            match(Token.INTLITERAL);
         }
         match(Token.RBRACKET);
      }
   }
   
   void initialiser() throws SyntaxError {
      if (currentToken.kind == Token.LCURLY){
         match(Token.LCURLY);
         parseExpr();
         if (currentToken.kind == Token.COMMA){
            do {
               match(Token.COMMA);
               parseExpr();
            } while (currentToken.kind == Token.COMMA);
         }
         match(Token.RCURLY);
      } else {
         parseExpr();
      }
   }

// ========================== PRIMITIVE TYPES ==========================
   
   void type() throws SyntaxError {
      if (currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT){
         match(currentToken.kind);
      } else {
         syntacticError("\"%\" wrong result type for a function", currentToken.spelling);
      }
   }

// ========================== LITERALS ========================

  // Call these methods rather than accept().  In Assignment 3, 
  // literal AST nodes will be constructed inside these methods. 

  void parseIntLiteral() throws SyntaxError {

    if (currentToken.kind == Token.INTLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("integer literal expected here", "");
  }

  void parseFloatLiteral() throws SyntaxError {

    if (currentToken.kind == Token.FLOATLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("float literal expected here", "");
  }

  void parseBooleanLiteral() throws SyntaxError {

    if (currentToken.kind == Token.BOOLEANLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("boolean literal expected here", "");
  }
  
   void parseStringLiteral() throws SyntaxError {

    if (currentToken.kind == Token.STRINGLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("string literal expected here", "");
  }

}
