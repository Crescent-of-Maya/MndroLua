require "import"
import "console"

import "moon3.widget.*"
import "moon3.utils.*"
import "moon3.app.*"

import "android.app.*"
import "android.os.*"
import "android.widget.*"
import "android.view.*"
import "android.content.*"
import "android.graphics.*"
import "com.androlua.*"
import "loadlayout3"
import "autotheme"
--activity.setTitle('XML转换器')
--activity.setTheme(autotheme())
activity.useDynamicColors()
cm=activity.getSystemService(Context.CLIPBOARD_SERVICE)
t={
  LinearLayout,
  id="l",
  orientation="vertical" ,
  --backgroundColor="#eeeeff",
  {
    LuaEditor,
    id="edit",
    --hint= "XML布局代码转换AndroLua布局表",
    layout_width="fill",
    layout_height="fill",
    layout_weight=1,
    --gravity="top"
  },
  {
    LinearLayout,
    layout_width="fill",
    --backgroundColor="#eeeeff",
    paddingTop="20px",
    paddingBottom="20px",
    paddingLeft="13px",
    paddingRight="13px",
    --[[{
      Button,
      id="open",
      text="转换",
      layout_width="fill",
      layout_weight=1,
      onClick ="click",
      marginLeft="7px",
      marginRight="7px",
    } ,]]
    {
      Button,
      id="open",
      text="预览",
      layout_width="fill",
      layout_weight=1,
      onClick ="click2",
      layout_marginLeft="7px",
      layout_marginRight="7px",
    } ,
    {
      Button,
      id="open",
      text="复制",
      layout_width="fill",
      layout_weight=1,
      onClick ="click3",
      layout_marginLeft="7px",
      layout_marginRight="7px",
    } ,
    {
      Button,
      id="open",
      text="确定",
      layout_width="fill",
      layout_weight=1,
      onClick ="click4",
      layout_marginLeft="7px",
      layout_marginRight="7px",
    } ,
  }
}

function xml2table(xml)
  local xml,s=xml:gsub("</%w+>","}")
  if s==0 then
    return xml
  end
  xml=xml:gsub("<%?[^<>]+%?>","")
  xml=xml:gsub("xmlns:android=%b\"\"","")
  xml=xml:gsub("%w+:","")
  xml=xml:gsub("\"([^\"]+)\"",function(s)return (string.format("\"%s\"",s:match("([^/]+)$")))end)
  xml=xml:gsub("[\t ]+","")
  xml=xml:gsub("\n+","\n")
  xml=xml:gsub("^\n",""):gsub("\n$","")
  xml=xml:gsub("<","{"):gsub("/>","}"):gsub(">",""):gsub("\n",",\n")
  return (xml)
end

dlg=Dialog(activity, luajava.bindClass("moon3.R$style").Moon3 )
dlg.setTitle("布局表预览")
function show(s)
  dlg.setContentView(loadlayout3(loadstring("return "..s)(),{}))
  dlg.show()
end

function click()
  local str=edit.getText().toString()
  str=xml2table(str)
  str=console.format(str)
  edit.setText(str)
end

function click2()
  local str=edit.getText().toString()
  show(str)
end


function click3(s)
  local cd = ClipData.newPlainText("label", edit.getText().toString())
  cm.setPrimaryClip(cd)
  FastToast.shortSnack(activity,"已复制到剪切板").show()
end

function click4()
  local str=edit.getText().toString()
  layout.main=loadstring("return "..str)()
  activity.setContentView(loadlayout2(layout.main,{}))
  dlg2.hide()

end


loadlayout(t)
dlg2=Dialog(activity, luajava.bindClass("moon3.R$style").Moon3)
dlg2.setTitle("编辑代码")
dlg2.getWindow().setSoftInputMode(0x10)

dlg2.setContentView(l)


function editlayout(txt)
  edit.Text=txt
  edit.format()
  dlg2.show()
end

function onResume2()
  local cd=cm.getPrimaryClip();
  local msg=cd.getItemAt(0).getText()--.toString();
  edit.setText(msg)
end

local 代码配色
if AndroidUtils.isNightMode(this) then
  --夜间配色
  代码配色={
    --BackgroundColor="#2b303b";
    TextColor="#ffffff";
    KeywordColor="#bb5f68";
    BasewordColor="#a3be8c";
    StringColor="#ebcb8b";
    CommentColor="#ab7967";
    UserwordColor="#a3be8c";
    TextHighlightColor="#ffff0097";
  }
 else
  --日间配色
  代码配色={
    TextColor="#333333";
    KeywordColor="#FF3F7FB5";
    BasewordColor="#FF6E81D9";
    StringColor="#FF2196F3";
    CommentColor="#FFA0A0A0";
    UserwordColor="#FF6E81D9";
    TextHighlightColor="#ffff0097";
  }
end

--自动换行
--editor.setWordWrap(true)
--自动缩进
edit.setAutoIndentWidth(2)
--隐藏行数
edit.setShowLineNumbers(true)
--字体大小
edit.setTextSize(42)

--配色
for k,v pairs(代码配色)
  edit[k]=Color.parseColor(v)
end

--自定义关键词
edit.addNames(String("").split(" "))
