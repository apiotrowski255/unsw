.class public t8_floats
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
.var 1 is vc$ Lt8_floats; from L0 to L1
	new t8_floats
	dup
	invokenonvirtual t8_floats/<init>()V
	astore_1
.var 2 is i F from L0 to L1
	ldc 5.1
	fstore_2
.var 3 is j F from L0 to L1
	ldc 5.3432345
	fstore_3
.var 4 is k F from L0 to L1
	fload_2
	fload_3
	fadd
	fstore 4
	fload_2
	invokestatic VC/lang/System/putFloatLn(F)V
L1:
	return
	
	; set limits used by this method
.limit locals 5
.limit stack 2
.end method
