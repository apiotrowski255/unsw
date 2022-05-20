/*
 * Parser.java            
 *
 * This parser for a subset of the VC language is intended to 
 *  demonstrate how to create the AST nodes, including (among others): 
 *  [1] a list (of statements)
 *  [2] a function
 *  [3] a statement (which is an expression statement), 
 *  [4] a unary expression
 *  [5] a binary expression
 *  [6] terminals (identifiers, integer literals and operators)
 *
 * In addition, it also demonstrates how to use the two methods start 
 * and finish to determine the position information for the start and 
 * end of a construct (known as a phrase) corresponding an AST node.
 *
 * NOTE THAT THE POSITION INFORMATION WILL NOT BE MARKED. HOWEVER, IT CAN BE
 * USEFUL TO DEBUG YOUR IMPLEMENTATION.
 *
 * --- 4 March 2021 --- 


program       -> func-decl
func-decl     -> type identifier "(" ")" compound-stmt
type          -> void
identifier    -> ID
// statements
compound-stmt -> "{" stmt* "}" 
stmt          -> expr-stmt
expr-stmt     -> expr? ";"
// expressions 
expr                -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
                    |  additive-expr "-" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
	            |  multiplicative-expr "/" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
 */

package VC.Parser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;
import VC.ASTs.*;

public class Parser {

  private Scanner scanner;
  private ErrorReporter errorReporter;
  private Token currentToken;
  private SourcePosition previousTokenPosition;
  private SourcePosition dummyPos = new SourcePosition();
  private Type previousType;

  public Parser (Scanner lexer, ErrorReporter reporter) {
    scanner = lexer;
    errorReporter = reporter;

    previousTokenPosition = new SourcePosition();

    currentToken = scanner.getToken();
    this.previousType = null;
  }

// match checks to see f the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

  void match(int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      previousTokenPosition = currentToken.position;
      currentToken = scanner.getToken();
    } else {
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }

  void accept() {
    previousTokenPosition = currentToken.position;
    currentToken = scanner.getToken();
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.reportError(messageTemplate, tokenQuoted, pos);
    throw(new SyntaxError());
  }

// start records the position of the start of a phrase.
// This is defined to be the position of the first
// character of the first token of the phrase.

  void start(SourcePosition position) {
    position.lineStart = currentToken.position.lineStart;
    position.charStart = currentToken.position.charStart;
  }

// finish records the position of the end of a phrase.
// This is defined to be the position of the last
// character of the last token of the phrase.

  void finish(SourcePosition position) {
    position.lineFinish = previousTokenPosition.lineFinish;
    position.charFinish = previousTokenPosition.charFinish;
  }

  void copyStart(SourcePosition from, SourcePosition to) {
    to.lineStart = from.lineStart;
    to.charStart = from.charStart;
  }

// ========================== PROGRAMS ========================

  public Program parseProgram() {

    Program programAST = null;
    
    SourcePosition programPos = new SourcePosition();
    start(programPos);

    try {
      List dlAST = parseFuncDeclList();
      finish(programPos);
      programAST = new Program(dlAST, programPos); 
      if (currentToken.kind != Token.EOF) {
        syntacticError("\"%\" unknown type", currentToken.spelling);
      }
    }
    catch (SyntaxError s) { return null; }
    return programAST;
  }

// ========================== DECLARATIONS ========================

  List parseFuncDeclList() throws SyntaxError {
    List dlAST = null;
    Decl dAST = null;

    SourcePosition funcPos = new SourcePosition();
    start(funcPos);
    if (currentToken.kind != Token.EOF) {
      dAST = parseFuncDecl();
    
      if (currentToken.kind == Token.VOID || currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT || currentToken.kind == Token.BOOLEAN) {
        dlAST = parseFuncDeclList();
        finish(funcPos);
        dlAST = new DeclList(dAST, dlAST, funcPos);
      } else if (currentToken.kind == Token.COMMA) { 
        match(Token.COMMA);
        dlAST = parseFuncDeclList();
        finish(funcPos);
        dlAST = new DeclList(dAST, dlAST, funcPos);
      } else if (currentToken.kind == Token.SEMICOLON) {
        match(Token.SEMICOLON);
        if (currentToken.kind == Token.VOID || currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT || currentToken.kind == Token.BOOLEAN) {
          dlAST = parseFuncDeclList();
          finish(funcPos);
          dlAST = new DeclList(dAST, dlAST, funcPos);
        } else {
          finish(funcPos);
          dlAST = new DeclList(dAST, new EmptyDeclList(dummyPos), funcPos);
        }
      } else if (dAST != null) {
        finish(funcPos);
        dlAST = new DeclList(dAST, new EmptyDeclList(dummyPos), funcPos);
      }
    }
    if (dlAST == null) 
      dlAST = new EmptyDeclList(dummyPos);

    return dlAST;
  }

  Decl parseFuncDecl() throws SyntaxError {

    Decl fAST = null; 
    
    SourcePosition funcPos = new SourcePosition();
    start(funcPos);
    Type tAST = null;
    if (currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.VOID) {
      tAST = parseType();
      previousType = tAST;
    } else {
      tAST = previousType;
    }
    Ident iAST = parseIdent();
    if (currentToken.kind == Token.LPAREN){
      List fplAST = parseParaList();
      Stmt cAST = parseCompoundStmt();
      finish(funcPos);
      fAST = new FuncDecl(tAST, iAST, fplAST, cAST, funcPos);
    } else {
      // Global function declation here
      // Handles array variables
      if (currentToken.kind == Token.LBRACKET){
       match(Token.LBRACKET);
        Expr intExpr = null;
        if (currentToken.kind == Token.INTLITERAL){
          intExpr = parseExpr();
        } else {
          intExpr = new EmptyExpr(funcPos);
        }
        match(Token.RBRACKET);
        finish(funcPos);
        tAST = new ArrayType(tAST, intExpr, funcPos);
      }
      
      Expr eAST = null;
      if (currentToken.kind == Token.EQ) {
        match(Token.EQ);
        if (currentToken.kind == Token.LCURLY){
          match(Token.LCURLY);
          List elist = parseArray();
          match(Token.RCURLY);
          eAST = new InitExpr(elist, funcPos);
        } else {
          eAST = parseExpr();
        }
      } else {
        eAST = new EmptyExpr(funcPos);
      }
      
      finish(funcPos);
      fAST = new GlobalVarDecl(tAST, iAST, eAST, funcPos);
    }
    return fAST;
  }

//  ======================== TYPES ==========================

  Type parseType() throws SyntaxError {
    Type typeAST = null;

    SourcePosition typePos = new SourcePosition();
    start(typePos);
    int functionType = currentToken.kind;
    accept();
    finish(typePos);
    if (functionType == Token.VOID) {
      typeAST = new VoidType(typePos);
    } else if (functionType == Token.INT) {
      typeAST = new IntType(typePos);
    } else if (functionType == Token.FLOAT) {
      typeAST = new FloatType(typePos);
    } else if (functionType == Token.BOOLEAN) {
      typeAST = new BooleanType(typePos);
    } else {
      syntacticError("\"%\" unknown type - Issue in parseType function", currentToken.spelling);
    }

    return typeAST;
    }

// ======================= STATEMENTS ==============================

  Stmt parseCompoundStmt() throws SyntaxError {
    Stmt cAST = null; 

    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);

    match(Token.LCURLY);

    // Insert code here to build a DeclList node for variable declarations
    // var_decl*
    List dlAST = null;
    if (currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.VOID) {
      dlAST = parseDeclList();
    } else {
      dlAST = new EmptyDeclList(dummyPos);
    }
    
    // stmt*
    List slAST = parseStmtList();
    match(Token.RCURLY);
    finish(stmtPos);

    /* In the subset of the VC grammar, no variable declarations are
     * allowed. Therefore, a block is empty if it has no statements.
     */
    if (slAST instanceof EmptyStmtList && dlAST instanceof EmptyDeclList) 
      cAST = new EmptyCompStmt(stmtPos);
    else
      cAST = new CompoundStmt(dlAST, slAST, stmtPos);
    return cAST;
  }

  List parseDeclList() throws SyntaxError {
    List dAST = null;
    Decl lAST = null;
    SourcePosition pos = new SourcePosition();
    start(pos);
    lAST = parseLocalDecl();
    if (currentToken.kind == Token.SEMICOLON){
      match(Token.SEMICOLON);
      if (currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.VOID) {
         List d1AST = parseDeclList();
         finish(pos);
         dAST = new DeclList(lAST, d1AST, pos);
      } else {
         finish(pos);
         dAST = new DeclList(lAST, new EmptyDeclList(pos), pos);
      }
    } else if (currentToken.kind == Token.COMMA) {
      match(Token.COMMA);
      List d1AST = parseDeclList();
      finish(pos);
      dAST = new DeclList(lAST, d1AST, pos);
    }
    return dAST;
  }
  
  Decl parseLocalDecl() throws SyntaxError {
    Decl lAST = null;
    SourcePosition pos = new SourcePosition();
    start(pos);
    Type tAST = null;
    if (currentToken.kind == Token.INT || currentToken.kind == Token.FLOAT || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.VOID) {
      tAST = parseType();
      previousType = tAST;
    } else {
      tAST = previousType;
    }
    Ident iAST = parseIdent();
    
    if (currentToken.kind == Token.LBRACKET){
      match(Token.LBRACKET);
      Expr intExpr = null;
      if (currentToken.kind == Token.INTLITERAL){
        intExpr = parseExpr();
      } else {
        intExpr = new EmptyExpr(pos);
      }
      match(Token.RBRACKET);
      finish(pos);
      tAST = new ArrayType(tAST, intExpr, pos);
    }
    
    Expr eAST = null;
    if (currentToken.kind == Token.EQ) {
      match(Token.EQ);
      if (currentToken.kind == Token.LCURLY){
        match(Token.LCURLY);
        List elist = parseArray();
        match(Token.RCURLY);
        eAST = new InitExpr(elist, pos);
      } else {
        eAST = parseExpr();
      }
    } else {
      eAST = new EmptyExpr(pos);
    }
    
    finish(pos);
    lAST = new LocalVarDecl(tAST, iAST, eAST, pos);
    return lAST;
  }
  
  List parseArray() throws SyntaxError {
    List list = null;
    SourcePosition pos = new SourcePosition();
    start(pos);

    Expr e1 = parseExpr();
    if (currentToken.kind == Token.COMMA) {
      match(Token.COMMA);
      List l2 = parseArray();
      finish(pos);
      list = new ExprList(e1, l2, pos);
    } else {
      finish(pos);
      list = new ExprList(e1, new EmptyExprList(pos), pos);
    }
    return list;
  }
  
  List parseInitDeclList() throws SyntaxError {
    List dAST = null;
    Decl dlAST = null;
    SourcePosition pos = new SourcePosition();
    start(pos);
    
    dlAST = parseInitDecl();
    dAST = new DeclList(dlAST, new EmptyDeclList(pos), pos);
    return dAST;
  }
  
  Decl parseInitDecl() throws SyntaxError {
    Decl lAST = null;
    SourcePosition pos = new SourcePosition();
    start(pos);
    
    Type tAST = parseType();
    Ident iAST = parseIdent();
    if (currentToken.kind == Token.LBRACKET){
      Expr expr = null;
      match(Token.LBRACKET);
      if (currentToken.kind == Token.INTLITERAL){
        expr = parseExpr();
      } else {
        expr = new EmptyExpr(pos);        
      }
      match(Token.RBRACKET);
      finish(pos);
      tAST = new ArrayType(tAST, expr, pos);
    } 
    
    Expr eAST = null;
    if (currentToken.kind == Token.EQ){
      match(Token.EQ);
      if (currentToken.kind == Token.LCURLY){
        match(Token.LCURLY);
        // eAST = parseExprList();
        match(Token.RCURLY);
      } else {
        eAST = parseExpr();
      }
    } else {
      finish(pos);
      eAST = new EmptyExpr(pos);
    }
    
    lAST = new LocalVarDecl(tAST, iAST, eAST, pos);
    return lAST;
  }


  List parseStmtList() throws SyntaxError {
    List slAST = null; 

    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);

    if (currentToken.kind != Token.RCURLY) {
      Stmt sAST = parseStmt();
      {
        if (currentToken.kind != Token.RCURLY) {
          slAST = parseStmtList();
          finish(stmtPos);
          slAST = new StmtList(sAST, slAST, stmtPos);
        } else {
          finish(stmtPos);
          slAST = new StmtList(sAST, new EmptyStmtList(dummyPos), stmtPos);
        }
      }
    }
    else
      slAST = new EmptyStmtList(dummyPos);
    
    return slAST;
  }

  Stmt parseStmt() throws SyntaxError {
    Stmt sAST = null;
    switch (currentToken.kind) {
      case Token.LCURLY:
         // This is compound statement
         sAST = parseCompoundStmt();
         break;
      case Token.IF:
         sAST = parseIfStmt();
         break;
      case Token.FOR:
         sAST = parseForStmt();
         break;
      case Token.WHILE:
         sAST = parseWhileStmt();
         break;
      case Token.BREAK:
         sAST = parseBreakStmt();
         break;
      case Token.CONTINUE:
         sAST = parseContinueStmt();
         break;
      case Token.RETURN:
         sAST = parseReturnStmt();
         break;
      default:
         sAST = parseExprStmt();
         break;
    }

    return sAST;
  }

  Stmt parseExprStmt() throws SyntaxError {
    Stmt sAST = null;

    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);
    
    if (currentToken.kind != Token.SEMICOLON) {
        Expr eAST = parseExpr();
        match(Token.SEMICOLON);
        finish(stmtPos);
        sAST = new ExprStmt(eAST, stmtPos);
    } else {
      match(Token.SEMICOLON);
      finish(stmtPos);
      sAST = new ExprStmt(new EmptyExpr(dummyPos), stmtPos);
    }
    return sAST;
  }


// ======================= PARAMETERS =======================

  List parseParaList() throws SyntaxError {
    List formalsAST = null;

    SourcePosition formalsPos = new SourcePosition();
    start(formalsPos);
    
    if (currentToken.kind == Token.LPAREN) {
      match(Token.LPAREN);
    }
    if (currentToken.kind == Token.RPAREN) {
      match(Token.RPAREN);
      finish(formalsPos);
      formalsAST = new EmptyParaList (formalsPos);
    } else {
      ParaDecl pAST = parseParaDec();
      List f1AST = null;
      if (currentToken.kind == Token.COMMA) {
        match(Token.COMMA);
        f1AST = parseParaList();
        finish(formalsPos);
        formalsAST = new ParaList(pAST, f1AST, formalsPos);
      } else {
        match(Token.RPAREN);
        finish(formalsPos);
        formalsAST = new ParaList(pAST, new EmptyParaList (formalsPos), formalsPos);
      }
    }

    return formalsAST;
  }

  ParaDecl parseParaDec() throws SyntaxError {
    ParaDecl pd = null;
    SourcePosition pos = new SourcePosition();
    start(pos);
    
    Type tAST = parseType();
    Ident iAST = parseIdent();
    if (currentToken.kind == Token.LBRACKET) {
      match(Token.LBRACKET);
      Expr e1 = null;
      if (currentToken.kind == Token.INTLITERAL) {
        e1 = parseExpr();
      } else {
        e1 = new EmptyExpr(pos);
      }
      match(Token.RBRACKET);
      finish(pos);
      tAST = new ArrayType(tAST, e1, pos);
    }
    finish(pos);
    pd = new ParaDecl(tAST, iAST, pos);
    return pd;
  }

// ======================= EXPRESSIONS ======================


  Expr parseExpr() throws SyntaxError {
    Expr exprAST = null;
    exprAST = parseAssignExpr();
    return exprAST;
  }

  Expr parseAssignExpr() throws SyntaxError {
    Expr assignExpr = null;
    
    SourcePosition assignStartPos = new SourcePosition();
    start(assignStartPos);
    
    assignExpr = parseCondOrExpr();
    while (currentToken.kind == Token.EQ){
      match (Token.EQ);
      
      Expr e2AST = parseAssignExpr();
      
      SourcePosition assignPos = new SourcePosition();
      copyStart(assignStartPos, assignPos);
      finish(assignPos);
      assignExpr = new AssignExpr(assignExpr, e2AST, assignPos);
    }
    return assignExpr;
  }
  
  Expr parseCondOrExpr() throws SyntaxError {
    Expr orAST = null;
    
    SourcePosition orStartPos = new SourcePosition();
    start(orStartPos);
  
    orAST = parseCondAndExpr();
    while (currentToken.kind == Token.OROR){
      Operator opAST = acceptOperator();
      Expr e2AST = parseCondAndExpr();
      
      SourcePosition orPos = new SourcePosition();
      copyStart(orStartPos, orPos);
      finish(orPos);
      orAST = new BinaryExpr(orAST, opAST, e2AST, orPos);
    }
    return orAST;
  }

  Expr parseCondAndExpr() throws SyntaxError {
    Expr andAST = null;
    
    SourcePosition andStartPos = new SourcePosition();
    start(andStartPos);
  
    andAST = parseEqualityExpr();
    while (currentToken.kind == Token.ANDAND){
      Operator opAST = acceptOperator();
      Expr e2AST = parseEqualityExpr();
      
      SourcePosition andPos = new SourcePosition();
      copyStart(andStartPos, andPos);
      finish(andPos);
      andAST = new BinaryExpr(andAST, opAST, e2AST, andPos);
    }
    return andAST;
  }
  
  Expr parseEqualityExpr() throws SyntaxError {
    Expr eqAST = null;
    
    SourcePosition eqStartPos = new SourcePosition();
    start(eqStartPos);
    
    eqAST = parseRelExpr();
    while (currentToken.kind == Token.EQEQ || currentToken.kind == Token.NOTEQ){
      Operator opAST = acceptOperator();
      Expr e2AST = parseRelExpr();
      
      SourcePosition eqPos = new SourcePosition();
      copyStart(eqStartPos, eqPos);
      finish(eqPos);
      eqAST = new BinaryExpr(eqAST, opAST, e2AST, eqPos);
    }
    return eqAST;
  }
  
  Expr parseRelExpr() throws SyntaxError {
    Expr relAST = null;
    
    SourcePosition relStartPos = new SourcePosition();
    start(relStartPos);
    
    relAST = parseAdditiveExpr();
    while (currentToken.kind == Token.LT || currentToken.kind == Token.LTEQ || currentToken.kind == Token.GT || currentToken.kind == Token.GTEQ){
      Operator opAST = acceptOperator();
      Expr e2AST = parseAdditiveExpr();
      
      SourcePosition relPos = new SourcePosition();
      copyStart(relStartPos, relPos);
      finish(relPos);
      relAST = new BinaryExpr(relAST, opAST, e2AST, relPos);
    }
    return relAST;
  }

  Expr parseAdditiveExpr() throws SyntaxError {
    Expr exprAST = null;

    SourcePosition addStartPos = new SourcePosition();
    start(addStartPos);

    exprAST = parseMultiplicativeExpr();
    while (currentToken.kind == Token.PLUS || currentToken.kind == Token.MINUS) {
      Operator opAST = acceptOperator();
      Expr e2AST = parseMultiplicativeExpr();

      SourcePosition addPos = new SourcePosition();
      copyStart(addStartPos, addPos);
      finish(addPos);
      exprAST = new BinaryExpr(exprAST, opAST, e2AST, addPos);
    }
    return exprAST;
  }

  Expr parseMultiplicativeExpr() throws SyntaxError {

    Expr exprAST = null;

    SourcePosition multStartPos = new SourcePosition();
    start(multStartPos);

    exprAST = parseUnaryExpr();
    while (currentToken.kind == Token.MULT || currentToken.kind == Token.DIV) {
      Operator opAST = acceptOperator();
      Expr e2AST = parseUnaryExpr();
      SourcePosition multPos = new SourcePosition();
      copyStart(multStartPos, multPos);
      finish(multPos);
      exprAST = new BinaryExpr(exprAST, opAST, e2AST, multPos);
    }
    return exprAST;
  }

  Expr parseUnaryExpr() throws SyntaxError {

    Expr exprAST = null;

    SourcePosition unaryPos = new SourcePosition();
    start(unaryPos);

    switch (currentToken.kind) {
      case Token.PLUS:
      case Token.MINUS:
      case Token.NOT:
        {
          
          Operator opAST = acceptOperator();
          Expr e2AST = parseUnaryExpr();
          finish(unaryPos);
          exprAST = new UnaryExpr(opAST, e2AST, unaryPos);
        }
        break;

      default:
        exprAST = parsePrimaryExpr();
        break;
       
    }
    return exprAST;
  }

  Expr parsePrimaryExpr() throws SyntaxError {

    Expr exprAST = null;

    SourcePosition primPos = new SourcePosition();
    start(primPos);

    switch (currentToken.kind) {

      case Token.ID:
         
        Ident iAST = parseIdent();
        finish(primPos);

        if (currentToken.kind == Token.LPAREN) {
          List argList = parseArgList();
          exprAST = new CallExpr(iAST, argList, primPos);
        } else if (currentToken.kind == Token.LBRACKET) {
          match(Token.LBRACKET);
          Expr e1 = null;
          if (currentToken.kind != Token.RBRACKET) {
            e1 = parseExpr();
          } else {
            e1 = new EmptyExpr(primPos);
          }
          match(Token.RBRACKET);
          finish(primPos);
          Var simVAST = new SimpleVar(iAST, primPos);
          exprAST = new ArrayExpr(simVAST, e1, primPos);
          
        } else {
         //  System.out.println("here");
          
          SimpleVar simVAST = new SimpleVar(iAST, primPos);
          exprAST = new VarExpr(simVAST, primPos);
          // System.out.println("Variable name is: " +  simVAST.I.spelling);
          // System.out.println("Variable type is: " +  exprAST.type);
        }
        break;

      case Token.LPAREN:
        {
          accept();
          exprAST = parseExpr();
	        match(Token.RPAREN);
        }
        break;

      case Token.INTLITERAL:
        IntLiteral ilAST = parseIntLiteral();
        finish(primPos);
        exprAST = new IntExpr(ilAST, primPos);
        break;
      case Token.FLOATLITERAL:
        FloatLiteral flAST = parseFloatLiteral();
        finish(primPos);
        exprAST = new FloatExpr(flAST, primPos);
        break;
      case Token.BOOLEANLITERAL:
        BooleanLiteral blAST = parseBooleanLiteral();
        finish(primPos);
        exprAST = new BooleanExpr(blAST, primPos);
        break;
      case Token.STRINGLITERAL:
        StringLiteral slAST = parseStringLiteral();
        finish(primPos);
        exprAST = new StringExpr(slAST, primPos);
        break;
      default:
        syntacticError("illegal primary expression", currentToken.spelling);
       
    }
    return exprAST;
  }

// ========================== PARAMETERS ========================

  List parseArgList() throws SyntaxError {
    List argList = null;
    SourcePosition alPos = new SourcePosition();
    start(alPos);
    
    match(Token.LPAREN);
    if (currentToken.kind != Token.RPAREN) {
      argList = parseProperArgList();
    } else {
      finish(alPos);
      argList = new EmptyArgList(alPos);
    }
    match(Token.RPAREN);
    
    return argList;
  }
  
  List parseProperArgList() throws SyntaxError {
    List argList = null;
    Arg a1 = parseArg();
    
    SourcePosition alPos = new SourcePosition();
    start(alPos);
    
    if (currentToken.kind == Token.COMMA) {
      match(Token.COMMA);
      List arg2List = parseProperArgList();
      finish(alPos);
      argList = new ArgList(a1, arg2List, alPos);
    } else {
      finish(alPos);
      List arg2List = new EmptyArgList(alPos);
      argList = new ArgList(a1, arg2List, alPos);
    }
    
    return argList;
  }
  
  Arg parseArg() throws SyntaxError {
    Expr e1 = null;
    Arg arg = null;
    SourcePosition argPos = new SourcePosition();
    start(argPos);
    e1 = parseExpr();
    finish(argPos);
    arg = new Arg(e1, argPos);
    return arg;
  }

// ========================== ID, OPERATOR and LITERALS ========================

  Ident parseIdent() throws SyntaxError {

    Ident I = null; 

    if (currentToken.kind == Token.ID) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      I = new Ident(spelling, previousTokenPosition);
      currentToken = scanner.getToken();
    } else 
      syntacticError("identifier expected here", "");
    return I;
  }

// acceptOperator parses an operator, and constructs a leaf AST for it

  Operator acceptOperator() throws SyntaxError {
    Operator O = null;

    previousTokenPosition = currentToken.position;
    String spelling = currentToken.spelling;
    O = new Operator(spelling, previousTokenPosition);
    currentToken = scanner.getToken();
    return O;
  }


  IntLiteral parseIntLiteral() throws SyntaxError {
    IntLiteral IL = null;

    if (currentToken.kind == Token.INTLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      IL = new IntLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("integer literal expected here", "");
    return IL;
  }

  FloatLiteral parseFloatLiteral() throws SyntaxError {
    FloatLiteral FL = null;

    if (currentToken.kind == Token.FLOATLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      FL = new FloatLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("float literal expected here", "");
    return FL;
  }

  BooleanLiteral parseBooleanLiteral() throws SyntaxError {
    BooleanLiteral BL = null;

    if (currentToken.kind == Token.BOOLEANLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      BL = new BooleanLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("boolean literal expected here", "");
    return BL;
  }
  
  StringLiteral parseStringLiteral() throws SyntaxError {
    StringLiteral SL = null;

    if (currentToken.kind == Token.STRINGLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      SL = new StringLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("String literal expected here", "");
    return SL;
  }
  
  Stmt parseIfStmt() throws SyntaxError {
    Stmt iStmt = null;
    
    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);
    match(Token.IF);
    match(Token.LPAREN);
    Expr e1 = parseExpr();
    match(Token.RPAREN);
    Stmt s1 = parseStmt();
    Stmt s2 = null;
    if (currentToken.kind == Token.ELSE) {
      match(Token.ELSE);
      s2 = parseStmt();
    } else {
      s2 = new EmptyStmt(stmtPos);
    }
    finish(stmtPos);
    iStmt = new IfStmt(e1, s1, s2, stmtPos);
    
    return iStmt;
  }
  
  Stmt parseForStmt() throws SyntaxError {
    Stmt fStmt = null;
    
    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);
    match(Token.FOR);
    match(Token.LPAREN);
    Expr e1, e2, e3 = null;
    
    if (currentToken.kind == Token.SEMICOLON){
      e1 = new EmptyExpr(stmtPos);
    } else {
      e1 = parseExpr();
    }
    match(Token.SEMICOLON);
    if (currentToken.kind == Token.SEMICOLON){
      e2 = new EmptyExpr(stmtPos);
    } else {
      e2 = parseExpr();
    }
    match(Token.SEMICOLON);
    if (currentToken.kind == Token.RPAREN){
      e3 = new EmptyExpr(stmtPos);
    } else {
      e3 = parseExpr();
    }
    match(Token.RPAREN);
    Stmt s1 = parseStmt();
    finish(stmtPos);
    fStmt = new ForStmt(e1, e2, e3, s1, stmtPos);
    
    return fStmt;
  }
  
  Stmt parseWhileStmt() throws SyntaxError {
    Stmt wStmt = null;
    
    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);
    match(Token.WHILE);
    match(Token.LPAREN);
    Expr e1 = parseExpr();
    match(Token.RPAREN);
    
    Stmt s1 = parseStmt();
    
    finish(stmtPos);
    wStmt = new WhileStmt(e1, s1, stmtPos);
    return wStmt;
  }
  
  Stmt parseBreakStmt() throws SyntaxError {
    Stmt bStmt = null;
    
    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);
    match(Token.BREAK);
    match(Token.SEMICOLON);
    finish(stmtPos);
    bStmt = new BreakStmt(stmtPos);
    
    return bStmt;
  }
  
  Stmt parseContinueStmt() throws SyntaxError {
    Stmt cStmt = null;
    
    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);
    match(Token.CONTINUE);
    match(Token.SEMICOLON);
    finish(stmtPos);
    cStmt = new ContinueStmt(stmtPos);
    
    return cStmt;
  }
  
  Stmt parseReturnStmt() throws SyntaxError {
    Stmt rStmt = null;
    
    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);
    
    match(Token.RETURN);
    
    Expr expr = null;
    
    if (currentToken.kind == Token.SEMICOLON){
      expr = new EmptyExpr(stmtPos);
    } else {
      expr = parseExpr();   // optional Expr
    }
    match(Token.SEMICOLON);
    finish(stmtPos);
    rStmt = new ReturnStmt(expr, stmtPos);
    return rStmt;
  }

}

