## MndroLua

A modern fork of [AndroLua+](https://github.com/nirenr/AndroLua_pro), a lua IDE for android app developing🌷

Chinese is the only language in this IDE because it's a hard work for me to add multi languages support :(

### Downloads

[Releases](https://github.com/MoonLeeeaf/MndroLua/releases)

### Modifies

See [this](https://github.com/Crescent-of-Maya/MndroLua/blob/main/app/src/main/assets/main.lua#L62) for more details (Chinese)🍉

### New features

<details>
  <summary>New features that were added in MndroLua</summary>
  
A modern and beautiful UI with night mode support:
![A materiel design 3 based UI, with code editor](https://github.com/user-attachments/assets/e47c6ad2-3166-425e-859f-dfa75d2a233d)

A better layout editor:
![A Dialog with opinions of a View](https://github.com/user-attachments/assets/892e5366-643a-49e8-b297-6c4f54ac288f)

New LuaEditor:
![A smarter code compiler that can tell you the variables](https://github.com/user-attachments/assets/b9cdb636-8755-4002-86cb-8a2f74a9ec3a)

AndroidManifest.xml  compiling support:
![Opened manifest.xml](https://github.com/user-attachments/assets/3fb6d589-989f-4d62-8fa8-53205bff9df4)
![You need a manifest.xml to compile a project](https://github.com/user-attachments/assets/bff35b38-25b4-436b-b86f-fcf124841f69)

New lua libraries:
```lua
-- Use any Java Class by full name
require 'javapackage'
local android = loadPackage('android')
print(android.os.Process.myPid())

-- Transplanted and Enchanted luajava.override
require 'javaoverride'
local NewTextView = luajava.override("android.widget.TextView", {
  setText = function(self, super, ...)
    local args = {...}
    args[1] = "ARGS: ".. dump(args)
    super(table.unpack(args))
  end
})
local t = NewTextView(this)
t.text = "Test114514"
activity.setContentView(t)
```

</details>

### License

MIT License (MndroLua, AndroLua+, AndroLua and LuaJava)

### Credits

🌷MndroLua
  - [AndroidXml2AXml](https://github.com/JealousCat/AndroidXml2AXml) (For compiling AndroidXML)
  - [JDom](https://github.com/hunterhacker/jdom) (For compiling AndroidXML)
  - [Zip4j](https://github.com/srikanth-lingala/zip4j) (For packing apk)
  - [LlamaLuaEditor](https://github.com/nwdxlgzs/LlamaLuaEditor) (For a better LuaEditor)
  - [AndroLua+](https://github.com/nirenr/AndroLua_pro) (The core)
    - [AndroLua](https://github.com/mkottman/AndroLua)
    - LuaJava

Although some projects were not listed here, all projects that were listed in build.gradle are used as libraries for AndroLua projects :)

### Authors

🌷MndroLua: GitHub [@MoonLeeeaf](https://github.com/MoonLeeeaf)
  - AndroLua+: GitHub [@nirenr](https://github.com/nirenr)
    - AndroLua: GitHub [@mkottman](https://github.com/mkottman)
    - LuaJava: @Kepler
