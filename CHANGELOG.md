[1.0.3]

**Added**
* ru_ru translation (thanks to tefnya)
* zh_tw translation (thanks to cherrypuff1120)
* it_it translation (thanks to serenautilus)

**Fixed**
* The "get all pottery" challenge advancements (Ruby Row, etc.) now correctly require crafting every pottery variant of that clay color instead of completing as soon as the urn was crafted
* Garden Pot and Square Pot now accept regular flowers again, not just the 5 tall flower variants (also fixed a crash when placing a regular flower in them)
* Glazing an Urn or Amphore now actually renders a visible glossy sheen instead of doing nearly nothing 
* Picking up a glazed pot/urn now shows a "Glazed" tooltip line, matching the existing dye tooltip
* Museum Curator (get_everything) no longer requires the Lili's Statue, which was only obtainable as this advancement's own reward, making it impossible to complete
* Planters, Vases, Bud Vases, Cupellas, Plant Bowls, Garden Pots and Square Pots now implement `Clearable`, so contraption mods (e.g. Create/Sable) can properly empty their inventory before moving or destroying them, preventing an item duplication exploit
* Pottery Table and Lili's Pottery Table no longer spawn blue break/mining particles (the small water basin's tint index was reused by Minecraft as the block-wide particle color)

**Changed**
* The get_clay advancement now completes when obtaining any clay ball instead of requiring all variants

***

[1.0.2]

**Changed**	
* Honeycomb glazing is now blocked once glaze strength reaches 2.0.
* Pottery table now uses its own custom texture instead of the stonecutter texture.

***

[1.0.1]

**Fixed**
* Missing Zombie Villager texture for Clayworkers

***

[1.0.0+hotfix]

**Fixed**
* Missing Assets on NeoForge

***

[1.0.0]

**Initial Release**

Hey there and Welcome to Lili's Pottery!

***
