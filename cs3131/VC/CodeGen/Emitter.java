/*
 *** Emitter.java 
 *** Thu  1 Apr 11:37:43 AEDT 2021
 *** Jingling Xue, School of Computer Science, UNSW, Australia
 */

// A new frame object is created for every function just before the
// function is being translated in visitFuncDecl.
//
// All the information about the translation of a function should be
// placed in this Frame object and passed across the AST nodes as the
// 2nd argument of every visitor method in Emitter.java.

package VC.CodeGen;

import java.util.LinkedList;
import java.util.Enumeration;
import java.util.ListIterator;

import VC.ASTs.*;
import VC.ErrorReporter;
import VC.StdEnvironment;

public final class Emitter implements Visitor {

  private ErrorReporter errorReporter;
  private String inputFilename;
  private String classname;
  private String outputFilename;

  public Emitter(String inputFilename, ErrorReporter reporter) {
    this.inputFilename = inputFilename;
    errorReporter = reporter;
    
    int i = inputFilename.lastIndexOf('.');
    if (i > 0)
      classname = inputFilename.substring(0, i);
    else
      classname = inputFilename;
    
  }

  // PRE: ast must be a Program node

  public final void gen(AST ast) {
    ast.visit(this, null); 
    JVM.dump(classname + ".j");
  }
    
  // Programs
  public Object visitProgram(Program ast, Object o) {
     /** This method works for scalar variables only. You need to modify
         it to handle all array-related declarations and initialisations.
      **/ 

    // Generates the default constructor initialiser 
    emit(JVM.CLASS, "public", classname);
    emit(JVM.SUPER, "java/lang/Object");

    emit("");

    // Three subpasses:

    // (1) Generate .field definition statements since
    //     these are required to appear before method definitions
    List list = ast.FL;
    while (!list.isEmpty()) {
      DeclList dlAST = (DeclList) list;
      if (dlAST.D instanceof GlobalVarDecl) {
        GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
        emit(JVM.STATIC_FIELD, vAST.I.spelling, VCtoJavaType(vAST.T));
        }
      list = dlAST.DL;
    }

    emit("");

    // (2) Generate <clinit> for global variables (assumed to be static)
 
    emit("; standard class static initializer ");
    emit(JVM.METHOD_START, "static <clinit>()V");
    emit("");

    // create a Frame for <clinit>

    Frame frame = new Frame(false);

    list = ast.FL;
    while (!list.isEmpty()) {
      DeclList dlAST = (DeclList) list;
      if (dlAST.D instanceof GlobalVarDecl) {
        GlobalVarDecl vAST = (GlobalVarDecl) dlAST.D;
        if (!vAST.E.isEmptyExpr()) {
          vAST.E.visit(this, frame);
        } else {
          if (vAST.T.equals(StdEnvironment.floatType))
            emit(JVM.FCONST_0);
          else
            emit(JVM.ICONST_0);
          frame.push();
        }
        emitPUTSTATIC(VCtoJavaType(vAST.T), vAST.I.spelling); 
        frame.pop();
      }
      list = dlAST.DL;
    }
   
    emit("");
    emit("; set limits used by this method");
    emit(JVM.LIMIT, "locals", frame.getNewIndex());

    emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
    emit(JVM.RETURN);
    emit(JVM.METHOD_END, "method");

    emit("");

    // (3) Generate Java bytecode for the VC program

    emit("; standard constructor initializer ");
    emit(JVM.METHOD_START, "public <init>()V");
    emit(JVM.LIMIT, "stack 1");
    emit(JVM.LIMIT, "locals 1");
    emit(JVM.ALOAD_0);
    emit(JVM.INVOKESPECIAL, "java/lang/Object/<init>()V");
    emit(JVM.RETURN);
    emit(JVM.METHOD_END, "method");

    return ast.FL.visit(this, o);
  }

  // Statements

  public Object visitStmtList(StmtList ast, Object o) {
    ast.S.visit(this, o);
    ast.SL.visit(this, o);
    return null;
  }

  public Object visitCompoundStmt(CompoundStmt ast, Object o) {
    Frame frame = (Frame) o; 

    String scopeStart = frame.getNewLabel();
    String scopeEnd = frame.getNewLabel();
    frame.scopeStart.push(scopeStart);
    frame.scopeEnd.push(scopeEnd);
   
    emit(scopeStart + ":");
    if (ast.parent instanceof FuncDecl) {
      if (((FuncDecl) ast.parent).I.spelling.equals("main")) {
        emit(JVM.VAR, "0 is argv [Ljava/lang/String; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
        emit(JVM.VAR, "1 is vc$ L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
        // Generate code for the initialiser vc$ = new classname();
        emit(JVM.NEW, classname);
        emit(JVM.DUP);
        frame.push(2);
        emit("invokenonvirtual", classname + "/<init>()V");
        frame.pop();
        emit(JVM.ASTORE_1);
        frame.pop();
      } else {
        emit(JVM.VAR, "0 is this L" + classname + "; from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
        ((FuncDecl) ast.parent).PL.visit(this, o);
      }
    }
    ast.DL.visit(this, o);
    ast.SL.visit(this, o);
    emit(scopeEnd + ":");

    frame.scopeStart.pop();
    frame.scopeEnd.pop();
    return null;
  }

  public Object visitReturnStmt(ReturnStmt ast, Object o) {
    Frame frame = (Frame)o;

/*
  int main() { return 0; } must be interpretted as 
  public static void main(String[] args) { return ; }
  Therefore, "return expr", if present in the main of a VC program
  must be translated into a RETURN rather than IRETURN instruction.
*/

     if (frame.isMain())  {
        emit(JVM.RETURN);
        return null;
     }

    // Your other code goes here
    ast.E.visit(this, o);
    Type t = getTypeFromExpr(ast.E);
    
    if (t.isIntType() || t.isBooleanType()) {
      emit(JVM.IRETURN);
    } else if (t.isFloatType()) {
      emit(JVM.FRETURN);
    } else if (t.isVoidType()) {
      emit(JVM.RETURN);
    } else if (ast.E instanceof FloatExpr) {
      emit(JVM.FRETURN);
    } else if (ast.E instanceof VarExpr) {
      VarExpr ve = (VarExpr) ast.E;
      Type vt = ve.type;
      if (vt.isIntType() || vt.isBooleanType()) {
        emit(JVM.IRETURN);
      } else if (vt.isFloatType()) {
        emit(JVM.FRETURN);
      } else if (vt.isVoidType()) { // This case should not be possible
         emit(JVM.RETURN);
      }
    } else if (ast.E instanceof CallExpr) {
      CallExpr ce = (CallExpr) ast.E;
      Type ct = getTypeFromCallExpr(ce);
      System.out.println(ct);
      if (ct.isIntType() || ct.isBooleanType()) {
        emit(JVM.IRETURN);
      } else if (ct.isFloatType()) {
        emit(JVM.FRETURN);
      } else if (ct.isVoidType()) {
         emit(JVM.RETURN);
      }
    } else {
      System.out.println("Issue in Return Stmt");
      System.out.println(ast.E.type);
      emit(JVM.IRETURN);
    }
  
    return null; 
  }

  public Type getTypeFromCallExpr(CallExpr ast) {
    Ident i = ast.I;
    Decl d = (Decl) i.decl;
    Type t = d.T;
    return t;
  }

  // Expressions

  public Object visitCallExpr(CallExpr ast, Object o) {
    Frame frame = (Frame) o;
    String fname = ast.I.spelling;

    if (fname.equals("getInt")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System.getInt()I");
      frame.push();
    } else if (fname.equals("putInt")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System.putInt(I)V");
      frame.pop();
    } else if (fname.equals("putIntLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putIntLn(I)V");
      frame.pop();
    } else if (fname.equals("getFloat")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/getFloat()F");
      frame.push();
    } else if (fname.equals("putFloat")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putFloat(F)V");
      frame.pop();
    } else if (fname.equals("putFloatLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putFloatLn(F)V");
      frame.pop();
    } else if (fname.equals("putBool")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putBool(Z)V");
      frame.pop();
    } else if (fname.equals("putBoolLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putBoolLn(Z)V");
      frame.pop();
    } else if (fname.equals("putString")) {
      ast.AL.visit(this, o);
      emit(JVM.INVOKESTATIC, "VC/lang/System/putString(Ljava/lang/String;)V");
      frame.pop();
    } else if (fname.equals("putStringLn")) {
      ast.AL.visit(this, o);
      emit(JVM.INVOKESTATIC, "VC/lang/System/putStringLn(Ljava/lang/String;)V");
      frame.pop();
    } else if (fname.equals("putLn")) {
      ast.AL.visit(this, o); // push args (if any) into the op stack
      emit("invokestatic VC/lang/System/putLn()V");
    } else { // programmer-defined functions

      FuncDecl fAST = (FuncDecl) ast.I.decl;

      // all functions except main are assumed to be instance methods
      if (frame.isMain()) 
        emit("aload_1"); // vc.funcname(...)
      else
        emit("aload_0"); // this.funcname(...)
      frame.push();

      ast.AL.visit(this, o);
    
      String retType = VCtoJavaType(fAST.T);
      
      // The types of the parameters of the called function are not
      // directly available in the FuncDecl node but can be gathered
      // by traversing its field PL.

      StringBuffer argsTypes = new StringBuffer("");
      List fpl = fAST.PL;
      while (! fpl.isEmpty()) {
        if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType))
          argsTypes.append("Z");         
        else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType))
          argsTypes.append("I");         
        else
          argsTypes.append("F");         
        fpl = ((ParaList) fpl).PL;
      }
      
      emit("invokevirtual", classname + "/" + fname + "(" + argsTypes + ")" + retType);
      frame.pop(argsTypes.length() + 1);

      if (! retType.equals("V"))
        frame.push();
    }
    return null;
  }

  public Object visitFuncDecl(FuncDecl ast, Object o) {

    Frame frame; 

    if (ast.I.spelling.equals("main")) {

       frame = new Frame(true);

      // Assume that main has one String parameter and reserve 0 for it
      frame.getNewIndex(); 

      emit(JVM.METHOD_START, "public static main([Ljava/lang/String;)V"); 
      // Assume implicitly that
      //      classname vc$; 
      // appears before all local variable declarations.
      // (1) Reserve 1 for this object reference.

      frame.getNewIndex(); 

    } else {

       frame = new Frame(false);

      // all other programmer-defined functions are treated as if
      // they were instance methods
      frame.getNewIndex(); // reserve 0 for "this"

      String retType = VCtoJavaType(ast.T);

      // The types of the parameters of the called function are not
      // directly available in the FuncDecl node but can be gathered
      // by traversing its field PL.

      StringBuffer argsTypes = new StringBuffer("");
      List fpl = ast.PL;
      while (! fpl.isEmpty()) {
        if (((ParaList) fpl).P.T.equals(StdEnvironment.booleanType))
          argsTypes.append("Z");         
        else if (((ParaList) fpl).P.T.equals(StdEnvironment.intType))
          argsTypes.append("I");         
        else
          argsTypes.append("F");         
        fpl = ((ParaList) fpl).PL;
      }

      emit(JVM.METHOD_START, ast.I.spelling + "(" + argsTypes + ")" + retType);
    }

    ast.S.visit(this, frame);

    // JVM requires an explicit return in every method. 
    // In VC, a function returning void may not contain a return, and
    // a function returning int or float is not guaranteed to contain
    // a return. Therefore, we add one at the end just to be sure.

    if (ast.T.equals(StdEnvironment.voidType)) {
      emit("");
      emit("; return may not be present in a VC function returning void"); 
      emit("; The following return inserted by the VC compiler");
      emit(JVM.RETURN); 
    } else if (ast.I.spelling.equals("main")) {
      // In case VC's main does not have a return itself
      emit(JVM.RETURN);
    } else
      emit(JVM.NOP); 

    emit("");
    emit("; set limits used by this method");
    emit(JVM.LIMIT, "locals", frame.getNewIndex());

    emit(JVM.LIMIT, "stack", frame.getMaximumStackSize());
    emit(".end method");

    return null;
  }

  public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
    // nothing to be done
    return null;
  }

  public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
    Frame frame = (Frame) o;
    ast.index = frame.getNewIndex();
    String T = VCtoJavaType(ast.T);

    if (ast.T.isArrayType()){
      // System.out.println("Start local array var decl");
      ArrayType at = (ArrayType) ast.T;
      
      Type actualArrayType = at.T;
      T = VCtoJavaType(actualArrayType);
      
      emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " [" + T + " from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
      at.E.visit(this, o);
      frame.push();
      
      emit(JVM.NEWARRAY, VCtoArrayType(at));
			if (!ast.E.isEmptyExpr()) {
				/* set type for init Expr */
				ast.E.type = actualArrayType;
				/* store initialised stuff */
				ast.E.visit(this, o);
			}
			/* store obj ref in array variable */
			frame.pop();
			emit(JVM.ASTORE, ast.index);
      
    } else {

      emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());

      if (!ast.E.isEmptyExpr()) {
        ast.E.visit(this, o);
    
        if (ast.T.equals(StdEnvironment.floatType)) {
          // cannot call emitFSTORE(ast.I) since this I is not an
          // applied occurrence 
          if (ast.index >= 0 && ast.index <= 3) 
            emit(JVM.FSTORE + "_" + ast.index); 
          else
            emit(JVM.FSTORE, ast.index); 
          frame.pop();
        } else {
          // cannot call emitISTORE(ast.I) since this I is not an
          // applied occurrence 
          if (ast.index >= 0 && ast.index <= 3) 
            emit(JVM.ISTORE + "_" + ast.index); 
          else
            emit(JVM.ISTORE, ast.index); 
          frame.pop();
        }
      }
    }
    return null;
  }

  // Parameters

  public Object visitParaList(ParaList ast, Object o) {
    ast.P.visit(this, o);
    ast.PL.visit(this, o);
    return null;
  }

  public Object visitParaDecl(ParaDecl ast, Object o) {
    Frame frame = (Frame) o;
    ast.index = frame.getNewIndex();
    String T = VCtoJavaType(ast.T);

    emit(JVM.VAR + " " + ast.index + " is " + ast.I.spelling + " " + T + " from " + (String) frame.scopeStart.peek() + " to " +  (String) frame.scopeEnd.peek());
    return null;
  }

  // Arguments

  public Object visitArgList(ArgList ast, Object o) {
    ast.A.visit(this, o);
    ast.AL.visit(this, o);
    return null;
  }

  public Object visitArg(Arg ast, Object o) {
    ast.E.visit(this, o);
    return null;
  }

  public Object visitIntLiteral(IntLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emitICONST(Integer.parseInt(ast.spelling));
    frame.push();
    return null;
  }

  public Object visitFloatLiteral(FloatLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emitFCONST(Float.parseFloat(ast.spelling));
    frame.push();
    return null;
  }

  public Object visitBooleanLiteral(BooleanLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emitBCONST(ast.spelling.equals("true"));
    frame.push();
    return null;
  }

  public Object visitStringLiteral(StringLiteral ast, Object o) {
    Frame frame = (Frame) o;
    emit(JVM.LDC, "\"" + ast.spelling + "\"");
    frame.push();
    return null;
  }

  // Variables 

  public Object visitSimpleVar(SimpleVar ast, Object o) {
    ast.I.visit(this, o);
    return null;
  }

  // Auxiliary methods for byte code generation

  // The following method appends an instruction directly into the JVM 
  // Code Store. It is called by all other overloaded emit methods.

  private void emit(String s) {
    JVM.append(new Instruction(s)); 
  }

  private void emit(String s1, String s2) {
    emit(s1 + " " + s2);
  }

  private void emit(String s1, int i) {
    emit(s1 + " " + i);
  }

  private void emit(String s1, float f) {
    emit(s1 + " " + f);
  }

  private void emit(String s1, String s2, int i) {
    emit(s1 + " " + s2 + " " + i);
  }

  private void emit(String s1, String s2, String s3) {
    emit(s1 + " " + s2 + " " + s3);
  }

  private void emitIF_ICMPCOND(String op, Frame frame) {
    String opcode;

    if (op.equals("i!="))
      opcode = JVM.IF_ICMPNE;
    else if (op.equals("i=="))
      opcode = JVM.IF_ICMPEQ;
    else if (op.equals("i<"))
      opcode = JVM.IF_ICMPLT;
    else if (op.equals("i<="))
      opcode = JVM.IF_ICMPLE;
    else if (op.equals("i>"))
      opcode = JVM.IF_ICMPGT;
    else // if (op.equals("i>="))
      opcode = JVM.IF_ICMPGE;

    String falseLabel = frame.getNewLabel();
    String nextLabel = frame.getNewLabel();

    emit(opcode, falseLabel);
    frame.pop(2); 
    emit("iconst_0");
    emit("goto", nextLabel);
    emit(falseLabel + ":");
    emit(JVM.ICONST_1);
    frame.push(); 
    emit(nextLabel + ":");
  }

  private void emitFCMP(String op, Frame frame) {
    String opcode;

    if (op.equals("f!=") || op.equals("!="))
      opcode = JVM.IFNE;
    else if (op.equals("f==") || op.equals("=="))
      opcode = JVM.IFEQ;
    else if (op.equals("f<") || op.equals("<"))
      opcode = JVM.IFLT;
    else if (op.equals("f<=") || op.equals("<="))
      opcode = JVM.IFLE;
    else if (op.equals("f>") || op.equals(">"))
      opcode = JVM.IFGT;
    else // if (op.equals("f>="))
      opcode = JVM.IFGE;

    String falseLabel = frame.getNewLabel();
    String nextLabel = frame.getNewLabel();

    emit(JVM.FCMPG);
    frame.pop(2);
    emit(opcode, falseLabel);
    emit(JVM.ICONST_0);
    emit("goto", nextLabel);
    emit(falseLabel + ":");
    emit(JVM.ICONST_1);
    frame.push();
    emit(nextLabel + ":");

  }

  private void emitILOAD(int index) {
    if (index >= 0 && index <= 3) 
      emit(JVM.ILOAD + "_" + index); 
    else
      emit(JVM.ILOAD, index); 
  }

  private void emitFLOAD(int index) {
    if (index >= 0 && index <= 3) 
      emit(JVM.FLOAD + "_"  + index); 
    else
      emit(JVM.FLOAD, index); 
  }

  private void emitGETSTATIC(String T, String I) {
    emit(JVM.GETSTATIC, classname + "/" + I, T); 
  }

  private void emitISTORE(Ident ast) {
    int index;
    if (ast.decl instanceof ParaDecl)
      index = ((ParaDecl) ast.decl).index; 
    else
      index = ((LocalVarDecl) ast.decl).index; 
    
    if (index >= 0 && index <= 3) 
      emit(JVM.ISTORE + "_" + index); 
    else
      emit(JVM.ISTORE, index); 
  }

  private void emitFSTORE(Ident ast) {
    int index;
    if (ast.decl instanceof ParaDecl)
      index = ((ParaDecl) ast.decl).index; 
    else
      index = ((LocalVarDecl) ast.decl).index; 
    if (index >= 0 && index <= 3) 
      emit(JVM.FSTORE + "_" + index); 
    else
      emit(JVM.FSTORE, index); 
  }

  private void emitPUTSTATIC(String T, String I) {
    emit(JVM.PUTSTATIC, classname + "/" + I, T); 
  }

  private void emitICONST(int value) {
    if (value == -1)
      emit(JVM.ICONST_M1); 
    else if (value >= 0 && value <= 5) 
      emit(JVM.ICONST + "_" + value); 
    else if (value >= -128 && value <= 127) 
      emit(JVM.BIPUSH, value); 
    else if (value >= -32768 && value <= 32767)
      emit(JVM.SIPUSH, value); 
    else 
      emit(JVM.LDC, value); 
  }

  private void emitFCONST(float value) {
    if(value == 0.0)
      emit(JVM.FCONST_0); 
    else if(value == 1.0)
      emit(JVM.FCONST_1); 
    else if(value == 2.0)
      emit(JVM.FCONST_2); 
    else 
      emit(JVM.LDC, value); 
  }

  private void emitBCONST(boolean value) {
    if (value)
      emit(JVM.ICONST_1);
    else
      emit(JVM.ICONST_0);
  }

  private String VCtoJavaType(Type t) {
    if (t.equals(StdEnvironment.booleanType))
      return "Z";
    else if (t.equals(StdEnvironment.intType))
      return "I";
    else if (t.equals(StdEnvironment.floatType))
      return "F";
    else // if (t.equals(StdEnvironment.voidType))
      return "V";
  }
  
  /********************* WORKSPACE *****************************/
  
  public Object visitAssignExpr(AssignExpr ast, Object o) {
    Frame frame = (Frame) o;
  
    if (ast.E1 instanceof ArrayExpr) {
      // emit(";;Start Array assign");
      ArrayExpr ae = (ArrayExpr) ast.E1;
      int index = getIndexFromArrayExpr(ae);
      emit(JVM.ALOAD + "_" + index);
      ae.E.visit(this, o);
      ast.E2.visit(this, o);
      emit(JVM.IASTORE);
      frame.push();
      // emit(";;end Array assign");
      return null;
    }
  
    ast.E2.visit(this, o);
    if (ast.parent instanceof AssignExpr) {
      emit(JVM.DUP);
      frame.push();
    }
    if (ast.E1 instanceof VarExpr) {
      VarExpr ve = (VarExpr) ast.E1;
      SimpleVar sv = (SimpleVar) ve.V;
      Ident id = (Ident) sv.I;
      Decl d = (Decl) id.decl;
      if (d instanceof GlobalVarDecl) {
        Type globalVarType = d.T;
        emitPUTSTATIC(VCtoJavaType(globalVarType), id.spelling);
      } else if (d instanceof LocalVarDecl) {
        Type t = d.T;
        if (t.isFloatType()) {
          emitFSTORE(id);
        } else if (t.isIntType()) {
          emitISTORE(id);
        }
      } 
    }
    frame.pop();
    return null;
  }
  
  public Object visitVarExpr(VarExpr ast, Object o) {
    Frame frame = (Frame) o;
    SimpleVar sv = (SimpleVar) ast.V;
    Ident id = (Ident) sv.I;
    Decl d = (Decl) id.decl;
    int index = 0;

    if (d instanceof LocalVarDecl) {
      index = getIndexFromLocalVarExpr(ast);
      if (d.T.isIntType() || d.T.isBooleanType()) {
         emitILOAD(index);
      } else if (d.T.isFloatType()) {
         emitFLOAD(index);
      }
    } else if (d instanceof ParaDecl) {
      index = getIndexFromParaExpr(ast);
      emitILOAD(index);
    } else if (d instanceof GlobalVarDecl) {
      Type globalVarType = d.T;
      emitGETSTATIC(VCtoJavaType(globalVarType), id.spelling);
    } else if (d instanceof FuncDecl) {
      System.out.println("here");
    }
    
    frame.push();
    return null;
  }
  
  // Loads the index of the array onto the stack
  public Object visitArrayExpr(ArrayExpr ast, Object o) {
    Frame frame = (Frame) o;
    int index = getIndexFromArrayExpr(ast);
    emitALOAD(index);
    ast.E.visit(this, o);
    Type t = getTypeFromArrayExpr(ast);
    // System.out.println(t);
    if (t.isIntType()) {
      emit(JVM.IALOAD);
    } else if (t.isBooleanType()) {
      emit(JVM.BALOAD);
    } else if (t.isFloatType()) {
      emit(JVM.FALOAD);
    } else {
      // TODO sort out this case
      System.out.println("Error in VisitArrayExpr");
    }
    frame.push();
    return null;
  }
  
  public Type getTypeFromArrayExpr(ArrayExpr ast) {
    SimpleVar se = (SimpleVar) ast.V;
    Ident i = se.I;
    Decl d = (Decl) i.decl;
    Type t = d.T;
    Type returnType = null;
    if (t instanceof ArrayType) {
      ArrayType at = (ArrayType) t;
      returnType = at.T;
    } else {
      returnType = t;
    }
    return returnType;
  }
  
  public void emitALOAD(int index) {
    emit(JVM.ALOAD + "_" + index);
  }
  
  public Object visitExprList(ExprList ast, Object o) {
    ast.E.visit(this, o);
    ast.EL.visit(this, o);
    return null;
  }
  
  public Object visitInitExpr(InitExpr ast, Object o) {
    Frame frame = (Frame) o;
    List l = ast.IL;
    int ArraySize = 0;
    
    while (!l.isEmpty()){
      ExprList el = (ExprList) l;

			frame.push();
			emit(JVM.DUP);
			// emit index at which to store
			frame.push();
			emitICONST(ArraySize);
			// emit value which we want to store
			el.E.visit(this, o);

			// Store instruction
			frame.pop(3);
			if (ast.type.isIntType()) {
				emit(JVM.IASTORE);
			} else if (ast.type.isBooleanType()) {
				emit(JVM.BASTORE);
			} else if (ast.type.isFloatType()) {
				emit(JVM.FASTORE);
			} else {    // This Case should not be possible
				throw new AssertionError("visitInitExpr: expect int, boolean, float type");
			}
			// Move to next AST 
			l = el.EL;
			ArraySize++;
    }
  
    return null;
  }
  
  public Object visitBinaryExpr(BinaryExpr ast, Object o) {
    Frame frame = (Frame) o;
    String op = ast.O.spelling;
    
    if (op.equals(">=")) {
      emitGreaterthanOrEqualExpr(ast, op, frame);
    } else if (op.equals("i>=")) {
      emitIGreaterthanOrEqualExpr(ast, op, frame);
    } else if (op.equals("f>=")) {
      emitFGreaterthanOrEqualExpr(ast, op, frame);
    } else if (op.equals("<=")) {
      emitLessThanOrEqualExpr(ast, op, frame);
    } else if (op.equals("i<=")) {
      emitILessThanOrEqualExpr(ast, op, frame);
    } else if (op.equals("f<=")) {
      emitFLessThanOrEqualExpr(ast, op, frame);
    } else if (op.equals("==")){
      emitEqualEqualCompareExpr(ast, op, frame);
    } else if (op.equals("i==")) {
      emitIEqualEqualCompareExpr(ast, op, frame);
    } else if (op.equals("f==")) {
      emitFEqualEqualCompareExpr(ast, op, frame);
    } else if (op.equals("!=")){
      emitNotEqualExpr(ast, op, frame);
    } else if (op.equals("i!=")) {
      emitINotEqualExpr(ast, op, frame);
    } else if (op.equals("f!=")) {
      emitFNotEqualExpr(ast, op, frame);
    } else if (op.equals(">")) { 
      emitGreaterthanExpr(ast, op, frame);
    } else if (op.equals("i>")) { 
      emitIGreaterthanExpr(ast, op, frame);
    } else if (op.equals("f>")) { 
      emitFGreaterThanExpr(ast, op, o);
    } else if (op.equals("<")) {
      emitLessThanExpr(ast, op, o);
    } else if (op.equals("i<")) {
      emitILessThanExpr(ast, op, o);
    } else if (op.equals("f<")) {
      emitFLessThanExpr(ast, op, o);
    } else if (op.equals("-")) {
      emitMinusExpr(ast, op, frame);
    } else if (op.equals("i-")) {
      emitIMinusExpr(ast, op, frame);
    } else if (op.equals("f-")) {
      emitFMinusExpr(ast, op, frame);
    } else if (op.equals("*")) {
      emitMultExpr(ast, op, frame);
    } else if (op.equals("i*")) {
      emitIMultExpr(ast, op, frame);
    } else if (op.equals("f*")) {
      emitFMultExpr(ast, op, frame);
    } else if (op.equals("/")) {
      emitDivExpr(ast, op, frame);
    } else if (op.equals("i/")) {
      emitIDivExpr(ast, op, frame);
    } else if (op.equals("f/")) {
      emitFDivExpr(ast, op, frame);
    } else if (op.equals("+")) {
      emitAddExpr(ast, op, frame);
    } else if (op.equals("i+")) {
      emitIAddExpr(ast, op, frame);
    } else if (op.equals("f+")) {
      emitFAddExpr(ast, op, frame);
    } else if (op.equals("&&")) {
      emitANDExpr(ast, op, o);
    } else if (op.equals("i&&")) {
      emitANDExpr(ast, op, o);
    } else if (op.equals("||")) {
      emitORExpr(ast, op, o);
    } else if (op.equals("i||")) {
      emitORExpr(ast, op, o);
    }
    return null;
  }
  
  public Object visitUnaryExpr(UnaryExpr ast, Object o) {
    Frame frame = (Frame) o;
    String op = ast.O.spelling;
    
    ast.E.visit(this, o);
    
    if (op.equals("i2f")) {
      emit(JVM.I2F);
    } else if (op.equals("-")) {
      emit(JVM.INEG);
    } else if (op.equals("i-")) {
      emit(JVM.INEG);
    } else if (op.equals("!")) {
      String falseLabel = frame.getNewLabel();
			String doneLabel = frame.getNewLabel();
      frame.pop();
			emit(JVM.IFEQ, falseLabel);
			
			// expr is true and therefore load false onto the stack
			emit(JVM.ICONST_0);
			emit(JVM.GOTO, doneLabel);
			
			// expr is false and therefore load true onto the stack
			emit(falseLabel + ":");
			emit(JVM.ICONST_1);
			
			// come here after loading 1 or 0 
			emit(doneLabel + ":");
			frame.push();
    }
    // Nothing to be done in the case of +
    return null;
  }
  
  public Object visitExprStmt(ExprStmt ast, Object o) {
    ast.E.visit(this, o);
    return null;
  }
  
  public Object visitContinueStmt(ContinueStmt ast, Object o) {
    Frame frame = (Frame) o;
    String continueLabel = frame.conStack.peek();
    emit(JVM.GOTO, continueLabel);
    return null;
  }
  
  public Object visitBreakStmt(BreakStmt ast, Object o) {
    Frame frame = (Frame) o;
    String breakLabel = frame.brkStack.peek();
    emit(JVM.GOTO, breakLabel);
    return null;
  }
  
  public Object visitForStmt(ForStmt ast, Object o) {
    Frame frame = (Frame) o;
    String continueLabel = frame.getNewLabel();
    String breakLabel = frame.getNewLabel();
    
    frame.conStack.push(continueLabel);    
    frame.brkStack.push(breakLabel);
    
    ast.E1.visit(this, o);
    emit(continueLabel + ":");
    ast.E2.visit(this, o);
    emit(JVM.IFGT, breakLabel);
    ast.E3.visit(this, o);
    ast.S.visit(this, o);
    emit(JVM.GOTO, continueLabel);
    emit(breakLabel + ":");
    
    frame.conStack.pop();
    frame.brkStack.pop();
    
    return null;
  }
  
  public Object visitWhileStmt(WhileStmt ast, Object o) {
    Frame frame = (Frame) o;
    String continueLabel = frame.getNewLabel();
    String breakLabel = frame.getNewLabel();
    
    frame.conStack.push(continueLabel);    
    frame.brkStack.push(breakLabel);
    
    emit(continueLabel + ":");
    ast.E.visit(this, o);
    emit(JVM.IFEQ, breakLabel);
    ast.S.visit(this, o);
    emit(JVM.GOTO, continueLabel);
    emit(breakLabel + ":");
    
    frame.conStack.pop();
    frame.brkStack.pop();
    
    return null;
  }
  
  public Object visitIfStmt(IfStmt ast, Object o) {
    // emit(";; Visit If Stmt here");
    Frame frame = (Frame) o;
    String firstLabel = frame.getNewLabel();
    String secondLabel = frame.getNewLabel();
    
    ast.E.visit(this, o);
    emit(JVM.IFEQ, firstLabel);
    // System.out.println(frame.getCurStackSize());
    frame.pop();
    ast.S1.visit(this, o);
    emit(JVM.GOTO, secondLabel);
    emit(firstLabel + ":");
    ast.S2.visit(this, o);
    emit(secondLabel + ":");
    // emit(";; Finish If Stmt here");
    return null;
  }
  

  
  /******************* Helper functions **************************/
  
  public int getIndexFromArrayExpr(ArrayExpr ast) {
    SimpleVar sv = (SimpleVar) ast.V;
    Ident i = sv.I;
    Decl d  = (Decl) i.decl;
    int index = ((LocalVarDecl) d).index; 
    return index;
  }
  
  // This is the case were op = 'i>='
  public void emitIGreaterthanOrEqualExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    String firstLabel = frame.getNewLabel();
    String secondLabel = frame.getNewLabel();
    
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitIF_ICMPCOND(op, frame);
  }
  
  public void emitIGreaterthanExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitIF_ICMPCOND(op, frame);
  }
  
  // Case were op = '=='
  public void emitIEqualEqualCompareExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitIF_ICMPCOND(op, frame);
  }

  public void emitIDivExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emit(JVM.IDIV);
    frame.pop(2);
    frame.push();
  }
  
  public void emitFDivExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emit(JVM.FDIV);
    frame.pop(2);
    frame.push();
  }
  
  public void emitIMultExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emit(JVM.IMUL);
    frame.pop(2);
    frame.push();
  }
  
  public void emitFMultExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emit(JVM.FMUL);
    frame.pop(2);
    frame.push();
  }
  
  public void emitIMinusExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emit(JVM.ISUB);
    frame.pop(2);
    frame.push();
  }

  public void emitFMinusExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emit(JVM.FSUB);
    frame.pop(2);
    frame.push();
  }

public void emitNotEqualExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitINotEqualExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){ 
      emitFNotEqualExpr(ast, op, o);
    } else {
      System.out.println("issue in bin function -> emitNotEqualExpr");
    }
  }
  
  public void emitINotEqualExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitIF_ICMPCOND(op, frame);
  }
  
  public void emitFNotEqualExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitFCMP(op, frame);
  }
  
  public void emitLessThanOrEqualExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitILessThanOrEqualExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){ 
      emitFLessThanOrEqualExpr(ast, op, o);
    } else {
      System.out.println("issue in bin function -> emitLessThanOrEqualExpr");
    }
  }
  
  public void emitGreaterthanOrEqualExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitIGreaterthanOrEqualExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){ 
      emitFGreaterthanOrEqualExpr(ast, op, o);
    } else {
      System.out.println("issue in bin function -> emitGreaterthanOrEqualExpr");
    }
  }
  
  public void emitILessThanOrEqualExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitIF_ICMPCOND(op, frame);
  }
  
  public void emitFLessThanOrEqualExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitFCMP(op, frame);
  }
  
  public void emitFGreaterthanOrEqualExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitFCMP(op, frame);
  }
  
  public void emitEqualEqualCompareExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitIEqualEqualCompareExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){ 
      emitFEqualEqualCompareExpr(ast, op, o);
    } else {
      System.out.println("issue in bin function -> Equal Equals");
    }
  }
  
  public void emitFEqualEqualCompareExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitFCMP(op, frame);
  }
  
  public void emitGreaterthanExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitIGreaterthanExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){ 
      emitFGreaterThanExpr(ast, op, o);
    } else {
      System.out.println("issue in bin function -> less than");
    }
  }
  
  public void emitLessThanExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitILessThanExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){ 
      emitFLessThanExpr(ast, op, o);
    } else {
      System.out.println("issue in bin function -> less than");
    }
  }
  
  public void emitFGreaterThanExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitFCMP(op, frame);
  }
  
  public void emitFLessThanExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emitFCMP(op, frame);
  }
  
  public void emitILessThanExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    String firstLabel = frame.getNewLabel();
    String secondLabel = frame.getNewLabel();
    
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    emit(JVM.IF_ICMPLT, firstLabel);
    emit(JVM.ICONST_1);
    emit(JVM.GOTO, secondLabel);
    emit(firstLabel + ":");
    emit(JVM.ICONST_0);
    emit(secondLabel + ":");
    frame.pop(2);
    frame.push();
  }
  
  // Assumes that both exprs are Ints
  public void emitIAddExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    
    emit(JVM.IADD);
    frame.pop(2);
    frame.push();
  }
  
  // Assumes that at least one expr is a float
  public void emitFAddExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    ast.E1.visit(this, o);
    ast.E2.visit(this, o);
    
    emit(JVM.FADD);
    frame.pop(2);
    frame.push();
  }
  
  public Type getTypeFromUnaryExpr(UnaryExpr ast) {
    String op = ast.O.spelling;
    if (op.equals("i2f")) {
      return StdEnvironment.floatType;
    } else {
      return ast.E.type;
    }
  }


  public String VCtoArrayType(Type t) {
    Type actualArrayType = ((ArrayType) t).T;
		if (actualArrayType.equals(StdEnvironment.booleanType))
			return JVM.BOOLEAN;
		else if (actualArrayType.equals(StdEnvironment.intType))
			return JVM.INT;
		else if (actualArrayType.equals(StdEnvironment.floatType))
			return JVM.FLOAT;
		else 
			throw new AssertionError("should only get boolean int or float for array type");
  }

  public int getIndexFromLocalVarExpr(VarExpr ast) {
    SimpleVar sv = (SimpleVar) ast.V;
    Ident id = (Ident) sv.I;
    int index;
    index = ((LocalVarDecl) id.decl).index; 
    return index;
  }
  
  public int getIndexFromParaExpr(VarExpr ast) {
    SimpleVar sv = (SimpleVar) ast.V;
    Ident id = (Ident) sv.I;
    int index;
    index = ((ParaDecl) id.decl).index; 
    return index;
  }

  public void emitMultExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitIMultExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){
      emitFMultExpr(ast, op, o);
    } else {
      System.out.println("issue in BinaryExpr -> emitMultExpr");
    }
  }  
  
  public void emitDivExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitIDivExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){
      emitFDivExpr(ast, op, o);
    } else {
      System.out.println("issue in BinaryExpr -> emitDivExpr");
    }
  }
  
  public void emitAddExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitIAddExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){
      emitFAddExpr(ast, op, o);
    } else {
      System.out.println("issue in BinaryExpr -> emitAddExpr");
    }
  }

  public Type getTypeFromExpr(Expr ast) {
    if (ast instanceof UnaryExpr) {
      return getTypeFromUnaryExpr((UnaryExpr) ast);
    } else if (ast instanceof BinaryExpr) {
      BinaryExpr be = (BinaryExpr) ast;
      Type t1 = getTypeFromExpr(be.E1);
      Type t2 = getTypeFromExpr(be.E2);
      if (t1.equals(t2)) {
        return t1;
      } else if (t1.isFloatType() || t2.isFloatType()) {
        return StdEnvironment.floatType;
      } else if (t1.assignable(t2)) {
        return t2;
      } else if (t2.assignable(t1)) {
        return t1;
      } else {
        System.out.println("Oh !@#$ - Error in getTypeFromExpr function");
        System.out.println(t1);
        System.out.println(t2);
        return StdEnvironment.intType;        // Just return Int by default
      }
    } else if (ast instanceof CallExpr) {
      CallExpr ce = (CallExpr) ast;
      return getTypeFromCallExpr(ce);
    } else if (ast instanceof ArrayExpr) {
      ArrayExpr ae = (ArrayExpr) ast;
      return getTypeFromArrayExpr(ae);
    } else {
      return ast.type;
    }
  }

  public void emitORExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    String firstLabel = frame.getNewLabel();
    String secondLabel = frame.getNewLabel();
    ast.E1.visit(this, o);
    emit(JVM.IFNE, firstLabel);
    ast.E2.visit(this, o);
    emit(JVM.IFNE, firstLabel);
    emit(JVM.ICONST_0);
    emit(JVM.GOTO, secondLabel);
    emit(firstLabel + ":");
    emit(JVM.ICONST_1);
    emit(secondLabel + ":");
  }
  
  public void emitANDExpr(BinaryExpr ast, String op, Object o) {
    Frame frame = (Frame) o;
    String firstLabel = frame.getNewLabel();
    String secondLabel = frame.getNewLabel();
    ast.E1.visit(this, o);
    emit(JVM.IFEQ, firstLabel);
    ast.E2.visit(this, o);
    emit(JVM.IFEQ, firstLabel);
    emit(JVM.ICONST_1);
    emit(JVM.GOTO, secondLabel);
    emit(firstLabel + ":");
    emit(JVM.ICONST_0);
    emit(secondLabel + ":");
    frame.pop(2);
    frame.push();
  }

  public void emitMinusExpr(BinaryExpr ast, String op, Object o) {
    Type t1 = getTypeFromExpr(ast.E1);
    Type t2 = getTypeFromExpr(ast.E2);
    if (t1.isIntType() && t2.isIntType()) {
      emitIMinusExpr(ast, op, o);
    } else if (t1.isFloatType() || t2.isFloatType()){
      emitFMinusExpr(ast, op, o);
    } else {
      System.out.println("issue in BinaryExpr -> emitMinusExpr");
    }
  }  





// Nothing to be done with empties
  public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
    return null;
  }

  public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
    return null;
  }

  public Object visitEmptyStmt(EmptyStmt ast, Object o) {
    return null;
  }

  public Object visitEmptyExpr(EmptyExpr ast, Object o) {
    return null;
  }
  
  public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
    return null;
  }

  public Object visitEmptyParaList(EmptyParaList ast, Object o) {
    return null;
  }
  
  public Object visitEmptyExprList(EmptyExprList ast, Object o) {
    return null;
  }
  
  public Object visitEmptyArgList(EmptyArgList ast, Object o) {
    return null;
  }
  
    public Object visitArrayType(ArrayType ast, Object o) {
    return null;
  }
  
  public Object visitStringType(StringType ast, Object o) {
    return null;
  }

  public Object visitOperator(Operator ast, Object o) {
    return null;
  }
  
    // Types

  public Object visitIntType(IntType ast, Object o) {
    return null;
  }

  public Object visitFloatType(FloatType ast, Object o) {
    return null;
  }

  public Object visitBooleanType(BooleanType ast, Object o) {
    return null;
  }

  public Object visitVoidType(VoidType ast, Object o) {
    return null;
  }

  public Object visitErrorType(ErrorType ast, Object o) {
    return null;
  }

  // Literals, Identifiers and Operators 

  public Object visitIdent(Ident ast, Object o) {
    return null;
  }

// No need to touch these functions
  public Object visitIntExpr(IntExpr ast, Object o) {
    ast.IL.visit(this, o);
    return null;
  }

  public Object visitFloatExpr(FloatExpr ast, Object o) {
    ast.FL.visit(this, o);
    return null;
  }

  public Object visitBooleanExpr(BooleanExpr ast, Object o) {
    ast.BL.visit(this, o);
    return null;
  }

  public Object visitStringExpr(StringExpr ast, Object o) {
    ast.SL.visit(this, o);
    return null;
  }

  // Declarations

  public Object visitDeclList(DeclList ast, Object o) {
    ast.D.visit(this, o);
    ast.DL.visit(this, o);
    return null;
  }


}
