-----------------------------------------------------------------------------
-- Author: AndLua+ 陵阳
-----------------------------------------------------------------------------

import "java.io.*"

function loadGif(image,file)
  if type(image) == "string" then
    if File(image).isFile() then
      return LuaBitmapDrawable(activity,image)
     else
      return nil
    end
   else
    if File(file).isFile() then
      local load = true
      image.getViewTreeObserver().addOnGlobalLayoutListener{
        onGlobalLayout=function()
          if load then
            local bitmap = LuaBitmapDrawable(activity,file)
            local params = image.getLayoutParams()
            if image.Width==0 then
              local _size=loadbitmap(file)
              params.width = _size.getWidth()
              image.setLayoutParams(params)
              _size.recycle()
            end
            if image.Height==0 then
              local _size=loadbitmap(file)
              params.height = _size.getHeight()
              image.setLayoutParams(params)
              _size.recycle()
            end
            image.background=bitmap
          end
          load = false
        end
      }
    end
  end
end