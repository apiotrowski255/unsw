.class public t13_unary
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
.method y()Z
L0:
.var 0 is this Lt13_unary; from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Lt13_unary; from L0 to L1
	new t13_unary
	dup
	invokenonvirtual t13_unary/<init>()V
	astore_1
.var 2 is x I from L0 to L1
	iconst_0
	istore_2
	aload_1
	invokevirtual t13_unary/y()Z
	ifeq L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L2
L6:
	iconst_3
	istore_2
L7:
	goto L3
L2:
L8:
	iconst_5
	istore_2
L9:
L3:
	iload_2
	invokestatic VC/lang/System/putIntLn(I)V
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
