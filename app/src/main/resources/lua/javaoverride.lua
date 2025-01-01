-- luajava.override 移植
-- For MndroLua
-- Author: 满月叶

local Exception = luajava.bindClass("java.lang.Exception")
local LuaEnhancer = luajava.bindClass("com.androlua.LuaEnhancer")
local LuaObject = luajava.bindClass("com.luajava.LuaObject")

local MethodInterceptor = luajava.bindClass("com.android.cglib.proxy.MethodInterceptor")
local LuaMethodInterceptor = luajava.bindClass("com.luajava.LuaMethodInterceptor")
local LuaControlMethodInterceptor pcall(function()LuaControlMethodInterceptor = luajava.bindClass("com.luajava.LuaControlMethodInterceptor")end)

local String = luajava.bindClass("java.lang.String")

luajava.override = setmetatable({
  help = [[
    温馨提示
    
    0. 这只是一个移植, 不保证可用性
    1. 不能完全代替原版的 luajava.override, 而且写了很多废的代码()
    2. 在不同的 AndroLua 上, 会有不同的表现
       例如 AndLuaFrame 的LuaMethodInterceptor 是没有SuperCall的
       而 AndroLua+ 开源 是有的(别问我怎么知道的, MndroLua 用了Frame和官方开源代码缝合, 也是没有 SuperCall的)
       因此应该先像下面这样测试
       xxx = function(superCall, ...)
           print(superCall, ...) -- 如果superCall为null, 就说明只支持获取args和返回
       end
    3. 稳定性特别差, 因为不是Java层的实现, 如果错误调用会在C层 stackoverflow
       实际上是Java层的崩溃, 日志才是正确的
       而且会有莫名其妙的错误, 尤其是有生命周期的
       例如EditText啦, 什么的, 报错会非常莫名其妙
       会报一些(看似)无关紧要的东西如 length on null reference(空指针)
    4. 本库建议在 MndroLua 使用(因为有专门适配的Java代码, 能更全面的Override & super调用)
    5. 在 MndroLua 下, 默认启用增强模式
       参数为 (Object 自身, function 父类原方法, Object... 参数)
       至于为什么不给普通的AndroLua适配
       我也不知道 可能是因为有点麻烦吧
       其他环境使用默认模式
       参数不定, 看你所用的环境
    6. 支持传入Class的name
       支持直接构造和返回类构造
       直接构造: luajava.override(类名, 重写表, 构造参数...)
       返回类: luajava.override(类名, 重写表)
       为了让该功能正常使用, 如果不传入构造参数, 会返回元表
       这个表伪装成了 java.lang.Class, 而且可以直接实例化(返回真正的实例化对象)
       
    Author: 满月叶
    Github: MoonLeeeaf
    Date: 2024/12/29 13:29:01
  ]],
  -- 增强模式, 更加全面的魔改(self, super,  args...)
  -- 在MndroLua默认开启
  enhanced = not (LuaControlMethodInterceptor == nil),
}, {
  -- 参数: 类名(string / Class), 覆写方法表(table), [传入类构造参数](Object...)
  -- 若传入构造参数, 返回实例化的对象
  -- 否则返回伪类
  __call = function(self, cls, impl, ...)
    assert(type(cls) == "string" or type(cls) == "java.lang.Class", "arg #1 expect string or Class, got " .. tostring(cls))
    assert(type(impl) == "table", "arg #2 expect table, got " .. tostring(impl))
    -- 修补, 仅增强模式
    local impl = impl
    if self.enhanced then
      local _ = impl
      impl = {}
      for k, v in pairs(_) do
        impl[k] = function(obj, mp, args)
          local args = args and luajava.astable(args) or {}
          return xpcall(_[k],function(e)
            this.sendError("(Overridden) "..k, Exception(e))
            return nil
          end,
          obj, -- Object self
          function(...)
            xpcall(function(...)
              return mp.invokeSuper(obj, {...})
            end,function(e)
              this.sendError("(SuperCall) "..k, Exception(e))
            end,...)
          end, -- function super
          table.unpack(args)-- any... args
          )
        end
      end
    end
    -- 创建实例
    local newInstance = function(clazz, ...)
      local inst = clazz(...)
      -- Lua内的局限 有些时候需要用特殊方式
      -- 直接实例化会返回一个0数据的Array<>
      local interceptor = (self.enhanced and LuaControlMethodInterceptor or LuaMethodInterceptor).getConstructor({ LuaObject }).newInstance({ impl })
      -- 逆天特性
      -- 直接调用会expect A but got A
      -- 反射即可
      inst.getClass().getMethod("setMethodInterceptor_Enhancer", { MethodInterceptor }).invoke(inst, { interceptor })
      return inst
    end
    local clazz = LuaEnhancer(cls).create({
      -- 备注: final方法是不包括的
      -- 例如 TextView的setHint
      filter = function(method, name)
        return not (impl[name] == nil)
      end,
    })
    -- 直接创建
    if not (#({...}) == 0) then
      return newInstance(clazz, ...)
    end
    -- 返回伪类
    return setmetatable({}, {
      __index = function(self, k)
        return function(...)
          local a = {...}
          local cl = {}
          for _,v in pairs(a) do
            local t = v
            if type(v) == "string" then
              t = String
             elseif type(v) == "number" then
              local v = tostring(v)
              if v:find("%.") then
                t = float
               else
                t = int
              end
             else
              t = t.getClass()
            end
            table.insert(cl, t)
          end
          local m = clazz.getMethod(k, cl)
          return m.invoke((m.getModifiers() & 8) == 8 and nil or clazz, a)
        end
      end,
      __call = function(self, ...)
        return newInstance(clazz, ...)
      end,
      __tostring = function()
        return "伪类(" .. tostring(clazz)..")"
      end,
      __type = function()
        return clazz.getName()
      end
    })
  end
})
