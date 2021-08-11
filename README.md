# NotifMod
A client-side Fabric mod that gives you notifications when certain things happen!
The notifications can be in the form of a sound, a message (chat/actionbar/title) or both.

The mod is highly configurable and currently supports the following events:
- Equipment durability low
- Unbreakable equipment stopped working (elytra)
- Equipment fully repaired (mending)
- Chat message
- Chat username mention
- Player joined server
- Player left server
- Game's done loading
- World's done loading (when initially connecting)

It can also start a timer and remind you when the time's up (useful for things like breeding).

## Dependencies
This mod depends on:
- Fabric API
- Cloth Config API
- Mod Menu (optional)

The mod will function without Mod Menu, but you won't be able to access the
in-game configuration screen, so it's recommended to include it.

## Notification sounds
The mod comes with a few simple sounds you can use if you want. However, you can use any sound
in the game, or even add your own, by replacing NotifMod's sounds using a simple resource pack!

Credits and licenses for the custom sounds included in this mod can be found in *src/main/resources/assets/notifmod/sounds/LICENSES-CREDITS.txt*