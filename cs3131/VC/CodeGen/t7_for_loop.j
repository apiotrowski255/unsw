.class public t7_for_loop
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
.var 1 is vc$ Lt7_for_loop; from L0 to L1
	new t7_for_loop
	dup
	invokenonvirtual t7_for_loop/<init>()V
	astore_1
.var 2 is i I from L0 to L1
	iconst_0
	istore_2
	iconst_0
	istore_2
L2:
	iload_2
	bipush 100
	if_icmplt L4
	iconst_1
	goto L5
L4:
	iconst_0
L5:
	ifgt L3
	iload_2
	iconst_1
	iadd
	istore_2
L6:
	iload_2
	invokestatic VC/lang/System/putIntLn(I)V
L7:
	goto L2
L3:
	iload_2
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 3
.end method
