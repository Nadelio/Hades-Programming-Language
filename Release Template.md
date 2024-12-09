# Hades v1.X.X
<!-- Major updates are fundamental changes to the overall behavior of Hades, they will be denoted by the first number in the version code -->
<!-- Instruction changes/additions/removals will all be denoted in the second number -->
<!-- Bug fixes and Hotfixes will be denoted in the last number -->
<!-- Hot fixes are any bugfixes that happen within 2 weeks of the most recent Major/Minor release -->

### Version Type: Mewo<!-- Major Update/Instruction change/Instruction Addition/Instruction Removal/Bugfix/Hotfix -->
<br></br>
## Changelog:
### New Instructions:
- `MEWO [some arg] [other arg]` <!-- num/label/postion/value/alias/file -->

### Instruction Changes:
- `MEWO`: No longer takes in 2 args, now always outputs `MEWO` instead of changing based on args

### Removed Instructions:
- `MEWO`

<br></br>
## Bugfixes: <!-- Rename to Hotfix if it is a hotfix release -->
- Issue #0001: `MEWO` instruction doesn't always output `MEWO`
- `MEWO` misspelled in lexer as `MEOW`
