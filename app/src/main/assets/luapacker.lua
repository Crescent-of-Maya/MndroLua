-- LuaPacker for MndroLua
-- GitHub @MoonLeeeaf<满月叶>
-- Date 2025.1.10

-- 签名
function signApk(apk)
  -- 不支持
end

-- 编译 Lua
function compileLua(src)
  io.open(src, "w"):write(string.dump(load(io.open(src, "r"):read("*a")))):close()
end

-- 编译 XML
function compileXML(from, to)
  local Files = luajava.bindClass("java.nio.file.Files")
  local Path = luajava.bindClass("java.nio.file.Path")
  local StandardOpenOption = luajava.bindClass("java.nio.file.StandardOpenOption")
  local AndroidXML = luajava.bindClass("io.github.moonleeeaf.androidxml.AndroidXML")
  local String = luajava.bindClass("java.lang.String")

  local xml = io.open(from, "r"):read("*a")
  Files.write(Path.of(to, {}),
  AndroidXML.encode(activity,xml),
  {StandardOpenOption.WRITE, StandardOpenOption.CREATE}
  )
end

-- 获取文件列表
local function getFilesList(dir, _list)
  local File = luajava.bindClass("java.io.File")

  local dir = File(dir)
  local list = _list or {}
  for k, v in pairs(luajava.astable(dir.listFiles())) do
    if v.isFile() then
      table.insert(list, v)
     elseif v.isDirectory() then
      getFilesList(v.getAbsolutePath(), list)
    end
  end
  return list
end

-- 打包项目
function pack(...)
  local args = {...}
  while #args < 2 do
    table.insert(args, '')
  end

  -- luaDir
  table.insert(args, luajava.luadir)
  -- getFilesList
  table.insert(args, getFilesList)
  -- compileLua
  table.insert(args, compileLua)
  -- compileXML
  table.insert(args, compileXML)
  -- signApk
  table.insert(args, signApk)
  -- loadDex
  table.insert(args, loadDex)

  LuaThread(activity, function(src, callbacks, luaDir, getFilesList, compileLua, compileXML, signApk, loadDex)
    xpcall(function()
      local File = luajava.bindClass("java.io.File")
      local ZipFile = luajava.bindClass("net.lingala.zip4j.ZipFile")
      local LuaUtil = luajava.bindClass("com.androlua.LuaUtil")
      local ProgressMonitor = luajava.bindClass("net.lingala.zip4j.progress.ProgressMonitor")
      local Thread = luajava.bindClass("java.lang.Thread")

      local callbacks = callbacks or {}
      local emptyFunction = function()end
      local beginCallback, updateCallback, endCallback = callbacks.onStart or emptyFunction, callbacks.onUpdate or emptyFunction, callbacks.onFinish or emptyFunction

      luajava.luadir = luaDir
      local rootTmpDir = luaDir .. "/.packer_tmp"
      local tmpDir = rootTmpDir .. "/resources"

      beginCallback()
      updateCallback("开始打包...")

      -- 清理缓存目录
      LuaUtil.rmDir(File(rootTmpDir))
      do
        -- 解压当前安装包作为运行时
        local apk = ZipFile(activity.getApplicationInfo().sourceDir)
        local pm = apk.getProgressMonitor()
        apk.setRunInThread(true)
        apk.extractAll(tmpDir)
        while not (pm.getState() == ProgressMonitor.State.READY) do
          Thread.sleep(100)
          updateCallback("解压程序运行时... (" .. tostring(pm.getPercentDone()) .. "%)")
        end
      end
      updateCallback("删除原资源文件...")
      -- 删除并替换资源文件
      LuaUtil.rmDir(File(tmpDir .. "/assets"))
      LuaUtil.rmDir(File(tmpDir .. "/META-INF"))
      LuaUtil.rmDir(File(tmpDir .. "/AndroidManifest.xml"))
      updateCallback("复制项目资源...")
      -- 复制资源文件
      LuaUtil.copyDir(src, tmpDir .. "/assets")

      -- 替换文件
      -- local from = v[1]
      -- local to = v[2]
      -- local fromIfNotFound = v[3]
      local replaces = {
        icon_file = {
          tmpDir .. "/assets/icon.png",
          tmpDir .. "/res/drawable/icon.png",
          tmpDir .. "/res/drawable/icon.png",
        },
        icon_round_file = {
          tmpDir .. "/assets/icon_round.png",
          tmpDir .. "/res/drawable/icon_round.png",
          tmpDir .. "/assets/icon_round.png",
        },
      }
      -- 编译文件
      -- 线程环境中的Table会变成 Java Object 要特别注意
      do
        updateCallback("编译Lua...")
        local fileList = luajava.astable(getFilesList(tmpDir))
        for k, v in pairs(fileList) do
          local v = v.getAbsolutePath()
          if (v:match("%.lua$") or v:match("%.aly$")) and not v:match("init.lua$") then
            compileLua(v)
          end
        end
      end

      -- 编译AndroidManifest.xml
      updateCallback("预处理 manifest.xml...")
      assert(File(tmpDir .. "/assets/manifest.xml").exists(), "缺失 manifest.xml, 请拷贝或创建一份有效的 AndroidManifest.xml 供编译使用!")
      do
        local init = {}
        loadfile(tmpDir .. "/assets/init.lua", "bt", init)()
        local content = io.open(tmpDir .. "/assets/manifest.xml", "r"):read("*a")
        content = content:gsub("%$%{%{(.-)%}%}", function(code)
          local _env = _G
          _env.init = init
          local f, e = load(code, nil, "bt", _env)
          if e then
            error("预处理 manifest.xml: 处理 " .. code .. "时出错! " .. e)
          end
          -- 最起码不能打回原型
          return f() or '0'
        end)
        -- io.write一定要close!!!!!一定要close!!!!!一定要close!!!!!
        io.open(tmpDir .. "/assets/manifest.xml", "w"):write(content):close()
      end
      updateCallback("编译 manifest.xml...")
      compileXML(tmpDir .. "/assets/manifest.xml", tmpDir .. "/AndroidManifest.xml")

      -- 替换文件
      updateCallback("替换文件中...")
      for k, v in pairs(replaces) do
        local from = v[1]
        local to = v[2]
        local fromIfNotFound = v[3]
        if File(from).exists() then
          LuaUtil.copyDir(from, to)
         elseif File(fromIfNotFound).exists()
          LuaUtil.copyDir(fromIfNotFound, to)
        end
      end

      -- 写出apk
      updateCallback("写出Apk...")
      do
        local apk = ZipFile(rootTmpDir.."/tmp.apk")
        local pm = apk.getProgressMonitor()
        apk.setRunInThread(true)
        local fileList = luajava.astable(File(tmpDir).listFiles())
        local i = 0
        for k, v in pairs(fileList) do
          i = i + 1
          if v.isFile() then
            apk.addFile(v)
           elseif v.isDirectory() then
            apk.addFolder(v)
          end
          while not (pm.getState() == ProgressMonitor.State.READY) do
            Thread.sleep(100)
            updateCallback("写出Apk... (" .. tostring(i) .. "/" .. tostring(#fileList) .. ", " .. tostring(pm.getPercentDone()) .. "%)")
          end
        end
      end

      updateCallback("正在签名...")
      signApk(rootTmpDir.."/tmp.apk")

      updateCallback("打包完毕")
      endCallback(rootTmpDir.."/tmp.apk")
    end, function(e) ;(callbacks.onError or function()end)(e) end)
  end, args).start()
end

return _G
