.class public t15_para
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
.method test(I)I
L0:
.var 0 is this Lt15_para; from L0 to L1
.var 1 is a I from L0 to L1
.var 2 is b I from L0 to L1
	iconst_0
	istore_2
	iload_1
	iconst_2
	iadd
	istore_2
	iload_2
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Lt15_para; from L0 to L1
	new t15_para
	dup
	invokenonvirtual t15_para/<init>()V
	astore_1
.var 2 is a I from L0 to L1
	iconst_0
	istore_2
	aload_1
	iconst_3
	invokevirtual t15_para/test(I)I
	istore_2
	aload_1
	iconst_3
	invokevirtual t15_para/test(I)I
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
