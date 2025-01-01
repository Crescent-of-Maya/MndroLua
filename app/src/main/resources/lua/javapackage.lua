local String = luajava.bindClass("java.lang.String")

function loadPackage(packageName)
  return setmetatable({}, {
    __index = function(self, key)
      local c = packageName .. "." .. key
      return ({xpcall(luajava.bindClass, function()return loadPackage(packageName .. "." .. key) end, c)})[2]
    end,
    __call = function()
      local methodName = packageName:sub(String(packageName).lastIndexOf(".") + 2)
      error("Cannot find Method: " .. packageName:sub(0, packageName:find(methodName) - 2).."@"..methodName)
    end,
    __tostring = function()
      return "JavaPackage packageName="..packageName
    end,
  })
end
