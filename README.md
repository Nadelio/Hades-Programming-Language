# Hades Programming Language
### Overview
Hades is an Assembly mimic intended to target the eBin Bytecode.\
Hades is a programming language built for easier development within the [ePUx32 Computer Simulator](https://github.com/Nadelio/ePU)

### Getting Started
1. Download the newest release of the `Hades-Language.jar` file (check the Github release page)
  - Alternatively, you can build from source using: `bash -c 'javac -d ./src/classFiles ./src/**/*.java' && jar cvfm ./bin/HadesLanguage.jar ./src/manifest.txt -C ./src/classFiles .`
2. Put the `.jar` and the `.bat` file in the same folder as where you want to put your files
3. Write your Hades program
\
**For Linux Systems:**
4. Run the `hades` file using the `./hades -[flags] [file]` format
  - Use `-fc` flag to compile the given file to eBin
  - Use `-fr` flag to run the given file
  - Use `-fce` flag to compile a ePU formatted Hades file to relevant eBin
\
**For Windows Systems:**
4. Run the bat script using the `./hades.bat -[flags] [file]` format
  - Use `-fc` flag to compile the given file to eBin
  - Use `-fr` flag to run the given file
  - Use `-fce` flag to compile a ePU formatted Hades file to relevant eBin
5. Profit

### Roadmap
- [x] v1.0.0: Get the damn thing working
- [ ] v1.1.0: Add new instructions for label manipulation
  - Add `Hand` single-cell label register
    - `HOLD [label]` and `DROP` instructions for adding/removing labels from the `Hand`
    - `MLB [num]`, `MLP`, `SLB [num]`, `SLV` held label manipulation instructions
  - `WDD [num0 num1 num2 ... numN]` for writing large amounts of data at once to the `Tape`
  - `DS [num0 num1 num2 ... numN] [alias]` for referencing a large portion of data, such as strings, can be written to Tape using `WDD` instruction
  - `FUNC [alias] [ body ]` (QoL change), `CDP [file] [alias]` will still exist
  - `OUTN`, `OUTV [num]`, and `OUTR [startPos endPos]` for different I/O choices. `OUTV [num]` outputs the character related to the given number, `OUTN` outputs the raw number at `Tape[Pointer]`, and `OUTR [startPos endPos]` outputs a range of the Tape.
  - `INV` and `INS` for more I/O choices. `INV` for taking in a raw number, `INS` for taking in a string (that will then be split into characters and converted into integers and stored)
  - `FSO` and `FSC` for opening file streams for reading in file contents, `FSO` takes in a file path or a data structure alias, and 0 or 1 for read or write mode. `FSC` closes a file stream connected to the given file path/data structure alias
  - `RFF` and `WTF` to read/write from/to a file, `RFF` takes in a data structure alias/string and a destination address, `RFF` reads in the next character and stores it in the destination address, `WTF` writes the given value to the given file at the very end of the file
  - `SWM` to set the write mode, 0 indicates writing a character, 1 indicates writing the raw integer value
  - String literals `"..."`, these can be used as input for the `DS` instruction and the `WDD` instruction, as well as any instruction that takes in a file path
- [ ] v1.2.0: Add ability to use labels in place of numbers in all instructions that can take in a number
- [ ] v2.0.0: Add `package` system which adds new systems for defining types, defining instructions, defining instruction arguments, and defining instruction behaviors using a mix of Java and Hades
  - Add new file type: `*.pkg.hds`
  - Add new instructions `DPKG [package]`, `UPKG [package]`, `INST [instruction name] [args] [args]`, `TYPE [type name] [body]`
### Examples
Say "Hello" in Hades:
```nasm
WDD ["Hello, World!"]
OUTR[0 12]
HLT
```
Addition in Hades:
```nasm
WRT [1] ; write 1 to Tape ;
INCP ; move ptr to the right
WRT [2] ; write 2 to Tape ;
LOOP [ ; start a loop ;
    DECP ; move ptr to the left ;
    RDV  ; read value from Tape ;
    INCV ; increment ptr value ;
    WTV ; write ptr value to Tape ;
    INCP ; move ptr to the right ;
    RDV ; read value from Tape ;
    DECV ; decrement ptr value ;
    WTV ; write ptr value to Tape ;
]
DECP ; move ptr to the left ;
RDV ; read value from Tape ;
; end program ;
HLT
```
Using labels in Hades:
```nasm
CLB [foo] ; create label called "foo" ;
MOV [5] ; move ptr to position 5 of Tape ;
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
