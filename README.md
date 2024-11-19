# Hades Programming Language

Hades is an Assembly mimic intended to target the eBin Bytecode.\
Hades is a programming language built for easier development within the [ePUx32 Computer Simulator](https://github.com/Nadelio/ePU)

# 
\
Hello World in Hades:
```nasm
WRT [40] ; H ;
OUT ; print to terminal ;
WRT [69] ; e ;
OUT
WRT [76] ; l ;
OUT ; print l twice ;
OUT
WRT [79] ; o ;
OUT
WRT [0] ; whitespace ;
OUT
WRT [55] ; W ;
OUT
WRT [79] ; o ;
OUT
WRT [82] ; r ;
OUT
WRT [76] ; l ;
OUT
WRT [68] ; d ;
OUT
WRT [1] ; ! ;
OUT
; end program ;
HLT
```
Addition in Hades:
```nasm
WRT [1] ; write 1 to RAM ;
INCP ; move ptr to the right
WRT [2] ; write 2 to RAM ;
LOOP [ ; start a loop ;
    DECP ; move ptr to the left ;
    RDV  ; read value from RAM ;
    INCV ; increment ptr value ;
    WTV ; write ptr value to RAM ;
    INCP ; move ptr to the right ;
    RDV ; read value from RAM ;
    DECV ; decrement ptr value ;
    WTV ; write ptr value to RAM ;
]
DECP ; move ptr to the left ;
RDV ; read value from RAM ;
; end program ;
HLT
```
Using labels in Hades:
```nasm
CLB [foo] ; create label called "foo" ;
MOV [5] ; move ptr to position 5 of RAM ;
CLB [bar] ; create label called "bar" ;
JLB [foo] ; jump to foo label ;
WRT [1] ; write 1 to foo ;
JLB [bar] ; jump back to bar label ;
WRT [2] ; write 2 to bar ;
DLB [foo] ; delete foo label ;
DLB [bar] ; delete bar label ;
; end program ;
HLT
```
Using functions in Hades:
```nasm
CDP [someFile.eBin] [foo] ; create function from file "someFile.eBin" and call it "foo" ;
; for ePU development ;
CDP [0 0] [foo] ; create function from position in ROM, follows [X Y] format;
CALL [foo] ; run "foo" function ;
; end program ;
HLT
```
Using Syscalls in Hades (ePU only):
```nasm
; syscalls always need 5 parameters, they can be labels or numbers ;
SYS [14 0 0 0 0] ; print to terminal syscall ;

CLB[zero]
INCP
CLB [foo]
WRT [14]
SYS [foo zero zero zero zero] ; print to terminal syscall but using labels called "foo" and "zero" ;

; check the ePUx32 syscall documentation for help on the syscall arguments ;

; end program ;
HLT
```
