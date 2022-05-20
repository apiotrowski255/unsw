.class public t14_float_EqualEquals
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
.var 1 is vc$ Lt14_float_EqualEquals; from L0 to L1
	new t14_float_EqualEquals
	dup
	invokenonvirtual t14_float_EqualEquals/<init>()V
	astore_1
.var 2 is x F from L0 to L1
	ldc 5.1
	fstore_2
.var 3 is y F from L0 to L1
	ldc 5.1
	fstore_3
	fload_2
	fload_3
	fcmpg
	ifeq L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L2
L6:
	iconst_0
	invokestatic VC/lang/System/putIntLn(I)V
L7:
	goto L3
L2:
L8:
	iconst_1
	invokestatic VC/lang/System/putIntLn(I)V
L9:
L3:
	return
L1:
	return
	
	; set limits used by this method
.limit locals 4
.limit stack 2
.end method
