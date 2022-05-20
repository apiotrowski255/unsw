.class public t3_array
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
.var 1 is vc$ Lt3_array; from L0 to L1
	new t3_array
	dup
	invokenonvirtual t3_array/<init>()V
	astore_1
.var 2 is a [I from L0 to L1
	bipush 10
	newarray int
	dup
	iconst_0
	iconst_0
	iastore
	dup
	iconst_1
	iconst_0
	iastore
	dup
	iconst_2
	iconst_0
	iastore
	dup
	iconst_3
	iconst_0
	iastore
	dup
	iconst_4
	iconst_0
	iastore
	dup
	iconst_5
	iconst_0
	iastore
	dup
	bipush 6
	iconst_0
	iastore
	dup
	bipush 7
	iconst_0
	iastore
	dup
	bipush 8
	iconst_0
	iastore
	dup
	bipush 9
	iconst_0
	iastore
	astore 2
.var 3 is i I from L0 to L1
	iconst_1
	istore_3
.var 4 is j I from L0 to L1
	iconst_2
	istore 4
	aload_2
	iload_3
	iload 4
	iadd
	iload 4
	bipush 10
	iadd
	iastore
	aload_2
	iload_3
	iload 4
	iadd
	iaload
	invokestatic VC/lang/System/putIntLn(I)V
	return
L1:
	return
	
	; set limits used by this method
.limit locals 5
.limit stack 6
.end method
