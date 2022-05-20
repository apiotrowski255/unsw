/**
 * Checker.java   
 * Thu 18 Mar 12:53:40 AEDT 2021
 **/

package VC.Checker;

import VC.ASTs.*;
import VC.Scanner.SourcePosition;
import VC.ErrorReporter;
import VC.StdEnvironment;

public final class Checker implements Visitor {

  private String errMesg[] = {
    "*0: main function is missing",                            
    "*1: return type of main is not int",                    

    // defined occurrences of identifiers
    // for global, local and parameters
    "*2: identifier redeclared",                             
    "*3: identifier declared void",                         
    "*4: identifier declared void[]",                      

    // applied occurrences of identifiers
    "*5: identifier undeclared",                          

    // assignments
    "*6: incompatible type for =",                       
    "*7: invalid lvalue in assignment",                 

     // types for expressions 
    "*8: incompatible type for return",                
    "*9: incompatible type for this binary operator", 
    "*10: incompatible type for this unary operator",

     // scalars
     "*11: attempt to use an array/function as a scalar", 

     // arrays
     "*12: attempt to use a scalar/function as an array",
     "*13: wrong type for element in array initialiser",
     "*14: invalid initialiser: array initialiser for scalar",   
     "*15: invalid initialiser: scalar initialiser for array",  
     "*16: excess elements in array initialiser",              
     "*17: array subscript is not an integer",                
     "*18: array size missing",                              

     // functions
     "*19: attempt to reference a scalar/array as a function",

     // conditional expressions in if, for and while
    "*20: if conditional is not boolean",                    
    "*21: for conditional is not boolean",                  
    "*22: while conditional is not boolean",               

    // break and continue
    "*23: break must be in a while/for",                  
    "*24: continue must be in a while/for",              

    // parameters 
    "*25: too many actual parameters",                  
    "*26: too few actual parameters",                  
    "*27: wrong type for actual parameter",           

    // reserved for errors that I may have missed (J. Xue)
    "*28: misc 1",
    "*29: misc 2",

    // the following two checks are optional 
    "*30: statement(s) not reached",     
    "*31: missing return statement",    
  };


  private SymbolTable idTable;
  private static SourcePosition dummyPos = new SourcePosition();
  private ErrorReporter reporter;
  private boolean intMainType, mainFunctionExist;
  private Type globalReturnType;

  // Checks whether the source program, represented by its AST, 
  // satisfies the language's scope rules and type rules.
  // Also decorates the AST as follows:
  //  (1) Each applied occurrence of an identifier is linked to
  //      the corresponding declaration of that identifier.
  //  (2) Each expression and variable is decorated by its type.

  public Checker (ErrorReporter reporter) {
    this.reporter = reporter;
    this.idTable = new SymbolTable ();
    establishStdEnvironment();
    this.globalReturnType = null;
  }

  public void check(AST ast) {
    ast.visit(this, null);
  }


  // auxiliary methods

  private void declareVariable(Ident ident, Decl decl) {
    IdEntry entry = idTable.retrieveOneLevel(ident.spelling);

    if (entry == null) {
      ; // no problem
    } else
      reporter.reportError(errMesg[2] + ": %", ident.spelling, ident.position);
    idTable.insert(ident.spelling, decl);
  }


  // Programs

  public Object visitProgram(Program ast, Object o) {
    ast.FL.visit(this, null);

    if (mainFunctionExist == false) { 
      reporter.reportError(errMesg[0], "", ast.position);
    } else if (intMainType == false) {
      reporter.reportError(errMesg[1], "", ast.position);
    }

    return null;
  }

  // Statements

  public Object visitCompoundStmt(CompoundStmt ast, Object o) {
    idTable.openScope();
    
    // extract the parameters from the funcDecl
    if (o instanceof FuncDecl) {
      FuncDecl fd = (FuncDecl) o;
      if (fd.PL instanceof EmptyParaList) {
        // no need to load in parameters
      } else {
        List pl = (ParaList) fd.PL;
        while(!pl.isEmptyParaList()) {
          ParaDecl pd = ((ParaList) pl).P;
          idTable.insert(pd.I.spelling, pd);
          pl = ((ParaList) pl).PL;
        }
      }
    }
    
    ast.DL.visit(this, o);
    if (o instanceof FuncDecl) {
      ast.SL.visit(this, ((FuncDecl) o).T);
    } else if (o instanceof Type) {
      ast.SL.visit(this, (Type) o);
    } else {
      ast.SL.visit(this, null);
    }
    

    idTable.closeScope();
    return null;
  }

  public Object visitStmtList(StmtList ast, Object o) {
    
    if (ast.S instanceof CompoundStmt) {
      idTable.openScope();
      ast.S.visit(this, o);
    } else {
      ast.S.visit(this, o);
    }
    
    if (ast.S instanceof ReturnStmt && ast.SL instanceof StmtList) {
      reporter.reportError(errMesg[30], "", ast.SL.position);
    } 
    ast.SL.visit(this, o);
    return null;
  }


  public Object visitExprStmt(ExprStmt ast, Object o) {
    ast.E.visit(this, o);
    return null;
  }



  // Expressions

  // Returns the Type denoting the type of the expression. Does
  // not use the given object.

  public Object visitEmptyExpr(EmptyExpr ast, Object o) {
    ast.type = StdEnvironment.errorType;
    return ast.type;
  }

  public Object visitBooleanExpr(BooleanExpr ast, Object o) {
    ast.type = StdEnvironment.booleanType;
    return ast.type;
  }

  public Object visitIntExpr(IntExpr ast, Object o) {
    ast.type = StdEnvironment.intType;
    return ast.type;
  }

  public Object visitFloatExpr(FloatExpr ast, Object o) {
    ast.type = StdEnvironment.floatType;
    return ast.type;
  }

  public Object visitStringExpr(StringExpr ast, Object o) {
    ast.type = StdEnvironment.stringType;
    return ast.type;
  }

  public Object visitVarExpr(VarExpr ast, Object o) {
    ast.type = (Type) ast.V.visit(this, null);
    // System.out.println(ast.type);
    return ast.type;
  }

  // Declarations
  // Always returns null. Does not use the given object.

  public Object visitFuncDecl(FuncDecl ast, Object o) {
    

    // Your code goes here
    
    Type t = (Type) ast.T.visit(this, ast);
    Decl binding = (Decl) ast.I.visit(this, ast);
    
    if (binding == null) {
      // Function has not been declared before
      idTable.insert (ast.I.spelling, ast); 
    } else if (binding.isFuncDecl() || binding.isGlobalVarDecl()) {
      // if the name is already taken by another function or a global variable then this is a problem. 
      reporter.reportError(errMesg[2] + ": %", ast.I.spelling, ast.I.position);
      idTable.insert (ast.I.spelling, ast); 
    } else {
      idTable.insert (ast.I.spelling, ast); 
    }
    // HINT
    // Pass ast as the 2nd argument (as done below) so that the
    // formal parameters of the function an be extracted from ast when the
    // function body is later visited
    
    ast.PL.visit(this, ast);
    ast.S.visit(this, ast);
    

    
    // Check for main function
    if (ast.I.spelling.equals("main")) {
      mainFunctionExist = true;
      if (ast.T.isIntType()){
        // All good, no problem
        intMainType = true;
      } 
    }
    
    // if the function is not a void type, then we expect there to be a return statement
    if (!ast.T.isVoidType()) { 
      if (scanForReturnStmt(ast.S) == false) {
        // reporter.reportError(errMesg[31], "", ast.position);
      }  
    }
    
    
    return null;
  }

  public Object visitDeclList(DeclList ast, Object o) {
    ast.D.visit(this, null);
    ast.DL.visit(this, null);
    return null;
  }



  public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
    Type t = (Type) ast.T.visit(this, null);
    Decl d = (Decl) ast.I.visit(this, null);
    Type et = (Type) ast.E.visit(this, null);
    if (ast.T instanceof ArrayType && t.isVoidType()) {
      reporter.reportError(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
    } else if (t.isVoidType()) {
      reporter.reportError(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
    } else if (ast.T instanceof ArrayType && !(t.isIntType() || t.isFloatType() || t.isBooleanType())) {
      System.out.println("only arrays of type INT Float or booleans are allowed");
    } else if (!(ast.T instanceof ArrayType) && ast.E instanceof InitExpr) {
      reporter.reportError(errMesg[14] + "", "", ast.E.position);
    } else if (ast.T instanceof ArrayType && ast.E instanceof EmptyExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      reporter.reportError(errMesg[18] + ": %", ast.I.spelling, ast.position);
    } else if (ast.T instanceof ArrayType && t.isFloatType() && ast.E instanceof InitExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      int arraySize = getExprListSize(el);
      ((ArrayType) ast.T).E = convertArraySizeToIntExpr(arraySize);
      checkArrayElementsAreFloats(el);
    } else if (ast.T instanceof ArrayType && t.isIntType() && ast.E instanceof InitExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      int arraySize = getExprListSize(el);
      ((ArrayType) ast.T).E = convertArraySizeToIntExpr(arraySize);
      checkArrayElementsAreInts(el);
    } else if (ast.T instanceof ArrayType && t.isBooleanType() && ast.E instanceof InitExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      int arraySize = getExprListSize(el);
      ((ArrayType) ast.T).E = convertArraySizeToIntExpr(arraySize);
      checkArrayElementsAreBooleans(el);
    } else if (ast.T instanceof ArrayType && t.isIntType() && ast.E instanceof InitExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      checkArrayElementsAreInts(el);
      IntExpr ie2 = (IntExpr) ((ArrayType) ast.T).E;
      int indexSize = ConvertIntExprToInt(ie2);
      int arraySize = getExprListSize(el);
      if (arraySize > indexSize) {
         reporter.reportError(errMesg[16] + ": %", ast.I.spelling, ast.position);
      } 
    } else if (ast.T instanceof ArrayType && ((ArrayType) ast.T).E instanceof IntExpr && ast.E instanceof EmptyExpr && t.isBooleanType()) {
      IntExpr ie = (IntExpr) ((ArrayType) ast.T).E;
      IntLiteral il = ie.IL;
      String s = il.spelling;
      int size = Integer.parseInt(s);
      ast.E = initBooleanArray(size);
    } else if (ast.T instanceof ArrayType && ((ArrayType) ast.T).E instanceof IntExpr && ast.E instanceof EmptyExpr && t.isIntType()) {
      IntExpr ie = (IntExpr) ((ArrayType) ast.T).E;
      IntLiteral il = ie.IL;
      String s = il.spelling;
      int size = Integer.parseInt(s);
      ast.E = initIntArray(size);
    } else if (ast.T instanceof ArrayType && ((ArrayType) ast.T).E instanceof IntExpr && ast.E instanceof EmptyExpr && t.isFloatType()) {
      IntExpr ie = (IntExpr) ((ArrayType) ast.T).E;
      IntLiteral il = ie.IL;
      String s = il.spelling;
      int size = Integer.parseInt(s);
      ast.E = initFloatArray(size);
    } else if (ast.T instanceof ArrayType && !(ast.E instanceof InitExpr)) {
      reporter.reportError(errMesg[15] + ": %", ast.I.spelling, ast.position);
    } else if (t.isFloatType() && ast.E instanceof EmptyExpr) {
      ast.E = initFloatExpr();
    } else if (t.isIntType() && ast.E instanceof EmptyExpr) {
      ast.E = initIntExpr();
    } else if (t.isBooleanType() && ast.E instanceof EmptyExpr) {
      ast.E = initBooleanExpr();
    } else if (t.isFloatType() && ast.E instanceof IntExpr) {
      ast.E = convertIntExprToFloat(ast.E);
    }

    declareVariable(ast.I, ast);

    
    
    return null;
  }

  public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
    Type t = (Type) ast.T.visit(this, null);
    Decl d = (Decl) ast.I.visit(this, null);
    Type et = (Type) ast.E.visit(this, null);
    
    if (ast.T instanceof ArrayType && t.isVoidType()) {
      reporter.reportError(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
    } else if (t.isVoidType()) {
      reporter.reportError(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
    } else if (ast.T instanceof ArrayType && !(t.isIntType() || t.isFloatType() || t.isBooleanType())) {
      System.out.println("only arrays of type INT Float or booleans are allowed");
    } else if (!(ast.T instanceof ArrayType) && ast.E instanceof InitExpr) {
      reporter.reportError(errMesg[14] + "", "", ast.E.position);
    } else if (ast.T instanceof ArrayType && ast.E instanceof EmptyExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      reporter.reportError(errMesg[18] + ": %", ast.I.spelling, ast.position);
    } else if (ast.T instanceof ArrayType && t.isFloatType() && ast.E instanceof InitExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      int arraySize = getExprListSize(el);
      ((ArrayType) ast.T).E = convertArraySizeToIntExpr(arraySize);
      checkArrayElementsAreFloats(el);
    } else if (ast.T instanceof ArrayType && t.isIntType() && ast.E instanceof InitExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      int arraySize = getExprListSize(el);
      ((ArrayType) ast.T).E = convertArraySizeToIntExpr(arraySize);
      checkArrayElementsAreInts(el);
    } else if (ast.T instanceof ArrayType && t.isBooleanType() && ast.E instanceof InitExpr && ((ArrayType) ast.T).E instanceof EmptyExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      int arraySize = getExprListSize(el);
      ((ArrayType) ast.T).E = convertArraySizeToIntExpr(arraySize);
      checkArrayElementsAreBooleans(el);
    } else if (ast.T instanceof ArrayType && t.isIntType() && ast.E instanceof InitExpr) {
      InitExpr ie = (InitExpr) ast.E;
      ExprList el = (ExprList) ie.IL;
      checkArrayElementsAreInts(el);
      IntExpr ie2 = (IntExpr) ((ArrayType) ast.T).E;
      int indexSize = ConvertIntExprToInt(ie2);
      int arraySize = getExprListSize(el);
      if (arraySize > indexSize) {
         reporter.reportError(errMesg[16] + ": %", ast.I.spelling, ast.position);
      } 
    } else if (ast.T instanceof ArrayType && ((ArrayType) ast.T).E instanceof IntExpr && ast.E instanceof EmptyExpr && t.isBooleanType()) {
      IntExpr ie = (IntExpr) ((ArrayType) ast.T).E;
      IntLiteral il = ie.IL;
      String s = il.spelling;
      int size = Integer.parseInt(s);
      ast.E = initBooleanArray(size);
    } else if (ast.T instanceof ArrayType && ((ArrayType) ast.T).E instanceof IntExpr && ast.E instanceof EmptyExpr && t.isIntType()) {
      IntExpr ie = (IntExpr) ((ArrayType) ast.T).E;
      IntLiteral il = ie.IL;
      String s = il.spelling;
      int size = Integer.parseInt(s);
      ast.E = initIntArray(size);
    } else if (ast.T instanceof ArrayType && ((ArrayType) ast.T).E instanceof IntExpr && ast.E instanceof EmptyExpr && t.isFloatType()) {
      IntExpr ie = (IntExpr) ((ArrayType) ast.T).E;
      IntLiteral il = ie.IL;
      String s = il.spelling;
      int size = Integer.parseInt(s);
      ast.E = initFloatArray(size);
    } else if (ast.T instanceof ArrayType && !(ast.E instanceof InitExpr)) {
      reporter.reportError(errMesg[15] + ": %", ast.I.spelling, ast.position);
    } else if (t.isFloatType() && ast.E instanceof EmptyExpr) {
      ast.E = initFloatExpr();
    } else if (t.isIntType() && ast.E instanceof EmptyExpr) {
      ast.E = initIntExpr();
    } else if (t.isBooleanType() && ast.E instanceof EmptyExpr) {
      ast.E = initBooleanExpr();
    } else if (t.isFloatType() && ast.E instanceof IntExpr) {
      ast.E = convertIntExprToFloat(ast.E);
    }

    declareVariable(ast.I, ast);
    
    return null;
  }

  // Parameters

 // Always returns null. Does not use the given object.

  public Object visitParaList(ParaList ast, Object o) {
    idTable.openScope();
    ast.P.visit(this, null);
    ast.PL.visit(this, null);
    idTable.closeScope();
    return null;
  }

  public Object visitParaDecl(ParaDecl ast, Object o) {
    Type t = (Type) ast.T.visit(this, null);
    Decl binding = (Decl) ast.I.visit(this, o);
    
    if (binding != null && (binding.isLocalVarDecl() || binding.isParaDecl())) {
      reporter.reportError(errMesg[2] + ": %", ast.I.spelling, ast.I.position);
    }
    idTable.insert(ast.I.spelling, ast);   

    if (ast.T.isVoidType()) {
      reporter.reportError(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
    } else if (ast.T.isArrayType()) {
     if (((ArrayType) ast.T).T.isVoidType())
        reporter.reportError(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
    }
    return t;
  }



  // Arguments

  // Your visitor methods for arguments go here

  // Types 

  // Returns the type predefined in the standard environment. 

  public Object visitErrorType(ErrorType ast, Object o) {
    return StdEnvironment.errorType;
  }

  public Object visitBooleanType(BooleanType ast, Object o) {
    return StdEnvironment.booleanType;
  }

  public Object visitIntType(IntType ast, Object o) {
    return StdEnvironment.intType;
  }

  public Object visitFloatType(FloatType ast, Object o) {
    return StdEnvironment.floatType;
  }

  public Object visitStringType(StringType ast, Object o) {
    return StdEnvironment.stringType;
  }

  public Object visitVoidType(VoidType ast, Object o) {
    return StdEnvironment.voidType;
  }

  // Literals, Identifiers and Operators

  public Object visitIdent(Ident I, Object o) {
    Decl binding = idTable.retrieve(I.spelling);
    if (binding != null) {
      I.decl = binding;
    }
    return binding;
  }

  public Object visitBooleanLiteral(BooleanLiteral SL, Object o) {
    return StdEnvironment.booleanType;
  }

  public Object visitIntLiteral(IntLiteral IL, Object o) {
    return StdEnvironment.intType;
  }

  public Object visitFloatLiteral(FloatLiteral IL, Object o) {
    return StdEnvironment.floatType;
  }

  public Object visitStringLiteral(StringLiteral IL, Object o) {
    return StdEnvironment.stringType;
  }

  public Object visitOperator(Operator O, Object o) {
    return O.spelling;
  }

  // Creates a small AST to represent the "declaration" of each built-in
  // function, and enters it in the symbol table.

  private FuncDecl declareStdFunc (Type resultType, String id, List pl) {

    FuncDecl binding;

    binding = new FuncDecl(resultType, new Ident(id, dummyPos), pl, 
           new EmptyStmt(dummyPos), dummyPos);
    idTable.insert (id, binding);
    return binding;
  }


  /****************************** Start WorkSpace Here *************************/ 

  public Object visitSimpleVar(SimpleVar ast, Object o) {
    Decl binding = (Decl) ast.I.visit(this, o);
    Type t = StdEnvironment.errorType;
    
    if (binding == null) {
      reporter.reportError(errMesg[5] + ": %", ast.I.spelling, ast.I.position);
    } else if (binding.isFuncDecl()) {
      reporter.reportError(errMesg[11] + ": %", ast.I.spelling, ast.I.position);
    } else {
      t = binding.T;
    }
    
    return t;
  }

  public Object visitArrayType(ArrayType ast, Object o) {
    Type t = (Type) ast.T.visit(this, null);
    Type e = (Type) ast.E.visit(this, null);
    
    return t;
  }

  public Object visitArg(Arg ast, Object o) {
    Type t = (Type) ast.E.visit(this, null);
    return t;
  }

  public Object visitArgList(ArgList ast, Object o) {
    ast.A.visit(this, null);
    ast.AL.visit(this, null);
    return null;
  }

  public Object visitAssignExpr(AssignExpr ast, Object o) {
    Type t1 = (Type) ast.E1.visit(this, null);
    Type t2 = (Type) ast.E2.visit(this, null);
    // System.out.println(t1);
    if (t1.isArrayType()) {
      t1 = ((ArrayType) t1).T;
    }
    if (t2.isArrayType()) {
      t1 = ((ArrayType) t2).T;
    }
    
    if (!(ast.E1 instanceof VarExpr || ast.E1 instanceof ArrayExpr)) {
      reporter.reportError(errMesg[7] + "", "", ast.position);
    } else if (t2.isErrorType()) {
      // Report an error but we also have to extract variable name
      if (ast.E1 instanceof VarExpr) {
        VarExpr ve = (VarExpr) ast.E1;
        SimpleVar sv = (SimpleVar) ve.V;
        reporter.reportError(errMesg[7] + ": %", sv.I.spelling, ast.position);
      } else {
        reporter.reportError(errMesg[7] + "", "", ast.position);
      }
      
    } else if (!(t1.assignable(t2))) {
      reporter.reportError(errMesg[6] + "", "", ast.position);
    } else if (t1.isFloatType() && t2.isIntType()) {
      ast.E2 = convertIntExprToFloat(ast.E2);
    } 
    return t1;
  } 

  // This is a call to a function. We need to make sure the variable exists and is actually a function
  // We have to also make sure the arguments are matching up with the function declation
  // return the type of the function
  public Object visitCallExpr(CallExpr ast, Object o) {
    // ast.AL.visit(this, null);
    Decl binding = (Decl) ast.I.visit(this, null);
    Type returnType = StdEnvironment.errorType;
    if (binding == null) {
      // Function does not exist
      reporter.reportError(errMesg[5] + ": %", ast.I.spelling, ast.I.position);
    } else if (!binding.isFuncDecl()) {
      // attempt to use an array/scalar as a function call
      reporter.reportError(errMesg[19] + ": %", ast.I.spelling, ast.I.position);
    } else {
      FuncDecl func = (FuncDecl) binding;
      returnType = (Type) func.T.visit(this, null);
      List pl = func.PL;
      List al = ast.AL;
      CompareArgListToParaList(al, pl);
    }
    return returnType;
  }

  public Object visitArrayExpr(ArrayExpr ast, Object o) {
    Type t = (Type) ast.V.visit(this, null);
    Type indexType = (Type) ast.E.visit(this, null);
    
    if (!t.isArrayType()) {
      reporter.reportError(errMesg[12], "", ast.position);
    }
    if (indexType.isArrayType() && ((ArrayType) indexType).E instanceof IntExpr && ast.E instanceof ArrayExpr) {
      indexType = ((ArrayType) indexType).T;
    } 
      
    
    if (!indexType.isIntType()) {
      reporter.reportError(errMesg[17], "", ast.position);
    }
    
    return t;
  }

  public Object visitExprList(ExprList ast, Object o) {
    ast.E.visit(this, null);
    ast.EL.visit(this, null);
    return null;
  } 

  public Object visitInitExpr(InitExpr ast, Object o) {
    ast.IL.visit(this, null);
    return null;
  }

  public Object visitBinaryExpr(BinaryExpr ast, Object o) {
    String operator = (String) ast.O.visit(this, o);
    Type t1 = (Type) ast.E1.visit(this, o);
    Type t2 = (Type) ast.E2.visit(this, o);

    if (t1.isArrayType() && ast.E1 instanceof ArrayExpr && ((ArrayExpr) ast.E1).E instanceof IntExpr) {
      t1 = ((ArrayType) t1).T;
    } else if (t1.isArrayType() && ast.E1 instanceof VarExpr) {
      VarExpr ve = (VarExpr) ast.E1;
      SimpleVar sv = (SimpleVar) ve.V;
      String s = sv.I.spelling;
      IdEntry d = idTable.retrieveOneLevel(s);
      if (d != null) {
        Decl decl = d.attr;
        Type tempt = decl.T;
        if (tempt.isArrayType()) {
          reporter.reportError(errMesg[11] + ": %", s, ast.E1.position);
          reporter.reportError(errMesg[9] + ": %", operator, ast.position);
        }
      }
      t1 = StdEnvironment.errorType;
    }
    if (t2.isArrayType() && ast.E1 instanceof ArrayExpr && ((ArrayExpr) ast.E2).E instanceof IntExpr) {
      t2 = ((ArrayType) t2).T;
    } else if (t2.isArrayType() && ast.E2 instanceof VarExpr) {
      VarExpr ve = (VarExpr) ast.E2;
      SimpleVar sv = (SimpleVar) ve.V;
      String s = sv.I.spelling;
      IdEntry d = idTable.retrieveOneLevel(s);
      if (d != null) {
        Decl decl = d.attr;
        Type tempt = decl.T;
        if (tempt.isArrayType()) {
          reporter.reportError(errMesg[11] + ": %", s, ast.E1.position);
          reporter.reportError(errMesg[9] + ": %", operator, ast.position);
        }
      }
      t2 = StdEnvironment.errorType;
    }
    
    
    if (t1.isErrorType() || t1.isErrorType()) {
      // no need to print error messages as they should have been printed within their visit methods.
      return StdEnvironment.errorType;
    } else if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")) {
      if (t1.isIntType() && t2.isIntType()) {
        return StdEnvironment.intType;
      } else if (t1.isIntType() && t2.isFloatType()) {
        ast.E1 = convertIntExprToFloat(ast.E1);
        return StdEnvironment.floatType;
      } else if (t1.isFloatType() && t2.isIntType()) {
        ast.E2 = convertIntExprToFloat(ast.E2);
        return StdEnvironment.floatType;
      } else if (t1.isFloatType() && t2.isFloatType()) {
        return StdEnvironment.floatType;
      } else {
        reporter.reportError(errMesg[9] + ": %", operator, ast.position);
        return StdEnvironment.errorType;
      }
    } else if (operator.equals("<") || operator.equals("<=") || operator.equals(">") || operator.equals(">=")) {
      if (t1.isIntType() && t2.isIntType()) {
        return StdEnvironment.booleanType;
      } else if (t1.isIntType() && t2.isFloatType()) {
        ast.E1 = convertIntExprToFloat(ast.E1);
        return StdEnvironment.booleanType;
      } else if (t1.isFloatType() && t2.isIntType()) {
        ast.E2 = convertIntExprToFloat(ast.E2);
        return StdEnvironment.booleanType;
      } else if (t1.isFloatType() && t2.isFloatType()) {
        return StdEnvironment.booleanType;
      } else {
        reporter.reportError(errMesg[9] + ": %", operator, ast.position);
        return StdEnvironment.errorType;
      }
    } else if (operator.equals("==") || operator.equals("!=")) {
       if (t1.isIntType() && t2.isIntType()) {
        return StdEnvironment.booleanType;
      } else if (t1.isIntType() && t2.isFloatType()) {
        ast.E1 = convertIntExprToFloat(ast.E1);
        return StdEnvironment.booleanType;
      } else if (t1.isFloatType() && t2.isIntType()) {
        ast.E2 = convertIntExprToFloat(ast.E2);
        return StdEnvironment.booleanType;
      } else if (t1.isFloatType() && t2.isFloatType()) {
        return StdEnvironment.booleanType;
      } else if (t1.isBooleanType() && t2.isBooleanType()) { 
        return StdEnvironment.booleanType;
      } else {
        reporter.reportError(errMesg[9] + ": %", operator, ast.position);
        return StdEnvironment.errorType;
      }
    } else if (operator.equals("&&") || operator.equals("||")) {
      if (t1.isBooleanType() && t2.isBooleanType()) {
        return StdEnvironment.booleanType;
      } else {
        reporter.reportError(errMesg[9] + ": %", operator, ast.position);
        return StdEnvironment.errorType;
      }
    } else {
      reporter.reportError(errMesg[28] + ": %", "Unknown operator", ast.position);
      return StdEnvironment.errorType;
    }
  }

  public Object visitUnaryExpr(UnaryExpr ast, Object o) {
    String operator = (String) ast.O.visit(this, null);
    Type t = (Type) ast.E.visit(this, null);
    
    if (operator.equals("!") && !t.isBooleanType()) {
      // In this case we expect Type boolean. If it is not a boolean then we print out an error message
      reporter.reportError(errMesg[10] + ": %", operator, ast.position);
      t = StdEnvironment.errorType;
    }  else if ((operator.equals("+") || operator.equals("-")) && !(t.isIntType() || t.isFloatType())) {
      // In this case we expect Type to be either Float or Int. If it is not an Int or Float, we print out an error message
      reporter.reportError(errMesg[10] + ": %", operator, ast.position);
      t = StdEnvironment.errorType;
    } else if (operator.equals("i2f")) {
      t = StdEnvironment.floatType;
      ast.type = StdEnvironment.floatType;
    }
    return t;
  }

  public Object visitReturnStmt(ReturnStmt ast, Object o) {
    Type returnType = (Type) ast.E.visit(this, null);
    if (returnType instanceof ArrayType && ((ArrayType) returnType).E instanceof IntExpr) {
      returnType = ((ArrayType) returnType).T;
    }
    
    if (o instanceof Type) {
      // We will grab the Expected return type from the function
      Type expectedReturnType = (Type) o;
      if (!expectedReturnType.assignable(returnType)) {
        reporter.reportError(errMesg[8], "", ast.position);
      } else if (returnType.isIntType() && expectedReturnType.isFloatType()) {
        // One case to check is if the function type is float and the return is int
        // Need to convert int to float
        ast.E = convertIntExprToFloat(ast.E);
      }
    } else if (globalReturnType != null) { 
      // We grab the funcDecl from the global variable globalReturnType
      Type expectedReturnType = globalReturnType;
      if (!expectedReturnType.assignable(returnType)) {
        reporter.reportError(errMesg[8], "", ast.position);
      } else if (returnType.isIntType() && expectedReturnType.isFloatType()) {
        // One case to check is if the function type is float and the return is int
        // Need to convert int to float
        ast.E = convertIntExprToFloat(ast.E);
      }
    } else {
      // We must have come from an for/while stmt and did not store a global variable
      // I am not too sure how we got here
    }
    return null;
  }

  public Object visitContinueStmt(ContinueStmt ast, Object o) {
    if (!(o instanceof ForStmt || o instanceof WhileStmt)) {
      reporter.reportError(errMesg[24], "", ast.position);
    }
    return null;
  }

  public Object visitBreakStmt(BreakStmt ast, Object o) {
    if (!(o instanceof ForStmt || o instanceof WhileStmt)) {
      reporter.reportError(errMesg[23], "", ast.position);
    }
    return null;
  }


  // Check if E2 is of type Boolean
  public Object visitForStmt(ForStmt ast, Object o) {
    ast.E1.visit(this, null);
    Type t = (Type) ast.E2.visit(this, null);
    ast.E3.visit(this, null);
    if (!t.isBooleanType()) {
      reporter.reportError(errMesg[21] + " (found: %)", typeToString(t), ast.E2.position);
    }
    if (o instanceof Type) {
      // Store object o in the global variable to be used in return stmt
      globalReturnType = (Type) o;
    }
    ast.S.visit(this, ast);
    return null;
  }

  // Need to check that the expr in the while stmt is of type Boolean

  public Object visitWhileStmt(WhileStmt ast, Object o) {
    Type t = (Type) ast.E.visit(this, null);
    if (!t.isBooleanType()) {
      reporter.reportError(errMesg[22] + " (found: %)", typeToString(t), ast.E.position);
    }
    if (o instanceof Type) {
      // Store object o in the global variable to be used in return stmt
      globalReturnType = (Type) o;
    }
    ast.S.visit(this, ast);
    return null;
  }


  // The only check we need to do in the If stmt is to see whether the 
  // Expr type is a boolean. If it is not a boolean then we print out an error message
  public Object visitIfStmt(IfStmt ast, Object o) {
    Type t = (Type) ast.E.visit(this, null);
    if (!t.isBooleanType()) {
      reporter.reportError(errMesg[20] + " (found: %)", typeToString(t), ast.E.position);
    }
    ast.S1.visit(this, o);
    ast.S2.visit(this, o);
    return null;
  }

















  /****************************************** Shit i never have to touch (And Therefore cannot break) ************************************/

  public Object visitEmptyArgList(EmptyArgList ast, Object o) {
    return null;
  }

  public Object visitEmptyExprList(EmptyExprList ast, Object o) {
    return null;
  }

  public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
    return null;
  }

  public Object visitEmptyStmt(EmptyStmt ast, Object o) {
    return null;
  }

  public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
    return null;
  }

  public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
    return null;
  }
  
  public Object visitEmptyParaList(EmptyParaList ast, Object o) {
    return null;
  }

  public String typeToString(Type type) {
    String s;
    if (type.isVoidType()) {
      s = "void";
    } else if (type.isIntType()) {
      s = "int";
    } else if (type.isFloatType()) {
      s = "float";
    } else if (type.isStringType()) {
      s = "string";
    } else if (type.isBooleanType()) {
      s = "boolean";
    } else if (type.isArrayType()) {
      s = "array";
    } else if (type.isErrorType()) {
      s = "error";
    } else {
      s = "unknown";
    }
    return s;
  }
  
  public UnaryExpr convertIntExprToFloat(Expr expr){
    Operator o = new Operator("i2f", expr.position);
    UnaryExpr E = new UnaryExpr(o, expr, expr.position);
    return E;
  }

  public boolean scanForReturnStmt(Stmt ast) {
    if (ast instanceof CompoundStmt) {
      CompoundStmt s = (CompoundStmt) ast;
      if (s.SL instanceof EmptyStmtList) {
        return false;
      } else {
        StmtList sl = (StmtList) s.SL;
        while (sl.SL instanceof StmtList) {
          if (scanForReturnStmt(sl.S) == true) {
            return true;
          } else {
            sl = (StmtList) sl.SL;
          }
        }
        return scanForReturnStmt(sl.S);
      }
    } else if (ast instanceof IfStmt) {
      IfStmt s = (IfStmt) ast;
      return (scanForReturnStmt(s.S1) || scanForReturnStmt(s.S2));
    } else if (ast instanceof ForStmt) {
      ForStmt s = (ForStmt) ast;
      return (scanForReturnStmt(s.S));
    } else if (ast instanceof WhileStmt) {
      WhileStmt s = (WhileStmt) ast;
      return (scanForReturnStmt(s.S));
    } else if (ast instanceof ExprStmt) {
      return false;
    } else if (ast instanceof ContinueStmt || ast instanceof BreakStmt) {
      return false;
    } else if (ast instanceof ReturnStmt) {
      return true;
    } else if (ast instanceof EmptyCompStmt || ast instanceof EmptyStmt) {
      return false;
    }  else {
      return false;
    }
  }
  
  public void CompareArgListToParaList(List al, List pl) {
    
    while (al instanceof ArgList && pl instanceof ParaList) {
      ArgList aal = (ArgList) al;
      ParaList ppl = (ParaList) pl;
      
      // compare the arguments. Print any errors if needed
      Type a = (Type) aal.A.visit(this, null);
      Type p = ppl.P.T;
      if (a instanceof ArrayType && p instanceof ArrayType) { 
        // System.out.println(a);
        // System.out.println(p);
        Type a1 = ((ArrayType) a).T;
        Type p1 = ((ArrayType) p).T;
        if (!p1.assignable(a1)) {
          reporter.reportError(errMesg[27] + ": %", ppl.P.I.spelling, aal.A.position);
        }
      } else if (a instanceof ArrayType) { 
        Type a1 = ((ArrayType) a).T;
        if (!p.assignable(a1)) {
          reporter.reportError(errMesg[27] + ": %", ppl.P.I.spelling, aal.A.position);
        }
      } else if (p instanceof ArrayType) {
        Type p1 = ((ArrayType) p).T;
        if (!p1.assignable(a)) {
          reporter.reportError(errMesg[27] + ": %", ppl.P.I.spelling, aal.A.position);
        }
      } else if (!p.assignable(a)) {
        reporter.reportError(errMesg[27] + ": %", ppl.P.I.spelling, aal.A.position);
      } 
      
      // increment to the next node
      al = aal.AL;
      pl = ppl.PL;
    }
    
    if (al instanceof EmptyArgList && pl instanceof EmptyParaList) {
      // All good
    } else if (al instanceof EmptyArgList) {
      reporter.reportError(errMesg[26], "", al.position);
    } else if (pl instanceof EmptyParaList) {
      reporter.reportError(errMesg[25], "", ((ArgList) al).A.position);
    }
    
  }
  
  public Object checkArrayElementsAreInts(ExprList exprList) {
    List l = exprList;
    int position = 0;
    while (!l.isEmptyExprList()) {
      ExprList e = (ExprList) l;
      Type t = (Type) e.E.visit(this, null);
      if (t.isArrayType() && e.E instanceof VarExpr) {
        reporter.reportError(errMesg[12] + ": at position %", String.valueOf(position), e.E.position);
      } else if (t.isArrayType() && ((ArrayType) t).E instanceof IntExpr) {
        ArrayType at = (ArrayType) t;
        t = at.T;
      }
      
      if (!t.isIntType()) {
        reporter.reportError(errMesg[13] + ": at position %", String.valueOf(position), e.E.position);
      }
      
      l = e.EL;
      position++;
    }
    return null;  
  }
  
  public void checkArrayElementsAreBooleans(ExprList exprList) {
    List l = exprList;
    int position = 0;
    while (!l.isEmptyExprList()) {
      ExprList e = (ExprList) l;
      Type t = (Type) e.E.visit(this, null);
      if (t.isArrayType()) {
        ArrayType at = (ArrayType) t;
        t = at.T;
      }
      if (!t.isBooleanType()) {
        reporter.reportError(errMesg[13] + ": at position %", String.valueOf(position), e.E.position);
      }
      
      l = e.EL;
      position++;
    }
  }
  
  public Object checkArrayElementsAreFloats(ExprList exprList) {
    List l = exprList;
    int position = 0;
    while (!l.isEmptyExprList()) {
      ExprList e = (ExprList) l;
      Type t = (Type) e.E.visit(this, null);
      if (t.isArrayType()) {
        ArrayType at = (ArrayType) t;
        t = at.T;
      }
      
      if (t.isIntType()) {
        // convert Int to float
        e.E = convertIntExprToFloat(e.E);
      } else if (!t.isFloatType()) {
        reporter.reportError(errMesg[13] + ": at position %", String.valueOf(position), e.E.position);
      }
      
      l = e.EL;
      position++;
    }
    return null;  
  }
  
  // Given an ExprList, returns the size of the ExprList
  public int getExprListSize(ExprList exprList) {
    List l = exprList;
    int size = 0;
    while (!l.isEmptyExprList()) {
      ExprList e = (ExprList) l;
      l = e.EL;
      size++;
    }
    return size; 
  }
  
  public int ConvertIntExprToInt(IntExpr intexpr) {
    IntLiteral il = intexpr.IL; 
    String s = il.spelling;
    int indexSize = Integer.parseInt(s);
    return indexSize;
  }
  
  public IntExpr convertArraySizeToIntExpr(int size) {
    if (size == 0) {
      // TODO array size of 0 is not possible
    }
    String arraySize = Integer.toString(size);
    IntLiteral il = new IntLiteral(arraySize, dummyPos);
    IntExpr ie = new IntExpr(il, dummyPos);
    return ie;
  }
  
  public InitExpr initIntArray(int size) {
    InitExpr ie = null;
    if (size == 0) {
      return null;
    } else {
      int i = size;
      IntLiteral il = new IntLiteral("0", dummyPos);
      IntExpr e = new IntExpr(il, dummyPos);
      EmptyExprList eel = new EmptyExprList(dummyPos);
      ExprList el = new ExprList(e, eel, dummyPos);
      while (i > 1) {
        ExprList new_el = new ExprList(e, el, dummyPos);
        el = new_el;
        i -= 1;
      }
      ie = new InitExpr(el, dummyPos);
    }
    
    return ie;
  }
  
  public InitExpr initBooleanArray(int size) {
    InitExpr ie = null;
    if (size == 0) {
      return null;
    } else {
      int i = size;
      BooleanLiteral bl = new BooleanLiteral("false", dummyPos);
      BooleanExpr b = new BooleanExpr(bl, dummyPos);
      EmptyExprList eel = new EmptyExprList(dummyPos);
      ExprList el = new ExprList(b, eel, dummyPos);
      while (i > 1) {
        ExprList new_el = new ExprList(b, el, dummyPos);
        el = new_el;
        i -= 1;
      }
      ie = new InitExpr(el, dummyPos);
    }
    
    return ie;
  }
  
  public InitExpr initFloatArray(int size) {
    InitExpr ie = null;
    if (size == 0) {
      return null;
    } else {
      int i = size;
      FloatLiteral fl = new FloatLiteral("0.0", dummyPos);
      FloatExpr f = new FloatExpr(fl, dummyPos);
      EmptyExprList eel = new EmptyExprList(dummyPos);
      ExprList el = new ExprList(f, eel, dummyPos);
      while (i > 1) {
        ExprList new_el = new ExprList(f, el, dummyPos);
        el = new_el;
        i -= 1;
      }
      ie = new InitExpr(el, dummyPos);
    }
    
    return ie;
  }
  
  public FloatExpr initFloatExpr() {
    FloatLiteral fl = new FloatLiteral("0.0", dummyPos);
    FloatExpr f = new FloatExpr(fl, dummyPos);
    return f;
  }
  
  public IntExpr initIntExpr() {
    IntLiteral bl = new IntLiteral("0", dummyPos);
    IntExpr i = new IntExpr(bl, dummyPos);
    return i;
  }
  
  public BooleanExpr initBooleanExpr() {
    BooleanLiteral bl = new BooleanLiteral("false", dummyPos);
    BooleanExpr b = new BooleanExpr(bl, dummyPos);
    return b;
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  // Put this out of sight... I dont need to see it all the time. 
  // Creates small ASTs to represent "declarations" of all 
  // build-in functions.
  // Inserts these "declarations" into the symbol table.

  private final static Ident dummyI = new Ident("x", dummyPos);

   private void establishStdEnvironment () {

    // Define four primitive types
    // errorType is assigned to ill-typed expressions

    StdEnvironment.booleanType = new BooleanType(dummyPos);
    StdEnvironment.intType = new IntType(dummyPos);
    StdEnvironment.floatType = new FloatType(dummyPos);
    StdEnvironment.stringType = new StringType(dummyPos);
    StdEnvironment.voidType = new VoidType(dummyPos);
    StdEnvironment.errorType = new ErrorType(dummyPos);

    // enter into the declarations for built-in functions into the table

    StdEnvironment.getIntDecl = declareStdFunc( StdEnvironment.intType,
	"getInt", new EmptyParaList(dummyPos)); 
    StdEnvironment.putIntDecl = declareStdFunc( StdEnvironment.voidType,
	"putInt", new ParaList(
	new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putIntLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putIntLn", new ParaList(
	new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.getFloatDecl = declareStdFunc( StdEnvironment.floatType,
	"getFloat", new EmptyParaList(dummyPos)); 
    StdEnvironment.putFloatDecl = declareStdFunc( StdEnvironment.voidType,
	"putFloat", new ParaList(
	new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putFloatLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putFloatLn", new ParaList(
	new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putBoolDecl = declareStdFunc( StdEnvironment.voidType,
	"putBool", new ParaList(
	new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putBoolLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putBoolLn", new ParaList(
	new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putStringLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putStringLn", new ParaList(
	new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putStringDecl = declareStdFunc( StdEnvironment.voidType,
	"putString", new ParaList(
	new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putLn", new EmptyParaList(dummyPos));

  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

}
