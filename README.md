# RePack
A minecraft texture pack creation tool with one major intent: decreasing effort, increasing productivity. Summed up, RePack takes away all of the time consuming parts of making a texture pack and lets you define once, then reuse multiple times.

## How?
RePack works by defining a custom texture pack format, which is read from a workspace directory of any desired file structure and then compiled to your usual minecraft texture pack. Keep in mind that some parts require Optifine to work ingame.

## Docs
The RePack docs can be found ![here](https://github.com/ForestBlock-org/RePackDocs)

## Requirements
Java 17 or later.

## Example usage
Firstly, compile ReRePack using maven. To compile a RePack workspace to a minecraft texture pack, you need to use the command line with the following command:
```bash
java -jar RePack.jar 'your/workspace/directory/filepath' 'output/directory/filepath'
```
Example:
```bash
java -jar RePack.jar 'forestblock-texture-pack-src' 'forestblock-pack'
```
The output directory will contain all of the required assets of your texture pack. Copy that output to your .minecraft/resourcepacks to see it in action.
