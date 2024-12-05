# Hades Programming Language
### Overview
Hades is an Assembly mimic intended to target the eBin Bytecode.\
Hades is a programming language built for easier development within the [ePUx32 Computer Simulator](https://github.com/Nadelio/ePU)

### Getting Started
1. Download the newest release of the `Hades-Language.jar` file (check the Github release page)
2. Put the `.jar` and the `.bash` file in the same folder as where you want to put your files
3. Write your Hades program
4. Run the bash script using the `hades -[flags] [file]` format
  - Use `-fc` flag to compile the given file to eBin
  - Use `-fr` flag to run the given file
  - Use `-fce` flag to compile a ePU formatted Hades file to relevant eBin
5. Profit

### Examples
Say "Hello" in Hades:
```nasm
CLB [start]
; Hello ;
WRT [72]
INCP
WRT [101]
INCP
WRT [108]
INCP
WRT [108]
INCP
WRT [111]
INCP
WRT [32] ; space ;
INCP
; Underworld! ;
WRT [85]
INCP
WRT [110]
INCP
WRT [100]
INCP
WRT [101]
INCP
WRT [114]
INCP
WRT [119]
INCP
WRT [111]
INCP
WRT [114]
INCP
WRT [108]
INCP
WRT [100]
INCP
WRT [33]
INCP
WRT [0]
JLB [start]
LOOP [
    OUT
    INCP
]
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
