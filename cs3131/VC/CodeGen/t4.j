.class public t4
.super java/lang/Object
	
	
	; standard class static initializer 
.method static <clinit>()V
	
	
	; set limits used by this method
.limit locals 0
.limit stack 0
	return
.end method
	
	; standard constructor initializer 
.method public <init>()V
.limit stack 1
.limit locals 1
	aload_0
	invokespecial java/lang/Object/<init>()V
	return
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Lt4; from L0 to L1
	new t4
	dup
	invokenonvirtual t4/<init>()V
	astore_1
.var 2 is i I from L0 to L1
	iconst_3
	istore_2
.var 3 is r I from L0 to L1
	iconst_0
	istore_3
.var 4 is k I from L0 to L1
	bipush 9
	istore 4
.var 5 is w I from L0 to L1
	iconst_4
	istore 5
.var 6 is a I from L0 to L1
	iconst_1
	istore 6
.var 7 is b I from L0 to L1
	iconst_2
	istore 7
.var 8 is j I from L0 to L1
	iconst_4
	istore 8
	iload 8
	iload 7
	iadd
	bipush 10
	iadd
	istore_2
	iload_2
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	
	; set limits used by this method
.limit locals 9
.limit stack 3
.end method
