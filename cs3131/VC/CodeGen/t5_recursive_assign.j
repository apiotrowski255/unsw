.class public t5_recursive_assign
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
.var 1 is vc$ Lt5_recursive_assign; from L0 to L1
	new t5_recursive_assign
	dup
	invokenonvirtual t5_recursive_assign/<init>()V
	astore_1
.var 2 is a I from L0 to L1
	iconst_0
	istore_2
.var 3 is b I from L0 to L1
	iconst_0
	istore_3
.var 4 is c I from L0 to L1
	iconst_0
	istore 4
	bipush 10
	dup
	istore 4
	dup
	istore_3
	istore_2
	iload_2
	invokestatic VC/lang/System/putIntLn(I)V
	iload_3
	invokestatic VC/lang/System/putIntLn(I)V
	iload 4
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	
	; set limits used by this method
.limit locals 5
.limit stack 2
.end method
