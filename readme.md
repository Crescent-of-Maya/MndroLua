## MndroLua

A modern fork of [AndroLua+](https://github.com/nirenr/AndroLua_pro), a lua IDE for android app developing🌷

Chinese is the only language because it's a hard work for me to add multi languages support :(

### Downloads

[Releases](https://github.com/MoonLeeeaf/MndroLua/releases)

### Modifies

App core:
  - Don't support Android 4.4 and under anymore (MinSdk = 21)
  - Resources reference should use full name such as `moon3.R.style.Moon3`
  - Crash-reports save at `/sdcard/Android/data/{packageName}/files/crash`
  - Use `moon3.R.style.Moon3` as the default app theme
  - Remove welcome.png (It's very outdated and uncomfortable)
  - Round icon support (icon_round.png)
  - Ignore `theme` in init.lua
  - Add `request_all_permissions_on_start` in init.lua (false in default, requests all permission at every `main.lua`)
  - Add callback function: onRequestPermissionsResult(requestCode, permissionList, grantedPermissionsList)

Java libraries:
  - Add [Shziuku-API](https://github.com/RikkaApps/Shizuku-API) (Tips: MinSdk = 24)
  - Add [HiddenApiByPass](https://github.com/LSPosed/AndroidHiddenApiBypass)
  - Add [LlamaLuaEditor](https://github.com/nwdxlgzs/LlamaLuaEditor) (Removed AI features, disabled syntax checking in default and renamed packageName to com.luaeditor2)
  - Add Moon3

Lua libraries:
  - Add javapackage.lua
  - Add javaoverride.lua

Components:
  - Used [LlamaLuaEditor](https://github.com/nwdxlgzs/LlamaLuaEditor) instead of LuaEditor and let `com.androlua.LuaEditor` extend `com.luaeditor2.LuaEditor`

Editor:
  - Don't request all permissions in default and only file permissions
  - Night mode support
  - Projects save at `/sdcard/Documents` as default
  - Android 11+ file permission support
  - Material Design 3 UI
  - New Code editor style
  - A better layout editor

### License

MIT License (MndroLua, AndroLua+, AndroLua and LuaJava)

### Credits

🌷MndroLua
  - [LlamaLuaEditor](https://github.com/nwdxlgzs/LlamaLuaEditor)
  - [AndroLua+](https://github.com/nirenr/AndroLua_pro)
    - [AndroLua](https://github.com/mkottman/AndroLua)
    - LuaJava

### Author

🌷MndroLua: GitHub @MoonLeeeaf
  - AndroLua+: GitHub @nirenr
    - AndroLua: GitHub @mkottman
    - LuaJava: @Kepler
