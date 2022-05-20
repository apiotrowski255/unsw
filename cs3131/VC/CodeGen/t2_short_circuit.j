.class public t2_short_circuit
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
.method f()Z
L0:
.var 0 is this Lt2_short_circuit; from L0 to L1
	iconst_0
	invokestatic VC/lang/System/putBoolLn(Z)V
	iconst_0
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
.var 1 is vc$ Lt2_short_circuit; from L0 to L1
	new t2_short_circuit
	dup
	invokenonvirtual t2_short_circuit/<init>()V
	astore_1
	iconst_0
	ifeq L2
	aload_1
	invokevirtual t2_short_circuit/f()Z
	ifeq L2
	iconst_1
	goto L3
L2:
	iconst_0
L3:
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
