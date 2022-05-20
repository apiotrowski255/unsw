.class public t12_or
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
.var 1 is vc$ Lt12_or; from L0 to L1
	new t12_or
	dup
	invokenonvirtual t12_or/<init>()V
	astore_1
.var 2 is x I from L0 to L1
	iconst_0
	istore_2
	iconst_1
	ifne L4
	iconst_0
	ifne L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L2
L6:
	iconst_1
	istore_2
L7:
	goto L3
L2:
L8:
	iconst_2
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
